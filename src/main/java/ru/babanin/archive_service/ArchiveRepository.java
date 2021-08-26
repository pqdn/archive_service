package ru.babanin.archive_service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.babanin.archive_service.model.Archive;

@Repository
public interface ArchiveRepository extends CrudRepository<Archive, String> {
}
