package ru.yappy.docstorage.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;
import ru.yappy.docstorage.config.*;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.VerificationToken;
import ru.yappy.docstorage.model.dto.UserDto;
import ru.yappy.docstorage.out.repo.*;
import ru.yappy.docstorage.service.impl.*;
import ru.yappy.docstorage.service.mapper.UserMapper;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = { TestPersistenceConfig.class, UserServiceImpl.class,
        TestConfig.class, ConfirmationServiceImpl.class })
@ActiveProfiles("test")
@Transactional
public class ConfirmationServiceUnitTest {
    private final ConfirmationService confirmationService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository tokenRepository;
    private User testUser;
    @MockitoBean
    private final JavaMailSender mailSender;

    @Autowired
    public ConfirmationServiceUnitTest(ConfirmationService confirmationService,
                                       UserRepository userRepository,
                                       VerificationTokenRepository tokenRepository,
                                       JavaMailSender mailSender) {
        this.confirmationService = confirmationService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.mailSender = mailSender;
    }

    @BeforeEach
    void saveNewUserAndConfigureMessageCreating() {
        testUser = userRepository.save(createTestUser());
        Mockito.when(mailSender.createMimeMessage())
                .thenReturn(new MimeMessage(Session.getDefaultInstance(new Properties())));
    }

    @Test
    void sendMessageWithTokenToNewUser_whenGetPreviouslySavedUser_thenSaveTokenAndSendEmail() {
        assertDoesNotThrow(() -> confirmationService.sendMessageWithTokenToNewUser(testUser));

        Mockito.verify(mailSender, Mockito.times(1)).createMimeMessage();
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(MimeMessage.class));
    }

    @Test
    void sendMessageWithTokenToNewUser_whenGetNotSavedUser_thenThrowInvalidDataAccessApiUsageException() {
        assertThrows(InvalidDataAccessApiUsageException.class,
                () -> confirmationService.sendMessageWithTokenToNewUser(new User()));

        Mockito.verify(mailSender, Mockito.never()).createMimeMessage();
        Mockito.verify(mailSender, Mockito.never()).send(Mockito.any(MimeMessage.class));
    }

    @Test
    void sendMessageWithTokenToNewUser_whenMailSenderThrowsException_thenThrowISExceptionAndDeleteUserFromDB() {
        Mockito.doThrow(new MailSendException("test exception")).when(mailSender).send(Mockito.any(MimeMessage.class));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> confirmationService.sendMessageWithTokenToNewUser(testUser));
        assertThat(exception.getMessage()).isEqualTo("Невозможно завершить регистрацию пользователя из-за " +
                "ошибки, возникшей при отправке сообщения для подтверждения адреса электронной почты.");
        assertFalse(userRepository.existsById(testUser.getId()));

        Mockito.verify(mailSender, Mockito.times(1)).createMimeMessage();
        Mockito.verify(mailSender, Mockito.times(1)).send(Mockito.any(MimeMessage.class));
    }

    @Test
    void confirmEmailAndEnableUser_whenUserWasCreated_thenEnableUser() {
        assertDoesNotThrow(() -> confirmationService.sendMessageWithTokenToNewUser(testUser));
        VerificationToken token = tokenRepository.findAll().getLast();

        UserDto userDto = confirmationService.confirmEmailAndEnableUser(token.getToken());

        testUser = userRepository.findById(testUser.getId()).orElseThrow();

        assertTrue(testUser.isEnabled());
        assertThat(userDto).isEqualTo(UserMapper.toDto(testUser));
    }

    @Test
    void confirmEmailAndEnableUser_whenUserWasNotCreated_thenThrowInvalidDataAccessApiUsageException() {
        String uuidStr = UUID.randomUUID().toString();
        ObjectNotFoundException exception = assertThrows(ObjectNotFoundException.class,
                () -> confirmationService.confirmEmailAndEnableUser(uuidStr));
        assertThat(exception.getMessage()).isEqualTo("No row with the given identifier exists: " +
                "[VerificationToken#" + uuidStr + "]");
    }

    private User createTestUser() {
        User user = new User();
        user.setUsername("testUsername");
        user.setPassword("testPassword");
        user.setEmail("testEmail");
        return user;
    }

}