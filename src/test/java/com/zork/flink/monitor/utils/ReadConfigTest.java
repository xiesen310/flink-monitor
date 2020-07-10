package com.zork.flink.monitor.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiese
 * @Description TODO
 * @Email xiesen310@163.com
 * @Date 2020/7/5 16:11
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReadConfigTest {

    @Test
    public void readResource() {
        try {
            String path = this.getClass().getResource("/streamx/empty.conf").getPath();
            String read = ReadConfig.read(path);
            System.out.println(read);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
