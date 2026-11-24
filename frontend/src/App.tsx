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
import AuthModal, { type AuthUser } from './features/auth/AuthModal';
import {
  LayoutDashboard,
  FolderGit2,
  Tag,
  FlaskConical,
  GitFork,
  Rocket,
  ArrowLeftRight,
  Server,
  Bell,
  LogIn,
  LogOut,
  UserCheck,
  Activity,
  Layers,
  ChevronDown
} from 'lucide-react';

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
  icon: typeof LayoutDashboard;
}

const NAV_ITEMS: NavItem[] = [
  { key: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { key: 'catalog', label: 'API Catalog', icon: FolderGit2 },
  { key: 'versioning', label: 'Versioning', icon: Tag },
  { key: 'testing', label: 'API Testing', icon: FlaskConical },
  { key: 'dependencies', label: 'Dependencies', icon: GitFork },
  { key: 'release', label: 'Releases', icon: Rocket },
  { key: 'impexp', label: 'Import / Export', icon: ArrowLeftRight },
  { key: 'environments', label: 'Environments', icon: Server },
  { key: 'notifications', label: 'Notifications', icon: Bell },
];

const ROLE_BADGE_COLORS: Record<string, string> = {
  ADMIN: 'bg-violet-100 text-violet-800 border-violet-200',
  DEVELOPER: 'bg-blue-100 text-blue-800 border-blue-200',
  PROJECT_MANAGER: 'bg-amber-100 text-amber-800 border-amber-200',
  QA_ENGINEER: 'bg-emerald-100 text-emerald-800 border-emerald-200',
  CONSUMER: 'bg-slate-100 text-slate-800 border-slate-200',
};

export default function App() {
  const [activeTab, setActiveTab] = useState<Tab>('dashboard');
  const [backendHealth, setBackendHealth] = useState<string>('checking');
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);

  // Initialize auth state from localStorage on load
  useEffect(() => {
    const cachedUser = localStorage.getItem('api_hub_user');
    const token = localStorage.getItem('api_hub_token');
    if (cachedUser && token) {
      try {
        setCurrentUser(JSON.parse(cachedUser));
      } catch {
        localStorage.removeItem('api_hub_user');
        localStorage.removeItem('api_hub_token');
      }
    }

    // Health check
    api
      .get('/health')
      .then(() => setBackendHealth('connected'))
      .catch(() => setBackendHealth('offline'));
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('api_hub_token');
    localStorage.removeItem('api_hub_user');
    setCurrentUser(null);
    setIsAuthModalOpen(true);
  };

  const handleAuthSuccess = (user: AuthUser) => {
    setCurrentUser(user);
  };

  const primaryRole = currentUser?.roles?.[0] || 'GUEST';

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col text-slate-900 font-sans selection:bg-indigo-500 selection:text-white">
      {/* Top Application Header */}
      <header className="bg-white border-b border-slate-200 sticky top-0 z-40 shadow-xs">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">
          {/* Logo & Product Name */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-slate-900 flex items-center justify-center text-white shadow-sm ring-1 ring-slate-800/20">
              <Layers className="w-5 h-5 text-indigo-400" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-base tracking-tight text-slate-900">
                  API Hub
                </span>
                <span className="text-[10px] font-mono uppercase px-1.5 py-0.5 rounded bg-slate-100 text-slate-600 font-bold border border-slate-200">
                  v1.0.0
                </span>
              </div>
              <span className="hidden sm:block text-[11px] text-slate-400 font-medium">
                Centralized Lifecycle & Governance Platform
              </span>
            </div>
          </div>

          {/* Right Controls: Core Status & Auth Persona */}
          <div className="flex items-center gap-3">
            {/* Health Indicator */}
            <div
              className={`hidden md:flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium border ${
                backendHealth === 'connected'
                  ? 'bg-emerald-50 text-emerald-700 border-emerald-200'
                  : 'bg-amber-50 text-amber-700 border-amber-200'
              }`}
            >
              <Activity className="w-3.5 h-3.5" />
              <span>{backendHealth === 'connected' ? 'Core Online' : 'Connecting…'}</span>
            </div>

            {/* Persona / Authentication Component */}
            {currentUser ? (
              <div className="flex items-center gap-2 bg-slate-50 border border-slate-200/90 rounded-xl p-1.5 pl-3">
                <div className="w-7 h-7 rounded-lg bg-indigo-600 text-white text-xs font-bold flex items-center justify-center uppercase shrink-0">
                  {currentUser.displayName?.charAt(0) || 'U'}
                </div>
                <div className="hidden lg:block text-left leading-tight">
                  <div className="text-xs font-bold text-slate-800 truncate max-w-[140px]">
                    {currentUser.displayName}
                  </div>
                  <div className="text-[10px] text-slate-400 truncate max-w-[140px]">
                    {currentUser.email}
                  </div>
                </div>

                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-md border ${
                    ROLE_BADGE_COLORS[primaryRole] || 'bg-slate-100 text-slate-700'
                  }`}
                >
                  {primaryRole}
                </span>

                <button
                  onClick={() => setIsAuthModalOpen(true)}
                  title="Switch Persona Role"
                  className="p-1 rounded-lg hover:bg-slate-200 text-slate-600 transition-colors flex items-center gap-0.5 text-xs font-medium"
                >
                  <UserCheck className="w-3.5 h-3.5 text-indigo-600" />
                  <ChevronDown className="w-3 h-3 text-slate-400" />
                </button>

                <button
                  onClick={handleLogout}
                  title="Sign Out"
                  className="p-1.5 rounded-lg hover:bg-rose-50 hover:text-rose-600 text-slate-400 transition-colors"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <button
                onClick={() => setIsAuthModalOpen(true)}
                className="inline-flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold transition-all shadow-xs"
              >
                <LogIn className="w-3.5 h-3.5" />
                <span>Sign In / Demo Roles</span>
              </button>
            )}
          </div>
        </div>

        {/* Global Navigation Bar */}
        <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex overflow-x-auto no-scrollbar gap-1 pt-1 border-t border-slate-100">
          {NAV_ITEMS.map(item => {
            const IconComponent = item.icon;
            const isActive = activeTab === item.key;
            return (
              <button
                key={item.key}
                onClick={() => setActiveTab(item.key)}
                className={`px-3.5 py-2.5 text-xs font-semibold whitespace-nowrap border-b-2 transition-all flex items-center gap-2 ${
                  isActive
                    ? 'border-indigo-600 text-indigo-700 bg-indigo-50/60 rounded-t-lg font-bold'
                    : 'border-transparent text-slate-500 hover:text-slate-900 hover:bg-slate-100/60 rounded-t-lg'
                }`}
              >
                <IconComponent className={`w-4 h-4 ${isActive ? 'text-indigo-600' : 'text-slate-400'}`} />
                <span>{item.label}</span>
              </button>
            );
          })}
        </nav>
      </header>

      {/* Role Banner Notification if Guest */}
      {!currentUser && (
        <div className="bg-amber-50 border-b border-amber-200/80 px-4 py-2.5 text-center text-xs text-amber-900 flex items-center justify-center gap-2">
          <span className="font-semibold">Viewing as Guest:</span>
          <span>Sign in with 1-click demo profiles to test developer, QA, or admin privileges.</span>
          <button
            onClick={() => setIsAuthModalOpen(true)}
            className="underline font-bold text-amber-950 hover:text-amber-800 ml-1"
          >
            Launch Demo Credentials &rarr;
          </button>
        </div>
      )}

      {/* Main Feature Workspace */}
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

      {/* Identity & Demo Auth Modal */}
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
        onAuthSuccess={handleAuthSuccess}
      />

      {/* Refined Enterprise Footer */}
      <footer className="bg-white border-t border-slate-200 py-6 mt-16 text-center text-xs text-slate-400">
        <div className="max-w-7xl mx-auto px-4 flex flex-col sm:flex-row items-center justify-between gap-2">
          <div>
            API Hub &bull; Production Enterprise API Lifecycle Management Platform
          </div>
          <div className="flex items-center gap-4 text-slate-500">
            <span>Spring Boot 4.x / Java 25</span>
            <span>&bull;</span>
            <span>React + TypeScript + Tailwind</span>
            <span>&bull;</span>
            <button
              onClick={() => setIsAuthModalOpen(true)}
              className="text-indigo-600 hover:underline font-semibold"
            >
              Demo Personas
            </button>
          </div>
        </div>
      </footer>
    </div>
  );
}
