package com.adarsh.resumebuilder.service;

import com.adarsh.resumebuilder.dto.PageResponse;
import com.adarsh.resumebuilder.model.Resume;
import com.adarsh.resumebuilder.model.User;
import com.adarsh.resumebuilder.repository.ResumeRepository;
import com.adarsh.resumebuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Service layer: all business rules live here, controllers stay thin,
 * repositories stay dumb — classic Controller-Service-Repository.
 * Every operation is scoped to the authenticated owner's userId.
 */
@Service
@RequiredArgsConstructor
public class ResumeService {

    private static final Set<String> SORTABLE = Set.of("updatedAt", "createdAt", "title");
    private static final Set<String> TEMPLATES = Set.of("classic", "modern", "compact");

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public PageResponse<Resume> list(String email, int page, int size,
                                     String sortBy, String direction, String titleFilter) {
        String userId = userIdOf(email);
        String sortField = SORTABLE.contains(sortBy) ? sortBy : "updatedAt";
        Sort sort = "asc".equalsIgnoreCase(direction) ? Sort.by(sortField).ascending()
                                                      : Sort.by(sortField).descending();
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), sort);

        var result = (titleFilter == null || titleFilter.isBlank())
                ? resumeRepository.findByUserId(userId, pageable)
                : resumeRepository.findByUserIdAndTitleContainingIgnoreCase(userId, titleFilter, pageable);

        return PageResponse.from(result);
    }

    public Resume get(String email, String id) {
        return resumeRepository.findByIdAndUserId(id, userIdOf(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
    }

    public Resume create(String email, Resume resume) {
        resume.setId(null);
        resume.setUserId(userIdOf(email));
        validateTemplate(resume.getTemplate());
        return resumeRepository.save(resume);
    }

    public Resume update(String email, String id, Resume incoming) {
        Resume existing = get(email, id); // enforces ownership
        validateTemplate(incoming.getTemplate());
        existing.setTitle(incoming.getTitle());
        existing.setTemplate(incoming.getTemplate());
        existing.setPersonalInfo(incoming.getPersonalInfo());
        existing.setSections(incoming.getSections());
        return resumeRepository.save(existing);
    }

    /** Template switching: change only the template, keep all content. */
    public Resume switchTemplate(String email, String id, String template) {
        validateTemplate(template);
        Resume existing = get(email, id);
        existing.setTemplate(template);
        return resumeRepository.save(existing);
    }

    public void delete(String email, String id) {
        get(email, id); // 404 if not owned
        resumeRepository.deleteByIdAndUserId(id, userIdOf(email));
    }

    private void validateTemplate(String template) {
        if (template != null && !TEMPLATES.contains(template)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unknown template '" + template + "'. Valid: " + TEMPLATES);
        }
    }

    private String userIdOf(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    }
}
