import { 
  collection, 
  doc, 
  getDocs, 
  getDoc, 
  setDoc, 
  addDoc, 
  updateDoc, 
  deleteDoc, 
  query, 
  where, 
  orderBy, 
  limit, 
  onSnapshot,
  Timestamp 
} from 'firebase/firestore';
import { ref, uploadBytes, getDownloadURL } from 'firebase/storage';
import { db, storage, auth } from '../config/firebase';
import {
  UserProfile,
  WorkerRecord,
  AttendanceRecord,
  SiteRecord,
  ProjectRecord,
  ProjectMilestone,
  ClientRecord,
  PaymentRecord,
  WorkerSalaryRecord,
  MaterialRecord,
  ExpenseRecord,
  DailyProgressReport,
  NotificationItem,
  AuditLogRecord
} from '../types/schema';
import { sanitizeObject } from '../utils/sanitize';

// Collection references
const USERS_COL = 'users';
const WORKERS_COL = 'workers';
const ATTENDANCE_COL = 'attendance';
const SITES_COL = 'sites';
const PROJECTS_COL = 'projects';
const MILESTONES_COL = 'milestones';
const CLIENTS_COL = 'clients';
const PAYMENTS_COL = 'payments';
const SALARIES_COL = 'salaries';
const MATERIALS_COL = 'materials';
const EXPENSES_COL = 'expenses';
const DPR_COL = 'daily_progress_reports';
const NOTIFICATIONS_COL = 'notifications';
const AUDIT_LOGS_COL = 'audit_logs';

/**
 * ----------------------------------------------------
 * AUDIT LOGGING SERVICE
 * ----------------------------------------------------
 */
export const logActivity = async (
  action: string,
  targetType: AuditLogRecord['targetType'],
  targetId: string,
  details: string
): Promise<void> => {
  try {
    const user = auth.currentUser;
    const logEntry: Omit<AuditLogRecord, 'id'> = {
      userId: user?.uid || 'SYSTEM',
      userName: user?.displayName || user?.email || 'System User',
      role: 'ADMIN', // default fallback
      action,
      targetType,
      targetId,
      details,
      userAgent: typeof navigator !== 'undefined' ? navigator.userAgent : 'Unknown',
      timestamp: new Date().toISOString()
    };
    await addDoc(collection(db, AUDIT_LOGS_COL), logEntry);
  } catch (err) {
    console.warn('Failed to record audit log:', err);
  }
};

export const getAuditLogs = async (max = 50): Promise<AuditLogRecord[]> => {
  try {
    const q = query(collection(db, AUDIT_LOGS_COL), orderBy('timestamp', 'desc'), limit(max));
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as AuditLogRecord));
  } catch (err) {
    console.error('Error fetching audit logs:', err);
    return [];
  }
};

/**
 * ----------------------------------------------------
 * USERS SERVICE
 * ----------------------------------------------------
 */
export const getUserProfile = async (uid: string): Promise<UserProfile | null> => {
  try {
    const docRef = doc(db, USERS_COL, uid);
    const snap = await getDoc(docRef);
    if (snap.exists()) {
      return snap.data() as UserProfile;
    }
    return null;
  } catch (err) {
    console.error('Error fetching user profile:', err);
    return null;
  }
};

export const saveUserProfile = async (profile: UserProfile): Promise<void> => {
  const clean = sanitizeObject(profile);
  await setDoc(doc(db, USERS_COL, profile.uid), clean, { merge: true });
};

/**
 * ----------------------------------------------------
 * WORKERS CRUD
 * ----------------------------------------------------
 */
export const getWorkers = async (): Promise<WorkerRecord[]> => {
  try {
    const snap = await getDocs(collection(db, WORKERS_COL));
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as WorkerRecord));
  } catch (err) {
    console.error('Error fetching workers:', err);
    return [];
  }
};

