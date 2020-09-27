package com.yang.spring.mvc;

import com.yang.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 * @date 2020/9/13
 * @describe 动态参数适配器
 */
public class YHandlerAdapter {

    public YModelAndView getModelAndView(HttpServletRequest req, HttpServletResponse resp, YHandlerMapping handler) throws Exception{
        //形参列表：编译后就能拿到值

        //保存参数和位置的对应关系
        Map<String,Integer> paramIndexMapping = new HashMap<>();

        //提取加了RequestParam注解的参数的位置
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i ++){
            for (Annotation a : pa[i]) {
                if(a instanceof YRequestParam){
                    String paramName = ((YRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        //提取request和response的位置
        Class<?> [] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if(type == HttpServletRequest.class || type == HttpServletResponse.class){
                paramIndexMapping.put(type.getName(),i);
            }
        }


        //实参列表：要运行时才能拿到值
        Map<String,String[]> paramsMap = req.getParameterMap();
        //声明实参列表
        Object [] parameValues = new Object[paramTypes.length];
        for (Map.Entry<String,String[]> param : paramsMap.entrySet()) {
            String value = Arrays.toString(paramsMap.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s","");
            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());
            parameValues[index] = caseStringVlaue(value,paramTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            parameValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            parameValues[index] = resp;
        }


        Object result = handler.getMethod().invoke(handler.getInstance(),parameValues);

        //方法返回值为空or方法返回值为void
        if(result == null || result instanceof Void){return null;}

        //判断method返回值是不是YModelAndView
        boolean isModelAndView = handler.getMethod().getReturnType() == YModelAndView.class;
        if (isModelAndView){
            return (YModelAndView)result;
        }
        return null;
    }

    private Object caseStringVlaue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }
        if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }
    }
}
