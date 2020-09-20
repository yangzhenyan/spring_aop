package com.yang.spring.mvc;

import java.util.Map;

/**
 * @author yzy
 * @date 2020/9/13
 * @describe
 */
public class YModelAndView {

    private String viewName;

    private Map<String, ?> model;

    public YModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public YModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
