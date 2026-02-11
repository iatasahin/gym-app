package dev.ilkersahin.java.spring.gym.dto.view;

import java.util.List;

public record TrainerWithListView(
        String username,
        String firstName,
        String lastName,
        boolean active,
        String specialization,
        List<UserInfo> trainees
){
}
