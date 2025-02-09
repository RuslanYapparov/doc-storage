package ru.yappy.docstorage.in;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.net.URLConnection;

@Controller
public class BaseController {

    private final ResourceLoader resourceLoader;

    @Autowired
    public BaseController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping
    public String greetingPage() {
        return "index.html";
    }

    @GetMapping("/**")
    public ResponseEntity<Resource> serveStaticFile(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String resourcePath = "classpath:static" + requestPath;
        Resource resource = resourceLoader.getResource(resourcePath);
        if (!resource.exists() || !resource.isReadable()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        String mimeType = URLConnection.guessContentTypeFromName(resource.getFilename());
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mimeType)
                .body(resource);
    }

}