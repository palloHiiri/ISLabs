import React, { useState } from 'react';
import './ImportJsonModal.css';
import {cityService} from '../../services/cityService.js';

const ImportJsonModal = ({ isOpen, onClose, onImported }) => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    if (!isOpen) return null;

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
        setError('');
    };

    const handleImport = async () => {
        if (!file) {
            setError('Выберите файл');
            return;
        }
        setLoading(true);
        try {
            const result = await cityService.importCities(file);
            onImported(result);
            onClose();
        } catch (e) {
            setError(e.message || 'Ошибка импорта');
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
                    accept="application/json"
                    onChange={handleFileChange}
                    disabled={loading}
                />
                {error && <div className="error-text">{error}</div>}
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
