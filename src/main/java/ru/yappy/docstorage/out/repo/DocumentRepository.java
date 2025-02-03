package ru.yappy.docstorage.out.repo;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yappy.docstorage.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findAllByOwnerId(Long ownerId, Pageable page);

    Page<Document> findAllByIsSharedForAllOrOwnerIdOrUsersWithAccessUsername(boolean sharedForAllIncluded,
                                                                             Long ownerId,
                                                                             String username,
                                                                             Pageable page);

    Page<Document> findAllByUsersWithAccessUsername(String username, Pageable page);

    @Query("""
           SELECT doc.owner.username FROM Document doc
           WHERE doc.id = :docId
           """)
    String findOwnerUsernameByDocumentId(@Param("docId") Long docId);

}