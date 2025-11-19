import React, { useState } from 'react';
import { cityService } from '../../services/cityService.js';
import { useNotification } from '../errorNotification/errorNotification.jsx';
import './specialFunctions.css';

const SpecialFunctions = () => {
    const {showError, NotificationComponent} = useNotification();

    const [results, setResults] = useState({});
    const [timezoneInput, setTimezoneInput] = useState('');
    const [loadingFunctions, setLoadingFunctions] = useState({});

    const executeFunction = async (functionName, inputValue = null) => {
        try {
            setLoadingFunctions(prev => ({...prev, [functionName]: true}));

            let result;
            let successMessage = '';

            switch (functionName) {
                case 'sumTimezones':
                    result = await cityService.getSumOfTimezones();
                    successMessage = `Sum of timezones calculated: ${result}`;
                    setResults(prev => ({...prev, sumTimezones: result}));
                    break;

                case 'averageCarCode':
                    result = await cityService.getAverageCarCode();
                    successMessage = `Average car code calculated: ${result?.toFixed(2)}`;
                    setResults(prev => ({...prev, averageCarCode: result}));
                    break;

                case 'citiesWithTimezoneLess':
                    if (!inputValue || isNaN(inputValue)) {
                        showError('Please enter a valid timezone value');
                        return;
                    }

                    const timezoneValue = Number(inputValue);
                    if (timezoneValue < -13 || timezoneValue > 15) {
                        showError('Timezone must be between -13 and 15');
                        return;
                    }

                    result = await cityService.getCitiesWithTimezoneLessThan(timezoneValue);
                    successMessage = `Found ${result.length} cities with timezone greater than ${timezoneValue}`;
                    setResults(prev => ({...prev, citiesWithTimezoneLess: result}));
                    break;

                case 'distanceToMostPopulated':
                    result = await cityService.getDistanceToMostPopulated();
                    successMessage = `Distance to most populated city: ${result?.toFixed(2)} units`;
                    setResults(prev => ({...prev, distanceToMostPopulated: result}));
                    break;

                case 'distanceToNewest':
                    result = await cityService.getDistanceToNearest();
                    successMessage = `Distance to newest city: ${result?.toFixed(2)} units`;
                    setResults(prev => ({...prev, distanceToNewest: result}));
                    break;

                default:
                    showError('Unknown function requested');
                    return;
            }

            console.log(`${functionName} result:`, result);

        } catch (error) {
            console.error('Function execution error:', error);
            showError(error.message || `Failed to execute ${functionName}`);
        } finally {
            setLoadingFunctions(prev => ({...prev, [functionName]: false}));
        }
    };

    const goBackToCities = () => {
        window.location.href = '/';
    };

    const clearResults = () => {
        setResults({});
        setTimezoneInput('');
    };

    const handleTimezoneInputChange = (e) => {
        const value = e.target.value;
        setTimezoneInput(value);

        if (results.citiesWithTimezoneLess) {
            setResults(prev => ({...prev, citiesWithTimezoneLess: undefined}));
        }
    };

    const FunctionCard = ({title, description, onExecute, isLoading, result, children}) => (
        <div className="function-card">
            <div className="function-header">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>

            <div className="function-actions">
                {children}
                <button
                    onClick={onExecute}
                    disabled={isLoading}
                    className="function-actions button"
                >
                    Execute
                </button>
            </div>

            {result !== undefined && (
                <div className="result">
                    {typeof result === 'number' ? (
                        <p className="result-number">
                            <strong>Result:</strong> {result.toFixed(2)}
                        </p>
                    ) : Array.isArray(result) ? (
                        <div>
                            <p className="result-list-header">Found {result.length} cities:</p>
                            {result.length > 0 ? (
                                <div className="cities-list">
                                    {result.map((city, index) => (
                                        <div key={index} className="city-item">
                                            <span className="city-name">{city.name}</span>
                                            <span className="city-timezone">Timezone: {city.timezone}</span>
                                        </div>
                                    ))}
                                </div>
                            ) : (
                                <p className="no-cities">No cities match the criteria</p>
                            )}
                        </div>
                    ) : (
                        <p className="result-value">
                            <strong>Result:</strong> {result}
                        </p>
                    )}
                </div>
            )}
        </div>
    );

    return (
        <>
            <NotificationComponent/>

            <div className="special-functions">
                <div className="header">
                    <h1>Special City Functions</h1>
                    <div className="header-controls">
                        <button
                            onClick={goBackToCities}
                            className="header-controls button"
                        >
                            ← Back to Cities
                        </button>
                        <button
                            onClick={clearResults}
                            className="header-controls button"
                        >
                            Clear Results
                        </button>
                    </div>
                </div>

                <div className="functions-grid">
                    <FunctionCard
                        title="Sum of Timezones"
                        description="Calculate the sum of all city timezones"
                        onExecute={() => executeFunction('sumTimezones')}
                        isLoading={loadingFunctions.sumTimezones}
                        result={results.sumTimezones}
                    />

                    <FunctionCard
                        title="Average Car Code"
                        description="Calculate the average car code across all cities"
                        onExecute={() => executeFunction('averageCarCode')}
                        isLoading={loadingFunctions.averageCarCode}
                        result={results.averageCarCode}
                    />

                    <FunctionCard
                        title="Cities by Timezone"
                        description="Find cities with timezone less than specified value"
                        onExecute={() => executeFunction('citiesWithTimezoneLess', timezoneInput)}
                        isLoading={loadingFunctions.citiesWithTimezoneLess}
                        result={results.citiesWithTimezoneLess}
                    >
                        <div className="input-group">
                            <input
                                type="number"
                                placeholder="Enter timezone "
                                value={timezoneInput}
                                onChange={handleTimezoneInputChange}
                            />
                            {timezoneInput && (isNaN(timezoneInput) || timezoneInput < -13 || timezoneInput > 15) && (
                                <p className="input-error">Please enter a valid timezone between -13 and 15</p>
                            )}
                        </div>
                    </FunctionCard>

                    <FunctionCard
                        title="Distance to Most Populated"
                        description="Calculate distance from origin to the most populated city"
                        onExecute={() => executeFunction('distanceToMostPopulated')}
                        isLoading={loadingFunctions.distanceToMostPopulated}
                        result={results.distanceToMostPopulated}
                    />

                    <FunctionCard
                        title="Distance to Newest City"
                        description="Calculate distance from origin to the newest city"
                        onExecute={() => executeFunction('distanceToNewest')}
                        isLoading={loadingFunctions.distanceToNewest}
                        result={results.distanceToNewest}
                    />
                </div>

            </div>
        </>
    );
};

export default SpecialFunctions;