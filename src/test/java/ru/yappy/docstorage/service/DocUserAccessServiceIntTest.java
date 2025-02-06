package ru.yappy.docstorage.service;

import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.config.*;
import ru.yappy.docstorage.model.AccessType;
import ru.yappy.docstorage.model.dto.*;
import ru.yappy.docstorage.service.impl.*;

import java.io.*;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(classes = { TestPersistenceConfig.class, TestConfig.class, DocumentServiceImpl.class,
        UserServiceImpl.class, ConfirmationServiceImpl.class, DocUserAccessServiceImpl.class })
@ActiveProfiles("test")
@Transactional
public class DocUserAccessServiceIntTest {
    private final DocUserAccessService docUserAccessService;
    private final DocumentService documentService;
    @MockitoSpyBean(name = "userServiceImpl", reset = MockReset.NONE)
    private final UserService userService;
    @MockitoBean
    private FileManager fileManager;

    @Autowired
    public DocUserAccessServiceIntTest(DocUserAccessService docUserAccessService,
                                       DocumentService documentService,
                                       UserService userService) {
        this.docUserAccessService = docUserAccessService;
        this.documentService = documentService;
        this.userService = userService;
    }

    @Test
    void saveAccessToDocumentForOwner_whenInvoked_thenSavesNewAccessEntity() {
        assertDoesNotThrow(() -> docUserAccessService.saveAccessToDocumentForOwner(1L, "firstUser"));

        String[] usernamesWithGrantedAccess =
                docUserAccessService.getUsernamesWithGrantedAccess(1L, AccessType.REMOVE);

        assertThat(usernamesWithGrantedAccess).contains("firstUser");
    }

