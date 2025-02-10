package ru.yappy.docstorage.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.config.*;
import ru.yappy.docstorage.service.impl.FileManagerImpl;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { TestPersistenceConfig.class, TestConfig.class, FileManagerUnitTest.Config.class })
@ActiveProfiles("test")
public class FileManagerUnitTest {
    private final FileManager fileManager;

    @Autowired
    public FileManagerUnitTest(FileManager fileManager) {
        this.fileManager = fileManager;
    }

    @Test
    void postConstructInit_onBeanInitialization_createsBaseDirectoryForFileStoraging()
            throws IOException {
        long countedNumberOfCatalogsInBaseDirectory = 63L;
        Path path = Paths.get("src/test/resources/filestorage");
        assertTrue(Files.exists(path));
        assertTrue(Files.isDirectory(path));
        try (Stream<Path> paths = Files.walk(Paths.get("src/test/resources/filestorage/"))) {
            assertThat(paths.count()).isEqualTo(countedNumberOfCatalogsInBaseDirectory);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!шашлык", "шаурма", "3 котлеты", "sausage" })
    void saveFile_whenGetCorrectMultipartFiles_thenSavesItInStorage(String fileName) throws IOException {
        Path path = Paths.get("src/test/resources/filestorage/" + fileName);
        if (Files.notExists(path)) {
            Files.createFile(path);
        }
        MultipartFile file =
                new MockMultipartFile(fileName, fileName, null, new FileInputStream(path.toString()));
        Path newSavedFilePath = fileManager.saveFile(file);
        boolean isMatched = Stream.of(
                        Paths.get("src/test/resources/filestorage/other/!шашлык"),
                        Paths.get("src/test/resources/filestorage/rus/Ш/шаурма"),
                        Paths.get("src/test/resources/filestorage/0-9/3 котлеты"),
                        Paths.get("src/test/resources/filestorage/eng/S/sausage"))
                .anyMatch(filePath -> newSavedFilePath.toString().contains(filePath.toString()));

        assertThat(isMatched).isTrue();
    }

    @Test
    void saveFile_whenGetMultipartFileWithNullOrBlankFileName_thenThrowIllegalArgumentException()
            throws IOException {
        String exceptionMessage = "Невозможно прочитать название загруженного файла.";
        Path path = Paths.get("src/test/resources/MockFile");
        MultipartFile file = new MockMultipartFile("шаурма", null, null,
                new FileInputStream(path.toString()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fileManager.saveFile(file));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);

        MultipartFile anotherFile = new MockMultipartFile("шаурма", "\t\r", null,
                new FileInputStream(path.toString()));

        exception = assertThrows(IllegalArgumentException.class,
                () -> fileManager.saveFile(anotherFile));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    @Test
    void updateFile_whenGetCorrectMultipartFileAndDocPath_thenUpdatesFileInDirectory()
        throws IOException {
        Path path = Paths.get("src/test/resources/MockFile");
        MultipartFile file = new MockMultipartFile("фрикаделька", "MockFile",
                null, new FileInputStream(path.toString()));
        fileManager.saveFile(file);
        Path savedFilePath = Paths.get("src/test/resources/filestorage/eng/M/MockFile");

        assertThat(Files.exists(savedFilePath)).isTrue();
        assertDoesNotThrow(() -> fileManager.updateFile(file, savedFilePath));

        long numberOfSavedFilesInDirectory;
        try (Stream<Path> paths = Files.walk(savedFilePath.getParent())) {
            numberOfSavedFilesInDirectory = paths
                    .filter(dirPath -> !savedFilePath.getParent().equals(dirPath))
                    .count();
        }

        assertThat(numberOfSavedFilesInDirectory).isEqualTo(1);

        Files.deleteIfExists(savedFilePath);
    }

    @Test
    void updateFile_whenGetFileWithNullOrBlankOrIncorrectFileName_thenThrowIllegalArgumentException()
        throws IOException {
        String exceptionMessage = "Невозможно обновить файл документа. Невозможно прочитать название " +
                "загруженного файла либо название файла не соответствует названию файла в хранилище.";
        Path path = Paths.get("src/test/resources/MockFile");

        MultipartFile file = new MockMultipartFile("фрикаделька", null, null,
                new FileInputStream(path.toString()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fileManager.updateFile(file, path));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);

        MultipartFile anotherFile = new MockMultipartFile("фрикаделька", "\t\r",
                null, new FileInputStream(path.toString()));

        exception = assertThrows(IllegalArgumentException.class,
                () -> fileManager.updateFile(anotherFile, path));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);

        MultipartFile anotherAnotherFile = new MockMultipartFile("фрикаделька", "фрикаделька",
                null, new FileInputStream(path.toString()));

        exception = assertThrows(IllegalArgumentException.class,
                () -> fileManager.updateFile(anotherAnotherFile, path));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    @Test
    void deleteFile_whenGetCorrectMultipartFileAndDocPath_thenDeleteFileInDirectory()
            throws IOException {
        Path path = Paths.get("src/test/resources/MockFile");
        MultipartFile file = new MockMultipartFile("колбаска", "MockFile",
                null, new FileInputStream(path.toString()));
        fileManager.saveFile(file);
        Path savedFilePath = Paths.get("src/test/resources/filestorage/eng/M/MockFile");

        assertThat(Files.exists(savedFilePath)).isTrue();
        assertDoesNotThrow(() -> fileManager.deleteFile(savedFilePath));

        long numberOfSavedFilesInDirectory;
        try (Stream<Path> paths = Files.walk(savedFilePath.getParent())) {
            numberOfSavedFilesInDirectory = paths
                    .filter(dirPath -> !savedFilePath.getParent().equals(dirPath))
                    .count();
        }

        assertThat(numberOfSavedFilesInDirectory).isEqualTo(0);
    }

    @Test
    void deleteFile_whenGetNullOrIncorrectDocPath_thenThrowIllegalStateException() {
        String exceptionMessage = "При удалении файла документа произошла ошибка: " +
                "не был сохранен путь к файлу, либо файл документа не найден.";

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> fileManager.deleteFile(null));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);

        Path incorrectPath = Paths.get("гуляш");

        exception = assertThrows(IllegalStateException.class,
                () -> fileManager.deleteFile(incorrectPath));
        assertThat(exception.getMessage()).isEqualTo(exceptionMessage);
    }

    @AfterAll
    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void deleteDocStorageDirectory() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("src/test/resources/filestorage/"))) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Configuration
    @Profile("test")
    static class Config {
        private final Environment env = Mockito.mock(Environment.class);

        @Bean
        public FileManager fileManager() {
            Mockito.when(env.getRequiredProperty("doc.file.storage.directory"))
                    .thenReturn("src/test/resources/filestorage");
            return new FileManagerImpl(env);
        }

    }

}