import { useEffect, useState } from 'react';
import api from '../../lib/api';

type Stage = 'DEVELOPMENT' | 'TESTING' | 'STAGING' | 'PRODUCTION';

interface ReleaseDto {
  id: number;
  apiId: number;
  apiName: string;
  versionId: number | null;
  versionLabel: string | null;
  stage: Stage;
  approvedBy: string | null;
  notes: string | null;
  promotedAt: string | null;
  createdAt: string;
}

const STAGES: Stage[] = ['DEVELOPMENT', 'TESTING', 'STAGING', 'PRODUCTION'];

const STAGE_COLORS: Record<Stage, string> = {
  DEVELOPMENT: 'bg-blue-100 text-blue-800 border-blue-200',
  TESTING: 'bg-yellow-100 text-yellow-800 border-yellow-200',
  STAGING: 'bg-orange-100 text-orange-800 border-orange-200',
  PRODUCTION: 'bg-green-100 text-green-800 border-green-200',
};

const STAGE_DOT: Record<Stage, string> = {
  DEVELOPMENT: 'bg-blue-500',
  TESTING: 'bg-yellow-500',
  STAGING: 'bg-orange-500',
  PRODUCTION: 'bg-green-500',
};

export default function ReleaseManagement() {
  const [apiId, setApiId] = useState('');
  const [releases, setReleases] = useState<ReleaseDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Create form
  const [createApiId, setCreateApiId] = useState('');
  const [createNotes, setCreateNotes] = useState('');
  const [creating, setCreating] = useState(false);

  // Promote
  const [promoting, setPromoting] = useState<number | null>(null);
  const [promoterName, setPromoterName] = useState('');

  const fetchReleases = async () => {
    if (!apiId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<ReleaseDto[]>(`/apis/${apiId}/releases`);
      setReleases(Array.isArray(res.data) ? res.data : (res.data as { data: ReleaseDto[] }).data ?? []);
    } catch {
      setError('Failed to load releases.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (apiId) fetchReleases();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    try {
      await api.post('/releases', { apiId: Number(createApiId), notes: createNotes });
      setCreateNotes('');
      if (createApiId === apiId) fetchReleases();
    } catch {
      setError('Failed to create release.');
    } finally {
      setCreating(false);
    }
  };

  const handlePromote = async (releaseId: number) => {
    setPromoting(releaseId);
    try {
      await api.post(`/releases/${releaseId}/promote`, { approvedBy: promoterName || 'Admin' });
      setPromoterName('');
      fetchReleases();
    } catch {
      setError('Promotion failed — check stage order or permissions.');
    } finally {
      setPromoting(null);
    }
  };

  const getNextStage = (current: Stage): Stage | null => {
    const idx = STAGES.indexOf(current);
    return idx < STAGES.length - 1 ? STAGES[idx + 1] : null;
  };

  return (
    <div className="p-6 max-w-6xl mx-auto space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Release Management</h1>
        <p className="text-gray-500 mt-1">Track API releases through the 4-stage promotion pipeline.</p>
      </div>

      {/* Pipeline Legend */}
      <div className="flex gap-2 items-center flex-wrap">
        {STAGES.map((stage, i) => (
          <div key={stage} className="flex items-center gap-2">
            <span className={`px-3 py-1 rounded-full text-xs font-semibold border ${STAGE_COLORS[stage]}`}>
              {stage}
            </span>
            {i < STAGES.length - 1 && <span className="text-gray-400">→</span>}
          </div>
        ))}
      </div>

      {/* Create Release */}
      <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Create New Release</h2>
        <form onSubmit={handleCreate} className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">API ID</label>
            <input
              type="number"
              required
              className="border border-gray-300 rounded-lg px-3 py-2 w-28 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              value={createApiId}
              onChange={e => setCreateApiId(e.target.value)}
              placeholder="e.g. 1"
            />
          </div>
          <div className="flex-1 min-w-48">
            <label className="block text-xs font-medium text-gray-600 mb-1">Notes</label>
            <input
              type="text"
              className="border border-gray-300 rounded-lg px-3 py-2 w-full focus:outline-none focus:ring-2 focus:ring-indigo-400"
              value={createNotes}
              onChange={e => setCreateNotes(e.target.value)}
              placeholder="Release notes..."
            />
          </div>
          <button
            type="submit"
            disabled={creating}
            className="bg-indigo-600 text-white px-5 py-2 rounded-lg hover:bg-indigo-700 disabled:opacity-50 font-medium"
          >
            {creating ? 'Creating…' : 'Create Release'}
          </button>
        </form>
      </div>

      {/* Load Releases */}
      <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <div className="flex gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">API ID to View Releases</label>
            <input
              type="number"
              className="border border-gray-300 rounded-lg px-3 py-2 w-28 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              value={apiId}
              onChange={e => setApiId(e.target.value)}
              placeholder="API ID"
            />
          </div>
          <button
            onClick={fetchReleases}
            disabled={!apiId || loading}
            className="bg-gray-800 text-white px-5 py-2 rounded-lg hover:bg-gray-900 disabled:opacity-50 font-medium"
          >
            {loading ? 'Loading…' : 'Load Releases'}
          </button>
        </div>
        {error && <p className="text-red-500 mt-2 text-sm">{error}</p>}
      </div>

      {/* Approve Name */}
      {releases.length > 0 && (
        <div className="flex items-center gap-3">
          <label className="text-sm font-medium text-gray-700">Approver Name:</label>
          <input
            type="text"
            className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
            value={promoterName}
            onChange={e => setPromoterName(e.target.value)}
            placeholder="Your name"
          />
        </div>
      )}

      {/* Stage Pipeline Board */}
      {releases.length > 0 && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {STAGES.map(stage => {
            const stageReleases = releases.filter(r => r.stage === stage);
            return (
              <div key={stage} className={`rounded-xl border-2 p-4 ${STAGE_COLORS[stage]}`}>
                <div className="flex items-center gap-2 mb-3">
                  <div className={`w-2.5 h-2.5 rounded-full ${STAGE_DOT[stage]}`} />
                  <h3 className="font-bold text-sm">{stage}</h3>
                  <span className="ml-auto text-xs font-semibold opacity-70">{stageReleases.length}</span>
                </div>
                <div className="space-y-3">
                  {stageReleases.map(rel => (
                    <div key={rel.id} className="bg-white rounded-lg p-3 shadow-sm border border-white">
                      <p className="font-semibold text-gray-800 text-sm">{rel.apiName}</p>
                      {rel.versionLabel && (
                        <p className="text-xs text-gray-500">v{rel.versionLabel}</p>
                      )}
                      {rel.notes && (
                        <p className="text-xs text-gray-500 mt-1 italic truncate">{rel.notes}</p>
                      )}
                      {rel.approvedBy && (
                        <p className="text-xs text-gray-400 mt-1">✓ {rel.approvedBy}</p>
                      )}
                      {getNextStage(rel.stage) && (
                        <button
                          onClick={() => handlePromote(rel.id)}
                          disabled={promoting === rel.id}
                          className="mt-2 w-full text-xs bg-gray-800 text-white px-2 py-1.5 rounded hover:bg-gray-700 disabled:opacity-50 font-medium"
                        >
                          {promoting === rel.id ? 'Promoting…' : `→ Promote to ${getNextStage(rel.stage)}`}
                        </button>
                      )}
                      {!getNextStage(rel.stage) && (
                        <span className="mt-2 block text-center text-xs text-green-700 font-semibold">🚀 In Production</span>
                      )}
                    </div>
                  ))}
                  {stageReleases.length === 0 && (
                    <p className="text-xs opacity-60 text-center py-4">No releases</p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {releases.length === 0 && !loading && apiId && (
        <div className="text-center py-12 text-gray-400">
          <div className="text-5xl mb-4">📦</div>
          <p className="text-lg">No releases found for API #{apiId}</p>
        </div>
      )}
    </div>
  );
}
