import { useEffect, useState } from 'react';
import api from '../services/api';

const Alertas = () => {
  const [activeTab, setActiveTab] = useState('historial');
  const [historial, setHistorial] = useState([]);
  const [configuraciones, setConfiguraciones] = useState([]);
  const [dispositivos, setDispositivos] = useState([]);
  const [lotes, setLotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingConfig, setEditingConfig] = useState(null);
  const [formData, setFormData] = useState({
    targetType: 'DISPOSITIVO',
    targetId: '',
    tipo: 'TEMPERATURA',
    prioridad: 'MEDIA',
    umbralMin: '',
    umbralMax: '',
    mensaje: ''
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Fetch historial de alertas
      const historialResponse = await api.get('/alertas/historial/activas');
      setHistorial(historialResponse.data);
      
      // Fetch dispositivos and lotes for config
      try {
        const [dispResponse, lotesResponse] = await Promise.all([
          api.get('/dispositivos'),
          api.get('/lotes')
        ]);
        setDispositivos(dispResponse.data);
        setLotes(lotesResponse.data);

        // Load default configs for the first available target
        if (dispResponse.data.length > 0) {
          const targetId = dispResponse.data[0].id;
          setFormData(prev => ({ ...prev, targetType: 'DISPOSITIVO', targetId }));
          const configResponse = await api.get(`/alertas/configuracion/dispositivo/${targetId}`);
          setConfiguraciones(configResponse.data);
        } else if (lotesResponse.data.length > 0) {
          const targetId = lotesResponse.data[0].id;
          setFormData(prev => ({ ...prev, targetType: 'LOTE', targetId }));
          const configResponse = await api.get(`/alertas/configuracion/lote/${targetId}`);
          setConfiguraciones(configResponse.data);
        }
      } catch (e) {
        setDispositivos([]);
        setLotes([]);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const fetchConfigs = async (targetType, targetId) => {
    try {
      if (!targetId) {
        setConfiguraciones([]);
        return;
      }
      const path = targetType === 'LOTE'
        ? `/alertas/configuracion/lote/${targetId}`
        : `/alertas/configuracion/dispositivo/${targetId}`;
      const response = await api.get(path);
      setConfiguraciones(response.data);
    } catch (error) {
      console.error('Error fetching configs:', error);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleTargetTypeChange = (e) => {
    const targetType = e.target.value;
    setFormData({ ...formData, targetType, targetId: '' });
    setConfiguraciones([]);
  };

  const handleTargetIdChange = (e) => {
    const targetId = e.target.value;
    setFormData({ ...formData, targetId });
    if (targetId) {
      fetchConfigs(formData.targetType, targetId);
    } else {
      setConfiguraciones([]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        dispositivoId: formData.targetType === 'DISPOSITIVO' ? formData.targetId : null,
        loteId: formData.targetType === 'LOTE' ? formData.targetId : null,
        tipo: formData.tipo,
        prioridad: formData.prioridad,
        umbralMin: parseFloat(formData.umbralMin),
        umbralMax: parseFloat(formData.umbralMax),
        mensaje: formData.mensaje
      };

      if (editingConfig) {
        await api.put(`/alertas/configuracion/${editingConfig.id}`, payload);
      } else {
        await api.post('/alertas/configuracion', payload);
      }
      setShowModal(false);
      setEditingConfig(null);
      setFormData({
        targetType: formData.targetType,
        targetId: formData.targetId,
        tipo: 'TEMPERATURA',
        prioridad: 'MEDIA',
        umbralMin: '',
        umbralMax: '',
        mensaje: ''
      });
      if (formData.targetId) {
        fetchConfigs(formData.targetType, formData.targetId);
      }
    } catch (error) {
      console.error('Error saving config:', error);
      setError('Error al guardar la configuración');
    }
  };

  const handleEdit = (config) => {
    setEditingConfig(config);
    setFormData({
      targetType: config.loteId ? 'LOTE' : 'DISPOSITIVO',
      targetId: config.loteId || config.dispositivoId || '',
      tipo: config.tipo,
      prioridad: config.prioridad || 'MEDIA',
      umbralMin: config.umbralMin?.toString() || '',
      umbralMax: config.umbralMax?.toString() || '',
      mensaje: config.mensaje || ''
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar esta regla?')) {
      try {
        await api.delete(`/alertas/configuracion/${id}`);
        if (formData.targetId) {
          fetchConfigs(formData.targetType, formData.targetId);
        }
      } catch (error) {
        console.error('Error deleting config:', error);
        setError('Error al eliminar la configuración');
      }
    }
  };

  const marcarComoVista = async (id) => {
    try {
      await api.patch(`/alertas/historial/${id}/vista`);
      fetchData();
    } catch (error) {
      console.error('Error marking alerta as vista:', error);
    }
  };

  const openNewModal = () => {
    setEditingConfig(null);
    setFormData({
      targetType: formData.targetType || 'DISPOSITIVO',
      targetId: formData.targetId || (dispositivos.length > 0 ? dispositivos[0].id : ''),
      tipo: 'TEMPERATURA',
      umbralMin: '',
      umbralMax: '',
      mensaje: ''
    });
    setShowModal(true);
  };

  const getTipoLabel = (tipo) => {
    const labels = {
      'TEMPERATURA': '🌡️ Temperatura',
      'HUMEDAD': '💧 Humedad',
      'LUMINOSIDAD': '☀️ Luminosidad'
    };
    return labels[tipo] || tipo;
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
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Alertas</h1>
          <p className="text-gray-600 mt-1">Configura y visualiza las alertas del sistema</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100">
        <div className="flex border-b border-gray-200">
          <button
            onClick={() => setActiveTab('historial')}
            className={`flex-1 px-6 py-4 text-center font-medium transition-colors ${
              activeTab === 'historial'
                ? 'text-green-600 border-b-2 border-green-600 bg-green-50'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
            }`}
          >
            📋 Historial de Alertas
            {historial.length > 0 && (
              <span className="ml-2 bg-red-500 text-white text-xs px-2 py-0.5 rounded-full">
                {historial.length}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('configuracion')}
            className={`flex-1 px-6 py-4 text-center font-medium transition-colors ${
              activeTab === 'configuracion'
                ? 'text-green-600 border-b-2 border-green-600 bg-green-50'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
            }`}
          >
            ⚙️ Configuración de Reglas
          </button>
        </div>

        {/* Tab Content */}
        <div className="p-6">
          {error && (
            <div className="p-4 bg-red-50 border border-red-200 rounded-lg mb-4">
              <p className="text-red-600">{error}</p>
            </div>
          )}

          {activeTab === 'historial' && (
            <div className="space-y-4">
              {historial.length > 0 ? (
                historial.map(alerta => (
                  <div key={alerta.id} className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-center justify-between">
                    <div className="flex items-center space-x-4">
                      <div className="w-12 h-12 bg-red-100 rounded-full flex items-center justify-center">
                        <span className="text-2xl">⚠️</span>
                      </div>
                      <div>
                        <p className="font-medium text-gray-900">{alerta.mensaje}</p>
                        <p className="text-sm text-gray-500">
                          {new Date(alerta.fecha).toLocaleString('es-CO')}
                        </p>
                        {alerta.dispositivoNombre && (
                          <p className="text-sm text-gray-600">Dispositivo: {alerta.dispositivoNombre}</p>
                        )}
                        {alerta.loteNombre && (
                          <p className="text-sm text-gray-600">Lote: {alerta.loteNombre}</p>
                        )}
                      </div>
                    </div>
                    <button
                      onClick={() => marcarComoVista(alerta.id)}
                      className="px-4 py-2 bg-white border border-red-300 text-red-600 rounded-lg hover:bg-red-50 transition-colors"
                    >
                      Marcar como vista
                    </button>
                  </div>
                ))
              ) : (
                <div className="text-center py-12">
                  <div className="text-6xl mb-4">✅</div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">No hay alertas activas</h3>
                  <p className="text-gray-600">El sistema está operando correctamente</p>
                </div>
              )}
            </div>
          )}

          {activeTab === 'configuracion' && (
            <div className="space-y-4">
              <div className="grid gap-4 md:grid-cols-2 mb-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Objetivo</label>
                  <select
                    name="targetType"
                    value={formData.targetType}
                    onChange={handleTargetTypeChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  >
                    <option value="DISPOSITIVO">Dispositivo</option>
                    <option value="LOTE">Lote</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Seleccionar {formData.targetType === 'LOTE' ? 'Lote' : 'Dispositivo'}
                  </label>
                  <select
                    name="targetId"
                    value={formData.targetId}
                    onChange={handleTargetIdChange}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  >
                    <option value="">Seleccionar {formData.targetType === 'LOTE' ? 'lote' : 'dispositivo'}</option>
                    {(formData.targetType === 'LOTE' ? lotes : dispositivos).map(item => (
                      <option key={item.id} value={item.id}>{item.nombre}</option>
                    ))}
                  </select>
                </div>
              </div>

              {formData.targetId && (
                <button
                  onClick={openNewModal}
                  className="mb-4 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
                >
                  + Nueva Regla
                </button>
              )}

              {/* Configurations List */}
              {configuraciones.length > 0 ? (
                <div className="space-y-3">
                  {configuraciones.map(config => (
                    <div key={config.id} className="bg-white border border-gray-200 rounded-lg p-4 flex items-center justify-between">
                      <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                          <span className="text-2xl">{getTipoLabel(config.tipo).split(' ')[0]}</span>
                        </div>
                        <div>
                          <p className="font-medium text-gray-900">{getTipoLabel(config.tipo)}</p>
                          <p className="text-sm text-gray-600">
                            Umbral: {config.umbralMin} - {config.umbralMax}
                          </p>
                          {config.mensaje && (
                            <p className="text-sm text-gray-500">{config.mensaje}</p>
                          )}
                          <div className="mt-2 flex flex-wrap gap-2 text-xs">
                            <span className={`px-2 py-1 rounded-full ${config.prioridad === 'ALTA' ? 'bg-red-100 text-red-700' : config.prioridad === 'MEDIA' ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'}`}>
                              {config.prioridad || 'MEDIA'}
                            </span>
                            {(config.loteNombre || config.dispositivoNombre) && (
                              <span className="px-2 py-1 rounded-full bg-gray-100 text-gray-600">
                                {config.loteNombre ? `Lote ${config.loteNombre}` : `Dispositivo ${config.dispositivoNombre}`}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="flex space-x-2">
                        <button
                          onClick={() => handleEdit(config)}
                          className="px-3 py-2 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors"
                        >
                          ✏️
                        </button>
                        <button
                          onClick={() => handleDelete(config.id)}
                          className="px-3 py-2 bg-red-50 text-red-700 rounded-lg hover:bg-red-100 transition-colors"
                        >
                          🗑️
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="text-6xl mb-4">⚙️</div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    {formData.targetId ? 'No hay reglas configuradas' : `Selecciona un ${formData.targetType === 'LOTE' ? 'lote' : 'dispositivo'}`}
                  </h3>
                  <p className="text-gray-600">
                    {formData.targetId ? 'Crea tu primera regla de alerta' : `Para ver las reglas de configuración de ${formData.targetType === 'LOTE' ? 'lote' : 'dispositivo'}`}
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingConfig ? 'Editar Regla' : 'Nueva Regla de Alerta'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Tipo de Alerta
                </label>
                <select
                  name="tipo"
                  value={formData.tipo}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="TEMPERATURA">🌡️ Temperatura</option>
                  <option value="HUMEDAD">💧 Humedad</option>
                  <option value="LUMINOSIDAD">☀️ Luminosidad</option>
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Umbral Mínimo
                  </label>
                  <input
                    type="number"
                    name="umbralMin"
                    value={formData.umbralMin}
                    onChange={handleInputChange}
                    required
                    step="0.1"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    placeholder="15"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Umbral Máximo
                  </label>
                  <input
                    type="number"
                    name="umbralMax"
                    value={formData.umbralMax}
                    onChange={handleInputChange}
                    required
                    step="0.1"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    placeholder="30"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Prioridad
                </label>
                <select
                  name="prioridad"
                  value={formData.prioridad}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="ALTA">Alta</option>
                  <option value="MEDIA">Media</option>
                  <option value="BAJA">Baja</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Mensaje de Alerta
                </label>
                <input
                  type="text"
                  name="mensaje"
                  value={formData.mensaje}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Temperatura fuera de rango"
                />
              </div>
              <div className="flex space-x-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
                >
                  {editingConfig ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Alertas;
