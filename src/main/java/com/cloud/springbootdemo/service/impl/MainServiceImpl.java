package com.cloud.springbootdemo.service.impl;

import com.cloud.springbootdemo.assembler.ProductAssembler;
import com.cloud.springbootdemo.bean.ObjectPair;
import com.cloud.springbootdemo.bean.Product;
import com.cloud.springbootdemo.bean.UpdateProductDto;
import com.cloud.springbootdemo.bean.db.ProductBO;
import com.cloud.springbootdemo.mapper.MainMapper;
import com.cloud.springbootdemo.service.MainService;
import com.cloud.springbootdemo.util.Lists;
import com.cloud.springbootdemo.util.OfficeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @version v1.0
 * @ClassName MainServiceImpl
 * @Author rayss
 * @Datetime 2021/12/26 8:08 下午
 */
@Service
@Slf4j
public class MainServiceImpl implements MainService {


    @Resource
    private MainMapper mainMapper;


    /**
     * 修改后的workbook, sheetName对应的所有数据库中没有的产品名和数据库中存在的产品但是没有单价，这里拼接后返回
     */
    @Override
    public Workbook updateFile(InputStream fileStream) {
        /*
         * 1. 获取流中的所有sheet中的产品
         * 2. 根据产品查询数据库，获取单价
         * 3. 将单价数据转移到workbook中
         * 4. 合并其他所有sheet中的产品数据到一个新sheet中
         * */
        log.info("------------新的excel处理记录------------");
        ObjectPair<Map<String, List<Product>>, Workbook> objectPair = OfficeUtil.readExcel(fileStream);
        log.info("读取excel表成功");
        Map<String, ObjectPair<List<String>, List<String>>> sheetName2NotHaveProductMap = objectPair.getFirstValue()
                .entrySet().stream().map(sheetName2Products -> {
                    Map<String, String> name2unit =
                            sheetName2Products.getValue().stream().collect(Collectors.toMap(Product::getName, Product::getUnit));
                    List<ProductBO> productBOList = mainMapper.queryProductByNameAndUnit(name2unit.keySet(), name2unit.values());
                    //可能存在出库单有，但是数据库中没有对应的product
                    // 按照产品名+单位做两个集合的差集
                    ObjectPair<List<String>, List<String>> notHaveProduct = ProductAssembler.fillProductUnitPrice(productBOList, sheetName2Products.getValue());
                    sheetName2Products.setValue(sheetName2Products.getValue());
                    ObjectPair.ObjectPairBuilder<String, ObjectPair<List<String>, List<String>>> sheetName2NotHaveProductBuilder = ObjectPair.builder();
                    return sheetName2NotHaveProductBuilder.firstValue(sheetName2Products.getKey()).secondValue(notHaveProduct).build();
                }).collect(Collectors.toMap(ObjectPair::getFirstValue, ObjectPair::getSecondValue));
        log.info("成功补充每个院的单价与金额");

        // transfer
        OfficeUtil.transferDataToWorkbook(objectPair);

        //combine
        Workbook workbook = OfficeUtil.combineAllSheetToNewSheet(objectPair);
        log.info("新增总出库表成功");
        //补充额外的异常信息
        List<String> promptInfoList = combineResult(sheetName2NotHaveProductMap);
        Sheet sheet = workbook.getSheet(OfficeUtil.ALL_SHEET_NAME);
        for (String promptInfo : promptInfoList) {
            sheet.createRow(sheet.getLastRowNum() + 1).createCell(0).setCellValue(promptInfo);
        }
        log.info("校验数据异常问题成功");

        return workbook;
    }

    @Override
    public int updateUnitPrice(UpdateProductDto updateProductDto) {
        return mainMapper.updateUnitPriceByNameAndUnit(ProductAssembler.toProductBO(updateProductDto));
    }

    /**
     * 封装的结果格式如下
     * 更新文件过程中，发现异常数据，分别如下：
     * 1. 出库单具有的产品，但是总表中不存在，请及时补充到总表中，异常数据位于：
     * sheetName1, name+unit
     * sheetName2, name+unit
     * sheetName3, name+unit
     * 2. 出库单具有的产品，总表中存在，但是总表中没有记录单价，请及时补充到总表中 : 异常数据位于：
     * sheetName1, name+unit
     * sheetName2, name+unit
     * sheetName3, name+unit
     */
    private List<String> combineResult(Map<String, ObjectPair<List<String>, List<String>>> sheetName2NotHaveProductMap) {
        StringBuilder promptInfo1 = new StringBuilder();
        StringBuilder promptInfo2 = new StringBuilder();

        Map<String, List<String>> sheetName2ProductNameNotInDBMap = new HashMap<>();
        Map<String, List<String>> sheetName2ProductNameNotHaveUnitPriceInDBMap = new HashMap<>();
        sheetName2NotHaveProductMap.forEach((sheetName, objectPair) -> {
            List<String> productNameNotInDB = objectPair.getFirstValue();
            List<String> productNameNotHaveUnitPriceInDB = objectPair.getSecondValue();
            if (CollectionUtils.isNotEmpty(productNameNotInDB)) {
                sheetName2ProductNameNotInDBMap.put(sheetName, productNameNotInDB);
            }
            if (CollectionUtils.isNotEmpty(productNameNotHaveUnitPriceInDB)) {
                sheetName2ProductNameNotHaveUnitPriceInDBMap.put(sheetName, productNameNotHaveUnitPriceInDB);
            }
        });

        if (MapUtils.isNotEmpty(sheetName2ProductNameNotInDBMap)) {
            promptInfo1.append("出库单具有的产品，但是总表中不存在，请及时补充到总表中，异常数据位于：\n");
            for (Map.Entry<String, List<String>> sheetName2ProductNameNotInDB : sheetName2ProductNameNotInDBMap.entrySet()) {
                promptInfo1.append(sheetName2ProductNameNotInDB.getKey()).append(":").append(sheetName2ProductNameNotInDB.getValue()).append('\n');
            }
        }

        if (MapUtils.isNotEmpty(sheetName2ProductNameNotHaveUnitPriceInDBMap)) {
            promptInfo2.append("出库单具有的产品，总表中存在，但是总表中没有记录单价，请及时补充到总表中 : 异常数据位于：\n");
            for (Map.Entry<String, List<String>> sheetName2ProductNameNotHaveUnitPriceInDB : sheetName2ProductNameNotHaveUnitPriceInDBMap.entrySet()) {
                promptInfo2.append(sheetName2ProductNameNotHaveUnitPriceInDB.getKey()).append(":").append(sheetName2ProductNameNotHaveUnitPriceInDB.getValue()).append('\n');
            }
        }

        List<String> result = Lists.newArraryList();
        if (StringUtils.isNotEmpty(promptInfo1.toString())) {
            result.add(promptInfo1.toString());
        }
        if (StringUtils.isNotEmpty(promptInfo2.toString())) {
            result.add(promptInfo2.toString());
        }
        return result;
    }
}
