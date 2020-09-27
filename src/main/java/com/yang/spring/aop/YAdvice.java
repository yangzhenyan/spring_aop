package com.yang.spring.aop;

import java.lang.reflect.Method;

/**
 * @author yzy
 * @date 2020/9/21
 * @describe 通知回调 主要封装切面以及切面需要回调的方法
 */
public class YAdvice {

    private Object aspect;
    private Method adviceMethod;
    private String exceptionName; //拿到异常的名字再决定要不要回调

    public YAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }

    public Object getAspect() {
        return aspect;
    }

    public void setAspect(Object aspect) {
        this.aspect = aspect;
    }

    public Method getAdviceMethod() {
        return adviceMethod;
    }

    public void setAdviceMethod(Method adviceMethod) {
        this.adviceMethod = adviceMethod;
    }

    public String getExceptionName() {
        return exceptionName;
    }

    public void setExceptionName(String exceptionName) {
        this.exceptionName = exceptionName;
    }
}
