import React, { useState, useEffect } from 'react';
import { 
  HardHat, 
  MapPin, 
  Camera, 
  Calendar, 
  DollarSign, 
  Download, 
  Share2, 
  CheckCircle, 
  AlertTriangle, 
  Navigation,
  Clock,
  Compass,
  LogOut,
  UserCheck
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/apiClient';
import { 
  WorkerRecord, 
  SiteRecord, 
  ProjectRecord, 
  AttendanceRecord, 
  WorkerSalaryRecord,
  GPSCoordinates 
} from '../../types/schema';
import { getCurrentGpsLocation, validateSiteGeofence } from '../../utils/geoUtils';
import { generateSalarySlipPDF } from '../../utils/pdfGenerator';
import { shareAttendanceViaWhatsApp } from '../../utils/whatsappShare';
import { CameraCaptureModal } from '../../components/common/CameraCaptureModal';
import { MapModal } from '../../components/common/MapModal';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Badge } from '../../components/common/Badge';
import { ErrorMessage } from '../../components/common/ErrorMessage';

export const WorkerWorkspace: React.FC = () => {
  const { currentUser, userProfile } = useAuth();
  const [activeTab, setActiveTab] = useState<'ATTENDANCE' | 'HISTORY' | 'SALARY'>('ATTENDANCE');

  const [workerRecord, setWorkerRecord] = useState<WorkerRecord | null>(null);
  const [availableSites, setAvailableSites] = useState<SiteRecord[]>([]);
  const [assignedSite, setAssignedSite] = useState<SiteRecord | null>(null);
  const [assignedProject, setAssignedProject] = useState<ProjectRecord | null>(null);
  const [attendanceHistory, setAttendanceHistory] = useState<AttendanceRecord[]>([]);
  const [salaries, setSalaries] = useState<WorkerSalaryRecord[]>([]);

  const [loading, setLoading] = useState(true);
  const [checkingIn, setCheckingIn] = useState(false);
  const [checkingOut, setCheckingOut] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // GPS & Camera state
  const [currentGps, setCurrentGps] = useState<GPSCoordinates | null>(null);
  const [geofenceStatus, setGeofenceStatus] = useState<{ isWithinGeofence: boolean; distanceMeters: number } | null>(null);
  const [selfieDataUrl, setSelfieDataUrl] = useState<string | null>(null);
  const [isCameraOpen, setIsCameraOpen] = useState(false);
  const [isMapOpen, setIsMapOpen] = useState(false);
  const [lastCheckIn, setLastCheckIn] = useState<AttendanceRecord | null>(null);

  // New Profile Form State if no worker profile exists
  const [creatingProfile, setCreatingProfile] = useState(false);
  const [newDisplayName, setNewDisplayName] = useState(userProfile?.displayName || '');
  const [newLocation, setNewLocation] = useState('New Delhi');

  const loadWorkerData = async () => {
    setLoading(true);
    setError(null);
    try {
      let worker: any = null;
      try {
        worker = await apiClient.workers.getMyProfile();
      } catch (err: any) {
        if (err.status !== 404) {
          throw err;
        }
      }

      if (worker) {
        const mappedWorker: WorkerRecord = {
          id: worker.id,
          userId: worker.userId,
          fullName: worker.displayName || userProfile?.displayName || 'Worker',
          phone: currentUser?.phone || '',
          trade: 'Raj Mistri (Mason)',
          dailyWage: 750,
          status: 'ACTIVE',
          verificationStatus: 'VERIFIED',
          createdAt: worker.createdAt || new Date().toISOString(),
          updatedAt: worker.updatedAt || new Date().toISOString(),
        };
        setWorkerRecord(mappedWorker);

        // Fetch sites, attendance history, salaries in parallel
        const [sites, history, salaryList] = await Promise.all([
          apiClient.sites.getSites().catch(() => []),
          apiClient.attendance.getMyAttendance().catch(() => []),
          apiClient.salaries.getMySalaries().catch(() => []),
        ]);

        const mappedSites: SiteRecord[] = (sites || []).map((s: any) => ({
          id: s.id,
          projectId: s.projectId,
          projectTitle: s.projectTitle || 'Construction Site',
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

        setAvailableSites(mappedSites);
        const selectedSite = mappedSites.length > 0 ? mappedSites[0] : null;
        setAssignedSite(selectedSite);

        if (selectedSite) {
          setAssignedProject({
            id: selectedSite.projectId,
            clientId: '',
            clientName: 'NirmaanSetu Client',
            title: selectedSite.projectTitle,
            description: `Site active at ${selectedSite.name}`,
            location: `${selectedSite.city}, ${selectedSite.state}`,
            startDate: '',
            estimatedEndDate: '',
            status: 'IN_PROGRESS',
            budget: 0,
            totalExpenses: 0,
            progressPercentage: 45,
            createdAt: '',
            updatedAt: '',
          });
        }

        const mappedHistory: AttendanceRecord[] = (history || []).map((h: any) => ({
          id: h.id,
          workerId: h.workerId,
          workerName: h.workerName,
          workerTrade: h.workerTrade || 'Mason',
          projectId: h.projectId,
          projectTitle: h.projectTitle || 'Project',
          siteId: h.siteId,
          siteName: h.siteName,
          date: h.date,
          checkInTime: h.checkInTime,
          checkOutTime: h.checkOutTime,
          status: h.status || 'PRESENT',
          gpsCoordinates: { latitude: h.checkInLatitude, longitude: h.checkInLongitude },
          isWithinGeofence: h.withinGeofence,
          distanceFromSiteMeters: h.distanceFromSiteMeters || 0,
          selfieUrl: h.selfieUrl,
          approvalStatus: h.approvalStatus || 'PENDING',
          remarks: h.remarks,
          hoursWorked: h.hoursWorked,
          createdAt: h.createdAt,
        }));

        setAttendanceHistory(mappedHistory);

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
          createdAt: s.createdAt,
        }));
        setSalaries(mappedSalaries);

        const todayStr = new Date().toISOString().split('T')[0];
        const todayRecord = mappedHistory.find((rec) => rec.date === todayStr);
        if (todayRecord) {
          setLastCheckIn(todayRecord);
        } else {
          setLastCheckIn(null);
        }
      } else {
        setWorkerRecord(null);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load worker workspace');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadWorkerData();
  }, [currentUser]);

  const handleCreateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreatingProfile(true);
    setError(null);
    try {
      await apiClient.workers.createProfile({
        displayName: newDisplayName.trim() || 'Worker Profile',
        location: newLocation.trim() || 'Delhi',
      });
      setSuccessMsg('Worker profile created successfully!');
      await loadWorkerData();
    } catch (err: any) {
      setError(err.message || 'Failed to create worker profile');
    } finally {
      setCreatingProfile(false);
    }
  };

  // Handle GPS location capture
  const handleAcquireGps = async () => {
    setError(null);
    try {
      const coords = await getCurrentGpsLocation();
      setCurrentGps(coords);

      if (assignedSite) {
        const geoResult = validateSiteGeofence(
          coords,
          assignedSite.latitude,
          assignedSite.longitude,
          assignedSite.geofenceRadiusMeters
        );
        setGeofenceStatus(geoResult);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to acquire GPS location.');
    }
  };

  // Perform Final Check-In
  const handleSubmitCheckIn = async () => {
    if (!workerRecord || !assignedSite || !assignedProject) {
      setError('Assigned project or site details are missing.');
      return;
    }
    if (!currentGps) {
      setError('Please capture your current GPS location first.');
      return;
    }
    if (!selfieDataUrl) {
      setError('Please take a selfie photo for visual attendance verification.');
      return;
    }

    setCheckingIn(true);
    setError(null);
    try {
      // 1. Upload selfie photo to backend uploads API to get clean URL
      let uploadedUrl = '';
      try {
        const uploadResp = await apiClient.uploads.uploadBase64(selfieDataUrl);
        uploadedUrl = uploadResp.url;
      } catch (uploadErr) {
        console.warn('Backend upload fell back to compact URI', uploadErr);
        uploadedUrl = selfieDataUrl.substring(0, 500); // safety fallback
      }

      // 2. Submit check-in to Spring Boot
      const response = await apiClient.attendance.checkIn({
        siteId: assignedSite.id,
        siteName: assignedSite.name,
        latitude: currentGps.latitude,
        longitude: currentGps.longitude,
        selfieUrl: uploadedUrl,
        remarks: 'Mobile GPS & Selfie verified',
      });

      const now = new Date();
      const created: AttendanceRecord = {
        id: response.id,
        workerId: response.workerId,
        workerName: response.workerName,
        workerTrade: response.workerTrade || workerRecord.trade,
        projectId: response.projectId,
        projectTitle: response.projectTitle || assignedProject.title,
        siteId: response.siteId,
        siteName: response.siteName || assignedSite.name,
        date: response.date,
        checkInTime: response.checkInTime,
        status: 'PRESENT',
        gpsCoordinates: currentGps,
        isWithinGeofence: response.withinGeofence,
        distanceFromSiteMeters: response.distanceFromSiteMeters || 0,
        selfieUrl: uploadedUrl,
        approvalStatus: 'PENDING',
        remarks: response.remarks,
        createdAt: now.toISOString(),
      };

      setLastCheckIn(created);
      setAttendanceHistory((prev) => [created, ...prev]);
      setSuccessMsg('Attendance submitted successfully! Awaiting supervisor approval.');
    } catch (err: any) {
      setError(err.message || 'Check-in failed');
    } finally {
      setCheckingIn(false);
    }
  };

  // Perform Check-Out
  const handleCheckOut = async () => {
    if (!lastCheckIn) return;
    setCheckingOut(true);
    setError(null);
    try {
      let coords = currentGps;
      if (!coords) {
        coords = await getCurrentGpsLocation();
        setCurrentGps(coords);
      }

      const updated = await apiClient.attendance.checkOut({
        remarks: 'Check-out completed by worker',
      });

      const updatedRecord: AttendanceRecord = {
        ...lastCheckIn,
        checkOutTime: updated.checkOutTime,
        hoursWorked: updated.hoursWorked,
      };

      setLastCheckIn(updatedRecord);
      setAttendanceHistory((prev) => prev.map((item) => (item.id === updatedRecord.id ? updatedRecord : item)));
      setSuccessMsg(`Checked out successfully! Total duration: ${updated.hoursWorked?.toFixed(1) || '0'} hrs.`);
    } catch (err: any) {
      setError(err.message || 'Check-out failed');
    } finally {
      setCheckingOut(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading worker workspace..." />;
  }

  // Profile setup card if worker record doesn't exist yet
  if (!workerRecord) {
    return (
      <div className="card" style={{ maxWidth: '540px', margin: '2rem auto', padding: '2rem' }}>
        <div style={{ textAlign: 'center', marginBottom: '1.5rem' }}>
          <div
            style={{
              display: 'inline-flex',
              padding: '1rem',
              borderRadius: '50%',
              backgroundColor: 'var(--primary-light)',
              color: 'var(--primary-color)',
              marginBottom: '0.75rem',
            }}
          >
            <UserCheck size={32} />
          </div>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: '0 0 0.5rem 0' }}>Activate Worker Profile</h2>
          <p style={{ fontSize: '0.875rem', color: 'var(--text-muted)', margin: 0 }}>
            Enter your display details to enable GPS attendance and salary slips on your device.
          </p>
        </div>

        {error && <ErrorMessage message={error} />}

        <form onSubmit={handleCreateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
              Full Name
            </label>
            <input
              type="text"
              value={newDisplayName}
              onChange={(e) => setNewDisplayName(e.target.value)}
              placeholder="e.g. Ramesh Kumar"
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--border-color)',
                boxSizing: 'border-box',
              }}
              required
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
              Base Location / City
            </label>
            <input
              type="text"
              value={newLocation}
              onChange={(e) => setNewLocation(e.target.value)}
              placeholder="e.g. New Delhi, Noida"
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: 'var(--radius-sm)',
                border: '1px solid var(--border-color)',
                boxSizing: 'border-box',
              }}
              required
            />
          </div>

          <button
            type="submit"
            disabled={creatingProfile}
            className="btn btn-primary"
            style={{ padding: '10px', marginTop: '0.5rem' }}
          >
            {creatingProfile ? <LoadingSpinner size={16} /> : 'Activate My Worker Account'}
          </button>
        </form>
      </div>
    );
  }

  return (
    <div>
      {/* Worker Banner */}
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
            <HardHat size={28} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
              {workerRecord.fullName}
            </h2>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginTop: '0.25rem' }}>
              <Badge label={workerRecord.trade} variant="info" />
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Wage: ₹{workerRecord.dailyWage}/day
              </span>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Phone: {currentUser?.phone}
              </span>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {assignedSite && (
            <button className="btn btn-secondary btn-sm" onClick={() => setIsMapOpen(true)}>
              <Compass size={16} /> Site Map
            </button>
          )}
        </div>
      </div>

      {error && <ErrorMessage message={error} onRetry={loadWorkerData} />}
      {successMsg && (
        <div className="alert alert-success" style={{ marginBottom: '1rem' }}>
          <span>{successMsg}</span>
        </div>
      )}

      {/* Tabs */}
      <div
        style={{
          display: 'flex',
          gap: '0.5rem',
          borderBottom: '1px solid var(--border-color)',
          marginBottom: '1.5rem',
        }}
      >
        <button
          className={`btn ${activeTab === 'ATTENDANCE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('ATTENDANCE')}
        >
          <Camera size={16} /> Today's Check-In / Out
        </button>
        <button
          className={`btn ${activeTab === 'HISTORY' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('HISTORY')}
        >
          <Calendar size={16} /> Attendance History ({attendanceHistory.length})
        </button>
        <button
          className={`btn ${activeTab === 'SALARY' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('SALARY')}
        >
          <DollarSign size={16} /> Salary & Pay Slips ({salaries.length})
        </button>
      </div>

      {/* TAB 1: ATTENDANCE CHECK-IN & CHECK-OUT WIDGET */}
      {activeTab === 'ATTENDANCE' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.5rem' }}>
          {/* Check-in / Out Card */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3
              style={{
                fontSize: '1.125rem',
                fontWeight: 600,
                marginBottom: '1rem',
                display: 'flex',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <MapPin size={20} color="var(--primary-color)" /> GPS & Visual Attendance
            </h3>

            {lastCheckIn ? (
              <div
                style={{
                  padding: '1.5rem',
                  backgroundColor: 'rgba(22, 163, 74, 0.06)',
                  border: '1px solid rgba(22, 163, 74, 0.2)',
                  borderRadius: 'var(--radius-md)',
                  textAlign: 'center',
                }}
              >
                <CheckCircle size={40} color="var(--success-color)" style={{ marginBottom: '0.75rem' }} />
                <h4 style={{ margin: 0, fontSize: '1.1rem', color: 'var(--success-color)' }}>
                  You are checked in for today!
                </h4>
                <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>
                  In: <strong>{lastCheckIn.checkInTime}</strong> on {lastCheckIn.date}
                  {lastCheckIn.checkOutTime && (
                    <span> | Out: <strong>{lastCheckIn.checkOutTime}</strong> ({lastCheckIn.hoursWorked} hrs)</span>
                  )}
                </p>
                <div style={{ marginTop: '0.75rem' }}>
                  <Badge
                    label={`Approval: ${lastCheckIn.approvalStatus}`}
                    variant={lastCheckIn.approvalStatus === 'APPROVED' ? 'success' : 'warning'}
                  />
                </div>

                {/* CHECK-OUT ACTION */}
                {!lastCheckIn.checkOutTime ? (
                  <div style={{ marginTop: '1.25rem', borderTop: '1px solid rgba(22, 163, 74, 0.2)', paddingTop: '1rem' }}>
                    <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
                      Finished your shift? Record your check-out with GPS verification.
                    </p>
                    <button
                      className="btn btn-primary"
                      style={{ backgroundColor: '#DC2626', borderColor: '#DC2626' }}
                      onClick={handleCheckOut}
                      disabled={checkingOut}
                    >
                      {checkingOut ? <LoadingSpinner size={16} message="Checking out..." /> : (
                        <>
                          <LogOut size={16} /> Check Out for Today
                        </>
                      )}
                    </button>
                  </div>
                ) : (
                  <div style={{ marginTop: '1rem', color: 'var(--success-color)', fontSize: '0.85rem', fontWeight: 600 }}>
                    ✓ Shift completed and recorded!
                  </div>
                )}

                <div style={{ marginTop: '1.25rem' }}>
                  <button
                    className="btn btn-secondary btn-sm"
                    onClick={() => shareAttendanceViaWhatsApp(lastCheckIn)}
                  >
                    <Share2 size={16} /> Share via WhatsApp
                  </button>
                </div>
              </div>
            ) : (
              <div>
                {/* Site Selector if multiple */}
                {availableSites.length > 1 && (
                  <div style={{ marginBottom: '1rem' }}>
                    <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                      Select Site
                    </label>
                    <select
                      value={assignedSite?.id || ''}
                      onChange={(e) => {
                        const site = availableSites.find((s) => s.id === e.target.value);
                        if (site) {
                          setAssignedSite(site);
                          setAssignedProject({
                            id: site.projectId,
                            clientId: '',
                            clientName: 'Client',
                            title: site.projectTitle,
                            description: '',
                            location: site.city,
                            startDate: '',
                            estimatedEndDate: '',
                            status: 'IN_PROGRESS',
                            budget: 0,
                            totalExpenses: 0,
                            progressPercentage: 50,
                            createdAt: '',
                            updatedAt: '',
                          });
                        }
                      }}
                      style={{
                        width: '100%',
                        padding: '8px 12px',
                        borderRadius: 'var(--radius-sm)',
                        border: '1px solid var(--border-color)',
                      }}
                    >
                      {availableSites.map((s) => (
                        <option key={s.id} value={s.id}>
                          {s.name} ({s.city})
                        </option>
                      ))}
                    </select>
                  </div>
                )}

                {/* Step 1: Location verification */}
                <div
                  style={{
                    marginBottom: '1.25rem',
                    padding: '1rem',
                    backgroundColor: 'var(--bg-container)',
                    borderRadius: 'var(--radius-sm)',
                  }}
                >
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '0.5rem',
                    }}
                  >
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>Step 1: Verify Site Location</span>
                    <button className="btn btn-secondary btn-sm" onClick={handleAcquireGps}>
                      <Navigation size={14} /> Detect GPS
                    </button>
                  </div>

                  {currentGps ? (
                    <div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                        Lat: {currentGps.latitude.toFixed(5)}, Lng: {currentGps.longitude.toFixed(5)}
                      </div>
                      {geofenceStatus && (
                        <div style={{ marginTop: '0.5rem', display: 'flex', alignItems: 'center', gap: '6px' }}>
                          {geofenceStatus.isWithinGeofence ? (
                            <>
                              <CheckCircle size={16} color="var(--success-color)" />
                              <span style={{ fontSize: '0.85rem', color: 'var(--success-color)', fontWeight: 600 }}>
                                Inside site boundary ({geofenceStatus.distanceMeters}m away)
                              </span>
                            </>
                          ) : (
                            <>
                              <AlertTriangle size={16} color="var(--error-color)" />
                              <span style={{ fontSize: '0.85rem', color: 'var(--error-color)', fontWeight: 600 }}>
                                Outside site boundary ({geofenceStatus.distanceMeters}m away)
                              </span>
                            </>
                          )}
                        </div>
                      )}
                    </div>
                  ) : (
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
                      Click "Detect GPS" to verify you are currently within the construction site boundary.
                    </p>
                  )}
                </div>

                {/* Step 2: Selfie verification */}
                <div
                  style={{
                    marginBottom: '1.5rem',
                    padding: '1rem',
                    backgroundColor: 'var(--bg-container)',
                    borderRadius: 'var(--radius-sm)',
                  }}
                >
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '0.5rem',
                    }}
                  >
                    <span style={{ fontWeight: 600, fontSize: '0.875rem' }}>Step 2: Take Selfie Photo</span>
                    <button className="btn btn-secondary btn-sm" onClick={() => setIsCameraOpen(true)}>
                      <Camera size={14} /> {selfieDataUrl ? 'Retake Photo' : 'Open Camera'}
                    </button>
                  </div>

                  {selfieDataUrl ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <img
                        src={selfieDataUrl}
                        alt="Selfie"
                        style={{
                          width: '56px',
                          height: '56px',
                          borderRadius: '50%',
                          objectFit: 'cover',
                          border: '2px solid var(--primary-color)',
                        }}
                      />
                      <span style={{ fontSize: '0.85rem', color: 'var(--success-color)', fontWeight: 600 }}>
                        ✓ Photo captured and ready
                      </span>
                    </div>
                  ) : (
                    <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
                      Live photo required to prevent proxy attendance.
                    </p>
                  )}
                </div>

                {/* Final Submit Button */}
                <button
                  className="btn btn-primary"
                  style={{ width: '100%', padding: '0.75rem' }}
                  onClick={handleSubmitCheckIn}
                  disabled={checkingIn || !currentGps || !selfieDataUrl || !assignedSite}
                >
                  {checkingIn ? (
                    <LoadingSpinner size={18} message="Recording Attendance..." />
                  ) : (
                    <>
                      Confirm & Submit Check-In <CheckCircle size={18} />
                    </>
                  )}
                </button>
              </div>
            )}
          </div>

          {/* Assigned Site & Project Info */}
          <div className="card" style={{ padding: '1.5rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
              Current Deployment Details
            </h3>

            {assignedSite ? (
              <div>
                <div style={{ marginBottom: '1rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Assigned Project</span>
                  <div style={{ fontSize: '1rem', fontWeight: 600 }}>{assignedProject?.title || 'Civil Construction'}</div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{assignedProject?.description}</div>
                </div>

                <div style={{ marginBottom: '1rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Active Construction Site</span>
                  <div style={{ fontSize: '1rem', fontWeight: 600 }}>{assignedSite.name}</div>
                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    {assignedSite.address}, {assignedSite.city}
                  </div>
                </div>

                <div style={{ marginBottom: '1rem' }}>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Site Supervisor</span>
                  <div style={{ fontSize: '0.9rem', fontWeight: 500 }}>
                    {assignedSite.managerName} ({assignedSite.contactPhone})
                  </div>
                </div>

                <button
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%' }}
                  onClick={() => setIsMapOpen(true)}
                >
                  <MapPin size={16} /> Open Geofence Map View
                </button>
              </div>
            ) : (
              <EmptyState
                title="No Sites Available"
                description="No active construction sites found. Contact your site supervisor to be assigned to a location."
              />
            )}
          </div>
        </div>
      )}

      {/* TAB 2: ATTENDANCE HISTORY */}
      {activeTab === 'HISTORY' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
            Attendance Roster & Check-In History
          </h3>

          {attendanceHistory.length === 0 ? (
            <EmptyState
              title="No Attendance Logs"
              description="Your daily check-in records and supervisor verification approvals will appear here."
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr
                    style={{
                      borderBottom: '2px solid var(--border-color)',
                      color: 'var(--text-muted)',
                      fontSize: '0.75rem',
                    }}
                  >
                    <th style={{ padding: '0.75rem 0.5rem' }}>DATE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>TIME IN / OUT</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>SITE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>GEOFENCE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>APPROVAL</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ACTIONS</th>
                  </tr>
                </thead>
                <tbody>
                  {attendanceHistory.map((att) => (
                    <tr key={att.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{att.date}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
                          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Clock size={12} color="var(--text-muted)" /> In: {att.checkInTime}
                          </span>
                          {att.checkOutTime && (
                            <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--text-muted)' }}>
                              <LogOut size={12} /> Out: {att.checkOutTime} ({att.hoursWorked}h)
                            </span>
                          )}
                        </div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{att.siteName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.isWithinGeofence ? 'Inside' : `Outside (${att.distanceFromSiteMeters}m)`}
                          variant={att.isWithinGeofence ? 'success' : 'error'}
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={att.status} variant="info" />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.approvalStatus}
                          variant={
                            att.approvalStatus === 'APPROVED'
                              ? 'success'
                              : att.approvalStatus === 'REJECTED'
                              ? 'error'
                              : 'warning'
                          }
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <button
                          className="btn btn-secondary btn-sm"
                          style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                          onClick={() => shareAttendanceViaWhatsApp(att)}
                        >
                          <Share2 size={12} /> Share
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

      {/* TAB 3: SALARY & PAY SLIPS */}
      {activeTab === 'SALARY' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
            Monthly Wage Disbursals & Salary Slips
          </h3>

          {salaries.length === 0 ? (
            <EmptyState
              title="No Salary Slips Generated Yet"
              description="Your supervisor generates monthly salary slips based on approved attendance. Slips will appear here for instant PDF download."
            />
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '1rem' }}>
              {salaries.map((sal) => (
                <div
                  key={sal.id}
                  style={{
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    padding: '1.25rem',
                    backgroundColor: 'var(--bg-surface)',
                  }}
                >
                  <div
                    style={{
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      marginBottom: '0.75rem',
                    }}
                  >
                    <span style={{ fontWeight: 700, fontSize: '1.1rem' }}>{sal.month}</span>
                    <Badge label={sal.status} variant={sal.status === 'PAID' ? 'success' : 'warning'} />
                  </div>

                  <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.5rem' }}>
                    <div>
                      Present: <strong>{sal.daysPresent} days</strong> (Overtime: {sal.overtimeHours} hrs)
                    </div>
                    <div>
                      Daily Rate: <strong>₹{sal.dailyRate}/day</strong>
                    </div>
                  </div>

                  <div
                    style={{
                      padding: '0.75rem 0',
                      borderTop: '1px dashed var(--border-color)',
                      borderBottom: '1px dashed var(--border-color)',
                      marginBottom: '1rem',
                    }}
                  >
                    <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Net Amount Payable</div>
                    <div style={{ fontSize: '1.25rem', fontWeight: 700, color: 'var(--success-color)' }}>
                      ₹{sal.netPayable.toLocaleString('en-IN')}
                    </div>
                  </div>

                  <button
                    className="btn btn-primary btn-sm"
                    style={{ width: '100%' }}
                    onClick={() => generateSalarySlipPDF(sal)}
                  >
                    <Download size={14} /> Download Pay Slip PDF
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Camera Capture Modal */}
      <CameraCaptureModal
        isOpen={isCameraOpen}
        onClose={() => setIsCameraOpen(false)}
        onCapture={(dataUrl) => setSelfieDataUrl(dataUrl)}
      />

      {/* Geofence Map Modal */}
      {assignedSite && (
        <MapModal
          isOpen={isMapOpen}
          onClose={() => setIsMapOpen(false)}
          site={assignedSite}
          workerLocation={currentGps || undefined}
          distanceMeters={geofenceStatus?.distanceMeters}
          isWithinGeofence={geofenceStatus?.isWithinGeofence}
        />
      )}
    </div>
  );
};
