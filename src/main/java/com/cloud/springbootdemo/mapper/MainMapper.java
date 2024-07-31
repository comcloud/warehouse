package com.cloud.springbootdemo.mapper;

import com.cloud.springbootdemo.bean.db.ProductBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @version v1.0
 * @ClassName MainMapper
 * @Author rayss
 * @Datetime 2024/7/27 11:52 PM
 */
@Mapper
public interface MainMapper {

    List<ProductBO> queryProductByNameAndUnit(@Param("names") Set<String> names, @Param("units") Collection<String> units);

    int updateUnitPriceByNameAndUnit(ProductBO productBO);
}
