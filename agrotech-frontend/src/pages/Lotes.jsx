import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import api from '../services/api';

const Lotes = () => {
  const [searchParams] = useSearchParams();
  const [lotes, setLotes] = useState([]);
  const [fincas, setFincas] = useState([]);
  const [cultivos, setCultivos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingLote, setEditingLote] = useState(null);
  const [selectedFincaId, setSelectedFincaId] = useState(searchParams.get('finca') || '');
  const [formData, setFormData] = useState({
    nombre: '',
    areaHectareas: '',
    fincaId: '',
    cultivoId: ''
  });

  useEffect(() => {
    fetchData();
  }, [selectedFincaId]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Fetch fincas and cultivos in parallel
      const [fincasResponse, cultivosResponse] = await Promise.all([
        api.get('/fincas'),
        api.get('/cultivos')
      ]);
      setFincas(fincasResponse.data);
      setCultivos(cultivosResponse.data);
      
      // Fetch lotes (filtered by finca if selected)
      let lotesUrl = '/lotes';
      if (selectedFincaId) {
        lotesUrl = `/lotes/finca/${selectedFincaId}`;
      }
      const lotesResponse = await api.get(lotesUrl);
      setLotes(lotesResponse.data);
    } catch (error) {
      console.error('Error fetching data:', error);
      setError('Error al cargar los datos');
      setMessage('');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFincaFilterChange = (e) => {
    setSelectedFincaId(e.target.value);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        nombre: formData.nombre,
        areaHectareas: parseFloat(formData.areaHectareas),
        fincaId: formData.fincaId,
        cultivoId: formData.cultivoId
      };

      if (editingLote) {
        await api.put(`/lotes/${editingLote.id}`, payload);
      } else {
        await api.post('/lotes', payload);
      }
      setShowModal(false);
      setEditingLote(null);
      setFormData({ nombre: '', areaHectareas: '', fincaId: '', cultivoId: '' });
      setError('');
      setMessage('Lote guardado correctamente.');
      fetchData();
    } catch (error) {
      console.error('Error saving lote:', error);
      setError('Error al guardar el lote');
      setMessage('');
    }
  };

  const handleEdit = (lote) => {
    setEditingLote(lote);
    setFormData({
      nombre: lote.nombre,
      areaHectareas: lote.areaHectareas?.toString() || '',
      fincaId: lote.fincaId,
      cultivoId: lote.cultivoId
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('¿Estás seguro de que deseas eliminar este lote?')) {
      try {
        await api.delete(`/lotes/${id}`);
        setError('');
        setMessage('Lote eliminado correctamente.');
        fetchData();
      } catch (error) {
        console.error('Error deleting lote:', error);
        setError('Error al eliminar el lote');
        setMessage('');
      }
    }
  };

  const handleLoadClima = async (id) => {
    try {
      await api.post(`/lotes/${id}/cargar-clima`);
      setError('');
      setMessage('Datos climáticos solicitados correctamente.');
    } catch (error) {
      console.error('Error cargando clima:', error);
      setError('Error al cargar los datos climáticos');
      setMessage('');
    }
  };

  const openNewModal = () => {
    setEditingLote(null);
    setFormData({ 
      nombre: '', 
      areaHectareas: '', 
      fincaId: selectedFincaId || (fincas.length > 0 ? fincas[0].id : ''), 
      cultivoId: cultivos.length > 0 ? cultivos[0].id : '' 
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
      <div className="flex flex-col md:flex-row md:items-center md:justify-between">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Gestión de Lotes</h1>
          <p className="text-gray-600 mt-1">Administra los lotes de tus fincas</p>
        </div>
        <button
          onClick={openNewModal}
          className="mt-4 md:mt-0 px-4 py-2 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
        >
          + Nuevo Lote
        </button>
      </div>

      {/* Filter */}
      <div className="bg-white rounded-xl shadow-sm p-4 border border-gray-100">
        <label className="block text-sm font-medium text-gray-700 mb-2">Filtrar por Finca</label>
        <select
          value={selectedFincaId}
          onChange={handleFincaFilterChange}
          className="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
        >
          <option value="">Todas las fincas</option>
          {fincas.map(finca => (
            <option key={finca.id} value={finca.id}>{finca.nombre}</option>
          ))}
        </select>
      </div>

      {/* Error Message */}
      {error && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-lg">
          <p className="text-red-600">{error}</p>
        </div>
      )}
      {message && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
          <p className="text-emerald-700">{message}</p>
        </div>
      )}

      {/* Lotes Grid */}
      {lotes.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {lotes.map(lote => (
            <div key={lote.id} className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md transition-shadow">
              <div className="bg-gradient-to-r from-emerald-500 to-teal-600 p-4">
                <h3 className="text-xl font-bold text-white">{lote.nombre}</h3>
              </div>
              <div className="p-4">
                <div className="flex items-center space-x-2 text-gray-600 mb-2">
                  <span>📏</span>
                  <span>{lote.areaHectareas} hectáreas</span>
                </div>
                <div className="flex items-center space-x-2 text-gray-500 text-sm mb-4">
                  <span>🆔</span>
                  <span>ID: {lote.id.slice(0, 8)}...</span>
                </div>
                <div className="flex space-x-2">
                  <button
                    onClick={() => handleEdit(lote)}
                    className="flex-1 px-3 py-2 bg-blue-50 text-blue-700 rounded-lg hover:bg-blue-100 transition-colors text-sm font-medium"
                  >
                    ✏️ Editar
                  </button>
                  <button
                    onClick={() => handleLoadClima(lote.id)}
                    className="flex-1 px-3 py-2 bg-amber-50 text-amber-700 rounded-lg hover:bg-amber-100 transition-colors text-sm font-medium"
                  >
                    🌦️ Cargar Clima
                  </button>
                  <button
                    onClick={() => handleDelete(lote.id)}
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
          <div className="text-6xl mb-4">🗺️</div>
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No hay lotes registrados</h3>
          <p className="text-gray-600 mb-6">Comienza agregando tu primer lote</p>
          <button
            onClick={openNewModal}
            className="px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 text-white rounded-lg hover:from-green-600 hover:to-emerald-700 transition-all"
          >
            + Nuevo Lote
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-md w-full p-6">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingLote ? 'Editar Lote' : 'Nuevo Lote'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Nombre del Lote
                </label>
                <input
                  type="text"
                  name="nombre"
                  value={formData.nombre}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="Lote A1"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Área (hectáreas)
                </label>
                <input
                  type="number"
                  name="areaHectareas"
                  value={formData.areaHectareas}
                  onChange={handleInputChange}
                  required
                  step="0.01"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                  placeholder="10.5"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Finca
                </label>
                <select
                  name="fincaId"
                  value={formData.fincaId}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="">Seleccionar finca</option>
                  {fincas.map(finca => (
                    <option key={finca.id} value={finca.id}>{finca.nombre}</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Cultivo
                </label>
                <select
                  name="cultivoId"
                  value={formData.cultivoId}
                  onChange={handleInputChange}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                >
                  <option value="">Seleccionar cultivo</option>
                  {cultivos.map(cultivo => (
                    <option key={cultivo.id} value={cultivo.id}>{cultivo.nombre}</option>
                  ))}
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
                  {editingLote ? 'Actualizar' : 'Crear'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Lotes;