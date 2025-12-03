package com.visioners.civic.util;

import org.springframework.stereotype.Service;

import com.visioners.civic.complaint.model.Location;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintIdGenerator {

    private final CounterService counterService;
    
    public String generateComplaintId(Location location) {
        String statecode = safeCode(location.getState(), 2);
        String districtcode = safeCode(location.getDistrict(), 3);
        String blockcode = safeCode(location.getBlock(), 3);

        long counterValue = counterService.increment();
        return statecode + "-" + districtcode + "-" + blockcode + "-" + Base62.encode(counterValue);
    }

    private String safeCode(String s, int len) {
        if (s == null) return "XX";
        s = s.replaceAll("[^A-Za-z]", "");
        if (s.length() < len) return s.toUpperCase();
        return s.substring(0, len).toUpperCase();
    }
}
