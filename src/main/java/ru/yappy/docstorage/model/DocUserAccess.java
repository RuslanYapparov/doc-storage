package ru.yappy.docstorage.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users_documents")
@IdClass(DocUserAccess.DocUserAccessPrimaryKey.class)
public class DocUserAccess {
    @Id
    @Column(name = "document_id")
    private Long docId;
    @Id
    @Column(name = "username")
    private String username;
    @Column(name = "access_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class DocUserAccessPrimaryKey implements Serializable {
        private Long docId;
        private String username;
    }

}