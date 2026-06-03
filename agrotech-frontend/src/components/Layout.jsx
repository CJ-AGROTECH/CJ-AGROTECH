import { NavLink, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import api from '../services/api';
import { useAlertNotifications } from '../context/AlertNotificationContext';

const Layout = ({ children }) => {
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false);
  const [user, setUser] = useState(null);
  const {
    alertas,
    alertasCount,
    toast,
    dismissToast,
    notificationsOpen,
    setNotificationsOpen,
    refreshAlertas,
  } = useAlertNotifications();

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }
    fetchUserData();
  }, [navigate]);

  useEffect(() => {
    if (notificationsOpen) {
      refreshAlertas();
    }
  }, [notificationsOpen, refreshAlertas]);

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

  const handleLogout = () => {
    localStorage.removeItem('token');
    navigate('/login');
  };

  const prioridadClass = (prioridad) => {
    if (prioridad === 'CRITICA' || prioridad === 'ALTA') return 'bg-red-100 text-red-700';
    if (prioridad === 'MEDIA') return 'bg-yellow-100 text-yellow-700';
    return 'bg-green-100 text-green-700';
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
      {toast && (
        <div className="fixed top-20 right-4 z-[60] max-w-md w-full sm:w-96 animate-pulse">
          <div className="bg-white border-l-4 border-red-500 rounded-lg shadow-2xl p-4">
            <div className="flex justify-between items-start gap-2">
              <div>
                <p className="text-xs font-semibold text-red-600 uppercase tracking-wide">Nueva alerta</p>
                <p className="mt-1 text-sm text-gray-900">{toast.mensaje}</p>
                {(toast.dispositivoNombre || toast.loteNombre) && (
                  <p className="mt-1 text-xs text-gray-500">
                    {toast.loteNombre ? `Lote: ${toast.loteNombre}` : `Dispositivo: ${toast.dispositivoNombre}`}
                  </p>
                )}
              </div>
              <button
                type="button"
                onClick={dismissToast}
                className="text-gray-400 hover:text-gray-600 text-lg leading-none"
                aria-label="Cerrar"
              >
                ×
              </button>
            </div>
            <button
              type="button"
              onClick={() => { dismissToast(); setNotificationsOpen(true); }}
              className="mt-3 text-xs font-semibold text-green-700 hover:text-green-900"
            >
              Ver en notificaciones
            </button>
          </div>
        </div>
      )}

      <nav className="bg-white shadow-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center space-x-2 cursor-pointer" onClick={() => navigate('/dashboard')}>
              <div className="w-8 h-8 bg-gradient-to-r from-green-500 to-emerald-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">A</span>
              </div>
              <span className="text-xl font-bold text-gray-800">AGROTECH</span>
            </div>

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
                        <p className="text-xs text-gray-500 mt-1">Alertas activas en tiempo real.</p>
                      </div>
                      <button
                        type="button"
                        onClick={() => setNotificationsOpen(false)}
                        className="text-gray-500 hover:text-gray-700"
                      >
                        Cerrar
                      </button>
                    </div>
                  </div>
                  <div className="max-h-80 overflow-y-auto">
                    {alertas.length > 0 ? (
                      alertas.slice(0, 8).map((alerta) => (
                        <div key={alerta.id} className="p-4 border-b border-gray-100 hover:bg-gray-50">
                          <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-3">
                            <div>
                              <span className={`inline-flex items-center text-xs font-semibold px-2.5 py-1 rounded-full ${prioridadClass(alerta.prioridad)}`}>
                                {alerta.prioridad || 'MEDIA'}
                              </span>
                              <p className="mt-3 text-sm text-gray-900">{alerta.mensaje}</p>
                              {(alerta.dispositivoNombre || alerta.loteNombre) && (
                                <p className="mt-2 text-xs text-gray-500">
                                  {alerta.loteNombre ? `Lote: ${alerta.loteNombre}` : `Dispositivo: ${alerta.dispositivoNombre}`}
                                </p>
                              )}
                            </div>
                            <span className="text-xs text-gray-500 whitespace-nowrap">
                              {alerta.fecha ? new Date(alerta.fecha).toLocaleString('es-CO') : ''}
                            </span>
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
                    >
                      Ver todas las alertas →
                    </button>
                  </div>
                </div>
              )}
              <button
                onClick={handleLogout}
                className="text-sm text-gray-600 hover:text-red-600 transition-colors"
              >
                Cerrar Sesión
              </button>

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

        {alertasCount > 0 && (
          <div className="bg-red-600 text-white px-4 py-3 text-sm flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
            <div>
              <p className="font-semibold">
                Hay {alertasCount} alerta{alertasCount === 1 ? '' : 's'} activa{alertasCount === 1 ? '' : 's'}.
              </p>
              <p className="text-sm text-red-100">Revisa las notificaciones o la sección Alertas.</p>
            </div>
            <button
              type="button"
              onClick={() => setNotificationsOpen(true)}
              className="inline-flex items-center justify-center rounded-lg border border-white/20 bg-white/10 px-3 py-2 text-xs font-semibold text-white hover:bg-white/20"
            >
              Abrir notificaciones
            </button>
          </div>
        )}

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

      <main className="max-w-7xl mx-auto px-4 py-6">
        {children}
      </main>
    </div>
  );
};

export default Layout;
