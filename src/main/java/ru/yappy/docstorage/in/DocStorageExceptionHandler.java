package ru.yappy.docstorage.in;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.*;
import ru.yappy.docstorage.model.dto.ExceptionDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class DocStorageExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        String message = exception.getMessage();
        int firstIndex = message.lastIndexOf("ключа: ") + 7;
        int lastIndex = message.indexOf(".] [insert into ");
        String notUniqueValue = message.substring(firstIndex, lastIndex);
        String ruMessage = String.format("Невозможно сохранить данные пользователя, значение %s для соответствующего " +
                "поля уже используется.", notUniqueValue);
        return new ExceptionDto(LocalDateTime.now(),
                exception.getClass().getSimpleName(),
                ruMessage,
                getDefaultExceptionDetails(exception.getStackTrace()));
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleObjectNotFoundException(ObjectNotFoundException exception) {
        String ruMessage = String.format("Объект '%s' с идентификатором id=%s не найден в базе данных",
                exception.getEntityName(), exception.getIdentifier());
        return new ExceptionDto(LocalDateTime.now(),
                exception.getClass().getSimpleName(),
                ruMessage,
                getDefaultExceptionDetails(exception.getStackTrace()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleHttpMethodNotReadableException(HttpMessageNotReadableException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleConstraintViolationException(ConstraintViolationException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleIllegalArgumentException(IllegalArgumentException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionDto handleMethodValidationException(HandlerMethodValidationException exception) {
        String errorMessage = exception.getAllErrors().stream()
                .map(error -> "Значение параметра метода " +
                        exception.getMethod().getName() + " " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return new ExceptionDto(LocalDateTime.now(),
                exception.getClass().getSimpleName(),
                errorMessage,
                getDefaultExceptionDetails(exception.getStackTrace())
        );
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleIOException(IOException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleIllegalStateException(IllegalStateException exception) {
        return handleException(exception);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionDto handleRuntimeException(RuntimeException exception) {
        return handleException(exception);
    }

    protected ExceptionDto handleException(Exception exception) {
        log.warn(exception.getMessage(), exception);
        return new ExceptionDto(LocalDateTime.now(),
                exception.getClass().getSimpleName(),
                exception.getMessage(),
                getDefaultExceptionDetails(exception.getStackTrace())
        );
    }

    private String getDefaultExceptionDetails(StackTraceElement[] stackTrace) {
        StringBuilder details = new StringBuilder("(stack trace) ");
        Arrays.stream(stackTrace)
                .limit(7)
                .forEach(stackTraceLine -> {
                    String line = stackTraceLine.toString();
                    if (line.length() > 80) {
                        line = line.substring(0, 80) + " " + line.substring(80);
                    }
                    details.append(line).append(" ");
                });
        return details.toString();
    }

}