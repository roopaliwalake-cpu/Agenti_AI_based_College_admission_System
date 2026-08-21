package com.bcu.admission.service;

import com.bcu.admission.dto.CourseRecommendation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class CourseCatalogService {
    private static final String BCU_SOURCE = "Bangalore University admission and course notifications";

    public List<CourseRecommendation> recommend(String degree, Double percentage, String interestArea) {
        String normalizedDegree = degree == null ? "" : degree.toLowerCase(Locale.ROOT);
        String interest = interestArea == null ? "" : interestArea.toLowerCase(Locale.ROOT);
        double marks = percentage == null ? 0 : percentage;
        List<CourseRecommendation> result = new ArrayList<>();

        if (hasComputerBackground(normalizedDegree) && hasComputerInterest(interest) && marks >= 50) {
            result.add(new CourseRecommendation(
                    "MCA",
                    "2 years",
                    "Bachelor's degree with Computer Science/Computer Applications/Mathematics background, subject to Bangalore University notification.",
                    "Strong match for software development, AI, and application-building interests.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.Sc. Computer Science",
                    "2 years",
                    "Bachelor's degree in Computer Science/Computer Applications or related discipline, subject to Bangalore University notification.",
                    "Good fit for advanced computing, research, AI, data, and systems topics.",
                    BCU_SOURCE
            ));
        }

        if ((hasCommerceInterest(interest) || normalizedDegree.contains("b.com") || normalizedDegree.contains("bba"))
                && !hasHumanitiesInterest(interest)
                && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.Com Accounting",
                    "2 years",
                    "Bachelor's degree in Commerce/Management or equivalent, subject to Bangalore University notification.",
                    "Suitable for accounting, audit, taxation, and finance careers.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.Com Finance",
                    "2 years",
                    "Bachelor's degree in Commerce/Management or equivalent, subject to Bangalore University notification.",
                    "Good fit for financial analysis, banking, investment, and corporate finance interests.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.Com Banking and Insurance",
                    "2 years",
                    "Bachelor's degree in Commerce/Management or equivalent, subject to Bangalore University notification.",
                    "Useful for banking, insurance, financial services, and compliance-oriented careers.",
                    BCU_SOURCE
            ));
        }

        if (hasLifeScienceBackground(normalizedDegree) && hasLifeScienceInterest(interest) && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.Sc. Zoology",
                    "2 years",
                    "Bachelor's degree in Zoology/Life Sciences or relevant discipline, subject to Bangalore University notification.",
                    "Good match for animal biology, ecology, physiology, and life science research interests.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.Sc. Botany",
                    "2 years",
                    "Bachelor's degree in Botany/Life Sciences or relevant discipline, subject to Bangalore University notification.",
                    "Good match for plant science, ecology, taxonomy, and life science research interests.",
                    BCU_SOURCE
            ));
        }

        if (hasPoliticsInterest(interest) && hasArtsBackground(normalizedDegree) && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.A. Political Science",
                    "2 years",
                    "Bachelor's degree in Political Science or a relevant arts/social science discipline, subject to Bangalore University rules.",
                    "Good match for politics, public administration, governance, and policy interests.",
                    BCU_SOURCE
            ));
        }

        if (hasEconomicsInterest(interest) && hasArtsBackground(normalizedDegree) && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.A. Economics",
                    "2 years",
                    "Bachelor's degree in Economics or a relevant arts/social science discipline, subject to Bangalore University rules.",
                    "Good match for economic policy, markets, development, and research interests.",
                    BCU_SOURCE
            ));
        }

        if (hasHistoryInterest(interest) && hasArtsBackground(normalizedDegree) && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.A. History",
                    "2 years",
                    "Bachelor's degree in History or a relevant arts/humanities discipline, subject to Bangalore University rules.",
                    "Good match for history, culture, archives, research, and civil services-oriented interests.",
                    BCU_SOURCE
            ));
        }

        if (hasArtsBackground(normalizedDegree) && !hasHumanitiesInterest(interest) && marks >= 45) {
            result.add(new CourseRecommendation(
                    "M.A. Political Science",
                    "2 years",
                    "Bachelor's degree in Political Science or a relevant arts/social science discipline, subject to Bangalore University rules.",
                    "Available option for arts and social science graduates.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.A. Economics",
                    "2 years",
                    "Bachelor's degree in Economics or a relevant arts/social science discipline, subject to Bangalore University rules.",
                    "Available option for arts and social science graduates.",
                    BCU_SOURCE
            ));
            result.add(new CourseRecommendation(
                    "M.A. History",
                    "2 years",
                    "Bachelor's degree in History or a relevant arts/humanities discipline, subject to Bangalore University rules.",
                    "Available option for arts and humanities graduates.",
                    BCU_SOURCE
            ));
        }

        return result;
    }

    private boolean hasComputerBackground(String normalizedDegree) {
        return normalizedDegree.contains("bca")
                || normalizedDegree.contains("computer")
                || normalizedDegree.contains("b.sc");
    }

    private boolean hasComputerInterest(String interest) {
        return interest.isBlank()
                || interest.contains("not specified")
                || interest.contains("artificial intelligence")
                || interest.contains("software")
                || interest.contains("data")
                || interest.contains("computer");
    }

    private boolean hasCommerceInterest(String interest) {
        return interest.isBlank()
                || interest.contains("not specified")
                || interest.contains("account")
                || interest.contains("bank")
                || interest.contains("commerce")
                || interest.contains("finance")
                || interest.contains("insurance")
                || interest.contains("tax");
    }

    private boolean hasPoliticsInterest(String interest) {
        return interest.contains("politics") || interest.contains("political");
    }

    private boolean hasEconomicsInterest(String interest) {
        return interest.contains("economics") || interest.contains("economic");
    }

    private boolean hasHistoryInterest(String interest) {
        return interest.contains("history") || interest.contains("historical");
    }

    private boolean hasHumanitiesInterest(String interest) {
        return hasPoliticsInterest(interest)
                || hasEconomicsInterest(interest)
                || hasHistoryInterest(interest);
    }

    private boolean hasArtsBackground(String normalizedDegree) {
        return normalizedDegree.equals("ba")
                || normalizedDegree.contains("b.a")
                || normalizedDegree.contains("ba ")
                || normalizedDegree.contains("bachelor of arts")
                || normalizedDegree.contains("political")
                || normalizedDegree.contains("arts")
                || normalizedDegree.contains("social");
    }

    private boolean hasLifeScienceBackground(String normalizedDegree) {
        return normalizedDegree.contains("b.sc")
                || normalizedDegree.contains("bsc")
                || normalizedDegree.contains("zoology")
                || normalizedDegree.contains("botany")
                || normalizedDegree.contains("life science")
                || normalizedDegree.contains("biology");
    }

    private boolean hasLifeScienceInterest(String interest) {
        return interest.isBlank()
                || interest.contains("not specified")
                || interest.contains("zoology")
                || interest.contains("botany")
                || interest.contains("biology")
                || interest.contains("life science");
    }
}
