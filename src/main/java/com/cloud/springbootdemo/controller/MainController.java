package com.cloud.springbootdemo.controller;

import com.cloud.springbootdemo.assembler.ProductAssembler;
import com.cloud.springbootdemo.service.MainService;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;

/**
 * @version v1.0
 * @ClassName MainController
 * @Author rayss
 * @Datetime 2021/12/26 7:33 下午
 */
@RestController
public class MainController {


    @Resource
    private MainService mainService;

    @SneakyThrows
    @PostMapping("/updateFile")
    public ResponseEntity<byte[]> updateFile(MultipartFile file) {
        Workbook workbook = mainService.updateFile(file.getInputStream());

        // 将工作簿写入输出流
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=example.xlsx");

        // 将Excel文件作为字节数组发送回客户端
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

    @PostMapping("/updateUnitPrice")
    public ResponseEntity<String> updateUnitPrice(@RequestParam("mingcheng") String name,
                                                  @RequestParam("danwei") String unit,
                                                  @RequestParam("danjia") String unitPrice) {
        int res = mainService.updateUnitPrice(ProductAssembler.toUpdateProductDto(name, unit, unitPrice));
        return res == 1 ? ResponseEntity.ok().body("修改成功") : ResponseEntity.badRequest().body("修改失败");

    }

}
