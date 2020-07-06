package com.zork.flink.monitor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * @author xiese
 * @Description 读取任务配置文件
 * @Email xiesen310@163.com
 * @Date 2020/7/5 16:03
 */
public class ReadConfig {
    private static final Logger logger = LoggerFactory.getLogger(ReadConfig.class);

    public static String read(String path) {
        String content = null;
        try {
            File file = new File(getCPResourcePath(path));
            String path1 = file.getAbsolutePath();
            File file1 = new File(path1);
            FileInputStream in = new FileInputStream(file1);
            byte[] fileByte = new byte[(int) file1.length()];
            in.read(fileByte);
            content = new String(fileByte, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error("读取配置文件失败", e.getCause());
        }
        return content;
    }

    public static String getCPResourcePath(String name) {
        if (!"/".equals(File.separator)) {
            return name.replaceAll(Pattern.quote(File.separator), "/");
        }
        return name;
    }


}
