import { useEffect, useState } from 'react';
import api from '../../lib/api';

interface RecentActivity {
  id: number;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  details: string;
  timestamp: string;
}

interface DashboardSummary {
  totalApis: number;
  totalProjects: number;
  totalUsers: number;
  totalTestSuites: number;
  totalTestRuns: number;
  passedTestRuns: number;
  failedTestRuns: number;
  testPassRatePercentage: number;
  apisByLifecycleStatus: Record<string, number>;
  releasesByStage: Record<string, number>;
  recentActivity: RecentActivity[];
}

const STATUS_COLORS: Record<string, string> = {
  PLANNING: 'bg-gray-100 text-gray-700 border-gray-300',
  DRAFT: 'bg-amber-100 text-amber-800 border-amber-300',
  ACTIVE: 'bg-emerald-100 text-emerald-800 border-emerald-300',
  DEPRECATED: 'bg-orange-100 text-orange-800 border-orange-300',
  RETIRED: 'bg-rose-100 text-rose-800 border-rose-300',
};

const STAGE_COLORS: Record<string, string> = {
  DEVELOPMENT: 'bg-sky-500',
  TESTING: 'bg-amber-500',
  STAGING: 'bg-violet-500',
  PRODUCTION: 'bg-emerald-500',
};

export default function DashboardView() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSummary = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<DashboardSummary>('/dashboard/summary');
      setSummary(res.data);
    } catch {
      setError('Unable to load dashboard metrics. Ensure backend services are reachable.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSummary();
  }, []);

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-8">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Portfolio Dashboard</h1>
          <p className="text-slate-500 mt-1">
            Real-time analytics across APIs, release stages, automated test pass rates, and audit events.
          </p>
        </div>
        <button
          onClick={fetchSummary}
          disabled={loading}
          className="inline-flex items-center justify-center px-4 py-2 border border-slate-300 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 disabled:opacity-50 transition-colors shadow-sm"
        >
          {loading ? 'Refreshing…' : '↻ Refresh Data'}
        </button>
      </div>

      {error && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm">
          {error}
        </div>
      )}

      {loading && !summary && (
        <div className="text-center py-20 text-slate-400 animate-pulse">
          <div className="text-4xl mb-3">📊</div>
          <p>Aggregating platform metrics…</p>
        </div>
      )}

      {summary && (
        <>
          {/* Top KPI Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm hover:shadow transition-shadow">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Total APIs</span>
              <div className="mt-2 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-slate-900">{summary.totalApis}</span>
                <span className="text-xs px-2 py-1 rounded bg-indigo-50 text-indigo-700 font-semibold">Catalog</span>
              </div>
              <p className="text-xs text-slate-500 mt-3">{summary.totalProjects} Active Projects</p>
            </div>

            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm hover:shadow transition-shadow">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Test Pass Rate</span>
              <div className="mt-2 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-emerald-600">
                  {summary.testPassRatePercentage}%
                </span>
                <span className="text-xs px-2 py-1 rounded bg-emerald-50 text-emerald-700 font-semibold">Quality</span>
              </div>
              <p className="text-xs text-slate-500 mt-3">
                {summary.passedTestRuns} of {summary.totalTestRuns} runs passed
              </p>
            </div>

            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm hover:shadow transition-shadow">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Test Suites</span>
              <div className="mt-2 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-slate-900">{summary.totalTestSuites}</span>
                <span className="text-xs px-2 py-1 rounded bg-amber-50 text-amber-700 font-semibold">Automation</span>
              </div>
              <p className="text-xs text-slate-500 mt-3">{summary.failedTestRuns} failed executions</p>
            </div>

            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm hover:shadow transition-shadow">
              <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Platform Users</span>
              <div className="mt-2 flex items-baseline justify-between">
                <span className="text-3xl font-extrabold text-slate-900">{summary.totalUsers}</span>
                <span className="text-xs px-2 py-1 rounded bg-violet-50 text-violet-700 font-semibold">RBAC</span>
              </div>
              <p className="text-xs text-slate-500 mt-3">Active Contributors</p>
            </div>
          </div>

          {/* Middle Row: Lifecycle Distribution & Release Pipeline */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* API Lifecycle Distribution */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm">
              <h2 className="text-lg font-bold text-slate-900">API Lifecycle Distribution</h2>
              <p className="text-xs text-slate-500 mt-0.5 mb-6">Status breakdown across all cataloged interfaces</p>

              <div className="space-y-4">
                {Object.entries(summary.apisByLifecycleStatus).map(([status, count]) => {
                  const percentage = summary.totalApis > 0
                    ? Math.round((count / summary.totalApis) * 100)
                    : 0;
                  return (
                    <div key={status} className="space-y-1.5">
                      <div className="flex justify-between items-center text-xs">
                        <span className={`px-2 py-0.5 rounded font-medium border ${STATUS_COLORS[status] || 'bg-slate-100 text-slate-700'}`}>
                          {status}
                        </span>
                        <span className="font-semibold text-slate-700">
                          {count} <span className="text-slate-400 font-normal">({percentage}%)</span>
                        </span>
                      </div>
                      <div className="w-full bg-slate-100 rounded-full h-2 overflow-hidden">
                        <div
                          className="bg-indigo-600 h-2 rounded-full transition-all duration-500"
                          style={{ width: `${percentage}%` }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Release Pipeline Stages */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm">
              <h2 className="text-lg font-bold text-slate-900">Release Promotion Stages</h2>
              <p className="text-xs text-slate-500 mt-0.5 mb-6">Current deployment stage of governed releases</p>

              <div className="grid grid-cols-2 gap-4">
                {Object.entries(summary.releasesByStage).map(([stage, count]) => (
                  <div key={stage} className="p-4 rounded-xl border border-slate-200 bg-slate-50/50 flex flex-col justify-between">
                    <div className="flex items-center gap-2">
                      <span className={`w-2.5 h-2.5 rounded-full ${STAGE_COLORS[stage] || 'bg-slate-400'}`} />
                      <span className="text-xs font-semibold text-slate-700">{stage}</span>
                    </div>
                    <div className="mt-4">
                      <span className="text-2xl font-bold text-slate-900">{count}</span>
                      <span className="text-xs text-slate-400 ml-1">releases</span>
                    </div>
                  </div>
                ))}
              </div>

              {/* Testing Health Progress Bar */}
              <div className="mt-6 p-4 rounded-xl bg-slate-50 border border-slate-200">
                <div className="flex justify-between text-xs font-medium mb-1">
                  <span className="text-slate-700">Testing Pass Ratio</span>
                  <span className="text-emerald-600 font-bold">{summary.testPassRatePercentage}%</span>
                </div>
                <div className="w-full bg-slate-200 rounded-full h-2.5 overflow-hidden">
                  <div
                    className="bg-emerald-500 h-2.5 rounded-full transition-all duration-500"
                    style={{ width: `${summary.testPassRatePercentage}%` }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Bottom Section: Recent Audit Feed */}
          <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm">
            <h2 className="text-lg font-bold text-slate-900">Recent Platform Activity</h2>
            <p className="text-xs text-slate-500 mt-0.5 mb-4">Latest audited events and administrative actions</p>

            {summary.recentActivity.length === 0 ? (
              <p className="text-sm text-slate-400 text-center py-6">No recent audit events recorded.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs text-slate-600">
                  <thead className="bg-slate-50 text-slate-700 font-semibold border-b border-slate-200">
                    <tr>
                      <th className="py-2.5 px-3">Timestamp</th>
                      <th className="py-2.5 px-3">Actor</th>
                      <th className="py-2.5 px-3">Action</th>
                      <th className="py-2.5 px-3">Entity</th>
                      <th className="py-2.5 px-3">Details</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {summary.recentActivity.map(act => (
                      <tr key={act.id} className="hover:bg-slate-50/70 transition-colors">
                        <td className="py-2.5 px-3 whitespace-nowrap text-slate-400">
                          {act.timestamp ? new Date(act.timestamp).toLocaleString() : '—'}
                        </td>
                        <td className="py-2.5 px-3 font-medium text-slate-800">{act.actorEmail}</td>
                        <td className="py-2.5 px-3">
                          <span className="px-2 py-0.5 rounded bg-indigo-50 text-indigo-700 font-semibold text-[11px]">
                            {act.action}
                          </span>
                        </td>
                        <td className="py-2.5 px-3 text-slate-500">
                          {act.entityType} #{act.entityId}
                        </td>
                        <td className="py-2.5 px-3 text-slate-500 max-w-xs truncate">{act.details || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
