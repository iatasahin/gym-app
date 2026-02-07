package dev.ilkersahin.java.spring.gym;

import dev.ilkersahin.java.spring.gym.config.Config;
import dev.ilkersahin.java.spring.gym.config.FlywayConfig;
import dev.ilkersahin.java.spring.gym.config.PersistenceConfig;
import dev.ilkersahin.java.spring.gym.config.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class SpringGymApplication {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        Logger log = LoggerFactory.getLogger(SpringGymApplication.class);
        log.info("Starting SpringGymApplication with embedded Tomcat on port {}", PORT);

        var ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(Config.class, PersistenceConfig.class, FlywayConfig.class, WebConfig.class);

        var dispatcherServlet = new DispatcherServlet(ctx);

        try {
            Tomcat tomcat = new Tomcat();

            tomcat.setPort(PORT);
            tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());

            Context tomcatContext =
                    tomcat.addContext("", new File(".").getAbsolutePath());

            tomcat.addServlet(tomcatContext, "dispatcher", dispatcherServlet).setLoadOnStartup(1);
            tomcatContext.addServletMappingDecoded("/*", "dispatcher");

            registerFilter(tomcatContext, "jwtAuthenticationFilter", "/*");

            tomcat.getConnector();
            tomcat.start();

            log.info("Spring context started with {} beans",
                    ctx.getBeanDefinitionCount()
            );

            log.info("===========================================");
            log.info("  Application started successfully");
            log.info("  Local:  http://localhost:{}", PORT);
            log.info("  API:    http://localhost:{}/api/v1", PORT);
            log.info("===========================================");

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down...");
                try{
                    tomcat.stop();
                    tomcat.destroy();
                } catch (LifecycleException e){
                    log.error("Error during shutdown", e);
                }
            }));

            tomcat.getServer().await();

        } catch (LifecycleException e) {
            log.error("Failed to start application", e);
            System.exit(1);
        }
    }

    private static void registerFilter(Context context, String filterName, String urlPattern) {
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName(filterName);
        filterDef.setFilterClass(DelegatingFilterProxy.class.getName());

        filterDef.addInitParameter("targetBeanName", filterName);
        context.addFilterDef(filterDef);

        // FilterMap defines URL pattern
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName(filterName);
        filterMap.addURLPattern(urlPattern);
        context.addFilterMap(filterMap);
    }
}
