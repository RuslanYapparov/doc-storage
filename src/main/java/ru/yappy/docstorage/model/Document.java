package ru.yappy.docstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;
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
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;
    @Column(name = "description", length = 1000)
    private String description;
    @Column(name = "common_access_type")
    @Enumerated(EnumType.STRING)
    private AccessType commonAccessType;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "docId")
    private Set<DocUserAccess> usersWithAccess;

}