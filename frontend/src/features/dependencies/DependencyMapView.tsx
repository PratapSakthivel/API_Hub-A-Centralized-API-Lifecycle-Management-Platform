import { useEffect, useState } from 'react';
import api from '../../lib/api';

interface DependencyDto {
  id: number;
  sourceApiId: number;
  sourceApiName: string;
  targetApiId: number;
  targetApiName: string;
  relationshipType: string;
  createdAt: string;
}

interface ImpactAnalysisDto {
  apiId: number;
  apiName: string;
  affectedApiCount: number;
  affectedApiNames: string[];
  transitiveChain: string[];
}

const RELATIONSHIP_TYPES = ['DEPENDS_ON', 'EXTENDS', 'USES', 'INCLUDES'];

export default function DependencyMapView() {
  const [apiId, setApiId] = useState<string>('');
  const [upstreamDeps, setUpstreamDeps] = useState<DependencyDto[]>([]);
  const [downstreamDeps, setDownstreamDeps] = useState<DependencyDto[]>([]);
  const [impact, setImpact] = useState<ImpactAnalysisDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Create form state
  const [sourceApiId, setSourceApiId] = useState('');
  const [targetApiId, setTargetApiId] = useState('');
  const [relType, setRelType] = useState(RELATIONSHIP_TYPES[0]);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  const fetchDependencies = async () => {
    if (!apiId) return;
    setLoading(true);
    setError(null);
    try {
      const [upRes, downRes, impRes] = await Promise.all([
        api.get<DependencyDto[]>(`/apis/${apiId}/dependencies/upstream`),
        api.get<DependencyDto[]>(`/apis/${apiId}/dependencies/downstream`),
        api.get<ImpactAnalysisDto>(`/apis/${apiId}/dependencies/impact`),
      ]);
      setUpstreamDeps(upRes.data);
      setDownstreamDeps(downRes.data);
      setImpact(impRes.data);
    } catch {
      setError('Failed to load dependencies. Check the API ID.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (apiId) fetchDependencies();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleDelete = async (depId: number) => {
    try {
      await api.delete(`/dependencies/${depId}`);
      fetchDependencies();
    } catch {
      setError('Failed to delete dependency.');
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreating(true);
    setCreateError(null);
    try {
      await api.post('/dependencies', {
        sourceApiId: Number(sourceApiId),
        targetApiId: Number(targetApiId),
        relationshipType: relType,
      });
      setSourceApiId('');
      setTargetApiId('');
      if (apiId) fetchDependencies();
    } catch {
      setCreateError('Failed to create dependency. Check for cycles or duplicates.');
    } finally {
      setCreating(false);
    }
  };

  const DependencyRow = ({ dep, label }: { dep: DependencyDto; label: string }) => (
    <div className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-lg shadow-sm">
      <div className="flex items-center gap-3">
        <span className="text-xs font-semibold text-indigo-600 bg-indigo-50 px-2 py-1 rounded">{label}</span>
        <span className="font-medium text-gray-800">{dep.sourceApiName}</span>
        <span className="text-gray-400">→</span>
        <span className="font-medium text-gray-800">{dep.targetApiName}</span>
        <span className="text-xs text-gray-500 bg-gray-100 px-2 py-1 rounded">{dep.relationshipType}</span>
      </div>
      <button
        onClick={() => handleDelete(dep.id)}
        className="text-red-500 hover:text-red-700 text-sm font-medium"
      >
        Remove
      </button>
    </div>
  );

  return (
    <div className="p-6 max-w-5xl mx-auto space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">API Dependency Map</h1>
        <p className="text-gray-500 mt-1">Visualize upstream/downstream dependencies and assess impact of API changes.</p>
      </div>

      {/* API ID Search */}
      <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <label className="block text-sm font-semibold text-gray-700 mb-2">Enter API ID to Explore</label>
        <div className="flex gap-3">
          <input
            type="number"
            className="border border-gray-300 rounded-lg px-4 py-2 w-48 focus:outline-none focus:ring-2 focus:ring-indigo-400"
            placeholder="e.g. 1"
            value={apiId}
            onChange={e => setApiId(e.target.value)}
          />
          <button
            onClick={fetchDependencies}
            disabled={!apiId || loading}
            className="bg-indigo-600 text-white px-5 py-2 rounded-lg hover:bg-indigo-700 disabled:opacity-50 font-medium"
          >
            {loading ? 'Loading…' : 'Explore'}
          </button>
        </div>
        {error && <p className="text-red-500 mt-2 text-sm">{error}</p>}
      </div>

      {/* Add Dependency Form */}
      <div className="bg-white border border-gray-200 rounded-xl p-5 shadow-sm">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">Add Dependency</h2>
        <form onSubmit={handleCreate} className="flex flex-wrap gap-3 items-end">
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Source API ID</label>
            <input
              type="number"
              required
              className="border border-gray-300 rounded-lg px-3 py-2 w-32 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              placeholder="Source"
              value={sourceApiId}
              onChange={e => setSourceApiId(e.target.value)}
            />
          </div>
          <span className="text-gray-400 pb-2">→</span>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Target API ID</label>
            <input
              type="number"
              required
              className="border border-gray-300 rounded-lg px-3 py-2 w-32 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              placeholder="Target"
              value={targetApiId}
              onChange={e => setTargetApiId(e.target.value)}
            />
          </div>
          <div>
            <label className="block text-xs font-medium text-gray-600 mb-1">Relationship</label>
            <select
              className="border border-gray-300 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-400"
              value={relType}
              onChange={e => setRelType(e.target.value)}
            >
              {RELATIONSHIP_TYPES.map(r => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
          <button
            type="submit"
            disabled={creating}
            className="bg-green-600 text-white px-5 py-2 rounded-lg hover:bg-green-700 disabled:opacity-50 font-medium"
          >
            {creating ? 'Adding…' : 'Add Dependency'}
          </button>
        </form>
        {createError && <p className="text-red-500 mt-2 text-sm">{createError}</p>}
      </div>

      {/* Upstream */}
      {upstreamDeps.length > 0 && (
        <div className="space-y-3">
          <h2 className="text-lg font-semibold text-gray-800">
            Upstream Dependencies
            <span className="ml-2 text-sm text-gray-500 font-normal">
              ({upstreamDeps.length} APIs that this API depends on)
            </span>
          </h2>
          {upstreamDeps.map(dep => (
            <DependencyRow key={dep.id} dep={dep} label="UPSTREAM" />
          ))}
        </div>
      )}

      {/* Downstream */}
      {downstreamDeps.length > 0 && (
        <div className="space-y-3">
          <h2 className="text-lg font-semibold text-gray-800">
            Downstream Dependencies
            <span className="ml-2 text-sm text-gray-500 font-normal">
              ({downstreamDeps.length} APIs that depend on this API)
            </span>
          </h2>
          {downstreamDeps.map(dep => (
            <DependencyRow key={dep.id} dep={dep} label="DOWNSTREAM" />
          ))}
        </div>
      )}

      {/* Empty state */}
      {!loading && apiId && upstreamDeps.length === 0 && downstreamDeps.length === 0 && (
        <div className="text-center py-12 text-gray-400">
          <div className="text-5xl mb-4">🔗</div>
          <p className="text-lg">No dependencies found for API #{apiId}</p>
          <p className="text-sm mt-1">Use the form above to add dependency relationships.</p>
        </div>
      )}

      {/* Impact Analysis */}
      {impact && (
        <div className="bg-amber-50 border border-amber-200 rounded-xl p-5 shadow-sm">
          <h2 className="text-lg font-semibold text-amber-800 mb-3">
            Impact Analysis — {impact.apiName}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-white rounded-lg p-4 border border-amber-100">
              <p className="text-3xl font-bold text-amber-600">{impact.affectedApiCount}</p>
              <p className="text-sm text-gray-600 mt-1">APIs affected by a change to this API</p>
            </div>
            {impact.affectedApiNames.length > 0 && (
              <div className="bg-white rounded-lg p-4 border border-amber-100">
                <p className="text-sm font-semibold text-gray-700 mb-2">Affected APIs:</p>
                <ul className="list-disc list-inside space-y-1">
                  {impact.affectedApiNames.map(name => (
                    <li key={name} className="text-sm text-gray-600">{name}</li>
                  ))}
                </ul>
              </div>
            )}
            {impact.transitiveChain.length > 0 && (
              <div className="bg-white rounded-lg p-4 border border-amber-100 md:col-span-2">
                <p className="text-sm font-semibold text-gray-700 mb-2">Transitive Impact Chain:</p>
                <div className="flex flex-wrap gap-2">
                  {impact.transitiveChain.map((step, i) => (
                    <span key={i} className="text-xs bg-amber-100 text-amber-800 px-2 py-1 rounded-full">
                      {step}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
