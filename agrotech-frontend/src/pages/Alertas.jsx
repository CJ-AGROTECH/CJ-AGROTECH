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
    mensaje: '',
    useComparison: false,
    condicion: 'MAYOR_QUE',
    umbral: ''
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
    setError('');
    if (!formData.targetId) {
      setError('Selecciona un lote o dispositivo antes de crear la regla.');
      return;
    }

    let payload = {
      dispositivoId: formData.targetType === 'DISPOSITIVO' ? formData.targetId : null,
      loteId: formData.targetType === 'LOTE' ? formData.targetId : null,
      tipo: formData.tipo,
      prioridad: formData.prioridad,
      mensaje: formData.mensaje
    };

    if (formData.useComparison) {
      const umbralNum = parseFloat(formData.umbral);
      if (Number.isNaN(umbralNum)) {
        setError('Ingresa un valor numérico válido para el umbral.');
        return;
      }
      if (!formData.condicion) {
        setError('Selecciona una condición para la comparación.');
        return;
      }
      payload = { ...payload, condicion: formData.condicion, umbral: umbralNum };
    } else {
      const umbralMinNum = parseFloat(formData.umbralMin);
      const umbralMaxNum = parseFloat(formData.umbralMax);
      if (Number.isNaN(umbralMinNum) || Number.isNaN(umbralMaxNum)) {
        setError('Ingresa valores numéricos válidos para los umbrales.');
        return;
      }
      if (umbralMinNum >= umbralMaxNum) {
        setError('El umbral mínimo debe ser menor que el máximo.');
        return;
      }
      payload = { ...payload, umbralMin: umbralMinNum, umbralMax: umbralMaxNum };
    }

    try {
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
        mensaje: '',
        useComparison: false,
        condicion: 'MAYOR_QUE',
        umbral: ''
      });
      if (formData.targetId) {
        fetchConfigs(formData.targetType, formData.targetId);
      }
    } catch (error) {
      console.error('Error saving config:', error);
      const msg = error.response?.data?.message || error.response?.data?.error || 'Error al guardar la configuración';
      setError(msg);
    }
  };

  const normalizeTipoForForm = (tipo) => {
    switch (tipo) {
      case 'TEMP_AIRE':
      case 'TEMPERATURA':
        return 'TEMPERATURA';
      case 'HUM_AIRE':
      case 'HUMEDAD':
        return 'HUMEDAD';
      case 'LUX':
      case 'LUMINOSIDAD':
        return 'LUMINOSIDAD';
      default:
        return tipo;
    }
  };

  const handleEdit = (config) => {
    setEditingConfig(config);
    setFormData({
      targetType: config.loteId ? 'LOTE' : 'DISPOSITIVO',
      targetId: config.loteId || config.dispositivoId || '',
      tipo: normalizeTipoForForm(config.tipo),
      prioridad: config.prioridad || 'MEDIA',
      umbralMin: config.umbralMin?.toString() || '',
      umbralMax: config.umbralMax?.toString() || '',
      mensaje: config.mensaje || '',
      useComparison: config.condicion != null || config.umbral != null,
      condicion: config.condicion || 'MAYOR_QUE',
      umbral: config.umbral?.toString() || ''
    });
    setShowModal(true);
  };

  const getTipoLabel = (tipo) => {
    const labels = {
      'TEMPERATURA': '🌡️ Temperatura',
      'HUMEDAD': '💧 Humedad',
      'LUMINOSIDAD': '☀️ Luminosidad',
      'TEMP_AIRE': '🌡️ Temperatura',
      'HUM_AIRE': '💧 Humedad',
      'LUX': '☀️ Luminosidad'
    };
    return labels[tipo] || tipo;
  };

  const getTipoUnit = (tipo) => {
    const units = {
      'TEMPERATURA': '°C',
      'HUMEDAD': '%',
      'LUMINOSIDAD': 'lux',
      'TEMP_AIRE': '°C',
      'HUM_AIRE': '%',
      'LUX': 'lux'
    };
    return units[tipo] || '';
  };

  const getTipoExample = (tipo) => {
    const examples = {
      'TEMPERATURA': { min: '15', max: '30' },
      'HUMEDAD': { min: '20', max: '80' },
      'LUMINOSIDAD': { min: '2000', max: '10000' },
      'TEMP_AIRE': { min: '15', max: '30' },
      'HUM_AIRE': { min: '20', max: '80' },
      'LUX': { min: '2000', max: '10000' }
    };
    return examples[tipo] || { min: '0', max: '100' };
  };

  const getTipoHint = (tipo) => {
    const hints = {
      'TEMPERATURA': 'Ingrese un rango en grados Celsius. Por ejemplo, 15°C a 30°C para condiciones normales de clima de cultivo.',
      'HUMEDAD': 'Ingrese un rango en porcentaje de humedad del suelo. Por ejemplo, 20% a 80% según el tipo de cultivo.',
      'LUMINOSIDAD': 'Ingrese un rango en lux. Por ejemplo, 2000 a 10000 lux para niveles de luz adecuados en campo abierto.',
      'TEMP_AIRE': 'Ingrese un rango en grados Celsius. Por ejemplo, 15°C a 30°C para condiciones normales de clima de cultivo.',
      'HUM_AIRE': 'Ingrese un rango en porcentaje de humedad del aire. Por ejemplo, 20% a 80% según el tipo de cultivo.',
      'LUX': 'Ingrese un rango en lux. Por ejemplo, 2000 a 10000 lux para niveles de luz adecuados en campo abierto.'
    };
    return hints[tipo] || 'Ingrese un rango de valores válido para esta alerta.';
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
      mensaje: '',
      useComparison: false,
      condicion: 'MAYOR_QUE',
      umbral: ''
    });
    setShowModal(true);
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
      <div className="space-y-5">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Gestión de Alertas</h1>
            <p className="text-gray-600 mt-1">Configura y visualiza las alertas del sistema</p>
          </div>
          <button
            onClick={openNewModal}
            className="inline-flex items-center justify-center rounded-full bg-gradient-to-r from-green-500 to-emerald-600 text-white px-5 py-3 text-sm font-semibold shadow-lg hover:from-green-600 hover:to-emerald-700 transition-all"
          >
            + Nueva Regla
          </button>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="rounded-3xl bg-white border border-gray-200 p-4 shadow-sm">
            <p className="text-sm font-medium text-gray-500">Alertas activas</p>
            <p className="mt-3 text-3xl font-bold text-gray-900">{historial.length}</p>
            <p className="mt-2 text-sm text-gray-500">Elementos recientes</p>
          </div>
          <div className="rounded-3xl bg-white border border-gray-200 p-4 shadow-sm">
            <p className="text-sm font-medium text-gray-500">Reglas configuradas</p>
            <p className="mt-3 text-3xl font-bold text-gray-900">{configuraciones.length}</p>
            <p className="mt-2 text-sm text-gray-500">Filtra por lote o dispositivo</p>
          </div>
          <div className="rounded-3xl bg-white border border-gray-200 p-4 shadow-sm">
            <p className="text-sm font-medium text-gray-500">Objetivo actual</p>
            <p className="mt-3 text-lg font-semibold text-gray-900">
              {formData.targetId ? (formData.targetType === 'LOTE' ? 'Lote seleccionado' : 'Dispositivo seleccionado') : 'Sin selección'}
            </p>
            <p className="mt-2 text-sm text-gray-500">Cambia el objetivo para administrar sus reglas</p>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-3xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="flex flex-col sm:flex-row">
          <button
            onClick={() => setActiveTab('historial')}
            className={`flex-1 px-5 py-4 text-center font-medium transition-colors ${
              activeTab === 'historial'
                ? 'text-green-600 bg-green-50'
                : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
            }`}
          >
            📋 Historial de Alertas
            {historial.length > 0 && (
              <span className="ml-2 inline-flex items-center bg-red-500 text-white text-[10px] px-2 py-1 rounded-full">
                {historial.length}
              </span>
            )}
          </button>
          <button
            onClick={() => setActiveTab('configuracion')}
            className={`flex-1 px-5 py-4 text-center font-medium transition-colors ${
              activeTab === 'configuracion'
                ? 'text-green-600 bg-green-50'
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
                  <div key={alerta.id} className="bg-red-50 border border-red-200 rounded-3xl p-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 bg-red-100 rounded-3xl flex items-center justify-center">
                        <span className="text-2xl">⚠️</span>
                      </div>
                      <div>
                        <p className="font-semibold text-gray-900">{alerta.mensaje}</p>
                        <p className="mt-1 text-sm text-gray-500">{new Date(alerta.fecha).toLocaleString('es-CO')}</p>
                        {alerta.dispositivoNombre && (
                          <p className="mt-2 text-sm text-gray-600">Dispositivo: {alerta.dispositivoNombre}</p>
                        )}
                        {alerta.loteNombre && (
                          <p className="mt-1 text-sm text-gray-600">Lote: {alerta.loteNombre}</p>
                        )}
                      </div>
                    </div>
                    <button
                      onClick={() => marcarComoVista(alerta.id)}
                      className="self-start sm:self-center px-4 py-2 bg-white border border-red-300 text-red-600 rounded-full hover:bg-red-50 transition-colors"
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
              <div className="grid gap-4 sm:grid-cols-2 mb-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">Objetivo</label>
                  <select
                    name="targetType"
                    value={formData.targetType}
                    onChange={handleTargetTypeChange}
                    className="w-full px-4 py-3 border border-gray-300 rounded-3xl bg-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
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
                    className="w-full px-4 py-3 border border-gray-300 rounded-3xl bg-white focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  >
                    <option value="">Seleccionar {formData.targetType === 'LOTE' ? 'lote' : 'dispositivo'}</option>
                    {(formData.targetType === 'LOTE' ? lotes : dispositivos).map(item => (
                      <option key={item.id} value={item.id}>{item.nombre}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="rounded-3xl bg-green-50 border border-green-100 p-4 mb-4 text-sm text-green-800">
                <p className="font-semibold">¿Cómo funciona esta alerta?</p>
                <p className="mt-2 text-gray-700">
                  Selecciona un objetivo y define un rango mínimo y máximo. Si el sensor mide un valor fuera de ese rango, el sistema genera una alerta.
                </p>
                <p className="mt-2 text-gray-700">
                  Por ejemplo, para temperatura usamos grados Celsius (°C), para humedad usamos porcentaje (%) y para luminosidad usamos lux.
                </p>
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
                <div className="space-y-4">
                  {configuraciones.map(config => (
                    <div key={config.id} className="bg-white border border-gray-200 rounded-3xl p-4 grid gap-4 sm:grid-cols-[1fr_auto] items-start">
                      <div className="flex items-start gap-4 min-w-0">
                        <div className="w-12 h-12 bg-blue-100 rounded-3xl flex-shrink-0 flex items-center justify-center">
                          <span className="text-2xl">{getTipoLabel(config.tipo).split(' ')[0]}</span>
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-gray-900">{getTipoLabel(config.tipo)}</p>
                          <p className="mt-1 text-sm text-gray-600 break-words">
                            {config.condicion && config.umbral != null 
                              ? `${config.condicion === 'MAYOR_QUE' ? 'Mayor que' : config.condicion === 'MENOR_QUE' ? 'Menor que' : 'Igual a'} ${config.umbral} ${getTipoUnit(config.tipo)}`
                              : `Rango: ${config.umbralMin} - ${config.umbralMax} ${getTipoUnit(config.tipo)}`
                            }
                          </p>
                          {config.mensaje && (
                            <p className="mt-2 text-sm text-gray-500 break-words">"{config.mensaje}"</p>
                          )}
                          <div className="mt-3 flex flex-wrap gap-2 text-xs">
                            <span className={`inline-flex items-center px-2.5 py-1 rounded-full flex-shrink-0 ${config.prioridad === 'ALTA' ? 'bg-red-100 text-red-700' : config.prioridad === 'MEDIA' ? 'bg-yellow-100 text-yellow-700' : config.prioridad === 'BAJA' ? 'bg-green-100 text-green-700' : 'bg-purple-100 text-purple-700'}`}>
                              {config.prioridad || 'MEDIA'}
                            </span>
                            <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-white flex-shrink-0 ${config.condicion && config.umbral != null ? 'bg-indigo-500' : 'bg-orange-500'}`}>
                              {config.condicion && config.umbral != null ? 'Comparación' : 'Rango'}
                            </span>
                            {(config.loteNombre || config.dispositivoNombre) && (
                              <span className="inline-flex items-center px-2.5 py-1 rounded-full bg-gray-100 text-gray-600 flex-shrink-0">
                                {config.loteNombre ? `Lote: ${config.loteNombre}` : `Dispositivo: ${config.dispositivoNombre}`}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      <div className="flex flex-wrap gap-2 justify-end">
                        <button
                          onClick={() => handleEdit(config)}
                          className="px-4 py-2 bg-blue-50 text-blue-700 rounded-full hover:bg-blue-100 transition-colors"
                        >
                          ✏️ Editar
                        </button>
                        <button
                          onClick={() => handleDelete(config.id)}
                          className="px-4 py-2 bg-red-50 text-red-700 rounded-full hover:bg-red-100 transition-colors"
                        >
                          🗑️ Eliminar
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
                <label className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={formData.useComparison}
                    onChange={(e) => setFormData({ ...formData, useComparison: e.target.checked })}
                  />
                  <span className="text-sm font-medium text-gray-700">Usar comparación única (condición + umbral)</span>
                </label>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Tipo de Alerta</label>
                <select
                  name="tipo"
                  value={formData.tipo}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-3xl focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="TEMPERATURA">🌡️ Temperatura</option>
                  <option value="HUMEDAD">💧 Humedad</option>
                  <option value="LUMINOSIDAD">☀️ Luminosidad</option>
                </select>
              </div>

              {formData.useComparison ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Condición</label>
                    <select
                      name="condicion"
                      value={formData.condicion}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-3xl focus:ring-2 focus:ring-green-500 focus:border-transparent"
                    >
                      <option value="MAYOR_QUE">Mayor que</option>
                      <option value="MENOR_QUE">Menor que</option>
                      <option value="IGUAL_A">Igual a</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Umbral ({getTipoUnit(formData.tipo)})</label>
                    <input
                      type="number"
                      name="umbral"
                      value={formData.umbral}
                      onChange={handleInputChange}
                      required={formData.useComparison}
                      step="0.1"
                      className="w-full px-4 py-3 border border-gray-300 rounded-3xl focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder={getTipoExample(formData.tipo).min}
                    />
                  </div>
                </div>
              ) : (
                <>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Umbral Mínimo ({getTipoUnit(formData.tipo)})</label>
                      <input
                        type="number"
                        name="umbralMin"
                        value={formData.umbralMin}
                        onChange={handleInputChange}
                        required={!formData.useComparison}
                        step="0.1"
                        className="w-full px-4 py-3 border border-gray-300 rounded-3xl focus:ring-2 focus:ring-green-500 focus:border-transparent"
                        placeholder={getTipoExample(formData.tipo).min}
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Umbral Máximo ({getTipoUnit(formData.tipo)})</label>
                      <input
                        type="number"
                        name="umbralMax"
                        value={formData.umbralMax}
                        onChange={handleInputChange}
                        required={!formData.useComparison}
                        step="0.1"
                        className="w-full px-4 py-3 border border-gray-300 rounded-3xl focus:ring-2 focus:ring-green-500 focus:border-transparent"
                        placeholder={getTipoExample(formData.tipo).max}
                      />
                    </div>
                  </div>
                  <p className="text-sm text-gray-500 mt-2">{getTipoHint(formData.tipo)}</p>
                </>
              )}

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Prioridad</label>
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
                <label className="block text-sm font-medium text-gray-700 mb-1">Mensaje de Alerta</label>
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