export const createWorker = async (worker: Omit<WorkerRecord, 'id' | 'createdAt' | 'updatedAt'>): Promise<string> => {
  const clean = sanitizeObject(worker);
  const now = new Date().toISOString();
  const docRef = await addDoc(collection(db, WORKERS_COL), {
    ...clean,
    createdAt: now,
    updatedAt: now,
  });
  await logActivity('WORKER_CREATED', 'WORKER', docRef.id, `Created worker ${worker.fullName} (${worker.trade})`);
  return docRef.id;
};

export const updateWorker = async (id: string, updates: Partial<WorkerRecord>): Promise<void> => {
  const clean = sanitizeObject(updates);
  const ref = doc(db, WORKERS_COL, id);
  await updateDoc(ref, {
    ...clean,
    updatedAt: new Date().toISOString(),
  });
  await logActivity('WORKER_UPDATED', 'WORKER', id, `Updated worker ${updates.fullName || id}`);
};

export const deleteWorker = async (id: string, workerName: string): Promise<void> => {
  await deleteDoc(doc(db, WORKERS_COL, id));
  await logActivity('WORKER_DELETED', 'WORKER', id, `Deleted worker ${workerName}`);
};

/**
 * ----------------------------------------------------
 * SITES CRUD
 * ----------------------------------------------------
 */
export const getSites = async (): Promise<SiteRecord[]> => {
  try {
    const snap = await getDocs(collection(db, SITES_COL));
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as SiteRecord));
  } catch (err) {
    console.error('Error fetching sites:', err);
    return [];
  }
};

export const createSite = async (site: Omit<SiteRecord, 'id' | 'createdAt'>): Promise<string> => {
  const clean = sanitizeObject(site);
  const docRef = await addDoc(collection(db, SITES_COL), {
    ...clean,
    createdAt: new Date().toISOString()
  });
  await logActivity('SITE_CREATED', 'PROJECT', docRef.id, `Created site ${site.name} for project ${site.projectTitle}`);
  return docRef.id;
};

export const updateSite = async (id: string, updates: Partial<SiteRecord>): Promise<void> => {
  const clean = sanitizeObject(updates);
  await updateDoc(doc(db, SITES_COL, id), clean);
};

export const deleteSite = async (id: string, name: string): Promise<void> => {
  await deleteDoc(doc(db, SITES_COL, id));
  await logActivity('SITE_DELETED', 'PROJECT', id, `Deleted site ${name}`);
};

/**
 * ----------------------------------------------------
 * PROJECTS CRUD & TIMELINE
 * ----------------------------------------------------
 */
export const getProjects = async (): Promise<ProjectRecord[]> => {
  try {
    const snap = await getDocs(collection(db, PROJECTS_COL));
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as ProjectRecord));
  } catch (err) {
    console.error('Error fetching projects:', err);
    return [];
  }
};

export const getProjectById = async (id: string): Promise<ProjectRecord | null> => {
  try {
    const snap = await getDoc(doc(db, PROJECTS_COL, id));
    if (snap.exists()) {
      return { id: snap.id, ...snap.data() } as ProjectRecord;
    }
    return null;
  } catch (err) {
    console.error('Error fetching project:', err);
    return null;
  }
};

