package ru.yappy.docstorage.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.*;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.*;
import org.springframework.stereotype.Service;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.UserDto;
import ru.yappy.docstorage.out.repo.*;
import ru.yappy.docstorage.service.ConfirmationService;
import ru.yappy.docstorage.service.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class ConfirmationServiceImpl implements ConfirmationService {
    private final JavaMailSender mailSender;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final String serverName;
    private final String from;

    @Autowired
    public ConfirmationServiceImpl(JavaMailSender mailSender,
                                   VerificationTokenRepository verificationTokenRepository,
                                   UserRepository userRepository,
                                   @Value("${server.host.name}") String serverName,
                                   @Value("${mail.username}") String from) {
        this.mailSender = mailSender;
        this.verificationTokenRepository = verificationTokenRepository;
        this.userRepository = userRepository;
        this.serverName = serverName;
        this.from = from;
    }

    @Override
    public void sendMessageWithTokenToNewUser(User user) {
        log.debug("Начало выполнения операции отправки сообщения с ссылкой для подтверждения учетной записи " +
                "пользователю '{}'.", user.getUsername());
        VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), user, LocalDateTime.now());
        verificationTokenRepository.save(token);
        sendConfirmationEmail(user, token);
        log.debug("Электронное письмо с ссылкой для подтверждения учетной записи пользователя '{}' отправлено " +
                "на адрес '{}'.", user.getUsername(), user.getEmail());
    }

    @Override
    public UserDto confirmEmailAndEnableUser(String token) {
        log.debug("Начало выполнения операции подтверждения учетной записи в соответствии с токеном {}.", token);
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new ObjectNotFoundException((Object) token, "VerificationToken"));
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        UserDto userDto = UserMapper.toDto(user);
        log.debug("Учетная запись пользователя '{}' успешно подтверждена.", user.getUsername());
        return userDto;
    }

    private void sendConfirmationEmail(User user, VerificationToken token) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Подтверждение регистрации на " + serverName);
            helper.setText("Для подтверждения Вашей учетной записи на " + serverName + ", пожалуйста, перейдите по " +
                    "следующей ссылке: http://" + serverName + "/api/v1/users/confirm?token=" + token.getToken());
            helper.setFrom(from);
            mailSender.send(message);
        } catch (MessagingException | MailSendException exception) {
            log.error("Возникла ошибка при отправке сообщения для подтверждения адреса электронной почты: '{}'." +
                    " Данные о новом пользователе '{}' и токене для проверке удаляются из базы.",
                    exception.getMessage(), user.getUsername());
            verificationTokenRepository.delete(token);
            userRepository.deleteById(user.getId());
            log.info("Данные пользователя '{}' и его токен для проверки удалены из базы данных.", user.getUsername());
            throw new IllegalStateException("Невозможно завершить регистрацию пользователя из-за ошибки, возникшей " +
                    "при отправке сообщения для подтверждения адреса электронной почты.");
        }
    }

}