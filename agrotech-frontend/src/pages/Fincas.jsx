import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';

const Fincas = () => {
  const navigate = useNavigate();
  const [fincas, setFincas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingFinca, setEditingFinca] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    municipio: '',
    usuarioId: ''
  });

  useEffect(() => {
    fetchFincas();
  }, []);

  const fetchFincas = async () => {
    try {
      setLoading(true);
      const response = await api.get('/fincas');
      setFincas(response.data);
    } catch (error) {
      console.error('Error fetching fincas:', error);
      setError('Error al cargar las fincas');
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
      if (editingFinca) {
        await api.put(`/fincas/${editingFinca.id}`, formData);
      } else {
        await api.post('/fincas', formData);
      }
      setShowModal(false);
      setEditingFinca(null);
      setFormData({ nombre: '', municipio: '', usuarioId: '' });
      fetchFincas();
    } catch (error) {
      console.error('Error saving finca:', error);
      setError('Error al guardar la finca');
    }
  };

  const handleEdit = (finca) => {
    setEditingFinca(finca);
    setFormData({
      nombre: finca.nombre,
      municipio: finca.municipio,
      usuarioId: finca.usuarioId
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar esta finca?')) {
      try {
        await api.delete(`/fincas/${id}`);
        fetchFincas();
      } catch (error) {
        console.error('Error deleting finca:', error);
        setError('Error al eliminar la finca');
      }
    }
  };

  const openNewModal = () => {
    setEditingFinca(null);
    setFormData({ nombre: '', municipio: '', usuarioId: '' });
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
      <div className="flex flex-col md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Fincas</h1>
          <p className="text-gray-600 mt-1">Administra tus propiedades agrícolas</p>
        </div>
        <button
          onClick={openNewModal}
          className="mt-4 md:mt-0 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
        >
          + Nueva Finca
        </button>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}

      {/* Fincas Grid */}
      {fincas.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {fincas.map(finca => (
            <div key={finca.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
              <div className="bg-gradient-to-r from-green-500 to-emerald-600 p-4">
                <h3 className="text-xl font-bold text-white">{finca.nombre}</h3>
              </div>
              <div className="p-4">
                <div className="flex items-center space-x-2 text-gray-600 mb-3">
                  <span>📍</span>
                  <span>{finca.municipio}</span>
                </div>
                <div className="flex items-center space-x-2 text-gray-500 text-sm mb-4">
                  <span>🆔</span>
                  <span>ID: {finca.id.slice(0, 8)}...</span>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => navigate(`/lotes?finca=${finca.id}`)}
                    className="flex-1 px-3 py-2 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 transition-colors text-sm font-medium"
                  >
                    Ver Lotes
                  </button>
                  <button
                    onClick={() => handleEdit(finca)}
                    className="px-3 py-2 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors"
                  >
                    ✏️
                  </button>
                  <button
                    onClick={() => handleDelete(finca.id)}
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
          <div className="text-6xl mb-4">🌾</div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No hay fincas registradas</h3>
          <p className="text-gray-600 mb-6">Comienza agregando tu primera finca</p>
          <button
            onClick={openNewModal}
            className="px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
          >
            + Nueva Finca
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingFinca ? 'Editar Finca' : 'Nueva Finca'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre de la Finca
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Finca La Esperanza"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Municipio
                </label>
                <input
                  type="text"
                  name="municipio"
                  value={formData.municipio}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Rionegro"
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
                  {editingFinca ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Fincas;
