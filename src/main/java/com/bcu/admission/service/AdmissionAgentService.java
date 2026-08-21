package com.bcu.admission.service;

import com.bcu.admission.dto.AgentResponse;
import com.bcu.admission.dto.CourseRecommendation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AdmissionAgentService {
    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(\\d{2}(?:\\.\\d+)?)\\s*%?");

    private final CourseCatalogService courseCatalogService;
    private final OpenAiAdmissionAgent openAiAdmissionAgent;

    public AdmissionAgentService(CourseCatalogService courseCatalogService, OpenAiAdmissionAgent openAiAdmissionAgent) {
        this.courseCatalogService = courseCatalogService;
        this.openAiAdmissionAgent = openAiAdmissionAgent;
    }

    public AgentResponse analyze(String message) {
        String degree = extractDegree(message);
        Double percentage = extractPercentage(message);
        String interest = extractInterest(message);
        List<CourseRecommendation> recommendations = courseCatalogService.recommend(degree, percentage, interest);
        String summary = recommendations.isEmpty()
                ? "Sorry, based on the qualification provided, this system could not find a matching postgraduate course. Please verify eligibility with the university admission office or try entering another recognised bachelor's degree."
                : "Based on your profile, I found " + recommendations.size()
                + " suitable Bangalore University course option(s). Please select a course to review fees and continue.";
        AgentResponse fallback = new AgentResponse(
                summary,
                degree,
                percentage,
                interest,
                recommendations,
                recommendations.isEmpty()
                        ? "Please enter another qualification or contact the admission office for counselling."
                        : "Which Bangalore University course do you want to apply for?",
                false
        );

        String enhancedSummary = openAiAdmissionAgent.enhanceGuidance(message, fallback);
        return new AgentResponse(
                enhancedSummary,
                degree,
                percentage,
                interest,
                recommendations,
                fallback.nextQuestion(),
                openAiAdmissionAgent.isConfigured()
        );
    }

    private String extractDegree(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("bca")) return "BCA";
        if (lower.contains("b.sc") || lower.contains("bsc")) return "B.Sc.";
        if (lower.contains("b.com") || lower.contains("bcom")) return "B.Com";
        if (lower.contains("bba")) return "BBA";
        if (lower.contains("b.a") || lower.contains("ba ") || lower.contains("bachelor of arts")) return "B.A.";
        if (lower.contains("bachelor")) return "Bachelor's Degree";
        return "Not specified";
    }

    private Double extractPercentage(String message) {
        Matcher matcher = PERCENTAGE_PATTERN.matcher(message);
        while (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            if (value <= 100) {
                return value;
            }
        }
        return null;
    }

    private String extractInterest(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("artificial intelligence") || lower.contains(" ai")) return "Artificial Intelligence";
        if (lower.contains("software")) return "Software Development";
        if (lower.contains("data")) return "Data Science";
        if (lower.contains("finance")) return "Finance";
        if (lower.contains("account")) return "Accounting";
        if (lower.contains("bank")) return "Banking";
        if (lower.contains("insurance")) return "Insurance";
        if (lower.contains("commerce")) return "Commerce";
        if (lower.contains("economics") || lower.contains("economic")) return "Economics";
        if (lower.contains("history")) return "History";
        if (lower.contains("zoology")) return "Zoology";
        if (lower.contains("botany")) return "Botany";
        if (lower.contains("biology") || lower.contains("life science")) return "Life Science";
        if (lower.contains("politics") || lower.contains("political")) return "Politics";
        return "Not specified";
    }
}
