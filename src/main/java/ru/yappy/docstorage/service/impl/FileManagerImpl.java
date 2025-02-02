package ru.yappy.docstorage.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.service.FileManager;

import java.io.*;
import java.nio.file.*;

@Slf4j
@Service
public class FileManagerImpl implements FileManager {
    private final Path baseDirPath;

    @Autowired
    public FileManagerImpl(Environment environment) {
        this.baseDirPath = Paths.get(environment.getRequiredProperty("doc.file.storage.directory")).toAbsolutePath();
    }

    @PostConstruct
    public void init() throws IOException {
        if (Files.exists(baseDirPath)) {
            log.info("Обнаружена корневая директория для хранения файлов документов. " +
                    "Создание системы каталогов не требуется.");
        } else {
            log.info("Корневая директория для хранения файлов документов не обнаружена. " +
                    "Начало операции создания системы каталогов.");
            Files.createDirectory(baseDirPath);
            createDirectoryIfNotExists(baseDirPath.resolve("0-9"));
            createDirectoryIfNotExists(baseDirPath.resolve("rus"));
            createDirectoryIfNotExists(baseDirPath.resolve("eng"));
            createDirectoryIfNotExists(baseDirPath.resolve("other"));
            Path engPath = baseDirPath.resolve("eng");
            for (char c = 'A'; c <= 'Z'; c++) {
                createDirectoryIfNotExists(engPath.resolve(String.valueOf(c)));
            }
            Path rusPath = baseDirPath.resolve("rus");
            for (char c = 'А'; c <= 'Я'; c++) {
                createDirectoryIfNotExists(rusPath.resolve(String.valueOf(c)));
            }
            log.info("Корневая директория с системой каталогов для хранения файлов документов успешно создана.");
        }
    }

    @Override
    public Path saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        log.debug("Начало операции сохранения файла документа '{}'", fileName);
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("Невозможно прочитать название загруженного файла.");
        }
        Path filePath = generateFilePath(fileName);
        file.transferTo(filePath);
        log.debug("Файл документа успешно сохранен в хранилище. Path='{}'", filePath);
        return filePath;
    }

    @Override
    public InputStream getDocumentInputStream(Path docPath) throws IOException {
        log.debug("Начало операции получения Input-потока данных файла документа по пути '{}'.", docPath);
        if (docPath == null || Files.notExists(docPath)) {
            throw new IllegalStateException("В ходе выполнения операции произошла ошибка: " +
                    "не был сохранен путь к файлу, либо файл документа не найден.");
        }
        InputStream docInputStream = Files.newInputStream(docPath, StandardOpenOption.READ);
        log.debug("Input-поток данных файла документа успешно получен.");
        return docInputStream;
    }

    private Path generateFilePath(String fileName) {
        Path filePath;
        char firstChar = Character.toUpperCase(fileName.charAt(0));
        if (firstChar >= '0' && firstChar <= '9') {
            filePath = baseDirPath.resolve("0-9").resolve(fileName);
        } else if (firstChar >= 'А' && firstChar <= 'Я') {
            filePath = baseDirPath.resolve("rus").resolve(String.valueOf(firstChar)).resolve(fileName);
        } else if (firstChar >= 'A' && firstChar <= 'Z') {
            filePath = baseDirPath.resolve("eng").resolve(String.valueOf(firstChar)).resolve(fileName);
        } else {
            filePath = baseDirPath.resolve("other").resolve(fileName);
        }
        if (Files.exists(filePath)) {
            filePath = generateNewFilePathIfExists(filePath);
        }
        return filePath;
    }

    private Path generateNewFilePathIfExists(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String baseName;
        String extension;

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            baseName = fileName;
            extension = "";
        } else {
            baseName = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int counter = 1;
        Path newFilePath;
        do {
            newFilePath = filePath.getParent().resolve(baseName + " (" + counter + ")" + extension);
            counter++;
        } while (Files.exists(newFilePath));
        return newFilePath;
    }

    private void createDirectoryIfNotExists(Path path) throws IOException {
        if (Files.notExists(path)) {
            Files.createDirectory(path);
        }
    }

}