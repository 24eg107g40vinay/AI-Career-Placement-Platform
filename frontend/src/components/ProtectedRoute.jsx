import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const rolePaths = {
  STUDENT: '/student/dashboard',
  RECRUITER: '/recruiter/dashboard',
  ADMIN: '/admin/dashboard',
};

export function ProtectedRoute({ allowedRoles }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to={rolePaths[user.role] || '/'} replace />;
  }

  return <Outlet />;
}

export function pathForRole(role) {
  return rolePaths[role] || '/';
}