export const createProject = async (project: Omit<ProjectRecord, 'id' | 'createdAt' | 'updatedAt' | 'totalExpenses'>): Promise<string> => {
  const clean = sanitizeObject(project);
  const now = new Date().toISOString();
  const docRef = await addDoc(collection(db, PROJECTS_COL), {
    ...clean,
    totalExpenses: 0,
    createdAt: now,
    updatedAt: now,
  });

  // Automatically create default project milestones
  const defaultPhases = [
    { name: '1. Site Excavation & Clearing', desc: 'Site layout, ground levelling, deep trenching' },
    { name: '2. Foundation & Substructure', desc: 'PCC, Footing reinforcement, plinth beam casting' },
    { name: '3. RCC Superstructure', desc: 'Columns, beams, and slab formwork & casting' },
    { name: '4. Brick Masonry & Plastering', desc: 'Exterior and partition brickwork with internal plaster' },
    { name: '5. MEP & Electrical Conduit', desc: 'Plumbing lines, sanitary pipes, concealed conduits' },
    { name: '6. Finishes, Tile & Painting', desc: 'Vitrified tiles, primer and 2-coat emulsion painting' },
    { name: '7. Final Inspection & Handover', desc: 'Snag list clearance and formal handover to client' }
  ];

  for (let i = 0; i < defaultPhases.length; i++) {
    await addDoc(collection(db, MILESTONES_COL), {
      projectId: docRef.id,
      phaseName: defaultPhases[i].name,
      description: defaultPhases[i].desc,
      targetDate: project.estimatedEndDate,
      status: i === 0 ? 'IN_PROGRESS' : 'TODO',
      orderIndex: i + 1
    });
  }

  await logActivity('PROJECT_CREATED', 'PROJECT', docRef.id, `Created project "${project.title}" for client ${project.clientName}`);
  return docRef.id;
};

export const updateProject = async (id: string, updates: Partial<ProjectRecord>): Promise<void> => {
  const clean = sanitizeObject(updates);
  await updateDoc(doc(db, PROJECTS_COL, id), {
    ...clean,
    updatedAt: new Date().toISOString()
  });
  await logActivity('PROJECT_UPDATED', 'PROJECT', id, `Updated project status/details for ${id}`);
};

export const deleteProject = async (id: string, title: string): Promise<void> => {
  await deleteDoc(doc(db, PROJECTS_COL, id));
  await logActivity('PROJECT_DELETED', 'PROJECT', id, `Deleted project "${title}"`);
};

export const getMilestones = async (projectId: string): Promise<ProjectMilestone[]> => {
  try {
    const q = query(
      collection(db, MILESTONES_COL),
      where('projectId', '==', projectId),
      orderBy('orderIndex', 'asc')
    );
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as ProjectMilestone));
  } catch (err) {
    console.error('Error fetching milestones:', err);
    return [];
  }
};

export const updateMilestoneStatus = async (id: string, status: ProjectMilestone['status']): Promise<void> => {
  await updateDoc(doc(db, MILESTONES_COL, id), {
    status,
    completionDate: status === 'COMPLETED' ? new Date().toISOString() : null
  });
};

/**
 * ----------------------------------------------------
 * GPS & SELFIE ATTENDANCE
 * ----------------------------------------------------
 */
export const checkInAttendance = async (
  attendance: Omit<AttendanceRecord, 'id' | 'createdAt'>
): Promise<string> => {
  const clean = sanitizeObject(attendance);
  const now = new Date().toISOString();
  const docRef = await addDoc(collection(db, ATTENDANCE_COL), {
    ...clean,
    createdAt: now,
  });

  await logActivity(
    'ATTENDANCE_CHECK_IN',
    'ATTENDANCE',
    docRef.id,
    `${attendance.workerName} checked in at ${attendance.siteName} (Geofence: ${attendance.isWithinGeofence ? 'Pass' : 'Fail'} - ${attendance.distanceFromSiteMeters}m)`
  );

  return docRef.id;
};

export const getAttendanceRecords = async (filterDate?: string): Promise<AttendanceRecord[]> => {
  try {
    let q = query(collection(db, ATTENDANCE_COL), orderBy('createdAt', 'desc'), limit(100));
    if (filterDate) {
      q = query(collection(db, ATTENDANCE_COL), where('date', '==', filterDate), orderBy('createdAt', 'desc'));
    }
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as AttendanceRecord));
  } catch (err) {
    console.error('Error fetching attendance records:', err);
    return [];
  }
};

