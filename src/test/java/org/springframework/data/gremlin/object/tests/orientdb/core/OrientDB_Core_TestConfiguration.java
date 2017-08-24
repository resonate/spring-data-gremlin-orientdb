package org.springframework.data.gremlin.object.tests.orientdb.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gremlin.config.EnableGremlinRepositories;
import org.springframework.data.gremlin.object.core.TestService;
import org.springframework.data.gremlin.query.orientdb.NativeOrientdbGremlinQuery;
import org.springframework.data.gremlin.repository.GremlinGraphAdapter;
import org.springframework.data.gremlin.repository.GremlinRepositoryContext;
import org.springframework.data.gremlin.repository.orientdb.OrientDBGraphAdapter;
import org.springframework.data.gremlin.repository.orientdb.OrientDBGremlinRepository;
import org.springframework.data.gremlin.schema.GremlinBeanPostProcessor;
import org.springframework.data.gremlin.schema.GremlinSchemaFactory;
import org.springframework.data.gremlin.schema.generator.DefaultSchemaGenerator;
import org.springframework.data.gremlin.schema.generator.SchemaGenerator;
import org.springframework.data.gremlin.schema.property.encoder.orientdb.OrientDbIdEncoder;
import org.springframework.data.gremlin.schema.writer.SchemaWriter;
import org.springframework.data.gremlin.schema.writer.orientdb.OrientDbSchemaWriter;
import org.springframework.data.gremlin.support.GremlinRepositoryFactoryBean;
import org.springframework.data.gremlin.tx.GremlinGraphFactory;
import org.springframework.data.gremlin.tx.GremlinTransactionManager;
import org.springframework.data.gremlin.tx.orientdb.OrientDBGremlinGraphFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableGremlinRepositories(basePackages = "org.springframework.data.gremlin.object.core", repositoryFactoryBeanClass = GremlinRepositoryFactoryBean.class)
public class OrientDB_Core_TestConfiguration {

    @Bean
    public OrientDBGremlinGraphFactory factory() {
        OrientDBGremlinGraphFactory factory = new OrientDBGremlinGraphFactory();
        factory.setUrl("memory:spring-data-orientdb-core");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setAutoStartTx(false);

        return factory;
    }

    @Bean
    public GremlinSchemaFactory mapperFactory() {
        return new GremlinSchemaFactory();
    }

    @Bean
    public GremlinTransactionManager transactionManager() {
        return new GremlinTransactionManager(factory());
    }

    @Bean
    public SchemaGenerator schemaGenerator() {
        return new DefaultSchemaGenerator(new OrientDbIdEncoder());
    }

    @Bean
    public SchemaWriter schemaWriter() {
        return new OrientDbSchemaWriter();
    }

    @Bean
    public static GremlinBeanPostProcessor tinkerpopSchemaManager(SchemaGenerator schemaGenerator) {
        return new GremlinBeanPostProcessor(schemaGenerator, "org.springframework.data.gremlin.object.core.domain");
    }

    @Bean
    public GremlinGraphAdapter graphAdapter() {
        return new OrientDBGraphAdapter();
    }

    @Bean
    public GremlinRepositoryContext databaseContext(GremlinGraphFactory graphFactory, GremlinGraphAdapter graphAdapter, GremlinSchemaFactory schemaFactory, SchemaWriter schemaWriter) {
        return new GremlinRepositoryContext(graphFactory, graphAdapter, schemaFactory, schemaWriter, OrientDBGremlinRepository.class, NativeOrientdbGremlinQuery.class);
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

}
