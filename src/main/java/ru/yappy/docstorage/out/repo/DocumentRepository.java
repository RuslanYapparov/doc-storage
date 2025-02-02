package ru.yappy.docstorage.out.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yappy.docstorage.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}