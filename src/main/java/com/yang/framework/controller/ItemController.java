package com.yang.framework.controller;

import com.yang.annotation.YAutowired;
import com.yang.annotation.YController;
import com.yang.annotation.YRequestMapping;
import com.yang.annotation.YRequestParam;
import com.yang.framework.service.ItemService;
import com.yang.spring.mvc.YModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yzy
 * @date 2020/8/26
 * @describe
 */
@YController
@YRequestMapping("item")
public class ItemController {
    @YAutowired
    private ItemService itemService;

    private ItemService itemService1;

    //查询
    @YRequestMapping("query")
    public YModelAndView query(HttpServletRequest request, HttpServletResponse response,
                               @YRequestParam("name") String name, @YRequestParam("id") String id) {
        YModelAndView mv = new YModelAndView("first");
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("data", "我是data");
        map.put("token", id);
        mv.setModel(map);
        return mv;
    }

    //添加
    @YRequestMapping("add")
    public void add(HttpServletRequest request, HttpServletResponse response, @YRequestParam("name") String name) {
        try {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().write("增加name：" + name);
        } catch (IOException e) {
            e.getCause().getMessage();
        }
    }

    //删除
    @YRequestMapping("remove")
    public void remove(HttpServletRequest request, HttpServletResponse response, @YRequestParam("name") String name) {
        System.out.println("remove方法");
    }
}
