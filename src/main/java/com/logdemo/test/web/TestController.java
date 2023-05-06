package com.logdemo.test.web;

import com.logdemo.core.annotation.LogOperation;
import com.logdemo.core.annotation.MethodDesc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author lsw
 * @Date 2023/5/6 13:21
 */
@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private TestService testService;

    @MethodDesc(value = "查询接口", note = "此接口用于测试使用，非正式接口",tags = {"123465","()90999090909090（）（）)"})
    // @LogOperation(value = "测试API接口", skipSql = true)
    @GetMapping("/getMethod")
    public String getMethod(){
        return "你好!";
    }

    @LogOperation("post方法")
    @PostMapping("/postMethod")
    public String postMethod(@RequestParam String A, @RequestParam String B){
        return testService.Test(A, B);
    }

}
