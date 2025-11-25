package ru.itmo.repository;

import ru.itmo.model.Human;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;


@Repository
public class HumanRepository {
    private final SessionFactory sessionFactory;

    public HumanRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }


    public Long save(Human human) {
        Session session = sessionFactory.getCurrentSession();
        return (Long) session.save(human);
    }

    public Human findByPassport(Long passport) {
        Session session = sessionFactory.getCurrentSession();
        Query<Human> query = session.createQuery("FROM Human h WHERE h.passport = :passport", Human.class);
        query.setParameter("passport", passport);
        return query.uniqueResult();
    }

}
