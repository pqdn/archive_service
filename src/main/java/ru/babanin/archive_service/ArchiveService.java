package ru.babanin.archive_service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import ru.babanin.archive_service.model.Archive;
import ru.babanin.archive_service.model.ArchiveResponse;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;

    public ArchiveResponse archive(String name, byte[] bytes) {
        String hash = calculateHash(bytes);

        Optional<Archive> cacheArchive = archiveRepository.findById(hash);

        Archive currentArchive = cacheArchive
                .orElseGet(
                        () -> {
                            byte[] zipBytes = zipBytes(name, bytes);
                            Archive archive = new Archive(hash, zipBytes);
                            archiveRepository.save(archive);
                            return archive;
                        }
                );

        return new ArchiveResponse(currentArchive, cacheArchive.isEmpty());
    }

    private String calculateHash(byte[] bytes) {
        return DigestUtils.md5DigestAsHex(bytes);
    }

    @SneakyThrows
    private byte[] zipBytes(String filename, byte[] input) {
        log.debug("real zipping");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(input.length);
        zos.putNextEntry(entry);
        zos.write(input);
        zos.closeEntry();
        zos.close();
        return baos.toByteArray();
    }
}

