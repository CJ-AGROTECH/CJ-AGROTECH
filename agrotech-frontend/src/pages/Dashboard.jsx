import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import api from '../services/api';

const Dashboard = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [dispositivos, setDispositivos] = useState([]);
  const [selectedDispositivo, setSelectedDispositivo] = useState(null);
  const [historico, setHistorico] = useState([]);
  const [eficiencia, setEficiencia] = useState(null);
  const [alertasActivas, setAlertasActivas] = useState([]);
  const [stats, setStats] = useState({
    totalFincas: 0,
    totalLotes: 0,
    totalDispositivos: 0,
    dispositivosActivos: 0
  });

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      
      // Fetch dispositivos
      const dispResponse = await api.get('/dispositivos');
      setDispositivos(dispResponse.data);
      
      // Fetch alertas activas
      const alertasResponse = await api.get('/alertas/historial/activas');
      setAlertasActivas(alertasResponse.data);
      
      // Fetch fincas for stats
      const fincasResponse = await api.get('/fincas');
      const fincas = fincasResponse.data;
      
      // Fetch lotes for stats
      const lotesResponse = await api.get('/lotes');
      const lotes = lotesResponse.data;
      
      setStats({
        totalFincas: fincas.length,
        totalLotes: lotes.length,
        totalDispositivos: dispResponse.data.length,
        dispositivosActivos: dispResponse.data.filter(d => d.estado === 'ACTIVO').length
      });

      // If we have dispositivos, fetch data for the first one
      if (dispResponse.data.length > 0) {
        setSelectedDispositivo(dispResponse.data[0].id);
        await fetchDispositivoData(dispResponse.data[0].id);
      }
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError('Error al cargar los datos del dashboard');
    } finally {
      setLoading(false);
    }
  };

  const fetchDispositivoData = async (dispositivoId) => {
    try {
      // Fetch histórico
      const historicoResponse = await api.get(`/dashboard/historico/${dispositivoId}`);
      const formattedHistorico = historicoResponse.data.map(d => ({
        timestamp: new Date(d.timestamp).toLocaleString('es-CO', { 
          day: '2-digit', 
          month: '2-digit', 
          hour: '2-digit', 
          minute: '2-digit' 
        }),
        tempAire: d.lecturas?.ambiente?.tempAire || 0,
        humAire: d.lecturas?.ambiente?.humAire || 0,
        tempSuelo: d.lecturas?.suelo?.tempSuelo || 0,
        humSuelo: d.lecturas?.suelo?.humSuelo || 0,
        luminosidad: d.lecturas?.ambiente?.luminosidad || 0
      }));
      setHistorico(formattedHistorico);

      // Fetch eficiencia hídrica
      const efResponse = await api.get(`/dashboard/eficiencia-hidrica/${dispositivoId}`);
      setEficiencia(efResponse.data);
    } catch (error) {
      console.error('Error fetching dispositivo data:', error);
    }
  };

  const handleDispositivoChange = (e) => {
    const dispositivoId = e.target.value;
    setSelectedDispositivo(dispositivoId);
    if (dispositivoId) {
      fetchDispositivoData(dispositivoId);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Dashboard AGROTECH</h1>
          <p className="text-gray-600 mt-1">Monitoreo en tiempo real de tus cultivos</p>
        </div>
        <div className="mt-4 md:mt-0">
          <select
            value={selectedDispositivo || ''}
            onChange={handleDispositivoChange}
            className="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
          >
            <option value="">Seleccionar dispositivo</option>
            {dispositivos.map(d => (
              <option key={d.id} value={d.id}>{d.nombre}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Fincas</p>
              <p className="text-2xl font-bold text-gray-900">{stats.totalFincas}</p>
            </div>
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <span className="text-2xl">🌾</span>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Total Lotes</p>
              <p className="text-2xl font-bold text-gray-900">{stats.totalLotes}</p>
            </div>
            <div className="w-12 h-12 bg-emerald-100 rounded-lg flex items-center justify-center">
              <span className="text-2xl">🗺️</span>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Dispositivos</p>
              <p className="text-2xl font-bold text-gray-900">{stats.totalDispositivos}</p>
            </div>
            <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <span className="text-2xl">📡</span>
            </div>
          </div>
        </div>
        
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-gray-600">Activos</p>
              <p className="text-2xl font-bold text-green-600">{stats.dispositivosActivos}</p>
            </div>
            <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
              <span className="text-2xl">✓</span>
            </div>
          </div>
        </div>
      </div>

      {/* Alertas Activas */}
      {alertasActivas.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4">
          <h3 className="text-lg font-semibold text-red-800 mb-3">⚠️ Alertas Activas</h3>
          <div className="space-y-2">
            {alertasActivas.slice(0, 3).map(alerta => (
              <div key={alerta.id} className="bg-white rounded-lg p-3 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{alerta.mensaje}</p>
                  <p className="text-sm text-gray-500">{new Date(alerta.fecha).toLocaleString()}</p>
                </div>
                <button 
                  onClick={() => navigate('/alertas')}
                  className="text-sm text-red-600 hover:text-red-800"
                >
                  Ver →
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Temperature & Humidity Chart */}
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Temperatura y Humedad del Aire</h3>
          {historico.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={historico}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" tick={{ fontSize: 10 }} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="tempAire" name="Temp. Aire (°C)" stroke="#F59E0B" strokeWidth={2} />
                <Line type="monotone" dataKey="humAire" name="Hum. Aire (%)" stroke="#3B82F6" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-64 flex items-center justify-center text-gray-500">
              No hay datos de telemetría disponibles
            </div>
          )}
        </div>

        {/* Soil Chart */}
        <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Temperatura y Humedad del Suelo</h3>
          {historico.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={historico}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" tick={{ fontSize: 10 }} />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="tempSuelo" name="Temp. Suelo (°C)" stroke="#8B5CF6" strokeWidth={2} />
                <Line type="monotone" dataKey="humSuelo" name="Hum. Suelo (%)" stroke="#10B981" strokeWidth={2} />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-64 flex items-center justify-center text-gray-500">
              No hay datos de telemetría disponibles
            </div>
          )}
        </div>
      </div>

      {/* Eficiencia Hídrica */}
      <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">💧 Eficiencia Hídrica</h3>
        {eficiencia ? (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-blue-50 rounded-lg p-4">
              <p className="text-sm text-gray-600">Precipitación Acumulada</p>
              <p className="text-2xl font-bold text-blue-600">{eficiencia.precipitacionAcumulada} mm</p>
            </div>
            <div className="bg-green-50 rounded-lg p-4">
              <p className="text-sm text-gray-600">Humedad Suelo Promedio</p>
              <p className="text-2xl font-bold text-green-600">{eficiencia.humedadSueloPromedio}%</p>
            </div>
            <div className="bg-emerald-50 rounded-lg p-4">
              <p className="text-sm text-gray-600">Estado</p>
              <p className={`text-2xl font-bold ${eficiencia.estado === 'Óptimo' ? 'text-green-600' : 'text-yellow-600'}`}>
                {eficiencia.estado}
              </p>
            </div>
          </div>
        ) : (
          <div className="text-gray-500 text-center py-8">
            Selecciona un dispositivo para ver la eficiencia hídrica
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-xl shadow-sm p-6 border border-gray-100">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">⚡ Accesos Rápidos</h3>
        <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
          <button 
            onClick={() => navigate('/fincas')}
            className="p-4 bg-green-50 rounded-lg hover:bg-green-100 transition-colors text-center"
          >
            <span className="text-2xl">🌾</span>
            <p className="text-sm font-medium text-gray-700 mt-1">Fincas</p>
          </button>
          <button 
            onClick={() => navigate('/lotes')}
            className="p-4 bg-emerald-50 rounded-lg hover:bg-emerald-100 transition-colors text-center"
          >
            <span className="text-2xl">🗺️</span>
            <p className="text-sm font-medium text-gray-700 mt-1">Lotes</p>
          </button>
          <button 
            onClick={() => navigate('/dispositivos')}
            className="p-4 bg-blue-50 rounded-lg hover:bg-blue-100 transition-colors text-center"
          >
            <span className="text-2xl">📡</span>
            <p className="text-sm font-medium text-gray-700 mt-1">Dispositivos</p>
          </button>
          <button 
            onClick={() => navigate('/cultivos')}
            className="p-4 bg-purple-50 rounded-lg hover:bg-purple-100 transition-colors text-center"
          >
            <span className="text-2xl">🌱</span>
            <p className="text-sm font-medium text-gray-700 mt-1">Cultivos</p>
          </button>
          <button 
            onClick={() => navigate('/alertas')}
            className="p-4 bg-red-50 rounded-lg hover:bg-red-100 transition-colors text-center"
          >
            <span className="text-2xl">🔔</span>
            <p className="text-sm font-medium text-gray-700 mt-1">Alertas</p>
          </button>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
