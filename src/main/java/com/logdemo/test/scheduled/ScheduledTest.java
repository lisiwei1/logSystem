package com.logdemo.test.scheduled;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Author lsw
 * @Date 2023/5/6 13:59
 */
@Component
public class ScheduledTest {

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void testCron01() {
        System.out.println("测试调用任务");
    }

}
