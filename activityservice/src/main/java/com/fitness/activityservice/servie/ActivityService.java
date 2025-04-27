package com.fitness.activityservice.servie;

import com.fitness.activityservice.dto.ActivityRequestDTO;
import com.fitness.activityservice.dto.ActivityResponseDTO;
import com.fitness.activityservice.dto.mapper.ActivityMapper;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;

    public ActivityResponseDTO trackActivity(ActivityRequestDTO requestDto) {

        boolean isValidUser = userValidationService.validateUser(requestDto.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid User" + requestDto.getUserId());
        }
        Activity activity = ActivityMapper.toModel(requestDto);

        Activity savedActivity = activityRepository.save(activity);

        return ActivityMapper.toDTO(savedActivity);
    }

    public List<ActivityResponseDTO> getUserActivities(String userId) {
        List<Activity> activities = activityRepository.findByUserId(userId);
        return activities.stream().map(ActivityMapper::toDTO).toList();
    }

    public ActivityResponseDTO getActivityById(String activityId) {

        return activityRepository.findById(activityId).map(ActivityMapper::toDTO).orElseThrow(() -> new RuntimeException("Activity with id: " + activityId + " not found"));
    }
}
