package ru.yappy.docstorage.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.yappy.docstorage.config.*;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.*;
import ru.yappy.docstorage.service.impl.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(classes = { TestPersistenceConfig.class, TestConfig.class,
        UserServiceImpl.class, ConfirmationServiceImpl.class })
@ActiveProfiles("test")
@Transactional
public class UserServiceIntTest {
    private final UserService userService;

    @Autowired
    public UserServiceIntTest(UserService userService) {
        this.userService = userService;
    }

    @BeforeEach
    void saveNewUser_whenGetCorrectNewUserDto_thenSavesNewUserAndReturnDto() {
        NewUserDto newUserDto = new NewUserDto("testUsername", "testPassword", "testEmail",
                "TestFirstName", "TestLastName");

        UserDto userDto = userService.saveNewUser(newUserDto);

        assertThat(userDto).isNotNull();
        assertThat(userDto.username()).isEqualTo("testUsername");
        assertThat(userDto.firstName()).isEqualTo("TestFirstName");
        assertThat(userDto.lastName()).isEqualTo("TestLastName");
        assertThat(userDto.isEnabled()).isEqualTo(false);
    }

    @ParameterizedTest
    @CsvSource({ "testUsername,testPassword,testEmail,TestFirstName,TestLastName",
            "testUsername,testPassword,anotherTestEmail,TestFirstName,TestLastName",
            "anotherTestUsername,testPassword,testEmail,TestFirstName,TestLastName" })
    void saveNewUser_whenGetCorrectNewUserDtoWithRepeatedUsernameAndEmail_thenThrowDataIntegrityException(
            String username, String password, String email, String firstName, String lastName) {
        NewUserDto newUserDto = new NewUserDto(username, password, email, firstName, lastName);

        assertThat(newUserDto.toString()).doesNotContain("password", "id");
        DataIntegrityViolationException exception =
                assertThrows(DataIntegrityViolationException.class, () -> userService.saveNewUser(newUserDto));
        assertThat(exception.getMessage()).startsWith("could not execute statement [Нарушение уникального индекса " +
                "или первичного ключа: \"PUBLIC.CONSTRAINT_INDEX_");
    }

    @ParameterizedTest
    @CsvSource(value = { "null,testPassword,testEmail,TestFirstName,TestLastName",
            "testUsername,null,testEmail,TestFirstName,TestLastName",
            "testUsername,testPassword,null,TestFirstName,TestLastName" }, nullValues = { "null" })
    void saveNewUser_whenGetNewUserDtoWithNullRequiredParameter_thenThrowException(
            String username, String password, String email, String firstName, String lastName) {
        DataIntegrityViolationException exception =
                assertThrows(DataIntegrityViolationException.class,
                        () -> userService.saveNewUser(new NewUserDto(username, password, email, firstName, lastName)));
        assertThat(exception.getMessage())
                .startsWith("could not execute statement [Значение NULL не разрешено для поля \"");
    }

    @Test
    void loadUserByUsername_whenGetCorrectUsername_thenReturnCorrectUserDetails() {
        User user = (User) userService.loadUserByUsername("testUsername");

        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("testUsername");
        assertThat(user.getPassword()).isEqualTo("testPassword");
        assertThat(user.getEmail()).isEqualTo("testEmail");
        assertThat(user.getFirstName()).isEqualTo("TestFirstName");
        assertThat(user.getLastName()).isEqualTo("TestLastName");
        assertThat(user.isEnabled()).isEqualTo(false);
    }

    @Test
    void loadUserByUsername_whenGetIncorrectUsername_thenThrowUsernameNotFoundException() {
        UsernameNotFoundException exception =
                assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("incorrectUsername"));
        assertThat(exception.getMessage())
                .isEqualTo("Пользователь с username='incorrectUsername' не был сохранен.");
    }

    @Test
    void getAuthenticatedUser_whenSecurityContextGivesUserDetails_thenReturnCorrectUserDetails() {
        User user = (User) userService.loadUserByUsername("testUsername");
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new TestingAuthenticationToken(user, ""));
        try (MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);
            User userFromSecurityContext = (User) userService.getAuthenticatedUser();
            assertThat(userFromSecurityContext).isNotNull();
            assertThat(userFromSecurityContext).isEqualTo(user);
        }
    }

    @Test
    void getAuthenticatedUser_whenSecurityContextGivesObject_thenReturnNull() {
        SecurityContext mockedSecurityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(mockedSecurityContext.getAuthentication())
                .thenReturn(new TestingAuthenticationToken(new Object(), ""));
        try (MockedStatic<SecurityContextHolder> mockedHolder = Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);
            User userFromSecurityContext = (User) userService.getAuthenticatedUser();
            assertThat(userFromSecurityContext).isNull();
        }
    }

}