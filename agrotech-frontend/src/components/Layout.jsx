import { NavLink, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import api from '../services/api';

const Layout = ({ children }) => {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const [user, setUser] = useState(null);
  const [alertasCount, setAlertasCount] = useState(0);
  const [alertas, setAlertas] = useState([]);

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
      const response = await api.get('/auth/me');
      setUser(response.data);
    } catch (error) {
      console.error('Error fetching user:', error);
      if (error.response?.status === 401) {
        localStorage.removeItem('token');
        navigate('/login');
      }
    }
  };

  const fetchAlertas = async () => {
    try {
      const response = await api.get('/alertas/historial/activas');
      setAlertas(response.data);
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
    { path: '/account', label: 'Mi Cuenta', icon: '👤' },
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
            <div className="flex items-center space-x-4 relative">
              <button
                type="button"
                onClick={() => setNotificationsOpen(!notificationsOpen)}
                className="relative p-2 rounded-lg hover:bg-gray-100"
              >
                <span className="text-xl">🔔</span>
                {alertasCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] rounded-full h-5 w-5 flex items-center justify-center">
                    {alertasCount}
                  </span>
                )}
              </button>
              {notificationsOpen && (
                <div className="absolute right-0 top-14 z-50 w-full max-w-sm sm:max-w-md bg-white border border-gray-200 rounded-2xl shadow-xl overflow-hidden">
                  <div className="p-4 border-b border-gray-100 bg-gray-50">
                    <div className="flex items-center justify-between gap-3">
                      <div>
                        <h3 className="text-sm font-semibold text-gray-900">Notificaciones</h3>
                        <p className="text-xs text-gray-500 mt-1">Alertas activas e importantes del sistema.</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setNotificationsOpen(false)}
                        className="text-gray-500 hover:text-gray-700"
                      >Cerrar</button>
                    </div>
                  </div>
                  <div className="max-h-80 overflow-y-auto">
                    {alertas.length > 0 ? (
                      alertas.slice(0, 5).map((alerta) => (
                        <div key={alerta.id} className="p-4 border-b border-gray-100 hover:bg-gray-50">
                          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                            <div>
                              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${alerta.prioridad === 'ALTA' ? 'bg-red-100 text-red-700' : alerta.prioridad === 'MEDIA' ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'}`}>
                                {alerta.prioridad || 'MEDIA'}
                              </span>
                              <p className="mt-3 text-sm text-gray-900">{alerta.mensaje}</p>
                              {(alerta.dispositivoNombre || alerta.loteNombre) && (
                                <p className="mt-2 text-xs text-gray-500">
                                  {alerta.loteNombre ? `Lote: ${alerta.loteNombre}` : `Dispositivo: ${alerta.dispositivoNombre}`}
                                </p>
                              )}
                            </div>
                            <span className="text-xs text-gray-500 whitespace-nowrap">{new Date(alerta.fecha).toLocaleTimeString('es-CO')}</span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="p-4 text-sm text-gray-500">No hay notificaciones activas.</div>
                    )}
                  </div>
                  <div className="p-4 bg-gray-50 text-right">
                    <button
                      type="button"
                      onClick={() => { setNotificationsOpen(false); navigate('/alertas'); }}
                      className="text-sm font-semibold text-green-700 hover:text-green-900"
                    >Ver todas las alertas →</button>
                  </div>
                </div>
              )}
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