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
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    @Column(name = "file_path", nullable = false, unique = true)
    private String filePath;
    @Column(name = "description", length = 1000)
    private String description;
    @Column(name = "is_shared_for_all")
    private boolean isSharedForAll;
    @Column(name = "access_type_for_all")
    @Enumerated(EnumType.STRING)
    private AccessType accessTypeForAll;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "docId")
    private Set<DocUserAccess> usersWithAccess;

}