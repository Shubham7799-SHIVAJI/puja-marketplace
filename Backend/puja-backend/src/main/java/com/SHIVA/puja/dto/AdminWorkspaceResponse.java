package com.SHIVA.puja.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminWorkspaceResponse {

    private List<NavigationItem> navigation;
    private List<MetricCard> metrics;
    private List<AlertItem> approvalAlerts;
    private List<AlertItem> financeAlerts;
    private List<AlertItem> systemAlerts;
    private TableData userTable;
    private TableData sellerTable;
    private TableData productTable;
    private TableData orderTable;
    private TableData paymentTable;
    private TableData logisticsTable;
    private TableData reviewTable;
    private TableData promotionTable;
    private TableData supportTable;
    private TableData categoryTable;
    private TableData contentTable;
    private TableData activityTable;
    private List<ChartPoint> salesTrend;
    private List<ChartPoint> userGrowth;
    private List<ChartPoint> categoryPerformance;
    private List<ChartPoint> revenueTrend;
    private List<NotificationItem> notifications;
    private SystemSnapshot systemSnapshot;
    private RoleSummary roleSummary;

    @Data
    @Builder
    public static class NavigationItem {
        private String key;
        private String label;
        private String icon;
        private String badge;
    }

    @Data
    @Builder
    public static class MetricCard {
        private String label;
        private String value;
        private String delta;
        private String tone;
    }

    @Data
    @Builder
    public static class AlertItem {
        private String label;
        private String value;
        private String detail;
        private String tone;
    }

    @Data
    @Builder
    public static class TableData {
        private String title;
        private String subtitle;
        private List<TableColumn> columns;
        private List<Map<String, Object>> rows;
    }

    @Data
    @Builder
    public static class TableColumn {
        private String key;
        private String label;
        private String type;
    }

    @Data
    @Builder
    public static class ChartPoint {
        private String label;
        private Number value;
    }

    @Data
    @Builder
    public static class NotificationItem {
        private String id;
        private String title;
        private String detail;
        private String time;
        private String tone;
    }

    @Data
    @Builder
    public static class SystemSnapshot {
        private String apiStatus;
        private String uptime;
        private String heapUsage;
        private String activeUsers;
        private String databaseLoad;
        private String securityMode;
    }

    @Data
    @Builder
    public static class RoleSummary {
        private String superAdmin;
        private String financeAdmin;
        private String productAdmin;
        private String sellerAdmin;
        private String supportAdmin;
        private String twoFactorMode;
    }
}
