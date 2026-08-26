import React, { useState, useEffect } from 'react';
import { discoveryService } from '../../services/discoveryService';
import { WorkerProfileResponse } from '../../services/workerService';
import { ContractorProfileResponse } from '../../services/contractorService';
import { TeamResponse } from '../../services/teamService';
import { WorkforceRequirementResponse } from '../../services/requirementService';
import { Search, MapPin, Calendar, Briefcase, CheckCircle, ChevronLeft, ChevronRight } from 'lucide-react';

interface MarketplacePageProps {
  onSelectRequirementForBooking?: (req: WorkforceRequirementResponse) => void;
}

export const MarketplacePage: React.FC<MarketplacePageProps> = ({ onSelectRequirementForBooking }) => {
  const [tab, setTab] = useState<'WORKERS' | 'CONTRACTORS' | 'TEAMS' | 'REQUIREMENTS'>('REQUIREMENTS');
  const [location, setLocation] = useState('');
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [loading, setLoading] = useState(false);

  const [workers, setWorkers] = useState<WorkerProfileResponse[]>([]);
  const [contractors, setContractors] = useState<ContractorProfileResponse[]>([]);
  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [requirements, setRequirements] = useState<WorkforceRequirementResponse[]>([]);

  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const loadData = async () => {
    setLoading(true);
    try {
      if (tab === 'WORKERS') {
        const res = await discoveryService.searchWorkers({ location, page, size: pageSize });
        setWorkers(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      } else if (tab === 'CONTRACTORS') {
        const res = await discoveryService.searchContractors({ location, page, size: pageSize });
        setContractors(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      } else if (tab === 'TEAMS') {
        const res = await discoveryService.searchTeams({ location, page, size: pageSize });
        setTeams(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      } else if (tab === 'REQUIREMENTS') {
        const res = await discoveryService.searchRequirements({ location, page, size: pageSize });
        setRequirements(res.content);
        setTotalElements(res.totalElements);
        setTotalPages(res.totalPages);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setPage(0);
  }, [tab]);

  useEffect(() => {
    loadData();
  }, [tab, page, location]);

  return (
    <div>
      <div style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Marketplace Discovery</h2>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
          Explore open requirements, skilled workers, verified contractors, and teams across India
        </p>
      </div>

      {/* Tabs */}
      <div className="tabs-container">
        <button
          className={`tab-btn ${tab === 'REQUIREMENTS' ? 'active' : ''}`}
          onClick={() => setTab('REQUIREMENTS')}
        >
          Open Requirements
        </button>
        <button
          className={`tab-btn ${tab === 'WORKERS' ? 'active' : ''}`}
          onClick={() => setTab('WORKERS')}
        >
          Skilled Workers
        </button>
        <button
          className={`tab-btn ${tab === 'CONTRACTORS' ? 'active' : ''}`}
          onClick={() => setTab('CONTRACTORS')}
        >
          Contractors
        </button>
        <button
          className={`tab-btn ${tab === 'TEAMS' ? 'active' : ''}`}
          onClick={() => setTab('TEAMS')}
        >
          Teams
        </button>
      </div>

      {/* Search & Filters */}
      <div className="card" style={{ marginBottom: '1.5rem', padding: '1rem' }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <div style={{ position: 'relative', flex: 1 }}>
            <MapPin size={18} style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              type="text"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              placeholder="Filter by city or location (e.g. Mumbai, Bengaluru)..."
              style={{ paddingLeft: '2.25rem' }}
            />
          </div>
          <button onClick={() => loadData()} className="btn btn-primary">
            <Search size={16} /> Search
          </button>
        </div>
      </div>

      {/* Results Grid */}
      {loading ? (
        <div style={{ padding: '3rem', textAlign: 'center', color: 'var(--text-muted)' }}>Searching marketplace...</div>
      ) : (
        <>
          {tab === 'REQUIREMENTS' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '1.25rem' }}>
              {requirements.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No open requirements found.
                </div>
              ) : (
                requirements.map((req) => (
                  <div key={req.id} className="card">
                    <div className="card-header">
                      <span className="badge badge-open">{req.status}</span>
                      <span style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>{req.durationDays} days</span>
                    </div>
                    <h4 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: '0.5rem' }}>
                      {req.workerType.replace('_', ' ')} (Qty: {req.quantity})
                    </h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginBottom: '0.75rem' }}>
                      <MapPin size={14} style={{ display: 'inline', marginRight: '4px' }} />
                      {req.location}
                    </p>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.875rem', marginBottom: '0.75rem', color: 'var(--text-secondary)' }}>
                      <span>Start: {req.startDate}</span>
                      <span style={{ fontWeight: 600, color: 'var(--primary-color)' }}>
                        {req.dailyRate ? `₹${req.dailyRate}/day` : `₹${req.budgetAmount} Budget`}
                      </span>
                    </div>
                    {onSelectRequirementForBooking && (
                      <button
                        onClick={() => onSelectRequirementForBooking(req)}
                        className="btn btn-primary btn-sm"
                        style={{ width: '100%', marginTop: '0.5rem' }}
                      >
                        Submit Booking Proposal
                      </button>
                    )}
                  </div>
                ))
              )}
            </div>
          )}

          {tab === 'WORKERS' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.25rem' }}>
              {workers.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No workers found.
                </div>
              ) : (
                workers.map((w) => (
                  <div key={w.id} className="card">
                    <div className="card-header">
                      <span className={`badge ${w.availabilityStatus === 'AVAILABLE' ? 'badge-open' : 'badge-draft'}`}>
                        {w.availabilityStatus}
                      </span>
                      {w.isTravelWilling && <span className="badge badge-client">Travel Willing</span>}
                    </div>
                    <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>{w.displayName}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', margin: '0.375rem 0' }}>
                      <MapPin size={14} style={{ display: 'inline', marginRight: '4px' }} />
                      {w.location} • {w.experienceYears || 0} yrs exp
                    </p>
                    {w.dailyRate && (
                      <div style={{ fontSize: '1rem', fontWeight: 700, color: 'var(--accent-teal)' }}>
                        ₹{w.dailyRate}/day
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          )}

          {tab === 'CONTRACTORS' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.25rem' }}>
              {contractors.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No contractors found.
                </div>
              ) : (
                contractors.map((c) => (
                  <div key={c.id} className="card">
                    <div className="card-header">
                      <span className="badge badge-contractor">Contractor</span>
                    </div>
                    <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>{c.displayName}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.375rem' }}>
                      <MapPin size={14} style={{ display: 'inline', marginRight: '4px' }} />
                      {c.location}
                    </p>
                  </div>
                ))
              )}
            </div>
          )}

          {tab === 'TEAMS' && (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.25rem' }}>
              {teams.length === 0 ? (
                <div style={{ gridColumn: '1 / -1', padding: '2rem', textAlign: 'center', color: 'var(--text-muted)' }}>
                  No teams found.
                </div>
              ) : (
                teams.map((t) => (
                  <div key={t.id} className="card">
                    <div className="card-header">
                      <span className="badge badge-accepted">{t.status}</span>
                    </div>
                    <h4 style={{ fontSize: '1.125rem', fontWeight: 600 }}>{t.name}</h4>
                    <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', marginTop: '0.375rem' }}>
                      {t.description || 'Structured construction squad'}
                    </p>
                  </div>
                ))
              )}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="pagination-bar">
              <span style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                Page {page + 1} of {totalPages} ({totalElements} total items)
              </span>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <button
                  onClick={() => setPage(Math.max(0, page - 1))}
                  disabled={page === 0}
                  className="btn btn-secondary btn-sm"
                >
                  <ChevronLeft size={16} /> Previous
                </button>
                <button
                  onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                  disabled={page >= totalPages - 1}
                  className="btn btn-secondary btn-sm"
                >
                  Next <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
};
