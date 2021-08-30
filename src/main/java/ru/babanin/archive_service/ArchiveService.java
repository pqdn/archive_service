package ru.babanin.archive_service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import ru.babanin.archive_service.model.Archive;
import ru.babanin.archive_service.model.ArchiveResponse;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;

    public ArchiveResponse archive(String name, InputStream inputStream) {
        String hash = calculateHash(inputStream);

        Optional<Archive> cacheArchive = archiveRepository.findById(hash);

        Archive currentArchive = cacheArchive
                .orElseGet(
                        () -> {
                            ByteArrayOutputStream zipOutputStream = zipBytes(name, inputStream);
                            Archive archive = new Archive(hash, zipOutputStream.toByteArray());
                            archiveRepository.save(archive);
                            return archive;
                        }
                );

        return new ArchiveResponse(currentArchive, cacheArchive.isEmpty());
    }

    @SneakyThrows
    private String calculateHash(InputStream bytes) {
        return DigestUtils.md5DigestAsHex(bytes);
    }

    @SneakyThrows
    private ByteArrayOutputStream zipBytes(String filename, InputStream input) {
        log.debug("real zipping");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);

        byte[] buf = new byte[1024];
        int length;
        int fullLength = 0;
        while ((length = input.read(buf)) > 0) {
            zos.write(buf, 0, length);
            fullLength += length;
        }

        ZipEntry entry = new ZipEntry(filename);
        entry.setSize(fullLength);
        zos.putNextEntry(entry);

        zos.closeEntry();
        zos.close();
        return baos;
    }
}