export const getWorkerAttendance = async (workerId: string): Promise<AttendanceRecord[]> => {
  try {
    const q = query(
      collection(db, ATTENDANCE_COL),
      where('workerId', '==', workerId),
      orderBy('createdAt', 'desc'),
      limit(50)
    );
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as AttendanceRecord));
  } catch (err) {
    console.error('Error fetching worker attendance:', err);
    return [];
  }
};

export const updateAttendanceApproval = async (
  id: string,
  approvalStatus: AttendanceRecord['approvalStatus'],
  remarks?: string
): Promise<void> => {
  const user = auth.currentUser;
  const updates: Partial<AttendanceRecord> = {
    approvalStatus,
    approvedBy: user?.displayName || user?.email || 'Admin',
    approvedAt: new Date().toISOString(),
    remarks: remarks || '',
  };
  await updateDoc(doc(db, ATTENDANCE_COL, id), updates);
  await logActivity('ATTENDANCE_APPROVAL', 'ATTENDANCE', id, `Attendance marked as ${approvalStatus}`);
};

/**
 * ----------------------------------------------------
 * WORKER SALARY MANAGEMENT
 * ----------------------------------------------------
 */
export const getSalaries = async (month?: string): Promise<WorkerSalaryRecord[]> => {
  try {
    let q = query(collection(db, SALARIES_COL), orderBy('createdAt', 'desc'));
    if (month) {
      q = query(collection(db, SALARIES_COL), where('month', '==', month));
    }
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as WorkerSalaryRecord));
  } catch (err) {
    console.error('Error fetching salaries:', err);
    return [];
  }
};

export const generateMonthlySalary = async (
  worker: WorkerRecord,
  month: string,
  daysPresent: number,
  halfDays: number,
  overtimeHours: number,
  bonus: number,
  deductions: number
): Promise<string> => {
  const dailyRate = worker.dailyWage || 700;
  const baseSalary = (daysPresent * dailyRate) + (halfDays * (dailyRate / 2));
  const hourlyRate = dailyRate / 8;
  const overtimePay = Math.round(overtimeHours * hourlyRate * 1.5);
  const netPayable = Math.max(0, baseSalary + overtimePay + bonus - deductions);

  const salaryData: Omit<WorkerSalaryRecord, 'id'> = {
    workerId: worker.id,
    workerName: worker.fullName,
    month,
    daysPresent,
    halfDays,
    overtimeHours,
    dailyRate,
    baseSalary,
    overtimePay,
    bonus,
    deductions,
    netPayable,
    status: 'PENDING',
    createdAt: new Date().toISOString()
  };

  const docRef = await addDoc(collection(db, SALARIES_COL), salaryData);
  await logActivity('SALARY_CALCULATED', 'PAYMENT', docRef.id, `Calculated salary for ${worker.fullName} (Month: ${month}, Net: ₹${netPayable})`);
  return docRef.id;
};

export const markSalaryPaid = async (
  id: string,
  paymentMethod: WorkerSalaryRecord['paymentMethod'],
  paymentRef: string
): Promise<void> => {
  await updateDoc(doc(db, SALARIES_COL, id), {
    status: 'PAID',
    paidDate: new Date().toISOString(),
    paymentMethod,
    paymentRef,
  });
  await logActivity('SALARY_DISBURSED', 'PAYMENT', id, `Salary disbursed via ${paymentMethod} (Ref: ${paymentRef})`);
};

/**
 * ----------------------------------------------------
 * CLIENTS CRUD
 * ----------------------------------------------------
 */
export const getClients = async (): Promise<ClientRecord[]> => {
  try {
    const snap = await getDocs(collection(db, CLIENTS_COL));
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as ClientRecord));
  } catch (err) {
    console.error('Error fetching clients:', err);
    return [];
  }
};

