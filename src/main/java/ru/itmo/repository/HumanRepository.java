package ru.itmo.repository;

import ru.itmo.model.Human;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HumanRepository {
    private final SessionFactory sessionFactory;

    public HumanRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Human findById(Long id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Human.class, id);
    }

    public List<Human> findAll() {
        Session session = sessionFactory.getCurrentSession();
        return session.createQuery("FROM Human", Human.class).list();
    }


    public Human save(Human human) {
        Session session = sessionFactory.getCurrentSession();
        return session.merge(human);
    }

    public void update(Human human) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(human);
    }

    public void delete(Human human) {
        Session session = sessionFactory.getCurrentSession();
        session.delete(human);
    }

    public boolean existsByPassport(Long passport) {
        Session session = sessionFactory.getCurrentSession();
        Query<Long> query = session.createQuery("SELECT COUNT(h) FROM Human h WHERE h.passport = :passport", Long.class);
        query.setParameter("passport", passport);
        return query.uniqueResult() > 0;
    }

    public Human findByPassport(Long passport) {
        Session session = sessionFactory.getCurrentSession();
        Query<Human> query = session.createQuery("FROM Human h WHERE h.passport = :passport", Human.class);
        query.setParameter("passport", passport);
        return query.uniqueResult();
    }

}
