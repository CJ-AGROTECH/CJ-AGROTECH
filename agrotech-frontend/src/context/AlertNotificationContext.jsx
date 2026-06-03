import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import api from '../services/api';
import { getAlertStreamUrl } from '../utils/apiBase';

const AlertNotificationContext = createContext(null);

const mergeAlertas = (prev, incoming) => {
  const map = new Map();
  [...incoming, ...prev].forEach((a) => {
    if (a?.id) {
      map.set(a.id, a);
    }
  });
  return Array.from(map.values()).sort(
    (a, b) => new Date(b.fecha) - new Date(a.fecha)
  );
};

export const AlertNotificationProvider = ({ children }) => {
  const [alertas, setAlertas] = useState([]);
  const [toast, setToast] = useState(null);
  const [notificationsOpen, setNotificationsOpen] = useState(false);
  const prevCountRef = useRef(0);
  const toastTimerRef = useRef(null);

  const playAlertSound = useCallback(() => {
    try {
      const AudioContext = window.AudioContext || window.webkitAudioContext;
      if (!AudioContext) return;
      const context = new AudioContext();
      const oscillator = context.createOscillator();
      const gain = context.createGain();
      oscillator.type = 'sine';
      oscillator.frequency.setValueAtTime(800, context.currentTime);
      gain.gain.setValueAtTime(0.18, context.currentTime);
      oscillator.connect(gain);
      gain.connect(context.destination);
      oscillator.start();
      oscillator.stop(context.currentTime + 0.14);
      oscillator.onended = () => context.close();
    } catch {
      // autoplay bloqueado
    }
  }, []);

  const showToast = useCallback((alerta) => {
    if (!alerta) return;
    setToast(alerta);
    if (toastTimerRef.current) {
      clearTimeout(toastTimerRef.current);
    }
    toastTimerRef.current = setTimeout(() => setToast(null), 8000);
  }, []);

  const pushAlerta = useCallback((alerta, { sound = true, openPanel = false } = {}) => {
    if (!alerta?.id) return;
    setAlertas((prev) => {
      const next = mergeAlertas(prev, [alerta]);
      return next;
    });
    showToast(alerta);
    if (sound) {
      playAlertSound();
    }
    if (openPanel) {
      setNotificationsOpen(true);
    }
  }, [playAlertSound, showToast]);

  const refreshAlertas = useCallback(async () => {
    try {
      const response = await api.get('/alertas/historial/activas');
      const data = response.data || [];
      setAlertas((prev) => mergeAlertas(prev, data));
      if (data.length > prevCountRef.current) {
        playAlertSound();
        if (data[0]) {
          showToast(data[0]);
        }
      }
      prevCountRef.current = data.length;
    } catch (error) {
      console.error('Error fetching alertas:', error);
    }
  }, [playAlertSound, showToast]);

  useEffect(() => {
    refreshAlertas();
    const interval = setInterval(refreshAlertas, 30000);
    return () => clearInterval(interval);
  }, [refreshAlertas]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      return undefined;
    }

    let es;
    let reconnectTimer;
    let closed = false;

    const connect = () => {
      if (closed) return;
      try {
        es = new EventSource(getAlertStreamUrl());
      } catch {
        return;
      }

      es.addEventListener('connected', () => {});

      es.addEventListener('alerta', (event) => {
        try {
          const alerta = JSON.parse(event.data);
          pushAlerta(alerta, { sound: true, openPanel: false });
        } catch {
          // ignore parse errors
        }
      });

      es.onerror = () => {
        es.close();
        if (!closed) {
          reconnectTimer = setTimeout(connect, 5000);
        }
      };
    };

    connect();

    return () => {
      closed = true;
      if (reconnectTimer) clearTimeout(reconnectTimer);
      if (es) es.close();
      if (toastTimerRef.current) clearTimeout(toastTimerRef.current);
    };
  }, [pushAlerta]);

  const dismissToast = () => setToast(null);

  const alertasCount = alertas.length;

  return (
    <AlertNotificationContext.Provider
      value={{
        alertas,
        alertasCount,
        toast,
        dismissToast,
        notificationsOpen,
        setNotificationsOpen,
        refreshAlertas,
        pushAlerta,
      }}
    >
      {children}
    </AlertNotificationContext.Provider>
  );
};

export const useAlertNotifications = () => {
  const ctx = useContext(AlertNotificationContext);
  if (!ctx) {
    throw new Error('useAlertNotifications debe usarse dentro de AlertNotificationProvider');
  }
  return ctx;
};
