import React, { useState, useEffect } from 'react';
import { 
  Building, 
  MapPin, 
  Calendar, 
  FileText, 
  Download, 
  Share2, 
  IndianRupee,
  PlusCircle
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../services/apiClient';
import { 
  ProjectRecord, 
  SiteRecord, 
  ProjectMilestone, 
  DailyProgressReport, 
  PaymentRecord 
} from '../../types/schema';
import { generateInvoicePDF } from '../../utils/pdfGenerator';
import { shareDPRViaWhatsApp, shareInvoiceViaWhatsApp } from '../../utils/whatsappShare';
import { MapModal } from '../../components/common/MapModal';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { EmptyState } from '../../components/common/EmptyState';
import { Badge } from '../../components/common/Badge';
import { ErrorMessage } from '../../components/common/ErrorMessage';

export const ClientPortal: React.FC = () => {
  const { currentUser } = useAuth();
  const [activeTab, setActiveTab] = useState<'OVERVIEW' | 'TIMELINE' | 'DPR' | 'INVOICES'>('OVERVIEW');

  const [projects, setProjects] = useState<ProjectRecord[]>([]);
  const [selectedProject, setSelectedProject] = useState<ProjectRecord | null>(null);
  const [sites, setSites] = useState<SiteRecord[]>([]);
  const [milestones, setMilestones] = useState<ProjectMilestone[]>([]);
  const [dprs, setDprs] = useState<DailyProgressReport[]>([]);
  const [invoices, setInvoices] = useState<PaymentRecord[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isMapOpen, setIsMapOpen] = useState(false);

  // New Project Form
  const [showNewProjectModal, setShowNewProjectModal] = useState(false);
  const [newProjectTitle, setNewProjectTitle] = useState('');
  const [newProjectDesc, setNewProjectDesc] = useState('');
  const [newProjectBudget, setNewProjectBudget] = useState('2500000');
  const [creatingProject, setCreatingProject] = useState(false);

  const loadClientData = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch authenticated client's projects and invoices
      const [myProjects, allSites, myInvoices] = await Promise.all([
        apiClient.projects.getMyProjects().catch(() => []),
        apiClient.sites.getSites().catch(() => []),
        apiClient.invoices.getMyInvoices().catch(() => []),
      ]);

      const mappedProjects: ProjectRecord[] = (myProjects || []).map((p: any) => ({
        id: p.id,
        clientId: p.clientId,
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
      setSites(allSites || []);

      const mappedInvoices: PaymentRecord[] = (myInvoices || []).map((inv: any) => ({
        id: inv.id,
        invoiceNumber: inv.invoiceNumber,
        projectId: inv.projectId,
        projectTitle: inv.projectTitle || 'Project',
        paymentType: 'CLIENT_INVOICE',
        amount: inv.amount,
        taxAmount: inv.taxAmount || 0,
        totalAmount: inv.totalAmount,
        status: inv.status,
        dueDate: inv.dueDate || '',
        description: inv.description || '',
        createdAt: inv.createdAt,
      }));
      setInvoices(mappedInvoices);

      const active = mappedProjects.length > 0 ? mappedProjects[0] : null;
      setSelectedProject(active);

      if (active) {
        await loadProjectSubDetails(active.id);
      }
    } catch (err: any) {
      setError(err.message || 'Failed to load client portal');
    } finally {
      setLoading(false);
    }
  };

  const loadProjectSubDetails = async (projectId: string) => {
    try {
      const [ms, reportList] = await Promise.all([
        apiClient.milestones.getByProject(projectId).catch(() => []),
        apiClient.dpr.getDprs().catch(() => []),
      ]);

      const mappedMilestones: ProjectMilestone[] = (ms || []).map((m: any) => ({
        id: m.id,
        projectId: m.projectId,
        phaseName: m.phaseName,
        description: m.description || '',
        targetDate: m.targetDate || '',
        completionDate: m.completionDate,
        status: m.status || 'TODO',
        orderIndex: m.orderIndex || 0,
      }));
      setMilestones(mappedMilestones);

      const mappedDprs: DailyProgressReport[] = (reportList || [])
        .filter((d: any) => d.projectId === projectId || !d.projectId)
        .map((d: any) => ({
          id: d.id,
          projectId: d.projectId,
          projectTitle: d.projectTitle || 'Project Site',
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
          createdAt: d.createdAt,
        }));
      setDprs(mappedDprs);
    } catch (err) {
      console.error(err);
    }
  };

  useEffect(() => {
    loadClientData();
  }, [currentUser]);

  const handleSelectProject = async (proj: ProjectRecord) => {
    setSelectedProject(proj);
    await loadProjectSubDetails(proj.id);
  };

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreatingProject(true);
    setError(null);
    try {
      try {
        await apiClient.clients.getMyProfile();
      } catch (profileErr: any) {
        if (profileErr.status === 404) {
          await apiClient.clients.createProfile({
            displayName: newProjectTitle.trim() || 'Client Enterprise',
            clientType: 'BUILDER',
          });
        }
      }

      await apiClient.projects.createProject({
        title: newProjectTitle.trim(),
        description: newProjectDesc.trim(),
        location: 'Site Location',
        budget: Number(newProjectBudget) || 1000000,
      });
      setShowNewProjectModal(false);
      setNewProjectTitle('');
      setNewProjectDesc('');
      await loadClientData();
    } catch (err: any) {
      setError(err.message || 'Failed to create project');
    } finally {
      setCreatingProject(false);
    }
  };

  const activeSite = sites.find((s) => s.projectId === selectedProject?.id) || (sites.length > 0 ? sites[0] : null);

  if (loading) {
    return <LoadingSpinner message="Loading client portal..." />;
  }

  return (
    <div>
      {/* Top Banner with Project Switcher */}
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
          borderLeft: '4px solid var(--accent-amber)',
          gap: '1rem',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <div
            style={{
              padding: '0.75rem',
              borderRadius: '50%',
              backgroundColor: 'rgba(217, 119, 6, 0.1)',
              color: 'var(--accent-amber)',
            }}
          >
            <Building size={28} />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
              {selectedProject?.title || 'Client Construction Dashboard'}
            </h2>
            <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', marginTop: '0.25rem' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Location: {selectedProject?.location || 'India'}
              </span>
              {selectedProject && (
                <Badge
                  label={selectedProject.status}
                  variant={
                    selectedProject.status === 'COMPLETED'
                      ? 'success'
                      : selectedProject.status === 'IN_PROGRESS'
                      ? 'info'
                      : 'warning'
                  }
                />
              )}
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          {/* Project Selector dropdown if multiple */}
          {projects.length > 1 && (
            <div>
              <select
                value={selectedProject?.id}
                onChange={(e) => {
                  const found = projects.find((p) => p.id === e.target.value);
                  if (found) handleSelectProject(found);
                }}
                style={{ padding: '0.45rem 0.75rem', fontSize: '0.875rem', borderRadius: 'var(--radius-sm)' }}
              >
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title}
                  </option>
                ))}
              </select>
            </div>
          )}

          <button className="btn btn-primary btn-sm" onClick={() => setShowNewProjectModal(true)}>
            <PlusCircle size={14} /> New Project
          </button>
        </div>
      </div>

      {error && <ErrorMessage message={error} onRetry={loadClientData} />}

      {projects.length === 0 ? (
        <div className="card" style={{ padding: '3rem 2rem', textAlign: 'center' }}>
          <Building size={48} color="var(--primary-color)" style={{ marginBottom: '1rem' }} />
          <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '0.5rem' }}>No Projects Initiated</h3>
          <p style={{ color: 'var(--text-secondary)', maxWidth: '440px', margin: '0 auto 1.5rem auto' }}>
            You haven't commissioned any construction projects yet. Start a new project to track engineering execution, daily reports, and milestone payments.
          </p>
          <button className="btn btn-primary" onClick={() => setShowNewProjectModal(true)}>
            <PlusCircle size={16} /> Commission First Project
          </button>
        </div>
      ) : (
        <>
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
              className={`btn ${activeTab === 'OVERVIEW' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              onClick={() => setActiveTab('OVERVIEW')}
            >
              <Building size={16} /> Project Overview
            </button>
            <button
              className={`btn ${activeTab === 'TIMELINE' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              onClick={() => setActiveTab('TIMELINE')}
            >
              <Calendar size={16} /> Construction Timeline ({milestones.length})
            </button>
            <button
              className={`btn ${activeTab === 'DPR' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              onClick={() => setActiveTab('DPR')}
            >
              <FileText size={16} /> Daily Progress Reports ({dprs.length})
            </button>
            <button
              className={`btn ${activeTab === 'INVOICES' ? 'btn-primary' : 'btn-secondary'} btn-sm`}
              onClick={() => setActiveTab('INVOICES')}
            >
              <IndianRupee size={16} /> Invoices & Bills ({invoices.length})
            </button>
          </div>

          {/* TAB 1: OVERVIEW */}
          {activeTab === 'OVERVIEW' && (
            <div>
              {/* Key Metrics */}
              <div
                style={{
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                  gap: '1rem',
                  marginBottom: '1.5rem',
                }}
              >
                <div className="card" style={{ padding: '1.25rem' }}>
                  <span
                    style={{
                      fontSize: '0.75rem',
                      color: 'var(--text-muted)',
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px',
                    }}
                  >
                    Overall Progress
                  </span>
                  <div
                    style={{
                      fontSize: '1.75rem',
                      fontWeight: 700,
                      color: 'var(--primary-color)',
                      margin: '0.25rem 0',
                    }}
                  >
                    {selectedProject?.progressPercentage || 0}%
                  </div>
                  <div
                    style={{
                      height: '8px',
                      backgroundColor: 'var(--bg-container)',
                      borderRadius: '4px',
                      overflow: 'hidden',
                    }}
                  >
                    <div
                      style={{
                        height: '100%',
                        width: `${selectedProject?.progressPercentage || 0}%`,
                        backgroundColor: 'var(--primary-color)',
                        transition: 'width 0.3s',
                      }}
                    />
                  </div>
                </div>

                <div className="card" style={{ padding: '1.25rem' }}>
                  <span
                    style={{
                      fontSize: '0.75rem',
                      color: 'var(--text-muted)',
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px',
                    }}
                  >
                    Approved Project Budget
                  </span>
                  <div
                    style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)', margin: '0.25rem 0' }}
                  >
                    ₹{(selectedProject?.budget || 0).toLocaleString('en-IN')}
                  </div>
                  <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    Target Completion: {selectedProject?.estimatedEndDate || '2026-12-31'}
                  </span>
                </div>

                <div className="card" style={{ padding: '1.25rem' }}>
                  <span
                    style={{
                      fontSize: '0.75rem',
                      color: 'var(--text-muted)',
                      textTransform: 'uppercase',
                      letterSpacing: '0.5px',
                    }}
                  >
                    Active Site Tracking
                  </span>
                  <div
                    style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)', margin: '0.35rem 0' }}
                  >
                    {activeSite?.name || 'Main Worksite'}
                  </div>
                  {activeSite && (
                    <button
                      className="btn btn-secondary btn-sm"
                      style={{ padding: '2px 8px', fontSize: '0.75rem' }}
                      onClick={() => setIsMapOpen(true)}
                    >
                      <MapPin size={12} /> View Geofence Map
                    </button>
                  )}
                </div>
              </div>

              {/* Project Details */}
              <div className="card" style={{ padding: '1.5rem', marginBottom: '1.5rem' }}>
                <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '0.75rem' }}>
                  Project Scope & Architecture
                </h3>
                <p style={{ color: 'var(--text-secondary)', lineHeight: 1.6, margin: 0 }}>
                  {selectedProject?.description ||
                    'Commercial / Residential construction project under strict engineering supervision and quality milestones.'}
                </p>
              </div>
            </div>
          )}

          {/* TAB 2: TIMELINE / MILESTONES */}
          {activeTab === 'TIMELINE' && (
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1.25rem' }}>
                Construction Execution Stages & Milestone Timeline
              </h3>

              {milestones.length === 0 ? (
                <EmptyState
                  title="No Milestones Defined"
                  description="Project milestone stages will appear here as engineering phases progress."
                />
              ) : (
                <div style={{ position: 'relative', paddingLeft: '1.5rem' }}>
                  <div
                    style={{
                      position: 'absolute',
                      left: '7px',
                      top: '12px',
                      bottom: '12px',
                      width: '2px',
                      backgroundColor: 'var(--border-color)',
                    }}
                  />

                  {milestones.map((ms) => (
                    <div key={ms.id} style={{ position: 'relative', marginBottom: '1.75rem' }}>
                      <div
                        style={{
                          position: 'absolute',
                          left: '-1.5rem',
                          top: '4px',
                          width: '16px',
                          height: '16px',
                          borderRadius: '50%',
                          backgroundColor:
                            ms.status === 'COMPLETED'
                              ? 'var(--success-color)'
                              : ms.status === 'IN_PROGRESS'
                              ? 'var(--primary-color)'
                              : 'var(--border-color)',
                          border: '3px solid var(--bg-surface)',
                        }}
                      />

                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <h4 style={{ margin: 0, fontSize: '1rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                          {ms.phaseName}
                        </h4>
                        <Badge
                          label={ms.status.replace('_', ' ')}
                          variant={
                            ms.status === 'COMPLETED'
                              ? 'success'
                              : ms.status === 'IN_PROGRESS'
                              ? 'info'
                              : 'neutral'
                          }
                        />
                      </div>

                      <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '0.25rem 0' }}>
                        {ms.description}
                      </p>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        Target Completion Date: {ms.targetDate}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 3: DAILY PROGRESS REPORTS (DPR) */}
          {activeTab === 'DPR' && (
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
                Daily Progress Reports (DPR) & Site Logs
              </h3>

              {dprs.length === 0 ? (
                <EmptyState
                  title="No Progress Reports Logged Yet"
                  description="Daily engineering progress reports submitted by the site supervisor will appear here with photos, weather, and labor deployment."
                />
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
                  {dprs.map((dpr) => (
                    <div
                      key={dpr.id}
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
                        <div>
                          <span style={{ fontWeight: 700, fontSize: '1.05rem', color: 'var(--text-primary)' }}>
                            Report for {dpr.reportDate}
                          </span>
                          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                            Site: {dpr.siteName} | Supervisor: {dpr.reportedBy} | Weather: {dpr.weatherCondition}
                          </div>
                        </div>
                        <button className="btn btn-secondary btn-sm" onClick={() => shareDPRViaWhatsApp(dpr)}>
                          <Share2 size={14} /> WhatsApp Share
                        </button>
                      </div>

                      <div style={{ marginBottom: '0.75rem' }}>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '2px' }}>
                          Labor Strength on Site:
                        </div>
                        <span style={{ fontSize: '0.875rem', fontWeight: 600 }}>
                          {dpr.skilledLaborCount} Skilled Tradespeople + {dpr.unskilledLaborCount} Helpers (
                          {dpr.skilledLaborCount + dpr.unskilledLaborCount} Total)
                        </span>
                      </div>

                      <div style={{ marginBottom: '0.75rem' }}>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginBottom: '2px' }}>
                          Work Accomplished:
                        </div>
                        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                          {dpr.tasksCompleted}
                        </p>
                      </div>

                      {dpr.delaysOrIssues && (
                        <div
                          style={{
                            padding: '0.5rem 0.75rem',
                            backgroundColor: 'rgba(217, 119, 6, 0.08)',
                            borderRadius: 'var(--radius-sm)',
                            fontSize: '0.85rem',
                            color: 'var(--warning-color)',
                          }}
                        >
                          <strong>Hindrances / Delays:</strong> {dpr.delaysOrIssues}
                        </div>
                      )}

                      {dpr.photos && dpr.photos.length > 0 && (
                        <div style={{ marginTop: '0.75rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                          {dpr.photos.map((url, pIdx) => (
                            <img
                              key={pIdx}
                              src={url}
                              alt="Site Progress"
                              style={{
                                width: '80px',
                                height: '80px',
                                borderRadius: 'var(--radius-sm)',
                                objectFit: 'cover',
                                border: '1px solid var(--border-color)',
                              }}
                            />
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* TAB 4: INVOICES & BILLS */}
          {activeTab === 'INVOICES' && (
            <div className="card" style={{ padding: '1.5rem' }}>
              <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>
                Tax Invoices & Milestone Billing Records
              </h3>

              {invoices.length === 0 ? (
                <EmptyState
                  title="No Invoices Issued"
                  description="Milestone billing invoices will appear here for instant review and GST PDF download."
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
                        <th style={{ padding: '0.75rem 0.5rem' }}>INVOICE #</th>
                        <th style={{ padding: '0.75rem 0.5rem' }}>DATE</th>
                        <th style={{ padding: '0.75rem 0.5rem' }}>DESCRIPTION</th>
                        <th style={{ padding: '0.75rem 0.5rem' }}>TOTAL (INR)</th>
                        <th style={{ padding: '0.75rem 0.5rem' }}>STATUS</th>
                        <th style={{ padding: '0.75rem 0.5rem' }}>ACTIONS</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoices.map((inv) => (
                        <tr
                          key={inv.id}
                          style={{ borderBottom: '1px solid var(--border-color)', fontSize: '0.85rem' }}
                        >
                          <td style={{ padding: '0.75rem 0.5rem', fontWeight: 600 }}>{inv.invoiceNumber}</td>
                          <td style={{ padding: '0.75rem 0.5rem' }}>{inv.createdAt.split('T')[0]}</td>
                          <td style={{ padding: '0.75rem 0.5rem' }}>{inv.description}</td>
                          <td style={{ padding: '0.75rem 0.5rem', fontWeight: 700 }}>
                            ₹{inv.totalAmount.toLocaleString('en-IN')}
                          </td>
                          <td style={{ padding: '0.75rem 0.5rem' }}>
                            <Badge
                              label={inv.status}
                              variant={
                                inv.status === 'PAID'
                                  ? 'success'
                                  : inv.status === 'OVERDUE'
                                  ? 'error'
                                  : 'warning'
                              }
                            />
                          </td>
                          <td style={{ padding: '0.75rem 0.5rem' }}>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                              <button
                                className="btn btn-primary btn-sm"
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
        </>
      )}

      {/* New Project Modal */}
      {showNewProjectModal && (
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
          <div
            className="card"
            style={{ width: '100%', maxWidth: '480px', padding: '1.5rem', backgroundColor: 'var(--bg-surface)' }}
          >
            <h3 style={{ fontSize: '1.15rem', fontWeight: 700, margin: '0 0 1rem 0' }}>Initiate New Construction Project</h3>
            <form onSubmit={handleCreateProject} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Project Title
                </label>
                <input
                  type="text"
                  placeholder="e.g. Commercial Complex Phase 2"
                  value={newProjectTitle}
                  onChange={(e) => setNewProjectTitle(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-color)',
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Project Scope & Description
                </label>
                <textarea
                  placeholder="Describe building specifications, area, floors..."
                  value={newProjectDesc}
                  onChange={(e) => setNewProjectDesc(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-color)',
                    minHeight: '80px',
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.85rem', fontWeight: 600, marginBottom: '4px' }}>
                  Estimated Budget (INR)
                </label>
                <input
                  type="number"
                  placeholder="2500000"
                  value={newProjectBudget}
                  onChange={(e) => setNewProjectBudget(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '8px 12px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid var(--border-color)',
                    boxSizing: 'border-box',
                  }}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px', marginTop: '0.5rem' }}>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm"
                  onClick={() => setShowNewProjectModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" disabled={creatingProject} className="btn btn-primary btn-sm">
                  {creatingProject ? <LoadingSpinner size={14} /> : 'Create Project'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Site Geofence Map */}
      {activeSite && <MapModal isOpen={isMapOpen} onClose={() => setIsMapOpen(false)} site={activeSite} />}
    </div>
  );
};
