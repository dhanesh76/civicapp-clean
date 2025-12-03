package com.visioners.civic.complaint.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor

public class Location {
    @Transient
    private Double latitude;

    @Transient
    private Double longitude;
    
    private String department;
    private Long departmentId;
    private String block;        //block
    private String district;    //district
    private String state;       //state
}
