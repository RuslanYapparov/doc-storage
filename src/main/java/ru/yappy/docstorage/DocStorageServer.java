package ru.yappy.docstorage;

import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.*;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.*;
import java.nio.file.*;

public class DocStorageServer {
    private static final int PORT = 8080;
    private static final String TMP_FOLDER = "src/main/resources/tmp";
    private static final int MAX_UPLOAD_SIZE = 5 * 1024 * 1024;

    public static void main(String[] args) throws LifecycleException, IOException {
        createTmpFolder();
        Tomcat tomcat = new Tomcat();
        tomcat.getConnector().setPort(PORT);
        tomcat.getConnector().setURIEncoding("UTF-8");
        tomcat.getConnector().setProperty("relaxedQueryChars", "[]{}|");
        tomcat.getConnector().setProperty("useBodyEncodingForURI", "true");
        Context tomcatContext = tomcat.addContext("", new File(".").getAbsolutePath());

        AnnotationConfigWebApplicationContext applicationContext =
                new AnnotationConfigWebApplicationContext();
        applicationContext.setServletContext(tomcatContext.getServletContext());
        applicationContext.scan("ru.yappy.docstorage");
        applicationContext.refresh();

        DispatcherServlet dispatcherServlet = new DispatcherServlet(applicationContext);
        dispatcherServlet.setDispatchOptionsRequest(true);
        Wrapper dispatcherWrapper =
                Tomcat.addServlet(tomcatContext, "DocStorageDispatcherServlet", dispatcherServlet);
        dispatcherWrapper.addMapping("/");
        dispatcherWrapper.setLoadOnStartup(1);
        dispatcherWrapper.setMultipartConfigElement(new MultipartConfigElement(TMP_FOLDER, MAX_UPLOAD_SIZE,
                MAX_UPLOAD_SIZE * 2L, MAX_UPLOAD_SIZE));

        addFilterDefs(tomcatContext);

        tomcat.start();
    }

    private static void createTmpFolder() throws IOException {
        Path directoryPath = Paths.get(TMP_FOLDER);
        if (!Files.exists(directoryPath.toAbsolutePath())) {
            Files.createDirectory(directoryPath.toAbsolutePath());
        }
    }

    private static void addFilterDefs(Context tomcatContext) {
        FilterDef characterEncodingFilterDef = new FilterDef();
        characterEncodingFilterDef.setFilterName("characterEncodingFilter");
        characterEncodingFilterDef.setFilterClass(CharacterEncodingFilter.class.getName());
        characterEncodingFilterDef.addInitParameter("encoding", "UTF-8");
        characterEncodingFilterDef.addInitParameter("forceEncoding", "true");
        tomcatContext.addFilterDef(characterEncodingFilterDef);

        FilterMap characterEncodingFilterMap = new FilterMap();
        characterEncodingFilterMap.setFilterName("characterEncodingFilter");
        characterEncodingFilterMap.addURLPattern("/*");
        tomcatContext.addFilterMap(characterEncodingFilterMap);

        FilterDef securityFilterDef = new FilterDef();
        securityFilterDef.setFilterName("springSecurityFilterChain");
        securityFilterDef.setFilterClass(DelegatingFilterProxy.class.getName());
        tomcatContext.addFilterDef(securityFilterDef);

        FilterMap securityFilterMap = new FilterMap();
        securityFilterMap.setFilterName("springSecurityFilterChain");
        securityFilterMap.addURLPattern("/*");
        tomcatContext.addFilterMap(securityFilterMap);
    }

}