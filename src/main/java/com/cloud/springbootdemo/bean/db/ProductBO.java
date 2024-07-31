package com.cloud.springbootdemo.bean.db;

import lombok.*;

import java.io.Serializable;

/**
 * (Product)实体类
 *
 * @author makejava
 * @since 2024-07-28 15:17:11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ProductBO implements Serializable {
    private static final long serialVersionUID = 430749128501583013L;

    private Integer id;

    private String name;

    private String unit;

    private String stock;

    private String unitPrice;


}

