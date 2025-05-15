package com.example.providercomparison.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Table("service_rating")
public class ServiceRating {

    @Id
    private Long id;

    private String serviceName;
    private String userName;
    private String email;
    private Integer ranking;   // 1-5
    private String comment;
    private LocalDateTime createdAt;
}
