import React, { useState, useEffect } from 'react';
import { contractorService, ContractorProfileResponse, ContractorWorkerResponse } from '../../services/contractorService';
import { teamService, TeamResponse, TeamMemberResponse } from '../../services/teamService';
import { Briefcase, Users, Plus, UserMinus, Building } from 'lucide-react';

export const ContractorPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'PROFILE' | 'WORKERS' | 'TEAMS'>('PROFILE');
  const [profile, setProfile] = useState<ContractorProfileResponse | null>(null);
  const [workers, setWorkers] = useState<ContractorWorkerResponse[]>([]);
  const [teams, setTeams] = useState<TeamResponse[]>([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Profile Form State
  const [displayName, setDisplayName] = useState('');
  const [location, setLocation] = useState('');
  const [description, setDescription] = useState('');

  // Associate Worker Form State
  const [workerProfileId, setWorkerProfileId] = useState('');
  const [startsOn, setStartsOn] = useState(new Date().toISOString().split('T')[0]);

  // Create Team Form State
  const [teamName, setTeamName] = useState('');
  const [teamDesc, setTeamDesc] = useState('');

  const loadContractorData = async () => {
    setLoading(true);
    try {
      const p = await contractorService.getProfile();
      setProfile(p);
      setDisplayName(p.displayName);
      setLocation(p.location);
      setDescription(p.description || '');

      const wList = await contractorService.getAssociatedWorkers();
      setWorkers(wList);

      const tList = await teamService.getMyTeams();
      setTeams(tList);
    } catch (err: any) {
      if (err.code !== 'NOT_FOUND') {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContractorData();
  }, []);

  const handleProfileSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    const req = { displayName, location, description };
    try {
      if (profile) {
        const updated = await contractorService.updateProfile(req);
        setProfile(updated);
        setSuccess('Contractor profile updated.');
      } else {
        const created = await contractorService.createProfile(req);
        setProfile(created);
        setSuccess('Contractor profile created.');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to save contractor profile');
    }
  };

  const handleAssociateWorker = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!workerProfileId) return;
    setError(null);
    try {
      await contractorService.associateWorker({ workerProfileId, startsOn });
      setWorkerProfileId('');
      const updated = await contractorService.getAssociatedWorkers();
      setWorkers(updated);
      setSuccess('Worker associated with contractor profile.');
    } catch (err: any) {
      setError(err.message || 'Failed to associate worker');
    }
  };

  const handleEndWorkerAssociation = async (workerId: string, start: string) => {
    try {
      const endsOn = new Date().toISOString().split('T')[0];
      await contractorService.endWorkerAssociation(workerId, start, endsOn);
      const updated = await contractorService.getAssociatedWorkers();
      setWorkers(updated);
      setSuccess('Worker association ended.');
    } catch (err: any) {
      setError(err.message || 'Failed to end association');
    }
  };

  const handleCreateTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!teamName) return;
    try {
      await teamService.createTeam({ name: teamName, description: teamDesc });
      setTeamName('');
      setTeamDesc('');
      const updated = await teamService.getMyTeams();
      setTeams(updated);
      setSuccess('Team created successfully.');
    } catch (err: any) {
      setError(err.message || 'Failed to create team');
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading contractor workspace...</div>;

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Contractor & Workforce Management</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          Manage company profile, worker labor pool, and structured construction teams
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="tabs-container">
        <button
          className={`tab-btn ${activeTab === 'PROFILE' ? 'active' : ''}`}
          onClick={() => setActiveTab('PROFILE')}
        >
          Company Profile
        </button>
        <button
          className={`tab-btn ${activeTab === 'WORKERS' ? 'active' : ''}`}
          onClick={() => setActiveTab('WORKERS')}
        >
          Worker Roster ({workers.length})
        </button>
        <button
          className={`tab-btn ${activeTab === 'TEAMS' ? 'active' : ''}`}
          onClick={() => setActiveTab('TEAMS')}
        >
          Managed Teams ({teams.length})
        </button>
      </div>

      {activeTab === 'PROFILE' && (
        <div className="card" style={{ maxWidth: '700px' }}>
          <form onSubmit={handleProfileSubmit}>
            <div style={{ marginBottom: '1rem' }}>
              <label>Company / Contractor Name</label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Apex Infrastructure Pvt Ltd"
                required
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label>Operating City / Location</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="Delhi NCR"
                required
              />
            </div>
            <div style={{ marginBottom: '1.5rem' }}>
              <label>Business Description</label>
              <textarea
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="General contractor specializing in civil infrastructure..."
              />
            </div>
            <button type="submit" className="btn btn-primary">
              {profile ? 'Update Contractor Profile' : 'Create Contractor Profile'}
            </button>
          </form>
        </div>
      )}

      {activeTab === 'WORKERS' && (
        <div>
          <div className="card" style={{ marginBottom: '1.5rem', maxWidth: '600px' }}>
            <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem' }}>Associate Worker to Contractor Pool</h4>
            <form onSubmit={handleAssociateWorker} style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
              <input
                type="text"
                value={workerProfileId}
                onChange={(e) => setWorkerProfileId(e.target.value)}
                placeholder="Worker Profile UUID"
                style={{ flex: 1 }}
                required
              />
              <input
                type="date"
                value={startsOn}
                onChange={(e) => setStartsOn(e.target.value)}
                style={{ width: '160px' }}
                required
              />
              <button type="submit" className="btn btn-primary">
                <Plus size={16} /> Associate
              </button>
            </form>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
            {workers.length === 0 ? (
              <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No workers currently associated.
              </div>
            ) : (
              workers.map((w, idx) => (
                <div key={idx} className="card">
                  <div className="card-header">
                    <span className="badge badge-worker">Worker</span>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Since {w.startsOn}</span>
                  </div>
                  <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                    Profile ID: <code style={{ color: 'var(--primary-color)' }}>{w.workerProfileId}</code>
                  </p>
                  {w.endsOn ? (
                    <span style={{ fontSize: '0.75rem', color: 'var(--status-rejected-text)' }}>Ended on {w.endsOn}</span>
                  ) : (
                    <button
                      onClick={() => handleEndWorkerAssociation(w.workerProfileId, w.startsOn)}
                      className="btn btn-danger btn-sm"
                      style={{ width: '100%', marginTop: '0.75rem' }}
                    >
                      <UserMinus size={14} /> End Association
                    </button>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}

      {activeTab === 'TEAMS' && (
        <div>
          <div className="card" style={{ marginBottom: '1.5rem', maxWidth: '600px' }}>
            <h4 style={{ fontSize: '1rem', fontWeight: 600, marginBottom: '0.75rem' }}>Create Structured Team</h4>
            <form onSubmit={handleCreateTeam}>
              <div style={{ marginBottom: '0.75rem' }}>
                <input
                  type="text"
                  value={teamName}
                  onChange={(e) => setTeamName(e.target.value)}
                  placeholder="Team Name (e.g. Masonry Team Alpha)"
                  required
                />
              </div>
              <div style={{ marginBottom: '0.75rem' }}>
                <input
                  type="text"
                  value={teamDesc}
                  onChange={(e) => setTeamDesc(e.target.value)}
                  placeholder="Team description..."
                />
              </div>
              <button type="submit" className="btn btn-primary">
                <Plus size={16} /> Create Team
              </button>
            </form>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1rem' }}>
            {teams.length === 0 ? (
              <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                No teams created yet.
              </div>
            ) : (
              teams.map((t) => (
                <div key={t.id} className="card">
                  <div className="card-header">
                    <span className="badge badge-accepted">{t.status}</span>
                  </div>
                  <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>{t.name}</h4>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                    {t.description || 'Specialized workforce squad'}
                  </p>
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};
