import { useState, useEffect } from 'react';
import api from '../../lib/api';
import { Layers, Plus, Search, Filter, Calendar, CheckCircle2 } from 'lucide-react';

interface ApiItem {
  id: number;
  projectId: number;
  projectName: string;
  projectKey: string;
  name: string;
  slug: string;
  summary: string;
  lifecycleStatus: string;
  ownerName: string;
  createdAt: string;
}

interface ProjectItem {
  id: number;
  name: string;
  projectKey: string;
}

export default function ApiCatalog() {
  const [apis, setApis] = useState<ApiItem[]>([]);
  const [projects, setProjects] = useState<ProjectItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [showModal, setShowModal] = useState<boolean>(false);

  // Form State
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [summary, setSummary] = useState('');
  const [projectId, setProjectId] = useState<number | ''>('');
  const [formMsg, setFormMsg] = useState<{ text: string; error: boolean } | null>(null);

  const fetchCatalog = async () => {
    setLoading(true);
    try {
      let url = `/apis?page=0&size=20`;
      if (searchTerm) url += `&q=${encodeURIComponent(searchTerm)}`;
      if (selectedStatus) url += `&status=${selectedStatus}`;

      const res = await api.get(url);
      if (res.data?.success) {
        setApis(res.data.data.content || []);
      }
    } catch {
      // Keep state intact
    } finally {
      setLoading(false);
    }
  };

  const fetchProjects = async () => {
    try {
      const res = await api.get('/projects');
      if (res.data?.success) {
        setProjects(res.data.data || []);
        if (res.data.data.length > 0 && !projectId) {
          setProjectId(res.data.data[0].id);
        }
      }
    } catch {
      // Fallback
    }
  };

  useEffect(() => {
    fetchCatalog();
    fetchProjects();
  }, [searchTerm, selectedStatus]);

  const handleCreateApi = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormMsg(null);
    try {
      const res = await api.post('/apis', {
        projectId,
        name,
        slug,
        summary,
        lifecycleStatus: 'DRAFT',
      });
      if (res.data?.success) {
        setFormMsg({ text: 'API successfully registered in catalog!', error: false });
        setName('');
        setSlug('');
        setSummary('');
        fetchCatalog();
        setTimeout(() => setShowModal(false), 1200);
      }
    } catch (err: any) {
      setFormMsg({
        text: err?.response?.data?.message || 'Failed to register API. Check inputs and slug uniqueness.',
        error: true,
      });
    }
  };

  return (
    <div className="space-y-6">
      {/* Title & Actions Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
            <Layers className="w-6 h-6 text-indigo-600" />
            API Catalog
          </h2>
          <p className="text-sm text-slate-500 mt-1">
            Discover, monitor, and manage services published across organizational teams.
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center px-4 py-2 rounded-lg bg-indigo-600 text-white font-medium text-sm hover:bg-indigo-700 shadow-sm transition-all focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2"
        >
          <Plus className="w-4 h-4 mr-1.5" />
          Register New API
        </button>
      </div>

      {/* Search & Filter Controls */}
      <div className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full md:w-96">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search APIs by name, slug or summary..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-slate-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>

        <div className="flex items-center gap-3 w-full md:w-auto">
          <Filter className="w-4 h-4 text-slate-400" />
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="border border-slate-300 rounded-lg px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="">All Lifecycle Stages</option>
            <option value="DRAFT">Draft</option>
            <option value="ACTIVE">Active / Published</option>
            <option value="DEPRECATED">Deprecated</option>
            <option value="RETIRED">Retired</option>
          </select>
        </div>
      </div>

      {/* Catalog List / Grid */}
      {loading ? (
        <div className="py-12 text-center text-slate-500 text-sm">Loading API catalog...</div>
      ) : apis.length === 0 ? (
        <div className="bg-white rounded-xl border border-dashed border-slate-300 p-12 text-center">
          <Layers className="w-10 h-10 text-slate-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-slate-800">No APIs Found</h3>
          <p className="text-sm text-slate-500 mt-1 max-w-sm mx-auto">
            Get started by registering your first service or adjust your active search and filters.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {apis.map((apiItem) => (
            <div
              key={apiItem.id}
              className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm hover:border-indigo-400 transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <span className="text-xs font-semibold px-2 py-0.5 rounded bg-indigo-50 text-indigo-700">
                    {apiItem.projectKey || 'CORE'}
                  </span>
                  <span
                    className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                      apiItem.lifecycleStatus === 'ACTIVE'
                        ? 'bg-emerald-100 text-emerald-800'
                        : apiItem.lifecycleStatus === 'DRAFT'
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-slate-100 text-slate-800'
                    }`}
                  >
                    {apiItem.lifecycleStatus}
                  </span>
                </div>

                <h3 className="text-base font-bold text-slate-900 mt-3">{apiItem.name}</h3>
                <p className="text-xs font-mono text-slate-400 mt-0.5">/{apiItem.slug}</p>
                <p className="text-sm text-slate-600 mt-2 line-clamp-2">
                  {apiItem.summary || 'No summary provided for this service.'}
                </p>
              </div>

              <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
                <span className="flex items-center gap-1">
                  <Calendar className="w-3.5 h-3.5" />
                  {new Date(apiItem.createdAt).toLocaleDateString()}
                </span>
                <span className="font-medium text-slate-700">{apiItem.ownerName || 'Engineering'}</span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Registration Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-lg w-full p-6 shadow-xl border border-slate-200">
            <h3 className="text-lg font-bold text-slate-900 mb-2">Register API</h3>
            <p className="text-xs text-slate-500 mb-4">
              Enter key metadata to register an API definition into the organizational catalog.
            </p>

            {formMsg && (
              <div
                className={`p-3 rounded-lg text-xs font-medium mb-4 flex items-center gap-2 ${
                  formMsg.error ? 'bg-rose-50 text-rose-700' : 'bg-emerald-50 text-emerald-700'
                }`}
              >
                {!formMsg.error && <CheckCircle2 className="w-4 h-4 shrink-0" />}
                {formMsg.text}
              </div>
            )}

            <form onSubmit={handleCreateApi} className="space-y-4">
              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Project</label>
                <select
                  value={projectId}
                  onChange={(e) => setProjectId(Number(e.target.value))}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                >
                  {projects.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name} ({p.projectKey})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">API Name</label>
                <input
                  type="text"
                  placeholder="e.g. Payment Gateway Service"
                  value={name}
                  onChange={(e) => {
                    setName(e.target.value);
                    if (!slug) setSlug(e.target.value.toLowerCase().replace(/[^a-z0-9]/g, '-'));
                  }}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Slug / Identifier</label>
                <input
                  type="text"
                  placeholder="e.g. payment-gateway"
                  value={slug}
                  onChange={(e) => setSlug(e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ''))}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-slate-700 mb-1">Summary</label>
                <textarea
                  placeholder="Short description of this API's purpose..."
                  value={summary}
                  onChange={(e) => setSummary(e.target.value)}
                  rows={3}
                  className="w-full border border-slate-300 rounded-lg p-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-slate-300 rounded-lg text-xs font-medium text-slate-700 hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-indigo-600 text-white text-xs font-semibold hover:bg-indigo-700"
                >
                  Submit & Register
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
