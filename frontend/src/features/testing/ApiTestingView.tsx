import { useState, useEffect } from 'react';
import api from '../../lib/api';
import { Play, Plus, CheckCircle2, XCircle, Clock } from 'lucide-react';

interface TestCaseItem {
  id: number;
  name: string;
  httpMethod: string;
  endpointUrl: string;
  expectedStatusCode: number;
}

interface TestSuiteItem {
  id: number;
  name: string;
  description: string;
}

interface TestRunItem {
  id: number;
  overallStatus: string;
  totalTests: number;
  passedTests: number;
  failedTests: number;
  durationMs: number;
  startedAt: string;
}

export default function ApiTestingView({ apiId = 1 }: { apiId?: number }) {
  const [, setSuites] = useState<TestSuiteItem[]>([]);
  const [selectedSuite, setSelectedSuite] = useState<TestSuiteItem | null>(null);
  const [cases, setCases] = useState<TestCaseItem[]>([]);
  const [runs, setRuns] = useState<TestRunItem[]>([]);
  const [running, setRunning] = useState(false);
  const [showCaseModal, setShowCaseModal] = useState(false);

  // New Case Form
  const [caseName, setCaseName] = useState('');
  const [method, setMethod] = useState('GET');
  const [url, setUrl] = useState('');
  const [expectedStatus, setExpectedStatus] = useState(200);

  const fetchSuites = async () => {
    try {
      const res = await api.get(`/apis/${apiId}/test-suites`);
      if (res.data?.success) {
        setSuites(res.data.data || []);
        if (res.data.data.length > 0 && !selectedSuite) {
          setSelectedSuite(res.data.data[0]);
        }
      }
    } catch {}
  };

  const fetchCases = async (suiteId: number) => {
    try {
      const res = await api.get(`/test-suites/${suiteId}/cases`);
      if (res.data?.success) {
        setCases(res.data.data || []);
      }
    } catch {}
  };

  const fetchRuns = async () => {
    try {
      const res = await api.get(`/apis/${apiId}/test-runs?size=5`);
      if (res.data?.success) {
        setRuns(res.data.data?.content || []);
      }
    } catch {}
  };

  useEffect(() => {
    if (apiId) {
      fetchSuites();
      fetchRuns();
    }
  }, [apiId]);

  useEffect(() => {
    if (selectedSuite) {
      fetchCases(selectedSuite.id);
    }
  }, [selectedSuite]);

  const handleRunSuite = async () => {
    if (!selectedSuite) return;
    setRunning(true);
    try {
      await api.post(`/test-suites/${selectedSuite.id}/run`);
      fetchRuns();
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Execution error');
    } finally {
      setRunning(false);
    }
  };

  const handleCreateCase = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSuite) return;
    try {
      await api.post('/test-cases', {
        testSuiteId: selectedSuite.id,
        name: caseName,
        httpMethod: method,
        endpointUrl: url,
        expectedStatusCode: expectedStatus,
      });
      setShowCaseModal(false);
      setCaseName('');
      setUrl('');
      fetchCases(selectedSuite.id);
    } catch (err: any) {
      alert(err?.response?.data?.message || 'Failed to create test case');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h3 className="text-xl font-bold text-slate-900 flex items-center gap-2">
            Automated API Testing & SSRF Guard
          </h3>
          <p className="text-xs text-slate-500 mt-1">
            Execute outbound HTTP tests with safe host validation, timeouts, and pass/fail assertion history.
          </p>
        </div>

        <div className="flex gap-2">
          <button
            onClick={() => setShowCaseModal(true)}
            disabled={!selectedSuite}
            className="inline-flex items-center px-3.5 py-2 rounded-lg bg-slate-100 text-slate-700 font-medium text-xs hover:bg-slate-200 disabled:opacity-50"
          >
            <Plus className="w-3.5 h-3.5 mr-1" />
            Add Test Case
          </button>
          <button
            onClick={handleRunSuite}
            disabled={!selectedSuite || running}
            className="inline-flex items-center px-4 py-2 rounded-lg bg-indigo-600 text-white font-medium text-xs hover:bg-indigo-700 shadow-sm disabled:opacity-50"
          >
            <Play className={`w-3.5 h-3.5 mr-1.5 ${running ? 'animate-spin' : ''}`} />
            Run Suite
          </button>
        </div>
      </div>

      {/* Test Cases Table */}
      <div className="bg-white rounded-xl border border-slate-200 overflow-hidden shadow-sm">
        <div className="p-4 border-b border-slate-100 flex items-center justify-between">
          <span className="text-xs font-bold text-slate-800 uppercase tracking-wider">
            Suite Cases ({cases.length})
          </span>
          <span className="text-xs text-slate-400">Destination checked against SSRF rules</span>
        </div>
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 text-slate-600 font-semibold border-b border-slate-200">
            <tr>
              <th className="p-3.5">Method</th>
              <th className="p-3.5">Test Case Name</th>
              <th className="p-3.5">Target Destination</th>
              <th className="p-3.5 text-right">Expected Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100">
            {cases.length === 0 ? (
              <tr>
                <td colSpan={4} className="p-6 text-center text-slate-500">No test cases configured in this suite.</td>
              </tr>
            ) : (
              cases.map((c) => (
                <tr key={c.id}>
                  <td className="p-3.5">
                    <span className="px-2 py-0.5 rounded font-mono font-bold text-[11px] bg-slate-100 text-slate-800">
                      {c.httpMethod}
                    </span>
                  </td>
                  <td className="p-3.5 font-medium text-slate-800">{c.name}</td>
                  <td className="p-3.5 font-mono text-slate-500 truncate max-w-sm">{c.endpointUrl}</td>
                  <td className="p-3.5 text-right font-mono font-bold text-slate-700">{c.expectedStatusCode}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Execution Runs History */}
      <div className="bg-white p-5 rounded-xl border border-slate-200 shadow-sm space-y-3">
        <h4 className="text-xs font-bold uppercase tracking-wider text-slate-700">Execution History</h4>
        <div className="space-y-2">
          {runs.length === 0 ? (
            <p className="text-xs text-slate-400">No executions recorded yet.</p>
          ) : (
            runs.map((r) => (
              <div key={r.id} className="p-3 rounded-lg bg-slate-50 border border-slate-200 flex items-center justify-between text-xs">
                <div className="flex items-center gap-2">
                  {r.overallStatus === 'PASSED' ? (
                    <CheckCircle2 className="w-4 h-4 text-emerald-600" />
                  ) : (
                    <XCircle className="w-4 h-4 text-rose-600" />
                  )}
                  <span className="font-semibold text-slate-800">Run #{r.id}</span>
                  <span className="text-slate-400">|</span>
                  <span className="text-slate-600">{r.passedTests}/{r.totalTests} passed</span>
                </div>
                <div className="flex items-center gap-4 text-slate-500">
                  <span className="flex items-center gap-1 font-mono">
                    <Clock className="w-3.5 h-3.5" /> {r.durationMs}ms
                  </span>
                  <span>{new Date(r.startedAt).toLocaleTimeString()}</span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Modal */}
      {showCaseModal && (
        <div className="fixed inset-0 bg-slate-900/50 backdrop-blur-xs flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-slate-200">
            <h3 className="text-base font-bold text-slate-900 mb-1">Add Safe Test Case</h3>
            <p className="text-xs text-slate-500 mb-4">Internal IP destinations and blocked schemes are guarded against SSRF.</p>
            <form onSubmit={handleCreateCase} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-slate-700 mb-1">Test Name</label>
                <input
                  type="text"
                  placeholder="e.g. Verify health endpoint response"
                  value={caseName}
                  onChange={(e) => setCaseName(e.target.value)}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block font-medium text-slate-700 mb-1">Method</label>
                  <select
                    value={method}
                    onChange={(e) => setMethod(e.target.value)}
                    className="w-full border border-slate-300 rounded-lg p-2 bg-white"
                  >
                    <option>GET</option>
                    <option>POST</option>
                    <option>PUT</option>
                    <option>DELETE</option>
                  </select>
                </div>
                <div>
                  <label className="block font-medium text-slate-700 mb-1">Expected Status</label>
                  <input
                    type="number"
                    value={expectedStatus}
                    onChange={(e) => setExpectedStatus(Number(e.target.value))}
                    required
                    className="w-full border border-slate-300 rounded-lg p-2"
                  />
                </div>
              </div>
              <div>
                <label className="block font-medium text-slate-700 mb-1">Target URL</label>
                <input
                  type="url"
                  placeholder="https://api.example.com/v1/test"
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  required
                  className="w-full border border-slate-300 rounded-lg p-2 font-mono"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCaseModal(false)}
                  className="px-3 py-1.5 border border-slate-300 rounded-lg text-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-3.5 py-1.5 rounded-lg bg-indigo-600 text-white font-semibold"
                >
                  Save Case
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
