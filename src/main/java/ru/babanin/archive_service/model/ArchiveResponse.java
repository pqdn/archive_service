package ru.babanin.archive_service.model;

import lombok.Data;

@Data
public class ArchiveResponse {
    private final Archive archive;
    private final boolean isNewArchive;
}