export const createClient = async (client: Omit<ClientRecord, 'id' | 'createdAt' | 'totalProjectsCount'>): Promise<string> => {
  const clean = sanitizeObject(client);
  const docRef = await addDoc(collection(db, CLIENTS_COL), {
    ...clean,
    totalProjectsCount: 0,
    createdAt: new Date().toISOString(),
  });
  await logActivity('CLIENT_CREATED', 'PROJECT', docRef.id, `Created client ${client.companyName} (${client.contactPerson})`);
  return docRef.id;
};

export const updateClient = async (id: string, updates: Partial<ClientRecord>): Promise<void> => {
  const clean = sanitizeObject(updates);
  await updateDoc(doc(db, CLIENTS_COL, id), clean);
};

/**
 * ----------------------------------------------------
 * PAYMENTS & INVOICES
 * ----------------------------------------------------
 */
export const getPayments = async (): Promise<PaymentRecord[]> => {
  try {
    const snap = await getDocs(query(collection(db, PAYMENTS_COL), orderBy('createdAt', 'desc')));
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as PaymentRecord));
  } catch (err) {
    console.error('Error fetching payments:', err);
    return [];
  }
};

export const createPayment = async (payment: Omit<PaymentRecord, 'id' | 'createdAt'>): Promise<string> => {
  const clean = sanitizeObject(payment);
  const docRef = await addDoc(collection(db, PAYMENTS_COL), {
    ...clean,
    createdAt: new Date().toISOString(),
  });
  await logActivity('PAYMENT_INVOICE_CREATED', 'PAYMENT', docRef.id, `Generated invoice ${payment.invoiceNumber} for ₹${payment.totalAmount}`);
  return docRef.id;
};

export const updatePaymentStatus = async (
  id: string,
  status: PaymentRecord['status'],
  paymentMethod?: PaymentRecord['paymentMethod'],
  transactionRef?: string
): Promise<void> => {
  const updates: Partial<PaymentRecord> = {
    status,
    ...(paymentMethod && { paymentMethod }),
    ...(transactionRef && { transactionRef }),
    ...(status === 'PAID' && { paidAt: new Date().toISOString() })
  };
  await updateDoc(doc(db, PAYMENTS_COL, id), updates);
  await logActivity('PAYMENT_STATUS_UPDATED', 'PAYMENT', id, `Payment updated to ${status} (Ref: ${transactionRef || 'N/A'})`);
};

/**
 * ----------------------------------------------------
 * MATERIAL INVENTORY
 * ----------------------------------------------------
 */
export const getMaterials = async (projectId?: string): Promise<MaterialRecord[]> => {
  try {
    let q = query(collection(db, MATERIALS_COL), orderBy('itemName', 'asc'));
    if (projectId) {
      q = query(collection(db, MATERIALS_COL), where('projectId', '==', projectId));
    }
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as MaterialRecord));
  } catch (err) {
    console.error('Error fetching materials:', err);
    return [];
  }
};

export const createMaterial = async (material: Omit<MaterialRecord, 'id' | 'createdAt' | 'lastRestockedAt' | 'status'>): Promise<string> => {
  const clean = sanitizeObject(material);
  const status: MaterialRecord['status'] = material.quantity <= 0 
    ? 'OUT_OF_STOCK' 
    : material.quantity <= material.minThreshold 
      ? 'LOW_STOCK' 
      : 'IN_STOCK';

  const docRef = await addDoc(collection(db, MATERIALS_COL), {
    ...clean,
    status,
    lastRestockedAt: new Date().toISOString(),
    createdAt: new Date().toISOString(),
  });
  await logActivity('MATERIAL_ADDED', 'MATERIAL', docRef.id, `Added material ${material.itemName} (${material.quantity} ${material.unit})`);
  return docRef.id;
};

