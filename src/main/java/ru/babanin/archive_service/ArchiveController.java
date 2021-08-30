package ru.babanin.archive_service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.babanin.archive_service.exceptions.AbsentRequestFIleException;
import ru.babanin.archive_service.exceptions.EmptyRequestFIleException;
import ru.babanin.archive_service.model.ArchiveResponse;

import java.io.ByteArrayInputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @GetMapping(value = "/test")
    public ResponseEntity<?> testZipFile(@RequestParam String content) {
        ArchiveResponse response = archiveService.archive("testFile.txt", new ByteArrayInputStream(content.getBytes()));

        return ResponseEntity
                // Использован 208 статус заместо 304, т.к.
                // для 304 нельзя прилепить тело сообщений
                // для 304 подразумевается, что браузер попытается использовать
                // закешируванную версию файла со своей стороны
                // а задании явно сказано: что нужно возвращать файл с бека из собственного кеша
                .status(HttpStatus.OK)
                .eTag(response.getArchive().getHashOrigin())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + "testFile" + ".zip")
                .body(response.getArchive().getZipBytes());
    }

    @SneakyThrows
    @PostMapping(value = "/zipFile")
    public ResponseEntity<?> zipFile(MultipartFile file) {
        if (file == null) {
            throw new AbsentRequestFIleException();
        }

        if (file.getSize() == 0) {
            throw new EmptyRequestFIleException();
        }

        String fileName = file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename() : file.getName();

        byte[] bytes = file.getBytes();
        log.info("Archive file with name={}, bytes={}", fileName, file.getSize());


        ArchiveResponse response = archiveService.archive(fileName, file.getInputStream());

        return ResponseEntity
                // Использован 208 статус заместо 304, т.к.
                // для 304 нельзя прилепить тело сообщений
                // для 304 подразумевается, что браузер попытается использовать
                // закешируванную версию файла со своей стороны
                // а задании явно сказано: что нужно возвращать файл с бека из собственного кеша
                .status(response.isNewArchive() ? HttpStatus.OK : HttpStatus.ALREADY_REPORTED)
                .eTag(response.getArchive().getHashOrigin())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=" + fileName + ".zip")
                .body(response.getArchive().getZipBytes());
    }


    @ExceptionHandler({ AbsentRequestFIleException.class, EmptyRequestFIleException.class })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleException(RuntimeException ex) {
        if (ex instanceof AbsentRequestFIleException) {
            return "file is absent";
        }

        if (ex instanceof EmptyRequestFIleException) {
            return "file empty";
        }
        throw new IllegalStateException();
    }
}
