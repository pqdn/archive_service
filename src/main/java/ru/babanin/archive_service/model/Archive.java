package ru.babanin.archive_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@AllArgsConstructor
@RedisHash("Archive")
public class Archive {
    @Id
    private String hashOrigin;
    private byte[] zipBytes;
}
