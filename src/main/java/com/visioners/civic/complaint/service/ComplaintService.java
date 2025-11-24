package com.visioners.civic.complaint.service;

import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

import com.visioners.civic.complaint.dto.ComplaintView;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.model.Location;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.ComplaintNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    public Complaint getComplaint(long complaintId){
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new  ComplaintNotFoundException ("no complaint exists with id: " + complaintId));
    }   

    //helper methods
    public static ComplaintView getComplaintView(Complaint complaint){
        return ComplaintView.builder()
                    .complaintId(complaint.getComplaintId())
                    .raidedBy(complaint.getRaisedBy().getMobileNumber())
                    .imageUrl(complaint.getImageUrl())
                    .assignedBy(complaint.getAssignedBy().getUser().getUsername())
                    .assignedTo(complaint.getAssignedTo().getUser().getUsername())
                    .severity(complaint.getSeverity())
                    .status(complaint.getStatus())
                    .solutionImageUrl(complaint.getSolutionImageUrl())
                    .solutionNote(complaint.getSolutionNote())
                    .location(convertToLocation(complaint.getLocation(), complaint.getLocationPoint()))
                    .build();
    }

    public static ComplaintViewDTO mapToComplaintViewDTO(Complaint complaint) {
        if (complaint == null) {
            return null;
        }

        return ComplaintViewDTO.builder()
                .complaintId(complaint.getComplaintId())
                .description(complaint.getDescription())
                .status(complaint.getStatus())
                .severity(complaint.getSeverity() != null ? complaint.getSeverity() : null)
                .location(convertToLocation(complaint.getLocation(), complaint.getLocationPoint()))
                .assignedBy(
                    complaint.getAssignedBy() != null 
                        ? complaint.getAssignedBy().getUser().getUsername()
                        : null
                )
                .assignedTo(
                    complaint.getAssignedTo() != null 
                        ? complaint.getAssignedTo().getUser().getUsername()
                        : null
                )
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .assignedAt(complaint.getAssignedAt())
                .resolvedAt(complaint.getResolvedAt())
                .solutionNote(complaint.getSolutionNote())
                .solutionImageUrl(complaint.getSolutionImageUrl())
                .rejectionNote(complaint.getRejectionNote())
                .build();
    }

    public static Location convertToLocation(Location location, Point point){
        Location.LocationBuilder builder = Location.builder();

        if (location != null) {
            builder.accuracy(location.getAccuracy())
                    .adminArea(location.getAdminArea())
                    .altitude(location.getAltitude())
                    .country(location.getCountry())
                    .isoCountryCode(location.getIsoCountryCode())
                    .locality(location.getLocality())
                    .postalCode(location.getPostalCode())
                    .street(location.getStreet())
                    .subAdminArea(location.getSubAdminArea())
                    .subLocality(location.getSubLocality());
        }

        if (point != null) {
            builder.longitude(point.getX())
                    .latitude(point.getY());
        } else if (location != null) {
            // if the persisted point is missing, fall back to any lat/lon present in the embeddable
            builder.longitude(location.getLongitude())
                    .latitude(location.getLatitude());
        }

        return builder.build();

    }
}
