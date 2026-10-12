import { BrowserRouter, Navigate, Outlet, Route, Routes, useLocation, useNavigate } from 'react-router-dom';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Sidebar } from './components/Sidebar';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Home } from './pages/public/Home';
import { Login } from './pages/public/Login';
import { Register } from './pages/public/Register';
import { JobDetails, Jobs } from './pages/public/Jobs';
import { Dashboard } from './pages/student/Dashboard';
import { Profile } from './pages/student/Profile';
import { Skills } from './pages/student/Skills';
import { Resume } from './pages/student/Resume';
import { Applications } from './pages/student/Applications';
import { Recommendations } from './pages/student/Recommendations';
import { SkillGap } from './pages/student/SkillGap';
import { AIAssistant } from './pages/student/AIAssistant';
import { RecruiterDashboard } from './pages/recruiter/RecruiterDashboard';
import { ManageJobs } from './pages/recruiter/ManageJobs';
import { CompanyProfile } from './pages/recruiter/CompanyProfile';
import { AdminDashboard } from './pages/admin/AdminDashboard';
import { AdminManagement } from './pages/admin/AdminManagement';

function WorkspaceLayout() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  return <div className="app-shell">
    <Sidebar selectedTab={location.pathname} onSelectTab={navigate} onLogout={logout} user={user} />
    <Outlet />
  </div>;
}

function ApplicationRoutes() {
  const { user } = useAuth();

  return (
    <Routes>
      <Route path="/" element={user?.role === 'STUDENT' ? <Navigate to="/student/dashboard" replace /> : <Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/jobs" element={<Jobs />} />
      <Route path="/jobs/:id" element={<JobDetails />} />
      <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
        <Route element={<WorkspaceLayout />}>
          <Route path="/student/dashboard" element={<Dashboard />} />
          <Route path="/student/profile" element={<Profile />} />
          <Route path="/student/skills" element={<Skills />} />
          <Route path="/student/resume" element={<Resume />} />
          <Route path="/student/recommended" element={<Recommendations />} />
          <Route path="/student/applications" element={<Applications />} />
          <Route path="/student/skill-gap" element={<SkillGap />} />
          <Route path="/student/assistant" element={<AIAssistant />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['RECRUITER']} />}>
        <Route element={<WorkspaceLayout />}>
          <Route path="/recruiter/dashboard" element={<RecruiterDashboard />} />
          <Route path="/recruiter/jobs" element={<ManageJobs />} />
          <Route path="/recruiter/company" element={<CompanyProfile />} />
        </Route>
      </Route>
      <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
        <Route element={<WorkspaceLayout />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />} />
          <Route path="/admin/:section" element={<AdminManagement />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ApplicationRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
