package com.yang.spring.mvc;

import com.yang.annotation.YController;
import com.yang.annotation.YRequestMapping;
import com.yang.spring.ioc.YApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author yzy
 * @date 2020/8/23
 * @describe
 */
public class YzyDispatcherServlet extends HttpServlet {

    private List<YHandlerMapping> handlerMappingList = new ArrayList<>();

    /**
     * 一个HandlerMapping对应一个HandlerAdapter(动态参数适配器)
     */
    private Map<YHandlerMapping, YHandlerAdapter> adapterMap = new HashMap<>();

    /**
     * 每个模板对应一个视图解析引擎
     */
    private List<YViewResovler> viewResovlers = new ArrayList<>();

    private YApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDisPath(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String,Object> model = new HashMap<>();
            model.put("detail","doDisPath detail 500 Exception!!");
            model.put("stackTrace",Arrays.toString(e.getStackTrace()));
            try {
                processDispatchResult(req,resp,new YModelAndView("500",model));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doDisPath(HttpServletRequest req, HttpServletResponse resp) throws Exception{
        //1、根据uri拿到对应的handleMapping
        YHandlerMapping handleMapping = getHandleMapping(req);

        //处理返回结果
        if (handleMapping == null) {
            processDispatchResult(req, resp, new YModelAndView("404"));
            return;
        }
        //2、根据拿到的handleMapping获取对应的HandlerAdapter
        YHandlerAdapter ha = getHandlerAdapter(handleMapping);

        //3、根据handlerAdapter动态参数匹配并拿到ModelAndView
        YModelAndView mv = ha.getModelAndView(req, resp, handleMapping);
        //4、根据ModelAndView决定用哪个ViewResovler去解析
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, YModelAndView mv) throws Exception{
        if (mv == null){
            return;
        }
        if (this.viewResovlers.isEmpty()){
            return;
        }
        for (YViewResovler viewResovler : viewResovlers){
            YView view = viewResovler.resolveViewName(mv.getViewName());
            //进行页面渲染
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    private YHandlerAdapter getHandlerAdapter(YHandlerMapping handleMapping) {
        if (adapterMap.isEmpty()){
            return null;
        }
        return adapterMap.get(handleMapping);
    }

    private YHandlerMapping getHandleMapping(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        //获取项目名 "/项目名"
        String contextPath = req.getContextPath();
        //获取请求的uri
        String uri = requestURI.replace(contextPath, "");

        for (YHandlerMapping handlerMapping : handlerMappingList) {
            if (uri.equals(handlerMapping.getUrl())) {
                return handlerMapping;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        /*---------------------------------IoC------------------------------*/
        //读取配置文件
        context = new YApplicationContext(config.getInitParameter("myConfigLocation"));

        /*----------------------------------MVC-----------------------------*/
        //初始化mvc九大组件 我们这里实现其中3个
        initStrategies(context);
        System.out.println("Spring Framework init......");
    }

    private void initStrategies(YApplicationContext context) {

        //保存Url映射关系
        initHandlerMapping(context);
        //动态参数适配器
        initHandlerAdapter(context);
        //视图转换器，模板引擎
        initViewResolvers(context);
    }

    private void initViewResolvers(YApplicationContext context) {
        //一个页面对应一个视图解析器
        String templateRoot = context.getConfig();
        URL resource = this.getClass().getClassLoader().getResource(templateRoot);

        File files = new File(resource.getFile());
        for (File file : files.listFiles()) {
            viewResovlers.add(new YViewResovler(templateRoot));
        }
    }

    private void initHandlerAdapter(YApplicationContext context) {
        for (YHandlerMapping handlerMapping : handlerMappingList) {
            adapterMap.put(handlerMapping, new YHandlerAdapter());
        }
    }

    private void initHandlerMapping(YApplicationContext context) {
        if (context.getBeanDefinitionCount() == 0) {
            return;
        }
        String[] beanNames = context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object bean = context.getBean(beanName);

            Class<?> aClass = bean.getClass();
            if (!aClass.isAnnotationPresent(YController.class)) {
                continue;
            }
            //判断类路径上是否有@YRequestMapping注解 有就获取
            String baseUrl = null;
            if (aClass.isAnnotationPresent(YRequestMapping.class)) {
                baseUrl = aClass.getAnnotation(YRequestMapping.class).value();
            }
            //判断方法上@YRequestMapping注解
            Method[] methods = aClass.getMethods();
            String methodUrl = null;
            for (Method method : methods) {
                if (method.isAnnotationPresent(YRequestMapping.class)) {
                    methodUrl = method.getAnnotation(YRequestMapping.class).value();
                    String uri = "/" + baseUrl + "/" + methodUrl;
                    //url method放入集合
                    handlerMappingList.add(new YHandlerMapping(uri, method, bean));

                }
            }

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
