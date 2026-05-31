import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { MapContainer, TileLayer, Marker, Popup, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import markerIcon2x from 'leaflet/dist/images/marker-icon-2x.png';
import markerIcon from 'leaflet/dist/images/marker-icon.png';
import markerShadow from 'leaflet/dist/images/marker-shadow.png';

delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: markerIcon2x,
  iconUrl: markerIcon,
  shadowUrl: markerShadow,
});

const DEFAULT_ANTIOQUIA = { lat: 6.2442, lng: -75.5812 };

const Fincas = () => {
  const navigate = useNavigate();
  const [fincas, setFincas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [climaLoading, setClimaLoading] = useState(false);
  const [notification, setNotification] = useState({ open: false, title: '', message: '', type: '' });
  const [locationLoading, setLocationLoading] = useState(false);
  const [selectedPosition, setSelectedPosition] = useState(null);
  const [addressLabel, setAddressLabel] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingFinca, setEditingFinca] = useState(null);
  const [formData, setFormData] = useState({
    nombre: '',
    municipio: '',
    latitud: '',
    longitud: ''
  });

  useEffect(() => {
    fetchFincas();
  }, []);

  const fetchFincas = async () => {
    try {
      setLoading(true);
      setError('');
      setMessage('');
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
    const { name, value: rawValue } = e.target;
    const value = (name === 'latitud' || name === 'longitud') ? rawValue.replace(',', '.') : rawValue;

    setFormData((current) => {
      const updated = { ...current, [name]: value };
      const lat = Number.parseFloat(updated.latitud);
      const lng = Number.parseFloat(updated.longitud);

      if (!Number.isNaN(lat) && !Number.isNaN(lng)) {
        setSelectedPosition({ lat, lng });
      } else {
        setSelectedPosition(null);
      }

      return updated;
    });
  };

  const reverseGeocode = async (lat, lng) => {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lng}`,
        {
          headers: {
            'Accept-Language': 'es',
            'User-Agent': 'AGROTECH-App/1.0'
          }
        }
      );

      if (!response.ok) {
        return;
      }

      const data = await response.json();
      const address = data.address || {};
      const place =
        address.city ||
        address.town ||
        address.village ||
        address.municipality ||
        address.county ||
        data.display_name;

      setAddressLabel(place || 'Ubicación detectada');
      setFormData((current) => {
        if (current.municipio?.trim()) {
          return current;
        }
        return { ...current, municipio: place || current.municipio };
      });
    } catch (error) {
      console.error('Reverse geocode error:', error);
    }
  };

  const updateCoordinates = (lat, lng) => {
    setSelectedPosition({ lat, lng });
    setFormData((current) => ({
      ...current,
      latitud: lat.toFixed(6),
      longitud: lng.toFixed(6)
    }));
    setError('');
    setMessage('Ubicación seleccionada. Ajusta el marcador en el mapa si necesitas mayor precisión.');
    reverseGeocode(lat, lng);
  };

  const LocationMarker = ({ position, onUpdate }) => {
    const map = useMapEvents({
      click(e) {
        onUpdate(e.latlng);
      }
    });

    useEffect(() => {
      if (position) {
        map.setView(position, map.getZoom());
      }
    }, [position, map]);

    return position ? (
      <Marker
        position={position}
        draggable={true}
        eventHandlers={{
          dragend: (event) => {
            const marker = event.target;
            const newPos = marker.getLatLng();
            onUpdate(newPos);
          }
        }}
      >
        <Popup>Arrastra el marcador para ajustar la ubicación exacta.</Popup>
      </Marker>
    ) : null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const lat = Number.parseFloat(formData.latitud);
    const lng = Number.parseFloat(formData.longitud);
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
      setError('Debes seleccionar una ubicación válida en el mapa o ingresar latitud y longitud correctas.');
      return;
    }
    try {
      const payload = {
        nombre: formData.nombre,
        municipio: formData.municipio,
        latitud: parseFloat(formData.latitud),
        longitud: parseFloat(formData.longitud)
      };

      if (editingFinca) {
        await api.put(`/fincas/${editingFinca.id}`, payload);
      } else {
        await api.post('/fincas', payload);
      }
      setShowModal(false);
      setEditingFinca(null);
      setFormData({ nombre: '', municipio: '', latitud: '', longitud: '' });
      fetchFincas();
    } catch (error) {
      console.error('Error saving finca:', error);
      setError(
        error.response?.data?.message ||
        error.response?.status === 404
          ? 'No se encontró el servicio de fincas. Verifica que el backend esté activo en http://localhost:8080.'
          : 'Error al guardar la finca.'
      );
      setMessage('');
    }
  };

  const showNotification = (title, message, type = 'success') => {
    setNotification({ open: true, title, message, type });
  };

  const closeNotification = () => {
    setNotification((current) => ({ ...current, open: false }));
  };

  const handleEdit = (finca) => {
    setEditingFinca(finca);
    setFormData({
      nombre: finca.nombre,
      municipio: finca.municipio,
      latitud: finca.latitud ?? '',
      longitud: finca.longitud ?? ''
    });
    setSelectedPosition(
      finca.latitud != null && finca.longitud != null
        ? { lat: Number(finca.latitud), lng: Number(finca.longitud) }
        : null
    );
    setAddressLabel(finca.municipio || '');
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

  const handleLoadClima = async (id) => {
    try {
      setClimaLoading(true);
      setError('');
      setMessage('Solicitando datos climáticos al servidor. Esto puede tardar unos segundos.');
      await api.post(`/fincas/${id}/cargar-clima`);
      setMessage('Datos climáticos solicitados correctamente. Revisa el dashboard en unos instantes.');
      showNotification(
        'Clima cargado',
        'La solicitud de clima fue enviada. El dashboard se actualizará cuando los datos estén disponibles.',
        'success'
      );
    } catch (error) {
      console.error('Error cargando clima:', error);
      setError('Error al cargar los datos climáticos');
      setMessage('');
      showNotification(
        'Error',
        'No se pudo cargar los datos climáticos. Verifica la conexión y vuelve a intentarlo.',
        'error'
      );
    } finally {
      setClimaLoading(false);
    }
  };

  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError('Geolocalización no está disponible en este navegador.');
      return;
    }

    setLocationLoading(true);
    setError('');
    setMessage('Buscando ubicación actual...');

    navigator.geolocation.getCurrentPosition(
      (position) => {
        if (!position?.coords) {
          setError('No se pudo obtener la ubicación actual. Intenta nuevamente.');
          setLocationLoading(false);
          return;
        }

        updateCoordinates(position.coords.latitude, position.coords.longitude);
        setMessage('Ubicación actual cargada correctamente. Ajusta el marcador si necesitas más precisión.');
        setLocationLoading(false);
      },
      (err) => {
        console.error('Geolocation error:', err);
        let errorMessage = 'No se pudo obtener la ubicación actual. Revisa permisos de ubicación y vuelve a intentarlo.';

        if (err?.code === err.TIMEOUT) {
          errorMessage = 'Tiempo de espera agotado al obtener la ubicación. Intenta nuevamente o selecciona la ubicación en el mapa.';
        } else if (err?.code === err.PERMISSION_DENIED) {
          errorMessage = 'Permiso de ubicación denegado. Habilítalo en el navegador y vuelve a intentarlo.';
        } else if (err?.code === err.POSITION_UNAVAILABLE) {
          errorMessage = 'No se pudo encontrar la ubicación actual. Verifica que el dispositivo tenga señal GPS o conexión de red.';
        }

        setError(errorMessage);
        setLocationLoading(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 45000,
        maximumAge: 0
      }
    );
  };

  const openNewModal = () => {
    setEditingFinca(null);
    setFormData({ nombre: '', municipio: '', latitud: '', longitud: '' });
    setSelectedPosition(null);
    setAddressLabel('');
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
      {message && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-lg">
          <p className="text-emerald-700">{message}</p>
        </div>
      )}

      {notification.open && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className={`w-full max-w-md rounded-3xl p-6 shadow-2xl ${notification.type === 'error' ? 'bg-red-50 border-red-200 border' : 'bg-white border-gray-200'}`}>
            <div className="mb-4">
              <h3 className="text-xl font-semibold text-gray-900">{notification.title}</h3>
              <p className="mt-2 text-gray-600">{notification.message}</p>
            </div>
            <div className="flex flex-col gap-3 sm:flex-row sm:justify-end">
              {notification.type === 'success' && (
                <button
                  type="button"
                  onClick={() => {
                    closeNotification();
                    navigate('/dashboard');
                  }}
                  className="w-full sm:w-auto px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                >
                  Ver dashboard
                </button>
              )}
              <button
                type="button"
                onClick={closeNotification}
                className="w-full sm:w-auto px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              >
                Cerrar
              </button>
            </div>
          </div>
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
                <div className="flex items-center space-x-2 text-gray-500 text-sm mb-2">
                  <span>📍</span>
                  <span>Lat: {finca.latitud ?? '-'}, Lon: {finca.longitud ?? '-'}</span>
                </div>
                <div className="flex items-center space-x-2 text-gray-500 text-sm mb-4">
                  <span>🆔</span>
                  <span>ID: {finca.id.slice(0, 8)}...</span>
                </div>
                <div className="flex flex-wrap gap-2">
                  <button
                    onClick={() => navigate(`/lotes?finca=${finca.id}`)}
                    className="flex-1 min-w-[120px] px-3 py-2 bg-green-50 text-green-700 rounded-lg hover:bg-green-100 transition-colors text-sm font-medium"
                  >
                    Ver Lotes
                  </button>
                  <button
                    onClick={() => handleLoadClima(finca.id)}
                    disabled={climaLoading}
                    className={`flex-1 min-w-[120px] px-3 py-2 rounded-lg text-sm font-medium transition-colors ${climaLoading ? 'bg-amber-200 text-amber-900 cursor-not-allowed' : 'bg-amber-50 text-amber-700 hover:bg-amber-100'}`}
                  >
                    {climaLoading ? 'Solicitando...' : '🌦️ Cargar Clima'}
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
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-4 md:p-6 max-h-[calc(100vh-3rem)] overflow-hidden">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              {editingFinca ? 'Editar Finca' : 'Nueva Finca'}
            </h2>
            <form onSubmit={handleSubmit} className="flex flex-col h-full">
              <div className="space-y-4 overflow-y-auto pr-2 max-h-[calc(100vh-18rem)]">
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
                <div className="flex flex-col gap-3">
                  <div className="flex flex-col sm:flex-row gap-3">
                    <button
                      type="button"
                      onClick={handleUseCurrentLocation}
                      disabled={locationLoading}
                      className="w-full sm:w-auto px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-60"
                    >
                      {locationLoading ? 'Obteniendo ubicación...' : 'Usar ubicación actual'}
                    </button>
                    <button
                      type="button"
                      onClick={() => updateCoordinates(DEFAULT_ANTIOQUIA.lat, DEFAULT_ANTIOQUIA.lng)}
                      className="w-full sm:w-auto px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
                    >
                      Usar Antioquia, Colombia
                    </button>
                  </div>
                  <p className="text-sm text-gray-500">
                    Usa tu ubicación actual o comienza con la región por defecto de Antioquia, Colombia.
                  </p>
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
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Latitud
                    </label>
                    <input
                      type="number"
                      step="0.000001"
                      name="latitud"
                      value={formData.latitud}
                      onChange={handleInputChange}
                      required
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder="4.7110"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Longitud
                    </label>
                    <input
                      type="number"
                      step="0.000001"
                      name="longitud"
                      value={formData.longitud}
                      onChange={handleInputChange}
                      required
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
                      placeholder="-74.0721"
                    />
                  </div>
                </div>
                {addressLabel && (
                  <div className="text-sm text-gray-600 pt-2">
                    <span className="font-medium">Lugar detectado:</span> {addressLabel}
                  </div>
                )}
                <div className="mt-4">
                  <div className="h-64 sm:h-72 rounded-xl overflow-hidden border border-gray-200">
                    <MapContainer
                      center={selectedPosition || DEFAULT_ANTIOQUIA}
                      zoom={selectedPosition ? 13 : 8}
                      scrollWheelZoom={true}
                      className="h-full w-full"
                    >
                      <TileLayer
                        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                      />
                      <LocationMarker
                        position={selectedPosition}
                        onUpdate={(latlng) => updateCoordinates(latlng.lat, latlng.lng)}
                      />
                    </MapContainer>
                  </div>
                  <p className="text-xs text-gray-500 mt-2">
                    Haz clic en el mapa para seleccionar la ubicación, o arrastra el marcador si ya hay coordenadas. También puedes ingresar latitud y longitud manualmente (usa punto decimal o coma).
                  </p>
                </div>
              </div>
              <div className="flex flex-col sm:flex-row gap-3 pt-4">
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
