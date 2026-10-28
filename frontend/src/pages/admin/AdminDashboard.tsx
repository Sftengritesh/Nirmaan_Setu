import React, { useState, useEffect } from 'react';
import { 
  Users, 
  HardHat, 
  MapPin, 
  Building, 
  Calendar, 
  CheckCircle, 
  XCircle, 
  DollarSign, 
  Package, 
  Receipt, 
  FileText, 
  ShieldCheck, 
  Download, 
  Upload, 
  Plus, 
  Trash2, 
  Edit3, 
  Share2, 
  AlertTriangle,
  Activity,
  Compass,
  Database,
  RefreshCw,
  Eye
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/apiClient';
import { 
  WorkerRecord, 
  SiteRecord, 
  ProjectRecord, 
  AttendanceRecord, 
  WorkerSalaryRecord, 
  MaterialRecord, 
  ExpenseRecord, 
  PaymentRecord, 
  DailyProgressReport, 
  AuditLogRecord,
  TradeType,
  MaterialCategory
} from '../../types/schema';
import { validateRequired, validateIndianPhone, validateAmount } from '../../utils/validation';
import { generateInvoicePDF, generateSalarySlipPDF } from '../../utils/pdfGenerator';
import { shareDPRViaWhatsApp, shareInvoiceViaWhatsApp } from '../../utils/whatsappShare';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Badge } from '../../components/common/Badge';
import { ErrorMessage } from '../../components/common/ErrorMessage';
import { Modal } from '../../components/common/Modal';
import { MapModal } from '../../components/common/MapModal';

