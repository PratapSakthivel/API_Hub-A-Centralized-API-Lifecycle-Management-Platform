import { useEffect, useState } from 'react';
import api from './lib/api';
import ApiCatalog from './features/catalog/ApiCatalog';
import VersionManagement from './features/versioning/VersionManagement';
import ApiTestingView from './features/testing/ApiTestingView';
import DependencyMapView from './features/dependencies/DependencyMapView';
import ReleaseManagement from './features/release/ReleaseManagement';
import DashboardView from './features/dashboard/DashboardView';
import ApiImportExportView from './features/impexp/ApiImportExportView';
import EnvironmentManagementView from './features/environments/EnvironmentManagementView';
import NotificationCenterView from './features/notifications/NotificationCenterView';

type Tab =
  | 'dashboard'
  | 'catalog'
  | 'versioning'
  | 'testing'
  | 'dependencies'
  | 'release'
  | 'impexp'
  | 'environments'
  | 'notifications';

interface NavItem {
  key: Tab;
  label: string;
  icon: string;
}

const NAV_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', icon: '📊' },
  { key: 'catalog', label: 'API Catalog', icon: '📁' },
  { key: 'versioning', label: 'Versioning', icon: '🏷️' },
  { key: 'testing', label: 'API Testing', icon: '🧪' },
  { key: 'dependencies', label: 'Dependencies', icon: '🔗' },
  { key: 'release', label: 'Releases', icon: '🚀' },
  { key: 'impexp', label: 'Import / Export', icon: '🔄' },
  { key: 'environments', label: 'Environments', icon: '🌐' },
  { key: 'notifications', label: 'Notifications', icon: '🔔' },
];

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('dashboard');
  const [backendHealth, setBackendHealth] = useState<string>('checking');

  useEffect(() => {
    api
      .get('/health')
      .then(() => setBackendHealth('connected'))
      .catch(() => setBackendHealth('offline'));
  }, []);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-800">
      {/* Top Header */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-indigo-600 to-violet-600 flex items-center justify-center text-white font-bold text-lg shadow-sm">
              ⚡
            </div>
            <div>
              <span className="font-extrabold text-lg tracking-tight bg-gradient-to-r from-indigo-700 to-violet-700 bg-clip-text text-transparent">
                API Hub
              </span>
              <span className="hidden sm:inline-block text-xs text-slate-400 ml-2 font-medium">
                Lifecycle Management Platform
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <span
              className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${
                backendHealth === 'connected'
                  ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                  : 'bg-amber-50 text-amber-700 border border-amber-200'
              }`}
            >
              <span
                className={`w-2 h-2 rounded-full mr-1.5 ${
                  backendHealth === 'connected' ? 'bg-emerald-500 animate-pulse' : 'bg-amber-500'
                }`}
              />
              {backendHealth === 'connected' ? 'Core Online' : 'Connecting…'}
            </span>
          </div>
        </div>

        {/* Global Navigation Bar */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex overflow-x-auto no-scrollbar gap-1 pt-1">
          {NAV_ITEMS.map(item => (
            <button
              key={item.key}
              onClick={() => setActiveTab(item.key)}
              className={`px-3.5 py-2.5 text-xs font-semibold whitespace-nowrap border-b-2 transition-all flex items-center gap-1.5 ${
                activeTab === item.key
                  ? 'border-indigo-600 text-indigo-700 bg-indigo-50/50 rounded-t-lg'
                  : 'border-transparent text-slate-500 hover:text-slate-800 hover:bg-slate-100/60 rounded-t-lg'
              }`}
            >
              <span>{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </div>
      </header>

      {/* Main Workspace */}
      <main className="flex-1 w-full">
        {activeTab === 'dashboard' && <DashboardView />}
        {activeTab === 'catalog' && (
          <div className="p-8 max-w-7xl mx-auto">
            <ApiCatalog />
          </div>
        )}
        {activeTab === 'versioning' && <VersionManagement />}
        {activeTab === 'testing' && <ApiTestingView />}
        {activeTab === 'dependencies' && <DependencyMapView />}
        {activeTab === 'release' && <ReleaseManagement />}
        {activeTab === 'impexp' && <ApiImportExportView />}
        {activeTab === 'environments' && <EnvironmentManagementView />}
        {activeTab === 'notifications' && <NotificationCenterView />}
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 mt-12 text-center text-xs text-slate-400">
        API Hub &bull; Production Ready Centralized API Lifecycle Management Platform &bull; v1.0.0
      </footer>
    </div>
  );
}
