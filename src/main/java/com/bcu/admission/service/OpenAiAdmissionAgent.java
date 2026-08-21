package com.bcu.admission.service;

import com.bcu.admission.dto.AgentResponse;
import com.bcu.admission.dto.CourseRecommendation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiAdmissionAgent {
    private final String apiKey;
    private final String model;
    private final String responsesUrl;
    private final RestClient restClient;

    public OpenAiAdmissionAgent(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.model}") String model,
            @Value("${openai.responses.url}") String responsesUrl
    ) {
        this.apiKey = apiKey;
        this.model = model;
        this.responsesUrl = responsesUrl;
        this.restClient = RestClient.create();
    }

    public String enhanceGuidance(String studentMessage, AgentResponse fallback) {
        if (apiKey == null || apiKey.isBlank()) {
            return fallback.summary();
        }

        List<CourseRecommendation> courses = fallback.recommendations();
        String prompt = """
                You are an admission assistant for Bangalore University postgraduate admissions.
                Use only cautious guidance. Do not invent final eligibility rules, fees, or deadlines.
                Student message: %s
                Extracted profile: degree=%s, percentage=%s, interest=%s
                Local recommendation candidates: %s
                Provide a short helpful summary and ask the next best question.
                """.formatted(
                studentMessage,
                fallback.extractedDegree(),
                fallback.extractedPercentage(),
                fallback.interestArea(),
                courses
        );

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "input", prompt
            );

            Map<?, ?> response = restClient.post()
                    .uri(responsesUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            Object outputText = response == null ? null : response.get("output_text");
            if (outputText instanceof String text && !text.isBlank()) {
                return text;
            }
        } catch (RuntimeException ignored) {
            return fallback.summary();
        }
        return fallback.summary();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
