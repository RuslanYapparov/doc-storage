package ru.yappy.docstorage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

public interface FileManager {

    Path saveFile(MultipartFile file) throws IOException;

    InputStream getDocumentInputStream(Path docPath) throws IOException;

}