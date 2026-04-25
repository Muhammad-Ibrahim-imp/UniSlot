package DBMS.UniSlot.Backend.controller;



import DBMS.UniSlot.Backend.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * PdfController — authenticated students only.
 *
 * GET /api/pdf/timetable
 *   Generates and returns the student's weekly timetable as a
 *   downloadable PDF file (Content-Disposition: attachment).
 *
 * The PDF is generated on-demand by PdfService, saved to disk,
 * and streamed back to the client using Spring's FileSystemResource.
 * Subsequent calls overwrite the previous file (idempotent per student).
 */
@Slf4j
@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@Tag(name = "PDF Generation", description = "Download weekly timetable as PDF")
@SecurityRequirement(name = "bearerAuth")
public class PdfController {

    private final PdfService pdfService;

    @GetMapping("/timetable")
    @Operation(summary = "Download your weekly timetable as a PDF",
            description = "Generates a PDF timetable grid with course, professor, venue, " +
                    "and time for each enrolled slot. Requires at least one enrolled slot.")
    public ResponseEntity<Resource> downloadTimetable(
            @AuthenticationPrincipal UserDetails user) {

        // Generate PDF and get the file path
        String pdfPath = pdfService.generateTimetablePdf(user.getUsername());

        File pdfFile = new File(pdfPath);
        Resource resource = new FileSystemResource(pdfFile);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                // attachment triggers browser "Save As" dialog
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pdfFile.getName() + "\"")
                .body(resource);
    }
}

