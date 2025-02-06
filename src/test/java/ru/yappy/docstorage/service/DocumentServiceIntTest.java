package ru.yappy.docstorage.service;

import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.config.*;
import ru.yappy.docstorage.model.AccessType;
import ru.yappy.docstorage.model.dto.*;
import ru.yappy.docstorage.model.paramholder.*;
import ru.yappy.docstorage.service.impl.*;

import java.io.*;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { TestPersistenceConfig.class, TestConfig.class, FileManagerUnitTest.Config.class,
        DocumentServiceImpl.class, UserServiceImpl.class, ConfirmationServiceImpl.class,
        DocUserAccessServiceImpl.class })
@ActiveProfiles("test")
@Transactional
public class DocumentServiceIntTest {
    private final DocumentService documentService;
    @MockitoSpyBean(name = "userServiceImpl", reset = MockReset.NONE)
    private final UserService userService;
    private final DocUserAccessService docUserAccessService;

    @Autowired
    public DocumentServiceIntTest(DocumentService documentService,
                                  UserService userService,
                                  DocUserAccessService docUserAccessService) throws IOException {
        this.documentService = documentService;
        this.userService = userService;
        this.docUserAccessService = docUserAccessService;
        configureDocumentServiceTestStartState();
    }

    @AfterEach
    void returnAuthenticationToFirstUser() {
        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
    }

    @Test
    void saveNewDocument_whenGetSameDocument_thenSavesItWithAddedNumberInFilename()
            throws IOException {
        MultipartFile file1 = new MockMultipartFile("MockFile", "MockFile", null,
                new FileInputStream("src/test/resources/MockFile"));
        DocumentDto docDto = documentService.saveNewDocument(file1, "another MockFile",
                "description of another MockFile");

        assertThat(docDto).isNotNull();
        assertThat(docDto.title()).isEqualTo("another MockFile");
        assertThat(docDto.description()).isEqualTo("description of another MockFile");
        assertThat(docDto.fileName()).isEqualTo("MockFile (2)");
        assertThat(docDto.createdAt()).isEqualTo(LocalDate.now());
    }

    @Test
    void getDocumentResourceById_whenOwnerRequestsWithCorrectId_thenReturnCorrectResource()
            throws IOException {
        long mockFileLength = 3L;
        long suYoJpgLength = 108640L;
        Resource fileResource1 = documentService.getDocumentResourceById(2L);
        Resource fileResource2 = documentService.getDocumentResourceById(3L);

        assertThat(fileResource1).isNotNull();
        assertThat(fileResource2).isNotNull();
        assertThat(fileResource1.contentLength()).isEqualTo(mockFileLength);
        assertThat(fileResource2.contentLength()).isEqualTo(suYoJpgLength);
    }

