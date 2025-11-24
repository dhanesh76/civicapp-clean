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
    
    private double accuracy;
    private double altitude;
    private String street;
    private String subLocality;     
    private String locality;        //block
    private String subAdminArea;    //district
    private String adminArea;       //state
    private String postalCode;  
    private String country;
    private String isoCountryCode;
}
