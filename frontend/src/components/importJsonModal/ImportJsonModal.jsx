// javascript
import React, { useState } from 'react';
import './ImportJsonModal.css';
import { cityService } from '../../services/cityService.js';
import { useNotification } from '../errorNotification/errorNotification.jsx';

const ImportJsonModal = ({ isOpen, onClose, onImported }) => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const { showSuccess, showError, NotificationComponent } = useNotification();

    if (!isOpen) return null;

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setError('');
    };

    const wait = (ms) => new Promise((res) => setTimeout(res, ms));

    const handleImport = async () => {
        if (!file) {
            setError('Выберите файл');
            return;
        }
        setLoading(true);
        try {
            const result = await cityService.importCities(file);

            if (result && result.status === 'SUCCESS') {
                const duration = 2000;
                showSuccess('Импорт успешно завершён', duration, true);
                if (onImported) onImported();
                await wait(duration);
            } else {
                const msg = result && result.message ? result.message : 'Импорт завершился с ошибкой';
                const duration = 5000;
                showError(msg, duration, true);
                setError(msg);
                await wait(duration);
            }
        } catch (e) {
            const msg = e.message || 'Ошибка импорта';
            const duration = 5000;
            showError(msg, duration, true);
            setError(msg);
            await wait(duration);
        } finally {
            setLoading(false);
            onClose();
        }
    };

    return (
        <div className="modal-overlay">
            <NotificationComponent />
            <div className="modal">
                <h3>Импорт JSON</h3>
                <input type="file" accept=".json" onChange={handleFileChange} />
                {error && <div className="import-error">{error}</div>}
                <div className="modal-actions">
                    <button onClick={onClose} disabled={loading}>Отмена</button>
                    <button onClick={handleImport} disabled={loading}>
                        {loading ? 'Импорт...' : 'Импортировать'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ImportJsonModal;