    @Test
    void getDocumentResourceById_whenUserWithoutAccessRequestsFile_thenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> documentService.getDocumentResourceById(1L));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=1 не доступен для скачивания" +
                " пользователю 'firstUser'");
    }

    @Test
    void getDocumentResourceById_whenUserRequestsNotExistingFile_thenThrowObjectNotFoundException() {
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> documentService.getDocumentResourceById(-1L));
        assertThat(exception.getMessage()).isEqualTo("No row with the given identifier exists: [Document#-1]");
    }

    @Test
    void getSavedDocumentsWithParameters_whenGetDifferentParameters_thenReturnCorrectDocumentArray() {
        GetDocsParamHolder paramHolder = new GetDocsParamHolder(DocSortType.DATE, Sort.Direction.ASC, 0, 5);
        DocumentDto[] docs = documentService.getSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(2L);

        paramHolder = new GetDocsParamHolder(DocSortType.TITLE, Sort.Direction.ASC, 0, 5);
        docs = documentService.getSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(3L);

        paramHolder = new GetDocsParamHolder(DocSortType.TITLE, Sort.Direction.ASC, 5, 5);
        docs = documentService.getSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();
    }

    @Test
    void searchInSavedDocumentsWithParameters_whenGetDifferentParameters_thenReturnCorrectDocumentArray() {
        SearchInDocsParamHolder paramHolder = new SearchInDocsParamHolder("mock", null, null,
                DocSortType.DATE, Sort.Direction.ASC, 0, 5);
        DocumentDto[] docs = documentService.searchInSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(2L);

        paramHolder = new SearchInDocsParamHolder("mock", null, LocalDate.now().minusDays(1),
                DocSortType.DESCRIPTION, Sort.Direction.ASC, 0, 5);
        docs = documentService.searchInSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();

        paramHolder = new SearchInDocsParamHolder(null, LocalDate.now(), null, DocSortType.OWNER,
                Sort.Direction.ASC, 0, 5);
        docs = documentService.searchInSavedDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();
    }

    @Test
    void searchInSavedDocumentsWithParameters_whenGetNullSortByParam_thenThrowNPE() {
        SearchInDocsParamHolder paramHolder = new SearchInDocsParamHolder(null, null, null,
                null, Sort.Direction.ASC, 0, 5);
        NullPointerException nullPointerException = assertThrows(NullPointerException.class,
                () -> documentService.searchInSavedDocumentsWithParameters(paramHolder));
        assertThat(nullPointerException.getMessage())
                .isEqualTo("Cannot invoke \"ru.yappy.docstorage.model.paramholder." +
                        "DocSortType.getPropertyName()\" because the return value of \"ru.yappy.docstorage.model." +
                        "paramholder.SearchInDocsParamHolder.sortBy()\" is null");
    }

    @Test
    void searchInSavedDocumentsWithParameters_whenGetNullOrderParam_thenThrowIllegalArgumentException() {
        SearchInDocsParamHolder paramHolder = new SearchInDocsParamHolder(null, null, null,
                DocSortType.OWNER, null, 0, 5);
        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                () -> documentService.searchInSavedDocumentsWithParameters(paramHolder));
        assertThat(illegalArgumentException.getMessage()).isEqualTo("Direction must not be null");
    }

    @Test
    void getAvailableDocumentsWithParameters_whenGetDifferentParameters_thenReturnCorrectDocumentArray() {
        GetDocsParamHolder paramHolder = new GetDocsParamHolder(DocSortType.DESCRIPTION, Sort.Direction.ASC,
                0, 5, true, true);
        DocumentDto[] docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(2L);
        assertThat(docs[1].id()).isEqualTo(3L);

        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(1L);

        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
        paramHolder = new GetDocsParamHolder(DocSortType.DESCRIPTION, Sort.Direction.ASC,
                0, 5, false, true);
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();
    }

    @Test
    void searchInAvailableDocumentsWithParameters_whenGetDifferentParameters_thenReturnCorrectDocumentArray() {
        SearchInDocsParamHolder paramHolder = new SearchInDocsParamHolder(" jp", null, null,
                DocSortType.DATE, Sort.Direction.ASC, 0, 5);
        DocumentDto[] docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(3L);

        paramHolder = new SearchInDocsParamHolder(" jp", null, null,
                DocSortType.DATE, Sort.Direction.ASC, 0, 5, false, false);
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();

        paramHolder = new SearchInDocsParamHolder(" jp", null, null,
                DocSortType.DATE, Sort.Direction.ASC, 0, 5, true, false);
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(3L);

        documentService.shareDocumentForAllUsers(3L, AccessType.READ_ONLY);
        paramHolder = new SearchInDocsParamHolder(" jp", null, null,
                DocSortType.DATE, Sort.Direction.ASC, 0, 5, false, true);
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs).isEmpty();

        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(3L);
    }

    @Test
    void shareAndCloseDocumentForAllUsers_whenCorrectlyInvoked_thenAnotherUserCanRequestThisDocument() {
        assertDoesNotThrow(() -> documentService.shareDocumentForAllUsers(3L, AccessType.REMOVE));

        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        assertDoesNotThrow(() -> documentService.getDocumentResourceById(3L));
        GetDocsParamHolder paramHolder = new GetDocsParamHolder(DocSortType.OWNER, Sort.Direction.ASC,
                0, 5, true, false);
        DocumentDto[] docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(1L);

        paramHolder = new GetDocsParamHolder(DocSortType.OWNER, Sort.Direction.ASC,
                0, 5, false, true);
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(3L);

        paramHolder = new GetDocsParamHolder(DocSortType.OWNER, Sort.Direction.ASC,
                0, 5, true, true);
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(3L);
        assertThat(docs[1].id()).isEqualTo(1L);

        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
        assertDoesNotThrow(() -> documentService.shareDocumentForAllUsers(2L, AccessType.REMOVE));
        assertDoesNotThrow(() -> documentService.closeSharedAccessToDocument(3L));
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(2L);
        assertThat(docs[1].id()).isEqualTo(1L);
    }

    @Test
    void shareAndCloseDocumentForAllUsers_whenInvokedForAlreadySharedOrClosedDoc_thenThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> documentService.closeSharedAccessToDocument(3L));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=3 уже закрыт для всех пользователей.");
        assertDoesNotThrow(() -> documentService.shareDocumentForAllUsers(3L, AccessType.REMOVE));
        exception = assertThrows(IllegalArgumentException.class,
                () -> documentService.shareDocumentForAllUsers(3L, AccessType.REMOVE));
        assertThat(exception.getMessage()).isEqualTo("Документ с id=3 уже открыт с доступом 'REMOVE' " +
                "для всех пользователей.");
        assertDoesNotThrow(() -> documentService.shareDocumentForAllUsers(3L, AccessType.READ_ONLY));
    }

    @Test
    void shareAndCloseDocumentForAllUsers_whenInvokedByNotOwner_thenThrowIllegalArgumentException() {
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        String notOwnerExceptionText = "Пользователь 'secondUser' не является обладателем " +
                "документа с id=2 и не может управлять доступом к нему.";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> documentService.closeSharedAccessToDocument(2L));
        assertThat(exception.getMessage()).isEqualTo(notOwnerExceptionText);
        exception = assertThrows(IllegalArgumentException.class,
                () -> documentService.shareDocumentForAllUsers(2L, AccessType.EDIT));
        assertThat(exception.getMessage()).isEqualTo(notOwnerExceptionText);
    }

    @Test
    void grantAndRevokeAccessForUserToDocument_whenInvoked_thenAnotherUserCanRequestThisDocument() {
        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(3L,
                "secondUser", AccessType.REMOVE)));

        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        assertDoesNotThrow(() -> documentService.getDocumentResourceById(3L));
        GetDocsParamHolder paramHolder = new GetDocsParamHolder(DocSortType.OWNER, Sort.Direction.ASC,
                0, 5, false, false);
        DocumentDto[] docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(3L);

        paramHolder = new GetDocsParamHolder(DocSortType.OWNER, Sort.Direction.ASC,
                0, 5, true, false);
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(3L);
        assertThat(docs[1].id()).isEqualTo(1L);

        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
        assertDoesNotThrow(() -> docUserAccessService.revokeAccessToDocumentForUser(3L, "secondUser"));
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        docs = documentService.getAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(1L);
    }

    @Test
    void updateEditedDocument_whenInvokedByUserWithAccess_thenCorrectlyUpdateDocumentInStorageAndDb()
            throws IOException {
        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(3L,
                "secondUser", AccessType.EDIT)));
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        DocumentDto docDto = documentService.updateEditedDocument(null, 3L, null, null);

        assertThat(docDto).isNotNull();
        assertThat(docDto.id()).isEqualTo(3L);
        assertThat(docDto.title()).isEqualTo("Nigerian HR Consultant");
        assertThat(docDto.description()).isEqualTo("Testing jpg file with screenshot from LinkedIn " +
                "uploaded by FIRST User");
        assertThat(docDto.commonAccessType()).isNull();
        assertThat(docDto.ownerName()).isEqualTo("firstUser");
        assertThat(docDto.createdAt()).isEqualTo(LocalDate.now());
        assertThat(docDto.updatedBy()).isEqualTo("secondUser");
        assertThat(docDto.updatedAt()).isBetween(LocalDateTime.now().minusSeconds(1), LocalDateTime.now());

        MultipartFile file2 = new MockMultipartFile("SuYo.jpg", "SuYo.jpg", null,
                new FileInputStream("src/test/resources/SuYo.jpg"));
        docDto = documentService.updateEditedDocument(file2, 3L, "Updated title of SuYo.jpg",
                "Updated description of SuYo.jpg");

        assertThat(docDto).isNotNull();
        assertThat(docDto.id()).isEqualTo(3L);
        assertThat(docDto.title()).isEqualTo("Updated title of SuYo.jpg");
        assertThat(docDto.description()).isEqualTo("Updated description of SuYo.jpg");
        assertThat(docDto.commonAccessType()).isNull();
        assertThat(docDto.ownerName()).isEqualTo("firstUser");
        assertThat(docDto.createdAt()).isEqualTo(LocalDate.now());
        assertThat(docDto.updatedBy()).isEqualTo("secondUser");
        assertThat(docDto.updatedAt()).isBetween(LocalDateTime.now().minusSeconds(1), LocalDateTime.now());
    }

    @Test
    void deleteDocument_whenInvokedByUserWithAccess_thenCorrectlyDeleteDocumentFromStorageAndDb()
            throws IOException {
        assertDoesNotThrow(() -> docUserAccessService.grantAccessToDocumentForUser(new DocUserAccessDto(3L,
                "secondUser", AccessType.REMOVE)));
        Mockito.doReturn(userService.loadUserByUsername("secondUser")).when(userService).getAuthenticatedUser();
        SearchInDocsParamHolder paramHolder = new SearchInDocsParamHolder("", null, null,
                DocSortType.OWNER, Sort.Direction.ASC, 0, 5);
        DocumentDto[] docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(2);
        assertThat(docs[0].id()).isEqualTo(3L);

        DocumentDto docDto = documentService.deleteDocument(3L);

        assertThat(docDto).isNotNull();
        assertThat(docDto.id()).isEqualTo(3L);
        assertThat(docDto.title()).isEqualTo("Nigerian HR Consultant");
        assertThat(docDto.description()).isEqualTo("REMOVED");

        paramHolder = new SearchInDocsParamHolder("", null, null,
                DocSortType.OWNER, Sort.Direction.ASC, 0, 5);
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(1L);

        Mockito.doReturn(userService.loadUserByUsername("firstUser")).when(userService).getAuthenticatedUser();
        MultipartFile file2 = new MockMultipartFile("SuYo.jpg", "SuYo.jpg", null,
                new FileInputStream("src/test/resources/SuYo.jpg"));
        docs = documentService.searchInAvailableDocumentsWithParameters(paramHolder);

        assertThat(docs).isNotNull();
        assertThat(docs.length).isEqualTo(1);
        assertThat(docs[0].id()).isEqualTo(2L);
        assertDoesNotThrow(() -> documentService.saveNewDocument(file2, "Nigerian HR Consultant",
                "Testing jpg file with screenshot from LinkedIn uploaded by FIRST User"));
    }

    @AfterAll
    static void deleteDocStorageDirectory() throws IOException {
        FileManagerUnitTest.deleteDocStorageDirectory();
    }

    private void configureDocumentServiceTestStartState() throws IOException {
        try {
            userService.saveNewUser(new NewUserDto("firstUser", "firstUser",
                    "firstUser@mail.ru", "firstUserFirstName", "FirstUserLastName"));
        } catch (DataIntegrityViolationException exception) {
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