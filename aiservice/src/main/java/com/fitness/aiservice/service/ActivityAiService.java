package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAiService {

    private final GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getPrediction(prompt);
        log.info("AI Response : {}", aiResponse);
        return parseAiResponse(activity, aiResponse);
    }

    public Recommendation parseAiResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);

            JsonNode textNode = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text");

            String jsonContent = textNode.asText().replaceAll("```json\\n", "").replaceAll("\\n```", "").trim();
            //    log.info("Parsed AI Response : {}", jsonContent);

            JsonNode analysisJson = objectMapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");
            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "OverAll:");
            addAnalysisSection(fullAnalysis, analysisNode, "pace", "Pace:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "Heart Rate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "Calories:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));

            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));

            List<String> safety = extractsafetyGuidelines(analysisJson.path("safety"));
            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safetyMeasures(safety)
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return createDefaultRecommendation(activity);
        }
    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("No Improvements Provided"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safetyMeasures(Arrays.asList("Always warmup before exercise", "Stay hydrated", "Listen to your body"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractsafetyGuidelines(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            safetyNode.forEach(safetyItem -> safety.add(safetyItem.asText()));
        }
        return safety.isEmpty() ? Collections.singletonList("Follow General Safety Guidelines") : safety;
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(suggestionNode -> {
                String workout = suggestionNode.path("workout").asText();
                String description = suggestionNode.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ? Collections.singletonList("No Suggestions Provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvementNode -> {
                String area = improvementNode.path("area").asText();
                String recommendation = improvementNode.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, recommendation));
            });
        }
        return improvements.isEmpty() ? Collections.singletonList("No Improvements Provided") : improvements;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix).append(analysisNode.path(key).asText()).append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format(""" 
                          Analyze this fitness activity and provide detailed recommendations in the following EXACT JSON format:
                          {
                              "analysis" : {
                                  "overall": "Overall analysis here",
                                  "pace": "Pace analysis here",
                                  "heartRate": "Heart rate analysis here",
                                  "caloriesBurned": "Calories analysis here"
                              },
                              "improvements": [
                                  {
                                      "area": "Area name",
                                      "recommendation": "Detailed Recommendation"
                                  }
                              ],
                              "suggestions" : [
                                  {
                                      "workout": "Workout name",
                                      "description": "Detailed workout description"
                                  }
                              ],
                              "safety": [
                                  "Safety point 1",
                                  "Safety point 2"
                              ]
                          }
                        
                          Analyze this activity:
                          Activity Type: %s
                          Duration: %d minutes
                          calories Burned: %d
                          Additional Metrics: %s
                        
                          Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety guidelines
                          Ensure the response follows the EXACT JSON format shown above.
                        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCaloriesBurned(),
                activity.getAdditionalMetrics()
        );
    }
}
