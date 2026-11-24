import { useState, useEffect } from 'react';
import api from '../../lib/api';
import { GitBranch, Plus, ArrowRight } from 'lucide-react';

interface VersionItem {
  id: number;
  apiId: number;
  apiName: string;
  versionLabel: string;
  status: string;
  changeSummary: string;
  publishedAt: string | null;
  createdAt: string;
}

interface VersionComparison {
  fromVersionLabel: string;
  toVersionLabel: string;
  addedEndpoints: any[];
  removedEndpoints: any[];
  modifiedEndpoints: any[];
}

export default function VersionManagement({ apiId = 1 }: { apiId?: number }) {
  const [versions, setVersions] = useState<VersionItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [versionLabel, setVersionLabel] = useState('');
  const [changeSummary, setChangeSummary] = useState('');

  // Comparison State
  const [compareFrom, setCompareFrom] = useState<number | ''>('');
  const [compareTo, setCompareTo] = useState<number | ''>('');
  const [diff, setDiff] = useState<VersionComparison | null>(null);

  const fetchVersions = async () => {
    setLoading(true);
    try {
      const res = await api.get(`/apis/${apiId}/versions`);
      if (res.data?.success) {
        setVersions(res.data.data || []);
      }
    } catch {
      // Intact
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (apiId) fetchVersions();
  }, [apiId]);

  const handleCreateVersion = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post(`/apis/${apiId}/versions`, {
        versionLabel,
        changeSummary,
      });
      if (res.data?.success) {
        setShowCreateModal(false);
        setVersionLabel('');
        setChangeSummary('');
        fetchVersions();
      }
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to create version');
    }
  };

  const handlePublish = async (id: number) => {
    if (!confirm('Publishing will make this API version immutable. Proceed?')) return;
    try {
      await api.post(`/versions/${id}/publish`);
      fetchVersions();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to publish');
    }
  };

  const handleLifecycle = async (id: number, status: string) => {
    try {
      await api.patch(`/versions/${id}/lifecycle?status=${status}`);
      fetchVersions();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to update status');
    }
  };

  const handleCompare = async () => {
    if (!compareFrom || !compareTo) return;
    try {
      const res = await api.get(`/versions/compare?fromId=${compareFrom}&toId=${compareTo}`);
      if (res.data?.success) {
        setDiff(res.data.data);
      }
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to compare');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h3 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            <GitBranch className="w-5 h-5 text-indigo-600" />
            API Version Lifecycle
          </h3>
          <p className="text-xs text-slate-500 mt-1">
            Maintain semantic versions (MAJOR.MINOR.PATCH) without silently overwriting history.
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center px-3.5 py-2 rounded-lg bg-indigo-600 text-white font-medium text-xs hover:bg-indigo-700 shadow-sm"
        >
          <Plus className="w-3.5 h-3.5 mr-1.5" />
          Create Draft Version
        </button>
      </div>

      {/* Versions List */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 border-b border-slate-200 text-slate-600 font-semibold uppercase tracking-wider">
            <tr>
              <th className="p-4">Version</th>
              <th className="p-4">Status</th>
              <th className="p-4">Summary</th>
              <th className="p-4">Published Date</th>
              <th className="p-4 text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {loading ? (
              <tr>
                <td colSpan={5} className="p-6 text-center text-slate-500">Loading versions...</td>
              </tr>
            ) : versions.length === 0 ? (
              <tr>
                <td colSpan={5} className="p-6 text-center text-slate-500">No versions defined yet.</td>
              </tr>
            ) : (
              versions.map((v) => (
                <tr key={v.id} className="hover:bg-slate-50/50">
                  <td className="p-4 font-mono font-bold text-slate-900">{v.versionLabel}</td>
                  <td className="p-4">
                    <span
                      className={`px-2 py-0.5 rounded-full text-[11px] font-semibold ${
                        v.status === 'PUBLISHED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : v.status === 'DEPRECATED'
                          ? 'bg-amber-100 text-amber-800'
                          : v.status === 'RETIRED'
                          ? 'bg-rose-100 text-rose-800'
                          : 'bg-slate-100 text-slate-800'
                      }`}
                    >
                      {v.status}
                    </span>
                  </td>
                  <td className="p-4 text-slate-600 max-w-xs truncate">{v.changeSummary || '—'}</td>
                  <td className="p-4 text-slate-500">
                    {v.publishedAt ? new Date(v.publishedAt).toLocaleDateString() : 'Not published'}
                  </td>
                  <td className="p-4 text-right space-x-2">
                    {v.status === 'DRAFT' && (
                      <button
                        onClick={() => handlePublish(v.id)}
                        className="px-2.5 py-1 rounded bg-emerald-600 text-white font-medium text-[11px] hover:bg-emerald-700"
                      >
                        Publish
                      </button>
                    )}
                    {v.status === 'PUBLISHED' && (
                      <button
                        onClick={() => handleLifecycle(v.id, 'DEPRECATED')}
                        className="px-2.5 py-1 rounded bg-amber-500 text-white font-medium text-[11px] hover:bg-amber-600"
                      >
                        Deprecate
                      </button>
                    )}
                    {v.status === 'DEPRECATED' && (
                      <button
                        onClick={() => handleLifecycle(v.id, 'RETIRED')}
                        className="px-2.5 py-1 rounded bg-rose-600 text-white font-medium text-[11px] hover:bg-rose-700"
                      >
                        Retire
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Comparison Engine */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-4">
        <h4 className="text-sm font-bold text-slate-900 flex items-center gap-1.5">
          <ArrowRight className="w-4 h-4 text-indigo-600" />
          Compare Version Changes
        </h4>
        <div className="flex flex-wrap items-center gap-3">
          <select
            value={compareFrom}
            onChange={(e) => setCompareFrom(Number(e.target.value))}
            className="border border-slate-300 rounded-lg px-3 py-1.5 text-xs bg-white"
          >
            <option value="">Base Version...</option>
            {versions.map((v) => (
              <option key={v.id} value={v.id}>{v.versionLabel} ({v.status})</option>
            ))}
          </select>

          <span className="text-slate-400 font-bold">&rarr;</span>

          <select
            value={compareTo}
            onChange={(e) => setCompareTo(Number(e.target.value))}
            className="border border-slate-300 rounded-lg px-3 py-1.5 text-xs bg-white"
          >
            <option value="">Target Version...</option>
            {versions.map((v) => (
              <option key={v.id} value={v.id}>{v.versionLabel} ({v.status})</option>
            ))}
          </select>

          <button
            onClick={handleCompare}
            disabled={!compareFrom || !compareTo}
            className="px-3.5 py-1.5 rounded-lg bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700 disabled:opacity-50"
          >
            Compute Diff
          </button>
        </div>

        {diff && (
          <div className="mt-4 p-4 rounded-lg bg-slate-50 border border-slate-200 space-y-2 text-xs">
            <div className="font-semibold text-slate-800">
              Diff: <span className="font-mono text-indigo-600">{diff.fromVersionLabel}</span> &rarr; <span className="font-mono text-indigo-600">{diff.toVersionLabel}</span>
            </div>
            <div className="grid grid-cols-3 gap-3 pt-2 text-center">
              <div className="p-2.5 bg-emerald-50 text-emerald-800 rounded border border-emerald-200 font-medium">
                +{diff.addedEndpoints.length} Added Endpoints
              </div>
              <div className="p-2.5 bg-rose-50 text-rose-800 rounded border border-rose-200 font-medium">
                -{diff.removedEndpoints.length} Removed Endpoints
              </div>
              <div className="p-2.5 bg-amber-50 text-amber-800 rounded border border-amber-200 font-medium">
                ~{diff.modifiedEndpoints.length} Modified Endpoints
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-slate-200">
            <h3 className="text-base font-bold text-slate-900 mb-1">Create API Version</h3>
            <p className="text-xs text-slate-500 mb-4">Enter a semantic version tag and summary of changes.</p>
            <form onSubmit={handleCreateVersion} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Semantic Version Tag</label>
                <input
                  type="text"
                  placeholder="e.g. v1.1.0"
                  value={versionLabel}
                  onChange={(e) => setVersionLabel(e.target.value)}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2 text-xs font-mono"
                />
              </div>
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Change Summary</label>
                <textarea
                  placeholder="What changed in this version..."
                  value={changeSummary}
                  onChange={(e) => setChangeSummary(e.target.value)}
                  rows={3}
                  className="w-full border border-slate-300 rounded-lg p-2 text-xs"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-xs"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-3.5 py-1.5 rounded-lg bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700"
                >
                  Create Draft
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
