package com.fitness.activityservice.dto.mapper;

import com.fitness.activityservice.dto.ActivityRequestDTO;
import com.fitness.activityservice.dto.ActivityResponseDTO;
import com.fitness.activityservice.model.Activity;

public class ActivityMapper {

    public static ActivityResponseDTO toDTO(Activity activity) {
        ActivityResponseDTO activityResponse = new ActivityResponseDTO();

        activityResponse.setId(activity.getId());
        activityResponse.setUserId(activity.getUserId());
        activityResponse.setType(activity.getType());
        activityResponse.setDuration(activity.getDuration());
        activityResponse.setStartTime(activity.getStartTime());
        activityResponse.setCaloriesBurned(activity.getCaloriesBurned());
        activityResponse.setAdditionalMetrics(activity.getAdditionalMetrics());
        activityResponse.setCreatedAt(activity.getCreatedAt());
        activityResponse.setUpdatedAt(activity.getUpdatedAt());

        return activityResponse;
    }

    public static Activity toModel(ActivityRequestDTO requestDTO) {
        Activity activity = new Activity();

        activity.setUserId(requestDTO.getUserId());
        activity.setType(requestDTO.getType());
        activity.setDuration(requestDTO.getDuration());
        activity.setCaloriesBurned(requestDTO.getCaloriesBurned());
        activity.setStartTime(requestDTO.getStartTime());
        activity.setAdditionalMetrics(requestDTO.getAdditionalMetrics());

        return activity;
    }
}
