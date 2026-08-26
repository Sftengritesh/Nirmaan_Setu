import React, { useState, useEffect } from 'react';
import { clientService, ClientProfileResponse } from '../../services/clientService';
import { projectService, ProjectResponse } from '../../services/projectService';
import { requirementService, WorkforceRequirementResponse } from '../../services/requirementService';
import { bookingService, BookingResponse } from '../../services/bookingService';
import { Building, Plus, MapPin, Calendar, Layers, Send, XCircle } from 'lucide-react';

export const ClientPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'PROFILE' | 'PROJECTS'>('PROJECTS');
  const [profile, setProfile] = useState<ClientProfileResponse | null>(null);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [selectedProjectId, setSelectedProjectId] = useState<string | null>(null);
  const [requirements, setRequirements] = useState<WorkforceRequirementResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Client Profile Form
  const [clientType, setClientType] = useState('BUILDER');
  const [displayName, setDisplayName] = useState('');

  // Project Form
  const [projectTitle, setProjectTitle] = useState('');
  const [projectLocation, setProjectLocation] = useState('');
  const [projectDescription, setProjectDescription] = useState('');
  const [projectStartDate, setProjectStartDate] = useState(new Date().toISOString().split('T')[0]);

  // Requirement Form
  const [reqLocation, setReqLocation] = useState('');
  const [reqStartDate, setReqStartDate] = useState(new Date().toISOString().split('T')[0]);
  const [reqDuration, setReqDuration] = useState<number>(14);
  const [reqWorkerType, setReqWorkerType] = useState('SKILLED_WORKER');
  const [reqSkillId, setReqSkillId] = useState('a1b2c3d4-e89b-12d3-a456-426614174000');
  const [reqQuantity, setReqQuantity] = useState<number>(5);
  const [reqCompensationType, setReqCompensationType] = useState<'DAILY' | 'BUDGET'>('DAILY');
  const [reqDailyRate, setReqDailyRate] = useState<number | ''>(900);
  const [reqBudgetAmount, setReqBudgetAmount] = useState<number | ''>('');
  const [showReqModal, setShowReqModal] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const c = await clientService.getProfile();
      setProfile(c);
      setClientType(c.clientType);
      setDisplayName(c.displayName);

      const pList = await projectService.getMyProjects();
      setProjects(pList);
      if (pList.length > 0 && !selectedProjectId) {
        setSelectedProjectId(pList[0].id);
      }
    } catch (err: any) {
      if (err.code !== 'NOT_FOUND') setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const loadRequirements = async (pId: string) => {
    try {
      const list = await requirementService.getRequirementsByProject(pId);
      setRequirements(list);
    } catch (err: any) {
      console.error(err);
    }
  };

  useEffect(() => {
    if (selectedProjectId) {
      loadRequirements(selectedProjectId);
    }
  }, [selectedProjectId]);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    const req = { clientType, displayName };
    try {
      if (profile) {
        const updated = await clientService.updateProfile(req);
        setProfile(updated);
        setSuccess('Client profile updated.');
      } else {
        const created = await clientService.createProfile(req);
        setProfile(created);
        setSuccess('Client profile created.');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to save client profile');
    }
  };

  const handleCreateProject = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!projectTitle || !projectLocation) return;
    setError(null);
    try {
      const p = await projectService.createProject({
        title: projectTitle,
        location: projectLocation,
        description: projectDescription,
        startDate: projectStartDate,
      });
      setProjectTitle('');
      setProjectLocation('');
      setProjectDescription('');
      const updated = await projectService.getMyProjects();
      setProjects(updated);
      setSelectedProjectId(p.id);
      setSuccess('Project site created successfully.');
    } catch (err: any) {
      setError(err.message || 'Failed to create project');
    }
  };

  const handleCreateRequirement = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProjectId) return;
    setError(null);

    // Enforce XOR dailyRate vs budgetAmount
    const dailyRateVal = reqCompensationType === 'DAILY' && reqDailyRate ? Number(reqDailyRate) : undefined;
    const budgetAmountVal = reqCompensationType === 'BUDGET' && reqBudgetAmount ? Number(reqBudgetAmount) : undefined;

    if (!dailyRateVal && !budgetAmountVal) {
      setError('You must specify either a daily rate OR total budget amount.');
      return;
    }

    try {
      await requirementService.createRequirement(selectedProjectId, {
        location: reqLocation || 'Site Location',
        startDate: reqStartDate,
        durationDays: reqDuration,
        workerType: reqWorkerType,
        skillId: reqSkillId,
        quantity: reqQuantity,
        dailyRate: dailyRateVal,
        budgetAmount: budgetAmountVal,
        currencyCode: 'INR',
        accommodationAvailable: true,
        foodAvailable: false,
      });
      setShowReqModal(false);
      loadRequirements(selectedProjectId);
      setSuccess('Requirement draft created.');
    } catch (err: any) {
      setError(err.message || 'Failed to create requirement');
    }
  };

  const handleOpenRequirement = async (reqId: string) => {
    try {
      await requirementService.openRequirement(reqId);
      if (selectedProjectId) loadRequirements(selectedProjectId);
      setSuccess('Requirement published to marketplace discovery!');
    } catch (err: any) {
      setError(err.message || 'Failed to open requirement');
    }
  };

  const handleCancelRequirement = async (reqId: string) => {
    try {
      await requirementService.cancelRequirement(reqId);
      if (selectedProjectId) loadRequirements(selectedProjectId);
      setSuccess('Requirement cancelled.');
    } catch (err: any) {
      setError(err.message || 'Failed to cancel requirement');
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading client workspace...</div>;

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Client & Project Workspace</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          Manage client profile, site projects, and publish workforce requirements
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="tabs-container">
        <button
          className={`tab-btn ${activeTab === 'PROJECTS' ? 'active' : ''}`}
          onClick={() => setActiveTab('PROJECTS')}
        >
          Construction Projects ({projects.length})
        </button>
        <button
          className={`tab-btn ${activeTab === 'PROFILE' ? 'active' : ''}`}
          onClick={() => setActiveTab('PROFILE')}
        >
          Client Profile
        </button>
      </div>

      {activeTab === 'PROFILE' && (
        <div className="card" style={{ maxWidth: '600px' }}>
          <form onSubmit={handleProfileSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <label>Client Type</label>
              <select value={clientType} onChange={(e) => setClientType(e.target.value)}>
                <option value="HOMEOWNER">HOMEOWNER</option>
                <option value="BUILDER">BUILDER</option>
                <option value="BUSINESS">BUSINESS</option>
                <option value="OTHER">OTHER</option>
              </select>
            </div>
            <div style={{ marginBottom: '1.5rem' }}>
              <label>Business / Client Display Name</label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Sharma Builders & Developers"
                required
              />
            </div>
            <button type="submit" className="btn btn-primary">
              {profile ? 'Update Client Profile' : 'Create Client Profile'}
            </button>
          </form>
        </div>
      )}

      {activeTab === 'PROJECTS' && (
        <div style={{ display: 'grid', gridTemplateColumns: '320px 1fr', gap: '1.5rem' }}>
          {/* Project List Sidebar */}
          <div>
            <div className="card" style={{ marginBottom: '1rem' }}>
              <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem' }}>Create New Project</h4>
              <form onSubmit={handleCreateProject}>
                <div style={{ marginBottom: '0.75rem' }}>
                  <input
                    type="text"
                    value={projectTitle}
                    onChange={(e) => setProjectTitle(e.target.value)}
                    placeholder="Project Site Name"
                    required
                  />
                </div>
                <div style={{ marginBottom: '0.75rem' }}>
                  <input
                    type="text"
                    value={projectLocation}
                    onChange={(e) => setProjectLocation(e.target.value)}
                    placeholder="Location (e.g. Bellandur, Bengaluru)"
                    required
                  />
                </div>
                <div style={{ marginBottom: '0.75rem' }}>
                  <input
                    type="date"
                    value={projectStartDate}
                    onChange={(e) => setProjectStartDate(e.target.value)}
                  />
                </div>
                <button type="submit" className="btn btn-primary btn-sm" style={{ width: '100%' }}>
                  <Plus size={14} /> Add Project
                </button>
              </form>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {projects.map((p) => (
                <div
                  key={p.id}
                  onClick={() => setSelectedProjectId(p.id)}
                  className="card"
                  style={{
                    cursor: 'pointer',
                    borderColor: selectedProjectId === p.id ? 'var(--primary-color)' : 'var(--border-color)',
                    backgroundColor: selectedProjectId === p.id ? 'var(--primary-light)' : 'var(--bg-card)',
                  }}
                >
                  <h5 style={{ fontWeight: 600 }}>{p.title}</h5>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{p.location}</span>
                </div>
              ))}
            </div>
          </div>

          {/* Project Details & Requirements */}
          <div>
            {selectedProjectId ? (
              <div>
                <div className="card" style={{ marginBottom: '1.5rem' }}>
                  <div className="card-header">
                    <div>
                      <h3 className="card-title">
                        {projects.find((p) => p.id === selectedProjectId)?.title}
                      </h3>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                        <MapPin size={14} style={{ display: 'inline', marginRight: '4px' }} />
                        {projects.find((p) => p.id === selectedProjectId)?.location}
                      </p>
                    </div>
                    <button onClick={() => setShowReqModal(true)} className="btn btn-primary btn-sm">
                      <Plus size={14} /> Create Requirement
                    </button>
                  </div>
                </div>

                <h4 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>Workforce Requirements</h4>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                  {requirements.length === 0 ? (
                    <div className="card" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                      No workforce requirements created for this project yet.
                    </div>
                  ) : (
                    requirements.map((req) => (
                      <div key={req.id} className="card">
                        <div className="card-header">
                          <span className={`badge badge-${req.status.toLowerCase()}`}>{req.status}</span>
                          <span style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                            Duration: {req.durationDays} Days
                          </span>
                        </div>
                        <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>
                          {req.workerType.replace('_', ' ')} (Quantity: {req.quantity})
                        </h4>
                        <div style={{ display: 'flex', gap: '1.5rem', fontSize: '0.875rem', color: 'var(--text-secondary)', margin: '0.5rem 0' }}>
                          <span>Start: {req.startDate}</span>
                          <span style={{ color: 'var(--primary-color)', fontWeight: 600 }}>
                            {req.dailyRate ? `₹${req.dailyRate}/day` : `₹${req.budgetAmount} Budget`}
                          </span>
                        </div>

                        <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.75rem' }}>
                          {req.status === 'DRAFT' && (
                            <button
                              onClick={() => handleOpenRequirement(req.id)}
                              className="btn btn-primary btn-sm"
                            >
                              <Send size={14} /> Publish / Open Requirement
                            </button>
                          )}
                          {(req.status === 'DRAFT' || req.status === 'OPEN') && (
                            <button
                              onClick={() => handleCancelRequirement(req.id)}
                              className="btn btn-danger btn-sm"
                            >
                              <XCircle size={14} /> Cancel
                            </button>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            ) : (
              <div className="card" style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                Select a project on the left to view workforce requirements.
              </div>
            )}
          </div>
        </div>
      )}

      {/* Requirement Creation Modal */}
      {showReqModal && (
        <div className="modal-overlay">
          <div className="modal-content">
            <h3 style={{ fontSize: '1.25rem', fontWeight: 700, marginBottom: '1rem' }}>Create Workforce Requirement</h3>
            <form onSubmit={handleCreateRequirement}>
              <div style={{ marginBottom: '0.75rem' }}>
                <label>Worker Type</label>
                <select value={reqWorkerType} onChange={(e) => setReqWorkerType(e.target.value)}>
                  <option value="SKILLED_WORKER">SKILLED WORKER</option>
                  <option value="LABOUR">LABOUR</option>
                </select>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '0.75rem' }}>
                <div>
                  <label>Quantity</label>
                  <input
                    type="number"
                    min="1"
                    value={reqQuantity}
                    onChange={(e) => setReqQuantity(Number(e.target.value))}
                    required
                  />
                </div>
                <div>
                  <label>Duration (Days)</label>
                  <input
                    type="number"
                    min="1"
                    value={reqDuration}
                    onChange={(e) => setReqDuration(Number(e.target.value))}
                    required
                  />
                </div>
              </div>

              <div style={{ marginBottom: '0.75rem' }}>
                <label>Compensation Option (XOR Constraint)</label>
                <div style={{ display: 'flex', gap: '1rem', marginBottom: '0.5rem' }}>
                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="radio"
                      name="comp"
                      checked={reqCompensationType === 'DAILY'}
                      onChange={() => setReqCompensationType('DAILY')}
                    /> Daily Rate Offered
                  </label>
                  <label style={{ cursor: 'pointer' }}>
                    <input
                      type="radio"
                      name="comp"
                      checked={reqCompensationType === 'BUDGET'}
                      onChange={() => setReqCompensationType('BUDGET')}
                    /> Total Budget Amount
                  </label>
                </div>

                {reqCompensationType === 'DAILY' ? (
                  <input
                    type="number"
                    value={reqDailyRate}
                    onChange={(e) => setReqDailyRate(e.target.value ? Number(e.target.value) : '')}
                    placeholder="Daily rate in ₹ (e.g. 900)"
                    required
                  />
                ) : (
                  <input
                    type="number"
                    value={reqBudgetAmount}
                    onChange={(e) => setReqBudgetAmount(e.target.value ? Number(e.target.value) : '')}
                    placeholder="Total budget in ₹ (e.g. 15000)"
                    required
                  />
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.5rem', marginTop: '1.25rem' }}>
                <button type="button" onClick={() => setShowReqModal(false)} className="btn btn-secondary">
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary">
                  Create Requirement Draft
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
