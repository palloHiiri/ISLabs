package ru.itmo.repository;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import ru.itmo.model.ImportOperation;

import java.util.List;

@Repository
public class ImportOperationRepository {
    private final SessionFactory sessionFactory;

    public ImportOperationRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Long save(ImportOperation op) {
        Session session = sessionFactory.getCurrentSession();
        return (Long) session.save(op);
    }

    public ImportOperation findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(ImportOperation.class, id);
    }

    public long countAll() {
        Session session = sessionFactory.getCurrentSession();
        Query<Long> q = session.createQuery("select count(o) from ImportOperation o", Long.class);
        Long r = q.uniqueResult();
        return r == null ? 0L : r;
    }

    public List<ImportOperation> findPaged(int page, int size) {
        Session session = sessionFactory.getCurrentSession();
        Query<ImportOperation> q = session.createQuery("from ImportOperation o order by o.timestamp desc", ImportOperation.class);
        q.setFirstResult(page * size);
        q.setMaxResults(size);
        return q.list();
    }
}
