package com.adarsh.resumebuilder.repository;

import com.adarsh.resumebuilder.model.Resume;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ResumeRepository extends MongoRepository<Resume, String> {

    Page<Resume> findByUserId(String userId, Pageable pageable);

    Page<Resume> findByUserIdAndTitleContainingIgnoreCase(String userId, String title, Pageable pageable);

    Optional<Resume> findByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);
}
