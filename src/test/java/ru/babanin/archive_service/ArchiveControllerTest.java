package ru.babanin.archive_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.babanin.archive_service.configuration.IntegrationTest;
import ru.babanin.archive_service.model.Archive;
import ru.babanin.archive_service.model.ArchiveResponse;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ArchiveControllerTest extends IntegrationTest {

    @MockBean
    private ArchiveService archiveService;

    @Autowired
    private MockMvc mockMvc;

    @SneakyThrows
    @Test
    void shouldSendRequestWithoutFile() {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.post("/zipFile")
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals("file is absent", result.getResponse().getContentAsString());
    }

    @SneakyThrows
    @Test
    void shouldSendRequestWithEmptyFile() {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/zipFile")
                                .file("file", "".getBytes())
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals("file empty", result.getResponse().getContentAsString());
    }

    @SneakyThrows
    @Test
    void shouldSendRequestWithNormalFile() {
        byte[] zipBytes = "54321".getBytes();
        when(archiveService.archive(any(), any()))
                .thenReturn(
                        new ArchiveResponse(
                                new Archive("hash", zipBytes),
                                true
                        )
                );


        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/zipFile")
                                .file("file", "12345".getBytes())
                )
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(header().stringValues("ETag", "\"hash\""))
                .andExpect(header().stringValues("Content-Disposition", "attachment; filename=file.zip"))
                .andReturn();

        assertArrayEquals(zipBytes, result.getResponse().getContentAsByteArray());
    }

    @SneakyThrows
    @Test
    void shouldSendRequestWithNormalFileRetry() {
        byte[] zipBytes = "54321".getBytes();
        when(archiveService.archive(any(), any()))
                .thenReturn(
                        new ArchiveResponse(
                                new Archive("hash", zipBytes),
                                false
                        )
                );


        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/zipFile")
                                .file("file", "12345".getBytes())
                )
                .andExpect(status().is(HttpStatus.ALREADY_REPORTED.value()))
                .andExpect(header().stringValues("ETag", "\"hash\""))
                .andExpect(header().stringValues("Content-Disposition", "attachment; filename=file.zip"))
                .andReturn();

        assertArrayEquals(zipBytes, result.getResponse().getContentAsByteArray());
    }
}