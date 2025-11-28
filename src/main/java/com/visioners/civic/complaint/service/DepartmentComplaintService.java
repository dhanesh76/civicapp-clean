package com.visioners.civic.complaint.service;

import java.time.Instant;
import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.visioners.civic.auth.userdetails.UserPrincipal;
import com.visioners.civic.complaint.Specifications.ComplaintSpecification;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.AssignComplaintDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.ComplaintViewDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentComplaintStatisticsDTO;
import com.visioners.civic.complaint.dto.departmentcomplaintdtos.RejectComplaintDto;
import com.visioners.civic.complaint.entity.Complaint;
import com.visioners.civic.complaint.exception.InvalidStatusTransitionException;
import com.visioners.civic.complaint.exception.ResourceNotFoundException;
import com.visioners.civic.complaint.model.IssueSeverity;
import com.visioners.civic.complaint.model.IssueStatus;
import com.visioners.civic.complaint.model.NotificationType;
import com.visioners.civic.complaint.repository.ComplaintRepository;
import com.visioners.civic.exception.*;
import com.visioners.civic.notification.ComplaintNotificationService;
import com.visioners.civic.staff.entity.Staff;
import com.visioners.civic.staff.repository.StaffRepository;
import com.visioners.civic.staff.service.StaffService;
import com.visioners.civic.util.SmsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepartmentComplaintService {

    private final StaffService staffService;
    private final ComplaintService complaintService;
    private final ComplaintRepository complaintRepository;
    private final StaffRepository staffRepository;
    private final SmsService smsService;
    private final ComplaintNotificationService notificationService;
    private final ComplaintFeedbackService complaintFeedbackService;

    /** View complaints with optional filters */
    public Page<ComplaintViewDTO> viewDeptComplaints(UserPrincipal principal, Pageable page,
            IssueSeverity severity, IssueStatus status, Date from, Date to) {

        Staff officer = staffService.getStaff(principal.getUser());

        Specification<Complaint> specification = Specification.unrestricted();
        specification  = specification
                .and(ComplaintSpecification.hasDepartment(officer.getDepartment()))
                .and(ComplaintSpecification.hasDistrict(officer.getDistrict()))
                .and(ComplaintSpecification.hasBlock(officer.getBlock()))
                .and(ComplaintSpecification.hasSeverity(severity))
                .and(ComplaintSpecification.hasStatus(status))
                .and(ComplaintSpecification.hasDate(from, to));

        Page<Complaint> complaints = complaintRepository.findAll(specification, page);

        return complaints.map(complaintService::mapToComplaintViewDTO);
    }

    /** Assign complaint to worker */
    public ComplaintViewDTO assignComplaint(UserPrincipal principal, AssignComplaintDTO dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        Staff worker = staffService.getStaff(dto.getWorkerId());
        Complaint complaint = complaintService.getComplaintByComplaintId(dto.getComplaintId());

        validateAssignment(complaint, officer, worker);

        complaint.setAssignedBy(officer);
        complaint.setAssignedTo(worker);
        complaint.setAssignedAt(Instant.now());
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedAt(Instant.now());

        complaintRepository.save(complaint);

        // notification to the user 
        smsService.sendSms(
            complaint.getRaisedBy().getMobileNumber(),
            "Dear Citizen, your complaint (" + complaint.getComplaintId() +
            ") has been assigned to our field worker " + worker.getUser().getUsername() +
            ". We appreciate your contribution toward a cleaner city."
        );

        notificationService.notifyUser(complaint.getComplaintId(), complaint.getRaisedBy().getId(), NotificationType.ASSIGNED_COMPLAINT);

        // notification to the field worker: pass the user table id (Staff.user.id)
        notificationService.notifyFieldWorker(complaint.getComplaintId(), worker.getUser().getId(), NotificationType.ASSIGNED_COMPLAINT);

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Approve resolved complaint */
    public ComplaintViewDTO approveComplaint(UserPrincipal principal, String complaintId) {
        Staff officer = staffService.getStaff(principal.getUser());
        Complaint complaint = complaintService.getComplaintByComplaintId(complaintId);

        validateApproval(complaint, officer);

        complaint.setRejected(false);
        complaint.setStatus(IssueStatus.CLOSED);
        complaint.setActionedBy(officer);

        complaintRepository.save(complaint);

        //notification to the user 
        smsService.sendSms(
            complaint.getRaisedBy().getMobileNumber(),
            "Your complaint (" + complaint.getComplaintId() +
            ") has been approved and officially closed. Thank you for helping improve our community!"
        );

        // notify the original user who raised the complaint (pass Users.id)
        notificationService.notifyUser(complaintId, complaint.getRaisedBy().getId(), NotificationType.APPROVED_COMPLAINT);

        // notify the field worker by their user id (Staff.user.id)
        notificationService.notifyFieldWorker(complaintId, complaint.getAssignedTo().getUser().getId(), NotificationType.APPROVED_COMPLAINT);

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Reject resolved complaint */
    public ComplaintViewDTO rejectComplaint(UserPrincipal principal, RejectComplaintDto dto) {
        Staff officer = staffService.getStaff(principal.getUser());
        Complaint complaint = complaintService.getComplaintByComplaintId(dto.complaintId());

        validateRejection(complaint, officer);

        complaint.setRejected(true);
        complaint.setStatus(IssueStatus.ASSIGNED);
        complaint.setActionedBy(officer);
        complaint.setRejectionNote(dto.rejectionNote());

        complaintRepository.save(complaint);

        notificationService.notifyFieldWorker(complaint.getComplaintId(), complaint.getAssignedTo().getUser().getId(), NotificationType.REJECTED_COMPLAINT);
        
        return complaintService.mapToComplaintViewDTO(complaint);
    }

    /** Complaint statistics for department */
    public DepartmentComplaintStatisticsDTO getStatistics(UserPrincipal principal, Date from, Date to) {
        Staff officer = staffService.getStaff(principal.getUser());

        Specification<Complaint> spec = Specification.unrestricted();
        spec
                .and(ComplaintSpecification.hasDepartment(officer.getDepartment()))
                .and(ComplaintSpecification.hasDistrict(officer.getDistrict()))
                .and(ComplaintSpecification.hasBlock(officer.getBlock()))
                .and(ComplaintSpecification.hasDate(from, to));

        long total = complaintRepository.count(spec);
        long open = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.OPEN)));
        long assigned = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.ASSIGNED)));
        long resolved = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.RESOLVED)));
        long rejected = complaintRepository.count(spec.and(ComplaintSpecification.hasRejected()));
        long closed = complaintRepository.count(spec.and(ComplaintSpecification.hasStatus(IssueStatus.CLOSED)));
        double avgRating = complaintFeedbackService.findDepartmentAvgRating(officer.getDepartment().getId());
        return DepartmentComplaintStatisticsDTO.builder()
                .totalComplaints(total)
                .openCount(open)
                .assignedCount(assigned)
                .resolvedCount(resolved)
                .rejectedCount(rejected)
                .closedCount(closed)
                .avgRating(avgRating)
                .build();
    }

     public ComplaintViewDTO getComplaintByComplaintIdDetail(UserPrincipal principal, String complaintId) {
        Staff officer = staffRepository.findByUser(principal.getUser())
                .orElseThrow(() -> new ResourceNotFoundException("Officer not found"));

        Complaint complaint = complaintRepository.findByComplaintId(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found"));

        if (!complaint.getDepartment().getId().equals(officer.getDepartment().getId())) {
            throw new IllegalArgumentException("Complaint does not belong to your department");
        }

        return complaintService.mapToComplaintViewDTO(complaint);
    }

    // ---------------- Validation Methods ----------------
    private void validateAssignment(Complaint complaint, Staff officer, Staff worker) {
        if (!complaint.getDepartment().equals(officer.getDepartment())) {
            throw new InvalidAssignmentException("Complaint does not belong to your department");
        }

        if (!worker.getDepartment().equals(officer.getDepartment())) {
            throw new InvalidAssignmentException("Worker does not belong to your department");
        }
        
        if (!complaint.getStatus().equals(IssueStatus.OPEN)) {
            throw new InvalidStatusTransitionException("Cannot assign complaint with status " + complaint.getStatus());
        }
    }

    private void validateApproval(Complaint complaint, Staff officer) {
        if (!complaint.getAssignedBy().equals(officer)) {
            throw new UnauthorizedActionException("Complaint not assigned by this officer");
        }
        if (!complaint.getStatus().equals(IssueStatus.RESOLVED)) {
            throw new InvalidStatusTransitionException("Cannot approve complaint not resolved yet");
        }
    }

    private void validateRejection(Complaint complaint, Staff officer) {
        if (!complaint.getAssignedBy().equals(officer)) {
            throw new UnauthorizedActionException("Complaint not assigned by this officer");
        }
        if (!complaint.getStatus().equals(IssueStatus.RESOLVED)) {
            throw new InvalidStatusTransitionException("Cannot reject complaint not resolved yet");
        }
    }

    /** Get comprehensive analytics for department portal */
    public com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO getDepartmentAnalytics(UserPrincipal principal, Date from, Date to) {
        Staff officer = staffService.getStaff(principal.getUser());

        // Build base specification for department
        Specification<Complaint> baseSpec = Specification.unrestricted();
        baseSpec = baseSpec
                .and(ComplaintSpecification.hasDepartment(officer.getDepartment()))
                .and(ComplaintSpecification.hasDistrict(officer.getDistrict()))
                .and(ComplaintSpecification.hasBlock(officer.getBlock()))
                .and(ComplaintSpecification.hasDate(from, to));

        // Fetch all complaints for this department
        java.util.List<Complaint> allComplaints = complaintRepository.findAll(baseSpec);

        // If no complaints found, return empty analytics
        if (allComplaints.isEmpty()) {
            return buildEmptyAnalytics(officer);
        }

        // Build analytics with data
        return buildAnalyticsWithData(allComplaints, officer);
    }

    private com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO buildEmptyAnalytics(Staff officer) {
        return com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.builder()
                .departmentId(officer.getDepartment().getId())
                .departmentName(officer.getDepartment().getName())
                .overallMetrics(com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.OverallMetrics.builder()
                        .totalComplaints(0)
                        .activeComplaints(0)
                        .resolvedComplaints(0)
                        .closedComplaints(0)
                        .resolutionRate(0.0)
                        .avgRating(0.0)
                        .build())
                .statusDistribution(new java.util.HashMap<>())
                .severityDistribution(new java.util.HashMap<>())
                .categoryDistribution(new java.util.HashMap<>())
                .performanceMetrics(com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.PerformanceMetrics.builder()
                        .avgAssignmentTimeHours(0.0)
                        .avgResolutionTimeHours(0.0)
                        .avgApprovalTimeHours(0.0)
                        .totalRejected(0)
                        .rejectionRate(0.0)
                        .build())
                .dailyTrends(new java.util.ArrayList<>())
                .weeklyTrends(new java.util.ArrayList<>())
                .monthlyTrends(new java.util.ArrayList<>())
                .workerPerformanceList(new java.util.ArrayList<>())
                .resolutionRateByCategory(new java.util.HashMap<>())
                .avgResolutionTimeBySeverity(new java.util.HashMap<>())
                .build();
    }

    private com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO buildAnalyticsWithData(java.util.List<Complaint> allComplaints, Staff officer) {
        long total = allComplaints.size();
        long active = allComplaints.stream().filter(c -> 
            c.getStatus() == IssueStatus.OPEN || c.getStatus() == IssueStatus.ASSIGNED
        ).count();
        long resolved = allComplaints.stream().filter(c -> c.getStatus() == IssueStatus.RESOLVED).count();
        long closed = allComplaints.stream().filter(c -> c.getStatus() == IssueStatus.CLOSED).count();
        
        double resolutionRate = total > 0 ? (double) (resolved + closed) / total * 100 : 0;
        
        Double avgRatingObj = complaintFeedbackService.findDepartmentAvgRating(officer.getDepartment().getId());
        double avgRating = avgRatingObj != null ? avgRatingObj : 0.0;

        com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.OverallMetrics overallMetrics = 
            com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.OverallMetrics.builder()
                .totalComplaints(total)
                .activeComplaints(active)
                .resolvedComplaints(resolved)
                .closedComplaints(closed)
                .resolutionRate(resolutionRate)
                .avgRating(avgRating)
                .build();

        // Status distribution
        java.util.Map<String, Long> statusDistribution = new java.util.LinkedHashMap<>();
        for (IssueStatus status : IssueStatus.values()) {
            long count = allComplaints.stream().filter(c -> c.getStatus() == status).count();
            statusDistribution.put(status.name(), count);
        }

        // Severity distribution
        java.util.Map<String, Long> severityDistribution = new java.util.LinkedHashMap<>();
        for (com.visioners.civic.complaint.model.IssueSeverity severity : com.visioners.civic.complaint.model.IssueSeverity.values()) {
            long count = allComplaints.stream().filter(c -> c.getSeverity() == severity).count();
            severityDistribution.put(severity.name(), count);
        }

        // Category distribution - include all categories with counts
        java.util.Map<String, Long> categoryDistribution = new java.util.LinkedHashMap<>();
        for (com.visioners.civic.complaint.model.Category category : com.visioners.civic.complaint.model.Category.values()) {
            long count = allComplaints.stream().filter(c -> c.getCategory() == category).count();
            categoryDistribution.put(category.name(), count);
        }

        // Daily trends
        java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> dailyTrends = 
            calculateDailyTrends(allComplaints);

        // Weekly trends
        java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> weeklyTrends = 
            calculateWeeklyTrends(allComplaints);

        // Monthly trends
        java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> monthlyTrends = 
            calculateMonthlyTrends(allComplaints);

        // Performance metrics
        com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.PerformanceMetrics performanceMetrics = 
            calculatePerformanceMetrics(allComplaints);

        // Worker performance
        java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.WorkerPerformance> workerPerformanceList = 
            calculateWorkerPerformance(allComplaints);

        // Resolution rate by category
        java.util.Map<String, Double> resolutionRateByCategory = calculateResolutionRateByCategory(allComplaints);

        // Avg resolution time by severity
        java.util.Map<String, Double> avgResolutionTimeBySeverity = calculateAvgResolutionTimeBySeverity(allComplaints);

        return com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.builder()
                .departmentId(officer.getDepartment().getId())
                .departmentName(officer.getDepartment().getName())
                .overallMetrics(overallMetrics)
                .statusDistribution(statusDistribution)
                .severityDistribution(severityDistribution)
                .categoryDistribution(categoryDistribution)
                .performanceMetrics(performanceMetrics)
                .dailyTrends(dailyTrends)
                .weeklyTrends(weeklyTrends)
                .monthlyTrends(monthlyTrends)
                .workerPerformanceList(workerPerformanceList)
                .resolutionRateByCategory(resolutionRateByCategory)
                .avgResolutionTimeBySeverity(avgResolutionTimeBySeverity)
                .build();
    }

    private java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> calculateDailyTrends(java.util.List<Complaint> complaints) {
        java.util.Map<String, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> trendsMap = new java.util.TreeMap<>();
        
        for (Complaint c : complaints) {
            String date = c.getCreatedAt().toString().substring(0, 10);
            
            trendsMap.putIfAbsent(date, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint.builder()
                .label(date).received(0).assigned(0).resolved(0).closed(0).build());
            
            com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint trend = trendsMap.get(date);
            trend.setReceived(trend.getReceived() + 1);
            
            if (c.getAssignedAt() != null && c.getAssignedAt().toString().startsWith(date)) {
                trend.setAssigned(trend.getAssigned() + 1);
            }
            if (c.getResolvedAt() != null && c.getResolvedAt().toString().startsWith(date)) {
                trend.setResolved(trend.getResolved() + 1);
            }
            if (c.getStatus() == IssueStatus.CLOSED && c.getActionedAt() != null && c.getActionedAt().toString().startsWith(date)) {
                trend.setClosed(trend.getClosed() + 1);
            }
        }
        
        return new java.util.ArrayList<>(trendsMap.values());
    }

    private java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> calculateWeeklyTrends(java.util.List<Complaint> complaints) {
        java.util.Map<String, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> trendsMap = new java.util.TreeMap<>();
        
        for (Complaint c : complaints) {
            java.time.LocalDate date = c.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            String week = date.getYear() + "-W" + String.format("%02d", date.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            
            trendsMap.putIfAbsent(week, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint.builder()
                .label(week).received(0).assigned(0).resolved(0).closed(0).build());
            
            com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint trend = trendsMap.get(week);
            trend.setReceived(trend.getReceived() + 1);
            
            if (c.getAssignedAt() != null) {
                trend.setAssigned(trend.getAssigned() + 1);
            }
            if (c.getResolvedAt() != null) {
                trend.setResolved(trend.getResolved() + 1);
            }
            if (c.getStatus() == IssueStatus.CLOSED) {
                trend.setClosed(trend.getClosed() + 1);
            }
        }
        
        return new java.util.ArrayList<>(trendsMap.values());
    }

    private java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> calculateMonthlyTrends(java.util.List<Complaint> complaints) {
        java.util.Map<String, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint> trendsMap = new java.util.TreeMap<>();
        
        for (Complaint c : complaints) {
            String month = c.getCreatedAt().toString().substring(0, 7);
            
            trendsMap.putIfAbsent(month, com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint.builder()
                .label(month).received(0).assigned(0).resolved(0).closed(0).build());
            
            com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.TrendDataPoint trend = trendsMap.get(month);
            trend.setReceived(trend.getReceived() + 1);
            
            if (c.getAssignedAt() != null) {
                trend.setAssigned(trend.getAssigned() + 1);
            }
            if (c.getResolvedAt() != null) {
                trend.setResolved(trend.getResolved() + 1);
            }
            if (c.getStatus() == IssueStatus.CLOSED) {
                trend.setClosed(trend.getClosed() + 1);
            }
        }
        
        return new java.util.ArrayList<>(trendsMap.values());
    }

    private com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.PerformanceMetrics calculatePerformanceMetrics(java.util.List<Complaint> complaints) {
        java.util.List<Complaint> assignedComplaints = complaints.stream()
            .filter(c -> c.getAssignedAt() != null)
            .collect(java.util.stream.Collectors.toList());
        
        double avgAssignmentTime = assignedComplaints.stream()
            .mapToDouble(c -> java.time.Duration.between(c.getCreatedAt(), c.getAssignedAt()).toHours())
            .average().orElse(0.0);

        java.util.List<Complaint> resolvedComplaints = complaints.stream()
            .filter(c -> c.getResolvedAt() != null && c.getAssignedAt() != null)
            .collect(java.util.stream.Collectors.toList());
        
        double avgResolutionTime = resolvedComplaints.stream()
            .mapToDouble(c -> java.time.Duration.between(c.getAssignedAt(), c.getResolvedAt()).toHours())
            .average().orElse(0.0);

        java.util.List<Complaint> closedComplaints = complaints.stream()
            .filter(c -> c.getStatus() == IssueStatus.CLOSED && c.getResolvedAt() != null && c.getActionedAt() != null)
            .collect(java.util.stream.Collectors.toList());
        
        double avgApprovalTime = closedComplaints.stream()
            .mapToDouble(c -> java.time.Duration.between(c.getResolvedAt(), c.getActionedAt()).toHours())
            .average().orElse(0.0);

        long totalRejected = complaints.stream().filter(Complaint::isRejected).count();
        double rejectionRate = complaints.size() > 0 ? (double) totalRejected / complaints.size() * 100 : 0;

        return com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.PerformanceMetrics.builder()
            .avgAssignmentTimeHours(avgAssignmentTime)
            .avgResolutionTimeHours(avgResolutionTime)
            .avgApprovalTimeHours(avgApprovalTime)
            .totalRejected(totalRejected)
            .rejectionRate(rejectionRate)
            .build();
    }

    private java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.WorkerPerformance> calculateWorkerPerformance(java.util.List<Complaint> complaints) {
        java.util.Map<Long, java.util.List<Complaint>> complaintsByWorker = complaints.stream()
            .filter(c -> c.getAssignedTo() != null)
            .collect(java.util.stream.Collectors.groupingBy(c -> c.getAssignedTo().getId()));

        java.util.List<com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.WorkerPerformance> workerPerformanceList = new java.util.ArrayList<>();

        for (java.util.Map.Entry<Long, java.util.List<Complaint>> entry : complaintsByWorker.entrySet()) {
            Long workerId = entry.getKey();
            java.util.List<Complaint> workerComplaints = entry.getValue();
            
            String workerName = workerComplaints.get(0).getAssignedTo().getUser().getUsername();
            long assignedCount = workerComplaints.size();
            long resolvedCount = workerComplaints.stream()
                .filter(c -> c.getStatus() == IssueStatus.RESOLVED || c.getStatus() == IssueStatus.CLOSED)
                .count();
            long pendingCount = assignedCount - resolvedCount;
            double resolutionRate = assignedCount > 0 ? (double) resolvedCount / assignedCount * 100 : 0;
            
            double avgResolutionTime = workerComplaints.stream()
                .filter(c -> c.getResolvedAt() != null && c.getAssignedAt() != null)
                .mapToDouble(c -> java.time.Duration.between(c.getAssignedAt(), c.getResolvedAt()).toHours())
                .average().orElse(0.0);

            workerPerformanceList.add(
                com.visioners.civic.complaint.dto.departmentcomplaintdtos.DepartmentAnalyticsDTO.WorkerPerformance.builder()
                    .workerId(workerId)
                    .workerName(workerName)
                    .assignedCount(assignedCount)
                    .resolvedCount(resolvedCount)
                    .pendingCount(pendingCount)
                    .resolutionRate(resolutionRate)
                    .avgResolutionTimeHours(avgResolutionTime)
                    .build()
            );
        }

        return workerPerformanceList;
    }

    private java.util.Map<String, Double> calculateResolutionRateByCategory(java.util.List<Complaint> complaints) {
        java.util.Map<String, Double> resolutionRates = new java.util.LinkedHashMap<>();
        
        for (com.visioners.civic.complaint.model.Category category : com.visioners.civic.complaint.model.Category.values()) {
            java.util.List<Complaint> categoryComplaints = complaints.stream()
                .filter(c -> c.getCategory() == category)
                .collect(java.util.stream.Collectors.toList());
            
            if (categoryComplaints.isEmpty()) {
                resolutionRates.put(category.name(), 0.0);
            } else {
                long resolved = categoryComplaints.stream()
                    .filter(c -> c.getStatus() == IssueStatus.RESOLVED || c.getStatus() == IssueStatus.CLOSED)
                    .count();
                double rate = (double) resolved / categoryComplaints.size() * 100;
                resolutionRates.put(category.name(), rate);
            }
        }
        
        return resolutionRates;
    }

    private java.util.Map<String, Double> calculateAvgResolutionTimeBySeverity(java.util.List<Complaint> complaints) {
        java.util.Map<String, Double> avgTimes = new java.util.LinkedHashMap<>();
        
        for (com.visioners.civic.complaint.model.IssueSeverity severity : com.visioners.civic.complaint.model.IssueSeverity.values()) {
            double avgTime = complaints.stream()
                .filter(c -> c.getSeverity() == severity)
                .filter(c -> c.getResolvedAt() != null && c.getAssignedAt() != null)
                .mapToDouble(c -> java.time.Duration.between(c.getAssignedAt(), c.getResolvedAt()).toHours())
                .average().orElse(0.0);
            
            avgTimes.put(severity.name(), avgTime);
        }
        
        return avgTimes;
    }
}
