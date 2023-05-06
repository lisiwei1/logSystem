package com.logdemo.core.annotation;

import java.lang.annotation.*;

/**
 * 自定义测试注解
 * @Author lsw
 * @Date 2023/5/6 13:06
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodDesc {

    // 方法说明
    String value() ;

    String note() default "";

    String[] tags() default "";

}
