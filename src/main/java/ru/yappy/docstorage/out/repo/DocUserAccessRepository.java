package ru.yappy.docstorage.out.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.*;
import ru.yappy.docstorage.model.*;

import java.util.Optional;

@Repository
public interface DocUserAccessRepository extends JpaRepository<DocUserAccess, Long> {

    Optional<AccessType> findAccessTypeByDocIdAndUsername(Long docId, String username);

    @Query("""
           SELECT dua.username FROM DocUserAccess dua
           WHERE dua.docId = :docId
           """)
    String[] findUsernameByDocId(@Param("docId") Long docId);

    @Query("""
           SELECT dua.username FROM DocUserAccess dua
           WHERE dua.docId = :docId AND dua.accessType = :accessType
           """)
    String[] findUsernameByDocIdAndAccessType(@Param("docId") Long docId, @Param("accessType") AccessType accessType);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByDocIdAndUsername(Long docId, String username);

}