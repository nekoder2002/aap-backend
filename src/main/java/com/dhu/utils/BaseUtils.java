package com.dhu.utils;

import com.dhu.exception.FileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BaseUtils {
    //MultipartFile 转 File
    public static File toFile(MultipartFile multipartFile) {
        //选择用缓冲区来实现这个转换即使用java 创建的临时文件
        File file = null;
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            //获取文件后缀
            String prefix = originalFilename.substring(originalFilename.lastIndexOf("."));
            file = File.createTempFile(originalFilename, prefix);    //创建临时文件
            multipartFile.transferTo(file);
            //删除
            file.deleteOnExit();
        } catch (IOException e) {
            throw new FileException("服务器转为临时文件失败");
        }
        return file;
    }

    //生成随机码
    public static String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                if (random.nextBoolean()) {
                    builder.append((char)('a' + random.nextInt(26)));
                } else {
                    builder.append((char)('A' + random.nextInt(26)));
                }
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }
}
