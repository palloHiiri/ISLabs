import React from 'react';
import './cityTable.css';

const TableHeader = ({ field, label, filterType = 'text', options = [], filters, sortBy, sortDirection, onSortChange, onFilterChange, onClearFilter }) => {

    const getSortIcon = (field) => {
        if (sortBy !== field) return '↕️';
        return sortDirection === 'asc' ? '↑' : '↓';
    };

    return (
        <th className="table-header">
            <div className="column-header">
                <div className="header-content">
                    <span className="header-label">{label}</span>
                    <button
                        onClick={() => onSortChange(field)}
                        className="sort-btn"
                        title={`Sort by ${label}`}
                    >
                        {getSortIcon(field)}
                    </button>
                </div>
                <div className="column-filter">
                    {filterType === 'select' ? (
                        <select
                            value={filters[field] || ''}
                            onChange={(e) => onFilterChange(field, e.target.value)}
                            className="filter-select"
                        >
                            <option value="">All</option>
                            {options.map(option => (
                                <option key={option.value} value={option.value}>
                                    {option.label}
                                </option>
                            ))}
                        </select>
                    ) : (
                        <input
                            type={filterType}
                            placeholder={`Filter ${label.toLowerCase()}...`}
                            value={filters[field] || ''}
                            onChange={(e) => onFilterChange(field, e.target.value)}
                            className="filter-input"
                        />
                    )}
                    {filters[field] && (
                        <button
                            onClick={() => onClearFilter(field)}
                            className="clear-filter-btn"
                            title="Clear filter"
                        >
                            ✕
                        </button>
                    )}
                </div>
            </div>
        </th>
    );
};

const CityTable = ({
                       cities,
                       filters,
                       sortBy,
                       sortDirection,
                       onEdit,
                       onDelete,
                       onSortChange,
                       onFilterChange,
                       onClearFilter
                   }) => {

    return (
        <div className="table-container">
            <div className="table-wrapper">
                <table className="city-table">
                    <thead className="table-head">
                    <tr>
                        <TableHeader
                            field="id"
                            label="ID"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="name"
                            label="Name"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="coordinatesX"
                            label="Coord X"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="coordinatesY"
                            label="Coord Y"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="creationDate"
                            label="Creation Date"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="area"
                            label="Area"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="population"
                            label="Population"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="establishmentDate"
                            label="Est. Date"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="capital"
                            label="Capital"
                            filterType="select"
                            options={[
                                { value: 'true', label: 'Yes' },
                                { value: 'false', label: 'No' }
                            ]}
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="metersAboveSeaLevel"
                            label="Meters Above SL"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="timezone"
                            label="Timezone"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="carCode"
                            label="Car Code"
                            filterType="number"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="postalCode"
                            label="Postal Code"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="oktmo"
                            label="OKTMO"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="government"
                            label="Government"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="governor"
                            label="Governor"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <TableHeader
                            field="passport"
                            label="Passport"
                            filters={filters}
                            sortBy={sortBy}
                            sortDirection={sortDirection}
                            onSortChange={onSortChange}
                            onFilterChange={onFilterChange}
                            onClearFilter={onClearFilter}
                        />
                        <th className="table-header actions-header">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="table-body">
                    {cities.map((city, index) => (
                        <tr key={city.id} className="table-row" style={{'--row-index': index}}>
                            <td className="table-cell id-cell">{city.id}</td>
                            <td className="table-cell name-cell">{city.name}</td>
                            <td className="table-cell coord-cell">{city.coordinates?.x}</td>
                            <td className="table-cell coord-cell">{city.coordinates?.y}</td>
                            <td className="table-cell date-cell">
                                {city.creationDate ? new Date(city.creationDate).toLocaleDateString() : 'N/A'}
                            </td>
                            <td className="table-cell number-cell">{city.area}</td>
                            <td className="table-cell number-cell">{city.population?.toLocaleString()}</td>
                            <td className="table-cell date-cell">
                                {city.establishmentDate ? new Date(city.establishmentDate).toLocaleDateString() : 'N/A'}
                            </td>
                            <td className="table-cell capital-cell">
                                <span className={`capital-badge ${city.capital ? 'capital-yes' : 'capital-no'}`}>
                                    {city.capital ? 'Yes' : 'No'}
                                </span>
                            </td>
                            <td className="table-cell number-cell">{city.metersAboveSeaLevel || 'N/A'}</td>
                            <td className="table-cell number-cell">{city.timezone}</td>
                            <td className="table-cell number-cell">{city.carCode || 'N/A'}</td>
                            <td className="table-cell text-cell">{city.postalCode || 'N/A'}</td>
                            <td className="table-cell text-cell">{city.oktmo || 'N/A'}</td>
                            <td className="table-cell text-cell">{city.government}</td>
                            <td className="table-cell text-cell">{city.governor?.name || 'N/A'}</td>
                            <td className="table-cell text-cell">{city.governor?.passport || 'N/A'}</td>
                            <td className="table-cell actions-cell">
                                <div className="action-buttons">
                                    <button
                                        onClick={() => onEdit(city)}
                                        className="action-btn edit-btn"
                                        title="Edit city"
                                    >
                                        <span>✏️</span>
                                        Edit
                                    </button>
                                    <button
                                        onClick={() => onDelete(city.id)}
                                        className="action-btn delete-btn"
                                        title="Delete city"
                                    >
                                        <span>🗑️</span>
                                        Delete
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default CityTable;