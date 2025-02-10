package ru.yappy.docstorage.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;

public interface FileManager {

    Path saveFile(MultipartFile file) throws IOException;

    void updateFile(MultipartFile file, Path docPath) throws IOException;

    void deleteFile(Path docPath) throws IOException;

}