    @Test
    void grantAccessToDocumentForUser_whenOwnerGivesAccessToAnotherUser_thenSavesNewAccessEntity() {
        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(2L,
                "secondUser", AccessType.EDIT)));

        String[] usernamesWithGrantedAccess =
                docUserAccessService.getUsernamesWithGrantedAccess(2L, AccessType.EDIT);

        assertThat(usernamesWithGrantedAccess).contains("secondUser");
    }

    @Test
    void grantAccessToDocumentForUser_whenOwnerGivesAccessToHimself_thenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(3L,
                                "firstUser", AccessType.READ_ONLY)));
        assertThat(exception.getMessage()).isEqualTo("Обладатель документа имеет полный контроль над " +
                "ним и не может это изменить.");
    }

    @Test
    void getCheckedDocumentForOperations_whenHaveAccess_thenReturnsDocument() {
        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.READ_ONLY, 2L,
                "firstUser"));
        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.EDIT, 3L,
                "firstUser"));
        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.REMOVE, 2L,
                "firstUser"));

        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(2L,
                "secondUser", AccessType.REMOVE)));

        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.READ_ONLY, 2L,
                "secondUser"));
        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.EDIT, 2L,
                "secondUser"));
        assertDoesNotThrow(() -> docUserAccessService.getCheckedDocumentForOperations(AccessType.REMOVE, 2L,
                "secondUser"));
    }

    @Test
    void getCheckedDocumentForOperations_whenHaveNotAccess_thenThrowsIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.getCheckedDocumentForOperations(AccessType.READ_ONLY, 1L,
                        "firstUser"));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=1 не доступен для скачивания " +
                "пользователю 'firstUser'");

        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(2L,
                "secondUser", AccessType.READ_ONLY)));
        exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.getCheckedDocumentForOperations(AccessType.EDIT, 2L,
                        "secondUser"));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=2 не доступен для обновления " +
                "пользователю 'secondUser'");

        exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.getCheckedDocumentForOperations(AccessType.REMOVE, 2L,
                        "secondUser"));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=2 не доступен для удаления " +
                "пользователю 'secondUser'");
    }

    @Test
    void getUsernamesWithGrantedAccess_whenGetSpecificAccessType_thenReturnArrayWithSpecificAccessedUserNames() {
        String[] usernamesWithGrantedAccess =
                docUserAccessService.getUsernamesWithGrantedAccess(2L, AccessType.READ_ONLY);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess).isEmpty();

        usernamesWithGrantedAccess = docUserAccessService.getUsernamesWithGrantedAccess(1L, AccessType.REMOVE);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess.length).isEqualTo(1);
        assertThat(usernamesWithGrantedAccess).contains("secondUser");

        docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(2L, "secondUser",
                AccessType.EDIT));
        usernamesWithGrantedAccess = docUserAccessService.getUsernamesWithGrantedAccess(2L, AccessType.EDIT);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess.length).isEqualTo(1);
        assertThat(usernamesWithGrantedAccess).contains("secondUser");
    }

    @Test
    void getUsernamesWithGrantedAccess_whenGetNullAccessType_thenReturnArrayWithAllAccessedUserNames() {
        docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(2L, "secondUser",
                AccessType.EDIT));
        String[] usernamesWithGrantedAccess =
                docUserAccessService.getUsernamesWithGrantedAccess(2L, null);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess.length).isEqualTo(2);
        assertThat(usernamesWithGrantedAccess).contains("firstUser", "secondUser");
    }

    @Test
    void revokeAccessToDocumentForUser_whenCalledOwnerToAnotherUser_thenDeleteAccess() {
        assertDoesNotThrow(() ->
                docUserAccessService.revokeAccessToDocumentForUser(2L, "secondUser"));

        docUserAccessService.grantAccessToDocumentForUser(
                new DocUserAccessDto(2L, "secondUser", AccessType.EDIT));

        String[] usernamesWithGrantedAccess =
                docUserAccessService.getUsernamesWithGrantedAccess(2L, null);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess.length).isEqualTo(2);
        assertThat(usernamesWithGrantedAccess).contains("firstUser", "secondUser");

        assertDoesNotThrow(() ->
                docUserAccessService.revokeAccessToDocumentForUser(2L, "secondUser"));
        usernamesWithGrantedAccess = docUserAccessService.getUsernamesWithGrantedAccess(2L, null);

        assertThat(usernamesWithGrantedAccess).isNotNull();
        assertThat(usernamesWithGrantedAccess.length).isEqualTo(1);
        assertThat(usernamesWithGrantedAccess).contains("firstUser");
    }

    @Test
    void revokeAccessToDocumentForUser_whenCalledNotOwner_thenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.revokeAccessToDocumentForUser(1L, "firstUser"));
        assertThat(exception.getMessage()).isEqualTo("Пользователь 'firstUser' не является обладателем " +
                "документа с id='1' и не может управлять доступом к нему.");
    }

    @Test
    void revokeAccessToDocumentForUser_whenCalledByOwnerToOwner_thenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> docUserAccessService.revokeAccessToDocumentForUser(2L, "firstUser"));
        assertThat(exception.getMessage()).isEqualTo("Обладатель документа не может отозвать доступ к " +
                "нему у самого себя.");
    }

    @PostConstruct
    void configureDocumentServiceTestStartState() throws IOException {
        Mockito.when(fileManager.saveFile(Mockito.any(MultipartFile.class)))
                .thenAnswer(invocation ->
                        Paths.get("src/test/resources/MockFile" + new Object().hashCode()));
        try {
            userService.saveNewUser(new NewUserDto("firstUser", "firstUser",
                    "firstUser@mail.ru", "firstUserFirstName", "FirstUserLastName"));
        } catch (DataIntegrityViolationException exception) {
            Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
            return;
        }
        userService.saveNewUser(new NewUserDto("secondUser", "secondUser",
                "secondUser@mail.ru", "secondUserFirstName", "SecondUserLastName"));
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        MultipartFile file1 = new MockMultipartFile("MockFile", "MockFile", null,
                new FileInputStream("src/test/resources/MockFile"));
        MultipartFile file2 = new MockMultipartFile("SuYo.jpg", "SuYo.jpg", null,
                new FileInputStream("src/test/resources/SuYo.jpg"));
        assertDoesNotThrow(() -> documentService.saveNewDocument(file1, "Test mock file",
                "Test description of mock file uploaded by SECOND User"));
        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
        assertDoesNotThrow(() -> documentService.saveNewDocument(file1, "Test mock file",
                "Test description of mock file uploaded by FIRST User"));
        assertDoesNotThrow(() -> documentService.saveNewDocument(file2, "Nigerian HR Consultant",
                "Testing jpg file with screenshot from LinkedIn uploaded by FIRST User"));
    }

}