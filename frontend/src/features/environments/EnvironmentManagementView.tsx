import { useEffect, useState } from 'react';
import api from '../../lib/api';

interface EnvironmentVariable {
  id?: number;
  key: string;
  value: string;
  secret: boolean;
}

interface EnvironmentDto {
  id: number;
  apiId: number | null;
  apiName: string;
  name: string;
  code: string;
  baseUrl: string;
  description: string;
  active: boolean;
  variables: EnvironmentVariable[];
  createdAt: string;
}

const CODE_BADGES: Record<string, string> = {
  DEV: 'bg-sky-100 text-sky-800 border-sky-300',
  STAGING: 'bg-amber-100 text-amber-800 border-amber-300',
  PRODUCTION: 'bg-emerald-100 text-emerald-800 border-emerald-300',
  SANDBOX: 'bg-purple-100 text-purple-800 border-purple-300',
};

export default function EnvironmentManagementView() {
  const [environments, setEnvironments] = useState<EnvironmentDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [unmask, setUnmask] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // New Environment Form
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [name, setName] = useState('');
  const [code, setCode] = useState('DEV');
  const [baseUrl, setBaseUrl] = useState('');
  const [description, setDescription] = useState('');
  const [apiId, setApiId] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // Variable editor on selected environment
  const [selectedEnvId, setSelectedEnvId] = useState<number | null>(null);
  const [varKey, setVarKey] = useState('');
  const [varValue, setVarValue] = useState('');
  const [varSecret, setVarSecret] = useState(false);
  const [varSubmitting, setVarSubmitting] = useState(false);

  const fetchEnvironments = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<EnvironmentDto[]>('/environments', {
        params: { unmaskSecrets: unmask },
      });
      const data = Array.isArray(res.data)
        ? res.data
        : (res.data as unknown as { data: EnvironmentDto[] }).data || [];
      setEnvironments(data);
    } catch {
      setError('Failed to load environments.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEnvironments();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [unmask]);

  const handleCreateEnv = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/environments', {
        name,
        code,
        baseUrl,
        description,
        apiId: apiId ? Number(apiId) : undefined,
      });
      setName('');
      setBaseUrl('');
      setDescription('');
      setApiId('');
      setShowCreateModal(false);
      fetchEnvironments();
    } catch {
      setError('Failed to create environment.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (id: number) => {
    try {
      await api.patch(`/environments/${id}/toggle-status`);
      fetchEnvironments();
    } catch {
      setError('Failed to toggle environment status.');
    }
  };

  const handleAddVariable = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedEnvId || !varKey.trim()) return;
    setVarSubmitting(true);
    try {
      await api.post(`/environments/${selectedEnvId}/variables`, {
        key: varKey.trim(),
        value: varValue,
        secret: varSecret,
      });
      setVarKey('');
      setVarValue('');
      setVarSecret(false);
      fetchEnvironments();
    } catch {
      setError('Failed to save environment variable.');
    } finally {
      setVarSubmitting(false);
    }
  };

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Environment Management</h1>
          <p className="text-slate-500 mt-1">
            Manage target runtime hostnames, stage endpoints, and masked secret credentials.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setUnmask(!unmask)}
            className={`px-3 py-2 text-xs font-semibold rounded-lg border transition-colors ${
              unmask
                ? 'bg-rose-50 text-rose-700 border-rose-300'
                : 'bg-white text-slate-700 border-slate-300 hover:bg-slate-50'
            }`}
          >
            {unmask ? '🔒 Mask Secrets' : '👁 Unmask Secrets'}
          </button>
          <button
            onClick={() => setShowCreateModal(true)}
            className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg shadow-sm transition-colors"
          >
            + New Environment
          </button>
        </div>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm">
          {error}
        </div>
      )}

      {/* Environment List */}
      {loading && environments.length === 0 ? (
        <div className="text-center py-20 text-slate-400 animate-pulse">
          <div className="text-4xl mb-3">🌐</div>
          <p>Loading environments and variables…</p>
        </div>
      ) : environments.length === 0 ? (
        <div className="text-center py-16 bg-white border border-slate-200 rounded-2xl shadow-sm text-slate-400">
          <div className="text-4xl mb-3">🔌</div>
          <p className="text-base font-semibold text-slate-700">No environments registered</p>
          <p className="text-xs text-slate-400 mt-1">Create your first environment to set up endpoints and secrets.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {environments.map(env => (
            <div
              key={env.id}
              className={`bg-white rounded-2xl p-6 border transition-all ${
                env.active ? 'border-slate-200 shadow-sm' : 'border-slate-200/50 opacity-60 bg-slate-50'
              }`}
            >
              <div className="flex items-start justify-between gap-3 mb-4">
                <div>
                  <div className="flex items-center gap-2">
                    <h2 className="text-lg font-bold text-slate-900">{env.name}</h2>
                    <span
                      className={`text-[11px] px-2 py-0.5 rounded-full font-bold border ${
                        CODE_BADGES[env.code] || 'bg-slate-100 text-slate-700'
                      }`}
                    >
                      {env.code}
                    </span>
                    <span className="text-xs text-slate-400">({env.apiName})</span>
                  </div>
                  <code className="text-xs text-indigo-600 bg-indigo-50 px-2 py-1 rounded font-mono mt-1 inline-block">
                    {env.baseUrl}
                  </code>
                </div>
                <button
                  onClick={() => handleToggleStatus(env.id)}
                  className={`text-xs px-2.5 py-1 rounded-full font-semibold border ${
                    env.active
                      ? 'bg-emerald-50 text-emerald-700 border-emerald-300'
                      : 'bg-slate-200 text-slate-600 border-slate-300'
                  }`}
                >
                  {env.active ? 'Active' : 'Disabled'}
                </button>
              </div>

              {env.description && (
                <p className="text-xs text-slate-500 mb-4">{env.description}</p>
              )}

              {/* Variables Section */}
              <div className="border-t border-slate-100 pt-4">
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-semibold text-slate-600 uppercase">
                    Variables ({env.variables.length})
                  </span>
                  <button
                    onClick={() => setSelectedEnvId(selectedEnvId === env.id ? null : env.id)}
                    className="text-xs text-indigo-600 hover:text-indigo-800 font-medium"
                  >
                    {selectedEnvId === env.id ? '✕ Close' : '+ Add Variable'}
                  </button>
                </div>

                {/* Variable Inline Form */}
                {selectedEnvId === env.id && (
                  <form onSubmit={handleAddVariable} className="p-3 bg-slate-50 rounded-xl mb-3 space-y-2 border border-slate-200">
                    <div className="grid grid-cols-2 gap-2">
                      <input
                        type="text"
                        required
                        placeholder="KEY_NAME"
                        value={varKey}
                        onChange={e => setVarKey(e.target.value)}
                        className="text-xs border border-slate-300 rounded px-2.5 py-1.5 font-mono focus:outline-none"
                      />
                      <input
                        type="text"
                        placeholder="Value..."
                        value={varValue}
                        onChange={e => setVarValue(e.target.value)}
                        className="text-xs border border-slate-300 rounded px-2.5 py-1.5 focus:outline-none"
                      />
                    </div>
                    <div className="flex items-center justify-between">
                      <label className="flex items-center gap-1.5 text-xs text-slate-600">
                        <input
                          type="checkbox"
                          checked={varSecret}
                          onChange={e => setVarSecret(e.target.checked)}
                          className="rounded text-indigo-600"
                        />
                        Mark as Secret (Mask value)
                      </label>
                      <button
                        type="submit"
                        disabled={varSubmitting}
                        className="text-xs bg-slate-900 text-white px-3 py-1 rounded font-medium disabled:opacity-50"
                      >
                        {varSubmitting ? 'Saving…' : 'Save'}
                      </button>
                    </div>
                  </form>
                )}

                {/* Variable List */}
                <div className="space-y-1.5 max-h-40 overflow-y-auto">
                  {env.variables.length === 0 ? (
                    <p className="text-xs text-slate-400 italic">No variables defined</p>
                  ) : (
                    env.variables.map((v, idx) => (
                      <div
                        key={v.id || idx}
                        className="flex items-center justify-between text-xs py-1 px-2.5 rounded bg-slate-50 font-mono"
                      >
                        <span className="font-semibold text-slate-700">{v.key}</span>
                        <div className="flex items-center gap-2">
                          <span className={v.secret ? 'text-amber-600' : 'text-slate-600'}>
                            {v.value}
                          </span>
                          {v.secret && (
                            <span className="text-[10px] bg-amber-100 text-amber-800 px-1.5 py-0.5 rounded font-sans font-semibold">
                              SECRET
                            </span>
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-xl border border-slate-200 space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">Add New Environment</h2>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-slate-400 hover:text-slate-600 text-sm font-semibold"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleCreateEnv} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Environment Name *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. EU Production Gateway"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Code *</label>
                  <select
                    value={code}
                    onChange={e => setCode(e.target.value)}
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  >
                    <option value="DEV">DEV</option>
                    <option value="STAGING">STAGING</option>
                    <option value="PRODUCTION">PRODUCTION</option>
                    <option value="SANDBOX">SANDBOX</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Scope API ID</label>
                  <input
                    type="number"
                    placeholder="Blank for Global"
                    value={apiId}
                    onChange={e => setApiId(e.target.value)}
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Base URL *</label>
                <input
                  type="url"
                  required
                  placeholder="https://api.domain.com"
                  value={baseUrl}
                  onChange={e => setBaseUrl(e.target.value)}
                  className="w-full text-sm font-mono border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Description</label>
                <textarea
                  rows={2}
                  placeholder="Optional notes or cluster architecture details..."
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 text-sm text-slate-600 hover:text-slate-800 font-medium"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg disabled:opacity-50 shadow-sm"
                >
                  {submitting ? 'Creating…' : 'Create Environment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
