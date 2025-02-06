package ru.yappy.docstorage.out.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yappy.docstorage.model.*;

import java.util.Optional;

@Repository
public interface DocUserAccessRepository extends JpaRepository<DocUserAccess, Long> {

    Optional<DocUserAccess> findByDocIdAndUsername(Long docId, String username);

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

    void deleteByDocIdAndUsername(Long docId, String username);

}