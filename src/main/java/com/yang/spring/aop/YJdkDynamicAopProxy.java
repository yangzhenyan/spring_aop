package com.yang.spring.aop;

/**
 * @author yzy
 * @date 2020/9/21
 * @describe 动态代理 完成代码逻辑
 */
public class YJdkDynamicAopProxy {

    private YAdvisedSupport config;

    public YJdkDynamicAopProxy(YAdvisedSupport config) {
        this.config = config;
    }

    public Object getProxy() {
        return null;
    }
}
