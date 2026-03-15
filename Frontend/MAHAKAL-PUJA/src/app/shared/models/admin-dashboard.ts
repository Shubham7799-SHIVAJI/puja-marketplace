import type {
  AlertItem,
  ChartPoint,
  NotificationItem,
  SellerTableData,
} from './seller-dashboard';

export type AdminSectionKey =
  | 'dashboard'
  | 'users'
  | 'sellers'
  | 'products'
  | 'orders'
  | 'finance'
  | 'logistics'
  | 'reviews'
  | 'promotions'
  | 'analytics'
  | 'categories'
  | 'notifications'
  | 'content'
  | 'support'
  | 'system';

export interface AdminNavItem {
  key: AdminSectionKey;
  label: string;
  icon: string;
  badge?: string;
}

export interface AdminMetric {
  label: string;
  value: string;
  delta: string;
  tone: 'revenue' | 'orders' | 'warning' | 'success';
}

export interface SystemSnapshot {
  apiStatus: string;
  uptime: string;
  heapUsage: string;
  activeUsers: string;
  databaseLoad: string;
  securityMode: string;
}

export interface RoleSummary {
  superAdmin: string;
  financeAdmin: string;
  productAdmin: string;
  sellerAdmin: string;
  supportAdmin: string;
  twoFactorMode: string;
}

export interface AdminWorkspaceData {
  navigation: AdminNavItem[];
  metrics: AdminMetric[];
  approvalAlerts: AlertItem[];
  financeAlerts: AlertItem[];
  systemAlerts: AlertItem[];
  userTable: SellerTableData;
  sellerTable: SellerTableData;
  productTable: SellerTableData;
  orderTable: SellerTableData;
  paymentTable: SellerTableData;
  logisticsTable: SellerTableData;
  reviewTable: SellerTableData;
  promotionTable: SellerTableData;
  supportTable: SellerTableData;
  categoryTable: SellerTableData;
  contentTable: SellerTableData;
  activityTable: SellerTableData;
  salesTrend: ChartPoint[];
  userGrowth: ChartPoint[];
  categoryPerformance: ChartPoint[];
  revenueTrend: ChartPoint[];
  notifications: NotificationItem[];
  systemSnapshot: SystemSnapshot;
  roleSummary: RoleSummary;
}
