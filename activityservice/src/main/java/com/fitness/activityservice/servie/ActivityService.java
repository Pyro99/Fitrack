package com.fitness.activityservice.servie;

import com.fitness.activityservice.dto.ActivityRequestDTO;
import com.fitness.activityservice.dto.ActivityResponseDTO;
import com.fitness.activityservice.dto.mapper.ActivityMapper;
import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService userValidationService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public ActivityResponseDTO trackActivity(ActivityRequestDTO requestDto) {

        boolean isValidUser = userValidationService.validateUser(requestDto.getUserId());
        if (!isValidUser) {
            throw new RuntimeException("Invalid User" + requestDto.getUserId());
        }
        Activity activity = ActivityMapper.toModel(requestDto);

        Activity savedActivity = activityRepository.save(activity);

        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, savedActivity);
        } catch (Exception e) {
            log.error("Failed to publish activity to RabbitMQ ", e);
        }

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
