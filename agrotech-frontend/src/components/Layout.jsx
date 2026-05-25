import { NavLink, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import api from '../services/api';

const Layout = ({ children }) => {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [user, setUser] = useState(null);
  const [alertasCount, setAlertasCount] = useState(0);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    fetchUserData();
    fetchAlertas();
  }, []);

  const fetchUserData = async () => {
    try {
      const response = await api.get('/usuarios/me');
      setUser(response.data);
    } catch (error) {
      console.error('Error fetching user:', error);
    }
  };

  const fetchAlertas = async () => {
    try {
      const response = await api.get('/alertas/historial/activas');
      setAlertasCount(response.data.length);
    } catch (error) {
      console.error('Error fetching alertas:', error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: '📊' },
    { path: '/fincas', label: 'Fincas', icon: '🌾' },
    { path: '/lotes', label: 'Lotes', icon: '🗺️' },
    { path: '/dispositivos', label: 'Dispositivos', icon: '📡' },
    { path: '/cultivos', label: 'Cultivos', icon: '🌱' },
    { path: '/alertas', label: 'Alertas', icon: '🔔', badge: alertasCount },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Top Navigation Bar */}
      <nav className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center space-x-2 cursor-pointer" onClick={() => navigate('/dashboard')}>
              <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">A</span>
              </div>
              <span className="text-xl font-bold text-gray-800">AGROTECH</span>
            </div>

            {/* Desktop Navigation */}
            <div className="hidden md:flex items-center space-x-1">
              {navItems.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  className={({ isActive }) =>
                    `px-4 py-2 rounded-lg text-sm font-medium transition-colors relative ${
                      isActive
                        ? 'bg-green-50 text-green-700'
                        : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                    }`
                  }
                >
                  <span className="mr-1">{item.icon}</span>
                  {item.label}
                  {item.badge > 0 && (
                    <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                      {item.badge}
                    </span>
                  )}
                </NavLink>
              ))}
            </div>

            {/* User Menu */}
            <div className="flex items-center space-x-4">
              <button
                onClick={handleLogout}
                className="text-sm text-gray-600 hover:text-red-600 transition-colors"
              >
                Cerrar Sesión
              </button>
              
              {/* Mobile Menu Button */}
              <button
                className="md:hidden p-2 rounded-lg hover:bg-gray-100"
                onClick={() => setMenuOpen(!menuOpen)}
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        {/* Mobile Menu */}
        {menuOpen && (
          <div className="md:hidden bg-white border-t">
            <div className="px-4 py-2 space-y-1">
              {navItems.map((item) => (
                <NavLink
                  key={item.path}
                  to={item.path}
                  onClick={() => setMenuOpen(false)}
                  className={({ isActive }) =>
                    `block px-4 py-3 rounded-lg text-sm font-medium ${
                      isActive
                        ? 'bg-green-50 text-green-700'
                        : 'text-gray-600 hover:bg-gray-100'
                    }`
                  }
                >
                  <span className="mr-2">{item.icon}</span>
                  {item.label}
                  {item.badge > 0 && (
                    <span className="ml-2 bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                      {item.badge}
                    </span>
                  )}
                </NavLink>
              ))}
              <button
                onClick={handleLogout}
                className="block w-full text-left px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg"
              >
                Cerrar Sesión
              </button>
            </div>
          </div>
        )}
      </nav>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 py-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;