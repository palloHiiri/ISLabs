package ru.itmo.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;

public class WebAppInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);

        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", servlet);

        MultipartConfigElement multipartConfig = new MultipartConfigElement(
                "",
                10 * 1024 * 1024,
                50 * 1024 * 1024,
                0
        );

        registration.setMultipartConfig(multipartConfig);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}