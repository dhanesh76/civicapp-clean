package com.visioners.civic.ml;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MLService {

    /**
     * Validate image via ML.
     * Current implementation is a stub that accepts everything.
     * Replace with actual ML inference integration.
     */
    public boolean validateImage(MultipartFile image) {
        return true; // TODO: implement ML validation
    }

    public String routeDepartment(String description){
       return description.startsWith("manual") ? "UNKNOWN" : "Road Construction Department (RCD)";
    }
}
