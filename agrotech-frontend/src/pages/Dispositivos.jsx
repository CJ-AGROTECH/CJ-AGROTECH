import { useEffect, useState } from 'react';
import api from '../services/api';

const Dispositivos = () => {
  const [dispositivos, setDispositivos] = useState([]);
  const [lotes, setLotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingDispositivo, setEditingDispositivo] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    macAddress: '',
    loteId: '',
    estado: 'ACTIVO'
  });

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Fetch lotes for selection
      const lotesResponse = await api.get('/lotes');
      setLotes(lotesResponse.data);
      
      // Fetch all dispositivos (we need to get them from each lote or a different endpoint)
      // For now, let's try to get from a general endpoint or empty
      try {
        const dispResponse = await api.get('/dispositivos');
        setDispositivos(dispResponse.data);
      } catch (e) {
        // If no dispositivos endpoint, we'll show empty
        setDispositivos([]);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        nombre: formData.nombre,
        macAddress: formData.macAddress,
        lote: { id: formData.loteId },
        estado: formData.estado
      };

      if (editingDispositivo) {
        await api.put(`/dispositivos/${editingDispositivo.id}`, payload);
      } else {
        await api.post('/dispositivos', payload);
      }
      setShowModal(false);
      setEditingDispositivo(null);
      setFormData({ nombre: '', macAddress: '', loteId: '', estado: 'ACTIVO' });
      fetchData();
    } catch (error) {
      console.error('Error saving dispositivo:', error);
      setError('Error al guardar el dispositivo');
    }
  };

  const handleEdit = (dispositivo) => {
    setEditingDispositivo(dispositivo);
    setFormData({
      nombre: dispositivo.nombre,
      macAddress: dispositivo.macAddress,
      loteId: dispositivo.loteId || '',
      estado: dispositivo.estado
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar este dispositivo?')) {
      try {
        await api.delete(`/dispositivos/${id}`);
        fetchData();
      } catch (error) {
        console.error('Error deleting dispositivo:', error);
        setError('Error al eliminar el dispositivo');
      }
    }
  };

  const handleEstadoChange = async (id, nuevoEstado) => {
    try {
      await api.patch(`/dispositivos/${id}/estado?estado=${nuevoEstado}`);
      fetchData();
    } catch (error) {
      console.error('Error changing estado:', error);
      setError('Error al cambiar el estado');
    }
  };

  const openNewModal = () => {
    setEditingDispositivo(null);
    setFormData({ 
      nombre: '', 
      macAddress: '', 
      loteId: lotes.length > 0 ? lotes[0].id : '', 
      estado: 'ACTIVO' 
    });
    setShowModal(true);
  };

  const getEstadoColor = (estado) => {
    switch (estado) {
      case 'ACTIVO':
        return 'bg-green-100 text-green-800';
      case 'INACTIVO':
        return 'bg-gray-100 text-gray-800';
      case 'MANTENIMIENTO':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
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
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Dispositivos</h1>
          <p className="text-gray-600 mt-1">Administra los dispositivos IoT</p>
        </div>
        <button
          onClick={openNewModal}
          className="mt-4 md:mt-0 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
        >
          + Nuevo Dispositivo
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {/* Dispositivos Grid */}
      {dispositivos.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {dispositivos.map(dispositivo => (
            <div key={dispositivo.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
              <div className="bg-gradient-to-r from-blue-500 to-indigo-600 p-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-bold text-white">{dispositivo.nombre}</h3>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoColor(dispositivo.estado)}`}>
                    {dispositivo.estado}
                  </span>
                </div>
              </div>
              <div className="p-4">
                <div className="flex items-center space-x-2 text-gray-600 mb-2">
                  <span>📍</span>
                  <span className="text-sm">MAC: {dispositivo.macAddress}</span>
                </div>
                <div className="flex items-center space-x-2 text-gray-500 text-sm mb-4">
                  <span>🆔</span>
                  <span>ID: {dispositivo.id.slice(0, 8)}...</span>
                </div>
                <div className="flex space-x-2 mb-2">
                  <select
                    value={dispositivo.estado}
                    onChange={(e) => handleEstadoChange(dispositivo.id, e.target.value)}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-green-500"
                  >
                    <option value="ACTIVO">Activo</option>
                    <option value="INACTIVO">Inactivo</option>
                    <option value="MANTENIMIENTO">Mantenimiento</option>
                  </select>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEdit(dispositivo)}
                    className="flex-1 px-3 py-2 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors text-sm font-medium"
                  >
                    ✏️ Editar
                  </button>
                  <button
                    onClick={() => handleDelete(dispositivo.id)}
                    className="px-3 py-2 bg-red-50 text-red-700 rounded-lg hover:bg-red-100 transition-colors"
                  >
                    🗑️
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm p-12 text-center">
          <div className="text-6xl mb-4">📡</div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No hay dispositivos registrados</h3>
          <p className="text-gray-600 mb-6">Comienza agregando tu primer dispositivo</p>
          <button
            onClick={openNewModal}
            className="px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
          >
            + Nuevo Dispositivo
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingDispositivo ? 'Editar Dispositivo' : 'Nuevo Dispositivo'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del Dispositivo
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Sensor Temperatura 1"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Dirección MAC
                </label>
                <input
                  type="text"
                  name="macAddress"
                  value={formData.macAddress}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="00:1B:44:11:3A:B7"
                />
                <p className="text-xs text-gray-500 mt-1">La dirección MAC identifica el sensor físico y debe ser única para cada dispositivo. Usa la MAC del módulo de red del sensor o un identificador único equivalente del hardware.</p>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Lote
                </label>
                <select
                  name="loteId"
                  value={formData.loteId}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="">Seleccionar lote</option>
                  {lotes.map(lote => (
                    <option key={lote.id} value={lote.id}>{lote.nombre}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Estado
                </label>
                <select
                  name="estado"
                  value={formData.estado}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="ACTIVO">Activo</option>
                  <option value="INACTIVO">Inactivo</option>
                  <option value="MANTENIMIENTO">Mantenimiento</option>
                </select>
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
                  {editingDispositivo ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Dispositivos;