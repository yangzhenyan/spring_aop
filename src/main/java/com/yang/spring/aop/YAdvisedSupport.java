package com.yang.spring.aop;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yzy
 * @date 2020/9/21
 * @describe 读取aop配置文件
 */
public class YAdvisedSupport {

    private YAopConfig aopConfig;
    private Class targetClass;
    private Object target;
    private Pattern pointCutClassPattern;
    private Map<Method,Map<String,YAdvice>> methodCache;

    public YAdvisedSupport(YAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public YAopConfig getAopConfig() {
        return aopConfig;
    }

    public void setAopConfig(YAopConfig aopConfig) {
        this.aopConfig = aopConfig;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public boolean pintCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    private void parse() {

        //对配置文件中的特殊字符进行转义
        String pointCut = aopConfig.getPointCut()
                .replaceAll("\\.","\\\\.")
                .replaceAll("\\\\.\\*",".*")
                .replaceAll("\\(","\\\\(")
                .replaceAll("\\)","\\\\)");

        String pointCutForClassRegex = pointCut.substring(0,pointCut.lastIndexOf("\\(") - 4);
        //提取class的全名
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        try {
            //开始映射目标类方法和通知的关系
            methodCache = new HashMap<>();

            //开始匹配目标类的方法
            Pattern pointCutPattern = Pattern.compile(pointCut);

            //先把要织入的切面的方法缓存起来
            Class aspectClass = Class.forName(this.aopConfig.getAspectClass());
            Map<String,Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(),method);
            }

            //扫描目标类的所有的方法
            for (Method method : this.targetClass.getMethods()) {
                //包括了修饰符、返回值、方法名、形参列表
                String methodString = method.toString();
                //把异常去掉
                if(methodString.contains("throws")){
                    methodString = methodString.substring(0,methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                if(matcher.matches()){
                    Map<String,YAdvice> advices = new HashMap<>();

                    //前置通知
                    if(!(null == aopConfig.getAspectBefore() || "".equals(aopConfig.getAspectBefore()))){
                        advices.put("before",
                                new YAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectBefore())));
                    }

                    //后置通知
                    if(!(null == aopConfig.getAspectAfter() || "".equals(aopConfig.getAspectAfter()))){
                        advices.put("after",
                                new YAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectAfter())));
                    }

                    //异常通知
                    if(!(null == aopConfig.getAspectAfterThrow() || "".equals(aopConfig.getAspectAfterThrow()))){
                        advices.put("afterThrowing",
                                new YAdvice(aspectClass.newInstance(),aspectMethods.get(aopConfig.getAspectAfterThrow())));
                    }

                    methodCache.put(method,advices);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
