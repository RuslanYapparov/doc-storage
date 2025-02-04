package ru.yappy.docstorage.out.repo;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yappy.docstorage.model.Document;

import java.time.LocalDate;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAllByOwnerId(Long ownerId, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE doc.commonAccessType IS NOT NULL OR dua.username = :username
           """)
    Page<Document> findAllAvailable(@Param("username") String username, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE dua.username = :username
           """)
    Page<Document> findAllAvailableWithoutShared(@Param("username") String username, Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (doc.commonAccessType IS NOT NULL OR dua.username = :username) AND doc.owner.id <> :ownerId
           """)
    Page<Document> findAllAvailableWithoutOwned(@Param("username") String username,
                                                @Param("ownerId") Long ownerId,
                                                Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE dua.username = :username AND doc.owner.id <> :ownerId
           """)
    Page<Document> findAllAvailableWithoutOwnedAndShared(@Param("username") String username,
                                                         @Param("ownerId") Long ownerId,
                                                         Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           WHERE (doc.owner.id = :ownerId)
           AND (LOWER(doc.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(doc.description) LIKE LOWER(CONCAT('%', :text, '%')))
           AND (:since IS NULL OR doc.createdAt >= :since)
           AND (:until IS NULL OR doc.createdAt < :until)
           """)
    Page<Document> searchInSaved(@Param("ownerId") Long ownerId,
                                 @Param("text") String text,
                                 @Param("since") LocalDate since,
                                 @Param("until") LocalDate until,
                                 Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (doc.commonAccessType IS NOT NULL OR dua.username = :username)
           AND (LOWER(doc.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(doc.description) LIKE LOWER(CONCAT('%', :text, '%')))
           AND (:since IS NULL OR doc.createdAt >= :since)
           AND (:until IS NULL OR doc.createdAt < :until)
           """)
    Page<Document> searchInAllAvailable(@Param("username") String username,
                                        @Param("text") String text,
                                        @Param("since") LocalDate since,
                                        @Param("until") LocalDate until,
                                        Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (dua.username = :username)
           AND (LOWER(doc.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(doc.description) LIKE LOWER(CONCAT('%', :text, '%')))
           AND (:since IS NULL OR doc.createdAt >= :since)
           AND (:until IS NULL OR doc.createdAt < :until)
           """)
    Page<Document> searchInAllAvailableWithoutShared(@Param("username") String username,
                                                     @Param("text") String text,
                                                     @Param("since") LocalDate since,
                                                     @Param("until") LocalDate until,
                                                     Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (doc.commonAccessType IS NOT NULL OR dua.username = :username)
           AND (doc.owner.id <> :ownerId)
           AND (LOWER(doc.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(doc.description) LIKE LOWER(CONCAT('%', :text, '%')))
           AND (:since IS NULL OR doc.createdAt >= :since)
           AND (:until IS NULL OR doc.createdAt < :until)
           """)
    Page<Document> searchInAllAvailableWithoutOwned(@Param("username") String username,
                                                    @Param("ownerId") Long ownerId,
                                                    @Param("text") String text,
                                                    @Param("since") LocalDate since,
                                                    @Param("until") LocalDate until,
                                                    Pageable page);

    @Query("""
           SELECT doc FROM Document doc
           JOIN FETCH doc.owner AS u
           JOIN doc.usersWithAccess AS dua
           WHERE (dua.username = :username AND doc.owner.id <> :ownerId)
           AND (LOWER(doc.title) LIKE LOWER(CONCAT('%', :text, '%'))
           OR LOWER(doc.description) LIKE LOWER(CONCAT('%', :text, '%')))
           AND (:since IS NULL OR doc.createdAt >= :since)
           AND (:until IS NULL OR doc.createdAt < :until)
           """)
    Page<Document> searchInAllAvailableWithoutOwnedAndShared(@Param("username") String username,
                                                             @Param("ownerId") Long ownerId,
                                                             @Param("text") String text,
                                                             @Param("since") LocalDate since,
                                                             @Param("until") LocalDate until,
                                                             Pageable page);

    @Query("""
           SELECT doc.owner.username FROM Document doc
           WHERE doc.id = :docId
           """)
    String findOwnerUsernameByDocumentId(@Param("docId") Long docId);

}