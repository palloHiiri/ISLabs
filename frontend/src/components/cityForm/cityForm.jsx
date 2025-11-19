import React, { useState, useEffect } from 'react';
import { cityService } from '../../services/cityService.js';
import { useNotification } from '../errorNotification/errorNotification.jsx';
import './CityForm.css';

const CityForm = ({ city, onSave, onCancel }) => {
    const { showError, showSuccess, NotificationComponent } = useNotification();
    const [serverDown, setServerDown] = useState(false);
    const [activeTab, setActiveTab] = useState('basic');
    const [formData, setFormData] = useState({
        name: '',
        coordinates: { x: 0, y: 0 },
        area: 0,
        population: 0,
        establishmentDate: '',
        capital: false,
        metersAboveSeaLevel: null,
        timezone: 0,
        carCode: null,
        government: 'ARISTOCRACY',
        governor: { name: '', passport: 0},
        postalCode: 0,
        oktmo: 0,
    });
    const [errors, setErrors] = useState({});
    const [isLoading, setIsLoading] = useState(false);

    const [existingData, setExistingData] = useState({
        governors: [],
        coordinates: [],
        timezones: [],
        carCodes: []
    });
    const [isLoadingExistingData, setIsLoadingExistingData] = useState(false);

    const [inputModes, setInputModes] = useState({
        governor: 'input',
        coordinates: 'input',
    });

    useEffect(() => {
        if (city) {
            setFormData({
                ...city,
                establishmentDate: city.establishmentDate || '',
                metersAboveSeaLevel: city.metersAboveSeaLevel || null,
                carCode: city.carCode || null,
                postalCode: city.postalCode || '',
                oktmo: city.oktmo || ''
            });
        }
        loadExistingData();
    }, [city]);

    const loadExistingData = async () => {
        setIsLoadingExistingData(true);
        try {
            const [governors, coordinates] = await Promise.all([
                cityService.getGovernors(),
                cityService.getCoordinates()
            ]);

            const timezones = [...new Set(coordinates.map(coord => coord.timezone))].sort((a, b) => a - b);
            const carCodes = [...new Set(coordinates.map(coord => coord.carCode).filter(code => code !== null))].sort((a, b) => a - b);

            setExistingData({
                governors: governors,
                coordinates: coordinates,
                timezones,
                carCodes
            });
            setServerDown(false);
        } catch (error) {
            console.error('Error loading existing data:', error);
            showError('Failed to load existing data');
            setServerDown(true);
        } finally {
            setIsLoadingExistingData(false);
        }
    };


    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;

        if (name.includes('.')) {
            const [parent, child] = name.split('.');
            setFormData(prev => ({
                ...prev,
                [parent]: {
                    ...prev[parent],
                    [child]: type === 'number' ? (value === '' ? null : Number(value)) : value
                }
            }));
        } else {
            setFormData(prev => ({
                ...prev,
                [name]: type === 'checkbox' ? checked :
                    type === 'number' ? (value === '' ? null : Number(value)) : value
            }));
        }

        if (errors[name] || (name === 'governor.name' && errors.governorName)) {
            setErrors(prev => {
                const newErrors = { ...prev };
                if (name === 'governor.name') {
                    delete newErrors.governorName;
                } else {
                    delete newErrors[name];
                }
                return newErrors;
            });
        }
    };

    const handleSelectChange = (field, value) => {
        switch (field) {
            case 'governor':
                setFormData(prev => ({
                    ...prev,
                    governor: { name: value }
                }));
                if (errors.governorName) {
                    setErrors(prev => ({ ...prev, governorName: null }));
                }
                break;
            case 'coordinates':
                const selectedCoord = existingData.coordinates.find(coord =>
                    coord.x === value.x && coord.y === value.y
                );
                if (selectedCoord) {
                    setFormData(prev => ({
                        ...prev,
                        coordinates: { x: selectedCoord.x, y: selectedCoord.y },
                        timezone: selectedCoord.timezone,
                        carCode: selectedCoord.carCode
                    }));
                }
                break;
            case 'timezone':
                setFormData(prev => ({
                    ...prev,
                    timezone: Number(value)
                }));
                if (errors.timezone) {
                    setErrors(prev => ({ ...prev, timezone: null }));
                }
                break;
            case 'carCode':
                setFormData(prev => ({
                    ...prev,
                    carCode: value === '' ? null : Number(value)
                }));
                if (errors.carCode) {
                    setErrors(prev => ({ ...prev, carCode: null }));
                }
                break;
            default:
                break;
        }
    };

    const toggleInputMode = (field) => {
        setInputModes(prev => ({
            ...prev,
            [field]: prev[field] === 'input' ? 'select' : 'input'
        }));
    };

    const validateForm = () => {
        const newErrors = {};

        if (!formData.name?.trim()) newErrors.name = 'Name is required';
        if (!formData.population || formData.population <= 0) newErrors.population = 'Population must be > 0';
        if (!formData.area || formData.area <= 0) newErrors.area = 'Area must be > 0';
        if (formData.timezone < -13 || formData.timezone > 15) newErrors.timezone = 'Timezone must be between -13 and 15';
        if (!formData.governor?.name?.trim()) newErrors.governorName = 'Governor name is required';
        if (formData.coordinates.x > 913) newErrors.coordX = 'X must be ≤ 913';
        if (formData.coordinates.y <= -243) newErrors.coordY = 'Y must be > -243';
        if (formData.carCode !== null && (formData.carCode <= 0 || formData.carCode > 1000)) {
            newErrors.carCode = 'Car code must be between 1 and 1000';
        }
        if (formData.establishmentDate) {
            const picked = new Date(formData.establishmentDate);
            const today = new Date();
            picked.setHours(0,0,0,0);
            today.setHours(0,0,0,0);
            if (picked > today) {
                newErrors.establishmentDate = 'Establishment date cannot be in the future';
            }
        }
        if(formData.postalCode < 100000 || formData.postalCode > 999999){
            newErrors.postalCode = 'Postal code must be exactly 6 characters long';
        }
        if(formData.governor.passport < 1000000000 || formData.governor.passport > 9999999999){
            newErrors.passport = 'Passport number must be exactly 10 characters long';
        }
        if(formData.oktmo < 10000000 || formData.oktmo > 99999999){
            newErrors.oktmo = 'oktmo code must be exactly 8 characters long';
        }

        setErrors(newErrors);

        if (Object.keys(newErrors).length > 0) {
            const firstErrorField = Object.keys(newErrors)[0];
            if (['name', 'area', 'population'].includes(firstErrorField)) setActiveTab('basic');
            else if (firstErrorField.startsWith('coord')) setActiveTab('coordinates');
            else if (['government', 'capital'].includes(firstErrorField)) setActiveTab('government');
            else if (['timezone', 'carCode', 'metersAboveSeaLevel', 'establishmentDate', 'oktmo', 'postalCode'].includes(firstErrorField)) setActiveTab('additional');
            else if (['governor, passport'].includes(firstErrorField)) setActiveTab('governor');

            showError('Please fix the errors in the form');
            return false;
        }
        return true;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log('handleSubmit called');
        if (!validateForm()) return;

        if(serverDown){
            console.log('serverDown true, showing error');
            showError('Server is currently unreachable. Please try again later.');
            return;
        }

        setIsLoading(true);
        console.log('Starting submit...');
        try {
            const submitData = {
                ...formData,
                coordinates: {
                    x: Number(formData.coordinates.x),
                    y: Number(formData.coordinates.y)
                },
                area: Number(formData.area),
                population: Number(formData.population),
                timezone: Number(formData.timezone),
                carCode: formData.carCode || null,
                metersAboveSeaLevel: formData.metersAboveSeaLevel || null,
                postalCode: formData.postalCode,
                oktmo: formData.oktmo
            };

            if (city) {
                await cityService.updateCity(city.id, submitData);
                showSuccess('City updated successfully!');
            } else {
                await cityService.createCity(submitData);
                showSuccess('City created successfully!');
            }
            onSave();
        } catch (error) {
            console.error('Error saving city:', error);
            console.log('Error message:', error.message);
            if (error.message && error.message.includes('500')) {
                setServerDown(true);
                showError('Server is unavailable. Please try again later.');
            } else {
                showError(error.message || 'An unexpected error occurred');
            }
        } finally {
            setIsLoading(false);
        }
    };

    const handleCancel = () => {
        if (isLoading) return;
        onCancel();
    };

    const getError = (field) => errors[field] || (field === 'governorName' ? errors.governorName : null);

    const formatCoordinates = (coord) => `(${coord.x}, ${coord.y})`;
    const formatGovernor = (gov) => `${gov.name}, ${gov.passport}`;

    return (
        <div className="city-form-modal-overlay" onClick={handleCancel}>
            <div className={`city-form-modal-content ${isLoading ? 'loading' : ''}`} onClick={(e) => e.stopPropagation()}>
                <div className="city-form-modal-header">
                    <h2>{city ? 'Edit City' : 'Add New City'}</h2>
                </div>

                <div className="city-form-modal-tabs">
                    <div
                        className={`city-form-modal-tab ${activeTab === 'basic' ? 'active' : ''}`}
                        onClick={() => setActiveTab('basic')}
                    >
                        Basic Info
                    </div>
                    <div
                        className={`city-form-modal-tab ${activeTab === 'coordinates' ? 'active' : ''}`}
                        onClick={() => setActiveTab('coordinates')}
                    >
                        Coordinates
                    </div>
                    <div
                        className={`city-form-modal-tab ${activeTab === 'government' ? 'active' : ''}`}
                        onClick={() => setActiveTab('government')}
                    >
                        Government
                    </div>
                    <div
                        className={`city-form-modal-tab ${activeTab === 'additional' ? 'active' : ''}`}
                        onClick={() => setActiveTab('additional')}
                    >
                        Additional
                    </div>
                    <div
                        className={`city-form-modal-tab ${activeTab === 'governor' ? 'active' : ''}`}
                        onClick={() => setActiveTab('governor')}
                    >
                        Governor
                    </div>
                </div>

                <div className="city-form-modal-body">
                    <div className={`city-form-modal-section ${activeTab === 'basic' ? 'active' : ''}`}>
                        <div className="grid grid-cols-1 md:grid-cols-3">
                            <div className="form-group">
                                <label>City Name *</label>
                                <input
                                    type="text"
                                    name="name"
                                    value={formData.name}
                                    onChange={handleChange}
                                    className={getError('name') ? 'border-red-500' : ''}
                                    placeholder="Enter city name..."
                                />
                                {getError('name') && <span className="text-red-500">{getError('name')}</span>}
                            </div>
                            <div className="form-group">
                                <label>Area (km²) *</label>
                                <input
                                    type="number"
                                    step="0.01"
                                    name="area"
                                    value={formData.area}
                                    onChange={handleChange}
                                    className={getError('area') ? 'border-red-500' : ''}
                                    placeholder="0.00"
                                />
                                {getError('area') && <span className="text-red-500">{getError('area')}</span>}
                            </div>
                            <div className="form-group">
                                <label>Population *</label>
                                <input
                                    type="number"
                                    step="1"
                                    name="population"
                                    value={formData.population}
                                    onChange={handleChange}
                                    className={getError('population') ? 'border-red-500' : ''}
                                    placeholder="0"
                                />
                                {getError('population') && <span className="text-red-500">{getError('population')}</span>}
                            </div>
                        </div>
                    </div>

                    <div className={`city-form-modal-section ${activeTab === 'coordinates' ? 'active' : ''}`}>
                        <div className="space-y-4">
                            <div className="section-header">
                                <h3>Location Coordinates</h3>
                                <button
                                    type="button"
                                    onClick={() => toggleInputMode('coordinates')}
                                    className="toggle-btn"
                                >
                                    {inputModes.coordinates === 'input' ? 'Choose Existing' : 'Enter Manually'}
                                </button>
                            </div>

                            {inputModes.coordinates === 'input' ? (
                                <div className="grid grid-cols-1 md:grid-cols-2">
                                    <div className="form-group">
                                        <label>X Coordinate (≤913) *</label>
                                        <input
                                            type="number"
                                            name="coordinates.x"
                                            value={formData.coordinates.x}
                                            onChange={handleChange}
                                            className={getError('coordX') ? 'border-red-500' : ''}
                                            placeholder="0"
                                        />
                                        {getError('coordX') && <span className="text-red-500">{getError('coordX')}</span>}
                                    </div>
                                    <div className="form-group">
                                        <label>Y Coordinate (>-243) *</label>
                                        <input
                                            type="number"
                                            name="coordinates.y"
                                            value={formData.coordinates.y}
                                            onChange={handleChange}
                                            className={getError('coordY') ? 'border-red-500' : ''}
                                            placeholder="0"
                                        />
                                        {getError('coordY') && <span className="text-red-500">{getError('coordY')}</span>}
                                    </div>
                                </div>
                            ) : (
                                <div className="form-group">
                                    <label>Select Existing Coordinates</label>
                                    {isLoadingExistingData ? (
                                        <div className="loading-state">Loading coordinates...</div>
                                    ) : existingData.coordinates.length > 0 ? (
                                        <select
                                            value={`${formData.coordinates.x},${formData.coordinates.y}`}
                                            onChange={(e) => {
                                                const [x, y] = e.target.value.split(',').map(Number);
                                                handleSelectChange('coordinates', { x, y });
                                            }}
                                        >
                                            <option value="">Select coordinates...</option>
                                            {existingData.coordinates.map((coord, index) => (
                                                <option key={index} value={`${coord.x},${coord.y}`}>
                                                    {formatCoordinates(coord)}
                                                </option>
                                            ))}
                                        </select>
                                    ) : (
                                        <div className="no-data-state">No existing coordinates found</div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>

                    <div className={`city-form-modal-section ${activeTab === 'government' ? 'active' : ''}`}>
                        <div className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2">
                                <div className="form-group">
                                    <label>Government Type *</label>
                                    <select
                                        name="government"
                                        value={formData.government}
                                        onChange={handleChange}
                                    >
                                        <option value="ARISTOCRACY">Aristocracy</option>
                                        <option value="MATRIARCHY">Matriarchy</option>
                                        <option value="NOOCRACY">Noocracy</option>
                                        <option value="PATRIARCHY">Patriarchy</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Capital Status</label>
                                    <div className="checkbox-group">
                                        <input
                                            type="checkbox"
                                            name="capital"
                                            checked={formData.capital}
                                            onChange={handleChange}
                                            id="capital-checkbox"
                                        />
                                        <label htmlFor="capital-checkbox">This is a Capital City</label>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className={`city-form-modal-section ${activeTab === 'additional' ? 'active' : ''}`}>
                        <div className="space-y-4">
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4">
                                <div className="form-group">
                                    <label>Establishment Date</label>
                                    <input
                                        type="date"
                                        name="establishmentDate"
                                        value={formData.establishmentDate}
                                        onChange={handleChange}
                                        className={getError('establishmentDate') ? 'border-red-500' : ''}
                                    />
                                    {getError('establishmentDate') && <span className="text-red-500">{getError('establishmentDate')}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Meters Above Sea Level</label>
                                    <input
                                        type="number"
                                        step="0.1"
                                        name="metersAboveSeaLevel"
                                        value={formData.metersAboveSeaLevel || ''}
                                        onChange={handleChange}
                                        placeholder="Optional"
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Timezone (-13 to 15) *</label>
                                    <input
                                        type="number"
                                        name="timezone"
                                        value={formData.timezone}
                                        onChange={handleChange}
                                        min="-13"
                                        max="15"
                                        className={getError('timezone') ? 'border-red-500' : ''}
                                    />
                                    {getError('timezone') && <span className="text-red-500">{getError('timezone')}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Car Code (1-1000)</label>
                                    <input
                                        type="number"
                                        name="carCode"
                                        value={formData.carCode || ''}
                                        onChange={handleChange}
                                        min="1"
                                        max="1000"
                                        placeholder="Optional"
                                        className={getError('carCode') ? 'border-red-500' : ''}
                                    />
                                    {getError('carCode') && <span className="text-red-500">{getError('carCode')}</span>}
                                </div>
                                <div className="form-group">
                                    <label>Postal Code</label>
                                    <input
                                        type="number"
                                        name="postalCode"
                                        value={formData.postalCode || ''}
                                        onChange={handleChange}
                                        placeholder="Optional"
                                        className={getError('postalCode') ? 'border-red-500' : ''}
                                    />
                                    {getError('postalCode') && <span className="text-red-500">{getError('postalCode')}</span>}
                                </div>
                                <div className="form-group">
                                    <label>oktmo</label>
                                    <input
                                        type="number"
                                        name="oktmo"
                                        value={formData.oktmo || ''}
                                        onChange={handleChange}
                                        placeholder="Optional"
                                        className={getError('oktmo') ? 'border-red-500' : ''}
                                    />
                                    {getError('oktmo') && <span className="text-red-500">{getError('oktmo')}</span>}
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className={`city-form-modal-section ${activeTab === 'governor' ? 'active' : ''}`}>
                        <div className="space-y-4">
                            <div className="section-header">
                                <h3>Governor Information</h3>
                                <button
                                    type="button"
                                    onClick={() => toggleInputMode('governor')}
                                    className="toggle-btn"
                                >
                                    {inputModes.governor === 'input' ? 'Choose Existing' : 'Enter Manually'}
                                </button>
                            </div>

                            {inputModes.governor === 'input' ? (
                                <div className="grid grid-cols-1 md:grid-cols-2">
                                    <div className="form-group max-w-md">
                                        <label>Governor Name *</label>
                                        <input
                                            type="text"
                                            name="governor.name"
                                            value={formData.governor.name}
                                            onChange={handleChange}
                                            className={getError('governorName') ? 'border-red-500' : ''}
                                            placeholder="Enter governor name..."
                                        />
                                        {getError('governorName') && <span className="text-red-500">{getError('governorName')}</span>}
                                    </div>
                                    <div className="form-group max-w-md">
                                        <label>Governor Passport</label>
                                        <input
                                            type="number"
                                            name="governor.passport"
                                            value={formData.governor.passport}
                                            onChange={handleChange}
                                            className={getError('passport') ? 'border-red-500' : ''}
                                            placeholder="Enter governor name..."
                                        />
                                        {getError('passport') && <span className="text-red-500">{getError('passport')}</span>}
                                    </div>
                                </div>
                            ) : (
                                <div className="form-group max-w-md">
                                    <label>Select Existing Governor</label>
                                    {isLoadingExistingData ? (
                                        <div className="loading-state">Loading governors...</div>
                                    ) : existingData.governors.length > 0 ? (
                                        <select
                                            value={formData.governor.passport || ''}
                                            onChange={(e) => {
                                                const selectedGov = existingData.governors.find(g => g.passport == e.target.value);
                                                if (selectedGov) {
                                                    setFormData(prev => ({
                                                        ...prev,
                                                        governor: { name: selectedGov.name, passport: selectedGov.passport }
                                                    }));
                                                }
                                                if (errors.governorName) {
                                                    setErrors(prev => ({ ...prev, governorName: null }));
                                                }
                                            }}
                                            className={getError('governorName') ? 'border-red-500' : ''}
                                        >
                                            <option value="">Select governor...</option>
                                            {existingData.governors.map((gov, index) => (
                                                <option key={index} value={gov.passport}>
                                                    {formatGovernor(gov)}
                                                </option>
                                            ))}
                                        </select>

                                    ) : (
                                        <div className="no-data-state">No existing governors found</div>
                                    )}
                                    {getError('governorName') && <span className="text-red-500">{getError('governorName')}</span>}
                                </div>
                            )}
                        </div>
                    </div>
                    <NotificationComponent />
                </div>

                <div className="city-form-modal-footer">
                    <button
                        type="button"
                        onClick={handleCancel}
                        disabled={isLoading}
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={handleSubmit}
                        disabled={isLoading}
                    >
                        {isLoading ? 'Saving...' : (city ? 'Update City' : 'Create City')}
                    </button>
                </div>
            </div>

        </div>
    );
};

export default CityForm;