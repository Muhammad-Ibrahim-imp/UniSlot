package DBMS.UniSlot.Backend.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * AppConfig — General application configuration bean.
 *
 * Ensures the PDF output directory exists on startup.
 * If the directory cannot be created, startup fails fast with a clear error.
 */
@Configuration
@Getter
@Slf4j
public class AppConfig {

    @Value("${app.pdf.output-dir}")
    private String pdfOutputDir;

    /**
     * Runs once after Spring has injected all @Value fields.
     * Creates the PDF output directory if it does not exist.
     */
    @PostConstruct
    public void init() throws IOException {
        Path pdfPath = Paths.get(pdfOutputDir);
        if (!Files.exists(pdfPath)) {
            Files.createDirectories(pdfPath);
            log.info("Created PDF output directory: {}", pdfPath.toAbsolutePath());
        } else {
            log.info("PDF output directory exists: {}", pdfPath.toAbsolutePath());
        }
    }
}
