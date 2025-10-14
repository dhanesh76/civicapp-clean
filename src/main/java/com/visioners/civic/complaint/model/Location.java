package com.visioners.civic.complaint.model;

 
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Location {
    private double latitude;
    private double longitude;
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
