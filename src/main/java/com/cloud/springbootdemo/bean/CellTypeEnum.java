package com.cloud.springbootdemo.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.util.Arrays;
import java.util.Objects;

/**
 * @version v1.0
 * @ClassName CellTypeEnum
 * @Author rayss
 * @Datetime 2024/7/31 9:13 AM
 */

@Getter
@AllArgsConstructor
public enum CellTypeEnum {

    _NONE("", -1) {
        @Override
        public String readCellValue(Cell cell) {
            return ""; // or handle null case
        }
    },
    NUMERIC("numeric", CellType.NUMERIC.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return String.valueOf(cell.getNumericCellValue());
        }
    },
    STRING("string", CellType.STRING.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return cell.getStringCellValue();
        }
    },
    FORMULA("", CellType.FORMULA.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return ""; // You can handle formula cells if needed
        }
    },
    BLANK("blank", CellType.BLANK.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return "";
        }
    },
    BOOLEAN("boolean", CellType.BOOLEAN.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return String.valueOf(cell.getBooleanCellValue());
        }
    },
    ERROR("error", CellType.ERROR.getCode()) {
        @Override
        public String readCellValue(Cell cell) {
            return ""; // You can handle error cells if needed
        }
    };

    private final String name;
    private final Integer value;

    // Abstract method to be implemented by each enum constant
    public abstract String readCellValue(Cell cell);

    public static CellTypeEnum ofCellType(CellType cellType) {
        if (Objects.isNull(cellType)) {
            return CellTypeEnum._NONE;
        }
        return Arrays.stream(CellTypeEnum.values())
                .filter(cellTypeEnum -> cellTypeEnum.value.equals(cellType.getCode()))
                .findFirst()
                .orElse(CellTypeEnum._NONE);
    }

}
