package com.logdemo.test.web;

import com.logdemo.core.annotation.LogOperation;
import com.logdemo.core.log.LogPackageHolder;
import com.logdemo.core.log.LogVariableKey;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * @Author lsw
 * @Date 2023/5/6 14:17
 */
@Service
public class TestService {

    @LogOperation(value = "TestService的测试方法")
    public String Test(String A, String B){
        return A + " ===  " + B;
    }


    private HttpHeaders createHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentTraceToken());
        httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentOrder().toString());
        return httpHeaders;
    }

}
