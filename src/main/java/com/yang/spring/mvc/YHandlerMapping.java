package com.yang.spring.mvc;

import java.lang.reflect.Method;

/**
 * @author yzy
 * @date 2020/9/11
 * @describe 保存Url映射关系
 */
public class YHandlerMapping {
    private String url;
    private Method method;
    private Object instance;//Method对应的controller类的对象 每次通过反射去获取的时候 比较消耗性能 所以封装存储起来

    public YHandlerMapping(String url, Method method, Object instance) {
        this.url = url;
        this.method = method;
        this.instance = instance;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}
