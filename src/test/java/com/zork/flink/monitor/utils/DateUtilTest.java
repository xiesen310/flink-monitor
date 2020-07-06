package com.zork.flink.monitor.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author xiese
 * @Description TODO
 * @Email xiesen310@163.com
 * @Date 2020/7/5 17:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DateUtilTest {

    @Test
    public void formatDateTime() {
//        long mss = 4382884;
        long mss = 5021699;
        String ss = DateUtil.formatDateTime(mss);
        System.out.println(ss);
    }
}
