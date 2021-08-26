package ru.babanin.archive_service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.babanin.archive_service.model.ArchiveResponse;

import java.util.concurrent.TimeUnit;


@Slf4j
@RestController
@RequiredArgsConstructor
public class ArchiveController {

    private final ArchiveService archiveService;

    @SneakyThrows
    @PostMapping(value = "/zipFile")
    public ResponseEntity<?> zipFile(MultipartFile file) {
        if (file == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("file is absent");
        }

        byte[] bytes = file.getBytes();

        if (bytes.length == 0) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("file empty");
        }


        String fileName = file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename() : file.getName();

        log.info("Archive file with name={}, bytes={}", fileName, bytes.length);
        ArchiveResponse response = archiveService.archive(fileName, bytes);

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
}