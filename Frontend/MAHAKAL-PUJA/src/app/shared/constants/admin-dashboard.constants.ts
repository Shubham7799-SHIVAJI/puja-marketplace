export const ADMIN_DASHBOARD_TEXT = {
  shell: {
    eyebrow: 'Marketplace command center',
    brandTitle: 'MAHAKAL Commerce OS',
    title: 'Admin control plane for the full e-commerce ecosystem',
    description: 'Operate users, sellers, catalog quality, payouts, logistics, support, and system health from a single enterprise workspace.',
    searchLabel: 'Search',
    searchPlaceholder: 'Search users, sellers, orders, tickets, or products',
    environment: 'Production governance mode',
    refreshLabel: 'Live orchestration',
    signOutLabel: 'Secure session',
    loadingDescription: 'Loading marketplace telemetry, governance queues, and operational intelligence.',
    errorTitle: 'Workspace unavailable',
    returnHomeLabel: 'Return to home',
    heroTitle: 'Enterprise control for marketplace scale',
  },
  sections: {
    dashboard: {
      headline: 'Marketplace overview',
      description: 'Real-time platform metrics, approvals, finance alerts, and system signals for daily command review.',
    },
    users: {
      headline: 'User management',
      description: 'Profile governance, verification review, and customer history oversight.',
    },
    sellers: {
      headline: 'Seller management',
      description: 'Onboarding approvals, KYC controls, commission tuning, and seller performance visibility.',
    },
    products: {
      headline: 'Product governance',
      description: 'Catalog moderation, restricted-item detection, and bulk merchandising operations.',
    },
    orders: {
      headline: 'Order operations',
      description: 'Payment tracking, cancellations, refunds, returns, and shipping exceptions.',
    },
    finance: {
      headline: 'Finance and payouts',
      description: 'Platform revenue, seller settlements, commission health, and transaction oversight.',
    },
    logistics: {
      headline: 'Shipping and logistics',
      description: 'Delivery zones, courier partners, shipping fees, and SLA orchestration.',
    },
    reviews: {
      headline: 'Reviews and trust',
      description: 'Spam prevention, abuse moderation, and reputation controls across the marketplace.',
    },
    promotions: {
      headline: 'Promotions and campaigns',
      description: 'Marketplace-wide coupons, flash sales, and seasonal merchandising control.',
    },
    analytics: {
      headline: 'Analytics and reporting',
      description: 'Sales, growth, category mix, and revenue intelligence for marketplace leadership.',
    },
    categories: {
      headline: 'Category management',
      description: 'Organize category structures and track catalog distribution across sellers.',
    },
    notifications: {
      headline: 'Notification center',
      description: 'Broadcast announcements and review operational alerts sent to sellers and users.',
    },
    content: {
      headline: 'Content management',
      description: 'Homepage banners, featured products, editorial content, FAQs, and policy surfaces.',
    },
    support: {
      headline: 'Support and disputes',
      description: 'Complaint queues, ticket ownership, and service escalation management.',
    },
    system: {
      headline: 'System monitoring',
      description: 'Server health, API posture, audit activity, and security operating mode.',
    },
  },
  cards: {
    approvals: 'Approvals and compliance',
    approvalsTitle: 'Seller onboarding and compliance',
    finance: 'Finance watchlist',
    financeTitle: 'Revenue and payout watchlist',
    system: 'System watchlist',
    systemTitle: 'Platform resilience',
    roles: 'Role-based access model',
    rolesTitle: 'Multi-admin operating model',
    snapshot: 'System snapshot',
    broadcast: 'Broadcast operations',
    broadcastTitle: 'Notification delivery strategy',
    broadcastDescription: 'Use this panel to coordinate email, SMS, push, and in-app announcements around promotions, policy updates, order incidents, and seller communications.',
  },
  charts: {
    salesTrend: {
      title: 'Sales Trend',
      subtitle: 'Gross marketplace demand across the recent months',
      analyticsSubtitle: 'Marketplace transaction velocity',
    },
    userGrowth: {
      title: 'User Growth',
      subtitle: 'New account momentum across marketplace cohorts',
      analyticsSubtitle: 'Account acquisition across cohorts',
    },
    categoryPerformance: {
      title: 'Category Performance',
      subtitle: 'Catalog depth and category concentration',
      analyticsSubtitle: 'Top category concentration',
    },
    revenueTrend: {
      title: 'Revenue Trend',
      subtitle: 'Platform commission and monetization flow',
      analyticsSubtitle: 'Commission and platform yield',
    },
  },
  systemSnapshot: {
    apiStatus: 'API status',
    uptime: 'Uptime',
    heapUsage: 'Heap usage',
    activeUsers: 'Active users',
    databaseLoad: 'Database load',
    securityMode: 'Security mode',
  },
  roles: {
    superAdmin: 'Super admin',
    financeAdmin: 'Finance admin',
    productAdmin: 'Product admin',
    sellerAdmin: 'Seller admin',
    supportAdmin: 'Support admin',
    twoFactor: 'Two-factor',
  },
  emptyError: 'Unable to assemble the admin workspace right now.',
} as const;
