import React, { useState, useEffect } from 'react';
import { 
  Briefcase, 
  Users, 
  MapPin, 
  Calendar, 
  FileText, 
  PlusCircle, 
  CheckCircle, 
  Building, 
  Clock, 
  HardHat,
  Search,
  Layers
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/apiClient';
import { 
  WorkerRecord, 
  SiteRecord, 
  ProjectRecord, 
  AttendanceRecord, 
  DailyProgressReport 
} from '../../types/schema';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Badge } from '../../components/common/Badge';
import { ErrorMessage } from '../../components/common/ErrorMessage';
import { MapModal } from '../../components/common/MapModal';

export const ContractorPortal: React.FC = () => {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<'WORKFORCE' | 'SITES' | 'ATTENDANCE' | 'DPR'>('WORKFORCE');

  const [contractorProfile, setContractorProfile] = useState<any>(null);
  const [associatedWorkers, setAssociatedWorkers] = useState<any[]>([]);
  const [allWorkers, setAllWorkers] = useState<any[]>([]);
  const [projects, setProjects] = useState<ProjectRecord[]>([]);
  const [sites, setSites] = useState<SiteRecord[]>([]);
  const [attendanceRecords, setAttendanceRecords] = useState<AttendanceRecord[]>([]);
  const [dprs, setDprs] = useState<DailyProgressReport[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Modals & Forms
  const [showAssociateModal, setShowAssociateModal] = useState(false);
  const [selectedWorkerId, setSelectedWorkerId] = useState('');
  const [workerNotes, setWorkerNotes] = useState('');
  const [associating, setAssociating] = useState(false);

  // New DPR Form Modal
  const [showDprModal, setShowDprModal] = useState(false);
  const [dprSiteId, setDprSiteId] = useState('');
  const [dprWeather, setDprWeather] = useState('Sunny');
  const [dprSkilled, setDprSkilled] = useState(5);
  const [dprUnskilled, setDprUnskilled] = useState(8);
  const [dprTasks, setDprTasks] = useState('');
  const [dprDelays, setDprDelays] = useState('');
  const [submittingDpr, setSubmittingDpr] = useState(false);

  // Map Modal
  const [activeSiteForMap, setActiveSiteForMap] = useState<SiteRecord | null>(null);

  // Profile setup modal if no contractor profile exists
  const [showProfileSetup, setShowProfileSetup] = useState(false);
  const [companyName, setCompanyName] = useState('Apex Civil Contractors');
  const [licenseNumber, setLicenseNumber] = useState('LIC-2026-CC789');
  const [gstNumber, setGstNumber] = useState('07AAAAA0000A1Z5');
  const [savingProfile, setSavingProfile] = useState(false);

  const loadContractorData = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch Contractor Profile
      let profile: any = null;
      try {
        profile = await apiClient.contractors.getMyProfile();
        setContractorProfile(profile);
      } catch (err: any) {
        if (err.status === 404) {
          setShowProfileSetup(true);
        }
      }

      // 2. Load associated workers, sites, projects, attendance, and DPRs
      const [assoc, allW, projList, siteList, attList, dprList] = await Promise.all([
        apiClient.contractors.getAssociatedWorkers().catch(() => []),
        apiClient.workers.getAllWorkers().catch(() => []),
        apiClient.projects.getAllProjects().catch(() => []),
        apiClient.sites.getSites().catch(() => []),
        apiClient.attendance.getAllAttendance().catch(() => []),
        apiClient.dpr.getDprs().catch(() => []),
      ]);

      setAssociatedWorkers(assoc || []);
      setAllWorkers(allW || []);

      const mappedProjects: ProjectRecord[] = (projList || []).map((p: any) => ({
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
      setProjects(mappedProjects);

      const mappedSites: SiteRecord[] = (siteList || []).map((s: any) => ({
        id: s.id,
        projectId: s.projectId,
        projectTitle: s.projectTitle || 'Project Site',
        name: s.name,
        address: s.address || '',
        city: s.city || '',
        state: s.state || '',
        latitude: s.latitude,
        longitude: s.longitude,
        geofenceRadiusMeters: s.geofenceRadiusMeters || 150,
        managerName: s.managerName || 'Engineer',
        contactPhone: s.contactPhone || '',
        createdAt: s.createdAt || '',
      }));
      setSites(mappedSites);

      const mappedAttendance: AttendanceRecord[] = (attList || []).map((h: any) => ({
        id: h.id,
        workerId: h.workerId,
        workerName: h.workerName,
        workerTrade: h.workerTrade || 'Tradesperson',
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
        hoursWorked: h.hoursWorked,
        createdAt: h.createdAt,
      }));
      setAttendanceRecords(mappedAttendance);

      const mappedDprs: DailyProgressReport[] = (dprList || []).map((d: any) => ({
        id: d.id,
        projectId: d.projectId,
        projectTitle: d.projectTitle || 'Project Site',
        siteId: d.siteId,
        siteName: d.siteName,
        reportDate: d.reportDate,
        reportedBy: d.reportedBy || 'Contractor',
        weatherCondition: d.weatherCondition || 'Sunny',
        skilledLaborCount: d.skilledLaborCount || 0,
        unskilledLaborCount: d.unskilledLaborCount || 0,
        tasksCompleted: d.tasksCompleted || '',
        delaysOrIssues: d.delaysOrIssues,
        safetyObservations: d.safetyObservations,
        photos: d.photos || [],
        status: d.status || 'SUBMITTED',
        createdAt: d.createdAt,
      }));
      setDprs(mappedDprs);
    } catch (err: any) {
      setError(err.message || 'Failed to load contractor portal');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContractorData();
  }, [currentUser]);

  const handleCreateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setSavingProfile(true);
    setError(null);
    try {
      const resp = await apiClient.contractors.createProfile({
        companyName: companyName.trim(),
        licenseNumber: licenseNumber.trim(),
        gstNumber: gstNumber.trim(),
      });
      setContractorProfile(resp);
      setShowProfileSetup(false);
      setSuccessMsg('Contractor enterprise profile created!');
      await loadContractorData();
    } catch (err: any) {
      setError(err.message || 'Failed to save contractor profile');
    } finally {
      setSavingProfile(false);
    }
  };

  const handleAssociateWorker = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedWorkerId) return;
    setAssociating(true);
    setError(null);
    try {
      const todayStr = new Date().toISOString().split('T')[0];
      await apiClient.contractors.associateWorker(selectedWorkerId, todayStr, undefined, workerNotes);
      setShowAssociateModal(false);
      setSelectedWorkerId('');
      setWorkerNotes('');
      setSuccessMsg('Worker associated to contractor workforce roster!');
      await loadContractorData();
    } catch (err: any) {
      setError(err.message || 'Failed to associate worker');
    } finally {
      setAssociating(false);
    }
  };

  const handleCreateDpr = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!dprSiteId) {
      setError('Please select a site');
      return;
    }
    setSubmittingDpr(true);
    setError(null);
    try {
      const selectedSite = sites.find((s) => s.id === dprSiteId);
      const todayStr = new Date().toISOString().split('T')[0];
      await apiClient.dpr.create({
        projectId: selectedSite?.projectId || '',
        projectTitle: selectedSite?.projectTitle,
        siteId: dprSiteId,
        siteName: selectedSite?.name,
        reportDate: todayStr,
        reportedBy: contractorProfile?.companyName || 'Contractor Team',
        weatherCondition: dprWeather,
        skilledLaborCount: Number(dprSkilled),
        unskilledLaborCount: Number(dprUnskilled),
        tasksCompleted: dprTasks,
        delaysOrIssues: dprDelays,
      });

      setShowDprModal(false);
      setDprTasks('');
      setDprDelays('');
      setSuccessMsg('Daily Progress Report submitted to site engineers!');
      await loadContractorData();
    } catch (err: any) {
      setError(err.message || 'Failed to submit DPR');
    } finally {
      setSubmittingDpr(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading Contractor Operations Workspace..." />;
  }

  return (
    <div>
      {/* Top Banner */}
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
          borderLeft: '4px solid #4F46E5',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div
            style={{
              padding: '0.75rem',
              borderRadius: '50%',
              backgroundColor: 'rgba(79, 70, 229, 0.1)',
              color: '#4F46E5',
            }}
          >
            <Briefcase size={28} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
              {contractorProfile?.companyName || 'Contractor Operations Command'}
            </h2>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginTop: '0.25rem' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                License: {contractorProfile?.licenseNumber || 'Verified Contractor'}
              </span>
              <Badge label={`${associatedWorkers.length} Workers Deployed`} variant="info" />
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button className="btn btn-secondary btn-sm" onClick={() => setShowAssociateModal(true)}>
            <Users size={14} /> Associate Worker
          </button>
          <button className="btn btn-primary btn-sm" onClick={() => setShowDprModal(true)}>
            <PlusCircle size={14} /> Submit Daily Report
          </button>
        </div>
      </div>

      {error && <ErrorMessage message={error} onRetry={loadContractorData} />}
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
          className={`btn ${activeTab === 'WORKFORCE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('WORKFORCE')}
        >
          <Users size={16} /> Workforce Roster ({associatedWorkers.length})
        </button>
        <button
          className={`btn ${activeTab === 'SITES' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('SITES')}
        >
          <MapPin size={16} /> Active Sites ({sites.length})
        </button>
        <button
          className={`btn ${activeTab === 'ATTENDANCE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('ATTENDANCE')}
        >
          <Clock size={16} /> Site Attendance Oversight ({attendanceRecords.length})
        </button>
        <button
          className={`btn ${activeTab === 'DPR' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
          onClick={() => setActiveTab('DPR')}
        >
          <FileText size={16} /> Daily Progress Reports ({dprs.length})
        </button>
      </div>

      {/* TAB 1: WORKFORCE ROSTER */}
      {activeTab === 'WORKFORCE' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Associated Workforce & Deployed Labor
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setShowAssociateModal(true)}>
              <PlusCircle size={14} /> Add Worker to Roster
            </button>
          </div>

          {associatedWorkers.length === 0 ? (
            <EmptyState
              title="No Workers Associated Yet"
              description="Click 'Add Worker to Roster' to recruit registered tradespeople into your contractor pool."
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>WORKER ID</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>ENGAGEMENT START</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>NOTES / TRADE</th>
                  </tr>
                </thead>
                <tbody>
                  {associatedWorkers.map((w: any) => (
                    <tr key={w.id || w.workerProfileId} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <HardHat size={16} color="var(--primary-color)" />
                          <span>{w.workerProfileId}</span>
                        </div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge label={w.status || 'ACTIVE'} variant="success" />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{w.startsOn || 'Today'}</td>
                      <td style={{ padding: '0.75rem 0.5rem', color: 'var(--text-secondary)' }}>
                        {w.notes || 'Associated contractor workforce member'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: ACTIVE SITES */}
      {activeTab === 'SITES' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '1.25rem' }}>
          {sites.map((site) => (
            <div
              key={site.id}
              className="card"
              style={{ padding: '1.5rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}
            >
              <div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
                  <h4 style={{ margin: 0, fontSize: '1.1rem', fontWeight: 600 }}>{site.name}</h4>
                  <Badge label={`${site.geofenceRadiusMeters}m Geofence`} variant="info" />
                </div>
                <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '0.75rem' }}>
                  {site.address}, {site.city}, {site.state}
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                  Project: <strong>{site.projectTitle}</strong>
                </div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Engineer: {site.managerName} ({site.contactPhone})
                </div>
              </div>

              <div style={{ marginTop: '1.25rem', display: 'flex', gap: '0.5rem' }}>
                <button
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%' }}
                  onClick={() => setActiveSiteForMap(site)}
                >
                  <MapPin size={14} /> Open Site Geofence Map
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* TAB 3: ATTENDANCE OVERSIGHT */}
      {activeTab === 'ATTENDANCE' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
            Site Workforce Attendance Logs
          </h3>

          {attendanceRecords.length === 0 ? (
            <EmptyState
              title="No Attendance Logs"
              description="Attendance records recorded by workers at your assigned sites will be listed here."
            />
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border-color)', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                    <th style={{ padding: '0.75rem 0.5rem' }}>DATE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>WORKER</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>SITE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>CHECK IN / OUT</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>GEOFENCE</th>
                    <th style={{ padding: '0.75rem 0.5rem' }}>APPROVAL</th>
                  </tr>
                </thead>
                <tbody>
                  {attendanceRecords.map((att) => (
                    <tr key={att.id} style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}>
                      <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{att.date}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <div>{att.workerName}</div>
                        <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{att.workerTrade}</div>
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>{att.siteName}</td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        In: {att.checkInTime}
                        {att.checkOutTime && ` | Out: ${att.checkOutTime}`}
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.isWithinGeofence ? 'Inside' : `Outside (${att.distanceFromSiteMeters}m)`}
                          variant={att.isWithinGeofence ? 'success' : 'error'}
                        />
                      </td>
                      <td style={{ padding: '0.75rem 0.5rem' }}>
                        <Badge
                          label={att.approvalStatus}
                          variant={att.approvalStatus === 'APPROVED' ? 'success' : 'warning'}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* TAB 4: DAILY PROGRESS REPORTS */}
      {activeTab === 'DPR' && (
        <div className="card" style={{ padding: '1.5rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
            <h3 style={{ fontSize: '1.125rem', fontWeight: 600, margin: 0 }}>
              Daily Progress Reports (DPR)
            </h3>
            <button className="btn btn-primary btn-sm" onClick={() => setShowDprModal(true)}>
              <PlusCircle size={14} /> Submit New DPR
            </button>
          </div>

          {dprs.length === 0 ? (
            <EmptyState
              title="No Daily Progress Reports"
              description="Submit daily progress reports on work progress, labor count, and hindrances to keep site engineers updated."
            />
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {dprs.map((dpr) => (
                <div
                  key={dpr.id}
                  style={{
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-sm)',
                    padding: '1rem',
                    backgroundColor: 'var(--bg-surface)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                    <span style={{ fontWeight: 700 }}>
                      {dpr.siteName} — {dpr.reportDate}
                    </span>
                    <Badge label={dpr.status} variant={dpr.status === 'APPROVED' ? 'success' : 'info'} />
                  </div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '0.5rem' }}>
                    Labor: {dpr.skilledLaborCount} skilled + {dpr.unskilledLaborCount} helpers | Weather: {dpr.weatherCondition}
                  </div>
                  <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                    {dpr.tasksCompleted}
                  </p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* Modal: Associate Worker */}
      {showAssociateModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div className="card" style={{ width: '100%', maxWidth: '440px', padding: '1.5rem', backgroundColor: 'var(--bg-surface)' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, margin: '0 0 1rem 0' }}>Associate Worker to Team</h3>
            <form onSubmit={handleAssociateWorker} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Select Worker from Pool
                </label>
                <select
                  value={selectedWorkerId}
                  onChange={(e) => setSelectedWorkerId(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}
                  required
                >
                  <option value="">-- Choose Worker --</option>
                  {allWorkers.map((w: any) => (
                    <option key={w.id} value={w.id}>
                      {w.displayName || 'Worker'} ({w.location || 'Worker'})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Trade Notes / Scope
                </label>
                <input
                  type="text"
                  placeholder="e.g. Masonry for Block A"
                  value={workerNotes}
                  onChange={(e) => setWorkerNotes(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '0.5rem' }}>
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowAssociateModal(false)}>
                  Cancel
                </button>
                <button type="submit" disabled={associating} className="btn btn-primary btn-sm">
                  {associating ? <LoadingSpinner size={14} /> : 'Confirm Association'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Submit DPR */}
      {showDprModal && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div className="card" style={{ width: '100%', maxWidth: '480px', padding: '1.5rem', backgroundColor: 'var(--bg-surface)' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, margin: '0 0 1rem 0' }}>Submit Daily Progress Report</h3>
            <form onSubmit={handleCreateDpr} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Construction Site
                </label>
                <select
                  value={dprSiteId}
                  onChange={(e) => setDprSiteId(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}
                  required
                >
                  <option value="">-- Choose Site --</option>
                  {sites.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.name} ({s.city})
                    </option>
                  ))}
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                    Skilled Count
                  </label>
                  <input
                    type="number"
                    value={dprSkilled}
                    onChange={(e) => setDprSkilled(Number(e.target.value))}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                    Unskilled Count
                  </label>
                  <input
                    type="number"
                    value={dprUnskilled}
                    onChange={(e) => setDprUnskilled(Number(e.target.value))}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                  />
                </div>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Work Accomplished Today
                </label>
                <textarea
                  placeholder="Describe tasks completed, square footage, columns poured..."
                  value={dprTasks}
                  onChange={(e) => setDprTasks(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', minHeight: '70px', boxSizing: 'border-box' }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Delays or Hindrances (Optional)
                </label>
                <input
                  type="text"
                  placeholder="e.g. Rain in afternoon, material transit delayed"
                  value={dprDelays}
                  onChange={(e) => setDprDelays(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '0.5rem' }}>
                <button type="button" className="btn btn-secondary btn-sm" onClick={() => setShowDprModal(false)}>
                  Cancel
                </button>
                <button type="submit" disabled={submittingDpr} className="btn btn-primary btn-sm">
                  {submittingDpr ? <LoadingSpinner size={14} /> : 'Submit DPR'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Profile Setup Modal */}
      {showProfileSetup && (
        <div
          style={{
            position: 'fixed',
            inset: 0,
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div className="card" style={{ width: '100%', maxWidth: '440px', padding: '1.5rem', backgroundColor: 'var(--bg-surface)' }}>
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, margin: '0 0 1rem 0' }}>Setup Contractor Profile</h3>
            <form onSubmit={handleCreateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Company Name
                </label>
                <input
                  type="text"
                  value={companyName}
                  onChange={(e) => setCompanyName(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  License Number
                </label>
                <input
                  type="text"
                  value={licenseNumber}
                  onChange={(e) => setLicenseNumber(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', boxSizing: 'border-box' }}
                  required
                />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '0.5rem' }}>
                <button type="submit" disabled={savingProfile} className="btn btn-primary btn-sm">
                  {savingProfile ? <LoadingSpinner size={14} /> : 'Save Profile'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Map Modal */}
      {activeSiteForMap && (
        <MapModal isOpen={!!activeSiteForMap} onClose={() => setActiveSiteForMap(null)} site={activeSiteForMap} />
      )}
    </div>
  );
};
