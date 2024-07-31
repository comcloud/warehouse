package com.cloud.springbootdemo.bean;

import lombok.*;

/**
 * @version v1.0
 * @ClassName UpdateProductDto
 * @Author rayss
 * @Datetime 2024/7/30 11:12 AM
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UpdateProductDto {

    private String name;

    private String unit;

    private String stock;

    private String unitPrice;

}
