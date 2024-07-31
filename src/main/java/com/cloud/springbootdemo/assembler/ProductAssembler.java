package com.cloud.springbootdemo.assembler;

import com.cloud.springbootdemo.bean.ObjectPair;
import com.cloud.springbootdemo.bean.Product;
import com.cloud.springbootdemo.bean.UpdateProductDto;
import com.cloud.springbootdemo.bean.db.ProductBO;
import com.cloud.springbootdemo.util.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @version v1.0
 * @ClassName ProductAssembler
 * @Author rayss
 * @Datetime 2024/7/28 3:22 PM
 */

public class ProductAssembler {

    public static ObjectPair<List<String>, List<String>> fillProductUnitPrice(List<ProductBO> productBOList, List<Product> productList) {
        if (CollectionUtils.isEmpty(productBOList)) {
            return new ObjectPair<>();
        }
        Map<String, ProductBO> nameUnitToProductBOMap = productBOList.stream()
                .collect(Collectors.toMap(productBO -> productBO.getName() + productBO.getUnit(), Function.identity()));

        //记录数据库中没有的产品以及记录数据库中存在的产品，但是该产品没单价
        List<String> notHaveUnitPriceProductName = Lists.newArraryList();
        List<String> notHaveProductName = Lists.newArraryList();

        //以传入的出库单为主，从ProductBO进行查找
        for (Product product : productList) {
            //按照name+unit查找
            ProductBO productBO = nameUnitToProductBOMap.get(product.getName() + product.getUnit());
            if (Objects.nonNull(productBO)) {
                if (StringUtils.isNotEmpty(productBO.getUnitPrice())) {
                    product.setUnitPrice(productBO.getUnitPrice());
                } else {
                    //没有单价
                    notHaveUnitPriceProductName.add(product.getName() + "(" + product.getUnit() + ")");
                }
            } else {
                //没有该产品
                notHaveProductName.add(product.getName() + "(" + product.getUnit() + ")");
            }
        }

        ObjectPair.ObjectPairBuilder<List<String>, List<String>> builder = ObjectPair.builder();
        return builder.firstValue(notHaveProductName).secondValue(notHaveUnitPriceProductName).build();
    }


    public static UpdateProductDto toUpdateProductDto(String name, String unit, String unitPrice) {
        return UpdateProductDto.builder()
                .name(name)
                .unit(unit)
                .unitPrice(unitPrice)
                .build();
    }

    public static ProductBO toProductBO(UpdateProductDto updateProductDto) {
        if (Objects.isNull(updateProductDto)) {
            return null;
        }
        return ProductBO.builder()
                .name(updateProductDto.getName())
                .stock(updateProductDto.getStock())
                .unit(updateProductDto.getUnit())
                .unitPrice(updateProductDto.getUnitPrice())
                .build();
    }
}
