package br.unb.cic.bionimbus.services;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@Singleton
public class MetricsServletContextListener implements ServletContextListener {

    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;

    @Inject
    public MetricsServletContextListener(MetricRegistry metricRegistry, HealthCheckRegistry healthCheckRegistry) {
         this.metricRegistry = metricRegistry;
         this.healthCheckRegistry = healthCheckRegistry;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,healthCheckRegistry);
        servletContextEvent.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metricRegistry);

//            metricRegistry.counter("teste");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // do nothing
    }
}
