import { useEffect, useState } from 'react';
import api from '../services/api';

const Account = () => {
  const [user, setUser] = useState(null);
  const [fincasCount, setFincasCount] = useState(0);
  const [lotesCount, setLotesCount] = useState(0);
  const [devicesCount, setDevicesCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [passwordData, setPasswordData] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [passwordLoading, setPasswordLoading] = useState(false);

  useEffect(() => {
    fetchAccountData();
  }, []);

  const fetchAccountData = async () => {
    try {
      const [profileRes, fincasRes, lotesRes, devicesRes] = await Promise.all([
        api.get('/auth/me'),
        api.get('/fincas'),
        api.get('/lotes'),
        api.get('/dispositivos'),
      ]);

      setUser(profileRes.data);
      setFincasCount(fincasRes.data.length);
      setLotesCount(lotesRes.data.length);
      setDevicesCount(devicesRes.data.length);
    } catch (error) {
      console.error('Error loading account data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setPasswordError('Las contraseñas nuevas no coinciden.');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      setPasswordError('La nueva contraseña debe tener al menos 6 caracteres.');
      return;
    }

    setPasswordLoading(true);
    try {
      await api.post('/auth/change-password', {
        currentPassword: passwordData.currentPassword,
        newPassword: passwordData.newPassword,
      });
      setPasswordSuccess('Contraseña actualizada correctamente.');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (error) {
      setPasswordError(error.response?.data?.message || 'No se pudo cambiar la contraseña.');
      console.error('Password change error:', error);
    } finally {
      setPasswordLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-[70vh] flex items-center justify-center">
        <div className="animate-spin rounded-full h-14 w-14 border-b-2 border-green-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div className="rounded-3xl bg-white shadow-xl p-6 sm:p-8 border border-gray-200">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div>
            <p className="text-sm uppercase tracking-[0.2em] text-green-600 font-semibold">Mi Cuenta</p>
            <h1 className="text-3xl font-bold text-gray-900 mt-3">Bienvenido{user?.nombre ? `, ${user.nombre}` : ''}</h1>
            <p className="mt-2 text-gray-600 max-w-2xl">
              Revisa tu perfil, datos de acceso y la información de tus recursos creados en AGROTECH.
            </p>
          </div>
          <div className="rounded-3xl bg-green-50 border border-green-100 px-5 py-4 text-center">
            <p className="text-sm text-green-700">Rol</p>
            <p className="text-xl font-bold text-green-900 mt-2">{user?.rol}</p>
          </div>
        </div>

        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          <div className="rounded-3xl bg-gray-50 p-5 border border-gray-200">
            <p className="text-sm text-gray-500">Fincas</p>
            <p className="mt-3 text-3xl font-bold text-gray-900">{fincasCount}</p>
            <p className="mt-2 text-sm text-gray-500">Fincas asociadas a tu cuenta</p>
          </div>
          <div className="rounded-3xl bg-gray-50 p-5 border border-gray-200">
            <p className="text-sm text-gray-500">Lotes</p>
            <p className="mt-3 text-3xl font-bold text-gray-900">{lotesCount}</p>
            <p className="mt-2 text-sm text-gray-500">Lotes registrados</p>
          </div>
          <div className="rounded-3xl bg-gray-50 p-5 border border-gray-200">
            <p className="text-sm text-gray-500">Dispositivos</p>
            <p className="mt-3 text-3xl font-bold text-gray-900">{devicesCount}</p>
            <p className="mt-2 text-sm text-gray-500">Dispositivos conectados</p>
          </div>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="rounded-3xl bg-white shadow-xl p-6 border border-gray-200">
          <h2 className="text-xl font-semibold text-gray-900">Información de perfil</h2>
          <p className="mt-4 text-gray-600">Revisa los datos que tienes registrados y accede a tus recursos en la plataforma.</p>
          <div className="mt-6 space-y-4">
            <div className="rounded-2xl bg-gray-50 p-4 border border-gray-100">
              <p className="text-sm text-gray-500">Nombre</p>
              <p className="mt-1 text-lg font-medium text-gray-900">{user?.nombre || '-'}</p>
            </div>
            <div className="rounded-2xl bg-gray-50 p-4 border border-gray-100">
              <p className="text-sm text-gray-500">Correo</p>
              <p className="mt-1 text-lg font-medium text-gray-900">{user?.email || '-'}</p>
            </div>
            <div className="rounded-2xl bg-gray-50 p-4 border border-gray-100">
              <p className="text-sm text-gray-500">ID de usuario</p>
              <p className="mt-1 text-sm text-gray-700 break-all">{user?.id || '-'}</p>
            </div>
          </div>
        </div>

        <div className="rounded-3xl bg-white shadow-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-xl font-semibold text-gray-900">Cambiar contraseña</h2>
              <p className="mt-2 text-gray-600">Solo podrás cambiarla si ingresas tu contraseña actual.</p>
            </div>
          </div>
          <div className="mt-6 space-y-4">
            {passwordError && (
              <div className="rounded-2xl bg-red-50 border border-red-200 px-4 py-3 text-sm text-red-700">
                {passwordError}
              </div>
            )}
            {passwordSuccess && (
              <div className="rounded-2xl bg-green-50 border border-green-200 px-4 py-3 text-sm text-green-700">
                {passwordSuccess}
              </div>
            )}
            <form onSubmit={handlePasswordChange} className="space-y-4">
              <div>
                <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700">Contraseña actual</label>
                <input
                  id="currentPassword"
                  name="currentPassword"
                  type="password"
                  required
                  value={passwordData.currentPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                  className="mt-2 w-full rounded-2xl border border-gray-300 bg-gray-50 px-4 py-3 focus:border-green-500 focus:outline-none focus:ring-2 focus:ring-green-100"
                />
              </div>
              <div>
                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">Nueva contraseña</label>
                <input
                  id="newPassword"
                  name="newPassword"
                  type="password"
                  required
                  value={passwordData.newPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                  className="mt-2 w-full rounded-2xl border border-gray-300 bg-gray-50 px-4 py-3 focus:border-green-500 focus:outline-none focus:ring-2 focus:ring-green-100"
                />
              </div>
              <div>
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">Confirmar nueva contraseña</label>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  required
                  value={passwordData.confirmPassword}
                  onChange={(e) => setPasswordData({ ...passwordData, confirmPassword: e.target.value })}
                  className="mt-2 w-full rounded-2xl border border-gray-300 bg-gray-50 px-4 py-3 focus:border-green-500 focus:outline-none focus:ring-2 focus:ring-green-100"
                />
              </div>
              <button
                type="submit"
                disabled={passwordLoading}
                className="w-full rounded-2xl bg-gradient-to-r from-green-500 to-emerald-600 px-5 py-3 text-white font-semibold shadow-lg hover:from-green-600 hover:to-emerald-700 transition-all duration-200 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {passwordLoading ? 'Actualizando contraseña...' : 'Actualizar contraseña'}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Account;
