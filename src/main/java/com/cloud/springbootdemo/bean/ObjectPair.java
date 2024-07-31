package com.cloud.springbootdemo.bean;

import lombok.*;

/**
 * @version v1.0
 * @ClassName ResultPair
 * @Author rayss
 * @Datetime 2024/7/27 11:30 PM
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ObjectPair<T, U>{

    private T firstValue;

    private U secondValue;

}
