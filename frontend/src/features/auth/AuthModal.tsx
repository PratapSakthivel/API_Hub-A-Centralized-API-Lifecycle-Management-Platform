import { useState } from 'react';
import api from '../../lib/api';
import { Shield, Code, Briefcase, CheckCircle2, User, Key, Lock, Mail, ArrowRight, X } from 'lucide-react';

export interface AuthUser {
  id: number;
  email: string;
  displayName: string;
  roles: string[];
}

interface AuthModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAuthSuccess: (user: AuthUser, token: string) => void;
}

interface DemoProfile {
  title: string;
  role: string;
  email: string;
  pass: string;
  icon: typeof Shield;
  color: string;
  desc: string;
}

const DEMO_PROFILES: DemoProfile[] = [
  {
    title: 'Administrator',
    role: 'ADMIN',
    email: 'admin@apihub.local',
    pass: 'Admin@123456',
    icon: Shield,
    color: 'from-violet-600 to-indigo-600 text-violet-700 bg-violet-50 border-violet-200',
    desc: 'Unrestricted access, governance policies, user & environment administration',
  },
  {
    title: 'Senior Developer',
    role: 'DEVELOPER',
    email: 'dev@apihub.local',
    pass: 'Dev@123456',
    icon: Code,
    color: 'from-blue-600 to-cyan-600 text-blue-700 bg-blue-50 border-blue-200',
    desc: 'API design, OpenAPI 3.x ingestion, semantic versioning & endpoint documentation',
  },
  {
    title: 'Product Manager',
    role: 'PROJECT_MANAGER',
    email: 'pm@apihub.local',
    pass: 'Pm@123456',
    icon: Briefcase,
    color: 'from-amber-600 to-orange-600 text-amber-700 bg-amber-50 border-amber-200',
    desc: 'Release pipeline promotions, stage approvals & platform audit inspection',
  },
  {
    title: 'QA Engineer',
    role: 'QA_ENGINEER',
    email: 'qa@apihub.local',
    pass: 'Qa@123456',
    icon: CheckCircle2,
    color: 'from-emerald-600 to-teal-600 text-emerald-700 bg-emerald-50 border-emerald-200',
    desc: 'Automated test suite execution, SSRF-guarded HTTP runners & regression tracking',
  },
  {
    title: 'Partner Consumer',
    role: 'CONSUMER',
    email: 'consumer@apihub.local',
    pass: 'Consumer@123456',
    icon: User,
    color: 'from-slate-600 to-slate-700 text-slate-700 bg-slate-50 border-slate-200',
    desc: 'Catalog discovery, version diff comparison & OpenAPI contract specification exports',
  },
];

export default function AuthModal({ isOpen, onClose, onAuthSuccess }: AuthModalProps) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const executeLogin = async (loginEmail: string, loginPass: string) => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/login', {
        email: loginEmail.trim(),
        password: loginPass,
      });

      const data = res.data?.data || res.data;
      if (data?.token) {
        localStorage.setItem('api_hub_token', data.token);
        const userObj: AuthUser = {
          id: data.id,
          email: data.email,
          displayName: data.displayName,
          roles: Array.isArray(data.roles) ? data.roles : Array.from(data.roles || []),
        };
        localStorage.setItem('api_hub_user', JSON.stringify(userObj));
        onAuthSuccess(userObj, data.token);
        onClose();
      } else {
        setError('Login returned an unexpected response structure.');
      }
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { message?: string } } };
      setError(errorObj.response?.data?.message || 'Authentication failed. Please verify credentials.');
    } finally {
      setLoading(false);
    }
  };

  const handleManualSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) return;
    executeLogin(email, password);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
      <div className="relative w-full max-w-2xl bg-white rounded-2xl shadow-2xl border border-slate-200 overflow-hidden">
        {/* Modal Header */}
        <div className="px-6 py-5 bg-gradient-to-r from-slate-900 to-slate-800 text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-sm">
              <Key className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold">API Hub Identity & Access</h2>
              <p className="text-xs text-slate-300">Select a demo persona or authenticate with custom credentials</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-700 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-6 max-h-[80vh] overflow-y-auto">
          {error && (
            <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs flex items-center gap-2">
              <span className="font-bold">Error:</span> {error}
            </div>
          )}

          {/* 1-Click Demo Profiles Section */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="text-xs font-bold uppercase tracking-wider text-slate-500">
                1-Click Demo Profiles (Instant Role Switch)
              </h3>
              <span className="text-[11px] font-medium text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-200">
                Click any persona to log in
              </span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              {DEMO_PROFILES.map(demo => {
                const IconComponent = demo.icon;
                return (
                  <button
                    key={demo.role}
                    type="button"
                    disabled={loading}
                    onClick={() => executeLogin(demo.email, demo.pass)}
                    className="text-left p-3.5 rounded-xl border border-slate-200 hover:border-indigo-400 bg-slate-50/60 hover:bg-white hover:shadow-md transition-all group disabled:opacity-50 flex items-start gap-3"
                  >
                    <div className={`p-2 rounded-lg border ${demo.color} shrink-0`}>
                      <IconComponent className="w-4 h-4" />
                    </div>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center justify-between gap-1">
                        <span className="text-xs font-bold text-slate-800 group-hover:text-indigo-600 transition-colors">
                          {demo.title}
                        </span>
                        <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-slate-200/80 text-slate-600 font-semibold">
                          {demo.role}
                        </span>
                      </div>
                      <p className="text-[11px] text-slate-500 line-clamp-2 mt-1 leading-relaxed">
                        {demo.desc}
                      </p>
                      <div className="mt-2 flex items-center text-[10px] font-medium text-indigo-600 group-hover:translate-x-0.5 transition-transform">
                        <span>Sign in as {demo.title}</span>
                        <ArrowRight className="w-3 h-3 ml-1" />
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Divider */}
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200" />
            </div>
            <div className="relative flex justify-center text-xs uppercase">
              <span className="bg-white px-3 text-slate-400 font-semibold text-[10px]">
                Or enter credentials manually
              </span>
            </div>
          </div>

          {/* Manual Credentials Form */}
          <form onSubmit={handleManualSubmit} className="space-y-3.5">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Email Address</label>
              <div className="relative">
                <Mail className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="email"
                  required
                  placeholder="admin@apihub.local"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-400 absolute left-3 top-2.5" />
                <input
                  type="password"
                  required
                  placeholder="••••••••••••"
                  value={password}
                  onChange={e => setPassword(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 text-xs border border-slate-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:outline-none"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 px-4 rounded-lg bg-slate-900 hover:bg-slate-800 text-white text-xs font-bold shadow-sm transition-all disabled:opacity-50 flex items-center justify-center gap-2"
            >
              {loading ? 'Authenticating…' : 'Sign In with Password'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
