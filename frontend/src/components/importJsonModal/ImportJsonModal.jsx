import React, { useState, useEffect } from 'react';
import './ImportJsonModal.css';
import { cityService } from '../../services/cityService.js';

const ImportJsonModal = ({ isOpen, onClose, onImported, showSuccess, showError }) => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (isOpen) {
            setFile(null);
            setError('');
            setLoading(false);
        }
    }, [isOpen]);

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
                showSuccess('Импорт успешно завершён', 2000, true);

                if (window.cityWebSocket && window.cityWebSocket.readyState === WebSocket.OPEN) {
                    window.cityWebSocket.send(JSON.stringify({
                        type: 'IMPORT_FINISHED',
                        importId: result.id,
                        status: result.status,
                        addedCount: result.addedCount
                    }));
                }

                if (onImported) onImported(result);
                onClose();
            } else {
                const msg = result && result.message ? result.message : 'Импорт завершился с ошибкой';
                showError(msg, 0, true);
                setError(msg);

                if (window.cityWebSocket && window.cityWebSocket.readyState === WebSocket.OPEN) {
                    window.cityWebSocket.send(JSON.stringify({
                        type: 'IMPORT_FAILED',
                        importId: result?.id,
                        status: result?.status,
                        message: msg
                    }));
                }

                onClose()
            }
        } catch (e) {
            const msg = e.message || 'Ошибка импорта';
            showError(msg, 0, true);
            setError(msg);

            if (window.cityWebSocket && window.cityWebSocket.readyState === WebSocket.OPEN) {
                window.cityWebSocket.send(JSON.stringify({
                    type: 'IMPORT_FAILED',
                    message: msg
                }));
            }

            onClose();
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="modal-overlay">
            <div className="modal">
                <h3>Импорт JSON</h3>
                <input
                    type="file"
                    accept=".json"
                    onChange={handleFileChange}
                />
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
