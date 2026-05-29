package com.studgrasp.api.domain.dashboard;

import com.studgrasp.api.domain.dashboard.dto.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
public class AiInsightsClient {

    private final RestTemplate restTemplate;
    private final String aiApiUrl;

    public AiInsightsClient(RestTemplateBuilder builder,
                            @Value("${ai.api.url}") String aiApiUrl) {
        this.aiApiUrl = aiApiUrl;
        this.restTemplate = builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(15))
                .build();
    }

    public InsightResponseDTO fetchInsights(InsightRequestDTO body) {
        try {
            String url = aiApiUrl + "/ai/insights/" + body.getUserId();
            return restTemplate.postForObject(url, body, InsightResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch AI insights for user {}: {}", body.getUserId(), e.getMessage());
            return new InsightResponseDTO(body.getUserId(), Collections.emptyList());
        }
    }

    public ClassInsightResponseDTO fetchClassInsights(ClassInsightRequestDTO body) {
        try {
            String url = aiApiUrl + "/ai/insights/class/" + body.getClassId();
            return restTemplate.postForObject(url, body, ClassInsightResponseDTO.class);
        } catch (Exception e) {
            log.error("Failed to fetch AI class insights for class {}: {}", body.getClassId(), e.getMessage());
            return new ClassInsightResponseDTO(body.getClassId(), Collections.emptyList());
        }
    }
}
