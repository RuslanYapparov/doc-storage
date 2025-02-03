package ru.yappy.docstorage.out.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.yappy.docstorage.model.DocUserAccess;

@Repository
public interface DocUserAccessRepository extends JpaRepository<DocUserAccess, Long> {

    boolean existsByDocIdAndUsername(Long docId, String username);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteByDocIdAndUsername(Long docId, String username);

}