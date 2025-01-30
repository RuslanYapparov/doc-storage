package ru.yappy.docstorage;

import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class DocStorageServer {
    private static final int PORT = 8080;

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.getConnector().setPort(PORT);
        Context tomcatContext = tomcat.addContext("/api", new File(".").getAbsolutePath());

        AnnotationConfigWebApplicationContext applicationContext =
                new AnnotationConfigWebApplicationContext();
        applicationContext.setServletContext(tomcatContext.getServletContext());
        applicationContext.scan("ru.yappy.docstorage");
        applicationContext.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        Wrapper dispatcherWrapper =
                Tomcat.addServlet(tomcatContext, "DocStorageDispatcherServlet", dispatcherServlet);
        dispatcherWrapper.addMapping("/");
        dispatcherWrapper.setLoadOnStartup(1);

        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("springSecurityFilterChain");
        filterDef.setFilterClass(DelegatingFilterProxy.class.getName());
        tomcatContext.addFilterDef(filterDef);

        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("springSecurityFilterChain");
        filterMap.addURLPattern("/*");
        tomcatContext.addFilterMap(filterMap);

        tomcat.start();
    }

}