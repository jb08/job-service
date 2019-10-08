package com.example.jobservice;

import static com.google.common.cache.RemovalCause.EXPLICIT;
import static org.flywaydb.core.Flyway.configure;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

import com.example.jobservice.controllers.JobController;
import com.example.jobservice.dtos.Job;
import com.example.jobservice.exceptions.JobNotFound;
import com.example.jobservice.persistence.JobDao;
import com.example.jobservice.services.JobService;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import io.dropwizard.Application;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;


public class JobServiceApplication extends Application<JobServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new JobServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<JobServiceConfiguration> bootstrap) {
        // if needed set up any other initialization

        bootstrap.addBundle(new SwaggerBundle<JobServiceConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
                    JobServiceConfiguration configuration) {
                return configuration.swaggerBundleConfiguration;
            }
        });
    }

    @Override
    public void run(JobServiceConfiguration configuration, Environment environment) {
        DataSourceFactory dataSourceFactory = configuration.getDataSourceFactory();
        Flyway flyway = configure()
                .dataSource(dataSourceFactory.getUrl(),
                        dataSourceFactory.getUser(),
                        dataSourceFactory.getPassword())
                .schemas("demo")
                .load();

        flyway.migrate();

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(
                environment,
                configuration.getDataSourceFactory(),
                "postgresql")
                .installPlugin(new PostgresPlugin());

        JobDao jobDao = new JobDao(jdbi);

        RemovalListener<Integer, Job> listener = new RemovalListener<Integer, Job>() {

            public void onRemoval(RemovalNotification<Integer, Job> removal) {
                if (removal.wasEvicted()) {
                    jobDao.updateJob(removal.getValue());
                }
                if (removal.getCause().equals(EXPLICIT)) {
                    jobDao.deleteJobById(removal.getKey());
                }
            }
        };

        LoadingCache<Integer, Job> jobCache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .ticker(Ticker.systemTicker())
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .removalListener(listener)
                .build(new CacheLoader<Integer, Job>() {
                    @Override
                    public Job load(Integer id) throws Exception {
                        Optional<Job> optJob = jobDao.getJob(id);
                        if (optJob.isPresent()) {
                            return optJob.get();
                        } else {
                            throw new JobNotFound(id);
                        }
                    }
                });

        JobService jobService = new JobService(jobDao, jobCache);
        environment.jersey().register(new JobController(jobService));
    }
}