export const updateStock = async (id: string, newQuantity: number, itemName: string, unit: string): Promise<void> => {
  const ref = doc(db, MATERIALS_COL, id);
  const snap = await getDoc(ref);
  const minThreshold = snap.exists() ? snap.data()?.minThreshold || 10 : 10;
  
  const status: MaterialRecord['status'] = newQuantity <= 0 
    ? 'OUT_OF_STOCK' 
    : newQuantity <= minThreshold 
      ? 'LOW_STOCK' 
      : 'IN_STOCK';

  await updateDoc(ref, {
    quantity: newQuantity,
    status,
    lastRestockedAt: new Date().toISOString()
  });
  await logActivity('STOCK_ADJUSTED', 'MATERIAL', id, `Updated stock for ${itemName} to ${newQuantity} ${unit}`);
};

export const deleteMaterial = async (id: string, itemName: string): Promise<void> => {
  await deleteDoc(doc(db, MATERIALS_COL, id));
  await logActivity('MATERIAL_DELETED', 'MATERIAL', id, `Deleted material ${itemName}`);
};

/**
 * ----------------------------------------------------
 * EXPENSE TRACKING
 * ----------------------------------------------------
 */
export const getExpenses = async (projectId?: string): Promise<ExpenseRecord[]> => {
  try {
    let q = query(collection(db, EXPENSES_COL), orderBy('date', 'desc'));
    if (projectId) {
      q = query(collection(db, EXPENSES_COL), where('projectId', '==', projectId));
    }
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as ExpenseRecord));
  } catch (err) {
    console.error('Error fetching expenses:', err);
    return [];
  }
};

export const createExpense = async (expense: Omit<ExpenseRecord, 'id' | 'createdAt' | 'status'>): Promise<string> => {
  const clean = sanitizeObject(expense);
  const docRef = await addDoc(collection(db, EXPENSES_COL), {
    ...clean,
    status: 'PENDING',
    createdAt: new Date().toISOString(),
  });
  await logActivity('EXPENSE_RECORDED', 'EXPENSE', docRef.id, `Recorded expense ₹${expense.amount} for ${expense.description} (${expense.category})`);
  return docRef.id;
};

export const updateExpenseStatus = async (id: string, status: ExpenseRecord['status']): Promise<void> => {
  const user = auth.currentUser;
  await updateDoc(doc(db, EXPENSES_COL, id), {
    status,
    approvedBy: user?.displayName || user?.email || 'Admin',
  });
  await logActivity('EXPENSE_STATUS_UPDATED', 'EXPENSE', id, `Expense status set to ${status}`);
};

/**
 * ----------------------------------------------------
 * DAILY PROGRESS REPORTS (DPR)
 * ----------------------------------------------------
 */
export const getDPRs = async (projectId?: string): Promise<DailyProgressReport[]> => {
  try {
    let q = query(collection(db, DPR_COL), orderBy('reportDate', 'desc'));
    if (projectId) {
      q = query(collection(db, DPR_COL), where('projectId', '==', projectId));
    }
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as DailyProgressReport));
  } catch (err) {
    console.error('Error fetching DPRs:', err);
    return [];
  }
};

export const createDPR = async (dpr: Omit<DailyProgressReport, 'id' | 'createdAt' | 'status'>): Promise<string> => {
  const clean = sanitizeObject(dpr);
  const docRef = await addDoc(collection(db, DPR_COL), {
    ...clean,
    status: 'SUBMITTED',
    createdAt: new Date().toISOString(),
  });
  await logActivity('DPR_SUBMITTED', 'PROJECT', docRef.id, `Submitted DPR for ${dpr.projectTitle} on ${dpr.reportDate}`);
  return docRef.id;
};

/**
 * ----------------------------------------------------
 * NOTIFICATIONS
 * ----------------------------------------------------
 */
export const getNotifications = async (userId: string): Promise<NotificationItem[]> => {
  try {
    const q = query(
      collection(db, NOTIFICATIONS_COL),
      where('userId', '==', userId),
      orderBy('createdAt', 'desc'),
      limit(20)
    );
    const snap = await getDocs(q);
    return snap.docs.map(d => ({ id: d.id, ...d.data() } as NotificationItem));
  } catch (err) {
    console.error('Error fetching notifications:', err);
    return [];
  }
};

