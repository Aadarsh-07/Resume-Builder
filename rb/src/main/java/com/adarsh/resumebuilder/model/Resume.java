package com.adarsh.resumebuilder.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "resumes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resume {

    @Id
    private String id;

    /** Owner's user id — every query is scoped to this. */
    @Indexed
    private String userId;

    private String title;

    /** e.g. "classic", "modern", "compact" — drives template switching on the frontend. */
    @Builder.Default
    private String template = "classic";

    private PersonalInfo personalInfo;
    private List<Section> sections;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonalInfo {
        private String fullName;
        private String email;
        private String phone;
        private String location;
        private Map<String, String> links;
    }

    /**
     * Generic section (Experience, Education, Projects, Skills...) so the
     * document model stays flexible — a natural fit for MongoDB.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Section {
        private String heading;
        private int order;
        private List<Map<String, Object>> items;
    }
}
