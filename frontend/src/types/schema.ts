export type UserRole = 'ADMIN' | 'WORKER' | 'CLIENT' | 'CONTRACTOR';

export interface UserProfile {
  uid: string;
  email: string;
  phoneNumber?: string;
  displayName: string;
  role: UserRole;
  roles?: UserRole[];
  photoURL?: string;
  status: 'ACTIVE' | 'SUSPENDED';
  createdAt: string;
  updatedAt: string;
  lastLogin?: string;
}

export type TradeType = 
  | 'Raj Mistri (Mason)' 
  | 'Carpenter' 
  | 'Electrician' 
  | 'Plumber' 
  | 'Painter' 
  | 'Tile Worker' 
  | 'Welder' 
  | 'General Labour' 
  | 'Site Supervisor';

export interface WorkerRecord {
  id: string;
  userId?: string;
  fullName: string;
  phone: string;
  trade: TradeType;
  dailyWage: number;
  status: 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE';
  assignedProjectId?: string;
  assignedProjectTitle?: string;
  assignedSiteId?: string;
  assignedSiteName?: string;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  aadhaarNumber?: string;
  panNumber?: string;
  emergencyContact?: string;
  avatarUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface GPSCoordinates {
  latitude: number;
  longitude: number;
  accuracy?: number;
}

export interface AttendanceRecord {
  id: string;
  workerId: string;
  workerName: string;
  workerTrade: string;
  projectId: string;
  projectTitle: string;
  siteId: string;
  siteName: string;
  date: string; // YYYY-MM-DD
  checkInTime: string;
  checkOutTime?: string;
  status: 'PRESENT' | 'HALF_DAY' | 'OVERTIME' | 'ABSENT';
  gpsCoordinates: GPSCoordinates;
  isWithinGeofence: boolean;
  distanceFromSiteMeters: number;
  selfieUrl?: string;
  approvalStatus: 'PENDING' | 'APPROVED' | 'REJECTED';
  approvedBy?: string;
  approvedAt?: string;
  remarks?: string;
  hoursWorked?: number;
  createdAt: string;
}

export interface SiteRecord {
  id: string;
  projectId: string;
  projectTitle: string;
  name: string;
  address: string;
  city: string;
  state: string;
  latitude: number;
  longitude: number;
  geofenceRadiusMeters: number; // e.g. 150m
  managerName: string;
  contactPhone: string;
  activeWorkersCount?: number;
  createdAt: string;
}

export interface ProjectRecord {
  id: string;
  clientId: string;
  clientName: string;
  title: string;
  description: string;
  location: string;
  siteId?: string;
  startDate: string;
  estimatedEndDate: string;
  status: 'PLANNING' | 'IN_PROGRESS' | 'ON_HOLD' | 'COMPLETED';
  budget: number;
  totalExpenses: number;
  progressPercentage: number;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectMilestone {
  id: string;
  projectId: string;
  phaseName: string;
  description: string;
  targetDate: string;
  completionDate?: string;
  status: 'TODO' | 'IN_PROGRESS' | 'COMPLETED';
  orderIndex: number;
}

export interface ClientRecord {
  id: string;
  userId?: string;
  companyName: string;
  contactPerson: string;
  email: string;
  phone: string;
  billingAddress: string;
  gstNumber?: string;
  panNumber?: string;
  totalProjectsCount: number;
  createdAt: string;
}

export type PaymentType = 'CLIENT_INVOICE' | 'WORKER_WAGE' | 'MATERIAL_PURCHASE' | 'MISC_EXPENSE';
export type PaymentStatus = 'PAID' | 'PENDING' | 'OVERDUE' | 'CANCELLED';
export type PaymentMethod = 'UPI' | 'NEFT' | 'RTGS' | 'CASH' | 'CHEQUE';

export interface PaymentRecord {
  id: string;
  invoiceNumber: string;
  projectId: string;
  projectTitle: string;
  clientId?: string;
  clientName?: string;
  workerId?: string;
  workerName?: string;
  paymentType: PaymentType;
  amount: number;
  taxAmount?: number;
  totalAmount: number;
  status: PaymentStatus;
  paymentMethod?: PaymentMethod;
  transactionRef?: string;
  dueDate: string;
  paidAt?: string;
  description: string;
  generatedPdfUrl?: string;
  createdAt: string;
}

export interface WorkerSalaryRecord {
  id: string;
  workerId: string;
  workerName: string;
  month: string; // YYYY-MM
  daysPresent: number;
  halfDays: number;
  overtimeHours: number;
  dailyRate: number;
  baseSalary: number;
  overtimePay: number;
  bonus: number;
  deductions: number;
  netPayable: number;
  status: 'PENDING' | 'PAID';
  paidDate?: string;
  paymentRef?: string;
  paymentMethod?: PaymentMethod;
  createdAt: string;
}

export type MaterialCategory = 
  | 'CEMENT' 
  | 'STEEL' 
  | 'BRICKS' 
  | 'SAND' 
  | 'AGGREGATE' 
  | 'ELECTRICAL' 
  | 'PLUMBING' 
  | 'PAINT' 
  | 'OTHER';

export interface MaterialRecord {
  id: string;
  projectId: string;
  projectTitle: string;
  siteId: string;
  itemName: string;
  category: MaterialCategory;
  quantity: number;
  unit: 'BAGS' | 'TONS' | 'PIECES' | 'TRUCKS' | 'METERS' | 'LITRES';
  minThreshold: number;
  unitCost: number;
  supplierName: string;
  supplierContact?: string;
  status: 'IN_STOCK' | 'LOW_STOCK' | 'OUT_OF_STOCK';
  lastRestockedAt: string;
  createdAt: string;
}

export interface ExpenseRecord {
  id: string;
  projectId: string;
  projectTitle: string;
  siteId?: string;
  category: 'LABOR' | 'MATERIALS' | 'EQUIPMENT' | 'TRANSPORT' | 'PERMITS' | 'UTILITIES' | 'MISCELLANEOUS';
  amount: number;
  description: string;
  billReceiptUrl?: string;
  paidTo: string;
  paymentMode: PaymentMethod;
  date: string;
  approvedBy?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  createdAt: string;
}

export interface DailyProgressReport {
  id: string;
  projectId: string;
  projectTitle: string;
  siteId: string;
  siteName: string;
  reportDate: string;
  reportedBy: string;
  weatherCondition: 'Sunny' | 'Cloudy' | 'Rainy' | 'Stormy';
  skilledLaborCount: number;
  unskilledLaborCount: number;
  tasksCompleted: string;
  delaysOrIssues?: string;
  safetyObservations?: string;
  photos: string[];
  status: 'SUBMITTED' | 'APPROVED';
  createdAt: string;
}

export interface NotificationItem {
  id: string;
  userId: string;
  title: string;
  body: string;
  type: 'ATTENDANCE' | 'PAYMENT' | 'PROJECT' | 'MATERIAL' | 'ALERT';
  read: boolean;
  linkUrl?: string;
  createdAt: string;
}

export interface AuditLogRecord {
  id: string;
  userId: string;
  userName: string;
  role: UserRole;
  action: string;
  targetType: 'ATTENDANCE' | 'WORKER' | 'PAYMENT' | 'PROJECT' | 'MATERIAL' | 'EXPENSE' | 'AUTH';
  targetId: string;
  details: string;
  ipAddress?: string;
  userAgent?: string;
  timestamp: string;
}
