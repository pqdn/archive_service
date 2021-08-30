package ru.babanin.archive_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.babanin.archive_service.configuration.RedisIntegrationTest;
import ru.babanin.archive_service.model.Archive;
import ru.babanin.archive_service.model.ArchiveResponse;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;

class ArchiveServiceTest extends RedisIntegrationTest {

    @Autowired
    private ArchiveService archiveService;

    @Test
    void shouldArchiveFile() {
        String fileName = "name";
        byte[] content = "12345".getBytes();

        ArchiveResponse archiveResponse = archiveService.archive(fileName, new ByteArrayInputStream(content));
        assertTrue(archiveResponse.isNewArchive());

        Archive archive = archiveResponse.getArchive();
        assertFalse(archiveResponse.getArchive().getHashOrigin().isBlank());
        assertTrue(archiveResponse.getArchive().getZipBytes().length > 0);

        ArchiveResponse archiveResponse2 = archiveService.archive(fileName, new ByteArrayInputStream(content));
        assertFalse(archiveResponse2.isNewArchive());
    }


}