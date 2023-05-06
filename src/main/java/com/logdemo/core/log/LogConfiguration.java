package com.logdemo.core.log;

import java.lang.annotation.Annotation;

/**
 * 设置方法描述的自定义注解，比如可以设置为swagger的@ApiOperation
 * @Author lsw
 * @Date 2023/5/6 13:14
 */
public class LogConfiguration<T extends Annotation> {

    private Class<T> annotationClass;

    public Class<T> getAnnotationClass() {
        return annotationClass;
    }

    public void setAnnotationClass(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

}
