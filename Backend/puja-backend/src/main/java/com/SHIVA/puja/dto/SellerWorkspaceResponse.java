package com.SHIVA.puja.dto;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerWorkspaceResponse {

    private List<NavigationItem> navigation;
    private List<MetricCard> metrics;
    private List<AlertItem> lowStockAlerts;
    private TableData recentOrders;
    private TableData topProducts;
    private TableData productTable;
    private TableData inventoryTable;
    private TableData orderTable;
    private TableData customerTable;
    private TableData reviewTable;
    private TableData promotionsTable;
    private TableData paymentTable;
    private List<AlertItem> shippingAlerts;
    private TableData supportTable;
    private List<ChartPoint> revenueTrend;
    private List<ChartPoint> categoryPerformance;
    private List<ChartPoint> salesPerDay;
    private List<ChartPoint> customerGrowth;
    private List<NotificationItem> notifications;
    private ShippingSettings shippingSettings;
    private ShopSettingsSnapshot shopSettings;
    private List<MessageItem> messages;

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
    public static class ShippingSettings {
        private List<String> partners;
        private List<String> deliveryRegions;
        private String shippingCharge;
        private String freeShippingThreshold;
        private String estimatedDelivery;
    }

    @Data
    @Builder
    public static class ShopSettingsSnapshot {
        private String shopName;
        private String logo;
        private String banner;
        private String address;
        private String returnPolicy;
        private String gstNumber;
        private String bankAccount;
    }

    @Data
    @Builder
    public static class MessageItem {
        private String customer;
        private String subject;
        private String excerpt;
        private Boolean unread;
    }
}
