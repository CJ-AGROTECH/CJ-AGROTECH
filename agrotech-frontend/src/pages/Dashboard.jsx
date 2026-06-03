import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import api from '../services/api';
import { useAlertNotifications } from '../context/AlertNotificationContext';

const Dashboard = () => {
  const { alertas: alertasContexto, refreshAlertas } = useAlertNotifications();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [fincas, setFincas] = useState([]);
  const [lotes, setLotes] = useState([]);
  const [dispositivos, setDispositivos] = useState([]);
  const [selectedFincaId, setSelectedFincaId] = useState('');
  const [selectedLoteId, setSelectedLoteId] = useState('');
  const [selectedDispositivo, setSelectedDispositivo] = useState(null);
  const [historico, setHistorico] = useState([]);
  const [currentClima, setCurrentClima] = useState(null);
  const [loteClima, setLoteClima] = useState(null);
  const [fincaClima, setFincaClima] = useState(null);
  const [eficiencia, setEficiencia] = useState(null);
  const [alertasActivas, setAlertasActivas] = useState([]);
  const [stats, setStats] = useState({
    totalFincas: 0,
    totalLotes: 0,
    totalDispositivos: 0,
    dispositivosActivos: 0
  });

  const selectedFinca = fincas.find(f => f.id === selectedFincaId);
  const selectedLote = lotes.find(l => l.id === selectedLoteId);
  const displayedClima = loteClima || currentClima;
  const climaSource = loteClima ? 'lote' : currentClima ? 'dispositivo' : '';

  const formatValue = (value, decimals = 1, unit = '') => {
    if (value == null) return '--';
    return `${value.toFixed(decimals)}${unit}`;
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  useEffect(() => {
    setAlertasActivas(alertasContexto);
  }, [alertasContexto]);

  useEffect(() => {
    const interval = setInterval(() => {
      if (selectedFincaId) {
        fetchFincaClima(selectedFincaId);
      }
      if (selectedLoteId) {
        fetchLoteData(selectedLoteId);
      }
      if (selectedDispositivo) {
        fetchDispositivoData(selectedDispositivo);
      }
    }, 900000); // 15 minutos

    return () => clearInterval(interval);
  }, [selectedFincaId, selectedLoteId, selectedDispositivo]);

  const fetchDashboardData = async () => {
    try {
      setLoading(true);
      setError('');

      await refreshAlertas();
      const [fincasResponse, lotesResponse, dispositivosResponse] = await Promise.all([
        api.get('/fincas'),
        api.get('/lotes'),
        api.get('/dispositivos')
      ]);

      const fincasData = fincasResponse.data;
      const lotesData = lotesResponse.data;
      const dispositivosData = dispositivosResponse.data;

      setFincas(fincasData);
      setStats({
        totalFincas: fincasData.length,
        totalLotes: lotesData.length,
        totalDispositivos: dispositivosData.length,
        dispositivosActivos: dispositivosData.filter(d => d.estado === 'ACTIVO').length
      });

      setSelectedFincaId(fincasData[0]?.id || '');
      setLotes(lotesData);
      setDispositivos(dispositivosData);

      if (fincasData[0]?.id) {
        await fetchFincaClima(fincasData[0].id);
        await fetchLotesForFinca(fincasData[0].id);
      } else if (dispositivosData.length > 0) {
        setSelectedDispositivo(dispositivosData[0].id);
        await fetchDispositivoData(dispositivosData[0].id);
      }
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
      setError('Error al cargar los datos del dashboard');
    } finally {
      setLoading(false);
    }
  };

  const fetchLotesForFinca = async (fincaId, currentDevices = null) => {
    try {
      const lotesResponse = await api.get(`/lotes/finca/${fincaId}`);
      const lotesData = lotesResponse.data;
      setLotes(lotesData);
      const loteId = lotesData[0]?.id || '';
      setSelectedLoteId(loteId);

      if (loteId) {
        await fetchDevicesForLote(loteId, currentDevices);
        await fetchLoteData(loteId);
      } else {
        setDispositivos([]);
        setSelectedDispositivo(null);
        setHistorico([]);
        setEficiencia(null);
        setCurrentClima(null);
        setLoteClima(null);
      }
    } catch (error) {
      console.error('Error fetching lotes para finca:', error);
      setError('Error al cargar los lotes de la finca');
    }
  };

  const fetchDevicesForLote = async (loteId, currentDevices = null) => {
    try {
      const dispositivosResponse = await api.get(`/dispositivos/lote/${loteId}`);
      const dispositivosData = dispositivosResponse.data;
      setDispositivos(dispositivosData);
      const dispositivoId = dispositivosData[0]?.id || null;
      setSelectedDispositivo(dispositivoId);
      if (dispositivoId) {
        await fetchDispositivoData(dispositivoId);
      }
    } catch (error) {
      console.error('Error fetching dispositivos para lote:', error);
      setError('Error al cargar los dispositivos del lote');
    }
  };

  const fetchFincaClima = async (fincaId) => {
    try {
      const response = await api.get(`/dashboard/clima-finca/${fincaId}`);
      const data = response.data;
      setFincaClima({
        temperatura: data.tempAire,
        humedadAire: data.humAire,
        presion: data.presion ?? null,
        precipitacion: data.precipitacion ?? null,
        viento: data.viento ?? null,
        humSuelo: data.humSuelo ?? null,
        tempSuelo: data.tempSuelo ?? null,
        fecha: new Date(data.timestamp).toLocaleString('es-CO', {
          day: '2-digit',
          month: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        })
      });
    } catch (error) {
      console.error('Error fetching finca climate:', error);
      setFincaClima(null);
    }
  };

  const fetchLoteHistorico = async (loteId) => {
    try {
      const historicoResponse = await api.get(`/dashboard/historico/lote/${loteId}`);
      const formattedHistorico = historicoResponse.data.map(d => ({
        timestamp: new Date(d.timestamp).toLocaleString('es-CO', {
          day: '2-digit',
          month: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }),
        tempAire: d.lecturas?.ambiente?.tempAire ?? null,
        humAire: d.lecturas?.ambiente?.humAire ?? null,
        presion: d.lecturas?.ambiente?.presion ?? null,
        lux: d.lecturas?.ambiente?.lux ?? null,
        tempSuelo: d.lecturas?.suelo?.tempSuelo ?? null,
        humSuelo: d.lecturas?.suelo?.humSuelo ?? null,
        precipitacion: d.lecturas?.clima?.precipitacion ?? null,
        viento: d.lecturas?.clima?.viento ?? null
      }));
      setHistorico(formattedHistorico);
    } catch (error) {
      console.error('Error fetching lote historico:', error);
      setHistorico([]);
    }
  };

  const fetchLoteEficiencia = async (loteId) => {
    try {
      const efResponse = await api.get(`/dashboard/eficiencia-hidrica/lote/${loteId}`);
      setEficiencia(efResponse.data);
    } catch (error) {
      console.error('Error fetching lote eficiencia:', error);
      setEficiencia(null);
    }
  };

  const fetchLoteData = async (loteId) => {
    await Promise.all([fetchLoteClima(loteId), fetchLoteHistorico(loteId), fetchLoteEficiencia(loteId)]);
  };

  const handleFincaChange = async (e) => {
    const fincaId = e.target.value;
    setSelectedFincaId(fincaId);
    setSelectedLoteId('');
    setSelectedDispositivo(null);
    setHistorico([]);
    setEficiencia(null);
    setLoteClima(null);
    setFincaClima(null);
    if (fincaId) {
      await fetchFincaClima(fincaId);
      await fetchLotesForFinca(fincaId);
    }
  };

  const handleLoteChange = async (e) => {
    const loteId = e.target.value;
    setSelectedLoteId(loteId);
    setSelectedDispositivo(null);
    setHistorico([]);
    setEficiencia(null);
    setCurrentClima(null);
    setLoteClima(null);
    if (loteId) {
      await fetchDevicesForLote(loteId);
      await fetchLoteData(loteId);
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
        tempAire: d.lecturas?.ambiente?.tempAire ?? null,
        humAire: d.lecturas?.ambiente?.humAire ?? null,
        presion: d.lecturas?.ambiente?.presion ?? null,
        lux: d.lecturas?.ambiente?.lux ?? null,
        tempSuelo: d.lecturas?.suelo?.tempSuelo ?? null,
        humSuelo: d.lecturas?.suelo?.humSuelo ?? null,
        precipitacion: d.lecturas?.clima?.precipitacion ?? null,
        viento: d.lecturas?.clima?.viento ?? null
      }));
      setHistorico(formattedHistorico);

      // Fetch eficiencia hídrica
      const efResponse = await api.get(`/dashboard/eficiencia-hidrica/${dispositivoId}`);
      setEficiencia(efResponse.data);

      if (formattedHistorico.length > 0) {
        const latest = formattedHistorico[0];
        setCurrentClima({
          temperatura: latest.tempAire,
          humedad: latest.humAire,
          presion: latest.presion,
          lux: latest.lux,
          tempSuelo: latest.tempSuelo,
          humSuelo: latest.humSuelo,
          fecha: latest.timestamp,
          ubicacion: selectedLote ? `${selectedLote.nombre} • ${selectedFinca?.municipio || ''}` : '',
          precipitacion: latest.precipitacion,
          viento: latest.viento
        });
      } else {
        setCurrentClima(null);
      }
    } catch (error) {
      console.error('Error fetching dispositivo data:', error);
      setCurrentClima(null);
    }
  };

  const fetchLoteClima = async (loteId) => {
    try {
      const response = await api.get(`/dashboard/clima-lote/${loteId}`);
      const data = response.data;
      setLoteClima({
        temperatura: data.tempAire,
        humedad: data.humAire,
        presion: data.presion ?? null,
        lux: data.lux ?? null,
        tempSuelo: data.tempSuelo ?? null,
        humSuelo: data.humSuelo ?? null,
        fecha: new Date(data.timestamp).toLocaleString('es-CO', {
          day: '2-digit',
          month: '2-digit',
          hour: '2-digit',
          minute: '2-digit'
        }),
        ubicacion: selectedLote ? `${selectedLote.nombre} • ${selectedFinca?.municipio || ''}` : '',
        precipitacion: data.precipitacion ?? null,
        viento: data.viento ?? null
      });
    } catch (error) {
      console.error('Error fetching lote climate:', error);
      setLoteClima(null);
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
        <div className="space-y-3 mt-4 md:mt-0 md:flex md:items-center md:space-x-3 md:space-y-0">
          <div className="min-w-[200px]">
            <label className="block text-sm font-medium text-gray-600 mb-1">Finca</label>
            <select
              value={selectedFincaId || ''}
              onChange={handleFincaChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
            >
              <option value="">Seleccionar finca</option>
              {fincas.map(f => (
                <option key={f.id} value={f.id}>{f.nombre}</option>
              ))}
            </select>
          </div>
          <div className="min-w-[200px]">
            <label className="block text-sm font-medium text-gray-600 mb-1">Lote</label>
            <select
              value={selectedLoteId || ''}
              onChange={handleLoteChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              disabled={!selectedFincaId}
            >
              <option value="">Seleccionar lote</option>
              {lotes.map(l => (
                <option key={l.id} value={l.id}>{l.nombre}</option>
              ))}
            </select>
          </div>
          <div className="min-w-[200px]">
            <label className="block text-sm font-medium text-gray-600 mb-1">Dispositivo</label>
            <select
              value={selectedDispositivo || ''}
              onChange={handleDispositivoChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              disabled={!selectedLoteId}
            >
              <option value="">Seleccionar dispositivo</option>
              {dispositivos.map(d => (
                <option key={d.id} value={d.id}>{d.nombre}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {selectedFinca && (
        <div className="grid grid-cols-1 xl:grid-cols-4 gap-4">
          <div className="bg-white rounded-3xl shadow-lg p-6 border border-gray-100">
            <p className="text-sm text-gray-500 uppercase tracking-wide">Ubicación del lote</p>
            <h2 className="mt-2 text-2xl font-semibold text-gray-900">{selectedLote ? selectedLote.nombre : 'Sin lote seleccionado'}</h2>
            <p className="mt-3 text-gray-600">Finca: <span className="font-medium">{selectedFinca.nombre}</span></p>
            <p className="text-gray-600">Municipio: <span className="font-medium">{selectedFinca.municipio}</span></p>
            <p className="text-gray-600">Coordenadas: <span className="font-medium">{selectedLote?.fincaLatitud?.toFixed(6) ?? '-'} , {selectedLote?.fincaLongitud?.toFixed(6) ?? '-'}</span></p>
            <p className="text-gray-500 text-sm mt-2">Esta es la ubicación exacta asociada al lote seleccionado.</p>
          </div>

          <div className="bg-green-50 rounded-3xl shadow-lg p-6 border border-green-100">
            <p className="text-sm uppercase tracking-wide text-green-700">Clima general de finca</p>
            <p className="text-xs uppercase opacity-80">Fuente: clima de la zona</p>
            <h2 className="mt-4 text-4xl font-semibold text-green-900">{formatValue(fincaClima?.temperatura, 1, ' °C')}</h2>
            <p className="mt-1 text-sm text-green-800">{fincaClima?.fecha || 'Último registro reciente'}</p>
            <div className="mt-6 grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="bg-white rounded-3xl p-4">
                <p className="text-xs uppercase tracking-wide text-gray-500">Municipio</p>
                <p className="mt-2 text-lg font-semibold text-gray-900">{selectedFinca.municipio}</p>
              </div>
              <div className="bg-white rounded-3xl p-4">
                <p className="text-xs uppercase tracking-wide text-gray-500">Viento</p>
                <p className="mt-2 text-2xl font-semibold text-gray-900">{formatValue(fincaClima?.viento, 1, ' m/s')}</p>
              </div>
              <div className="bg-white rounded-3xl p-4">
                <p className="text-xs uppercase tracking-wide text-gray-500">Humedad Suelo</p>
                <p className="mt-2 text-2xl font-semibold text-gray-900">{formatValue(fincaClima?.humSuelo, 1, '%')}</p>
              </div>
            </div>
          </div>

          <div className="xl:col-span-2 bg-gradient-to-r from-sky-500 to-cyan-600 rounded-3xl shadow-lg p-6 text-white">
            <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
              <div>
                <p className="text-sm uppercase tracking-wide opacity-80">Clima actual</p>
                <p className="text-xs uppercase opacity-80">{climaSource === 'lote' ? 'Fuente: clima de lote' : 'Fuente: dispositivo'}</p>
                <h2 className="mt-2 text-4xl font-semibold">{formatValue(displayedClima?.temperatura, 1, ' °C')}</h2>
                <p className="mt-1 text-sm opacity-90">{displayedClima?.fecha || 'Último registro reciente'}</p>
              </div>
              <div className="text-right">
                <p className="text-sm opacity-90">Humedad Aire</p>
                <p className="text-4xl font-semibold">{formatValue(displayedClima?.humedad, 0, '%')}</p>
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4 mt-6">
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Precipitación</p>
                <p className="mt-2 text-2xl font-semibold">{formatValue(displayedClima?.precipitacion, 1, ' mm')}</p>
              </div>
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Viento</p>
                <p className="mt-2 text-2xl font-semibold">{formatValue(displayedClima?.viento, 1, ' m/s')}</p>
              </div>
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Presión</p>
                <p className="mt-2 text-2xl font-semibold">{formatValue(displayedClima?.presion, 1, ' hPa')}</p>
              </div>
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Temp. Suelo</p>
                <p className="mt-2 text-2xl font-semibold">{formatValue(displayedClima?.tempSuelo, 1, ' °C')}</p>
              </div>
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Humedad Suelo</p>
                <p className="mt-2 text-2xl font-semibold">{formatValue(displayedClima?.humSuelo, 1, '%')}</p>
              </div>
              <div className="bg-white/10 rounded-3xl p-4">
                <p className="text-xs uppercase opacity-80">Lugar</p>
                <p className="mt-2 text-xl font-semibold">{selectedLote ? selectedLote.nombre : 'Sin lote'}</p>
                <p className="text-sm opacity-80 mt-1">{selectedFinca.municipio}, Antioquia</p>
              </div>
            </div>
          </div>
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
