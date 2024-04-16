package com.dhu.utils;

import com.dhu.exception.FileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class BaseUtils {
    public static String getEncoding(String str) {
        String encode = "ISO-8859-1";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是ISO-8859-1
                String s1 = encode;
                return s1;
            }
        } catch (Exception exception1) {
        }
        encode = "UTF-8";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {   //判断是不是UTF-8
                String s2 = encode;
                return s2;
            }
        } catch (Exception exception2) {
        }
        encode = "GBK";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GBK
                String s3 = encode;
                return s3;
            }
        } catch (Exception exception3) {
        }
        encode = "GB2312";
        try {
            if (str.equals(new String(str.getBytes(encode), encode))) {      //判断是不是GB2312
                String s = encode;
                return s;      //是的话，返回“GB2312“，以下代码同理
            }
        } catch (Exception exception) {
        }

        return "";        //如果都不是，说明输入的内容不属于常见的编码格式
    }

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
                    builder.append((char) ('a' + random.nextInt(26)));
                } else {
                    builder.append((char) ('A' + random.nextInt(26)));
                }
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    //String 转 Boolean
    public static Boolean toBoolean(String str) {
        if ("null".equals(str)) {
            return null;
        } else {
            return Boolean.parseBoolean(str);
        }
    }
}
