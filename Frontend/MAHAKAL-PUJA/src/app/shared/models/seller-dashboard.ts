export type SellerSectionKey =
  | 'dashboard'
  | 'products'
  | 'add-product'
  | 'inventory'
  | 'orders'
  | 'customers'
  | 'reviews'
  | 'promotions'
  | 'analytics'
  | 'payments'
  | 'shipping'
  | 'messages'
  | 'settings'
  | 'support';

export interface SellerNavItem {
  key: SellerSectionKey;
  label: string;
  icon: string;
  badge?: string;
}

export interface SellerMetric {
  label: string;
  value: string;
  delta: string;
  tone: 'revenue' | 'orders' | 'warning' | 'success';
}

export interface ChartPoint {
  label: string;
  value: number;
}

export interface NotificationItem {
  id: string;
  title: string;
  detail: string;
  time: string;
  tone: 'info' | 'success' | 'warning' | 'critical';
}

export interface SellerTableColumn {
  key: string;
  label: string;
  type?: 'text' | 'currency' | 'badge' | 'date' | 'imageText' | 'list';
}

export interface ImageTextCell {
  image: string;
  title: string;
  subtitle?: string;
}

export interface BadgeCell {
  label: string;
  tone?: 'neutral' | 'success' | 'warning' | 'danger' | 'info';
}

export type SellerTableCell = string | number | string[] | ImageTextCell | BadgeCell | null;

export type SellerTableRow = Record<string, SellerTableCell>;

export interface SellerTableData {
  title: string;
  subtitle: string;
  columns: SellerTableColumn[];
  rows: SellerTableRow[];
}

export interface AlertItem {
  label: string;
  value: string;
  detail: string;
  tone: 'warning' | 'danger' | 'info';
}

export interface ShippingSettings {
  partners: string[];
  deliveryRegions: string[];
  shippingCharge: string;
  freeShippingThreshold: string;
  estimatedDelivery: string;
}

export interface ShopSettingsSnapshot {
  shopName: string;
  logo: string;
  banner: string;
  address: string;
  returnPolicy: string;
  gstNumber: string;
  bankAccount: string;
}

export interface SellerMessage {
  customer: string;
  subject: string;
  excerpt: string;
  unread: boolean;
}

export interface SellerWorkspaceData {
  navigation: SellerNavItem[];
  metrics: SellerMetric[];
  lowStockAlerts: AlertItem[];
  recentOrders: SellerTableData;
  topProducts: SellerTableData;
  productTable: SellerTableData;
  inventoryTable: SellerTableData;
  orderTable: SellerTableData;
  customerTable: SellerTableData;
  reviewTable: SellerTableData;
  promotionsTable: SellerTableData;
  paymentTable: SellerTableData;
  shippingAlerts: AlertItem[];
  supportTable: SellerTableData;
  revenueTrend: ChartPoint[];
  categoryPerformance: ChartPoint[];
  salesPerDay: ChartPoint[];
  customerGrowth: ChartPoint[];
  notifications: NotificationItem[];
  shippingSettings: ShippingSettings;
  shopSettings: ShopSettingsSnapshot;
  messages: SellerMessage[];
}