export const sendNotification = async (notif: Omit<NotificationItem, 'id' | 'createdAt' | 'read'>): Promise<void> => {
  try {
    await addDoc(collection(db, NOTIFICATIONS_COL), {
      ...notif,
      read: false,
      createdAt: new Date().toISOString(),
    });
  } catch (err) {
    console.warn('Failed to dispatch notification:', err);
  }
};

export const markNotificationRead = async (id: string): Promise<void> => {
  await updateDoc(doc(db, NOTIFICATIONS_COL, id), { read: true });
};

/**
 * ----------------------------------------------------
 * STORAGE / PHOTO UPLOAD
 * ----------------------------------------------------
 */
export const uploadFileToStorage = async (file: File, folder = 'uploads'): Promise<string> => {
  try {
    const filename = `${folder}/${Date.now()}_${file.name.replace(/\s+/g, '_')}`;
    const storageRef = ref(storage, filename);
    const snapshot = await uploadBytes(storageRef, file);
    const url = await getDownloadURL(snapshot.ref);
    return url;
  } catch (err: any) {
    console.warn('Storage bucket upload fallback:', err);
    // Offline / demo fallback using Data URL
    return new Promise((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        resolve(reader.result as string);
      };
      reader.readAsDataURL(file);
    });
  }
};

/**
 * ----------------------------------------------------
 * DATABASE BACKUP & RESTORE
 * ----------------------------------------------------
 */
export const exportDatabaseSnapshot = async (): Promise<string> => {
  const [workers, projects, sites, clients, attendance, payments, materials, expenses, dprs, auditLogs] = await Promise.all([
    getWorkers(),
    getProjects(),
    getSites(),
    getClients(),
    getAttendanceRecords(),
    getPayments(),
    getMaterials(),
    getExpenses(),
    getDPRs(),
    getAuditLogs(100)
  ]);

  const backupPayload = {
    exportDate: new Date().toISOString(),
    version: '1.0.0',
    data: {
      workers,
      projects,
      sites,
      clients,
      attendance,
      payments,
      materials,
      expenses,
      dailyProgressReports: dprs,
      auditLogs,
    }
  };

  return JSON.stringify(backupPayload, null, 2);
};

export const importDatabaseSnapshot = async (jsonString: string): Promise<{ success: boolean; importedCount: number }> => {
  try {
    const payload = JSON.parse(jsonString);
    if (!payload.data) {
      throw new Error('Invalid backup file structure: missing "data" key.');
    }

    let count = 0;
    const { workers, projects, sites, clients, materials } = payload.data;

    if (Array.isArray(workers)) {
      for (const w of workers) {
        const { id, ...data } = w;
        await addDoc(collection(db, WORKERS_COL), data);
        count++;
      }
    }
    if (Array.isArray(projects)) {
      for (const p of projects) {
        const { id, ...data } = p;
        await addDoc(collection(db, PROJECTS_COL), data);
        count++;
      }
    }
    if (Array.isArray(sites)) {
      for (const s of sites) {
        const { id, ...data } = s;
        await addDoc(collection(db, SITES_COL), data);
        count++;
      }
    }
    if (Array.isArray(clients)) {
      for (const c of clients) {
        const { id, ...data } = c;
        await addDoc(collection(db, CLIENTS_COL), data);
        count++;
      }
    }
    if (Array.isArray(materials)) {
      for (const m of materials) {
        const { id, ...data } = m;
        await addDoc(collection(db, MATERIALS_COL), data);
        count++;
      }
    }

    await logActivity('DATABASE_RESTORED', 'AUTH', 'SYSTEM', `Restored ${count} records from backup file.`);
    return { success: true, importedCount: count };
  } catch (err: any) {
    console.error('Database restore error:', err);
    throw new Error(`Restore failed: ${err.message}`);
  }
};
