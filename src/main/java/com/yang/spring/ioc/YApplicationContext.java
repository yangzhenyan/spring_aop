package com.yang.spring.ioc;

import com.yang.annotation.YAutowired;
import com.yang.annotation.YController;
import com.yang.annotation.YService;
import com.yang.spring.aop.YAdvisedSupport;
import com.yang.spring.aop.YAopConfig;
import com.yang.spring.aop.YJdkDynamicAopProxy;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author yzy
 * @date 2020/9/7
 * @describe 所有ioc容器都是通过ApplicationContext入口获取
 */
public class YApplicationContext {

    private String[] configLocations;

    private YBeanDefinitionReader reader;

    private Map<String, YBeanDefinition> definitionsMap = new HashMap<>();

    private Map<String, YBeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    //二级缓存
    private Map<String, Object> factoryObjectInstanceCache = new HashMap<>();

    public YApplicationContext(String... configLocation) {
        this.configLocations = configLocation;
        try {
            //1、读取配置文件
            reader = new YBeanDefinitionReader(configLocations[0]);
            List<YBeanDefinition> beanDefinitionList = reader.doLoadBeanDefinitions();

            //2、将definitions存放入definitionsMap集合中
            doRegistryBeanDefinition(beanDefinitionList);

            //3、实例化创建对象，循环调用getBean (有些对象不是延迟加载的)
            doCreateBean();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void doCreateBean() {
        //实例化对象
        for (Map.Entry<String, YBeanDefinition> entry : definitionsMap.entrySet()) {
            String beanName = entry.getKey();
            getBean(beanName);
        }
    }

    private void doRegistryBeanDefinition(List<YBeanDefinition> beanDefinitionList) throws Exception {
        for (YBeanDefinition beanDefinition : beanDefinitionList) {
            //默认单例
            if (definitionsMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The" + beanDefinition + "is exist !");
            }
            definitionsMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            definitionsMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    public Object getBean(String beanName) {
        YBeanDefinition beanDefinition = definitionsMap.get(beanName);
        //实例化对象
        Object instance = instanceBean(beanDefinition);

        //将对象封装成beanWrapper
        YBeanWrapper wrapper = new YBeanWrapper(instance);

        //将wrapper缓存到ioc中
        factoryBeanInstanceCache.put(beanName, wrapper);

        //完成DI注入
        populateBean(beanName, wrapper);
        return wrapper.getWrapperInstance();
    }

    private void populateBean(String beanName, YBeanWrapper wrapper) {

        Object wrapperInstance = wrapper.getWrapperInstance();
        Class<?> wrapperClass = wrapper.getWrapperClass();
        //判断是不是加了@YService或@YController注解
        if (!wrapperClass.isAnnotationPresent(YService.class) & !wrapperClass.isAnnotationPresent(YController.class)) {
            return;
        }
        //获取对象中的属性
        for (Field field : wrapperClass.getDeclaredFields()) {
            //如果加了@YAutowired注解就给属性赋值
            if (field.isAnnotationPresent(YAutowired.class)) {

                //暴力访问
                field.setAccessible(true);
                try {
                    String name = field.getType().getName();
                    field.set(wrapperInstance, factoryBeanInstanceCache.get(name).getWrapperInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;

                }
            }

        }
    }

    private Object instanceBean(YBeanDefinition beanDefinition) {
        Object instance = null;
        try {
            Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
            instance = aClass.newInstance();

            //AOP
            //封装配置类信息、对象、类对象
            YAdvisedSupport config = instanceAopConfig(beanDefinition);
            config.setTarget(instance);
            config.setTargetClass(aClass);
            //判断是否需要生成代理类
            if(config.pintCutMatch()){
                instance = new YJdkDynamicAopProxy(config).getProxy();
            }
            factoryObjectInstanceCache.put(beanDefinition.getFactoryBeanName(), instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    //aop封装配置文件信息
    private YAdvisedSupport instanceAopConfig(YBeanDefinition beanDefinition) {
        YAopConfig aopConfig = new YAopConfig();
        aopConfig.setPointCut(reader.getConfig().getProperty("pointCut"));
        aopConfig.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        aopConfig.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        aopConfig.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        aopConfig.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        aopConfig.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));
        return new YAdvisedSupport(aopConfig);
    }

    public Object getBean(Class<?> className) {
        return getBean(className.getName());
    }

    //获取容器中注册多少bean
    public int getBeanDefinitionCount() {
        return definitionsMap.size();
    }

    //获取容器中注册bean的名字
    public String[] getBeanDefinitionNames() {
        Collection<YBeanDefinition> definitions = definitionsMap.values();
        definitions.toArray(new String[definitionsMap.size()]);
        String[] toArray = (String[]) definitions.toArray();
        Set<String> keySet = definitionsMap.keySet();
        String[] array = keySet.toArray(new String[definitionsMap.size()]);
        return array;
    }

    //获取页面模板路径
    public String getHtmlConfig() {
        return reader.getConfig().getProperty("templateRoot");
    }
}
