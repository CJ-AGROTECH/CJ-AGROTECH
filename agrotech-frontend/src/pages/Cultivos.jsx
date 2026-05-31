import { useEffect, useState } from 'react';
import api from '../services/api';

const Cultivos = () => {
  const [cultivos, setCultivos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingCultivo, setEditingCultivo] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    variedad: '',
    descripcion: '',
    diasCrecimiento: ''
  });

  useEffect(() => {
    fetchCultivos();
  }, []);

  const fetchCultivos = async () => {
    try {
      setLoading(true);
      const response = await api.get('/cultivos');
      setCultivos(response.data);
    } catch (error) {
      console.error('Error fetching cultivos:', error);
      setError('Error al cargar los cultivos');
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
        variedad: formData.variedad,
        descripcion: formData.descripcion,
        diasCrecimiento: parseInt(formData.diasCrecimiento)
      };

      if (editingCultivo) {
        await api.put(`/cultivos/${editingCultivo.id}`, payload);
      } else {
        await api.post('/cultivos', payload);
      }
      setShowModal(false);
      setEditingCultivo(null);
      setFormData({ nombre: '', variedad: '', descripcion: '', diasCrecimiento: '' });
      fetchCultivos();
    } catch (error) {
      console.error('Error saving cultivo:', error);
      const message = error.response?.data?.message || error.response?.data?.error || 'Error al guardar el cultivo';
      setError(message);
    }
  };

  const handleEdit = (cultivo) => {
    setEditingCultivo(cultivo);
    setFormData({
      nombre: cultivo.nombre,
      variedad: cultivo.variedad || '',
      descripcion: cultivo.descripcion || '',
      diasCrecimiento: cultivo.diasCrecimiento?.toString() || ''
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar este cultivo?')) {
      try {
        await api.delete(`/cultivos/${id}`);
        fetchCultivos();
      } catch (error) {
        console.error('Error deleting cultivo:', error);
        setError('Error al eliminar el cultivo');
      }
    }
  };

  const openNewModal = () => {
    setEditingCultivo(null);
    setFormData({ nombre: '', variedad: '', descripcion: '', diasCrecimiento: '' });
    setShowModal(true);
  };

  const getCultivoIcon = (nombre) => {
    const icons = {
      'café': '☕',
      'frijol': '🫘',
      'maíz': '🌽',
      'tomate': '🍅',
      'papa': '🥔',
      'zanahoria': '🥕',
      'lechuga': '🥬',
      'aguacate': '🥑',
      'plátano': '🍌',
      'default': '🌱'
    };
    const key = nombre?.toLowerCase() || '';
    for (const [k, v] of Object.entries(icons)) {
      if (key.includes(k)) return v;
    }
    return icons.default;
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
          <h1 className="text-3xl font-bold text-gray-900">Catálogo de Cultivos</h1>
          <p className="text-gray-600 mt-1">Administra los tipos de cultivo</p>
        </div>
        <button
          onClick={openNewModal}
          className="mt-4 md:mt-0 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
        >
          + Nuevo Cultivo
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {/* Cultivos Grid */}
      {cultivos.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {cultivos.map(cultivo => (
            <div key={cultivo.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
              <div className="bg-gradient-to-r from-green-500 to-emerald-600 p-4 flex items-center space-x-3">
                <span className="text-4xl">{getCultivoIcon(cultivo.nombre)}</span>
                <div>
                  <h3 className="text-xl font-bold text-white">{cultivo.nombre}</h3>
                  {cultivo.variedad && (
                    <p className="text-green-100 text-sm">{cultivo.variedad}</p>
                  )}
                </div>
              </div>
              <div className="p-4">
                {cultivo.descripcion && (
                  <p className="text-gray-600 text-sm mb-2">{cultivo.descripcion}</p>
                )}
                {cultivo.diasCrecimiento && (
                  <div className="flex items-center space-x-2 text-gray-500 text-sm mb-4">
                    <span>📅</span>
                    <span>{cultivo.diasCrecimiento} días de crecimiento</span>
                  </div>
                )}
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEdit(cultivo)}
                    className="flex-1 px-3 py-2 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors text-sm font-medium"
                  >
                    ✏️ Editar
                  </button>
                  <button
                    onClick={() => handleDelete(cultivo.id)}
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
          <div className="text-6xl mb-4">🌱</div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No hay cultivos registrados</h3>
          <p className="text-gray-600 mb-6">Comienza agregando tu primer cultivo</p>
          <button
            onClick={openNewModal}
            className="px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
          >
            + Nuevo Cultivo
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingCultivo ? 'Editar Cultivo' : 'Nuevo Cultivo'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del Cultivo
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Café"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Variedad
                </label>
                <input
                  type="text"
                  name="variedad"
                  value={formData.variedad}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Caturra"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descripción
                </label>
                <textarea
                  name="descripcion"
                  value={formData.descripcion}
                  onChange={handleInputChange}
                  rows="3"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Cultivo de café arabico..."
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Días de Crecimiento
                </label>
                <input
                  type="number"
                  name="diasCrecimiento"
                  value={formData.diasCrecimiento}
                  onChange={handleInputChange}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="180"
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
                  {editingCultivo ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cultivos;