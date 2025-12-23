import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { cityService } from '../../services/cityService.js';
import './ImportHistory.css';

const ImportHistory = () => {
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [totalPages, setTotalPages] = useState(1);

    const ws = useRef(null);

    const loadPage = async (page = 0, size = pageSize) => {
        setLoading(true);
        setError('');
        try {
            const resp = await cityService.getImportHistory(page, size);

            if (Array.isArray(resp)) {
                setItems(resp);
                setTotalPages(1);
            } else if (resp && typeof resp === 'object') {
                const list = resp.items || resp.content || resp.data || resp.rows || [];
                setItems(Array.isArray(list) ? list : []);
                if (typeof resp.totalPages === 'number') {
                    setTotalPages(resp.totalPages);
                } else if (typeof resp.totalElements === 'number') {
                    setTotalPages(Math.max(1, Math.ceil(resp.totalElements / size)));
                } else if (typeof resp.total === 'number') {
                    setTotalPages(Math.max(1, Math.ceil(resp.total / size)));
                } else {
                    setTotalPages(Array.isArray(list) ? Math.max(1, Math.ceil(list.length / size)) : 1);
                }
            } else {
                setItems([]);
                setTotalPages(1);
            }
        } catch (e) {
            setError(e.message || 'Ошибка загрузки');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPage(currentPage, pageSize);

        ws.current = new WebSocket(`/ws/cities`);

        ws.current.onopen = () => {
            console.log('Import WebSocket connected');
        };

        ws.current.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                console.log('Import WebSocket message received:', data);

                if (data.type === 'CITY_ADDED' || data.type === 'CITY_UPDATED' || data.type === 'CITY_DELETED' ||
                    data.type === 'IMPORT_FAILED') {
                    console.log('Relevant event detected, refreshing import history...');
                    loadPage(currentPage, pageSize);
                }
            } catch (error) {
                console.error('Error parsing Import WebSocket message:', error);
            }
        };

        ws.current.onerror = (error) => {
            console.error('Import WebSocket error:', error);
        };

        ws.current.onclose = (event) => {
            console.log('Import WebSocket disconnected:', event.code, event.reason);
        };

        return () => {
            if (ws.current && ws.current.readyState === WebSocket.OPEN) {
                ws.current.close(1000, 'Component unmounting');
            }
        };
    }, [currentPage, pageSize]);

    const renderStatus = (status) => {
        const s = String(status || '').toUpperCase();
        const cls =
            s === 'SUCCESS' ? 'status-success'
                : s === 'FAILED' ? 'status-failed'
                    : 'status-running';
        return <span className={`status-badge ${cls}`}>{s || 'UNKNOWN'}</span>;
    };

    const getVisiblePages = () => {
        if (totalPages <= 7) {
            return Array.from({ length: totalPages }, (_, i) => i);
        }

        const pages = [];
        const start = Math.max(0, currentPage - 3);
        const end = Math.min(totalPages - 1, currentPage + 3);

        if (start > 0) {
            pages.push(0);
            if (start > 1) pages.push('...');
        }

        for (let i = start; i <= end; i++) pages.push(i);

        if (end < totalPages - 1) {
            if (end < totalPages - 2) pages.push('...');
            pages.push(totalPages - 1);
        }

        return pages;
    };

    const onPageChange = (page) => {
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        setCurrentPage(page);
    };

    const downloadFile = async (id) => {
        try {
            await cityService.downloadImportFile(id);
        } catch (e) {
            setError(e.message || 'Ошибка скачивания файла');
        }
    };

    return (
        <div className="import-history-page">
            <div className="header">
                <h1>История импортов</h1>
                <button
                    onClick={() => navigate('/')}
                    className="header-controls button"
                >
                    ← Back to Cities
                </button>
            </div>

            {loading && <div className="loading">Загрузка...</div>}
            {error && <div className="error-text">{error}</div>}

            {!loading && !error && (
                <div className="city-table-container">
                    <div className="table-container">
                        <div className="table-wrapper">
                            <table className="city-table">
                                <thead className="table-head">
                                <tr>
                                    <th className="table-header">ID операции</th>
                                    <th className="table-header">Статус</th>
                                    <th className="table-header">Добавлено объектов</th>
                                    <th className="table-header">Действия</th>
                                </tr>
                                </thead>
                                <tbody className="table-body">
                                {items.map((op, index) => (
                                    <tr key={op.id ?? index} className="table-row" style={{ '--row-index': index }}>
                                        <td className="table-cell id-cell">{op.id}</td>
                                        <td className="table-cell status-cell">{renderStatus(op.status)}</td>
                                        <td className="table-cell number-cell">
                                            {String(op.status).toUpperCase() === 'SUCCESS' ? (op.addedCount ?? 0) : '-'}
                                        </td>
                                        <td className="table-cell">
                                            <button
                                                onClick={() => downloadFile(op.id)}
                                                disabled={String(op.status).toUpperCase() !== 'SUCCESS'}
                                                className="download-btn"
                                            >
                                                Скачать
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {items.length === 0 && (
                                    <tr>
                                        <td className="table-cell" colSpan="4">Нет записей</td>
                                    </tr>
                                )}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination-wrapper">
                            <div className="pagination-container">
                                <div className="pagination-info">
                                    <span className="pagination-text">
                                        Page <span className="current-page">{currentPage + 1}</span> of <span className="total-pages">{totalPages}</span>
                                    </span>
                                </div>

                                <div className="pagination-controls">
                                    <button
                                        onClick={() => onPageChange(0)}
                                        disabled={currentPage === 0}
                                        className="pagination-btn first-page-btn"
                                        title="First page"
                                    >
                                        <span className="btn-icon">⏮️</span>
                                        First
                                    </button>

                                    <button
                                        onClick={() => onPageChange(currentPage - 1)}
                                        disabled={currentPage === 0}
                                        className="pagination-btn prev-btn"
                                        title="Previous page"
                                    >
                                        <span className="btn-icon">⬅️</span>
                                        Previous
                                    </button>

                                    <div className="page-numbers">
                                        {getVisiblePages().map((page, idx) => (
                                            page === '...' ? (
                                                <span key={`ellipsis-${idx}`} className="pagination-ellipsis">...</span>
                                            ) : (
                                                <button
                                                    key={page}
                                                    onClick={() => onPageChange(page)}
                                                    className={`page-number-btn ${currentPage === page ? 'active' : ''}`}
                                                >
                                                    {page + 1}
                                                </button>
                                            )
                                        ))}
                                    </div>

                                    <button
                                        onClick={() => onPageChange(currentPage + 1)}
                                        disabled={currentPage === totalPages - 1}
                                        className="pagination-btn next-btn"
                                        title="Next page"
                                    >
                                        Next
                                        <span className="btn-icon">➡️</span>
                                    </button>

                                    <button
                                        onClick={() => onPageChange(totalPages - 1)}
                                        disabled={currentPage === totalPages - 1}
                                        className="pagination-btn last-page-btn"
                                        title="Last page"
                                    >
                                        Last
                                        <span className="btn-icon">⏭️</span>
                                    </button>
                                </div>

                                <div className="pagination-quick-jump">
                                    <span className="jump-label">Go to:</span>
                                    <input
                                        type="number"
                                        min="1"
                                        max={totalPages}
                                        placeholder="Page"
                                        className="page-input"
                                        onKeyPress={(e) => {
                                            if (e.key === 'Enter') {
                                                const page = parseInt(e.target.value, 10) - 1;
                                                if (!isNaN(page) && page >= 0 && page < totalPages) {
                                                    onPageChange(page);
                                                    e.target.value = '';
                                                }
                                            }
                                        }}
                                    />
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default ImportHistory;
