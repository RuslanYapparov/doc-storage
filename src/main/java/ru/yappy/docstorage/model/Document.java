package ru.yappy.docstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;
    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;
    @Column(name = "description", length = 1000)
    private String description;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "users_documents",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<UserWithAccess> usersWithAccess;

}