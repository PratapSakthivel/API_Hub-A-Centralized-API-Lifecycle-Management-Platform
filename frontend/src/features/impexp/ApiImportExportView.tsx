import { useState } from 'react';
import api from '../../lib/api';

interface ImportResult {
  apiId: number;
  apiName: string;
  slug: string;
  versionId: number;
  versionLabel: string;
  endpointsImported: number;
  message: string;
}

const SAMPLE_SPEC = `{
  "openapi": "3.0.3",
  "info": {
    "title": "Customer Loyalty API",
    "description": "Manages points, rewards, and customer tiers.",
    "version": "1.0.0"
  },
  "paths": {
    "/customers/{id}/rewards": {
      "get": {
        "summary": "Get Customer Rewards",
        "description": "Retrieve current reward points balance and tier status",
        "operationId": "getCustomerRewards",
        "responses": {
          "200": { "description": "Rewards list returned" }
        }
      },
      "post": {
        "summary": "Redeem Reward",
        "description": "Deducts loyalty points to redeem an item",
        "operationId": "redeemReward",
        "responses": {
          "201": { "description": "Reward claimed successfully" }
        }
      }
    }
  }
}`;

export default function ApiImportExportView() {
  const [activeTab, setActiveTab] = useState<'import' | 'export'>('import');

  // Import State
  const [projectId, setProjectId] = useState<string>('');
  const [slug, setSlug] = useState<string>('');
  const [specContent, setSpecContent] = useState<string>('');
  const [importing, setImporting] = useState<boolean>(false);
  const [importResult, setImportResult] = useState<ImportResult | null>(null);
  const [importError, setImportError] = useState<string | null>(null);

  // Export State
  const [exportVersionId, setExportVersionId] = useState<string>('');
  const [exporting, setExporting] = useState<boolean>(false);
  const [exportedJson, setExportedJson] = useState<string | null>(null);
  const [exportError, setExportError] = useState<string | null>(null);
  const [copied, setCopied] = useState<boolean>(false);

  const handleImport = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!specContent.trim()) {
      setImportError('Please enter or paste an OpenAPI JSON specification.');
      return;
    }
    setImporting(true);
    setImportError(null);
    setImportResult(null);

    try {
      const res = await api.post<{ data: ImportResult }>('/apis/import', {
        projectId: projectId ? Number(projectId) : undefined,
        slug: slug.trim() || undefined,
        specContent: specContent.trim(),
      });
      const data = (res.data as { data?: ImportResult }).data || (res.data as unknown as ImportResult);
      setImportResult(data);
    } catch {
      setImportError('Failed to import specification. Verify that JSON is well-formed OpenAPI 3.x.');
    } finally {
      setImporting(false);
    }
  };

  const handleExport = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!exportVersionId.trim()) return;

    setExporting(true);
    setExportError(null);
    setExportedJson(null);
    setCopied(false);

    try {
      const res = await api.get<{ data?: { spec: string }; spec?: string }>(
        `/versions/${exportVersionId}/export/preview`
      );
      const content = res.data?.data?.spec || res.data?.spec || JSON.stringify(res.data, null, 2);
      setExportedJson(content);
    } catch {
      setExportError('Failed to export specification. Verify the Version ID exists.');
    } finally {
      setExporting(false);
    }
  };

  const handleCopy = () => {
    if (exportedJson) {
      navigator.clipboard.writeText(exportedJson);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleDownload = () => {
    if (!exportedJson) return;
    const blob = new Blob([exportedJson], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `openapi-version-${exportVersionId}.json`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="p-8 max-w-6xl mx-auto space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold tracking-tight text-slate-900">OpenAPI Import & Export</h1>
        <p className="text-slate-500 mt-1">
          Seamlessly ingest industry-standard OpenAPI 3.x specifications or export versioned contracts.
        </p>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 gap-4">
        <button
          onClick={() => setActiveTab('import')}
          className={`pb-3 text-sm font-semibold transition-colors border-b-2 ${
            activeTab === 'import'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          📥 Import Specification
        </button>
        <button
          onClick={() => setActiveTab('export')}
          className={`pb-3 text-sm font-semibold transition-colors border-b-2 ${
            activeTab === 'export'
              ? 'border-indigo-600 text-indigo-600'
              : 'border-transparent text-slate-500 hover:text-slate-800'
          }`}
        >
          📤 Export Specification
        </button>
      </div>

      {/* Tab: Import */}
      {activeTab === 'import' && (
        <div className="space-y-6">
          <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm space-y-4">
            <div className="flex justify-between items-center">
              <h2 className="text-lg font-bold text-slate-900">Ingest OpenAPI 3.x Spec</h2>
              <button
                type="button"
                onClick={() => setSpecContent(SAMPLE_SPEC)}
                className="text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1.5 rounded-lg font-medium transition-colors"
              >
                Load Sample JSON
              </button>
            </div>

            <form onSubmit={handleImport} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Project ID <span className="font-normal text-slate-400">(Optional - defaults to first)</span>
                  </label>
                  <input
                    type="number"
                    value={projectId}
                    onChange={e => setProjectId(e.target.value)}
                    placeholder="e.g. 1"
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">
                    Custom Slug <span className="font-normal text-slate-400">(Optional - auto-generated)</span>
                  </label>
                  <input
                    type="text"
                    value={slug}
                    onChange={e => setSlug(e.target.value)}
                    placeholder="e.g. loyalty-api"
                    className="w-full text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  OpenAPI JSON Specification <span className="text-rose-500">*</span>
                </label>
                <textarea
                  rows={14}
                  required
                  value={specContent}
                  onChange={e => setSpecContent(e.target.value)}
                  placeholder="Paste OpenAPI 3.x JSON specification here..."
                  className="w-full font-mono text-xs border border-slate-300 rounded-xl p-4 focus:ring-2 focus:ring-indigo-400 focus:outline-none bg-slate-50/50"
                />
              </div>

              {importError && (
                <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">
                  {importError}
                </div>
              )}

              <button
                type="submit"
                disabled={importing}
                className="w-full sm:w-auto px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium text-sm rounded-lg shadow-sm disabled:opacity-50 transition-colors"
              >
                {importing ? 'Parsing and Importing…' : '🚀 Ingest and Register API'}
              </button>
            </form>
          </div>

          {importResult && (
            <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-6 text-emerald-900 shadow-sm animate-fade-in">
              <div className="flex items-center gap-3 mb-3">
                <span className="text-2xl">🎉</span>
                <h3 className="text-lg font-bold">API Ingestion Successful!</h3>
              </div>
              <p className="text-xs text-emerald-700 mb-4">{importResult.message}</p>
              <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 bg-white/70 p-4 rounded-xl border border-emerald-200">
                <div>
                  <span className="text-xs text-slate-400 uppercase font-semibold">API Name</span>
                  <p className="font-bold text-slate-800 text-sm">{importResult.apiName}</p>
                </div>
                <div>
                  <span className="text-xs text-slate-400 uppercase font-semibold">API ID</span>
                  <p className="font-bold text-slate-800 text-sm">#{importResult.apiId}</p>
                </div>
                <div>
                  <span className="text-xs text-slate-400 uppercase font-semibold">Version</span>
                  <p className="font-bold text-slate-800 text-sm">{importResult.versionLabel}</p>
                </div>
                <div>
                  <span className="text-xs text-slate-400 uppercase font-semibold">Endpoints Created</span>
                  <p className="font-bold text-emerald-600 text-sm">{importResult.endpointsImported}</p>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Tab: Export */}
      {activeTab === 'export' && (
        <div className="space-y-6">
          <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm space-y-4">
            <h2 className="text-lg font-bold text-slate-900">Export Version as OpenAPI 3.0</h2>
            <form onSubmit={handleExport} className="flex flex-wrap items-end gap-3">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Target Version ID <span className="text-rose-500">*</span>
                </label>
                <input
                  type="number"
                  required
                  value={exportVersionId}
                  onChange={e => setExportVersionId(e.target.value)}
                  placeholder="e.g. 1"
                  className="w-48 text-sm border border-slate-300 rounded-lg px-3.5 py-2 focus:ring-2 focus:ring-indigo-400 focus:outline-none"
                />
              </div>
              <button
                type="submit"
                disabled={exporting || !exportVersionId}
                className="px-6 py-2 bg-slate-900 hover:bg-slate-800 text-white font-medium text-sm rounded-lg shadow-sm disabled:opacity-50 transition-colors"
              >
                {exporting ? 'Generating Spec…' : 'Generate OpenAPI 3.0'}
              </button>
            </form>

            {exportError && (
              <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-lg">
                {exportError}
              </div>
            )}
          </div>

          {exportedJson && (
            <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-sm space-y-4">
              <div className="flex justify-between items-center">
                <h3 className="text-sm font-bold text-slate-800">Generated OpenAPI 3.0 JSON</h3>
                <div className="flex gap-2">
                  <button
                    onClick={handleCopy}
                    className="text-xs bg-slate-100 hover:bg-slate-200 text-slate-700 px-3 py-1.5 rounded-lg font-medium transition-colors"
                  >
                    {copied ? '✓ Copied!' : '📋 Copy JSON'}
                  </button>
                  <button
                    onClick={handleDownload}
                    className="text-xs bg-indigo-600 hover:bg-indigo-700 text-white px-3 py-1.5 rounded-lg font-medium transition-colors shadow-sm"
                  >
                    ⬇ Download .json
                  </button>
                </div>
              </div>
              <pre className="max-h-96 overflow-y-auto bg-slate-900 text-emerald-400 p-4 rounded-xl font-mono text-xs leading-relaxed">
                {exportedJson}
              </pre>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
