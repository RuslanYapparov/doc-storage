package ru.yappy.docstorage.model;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "users_documents")
public class UserWithAccess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User userWithAccess;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private Document document;
    @Column(name = "access_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

}