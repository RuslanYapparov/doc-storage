package ru.yappy.docstorage.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ExceptionDto(@JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss.SS") LocalDateTime timeStamp,
                           String exception,
                           String message,
                           String details) {}