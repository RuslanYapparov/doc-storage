package ru.yappy.docstorage.in;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BaseController {
    private final ResourceLoader resourceLoader;

    @Autowired
    public BaseController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @GetMapping
    public String greetingPage() {
        return "HI!";
    }

    @GetMapping(value = "/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> serveStaticFile(@PathVariable(name = "fileName") String fileName) {
        String filePath = "classpath:static/" + fileName;
        Resource resource = resourceLoader.getResource(filePath);
        if (!resource.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(resource);
    }

}