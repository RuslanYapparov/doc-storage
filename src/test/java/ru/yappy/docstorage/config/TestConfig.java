package ru.yappy.docstorage.config;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.springframework.context.annotation.*;
import org.springframework.lang.NonNull;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.InputStream;
import java.util.Properties;

@Configuration
@Profile("test")
public class TestConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return (String) rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.equals(encodedPassword);
            }
        };
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public @NonNull MimeMessage createMimeMessage() {
                Session mailSession = Session.getDefaultInstance(new Properties());
                return new MimeMessage(mailSession);
            }

            @Override
            public @NonNull MimeMessage createMimeMessage(@NonNull InputStream contentStream) throws MailException {
                Session mailSession = Session.getDefaultInstance(new Properties());
                return new MimeMessage(mailSession);
            }

            @Override
            public void send(@NonNull MimeMessage... mimeMessages) throws MailException {
            }

            @Override
            public void send(@NonNull SimpleMailMessage... simpleMessages) throws MailException {
            }

        };

    }

}
