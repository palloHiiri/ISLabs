package com.example.repository;

import com.example.model.City;
import com.example.model.Coordinates;
import com.example.model.Human;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CityRepository {
    private final SessionFactory sessionFactory;

    public CityRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public City findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(City.class, id);
    }

    public void delete(City city) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(city);
    }

    public List<Human> findAllGovernors() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("SELECT DISTINCT c.governor FROM City c WHERE c.governor IS NOT NULL", Human.class).list();
    }

    public List<Coordinates> findAllCoordinates() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("SELECT DISTINCT c.coordinates FROM City c WHERE c.coordinates IS NOT NULL", Coordinates.class).list();
    }

    public Long save(City city) {
        Session session = sessionFactory.getCurrentSession();
        return (Long) session.save(city);
    }

    public void update(City city) {
        Session session = sessionFactory.getCurrentSession();
        session.update(city);
    }

    public Double getSumOfTimezones() {
        Session session = sessionFactory.getCurrentSession();
        Query<Long> sumQuery = session.createQuery("select sum(cast(c.timezone as long)) from City c where c.timezone is not null", Long.class);
        Long result = sumQuery.uniqueResult();
        return result != null ? result.doubleValue() : 0.0;
    }

    public Double getAverageCarCode() {
        Session session = sessionFactory.getCurrentSession();
        Query<Double> query = session.createQuery("select avg(c.carCode) from City c", Double.class);
        Double result = query.uniqueResult();
        return result != null ? result : 0.0;
    }

    public List<City> getCitiesWithTimezoneLessThan(Integer timezone) {
        Session session = sessionFactory.getCurrentSession();
        Query<City> query = session.createQuery("from City c where c.timezone > :timezone", City.class);
        query.setParameter("timezone", timezone);
        return query.list();
    }

    public Double calculateDistanceToTheMostPopulatedCity() {
        Session session = sessionFactory.getCurrentSession();

        Query<Long> maxPopQuery = session.createQuery(
                "select max(c.population) from City c where c.population is not null",
                Long.class
        );
        Long maxPopulation = maxPopQuery.uniqueResult();
        if (maxPopulation == null) return 0.0;

        Query<Object[]> query = session.createQuery(
                "select c.coordinates.x, c.coordinates.y from City c " +
                        "where c.population = :maxPopulation and c.coordinates is not null order by c.id",
                Object[].class
        );
        query.setParameter("maxPopulation", maxPopulation);
        query.setMaxResults(1);
        List<Object[]> results = query.list();

        if (results.isEmpty()) return 0.0;

        Object[] result = results.get(0);
        Double x = ((Number) result[0]).doubleValue();
        Double y = ((Number) result[1]).doubleValue();

        return Math.sqrt(x * x + y * y);
    }

    public Double calculateDistanceToNewestCity() {
        Session session = sessionFactory.getCurrentSession();

        Query<Long> countQuery = session.createQuery("select count(c) from City c", Long.class);
        Long count = countQuery.uniqueResult();
        if (count == null || count == 0) return 0.0;

        Query<java.time.LocalDate> maxDateQuery = session.createQuery(
                "select max(c.establishmentDate) from City c where c.establishmentDate is not null",
                java.time.LocalDate.class
        );
        java.time.LocalDate maxDate = maxDateQuery.uniqueResult();

        Query<Object[]> query = session.createQuery(
                "select c.coordinates.x, c.coordinates.y from City c " +
                        "where c.establishmentDate = :maxDate and c.coordinates is not null order by c.id",
                Object[].class
        );
        query.setParameter("maxDate", maxDate);
        query.setMaxResults(1);
        List<Object[]> results = query.list();

        if (results.isEmpty()) return 0.0;

        Object[] result = results.get(0);
        Double x = ((Number) result[0]).doubleValue();
        Double y = ((Number) result[1]).doubleValue();

        return Math.sqrt(x * x + y * y);
    }

    public List<City> findWithFiltersAndSort(Map<String, String> filters, String sortBy, String sortDirection) {
        Session session = sessionFactory.getCurrentSession();
        StringBuilder hql = new StringBuilder("FROM City c WHERE 1=1");
        Map<String, Object> params = new HashMap<>();

        if (filters.get("id") != null && !filters.get("id").trim().isEmpty()) {
            hql.append(" AND CAST(c.id AS string) LIKE :id");
            params.put("id", "%" + filters.get("id").trim() + "%");
        }

        if (filters.get("name") != null && !filters.get("name").trim().isEmpty()) {
            hql.append(" AND LOWER(c.name) LIKE :name");
            params.put("name", "%" + filters.get("name").trim().toLowerCase() + "%");
        }

        if (filters.get("coordinatesX") != null && !filters.get("coordinatesX").trim().isEmpty()) {
            hql.append(" AND CAST(c.coordinates.x AS string) LIKE :coordinatesX");
            params.put("coordinatesX", "%" + filters.get("coordinatesX").trim() + "%");
        }

        if (filters.get("coordinatesY") != null && !filters.get("coordinatesY").trim().isEmpty()) {
            hql.append(" AND CAST(c.coordinates.y AS string) LIKE :coordinatesY");
            params.put("coordinatesY", "%" + filters.get("coordinatesY").trim() + "%");
        }

        if (filters.get("creationDate") != null && !filters.get("creationDate").trim().isEmpty()) {
            hql.append(" AND CAST(c.creationDate AS string) LIKE :creationDate");
            params.put("creationDate", "%" + filters.get("creationDate").trim() + "%");
        }

        if (filters.get("area") != null && !filters.get("area").trim().isEmpty()) {
            hql.append(" AND CAST(c.area AS string) LIKE :area");
            params.put("area", "%" + filters.get("area").trim() + "%");
        }

        if (filters.get("population") != null && !filters.get("population").trim().isEmpty()) {
            hql.append(" AND CAST(c.population AS string) LIKE :population");
            params.put("population", "%" + filters.get("population").trim() + "%");
        }

        if (filters.get("establishmentDate") != null && !filters.get("establishmentDate").trim().isEmpty()) {
            hql.append(" AND CAST(c.establishmentDate AS string) LIKE :establishmentDate");
            params.put("establishmentDate", "%" + filters.get("establishmentDate").trim() + "%");
        }

        if (filters.get("capital") != null && !filters.get("capital").trim().isEmpty()) {
            String capitalValue = filters.get("capital").trim().toLowerCase();
            if (capitalValue.equals("true") || capitalValue.equals("yes") || capitalValue.equals("1")) {
                hql.append(" AND c.capital = true");
            } else if (capitalValue.equals("false") || capitalValue.equals("no") || capitalValue.equals("0")) {
                hql.append(" AND c.capital = false");
            }
        }

        if (filters.get("metersAboveSeaLevel") != null && !filters.get("metersAboveSeaLevel").trim().isEmpty()) {
            hql.append(" AND CAST(c.metersAboveSeaLevel AS string) LIKE :metersAboveSeaLevel");
            params.put("metersAboveSeaLevel", "%" + filters.get("metersAboveSeaLevel").trim() + "%");
        }

        if (filters.get("timezone") != null && !filters.get("timezone").trim().isEmpty()) {
            hql.append(" AND CAST(c.timezone AS string) LIKE :timezone");
            params.put("timezone", "%" + filters.get("timezone").trim() + "%");
        }

        if (filters.get("carCode") != null && !filters.get("carCode").trim().isEmpty()) {
            hql.append(" AND CAST(c.carCode AS string) LIKE :carCode");
            params.put("carCode", "%" + filters.get("carCode").trim() + "%");
        }

        if (filters.get("government") != null && !filters.get("government").trim().isEmpty()) {
            hql.append(" AND LOWER(CAST(c.government AS string)) LIKE :government");
            params.put("government", "%" + filters.get("government").trim().toLowerCase() + "%");
        }

        if (filters.get("governor") != null && !filters.get("governor").trim().isEmpty()) {
            hql.append(" AND LOWER(c.governor.name) LIKE :governor");
            params.put("governor", "%" + filters.get("governor").trim().toLowerCase() + "%");
        }

        hql.append(" ORDER BY ");
        switch (sortBy.toLowerCase()) {
            case "id":
                hql.append("c.id");
                break;
            case "name":
                hql.append("c.name");
                break;
            case "coordinatesx":
                hql.append("c.coordinates.x");
                break;
            case "coordinatesy":
                hql.append("c.coordinates.y");
                break;
            case "coordinates":
                hql.append("c.coordinates.x, c.coordinates.y");
                break;
            case "creationdate":
                hql.append("c.creationDate");
                break;
            case "area":
                hql.append("c.area");
                break;
            case "population":
                hql.append("c.population");
                break;
            case "establishmentdate":
                hql.append("c.establishmentDate");
                break;
            case "capital":
                hql.append("c.capital");
                break;
            case "metersabovesealevel":
                hql.append("c.metersAboveSeaLevel");
                break;
            case "timezone":
                hql.append("c.timezone");
                break;
            case "carcode":
                hql.append("c.carCode");
                break;
            case "government":
                hql.append("c.government");
                break;
            case "governor":
                hql.append("c.governor.name");
                break;
            default:
                hql.append("c.id");
                break;
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            hql.append(" DESC");
        } else {
            hql.append(" ASC");
        }

        Query<City> query = session.createQuery(hql.toString(), City.class);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.list();
    }
}
