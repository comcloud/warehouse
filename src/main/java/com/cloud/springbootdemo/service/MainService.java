package com.cloud.springbootdemo.service;


import com.cloud.springbootdemo.bean.ObjectPair;
import com.cloud.springbootdemo.bean.UpdateProductDto;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @version v1.0
 * @ClassName MainService
 * @Author rayss
 * @Datetime 2021/12/26 7:55 下午
 */
public interface MainService {

    Workbook updateFile(InputStream fileStream);

    int updateUnitPrice(UpdateProductDto updateProductDto);
}
