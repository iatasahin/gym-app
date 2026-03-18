package dev.ilkersahin.java.spring.gym.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "spring-gym-workload",
        configuration = FeignClientConfig.class,
        fallbackFactory = WorkloadClientFallbackFactory.class
)
public interface WorkloadClient {

    @PostMapping("/api/v1/workload")
    void sendWorkloadAction(@RequestBody WorkloadRequest request);
}