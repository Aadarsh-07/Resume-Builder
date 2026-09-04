package com.adarsh.resumebuilder.controller;

import com.adarsh.resumebuilder.dto.PageResponse;
import com.adarsh.resumebuilder.model.Resume;
import com.adarsh.resumebuilder.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API with pagination, filtering, and sorting:
 *   GET /api/resumes?page=0&size=10&sortBy=updatedAt&direction=desc&title=backend
 */
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public PageResponse<Resume> list(@AuthenticationPrincipal UserDetails principal,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(defaultValue = "updatedAt") String sortBy,
                                     @RequestParam(defaultValue = "desc") String direction,
                                     @RequestParam(required = false) String title) {
        return resumeService.list(principal.getUsername(), page, size, sortBy, direction, title);
    }

    @GetMapping("/{id}")
    public Resume get(@AuthenticationPrincipal UserDetails principal, @PathVariable String id) {
        return resumeService.get(principal.getUsername(), id);
    }

    @PostMapping
    public ResponseEntity<Resume> create(@AuthenticationPrincipal UserDetails principal,
                                         @RequestBody Resume resume) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.create(principal.getUsername(), resume));
    }

    @PutMapping("/{id}")
    public Resume update(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable String id,
                         @RequestBody Resume resume) {
        return resumeService.update(principal.getUsername(), id, resume);
    }

    @PatchMapping("/{id}/template")
    public Resume switchTemplate(@AuthenticationPrincipal UserDetails principal,
                                 @PathVariable String id,
                                 @RequestBody Map<String, String> body) {
        return resumeService.switchTemplate(principal.getUsername(), id, body.get("template"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable String id) {
        resumeService.delete(principal.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
