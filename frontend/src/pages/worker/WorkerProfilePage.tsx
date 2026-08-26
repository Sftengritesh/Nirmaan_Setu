import React, { useState, useEffect } from 'react';
import { workerService, WorkerProfileResponse } from '../../services/workerService';
import { User, MapPin, Briefcase, Plus, Trash2, CheckCircle2 } from 'lucide-react';

export const WorkerProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<WorkerProfileResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Form State
  const [displayName, setDisplayName] = useState('');
  const [location, setLocation] = useState('');
  const [availabilityStatus, setAvailabilityStatus] = useState('AVAILABLE');
  const [experienceYears, setExperienceYears] = useState<number | ''>(3);
  const [dailyRate, setDailyRate] = useState<number | ''>(850);
  const [profileDescription, setProfileDescription] = useState('');
  const [isTravelWilling, setIsTravelWilling] = useState(true);

  // Skill Input
  const [newSkillId, setNewSkillId] = useState('');

  const loadProfile = async () => {
    setLoading(true);
    try {
      const data = await workerService.getProfile();
      setProfile(data);
      setDisplayName(data.displayName);
      setLocation(data.location);
      setAvailabilityStatus(data.availabilityStatus);
      setExperienceYears(data.experienceYears || '');
      setDailyRate(data.dailyRate || '');
      setProfileDescription(data.profileDescription || '');
      setIsTravelWilling(data.isTravelWilling);
    } catch (err: any) {
      if (err.code !== 'NOT_FOUND') {
        setError(err.message || 'Worker profile not set up yet');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    const req = {
      displayName,
      location,
      availabilityStatus,
      experienceYears: Number(experienceYears) || 0,
      dailyRate: Number(dailyRate) || 0,
      profileDescription,
      isTravelWilling,
    };

    try {
      if (profile) {
        const updated = await workerService.updateProfile(req);
        setProfile(updated);
        setSuccess('Worker profile updated successfully.');
      } else {
        const created = await workerService.createProfile(req);
        setProfile(created);
        setSuccess('Worker profile created successfully.');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to save profile');
    }
  };

  const handleAddSkill = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newSkillId) return;
    try {
      const updated = await workerService.addSkill(newSkillId);
      setProfile(updated);
      setNewSkillId('');
    } catch (err: any) {
      setError(err.message || 'Failed to add skill');
    }
  };

  const handleRemoveSkill = async (skillId: string) => {
    try {
      const updated = await workerService.removeSkill(skillId);
      setProfile(updated);
    } catch (err: any) {
      setError(err.message || 'Failed to remove skill');
    }
  };

  if (loading) return <div style={{ padding: '2rem', textAlign: 'center' }}>Loading worker profile...</div>;

  return (
    <div style={{ maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Worker Profile & Skills</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          Manage your trade details, daily wages, availability, and skill certifications
        </p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {success && <div className="alert alert-success">{success}</div>}

      <div className="card" style={{ marginBottom: '1.5rem' }}>
        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
            <div>
              <label>Full Display Name</label>
              <input
                type="text"
                value={displayName}
                onChange={(e) => setDisplayName(e.target.value)}
                placeholder="Ramesh Kumar"
                required
              />
            </div>
            <div>
              <label>Current Location / City</label>
              <input
                type="text"
                value={location}
                onChange={(e) => setLocation(e.target.value)}
                placeholder="Mumbai"
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
            <div>
              <label>Availability Status</label>
              <select value={availabilityStatus} onChange={(e) => setAvailabilityStatus(e.target.value)}>
                <option value="AVAILABLE">AVAILABLE</option>
                <option value="LIMITED">LIMITED</option>
                <option value="UNAVAILABLE">UNAVAILABLE</option>
              </select>
            </div>
            <div>
              <label>Experience (Years)</label>
              <input
                type="number"
                value={experienceYears}
                onChange={(e) => setExperienceYears(e.target.value ? Number(e.target.value) : '')}
                placeholder="5"
              />
            </div>
            <div>
              <label>Expected Daily Rate (₹)</label>
              <input
                type="number"
                value={dailyRate}
                onChange={(e) => setDailyRate(e.target.value ? Number(e.target.value) : '')}
                placeholder="850"
              />
            </div>
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label>Profile Description</label>
            <textarea
              rows={3}
              value={profileDescription}
              onChange={(e) => setProfileDescription(e.target.value)}
              placeholder="Experienced mason specializing in residential and commercial brickwork..."
            />
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={isTravelWilling}
                onChange={(e) => setIsTravelWilling(e.target.checked)}
                style={{ width: 'auto' }}
              />
              Willing to travel for outstation projects
            </label>
          </div>

          <button type="submit" className="btn btn-primary">
            {profile ? 'Update Profile' : 'Create Profile'}
          </button>
        </form>
      </div>

      {/* Skills Management */}
      {profile && (
        <div className="card">
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '1rem' }}>Associated Trade Skills</h3>

          <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem', marginBottom: '1.5rem' }}>
            {profile.skills.length === 0 ? (
              <span style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>No skills associated yet.</span>
            ) : (
              profile.skills.map((s) => (
                <span
                  key={s.id}
                  className="badge badge-worker"
                  style={{ padding: '0.375rem 0.75rem', fontSize: '0.8125rem' }}
                >
                  {s.name} ({s.code})
                  <button
                    onClick={() => handleRemoveSkill(s.id)}
                    style={{ marginLeft: '0.375rem', color: 'var(--text-muted)' }}
                  >
                    <Trash2 size={12} />
                  </button>
                </span>
              ))
            )}
          </div>

          <form onSubmit={handleAddSkill} style={{ display: 'flex', gap: '0.75rem' }}>
            <input
              type="text"
              value={newSkillId}
              onChange={(e) => setNewSkillId(e.target.value)}
              placeholder="Enter Skill UUID (e.g. a1b2c3d4-e89b-12d3-a456-426614174000)"
              required
            />
            <button type="submit" className="btn btn-secondary">
              <Plus size={16} /> Add Skill
            </button>
          </form>
        </div>
      )}
    </div>
  );
};
