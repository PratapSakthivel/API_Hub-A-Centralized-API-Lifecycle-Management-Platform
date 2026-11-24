import { useEffect, useState } from 'react';
import api from './lib/api';
import ApiCatalog from './features/catalog/ApiCatalog';
import { Activity, ShieldCheck, Layers, Terminal, BookOpen, GitMerge, RefreshCw } from 'lucide-react';

interface HealthData {
  status: string;
  service: string;
  version: string;
  lifecycle: string;
}

export default function App() {
  const [health, setHealth] = useState<HealthData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchHealth = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/health');
      if (res.data?.success) {
        setHealth(res.data.data);
      } else {
        setError('Backend reported an unhealthy state');
      }
    } catch (err: any) {
      setError(err?.message || 'Unable to connect to backend API');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHealth();
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* Top Navigation Bar */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-200">
              <Layers className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xl font-bold bg-gradient-to-r from-indigo-600 to-indigo-900 bg-clip-text text-transparent">
                API Hub
              </span>
              <span className="block text-xs text-slate-500 font-medium">Lifecycle Management Platform</span>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800">
              v1.0.0-foundation
            </span>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 w-full space-y-8">
        {/* Welcome Hero */}
        <section className="bg-white rounded-2xl border border-slate-200 p-8 shadow-sm">
          <div className="max-w-3xl">
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight sm:text-4xl">
              Centralized API Lifecycle Management
            </h1>
            <p className="mt-4 text-base text-slate-600 leading-relaxed">
              Plan, catalog, document, test, version, govern, and release enterprise APIs from a single consolidated dashboard.
            </p>
          </div>

          {/* Backend Connection Health Badge */}
          <div className="mt-6 pt-6 border-t border-slate-100 flex flex-wrap items-center justify-between gap-4">
            <div className="flex items-center space-x-3">
              <div className="p-2 bg-slate-100 rounded-lg text-slate-600">
                <Activity className="w-5 h-5" />
              </div>
              <div>
                <div className="text-xs uppercase tracking-wider text-slate-500 font-semibold">Backend Status</div>
                <div className="flex items-center space-x-2 mt-0.5">
                  {loading && <span className="text-sm font-medium text-slate-500">Checking health...</span>}
                  {health && (
                    <span className="inline-flex items-center text-sm font-semibold text-emerald-600">
                      <span className="w-2 h-2 rounded-full bg-emerald-500 mr-1.5 animate-pulse"></span>
                      Connected ({health.service} - {health.status})
                    </span>
                  )}
                  {error && (
                    <span className="inline-flex items-center text-sm font-semibold text-amber-600">
                      <span className="w-2 h-2 rounded-full bg-amber-500 mr-1.5"></span>
                      Offline / Disconnected ({error})
                    </span>
                  )}
                </div>
              </div>
            </div>

            <button
              onClick={fetchHealth}
              disabled={loading}
              className="inline-flex items-center px-3.5 py-2 border border-slate-300 text-xs font-semibold rounded-lg text-slate-700 bg-white hover:bg-slate-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition-colors shadow-sm disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${loading ? 'animate-spin' : ''}`} />
              Test Connection
            </button>
          </div>
        </section>

        {/* Catalog Section */}
        <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <ApiCatalog />
        </section>

        {/* Core Capabilities Grid */}
        <section>
          <h2 className="text-lg font-bold text-slate-900 mb-4">Core Lifecycle Modules</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-indigo-50 flex items-center justify-center text-indigo-600 mb-4">
                <Layers className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">API Catalog</h3>
              <p className="mt-2 text-sm text-slate-600">Register, organize, search and filter APIs grouped by organizational projects.</p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-emerald-50 flex items-center justify-center text-emerald-600 mb-4">
                <BookOpen className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">Documentation & OpenAPI</h3>
              <p className="mt-2 text-sm text-slate-600">Maintain structured endpoints, schemas, parameters and OpenAPI 3.x specifications.</p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-purple-50 flex items-center justify-center text-purple-600 mb-4">
                <Terminal className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">API Testing & SSRF Guard</h3>
              <p className="mt-2 text-sm text-slate-600">Execute automated test cases with response validation and safe outbound HTTP policies.</p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 mb-4">
                <GitMerge className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">Dependency Mapping</h3>
              <p className="mt-2 text-sm text-slate-600">Visualize directional relationships and calculate downstream change impact.</p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-amber-50 flex items-center justify-center text-amber-600 mb-4">
                <ShieldCheck className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">RBAC & Governance</h3>
              <p className="mt-2 text-sm text-slate-600">5-role access policy (Admin, Developer, QA, PM, Consumer) with release promotions.</p>
            </div>

            <div className="p-6 bg-white rounded-xl border border-slate-200 shadow-sm hover:border-indigo-300 transition-colors">
              <div className="w-10 h-10 rounded-lg bg-rose-50 flex items-center justify-center text-rose-600 mb-4">
                <Activity className="w-5 h-5" />
              </div>
              <h3 className="font-semibold text-slate-900 text-base">Portfolio Dashboard</h3>
              <p className="mt-2 text-sm text-slate-600">Metrics aggregated directly from live database records across all API stages.</p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 mt-auto">
        <div className="max-w-7xl mx-auto px-4 text-center text-xs text-slate-500">
          API Hub – A Centralized API Lifecycle Management Platform &copy; {new Date().getFullYear()}
        </div>
      </footer>
    </div>
  );
}
