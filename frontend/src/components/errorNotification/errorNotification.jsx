import React, { useState, useEffect } from 'react';
import './ErrorNotification.css';

const ErrorNotification = ({
                               message,
                               type = 'error',
                               onClose,
                               duration = 5000,
                               isModal = true
                           }) => {
    const [isVisible, setIsVisible] = useState(false);
    const [isExiting, setIsExiting] = useState(false);

    useEffect(() => {
        if (message) {
            setIsVisible(true);
            setIsExiting(false);

            if (duration > 0) {
                const timer = setTimeout(() => {
                    handleClose();
                }, duration);

                return () => clearTimeout(timer);
            }
        }
    }, [message, duration]);

    const handleClose = () => {
        setIsExiting(true);
        setTimeout(() => {
            setIsVisible(false);
            if (onClose) {
                onClose();
            }
        }, 300);
    };

    const handleOverlayClick = (e) => {
        if (e.target === e.currentTarget) {
            handleClose();
        }
    };

    if (!message || !isVisible) return null;

    const getIcon = () => {
        switch (type) {
            case 'success':
                return '✅';
            case 'warning':
                return '⚠️';
            case 'info':
                return 'ℹ️';
            case 'error':
            default:
                return '❌';
        }
    };

    const getTypeClass = () => {
        switch (type) {
            case 'success':
                return 'notification-success';
            case 'warning':
                return 'notification-warning';
            case 'info':
                return 'notification-info';
            case 'error':
            default:
                return 'notification-error';
        }
    };

    if (isModal) {
        return (
            <div
                className={`notification-modal-overlay ${isExiting ? 'notification-modal-overlay-exit' : ''}`}
                onClick={handleOverlayClick}
            >
                <div className={`notification-modal ${getTypeClass()} ${isExiting ? 'notification-modal-exit' : ''}`}>
                    <div className="notification-modal-header">
                        <div className="notification-modal-icon">
                            {getIcon()}
                        </div>
                        <div className="notification-modal-title">
                            {type === 'error' ? 'Error' :
                                type === 'success' ? 'Success' :
                                    type === 'warning' ? 'Warning' :
                                        'Information'}
                        </div>
                        <button
                            onClick={handleClose}
                            className="notification-modal-close"
                        >
                            ×
                        </button>
                    </div>

                    <div className="notification-modal-content">
                        <p>{message}</p>
                    </div>

                    <div className="notification-modal-actions">
                        <button
                            onClick={handleClose}
                            className={`notification-modal-button ${getTypeClass()}`}
                        >
                            OK
                        </button>
                    </div>

                    {duration > 0 && (
                        <div className="notification-modal-progress">
                            <div
                                className={`notification-modal-progress-bar ${getTypeClass()}`}
                                style={{
                                    animationDuration: `${duration}ms`
                                }}
                            />
                        </div>
                    )}
                </div>
            </div>
        );
    }

    return (
        <div className={`notification-toast ${getTypeClass()} ${isExiting ? 'notification-toast-exit' : ''}`}>
            <div className="notification-toast-content">
                <div className="notification-toast-icon">
                    {getIcon()}
                </div>
                <div className="notification-toast-message">
                    <p>{message}</p>
                </div>
                <button
                    onClick={handleClose}
                    className="notification-toast-close"
                >
                    ×
                </button>
            </div>

            {duration > 0 && (
                <div className="notification-toast-progress">
                    <div
                        className={`notification-toast-progress-bar ${getTypeClass()}`}
                        style={{
                            animationDuration: `${duration}ms`
                        }}
                    />
                </div>
            )}
        </div>
    );
};

export const useNotification = () => {
    const [notification, setNotification] = useState({
        message: '',
        type: 'info',
        show: false,
        duration: 5000,
        isModal: true
    });

    const showNotification = (message, type = 'info', duration = 5000, isModal = true) => {
        setNotification({
            message,
            type,
            show: true,
            duration,
            isModal
        });
    };

    const showError = (message, duration = 0, isModal = true) => {
        showNotification(message, 'error', duration, isModal);
    };

    const showSuccess = (message, duration = 3000, isModal = true) => {
        showNotification(message, 'success', duration, isModal);
    };

    const showWarning = (message, duration = 4000, isModal = true) => {
        showNotification(message, 'warning', duration, isModal);
    };

    const showInfo = (message, duration = 3000, isModal = true) => {
        showNotification(message, 'info', duration, isModal);
    };

    const hideNotification = () => {
        setNotification(prev => ({ ...prev, show: false, message: '' }));
    };

    const NotificationComponent = () => (
        <ErrorNotification
            message={notification.show ? notification.message : ''}
            type={notification.type}
            onClose={hideNotification}
            duration={notification.duration}
            isModal={notification.isModal}
        />
    );

    return {
        showNotification,
        showError,
        showSuccess,
        showWarning,
        showInfo,
        hideNotification,
        NotificationComponent
    };
};

export default ErrorNotification;