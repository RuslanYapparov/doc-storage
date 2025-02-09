package ru.yappy.docstorage;

import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.*;
import java.nio.file.*;

public class DocStorageServer {
    private static final int PORT = 8080;
    private static final String TMP_FOLDER = "C:/Projects/doc-storage/src/main/resources/tmp";
    private static final int MAX_UPLOAD_SIZE = 5 * 1024 * 1024;

    public static void main(String[] args) throws LifecycleException, IOException {
        createTmpFolder();
        Tomcat tomcat = new Tomcat();
        tomcat.getConnector().setPort(PORT);
        tomcat.getConnector().setURIEncoding("UTF-8");
        Context tomcatContext = tomcat.addContext("", new File(".").getAbsolutePath());

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
        dispatcherWrapper.setMultipartConfigElement(new MultipartConfigElement(TMP_FOLDER, MAX_UPLOAD_SIZE,
                MAX_UPLOAD_SIZE * 2L, MAX_UPLOAD_SIZE));

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

    private static void createTmpFolder() throws IOException {
        Path directoryPath = Paths.get(TMP_FOLDER);
        if (!Files.exists(directoryPath.toAbsolutePath())) {
            Files.createDirectory(directoryPath.toAbsolutePath());
        }
    }

}