package ru.yappy.docstorage.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private LocalDate createdAt;


}
