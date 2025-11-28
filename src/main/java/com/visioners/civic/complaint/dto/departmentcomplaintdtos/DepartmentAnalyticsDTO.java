package com.visioners.civic.complaint.dto.departmentcomplaintdtos;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentAnalyticsDTO {
    
    // Department information
    private Long departmentId;
    private String departmentName;
    
    // Overall metrics
    private OverallMetrics overallMetrics;
    
    // Time-based trends (for line charts)
    private List<TrendDataPoint> dailyTrends;
    private List<TrendDataPoint> weeklyTrends;
    private List<TrendDataPoint> monthlyTrends;
    
    // Status distribution (for pie/donut charts)
    private Map<String, Long> statusDistribution;
    
    // Severity distribution (for pie/donut charts)
    private Map<String, Long> severityDistribution;
    
    // Category distribution (for bar charts)
    private Map<String, Long> categoryDistribution;
    
    // Performance metrics
    private PerformanceMetrics performanceMetrics;
    
    // Worker performance (for bar/table charts)
    private List<WorkerPerformance> workerPerformanceList;
    
    // Resolution rate by category (for bar charts)
    private Map<String, Double> resolutionRateByCategory;
    
    // Average resolution time by severity (for bar charts)
    private Map<String, Double> avgResolutionTimeBySeverity;
    
    @Data
    @Builder
    public static class OverallMetrics {
        private long totalComplaints;
        private long activeComplaints;
        private long resolvedComplaints;
        private long closedComplaints;
        private double resolutionRate;
        private double avgRating;
    }
    
    @Data
    @Builder
    public static class TrendDataPoint {
        private String label; // date/week/month label
        private long received;
        private long assigned;
        private long resolved;
        private long closed;
    }
    
    @Data
    @Builder
    public static class PerformanceMetrics {
        private double avgAssignmentTimeHours;
        private double avgResolutionTimeHours;
        private double avgApprovalTimeHours;
        private long totalRejected;
        private double rejectionRate;
    }
    
    @Data
    @Builder
    public static class WorkerPerformance {
        private Long workerId;
        private String workerName;
        private long assignedCount;
        private long resolvedCount;
        private long pendingCount;
        private double resolutionRate;
        private double avgResolutionTimeHours;
    }
}
