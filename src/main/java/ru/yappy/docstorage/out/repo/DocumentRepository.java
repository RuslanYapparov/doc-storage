package ru.yappy.docstorage.out.repo;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yappy.docstorage.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @EntityGraph(value = "Document.includeUsersWithAccess", type = EntityGraph.EntityGraphType.LOAD)
    Page<Document> findAllByOwnerId(Long ownerId, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE doc.commonAccessType IS NOT NULL OR dua.username = :username
           """)
    Page<Document> findAllAvailableByUsername(@Param("username") String username, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE dua.username = :username
           """)
    Page<Document> findAllAvailableWithoutSharedByUsername(@Param("username") String username, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (doc.commonAccessType IS NOT NULL OR dua.username = :username) AND doc.owner.id <> :ownerId
           """)
    Page<Document> findAllAvailableWithoutOwnedByUsername(@Param("username") String username,
                                                          @Param("ownerId") Long ownerId,
                                                          Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE dua.username = :username AND doc.owner.id <> :ownerId
           """)
    Page<Document> findAllAvailableWithoutOwnedAndSharedByUsername(@Param("username") String username,
                                                                   @Param("ownerId") Long ownerId,
                                                                   Pageable page);

    @Query("""
           SELECT doc.owner.username FROM Document doc
           WHERE doc.id = :docId
           """)
    String findOwnerUsernameByDocumentId(@Param("docId") Long docId);

}