package com.yang.spring.mvc;

import java.io.File;

/**
 * @author yzy
 * @date 2020/9/13
 * @describe
 */
public class YViewResovler {

    private File file;

    private static String SUFFIX_TEMPALTE = ".html";

    public YViewResovler(String templateRoot) {
        String filePath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        file = new File(filePath);
    }

    public YView resolveViewName(String viewName) {
        viewName = viewName.endsWith(SUFFIX_TEMPALTE) ? viewName : viewName + SUFFIX_TEMPALTE;
        File viewFile = new File(this.file.getPath() + "/" + viewName);
        return new YView(viewFile);
    }

}