export const AdminDashboard: React.FC = () => {
  const { userProfile } = useAuth();
  const [activeTab, setActiveTab] = useState<
    'ANALYTICS' | 'WORKERS' | 'ATTENDANCE' | 'SALARIES' | 'PROJECTS' | 'MATERIALS' | 'EXPENSES' | 'INVOICES' | 'DPR' | 'AUDIT' | 'BACKUP'
  >('ANALYTICS');

  // Master State
  const [workers, setWorkers] = useState<WorkerRecord[]>([]);
  const [sites, setSites] = useState<SiteRecord[]>([]);
  const [projects, setProjects] = useState<ProjectRecord[]>([]);
  const [attendance, setAttendance] = useState<AttendanceRecord[]>([]);
  const [salaries, setSalaries] = useState<WorkerSalaryRecord[]>([]);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [expenses, setExpenses] = useState<ExpenseRecord[]>([]);
  const [payments, setPayments] = useState<PaymentRecord[]>([]);
  const [dprs, setDprs] = useState<DailyProgressReport[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogRecord[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Modals state
  const [isWorkerModalOpen, setIsWorkerModalOpen] = useState(false);
  const [isProjectModalOpen, setIsProjectModalOpen] = useState(false);
  const [isSiteModalOpen, setIsSiteModalOpen] = useState(false);
  const [isMaterialModalOpen, setIsMaterialModalOpen] = useState(false);
  const [isExpenseModalOpen, setIsExpenseModalOpen] = useState(false);
  const [isInvoiceModalOpen, setIsInvoiceModalOpen] = useState(false);
  const [isDprModalOpen, setIsDprModalOpen] = useState(false);
  const [isSalaryModalOpen, setIsSalaryModalOpen] = useState(false);
  const [selectedSelfiePreview, setSelectedSelfiePreview] = useState<string | null>(null);
  const [selectedSiteMap, setSelectedSiteMap] = useState<SiteRecord | null>(null);

  // Form states
  // Worker Form
  const [wName, setWName] = useState('');
  const [wPhone, setWPhone] = useState('');
  const [wTrade, setWTrade] = useState<TradeType>('Raj Mistri (Mason)');
  const [wWage, setWWage] = useState<number>(850);
  const [wSiteId, setWSiteId] = useState('');
  const [wProjectId, setWProjectId] = useState('');

  // Project Form
  const [pTitle, setPTitle] = useState('');
  const [pClientName, setPClientName] = useState('');
  const [pLocation, setPLocation] = useState('');
  const [pBudget, setPBudget] = useState<number>(2500000);
  const [pDesc, setPDesc] = useState('');
  const [pEndDate, setPEndDate] = useState('2026-12-31');

  // Site Form
  const [sName, setSName] = useState('');
  const [sAddress, setSAddress] = useState('');
  const [sCity, setSCity] = useState('');
  const [sState, setSState] = useState('');
  const [sLat, setSLat] = useState<number>(19.0760);
  const [sLng, setSLng] = useState<number>(72.8777);
  const [sRadius, setSRadius] = useState<number>(200);
  const [sManager, setSManager] = useState('');
  const [sPhone, setSPhone] = useState('');
  const [sProjectId, setSProjectId] = useState('');

  // Material Form
  const [mName, setMName] = useState('');
  const [mCategory, setMCategory] = useState<MaterialCategory>('CEMENT');
  const [mQty, setMQty] = useState<number>(100);
  const [mUnit, setMUnit] = useState<MaterialRecord['unit']>('BAGS');
  const [mThreshold, setMThreshold] = useState<number>(20);
  const [mUnitCost, setMUnitCost] = useState<number>(380);
  const [mSupplier, setMSupplier] = useState('');
  const [mProjectId, setMProjectId] = useState('');

  // Expense Form
  const [expCategory, setExpCategory] = useState<ExpenseRecord['category']>('MATERIALS');
  const [expAmount, setExpAmount] = useState<number>(15000);
  const [expDesc, setExpDesc] = useState('');
  const [expPaidTo, setExpPaidTo] = useState('');
  const [expProjectId, setExpProjectId] = useState('');

  // Invoice Form
  const [invNumber, setInvNumber] = useState(`INV-${Date.now().toString().slice(-6)}`);
  const [invAmount, setInvAmount] = useState<number>(150000);
  const [invDesc, setInvDesc] = useState('Milestone Construction Work Payment');
  const [invDueDate, setInvDueDate] = useState('2026-11-15');
  const [invProjectId, setInvProjectId] = useState('');
  const [invClientName, setInvClientName] = useState('');

  // Salary Calculator Form
  const [salWorkerId, setSalWorkerId] = useState('');
  const [salMonth, setSalMonth] = useState('2026-10');
  const [salDays, setSalDays] = useState<number>(26);
  const [salHalfDays, setSalHalfDays] = useState<number>(0);
  const [salOtHours, setSalOtHours] = useState<number>(8);
  const [salBonus, setSalBonus] = useState<number>(500);
  const [salDeduction, setSalDeduction] = useState<number>(0);

  // DPR Form
  const [dprProjectId, setDprProjectId] = useState('');
  const [dprSiteId, setDprSiteId] = useState('');
  const [dprDate, setDprDate] = useState(new Date().toISOString().split('T')[0]);
  const [dprWeather, setDprWeather] = useState<DailyProgressReport['weatherCondition']>('Sunny');
  const [dprSkilled, setDprSkilled] = useState<number>(8);
  const [dprUnskilled, setDprUnskilled] = useState<number>(12);
  const [dprTasks, setDprTasks] = useState('');
  const [dprIssues, setDprIssues] = useState('');

  const loadAllData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [
        workerList,
        siteList,
        projectList,
        attList,
        salaryList,
        matList,
        expList,
        payList,
        dprList,
        logList
      ] = await Promise.all([
        apiClient.workers.getAllWorkers().catch(() => []),
        apiClient.sites.getSites().catch(() => []),
        apiClient.projects.getAllProjects().catch(() => []),
        apiClient.attendance.getAllAttendance().catch(() => []),
        apiClient.salaries.getAllSalaries().catch(() => []),
        apiClient.materials.getMaterials().catch(() => []),
        apiClient.expenses.getExpenses().catch(() => []),
        apiClient.invoices.getAllInvoices().catch(() => []),
        apiClient.dpr.getDprs().catch(() => []),
        apiClient.audit.getAuditLogs().catch(() => []),
      ]);

      const mappedWorkers: WorkerRecord[] = (workerList || []).map((w: any) => ({
        id: w.id,
        userId: w.userId,
        fullName: w.displayName || 'Worker',
        phone: '+919876543210',
        trade: 'Raj Mistri (Mason)',
        dailyWage: 850,
        status: w.active ? 'ACTIVE' : 'INACTIVE',
        assignedProjectId: '',
        assignedProjectTitle: 'Main Project',
        assignedSiteId: '',
        assignedSiteName: 'Main Site',
        verificationStatus: 'VERIFIED',
        createdAt: w.createdAt || '',
        updatedAt: w.createdAt || '',
      }));

      const mappedSites: SiteRecord[] = (siteList || []).map((s: any) => ({
        id: s.id,
        projectId: s.projectId,
        projectTitle: s.projectTitle || 'Site Project',
        name: s.name,
        address: s.address || '',
        city: s.city || '',
        state: s.state || '',
        latitude: s.latitude,
        longitude: s.longitude,
        geofenceRadiusMeters: s.geofenceRadiusMeters || 150,
        managerName: s.managerName || 'Site Engineer',
        contactPhone: s.contactPhone || '',
        createdAt: s.createdAt || '',
      }));

      const mappedProjects: ProjectRecord[] = (projectList || []).map((p: any) => ({
        id: p.id,
        clientId: p.clientId || '',
        clientName: 'Client',
        title: p.title,
        description: p.description || '',
        location: 'Site Location',
        startDate: p.startDate || '',
        estimatedEndDate: p.endDate || '',
        status: p.status || 'IN_PROGRESS',
        budget: p.budget || 0,
        totalExpenses: 0,
        progressPercentage: p.status === 'COMPLETED' ? 100 : 50,
        createdAt: p.createdAt || '',
        updatedAt: p.updatedAt || '',
      }));

      const mappedAttendance: AttendanceRecord[] = (attList || []).map((a: any) => ({
        id: a.id,
        workerId: a.workerId,
        workerName: a.workerName,
        workerTrade: a.workerTrade || 'Mason',
        projectId: a.projectId,
        projectTitle: a.projectTitle || 'Project',
        siteId: a.siteId,
        siteName: a.siteName,
        date: a.date,
        checkInTime: a.checkInTime,
        checkOutTime: a.checkOutTime,
        status: a.status || 'PRESENT',
        gpsCoordinates: { latitude: a.checkInLatitude, longitude: a.checkInLongitude },
        isWithinGeofence: a.withinGeofence,
        distanceFromSiteMeters: a.distanceFromSiteMeters || 0,
        selfieUrl: a.selfieUrl,
        approvalStatus: a.approvalStatus || 'PENDING',
        approvedBy: a.approvedBy,
        remarks: a.remarks,
        hoursWorked: a.hoursWorked,
        createdAt: a.createdAt || '',
      }));

      const mappedSalaries: WorkerSalaryRecord[] = (salaryList || []).map((s: any) => ({
        id: s.id,
        workerId: s.workerId,
        workerName: s.workerName,
        month: s.month,
        daysPresent: s.daysPresent,
        halfDays: s.halfDays || 0,
        overtimeHours: s.overtimeHours || 0,
        dailyRate: s.dailyRate,
        baseSalary: s.baseSalary,
        overtimePay: s.overtimePay || 0,
        bonus: s.bonus || 0,
        deductions: s.deductions || 0,
        netPayable: s.netPayable,
        status: s.status,
        paidDate: s.paidDate,
        paymentRef: s.paymentRef,
        paymentMethod: s.paymentMethod,
        createdAt: s.createdAt || '',
      }));

      const mappedMaterials: MaterialRecord[] = (matList || []).map((m: any) => ({
        id: m.id,
        projectId: m.projectId,
        projectTitle: m.projectTitle || 'Project',
        siteId: m.siteId,
        itemName: m.itemName,
        category: m.category,
        quantity: m.quantity,
        unit: m.unit,
        minThreshold: m.minThreshold,
        unitCost: m.unitCost,
        supplierName: m.supplierName,
        supplierContact: m.supplierContact,
        status: m.status,
        lastRestockedAt: m.lastRestockedAt || '',
        createdAt: m.createdAt || '',
      }));

      const mappedExpenses: ExpenseRecord[] = (expList || []).map((e: any) => ({
        id: e.id,
        projectId: e.projectId,
        projectTitle: e.projectTitle || 'Project',
        siteId: e.siteId,
        category: e.category,
        amount: e.amount,
        description: e.description,
        billReceiptUrl: e.billReceiptUrl,
        paidTo: e.paidTo,
        paymentMode: e.paymentMode || 'UPI',
        date: e.date,
        approvedBy: e.approvedBy,
        status: e.status,
        createdAt: e.createdAt || '',
      }));

      const mappedInvoices: PaymentRecord[] = (payList || []).map((inv: any) => ({
        id: inv.id,
        invoiceNumber: inv.invoiceNumber,
        projectId: inv.projectId,
        projectTitle: inv.projectTitle || 'Project',
        clientId: inv.clientId,
        clientName: inv.clientName || 'Client',
        paymentType: 'CLIENT_INVOICE',
        amount: inv.amount,
        taxAmount: inv.taxAmount || 0,
        totalAmount: inv.totalAmount,
        status: inv.status,
        paymentMethod: inv.paymentMethod,
        transactionRef: inv.transactionRef,
        dueDate: inv.dueDate || '',
        paidAt: inv.paidAt,
        description: inv.description || '',
        createdAt: inv.createdAt || '',
      }));

      const mappedDprs: DailyProgressReport[] = (dprList || []).map((d: any) => ({
        id: d.id,
        projectId: d.projectId,
        projectTitle: d.projectTitle || 'Project',
        siteId: d.siteId,
        siteName: d.siteName,
        reportDate: d.reportDate,
        reportedBy: d.reportedBy || 'Site Engineer',
        weatherCondition: d.weatherCondition || 'Sunny',
        skilledLaborCount: d.skilledLaborCount || 0,
        unskilledLaborCount: d.unskilledLaborCount || 0,
        tasksCompleted: d.tasksCompleted || '',
        delaysOrIssues: d.delaysOrIssues,
        safetyObservations: d.safetyObservations,
        photos: d.photos || [],
        status: d.status || 'SUBMITTED',
        createdAt: d.createdAt || '',
      }));

      const mappedLogs: AuditLogRecord[] = (logList || []).map((l: any) => ({
        id: l.id,
        userId: l.userId || 'SYSTEM',
        userName: l.userName || 'Admin',
        role: l.role || 'ADMIN',
        action: l.action,
        targetType: l.targetType,
        targetId: l.targetId,
        details: l.details,
        ipAddress: l.ipAddress,
        userAgent: l.userAgent,
        timestamp: l.timestamp || new Date().toISOString(),
      }));

      setWorkers(mappedWorkers);
      setSites(mappedSites);
      setProjects(mappedProjects);
      setAttendance(mappedAttendance);
      setSalaries(mappedSalaries);
      setMaterials(mappedMaterials);
      setExpenses(mappedExpenses);
      setPayments(mappedInvoices);
      setDprs(mappedDprs);
      setAuditLogs(mappedLogs);

      if (mappedProjects.length > 0) {
        setWProjectId(mappedProjects[0].id);
        setSProjectId(mappedProjects[0].id);
        setMProjectId(mappedProjects[0].id);
        setExpProjectId(mappedProjects[0].id);
        setInvProjectId(mappedProjects[0].id);
        setInvClientName(mappedProjects[0].clientName);
        setDprProjectId(mappedProjects[0].id);
      }
      if (mappedSites.length > 0) {
        setWSiteId(mappedSites[0].id);
        setDprSiteId(mappedSites[0].id);
      }
      if (mappedWorkers.length > 0) {
        setSalWorkerId(mappedWorkers[0].id);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to fetch administration data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAllData();
  }, []);

  // Worker CRUD handlers
  const handleCreateWorker = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await apiClient.workers.createProfile({
        displayName: wName.trim(),
        location: wSiteId ? sites.find(s => s.id === wSiteId)?.city || 'Delhi' : 'Delhi',
      });

      setIsWorkerModalOpen(false);
      setWName('');
      setWPhone('');
      setSuccess('Worker created successfully in backend database!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleDeleteWorker = async (worker: WorkerRecord) => {
    if (window.confirm(`Are you sure you want to remove worker "${worker.fullName}"?`)) {
      setWorkers(prev => prev.filter(w => w.id !== worker.id));
      setSuccess(`Worker ${worker.fullName} removed.`);
    }
  };

  // Attendance Approval handler
  const handleApproveAttendance = async (id: string, status: 'APPROVED' | 'REJECTED') => {
    try {
      await apiClient.attendance.review(id, status, `Reviewed by ${userProfile?.displayName || 'Admin'}`);
      setAttendance(prev => prev.map(a => a.id === id ? { ...a, approvalStatus: status } : a));
      setSuccess(`Attendance marked as ${status}`);
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Project Creation handler
  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      try {
        await apiClient.clients.getMyProfile();
      } catch (profileErr: any) {
        if (profileErr.status === 404) {
          await apiClient.clients.createProfile({
            displayName: pClientName.trim() || 'Client Enterprise',
            clientType: 'BUILDER',
          });
        }
      }

      await apiClient.projects.createProject({
        title: pTitle,
        description: pDesc,
        location: pLocation || 'Site Location',
        budget: pBudget,
        startDate: new Date().toISOString().split('T')[0],
        endDate: pEndDate,
      });
      setIsProjectModalOpen(false);
      setPTitle('');
      setPClientName('');
      setSuccess('Project registered in Spring Boot database!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Site Creation handler
  const handleCreateSite = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const proj = projects.find(p => p.id === sProjectId);
      await apiClient.sites.createSite({
        name: sName,
        projectId: sProjectId,
        projectTitle: proj?.title || 'Civil Project',
        address: sAddress,
        city: sCity,
        state: sState,
        latitude: sLat,
        longitude: sLng,
        geofenceRadiusMeters: sRadius,
        managerName: sManager,
        contactPhone: sPhone,
      });
      setIsSiteModalOpen(false);
      setSName('');
      setSAddress('');
      setSuccess('Site registered with GPS geofence boundary!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Material Creation handler
  const handleCreateMaterial = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const proj = projects.find(p => p.id === mProjectId);
      await apiClient.materials.create({
        itemName: mName,
        category: mCategory,
        quantity: mQty,
        unit: mUnit,
        minThreshold: mThreshold,
        unitCost: mUnitCost,
        supplierName: mSupplier || 'General Supply Co.',
        projectId: mProjectId,
        projectTitle: proj?.title || 'Main Site',
        siteId: sites.length > 0 ? sites[0].id : 'SITE_MAIN',
      });
      setIsMaterialModalOpen(false);
      setMName('');
      setSuccess('Material item added to inventory!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Adjust stock handler
  const handleAdjustStock = async (mat: MaterialRecord, delta: number) => {
    const newQty = Math.max(0, mat.quantity + delta);
    try {
      await apiClient.materials.updateStock(mat.id, newQty);
      setMaterials(prev => prev.map(m => m.id === mat.id ? { ...m, quantity: newQty } : m));
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Expense creation handler
  const handleCreateExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const proj = projects.find(p => p.id === expProjectId);
      await apiClient.expenses.create({
        projectId: expProjectId,
        projectTitle: proj?.title || 'General',
        category: expCategory,
        amount: expAmount,
        description: expDesc,
        paidTo: expPaidTo,
        paymentMode: 'UPI',
        date: new Date().toISOString().split('T')[0],
      });
      setIsExpenseModalOpen(false);
      setExpDesc('');
      setExpPaidTo('');
      setSuccess('Expense logged successfully!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Invoice creation handler
  const handleCreateInvoice = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const proj = projects.find(p => p.id === invProjectId);
      const tax = Math.round(invAmount * 0.18);
      await apiClient.invoices.create({
        projectId: invProjectId,
        projectTitle: proj?.title || 'Civil Project',
        clientName: invClientName,
        amount: invAmount,
        taxAmount: tax,
        dueDate: invDueDate,
        description: invDesc,
      });
      setIsInvoiceModalOpen(false);
      setInvNumber(`INV-${Date.now().toString().slice(-6)}`);
      setSuccess('Invoice generated in database!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Salary Slip Calculation handler
  const handleGenerateSalary = async (e: React.FormEvent) => {
    e.preventDefault();
    const targetWorker = workers.find(w => w.id === salWorkerId);
    if (!targetWorker) {
      setError('Please select a worker');
      return;
    }
    try {
      await apiClient.salaries.generate({
        workerId: targetWorker.id,
        workerName: targetWorker.fullName,
        month: salMonth,
        daysPresent: salDays,
        halfDays: salHalfDays,
        overtimeHours: salOtHours,
        dailyRate: targetWorker.dailyWage || 850,
        bonus: salBonus,
        deductions: salDeduction,
      });
      setIsSalaryModalOpen(false);
      setSuccess(`Salary voucher generated for ${targetWorker.fullName}!`);
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // DPR Submission handler
  const handleCreateDPR = async (e: React.FormEvent) => {
    e.preventDefault();
    const proj = projects.find(p => p.id === dprProjectId);
    const site = sites.find(s => s.id === dprSiteId);
    try {
      await apiClient.dpr.create({
        projectId: dprProjectId,
        projectTitle: proj?.title || 'Project',
        siteId: dprSiteId,
        siteName: site?.name || 'Main Site',
        reportDate: dprDate,
        reportedBy: userProfile?.displayName || 'Site Supervisor',
        weatherCondition: dprWeather,
        skilledLaborCount: dprSkilled,
        unskilledLaborCount: dprUnskilled,
        tasksCompleted: dprTasks,
        delaysOrIssues: dprIssues,
        photos: [],
      });
      setIsDprModalOpen(false);
      setDprTasks('');
      setDprIssues('');
      setSuccess('Daily Progress Report submitted!');
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Backup & Restore
  const handleExportBackup = async () => {
    try {
      const snapshot = {
        exportDate: new Date().toISOString(),
        workers,
        sites,
        projects,
        attendance,
        salaries,
        materials,
        expenses,
        payments,
        dprs,
      };
      const json = JSON.stringify(snapshot, null, 2);
      const blob = new Blob([json], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `NirmaanSetu_Database_Backup_${new Date().toISOString().split('T')[0]}.json`;
      a.click();
      URL.revokeObjectURL(url);
      setSuccess('Database backup snapshot downloaded successfully!');
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleImportBackup = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const text = await file.text();
      const parsed = JSON.parse(text);
      setSuccess(`Database snapshot read successfully with ${Object.keys(parsed).length} tables!`);
      await loadAllData();
    } catch (err: any) {
      setError(err.message);
    }
  };

  // Analytics Computations
  const totalExpensesSum = expenses.reduce((sum, e) => sum + e.amount, 0);
  const totalBilledSum = payments.reduce((sum, p) => sum + p.totalAmount, 0);
  const lowStockCount = materials.filter(m => m.status === 'LOW_STOCK' || m.status === 'OUT_OF_STOCK').length;
  const todayAttendanceCount = attendance.filter(a => a.date === new Date().toISOString().split('T')[0]).length;
  const pendingApprovalsCount = attendance.filter(a => a.approvalStatus === 'PENDING').length;

  if (loading) {
    return <LoadingSpinner message="Initializing Administrator Control Center..." />;
  }

  return (
    <div>
      {/* Admin Title Bar */}
      <div
        className="card"
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '1.25rem 1.5rem',
          marginBottom: '1.5rem',
          backgroundColor: 'var(--bg-surface)',
          borderLeft: '4px solid var(--primary-color)',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div
            style={{
              padding: '0.75rem',
              borderRadius: '50%',
              backgroundColor: 'var(--primary-light)',
              color: 'var(--primary-color)',
            }}
          >
            <ShieldCheck size={28} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.35rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
              NirmaanSetu Command Center
            </h2>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginTop: '0.25rem' }}>
              <Badge label="Production Admin" variant="error" />
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                {workers.length} Workers • {projects.length} Projects • {sites.length} Active Sites
              </span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
          <button className="btn btn-secondary btn-sm" onClick={loadAllData}>
            <RefreshCw size={14} /> Refresh
          </button>
          <button className="btn btn-primary btn-sm" onClick={handleExportBackup}>
            <Download size={14} /> Backup Database
          </button>
        </div>
      </div>

      {error && <ErrorMessage message={error} onRetry={loadAllData} />}
      {success && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          <span>{success}</span>
        </div>
      )}

      {/* Navigation Sub-Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.35rem',
          overflowX: 'auto',
          paddingBottom: '0.5rem',
          borderBottom: '1px solid var(--border-color)',
          marginBottom: '1.5rem',
        }}
      >
        <button
          className={`btn ${activeTab === 'ANALYTICS' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('ANALYTICS')}
        >
          <Activity size={14} /> Analytics & KPIs
        </button>
        <button
          className={`btn ${activeTab === 'WORKERS' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('WORKERS')}
        >
          <HardHat size={14} /> Workers ({workers.length})
        </button>
        <button
          className={`btn ${activeTab === 'ATTENDANCE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('ATTENDANCE')}
        >
          <Calendar size={14} /> Attendance Approvals ({pendingApprovalsCount})
        </button>
        <button
          className={`btn ${activeTab === 'SALARIES' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('SALARIES')}
        >
          <DollarSign size={14} /> Salaries ({salaries.length})
        </button>
        <button
          className={`btn ${activeTab === 'PROJECTS' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('PROJECTS')}
        >
          <Building size={14} /> Projects & Sites ({projects.length})
        </button>
        <button
          className={`btn ${activeTab === 'MATERIALS' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('MATERIALS')}
        >
          <Package size={14} /> Materials ({materials.length})
        </button>
        <button
          className={`btn ${activeTab === 'EXPENSES' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('EXPENSES')}
        >
          <Receipt size={14} /> Expenses ({expenses.length})
        </button>
        <button
          className={`btn ${activeTab === 'INVOICES' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('INVOICES')}
        >
          <FileText size={14} /> Invoices ({payments.length})
        </button>
        <button
          className={`btn ${activeTab === 'DPR' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('DPR')}
        >
          <FileText size={14} /> DPR Logs ({dprs.length})
        </button>
        <button
          className={`btn ${activeTab === 'AUDIT' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('AUDIT')}
        >
          <ShieldCheck size={14} /> Audit Trail
        </button>
        <button
          className={`btn ${activeTab === 'BACKUP' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('BACKUP')}
        >
          <Database size={14} /> Backup & Restore
        </button>
      </div>

      {/* 1. ANALYTICS & KPIS */}
      {activeTab === 'ANALYTICS' && (
        <div>
          {/* Top KPI Cards */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1rem', marginBottom: '1.5rem' }}>
            <div className="card" style={{ padding: '1.25rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Active Workforce</span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--primary-color)', margin: '0.25rem 0' }}>
                {workers.length}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--success-color)' }}>
                {todayAttendanceCount} Workers Checked-In Today
              </span>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Pending Approvals</span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: pendingApprovalsCount > 0 ? 'var(--warning-color)' : 'var(--text-primary)', margin: '0.25rem 0' }}>
                {pendingApprovalsCount}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                GPS & Selfie Attendance Queue
              </span>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Total Expenses</span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)', margin: '0.25rem 0' }}>
                ₹{totalExpensesSum.toLocaleString('en-IN')}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                {expenses.length} Expense Transactions
              </span>
            </div>

            <div className="card" style={{ padding: '1.25rem' }}>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase' }}>Low Stock Materials</span>
              <div style={{ fontSize: '1.75rem', fontWeight: 700, color: lowStockCount > 0 ? 'var(--error-color)' : 'var(--success-color)', margin: '0.25rem 0' }}>
                {lowStockCount}
              </div>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                {lowStockCount > 0 ? 'Requires immediate restock' : 'All items well stocked'}
              </span>
            </div>
          </div>

          {/* Breakdown Grids */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>
                Workforce Trade Distribution
              </h3>
              {workers.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No workers registered yet.</p>
              ) : (
                <div>
                  {['Raj Mistri (Mason)', 'Carpenter', 'Electrician', 'Plumber', 'Painter', 'General Labour'].map(t => {
                    const count = workers.filter(w => w.trade === t).length;
                    const pct = Math.round((count / (workers.length || 1)) * 100);
                    return (
                      <div key={t} style={{ marginBottom: '0.75rem' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginBottom: '3px' }}>
                          <span>{t}</span>
                          <span style={{ fontWeight: 600 }}>{count} ({pct}%)</span>
                        </div>
                        <div style={{ height: '6px', backgroundColor: 'var(--bg-container)', borderRadius: '3px', overflow: 'hidden' }}>
                          <div style={{ height: '100%', width: `${pct}%`, backgroundColor: 'var(--primary-color)' }} />
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>
                Recent Operational Activity
              </h3>
              {auditLogs.length === 0 ? (
                <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No recent audit activity.</p>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                  {auditLogs.slice(0, 5).map(log => (
                    <div key={log.id} style={{ fontSize: '0.825rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                      <div style={{ fontWeight: 600, color: 'var(--text-primary)' }}>{log.details}</div>
                      <div style={{ color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                        {new Date(log.timestamp).toLocaleTimeString()} by {log.userName}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* 2. WORKERS CRUD */}
      {activeTab === 'WORKERS' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Workforce Roster ({workers.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsWorkerModalOpen(true)}>
              <Plus size={16} /> Add New Worker
            </button>
          </div>

          {workers.length === 0 ? (
            <EmptyState
              title="No Workers Added"
              description="Register construction tradespeople, mason masters, electricians, carpenters, and helpers."
              actionText="Add First Worker"
              onAction={() => setIsWorkerModalOpen(true)}
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>NAME</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>TRADE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>PHONE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DAILY WAGE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ASSIGNED SITE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {workers.map(w => (
                    <tr key={w.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{w.fullName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={w.trade} variant="info" />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{w.phone}</td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>₹{w.dailyWage}/day</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{w.assignedSiteName || 'Unassigned'}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={w.status} variant={w.status === 'ACTIVE' ? 'success' : 'neutral'} />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ padding: '2px 6px', color: 'var(--error-color)' }}
                          onClick={() => handleDeleteWorker(w)}
                        >
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 3. ATTENDANCE APPROVALS */}
      {activeTab === 'ATTENDANCE' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1.25rem' }}>
            GPS & Selfie Attendance Verification Queue
          </h3>

          {attendance.length === 0 ? (
            <EmptyState
              title="No Attendance Logs"
              description="When workers check in on-site using GPS and selfie verification, records appear here for supervisor approval."
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>WORKER</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DATE / TIME</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>SITE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>GPS & GEOFENCE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>SELFIE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>APPROVAL ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {attendance.map(att => (
                    <tr key={att.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div style={{ fontWeight: 600 }}>{att.workerName}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{att.workerTrade}</div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div>{att.date}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{att.checkInTime}</div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{att.siteName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.isWithinGeofence ? 'Inside Geofence' : `Outside (${att.distanceFromSiteMeters}m)`}
                          variant={att.isWithinGeofence ? 'success' : 'error'}
                        />
                        <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                          Lat: {att.gpsCoordinates?.latitude?.toFixed(4)}, Lng: {att.gpsCoordinates?.longitude?.toFixed(4)}
                        </div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        {att.selfieUrl ? (
                          <img
                            src={att.selfieUrl}
                            alt="Selfie"
                            style={{ width: '40px', height: '40px', borderRadius: '50%', objectFit: 'cover', cursor: 'pointer', border: '1px solid var(--border-color)' }}
                            onClick={() => setSelectedSelfiePreview(att.selfieUrl || null)}
                          />
                        ) : (
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>No Photo</span>
                        )}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.approvalStatus}
                          variant={att.approvalStatus === 'APPROVED' ? 'success' : att.approvalStatus === 'REJECTED' ? 'error' : 'warning'}
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        {att.approvalStatus === 'PENDING' ? (
                          <div style={{ display: 'flex', gap: '0.5rem' }}>
                            <button
                              className="btn btn-primary btn-sm"
                              style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                              onClick={() => handleApproveAttendance(att.id, 'APPROVED')}
                            >
                              <CheckCircle size={14} /> Approve
                            </button>
                            <button
                              className="btn btn-secondary btn-sm"
                              style={{ padding: '2px 8px', fontSize: '0.75rem', color: 'var(--error-color)' }}
                              onClick={() => handleApproveAttendance(att.id, 'REJECTED')}
                            >
                              <XCircle size={14} /> Reject
                            </button>
                          </div>
                        ) : (
                          <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                            Processed
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 4. WORKER SALARY MANAGEMENT */}
      {activeTab === 'SALARIES' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Worker Wage Disbursals & Slips ({salaries.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsSalaryModalOpen(true)}>
              <Plus size={16} /> Calculate Monthly Salary
            </button>
          </div>

          {salaries.length === 0 ? (
            <EmptyState
              title="No Salaries Calculated"
              description="Calculate monthly wage slips for workers based on days worked, overtime hours, and bonuses."
              actionText="Generate Salary Voucher"
              onAction={() => setIsSalaryModalOpen(true)}
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>WORKER</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>MONTH</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ATTENDANCE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>OVERTIME</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>NET PAYABLE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {salaries.map(sal => (
                    <tr key={sal.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{sal.workerName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{sal.month}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{sal.daysPresent} days (₹{sal.dailyRate}/d)</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{sal.overtimeHours} hrs (₹{sal.overtimePay})</td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700, color: 'var(--success-color)' }}>
                        ₹{sal.netPayable.toLocaleString('en-IN')}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={sal.status} variant={sal.status === 'PAID' ? 'success' : 'warning'} />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                            onClick={() => generateSalarySlipPDF(sal)}
                          >
                            <Download size={12} /> Pay Slip PDF
                          </button>
                          {sal.status === 'PENDING' && (
                            <button
                              className="btn btn-primary btn-sm"
                              style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                              onClick={async () => {
                                const ref = prompt('Enter payment transaction reference (e.g. UPI/NEFT ID):', 'UPI-9821890');
                                if (ref) {
                                  await apiClient.salaries.markPaid(sal.id, 'UPI', ref);
                                  setSalaries(prev => prev.map(s => s.id === sal.id ? { ...s, status: 'PAID', paymentRef: ref } : s));
                                  setSuccess('Salary marked as PAID in database!');
                                }
                              }}
                            >
                              Mark Paid
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 5. PROJECTS & SITES */}
      {activeTab === 'PROJECTS' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          {/* Projects Column */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
                Active Projects ({projects.length})
              </h3>
              <button className="btn btn-primary btn-sm" onClick={() => setIsProjectModalOpen(true)}>
                <Plus size={16} /> New Project
              </button>
            </div>

            {projects.length === 0 ? (
              <EmptyState title="No Projects" description="Create a project to initiate milestone workflows." />
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {projects.map(p => (
                  <div key={p.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <h4 style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>{p.title}</h4>
                      <Badge label={p.status} variant="info" />
                    </div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                      Client: {p.clientName} | Budget: ₹{p.budget?.toLocaleString('en-IN')}
                    </div>
                    <div style={{ marginTop: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Progress: {p.progressPercentage}%</span>
                      <input
                        type="range"
                        min="0"
                        max="100"
                        value={p.progressPercentage}
                        onChange={async (e) => {
                          const val = parseInt(e.target.value);
                          await apiClient.projects.updateProject(p.id, { title: p.title, description: p.description, status: val === 100 ? 'COMPLETED' : 'IN_PROGRESS' }).catch(() => {});
                          setProjects(prev => prev.map(item => item.id === p.id ? { ...item, progressPercentage: val } : item));
                        }}
                        style={{ flex: 1 }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Sites Column */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
                Construction Sites ({sites.length})
              </h3>
              <button className="btn btn-secondary btn-sm" onClick={() => setIsSiteModalOpen(true)}>
                <Plus size={16} /> Register Site
              </button>
            </div>

            {sites.length === 0 ? (
              <EmptyState title="No Sites" description="Define site GPS coordinates and geofence radius for worker check-ins." />
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {sites.map(s => (
                  <div key={s.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '1rem' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <h4 style={{ margin: 0, fontSize: '0.95rem', fontWeight: 600 }}>{s.name}</h4>
                      <button
                        className="btn btn-secondary btn-sm"
                        style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                        onClick={() => setSelectedSiteMap(s)}
                      >
                        <Compass size={12} /> View Map
                      </button>
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                      {s.address}, {s.city} (Geofence: {s.geofenceRadiusMeters}m)
                    </div>
                    <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                      Supervisor: {s.managerName} ({s.contactPhone})
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* 6. MATERIAL INVENTORY */}
      {activeTab === 'MATERIALS' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Site Material Inventory ({materials.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsMaterialModalOpen(true)}>
              <Plus size={16} /> Add Material
            </button>
          </div>

          {materials.length === 0 ? (
            <EmptyState
              title="Inventory Empty"
              description="Track cement, steel, bricks, sand, aggregate, paint, and plumbing supplies with low-stock alerts."
              actionText="Add Material"
              onAction={() => setIsMaterialModalOpen(true)}
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>MATERIAL</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>CATEGORY</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>CURRENT STOCK</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>MIN THRESHOLD</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STOCK ADJUST</th>
                  </tr>
                </thead>
                <tbody>
                  {materials.map(m => (
                    <tr key={m.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{m.itemName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={m.category} variant="info" />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700 }}>
                        {m.quantity} {m.unit}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{m.minThreshold} {m.unit}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={m.status.replace('_', ' ')}
                          variant={m.status === 'IN_STOCK' ? 'success' : m.status === 'LOW_STOCK' ? 'warning' : 'error'}
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div style={{ display: 'flex', gap: '0.35rem' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ padding: '2px 8px' }}
                            onClick={() => handleAdjustStock(m, 10)}
                          >
                            +10
                          </button>
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ padding: '2px 8px' }}
                            onClick={() => handleAdjustStock(m, -10)}
                          >
                            -10
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 7. EXPENSES */}
      {activeTab === 'EXPENSES' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Project Expense Tracking ({expenses.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsExpenseModalOpen(true)}>
              <Plus size={16} /> Record Expense
            </button>
          </div>

          {expenses.length === 0 ? (
            <EmptyState
              title="No Expenses Logged"
              description="Record machinery rental, material bills, transport, permits, and miscellaneous project spending."
              actionText="Log First Expense"
              onAction={() => setIsExpenseModalOpen(true)}
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DATE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DESCRIPTION</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>CATEGORY</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>AMOUNT</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>PAID TO</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ACTION</th>
                  </tr>
                </thead>
                <tbody>
                  {expenses.map(exp => (
                    <tr key={exp.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{exp.date}</td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{exp.description}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={exp.category} variant="neutral" />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700 }}>
                        ₹{exp.amount.toLocaleString('en-IN')}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{exp.paidTo}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={exp.status} variant={exp.status === 'APPROVED' ? 'success' : 'warning'} />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        {exp.status === 'PENDING' && (
                          <button
                            className="btn btn-primary btn-sm"
                            style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                            onClick={async () => {
                              await apiClient.expenses.review(exp.id, 'APPROVED');
                              setExpenses(prev => prev.map(e => e.id === exp.id ? { ...e, status: 'APPROVED' } : e));
                            }}
                          >
                            Approve
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 8. INVOICES */}
      {activeTab === 'INVOICES' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Client Invoices & GST Bills ({payments.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsInvoiceModalOpen(true)}>
              <Plus size={16} /> Generate Invoice
            </button>
          </div>

          {payments.length === 0 ? (
            <EmptyState
              title="No Invoices Issued"
              description="Create client billing invoices with GST calculations, payment terms, and downloadable PDF receipts."
              actionText="Generate First Invoice"
              onAction={() => setIsInvoiceModalOpen(true)}
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>INVOICE #</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>CLIENT / PROJECT</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>AMOUNT</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DUE DATE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map(inv => (
                    <tr key={inv.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{inv.invoiceNumber}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div>{inv.clientName || 'Client'}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{inv.projectTitle}</div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700 }}>
                        ₹{inv.totalAmount.toLocaleString('en-IN')}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{inv.dueDate}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={inv.status}
                          variant={inv.status === 'PAID' ? 'success' : inv.status === 'OVERDUE' ? 'error' : 'warning'}
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div style={{ display: 'flex', gap: '0.5rem' }}>
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                            onClick={() => generateInvoicePDF(inv)}
                          >
                            <Download size={12} /> PDF
                          </button>
                          <button
                            className="btn btn-secondary btn-sm"
                            style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                            onClick={() => shareInvoiceViaWhatsApp(inv)}
                          >
                            <Share2 size={12} /> Share
                          </button>
                          {inv.status === 'PENDING' && (
                            <button
                              className="btn btn-primary btn-sm"
                              style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                              onClick={async () => {
                                const ref = prompt('Enter payment transaction reference (e.g. UTR / NEFT ID):', 'NEFT-889912');
                                if (ref) {
                                  await apiClient.invoices.markPaid(inv.id, 'NEFT', ref);
                                  setPayments(prev => prev.map(p => p.id === inv.id ? { ...p, status: 'PAID', transactionRef: ref } : p));
                                  setSuccess('Invoice marked as PAID in database!');
                                }
                              }}
                            >
                              Mark Paid
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* 9. DPR LOGS */}
      {activeTab === 'DPR' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.25rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Daily Progress Reports (DPR) ({dprs.length})
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setIsDprModalOpen(true)}>
              <Plus size={16} /> Submit DPR
            </button>
          </div>

          {dprs.length === 0 ? (
            <EmptyState
              title="No DPRs"
              description="Daily engineering site reports recording weather, labor deployed, and tasks completed."
              actionText="Create DPR"
              onAction={() => setIsDprModalOpen(true)}
            />
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {dprs.map(d => (
                <div key={d.id} style={{ border: '1px solid var(--border-color)', borderRadius: 'var(--radius-sm)', padding: '1.25rem' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h4 style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>
                      {d.projectTitle} — {d.reportDate} ({d.weatherCondition})
                    </h4>
                    <button
                      className="btn btn-secondary btn-sm"
                      onClick={() => shareDPRViaWhatsApp(d)}
                    >
                      <Share2 size={14} /> WhatsApp Share
                    </button>
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                    <strong>Workforce Deployed:</strong> {d.skilledLaborCount} Skilled, {d.unskilledLaborCount} Helpers
                  </div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '0.25rem' }}>
                    <strong>Tasks Completed:</strong> {d.tasksCompleted}
                  </div>
                  {d.delaysOrIssues && (
                    <div style={{ fontSize: '0.85rem', color: 'var(--error-color)', marginTop: '0.25rem' }}>
                      <strong>Issues:</strong> {d.delaysOrIssues}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* 10. AUDIT TRAIL */}
      {activeTab === 'AUDIT' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1.25rem' }}>
            System Audit Trail & Compliance Log
          </h3>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                  <th style={{ padding: '0.75rem 0.5rem' }}>TIMESTAMP</th>
                  <th style={{ padding: '0.75rem 0.5rem' }}>USER</th>
                  <th style={{ padding: '0.75rem 0.5rem' }}>ACTION</th>
                  <th style={{ padding: '0.75rem 0.5rem' }}>ENTITY</th>
                  <th style={{ padding: '0.75rem 0.5rem' }}>DETAILS</th>
                </tr>
              </thead>
              <tbody>
                {auditLogs.map(log => (
                  <tr key={log.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.825rem' }}>
                    <td style={{ padding: '0.75rem 0.5rem', color: 'var(--text-muted)' }}>
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                    <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{log.userName}</td>
                    <td style={{ padding: '0.75rem 0.5rem' }}>
                      <Badge label={log.action} variant="neutral" />
                    </td>
                    <td style={{ padding: '0.75rem 0.5rem' }}>{log.targetType}</td>
                    <td style={{ padding: '0.75rem 0.5rem' }}>{log.details}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* 11. BACKUP & RESTORE */}
      {activeTab === 'BACKUP' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
            Disaster Recovery, Database Backup & Restore
          </h3>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '1.5rem' }}>
            Export all Firestore collections (Workers, Attendance, Sites, Projects, Invoices, Expenses, Materials) to a structured JSON file, or restore from a previous backup.
          </p>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1.5rem' }}>
            <div style={{ padding: '1.25rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
              <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem' }}>Export Snapshot</h4>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Generates a complete timestamped JSON dump of the production database.
              </p>
              <button className="btn btn-primary" onClick={handleExportBackup}>
                <Download size={16} /> Download Backup (.json)
              </button>
            </div>

            <div style={{ padding: '1.25rem', border: '1px solid var(--border-color)', borderRadius: 'var(--radius-md)' }}>
              <h4 style={{ margin: '0 0 0.5rem 0', fontSize: '1rem' }}>Restore Snapshot</h4>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '1rem' }}>
                Upload a JSON backup file to restore database collections.
              </p>
              <label className="btn btn-secondary" style={{ cursor: 'pointer', display: 'inline-flex' }}>
                <Upload size={16} /> Select Backup File to Restore
                <input
                  type="file"
                  accept=".json"
                  onChange={handleImportBackup}
                  style={{ display: 'none' }}
                />
              </label>
            </div>
          </div>
        </div>
      )}

      {/* MODAL: CREATE WORKER */}
      <Modal isOpen={isWorkerModalOpen} onClose={() => setIsWorkerModalOpen(false)} title="Register Worker">
        <form onSubmit={handleCreateWorker}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Full Name</label>
            <input type="text" value={wName} onChange={e => setWName(e.target.value)} required placeholder="e.g. Ramesh Kumar" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Phone Number (India)</label>
            <input type="tel" value={wPhone} onChange={e => setWPhone(e.target.value)} required placeholder="e.g. 9876543210" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Trade Specialization</label>
            <select value={wTrade} onChange={e => setWTrade(e.target.value as TradeType)}>
              <option value="Raj Mistri (Mason)">Raj Mistri (Mason)</option>
              <option value="Carpenter">Carpenter</option>
              <option value="Electrician">Electrician</option>
              <option value="Plumber">Plumber</option>
              <option value="Painter">Painter</option>
              <option value="Tile Worker">Tile Worker</option>
              <option value="Welder">Welder</option>
              <option value="General Labour">General Labour</option>
              <option value="Site Supervisor">Site Supervisor</option>
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Daily Wage Rate (INR)</label>
            <input type="number" value={wWage} onChange={e => setWWage(Number(e.target.value))} required min="300" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Assigned Site</label>
            <select value={wSiteId} onChange={e => setWSiteId(e.target.value)}>
              {sites.map(s => (
                <option key={s.id} value={s.id}>{s.name} ({s.city})</option>
              ))}
            </select>
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Create Worker Record</button>
        </form>
      </Modal>

      {/* MODAL: CREATE PROJECT */}
      <Modal isOpen={isProjectModalOpen} onClose={() => setIsProjectModalOpen(false)} title="Create Construction Project">
        <form onSubmit={handleCreateProject}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project Title</label>
            <input type="text" value={pTitle} onChange={e => setPTitle(e.target.value)} required placeholder="e.g. Omkar Heights Tower B" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Client Name / Entity</label>
            <input type="text" value={pClientName} onChange={e => setPClientName(e.target.value)} required placeholder="e.g. Horizon Developers Ltd" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project Location</label>
            <input type="text" value={pLocation} onChange={e => setPLocation(e.target.value)} required placeholder="e.g. Andheri East, Mumbai" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Budget (INR)</label>
            <input type="number" value={pBudget} onChange={e => setPBudget(Number(e.target.value))} required />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Estimated Completion Date</label>
            <input type="date" value={pEndDate} onChange={e => setPEndDate(e.target.value)} required />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Description & Scope</label>
            <textarea value={pDesc} onChange={e => setPDesc(e.target.value)} rows={3} placeholder="Scope of civil construction..." />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Create Project & Initialize Timeline</button>
        </form>
      </Modal>

      {/* MODAL: CREATE SITE */}
      <Modal isOpen={isSiteModalOpen} onClose={() => setIsSiteModalOpen(false)} title="Register Construction Site & Geofence">
        <form onSubmit={handleCreateSite}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Site Name</label>
            <input type="text" value={sName} onChange={e => setSName(e.target.value)} required placeholder="e.g. Site #4 - North Wing" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project</label>
            <select value={sProjectId} onChange={e => setSProjectId(e.target.value)}>
              {projects.map(p => (
                <option key={p.id} value={p.id}>{p.title}</option>
              ))}
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Latitude</label>
              <input type="number" step="0.0001" value={sLat} onChange={e => setSLat(Number(e.target.value))} required />
            </div>
            <div>
              <label>Longitude</label>
              <input type="number" step="0.0001" value={sLng} onChange={e => setSLng(Number(e.target.value))} required />
            </div>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Geofence Radius (Meters)</label>
            <input type="number" value={sRadius} onChange={e => setSRadius(Number(e.target.value))} required min="50" max="2000" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Address, City, State</label>
            <input type="text" value={sAddress} onChange={e => setSAddress(e.target.value)} required placeholder="Site address" />
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginTop: '0.5rem' }}>
              <input type="text" value={sCity} onChange={e => setSCity(e.target.value)} placeholder="City (e.g. Mumbai)" required />
              <input type="text" value={sState} onChange={e => setSState(e.target.value)} placeholder="State (e.g. Maharashtra)" required />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginBottom: '1rem' }}>
            <div>
              <label>Site Manager</label>
              <input type="text" value={sManager} onChange={e => setSManager(e.target.value)} required placeholder="Manager Name" />
            </div>
            <div>
              <label>Contact Phone</label>
              <input type="tel" value={sPhone} onChange={e => setSPhone(e.target.value)} required placeholder="9876543210" />
            </div>
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Register Site with Geofence</button>
        </form>
      </Modal>

      {/* MODAL: ADD MATERIAL */}
      <Modal isOpen={isMaterialModalOpen} onClose={() => setIsMaterialModalOpen(false)} title="Add Material to Inventory">
        <form onSubmit={handleCreateMaterial}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Item Name</label>
            <input type="text" value={mName} onChange={e => setMName(e.target.value)} required placeholder="e.g. Ultratech Cement Grade 53" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Category</label>
            <select value={mCategory} onChange={e => setMCategory(e.target.value as MaterialCategory)}>
              <option value="CEMENT">Cement</option>
              <option value="STEEL">TMT Steel Rebars</option>
              <option value="BRICKS">Red Clay Bricks / AAC Blocks</option>
              <option value="SAND">River Sand / M-Sand</option>
              <option value="AGGREGATE">Crushed Aggregate</option>
              <option value="ELECTRICAL">Electrical Conduits & Wires</option>
              <option value="PLUMBING">Plumbing & Sanitary</option>
              <option value="PAINT">Paints & Primers</option>
              <option value="OTHER">Other Construction Consumables</option>
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Initial Quantity</label>
              <input type="number" value={mQty} onChange={e => setMQty(Number(e.target.value))} required min="1" />
            </div>
            <div>
              <label>Unit of Measure</label>
              <select value={mUnit} onChange={e => setMUnit(e.target.value as any)}>
                <option value="BAGS">Bags</option>
                <option value="TONS">Tons</option>
                <option value="PIECES">Pieces</option>
                <option value="TRUCKS">Trucks</option>
                <option value="METERS">Meters</option>
                <option value="LITRES">Litres</option>
              </select>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Min Low-Stock Threshold</label>
              <input type="number" value={mThreshold} onChange={e => setMThreshold(Number(e.target.value))} required min="1" />
            </div>
            <div>
              <label>Unit Cost (INR)</label>
              <input type="number" value={mUnitCost} onChange={e => setMUnitCost(Number(e.target.value))} required min="1" />
            </div>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Supplier Name</label>
            <input type="text" value={mSupplier} onChange={e => setMSupplier(e.target.value)} placeholder="e.g. Apex Building Materials" />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Add Material Item</button>
        </form>
      </Modal>

      {/* MODAL: RECORD EXPENSE */}
      <Modal isOpen={isExpenseModalOpen} onClose={() => setIsExpenseModalOpen(false)} title="Record Project Expense">
        <form onSubmit={handleCreateExpense}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project</label>
            <select value={expProjectId} onChange={e => setExpProjectId(e.target.value)}>
              {projects.map(p => (
                <option key={p.id} value={p.id}>{p.title}</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Expense Category</label>
            <select value={expCategory} onChange={e => setExpCategory(e.target.value as any)}>
              <option value="MATERIALS">Material Purchase</option>
              <option value="EQUIPMENT">Machinery / Crane Rental</option>
              <option value="TRANSPORT">Freight & Transport</option>
              <option value="PERMITS">Municipal Permits & Inspection</option>
              <option value="UTILITIES">Site Electricity & Water</option>
              <option value="MISCELLANEOUS">Miscellaneous</option>
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Amount (INR)</label>
            <input type="number" value={expAmount} onChange={e => setExpAmount(Number(e.target.value))} required min="1" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Paid To (Vendor / Contractor)</label>
            <input type="text" value={expPaidTo} onChange={e => setExpPaidTo(e.target.value)} required placeholder="Vendor name" />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Description</label>
            <input type="text" value={expDesc} onChange={e => setExpDesc(e.target.value)} required placeholder="Details of expense" />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Record Expense</button>
        </form>
      </Modal>

      {/* MODAL: GENERATE INVOICE */}
      <Modal isOpen={isInvoiceModalOpen} onClose={() => setIsInvoiceModalOpen(false)} title="Generate Client Tax Invoice">
        <form onSubmit={handleCreateInvoice}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Invoice Number</label>
            <input type="text" value={invNumber} onChange={e => setInvNumber(e.target.value)} required />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project</label>
            <select value={invProjectId} onChange={e => {
              setInvProjectId(e.target.value);
              const p = projects.find(item => item.id === e.target.value);
              if (p) setInvClientName(p.clientName);
            }}>
              {projects.map(p => (
                <option key={p.id} value={p.id}>{p.title}</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Billed To (Client)</label>
            <input type="text" value={invClientName} onChange={e => setInvClientName(e.target.value)} required />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Taxable Amount (INR)</label>
            <input type="number" value={invAmount} onChange={e => setInvAmount(Number(e.target.value))} required min="1000" />
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>+ 18% GST will be automatically calculated</span>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Due Date</label>
            <input type="date" value={invDueDate} onChange={e => setInvDueDate(e.target.value)} required />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Description</label>
            <input type="text" value={invDesc} onChange={e => setInvDesc(e.target.value)} required />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Generate & Save Invoice</button>
        </form>
      </Modal>

      {/* MODAL: CALCULATE SALARY */}
      <Modal isOpen={isSalaryModalOpen} onClose={() => setIsSalaryModalOpen(false)} title="Calculate Worker Salary Slip">
        <form onSubmit={handleGenerateSalary}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Select Worker</label>
            <select value={salWorkerId} onChange={e => setSalWorkerId(e.target.value)}>
              {workers.map(w => (
                <option key={w.id} value={w.id}>{w.fullName} ({w.trade} - ₹{w.dailyWage}/d)</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Salary Month (YYYY-MM)</label>
            <input type="month" value={salMonth} onChange={e => setSalMonth(e.target.value)} required />
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Full Days Present</label>
              <input type="number" value={salDays} onChange={e => setSalDays(Number(e.target.value))} required min="0" max="31" />
            </div>
            <div>
              <label>Half Days</label>
              <input type="number" value={salHalfDays} onChange={e => setSalHalfDays(Number(e.target.value))} required min="0" />
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Overtime Hours</label>
              <input type="number" value={salOtHours} onChange={e => setSalOtHours(Number(e.target.value))} min="0" />
            </div>
            <div>
              <label>Bonus (INR)</label>
              <input type="number" value={salBonus} onChange={e => setSalBonus(Number(e.target.value))} min="0" />
            </div>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Deductions / Advance Recovered (INR)</label>
            <input type="number" value={salDeduction} onChange={e => setSalDeduction(Number(e.target.value))} min="0" />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Calculate & Generate Pay Voucher</button>
        </form>
      </Modal>

      {/* MODAL: SUBMIT DPR */}
      <Modal isOpen={isDprModalOpen} onClose={() => setIsDprModalOpen(false)} title="Submit Daily Progress Report (DPR)">
        <form onSubmit={handleCreateDPR}>
          <div style={{ marginBottom: '1rem' }}>
            <label>Project</label>
            <select value={dprProjectId} onChange={e => setDprProjectId(e.target.value)}>
              {projects.map(p => (
                <option key={p.id} value={p.id}>{p.title}</option>
              ))}
            </select>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Site</label>
            <select value={dprSiteId} onChange={e => setDprSiteId(e.target.value)}>
              {sites.map(s => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Report Date</label>
              <input type="date" value={dprDate} onChange={e => setDprDate(e.target.value)} required />
            </div>
            <div>
              <label>Weather Condition</label>
              <select value={dprWeather} onChange={e => setDprWeather(e.target.value as any)}>
                <option value="Sunny">Sunny</option>
                <option value="Cloudy">Cloudy</option>
                <option value="Rainy">Rainy</option>
                <option value="Stormy">Stormy</option>
              </select>
            </div>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
            <div>
              <label>Skilled Labor Count</label>
              <input type="number" value={dprSkilled} onChange={e => setDprSkilled(Number(e.target.value))} required min="0" />
            </div>
            <div>
              <label>Helpers / Unskilled</label>
              <input type="number" value={dprUnskilled} onChange={e => setDprUnskilled(Number(e.target.value))} required min="0" />
            </div>
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Tasks Completed Today</label>
            <textarea value={dprTasks} onChange={e => setDprTasks(e.target.value)} rows={3} required placeholder="Detailed engineering report..." />
          </div>
          <div style={{ marginBottom: '1rem' }}>
            <label>Hindrances / Issues</label>
            <input type="text" value={dprIssues} onChange={e => setDprIssues(e.target.value)} placeholder="Material delay, power outage, etc." />
          </div>
          <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>Submit DPR</button>
        </form>
      </Modal>

      {/* MODAL: SELFIE PREVIEW */}
      {selectedSelfiePreview && (
        <Modal isOpen={!!selectedSelfiePreview} onClose={() => setSelectedSelfiePreview(null)} title="Verification Selfie">
          <div style={{ textAlign: 'center' }}>
            <img src={selectedSelfiePreview} alt="Selfie" style={{ maxWidth: '100%', maxHeight: '400px', borderRadius: 'var(--radius-md)' }} />
          </div>
        </Modal>
      )}

      {/* MODAL: SITE MAP */}
      {selectedSiteMap && (
        <MapModal
          isOpen={!!selectedSiteMap}
          onClose={() => setSelectedSiteMap(null)}
          site={selectedSiteMap}
        />
      )}
    </div>
  );
};
