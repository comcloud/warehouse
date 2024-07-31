package com.cloud.springbootdemo.util;

import com.cloud.springbootdemo.bean.CellTypeEnum;
import com.cloud.springbootdemo.bean.Product;
import com.cloud.springbootdemo.bean.ObjectPair;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 1. readExcel : 获取上传的excel文件内容，此时只有每个院的sheet，以及该sheet下的名称、单位、数量
 * 2. transferDataToWorkbook ： 传入数据库中存入的产品结果，将其迁移到Workbook中
 * 3. combineAllSheetToNewSheet ： 合并所有sheet到一个新的sheet中作为汇总
 *
 * @version v1.0
 * @ClassName OfficeUtil
 * @Author rayss
 * @Datetime 2024/7/27 6:44 PM
 */

public class OfficeUtil {


    public static final String ALL_SHEET_NAME = "总出库单";

    /**
     * 读取每一个sheet中的每个结果对象和workbook
     * sheetName : product1, product2 ... product n
     */
    @SneakyThrows
    public static ObjectPair<Map<String, List<Product>>, Workbook> readExcel(InputStream inputStream) {
        Map<String, List<Product>> sheetName2ProductsMap = new HashMap<>();
        Workbook workbook = WorkbookFactory.create(inputStream);

        int numberOfSheets = workbook.getNumberOfSheets();

        // 遍历每个 sheet
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String sheetName = sheet.getSheetName();

            //遍历每个行
            for (int j = 4; j <= sheet.getLastRowNum(); j++) {
                Row row = sheet.getRow(j);
                //cell值为空，则跳过该sheet
                if (Objects.isNull(row.getCell(0)) || StringUtils.isEmpty(readCellValue(row.getCell(0)))) {
                    continue;
                }
                Product product = Product.builder()
                        .name(readCellValue(row.getCell(0)))
                        .unit(readCellValue(row.getCell(1)))
                        .quantity(readCellValue(row.getCell(2)))
                        .build();
                sheetName2ProductsMap.compute(sheetName, (key, oldValue) -> {
                    if (Objects.isNull(oldValue)) {
                        return Lists.newArraryList(product);
                    }
                    oldValue.add(product);
                    return oldValue;
                });
            }

        }
        return new ObjectPair<>(sheetName2ProductsMap, workbook);
    }

    public static void transferDataToWorkbook(ObjectPair<Map<String, List<Product>>, Workbook> objectPair) {
        Map<String, List<Product>> sheetName2ProductsMap = objectPair.getFirstValue();
        Workbook workbook = objectPair.getSecondValue();
        // 获取每个条目的单价和金额列索引
        int priceColumnIndex = 3; // 单价所在列索引（从0开始）
        int amountColumnIndex = 4; // 金额所在列索引（从0开始）
        sheetName2ProductsMap.forEach((sheetName, products) -> {
            Sheet sheet = workbook.getSheet(sheetName);

            // 遍历每一行（前四行是标题，从第五行开始是数据行）
            for (int rowIndex = 4, listIndex = 0; listIndex < products.size(); rowIndex++, listIndex++) {
                Row row = sheet.getRow(rowIndex);
                Product product = products.get(listIndex);

                if (Objects.nonNull(row)) {
                    Cell priceCell = row.getCell(priceColumnIndex);
                    if (Objects.isNull(priceCell)) {
                        priceCell = row.createCell(priceColumnIndex);
                    }
                    priceCell.setCellValue(product.getUnitPrice());

                    Cell amountCell = row.getCell(amountColumnIndex);
                    if (Objects.isNull(amountCell)) {
                        amountCell = row.createCell(amountColumnIndex);
                    }
                    amountCell.setCellValue(product.getAmount());
                }
            }

        });

    }

    public static Workbook combineAllSheetToNewSheet(ObjectPair<Map<String, List<Product>>, Workbook> objectPair) {
        Map<String, List<Product>> sheetName2ProductsMap = objectPair.getFirstValue();
        Workbook workbook = objectPair.getSecondValue();
        /*
         * 1. 创建新sheet模版
         * 2. 补充该sheet中的数据
         *   循环其他sheet，直接新增row，然后按照名称+单位排序，再进行合并
         * */
        Sheet allSheetTemplate = createAllSheetTemplate(workbook);

        List<Product> products = reduceProductMap(sheetName2ProductsMap);

        for (int i = 0; i < products.size(); i++) {
            Row row = allSheetTemplate.createRow(i + 4);
            copyProductToRow(products.get(i), row);
        }

        return workbook;
    }

    /**
     * 归一所有sheet的数据到一个list中，其中包含合并相同产品的逻辑
     */
    private static List<Product> reduceProductMap(Map<String, List<Product>> sheetName2ProductsMap) {
        // 合并所有 List<Product> 到一个单独的 List<Product>
        List<Product> mergedList = sheetName2ProductsMap.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 根据 Product 的 equals 方法合并数量和金额数据
        return combineProducts(mergedList);
    }

    // 合并 Product 的方法
    private static List<Product> combineProducts(List<Product> products) {
        Map<Product, Product> combinedMap = new HashMap<>();

        // 遍历所有产品，合并数量和金额
        for (Product product : products) {
            if (combinedMap.containsKey(product)) {
                Product existingProduct = combinedMap.get(product);
                existingProduct.setQuantity(String.valueOf(Double.parseDouble(existingProduct.getQuantity()) + Double.parseDouble(product.getQuantity())));
            } else {
                combinedMap.put(product, new Product(product)); // 使用 Product 的拷贝构造函数
            }
        }

        // 转换回 List<Product>
        return new ArrayList<>(combinedMap.values());
    }


    /**
     * 返回新增的sheet
     */
    private static Sheet createAllSheetTemplate(Workbook workbook) {
        /*
         * 1. 创建sheet
         * 2. 创建合并单元格的标题
         * 3. 创建产品、单位等标题
         * */
        Sheet resultFromAllSheet = workbook.createSheet("总出库单");

        // 合并前三行和前五列的单元格（从第0行到第2行，从第0列到第4列）
        CellRangeAddress mergedRegion = new CellRangeAddress(0, 2, 0, 4);
        resultFromAllSheet.addMergedRegion(mergedRegion);
        // 获取合并后的单元格
        Row mergedRow = resultFromAllSheet.getRow(0); // 假设起始行是第0行
        if (mergedRow == null) {
            mergedRow = resultFromAllSheet.createRow(0);
        }
        Cell mergedCell = mergedRow.getCell(0); // 假设起始列是第0列
        if (mergedCell == null) {
            mergedCell = mergedRow.createCell(0);
        }

        // 设置单元格内容
        mergedCell.setCellValue(ALL_SHEET_NAME);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER); // 设置居中对齐
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER); // 设置垂直居中对齐
        mergedCell.setCellStyle(cellStyle);

        //获取sheet_0的标题
        Sheet sheet0 = workbook.getSheetAt(0);
        Row rowSheet0 = sheet0.getRow(3);
        Row newRow = resultFromAllSheet.createRow(3);
        copyRowToNewRow(rowSheet0, newRow);
        // done
        return resultFromAllSheet;
    }

    private static void copyRowToNewRow(Row sourceRow, Row targetRow) {
        for (int i = 0; i < sourceRow.getPhysicalNumberOfCells(); i++) {
            Cell cell = targetRow.createCell(i, sourceRow.getCell(i).getCellType());
            cell.setCellValue(readCellValue(sourceRow.getCell(i)));
        }
    }

    private static void copyProductToRow(Product product, Row row) {
        int i = 0;
        for (String att : product) {
            Cell cell = row.createCell(i++);
            cell.setCellValue(att);
        }
    }


    public static void exportExcel(Workbook workbook) {
        String newExcelFilePath = "/Users/rayss/Desktop/file.xlsx"; // 新 Excel 文件路径

        // 创建一个新的 Workbook 对象
        try (FileOutputStream fos = new FileOutputStream(newExcelFilePath)) {
            workbook.write(fos);
            System.out.println("New Excel file created successfully: " + newExcelFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        String excelFilePath = "/Users/rayss/Desktop/demo.xlsx"; // Excel 文件路径

        String newExcelFilePath = "/Users/rayss/Desktop/file.xlsx"; // 新 Excel 文件路径


        try (FileInputStream fis = new FileInputStream(excelFilePath);

             Workbook workbook = WorkbookFactory.create(fis)) {

            exportExcel(workbook);


            int numberOfSheets = workbook.getNumberOfSheets();


            // 遍历每个 sheet
            for (int i = 0; i < numberOfSheets; i++) {
                Sheet sheet = workbook.getSheetAt(i);
                System.out.println("Reading sheet: " + sheet.getSheetName());

                // 手动填充单价和金额示例
                fillPriceAndAmount(sheet);

            }

//            // 创建一个新的 Workbook 对象
//            try (FileOutputStream fos = new FileOutputStream(newExcelFilePath)) {
//                workbook.write(fos);
//                System.out.println("New Excel file created successfully: " + newExcelFilePath);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 手动填充单价和金额的方法
    private static void fillPriceAndAmount(Sheet sheet) {
        // 获取每个条目的单价和金额列索引
        int priceColumnIndex = 3; // 单价所在列索引（从0开始）
        int amountColumnIndex = 4; // 金额所在列索引（从0开始）

        // 遍历每一行（假设第一行是标题，从第二行开始是数据行）
        for (int rowIndex = 4; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                Cell priceCell = row.getCell(priceColumnIndex);
                if (priceCell == null) {
                    priceCell = row.createCell(priceColumnIndex);
                }
                priceCell.setCellValue(10.0); // 假设单价是10.0

                Cell amountCell = row.getCell(amountColumnIndex);
                if (amountCell == null) {
                    amountCell = row.createCell(amountColumnIndex);
                }
                // 假设金额是单价乘以数量
                Cell quantityCell = row.getCell(2); // 数量所在列索引
                double quantity = Double.parseDouble(readCellValue(quantityCell)); // 假设数量是数值类型
                double price = Double.parseDouble(readCellValue(priceCell)); // 假设单价是数值类型
                double amount = quantity * price;
                amountCell.setCellValue(amount);
            }
        }
    }


    /**
     * 读取cell中的数据，统一返回字符串类型
     */
    private static String readCellValue(Cell cell) {
        if (Objects.isNull(cell)) {
            return null;
        }
        CellTypeEnum cellTypeEnum = CellTypeEnum.ofCellType(cell.getCellType());
        return cellTypeEnum.readCellValue(cell);

    }
}
