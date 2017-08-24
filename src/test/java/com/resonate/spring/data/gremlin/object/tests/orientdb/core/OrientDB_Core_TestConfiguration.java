package com.resonate.spring.data.gremlin.object.tests.orientdb.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.resonate.spring.data.gremlin.config.EnableGremlinRepositories;
import com.resonate.spring.data.gremlin.object.core.TestService;
import com.resonate.spring.data.gremlin.query.orientdb.NativeOrientdbGremlinQuery;
import com.resonate.spring.data.gremlin.repository.GremlinGraphAdapter;
import com.resonate.spring.data.gremlin.repository.GremlinRepositoryContext;
import com.resonate.spring.data.gremlin.repository.orientdb.OrientDBGraphAdapter;
import com.resonate.spring.data.gremlin.repository.orientdb.OrientDBGremlinRepository;
import com.resonate.spring.data.gremlin.schema.GremlinBeanPostProcessor;
import com.resonate.spring.data.gremlin.schema.GremlinSchemaFactory;
import com.resonate.spring.data.gremlin.schema.generator.DefaultSchemaGenerator;
import com.resonate.spring.data.gremlin.schema.generator.SchemaGenerator;
import com.resonate.spring.data.gremlin.schema.property.encoder.orientdb.OrientDbIdEncoder;
import com.resonate.spring.data.gremlin.schema.writer.SchemaWriter;
import com.resonate.spring.data.gremlin.schema.writer.orientdb.OrientDbSchemaWriter;
import com.resonate.spring.data.gremlin.support.GremlinRepositoryFactoryBean;
import com.resonate.spring.data.gremlin.tx.GremlinGraphFactory;
import com.resonate.spring.data.gremlin.tx.GremlinTransactionManager;
import com.resonate.spring.data.gremlin.tx.orientdb.OrientDBGremlinGraphFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableGremlinRepositories(basePackages = "com.resonate.spring.data.gremlin.object.core", repositoryFactoryBeanClass = GremlinRepositoryFactoryBean.class)
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
        return new GremlinBeanPostProcessor(schemaGenerator, "com.resonate.spring.data.gremlin.object.core.domain");
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
