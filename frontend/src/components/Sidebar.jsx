import { useState } from 'react';
import { Menu, X } from 'lucide-react';

const navByRole = {
  STUDENT: [
  { label: 'Dashboard', path: '/student/dashboard' },
  { label: 'My Profile', path: '/student/profile' },
  { label: 'My Skills', path: '/student/skills' },
  { label: 'My Resume', path: '/student/resume' },
  { label: 'Find Jobs', path: '/jobs' },
  { label: 'Recommended Jobs', path: '/student/recommended' },
  { label: 'Applications', path: '/student/applications' },
  { label: 'Skill Gap', path: '/student/skill-gap' },
  { label: 'AI Assistant', path: '/student/assistant' },
  ],
  RECRUITER: [
    { label: 'Dashboard', path: '/recruiter/dashboard' },
    { label: 'Manage jobs', path: '/recruiter/jobs' },
    { label: 'Company profile', path: '/recruiter/company' },
  ],
  ADMIN: [
    { label: 'Dashboard', path: '/admin/dashboard' },
    { label: 'Students', path: '/admin/students' },
    { label: 'Recruiters', path: '/admin/recruiters' },
    { label: 'Companies', path: '/admin/companies' },
    { label: 'Jobs', path: '/admin/jobs' },
    { label: 'Applications', path: '/admin/applications' },
  ],
};

export function Sidebar({ selectedTab, onSelectTab, onLogout, user }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const navItems = navByRole[user?.role] || navByRole.STUDENT;
  const selectTab = (path) => {
    onSelectTab(path);
    setMenuOpen(false);
  };
  return (
    <aside className="sidebar">
      <div className="brand">
        <span className="brand-mark">N</span>
        Northstar
      </div>
      <button className="mobile-menu-toggle" type="button" aria-expanded={menuOpen}
        aria-label={menuOpen ? 'Close navigation menu' : 'Open navigation menu'}
        onClick={() => setMenuOpen((open) => !open)}>
        {menuOpen ? <X size={20} /> : <Menu size={20} />}
      </button>

      <div className="workspace-label">WORKSPACE</div>

      <nav className={menuOpen ? 'mobile-open' : ''} aria-label="Workspace navigation">
        {navItems.map((item) => (
          <button
            key={item.path}
            type="button"
            className={`nav-item ${selectedTab === item.path || (item.path !== '/student/dashboard' && selectedTab.startsWith(`${item.path}/`)) ? 'active' : ''}`}
            onClick={() => selectTab(item.path)}
          >
            <span>{item.label}</span>
          </button>
        ))}
        <button type="button" className="nav-item mobile-signout" onClick={onLogout}>
          <span>Sign out</span>
        </button>
      </nav>

      <div className="sidebar-bottom">
        <div className="workspace-label">ACCOUNT</div>
        <div className="profile-card">
          <div className="avatar">{(user?.name || 'User').split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase()}</div>
          <div>
            <strong>{user?.name || 'Student'}</strong>
            <small>{user?.role?.toLowerCase() || 'student'} account</small>
          </div>
        </div>
        <button type="button" className="nav-item sidebar-logout" onClick={() => { onLogout(); onSelectTab('/register'); }}>
          <span>Create another account</span>
        </button>
        <button type="button" className="nav-item sidebar-logout" onClick={onLogout}>
          <span>Sign out</span>
        </button>
      </div>
    </aside>
  );
}
