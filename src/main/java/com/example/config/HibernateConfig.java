package com.example.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Bean
    public SessionFactory sessionFactory() {
        try {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(com.example.model.City.class)
                    .addAnnotatedClass(com.example.model.Coordinates.class)
                    .addAnnotatedClass(com.example.model.Government.class)
                    .addAnnotatedClass(com.example.model.Human.class)
                    .getMetadataBuilder()
                    .build();

            return metadata.getSessionFactoryBuilder().build();

        } catch (Exception e) {
            System.err.println("Ошибка при создании SessionFactory: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }
    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

}