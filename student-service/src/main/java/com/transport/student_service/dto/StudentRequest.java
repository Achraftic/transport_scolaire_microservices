package com.transport.student_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {
    private String firstName;
    private String lastName;
    private Double latitude;
    private Double longitude;
}
