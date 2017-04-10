package org.springframework.data.gremlin.query;

import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.Contains;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.filter.AndFilterPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.gremlin.schema.GremlinSchema;
import org.springframework.data.gremlin.schema.GremlinSchemaFactory;
import org.springframework.data.gremlin.schema.property.GremlinAdjacentProperty;
import org.springframework.data.gremlin.schema.property.GremlinProperty;
import org.springframework.data.gremlin.schema.property.GremlinRelatedProperty;
import org.springframework.data.gremlin.tx.GremlinGraphFactory;
import org.springframework.data.mapping.PropertyPath;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.parser.AbstractQueryCreator;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

import static org.apache.tinkerpop.gremlin.process.traversal.P.gt;

/**
 * Concrete {@link AbstractQueryCreator} for Gremlin.
 *
 * @author Gman
 */
public class GremlinQueryCreator extends AbstractQueryCreator<GraphTraversal, GraphTraversal> {

    private static final Logger logger = LoggerFactory.getLogger(GremlinQueryCreator.class);

    private final PartTree tree;

    private GremlinGraphFactory factory;
    private GremlinSchemaFactory schemaFactory;

    private ParameterAccessor accessor;

    private GremlinSchema schema;

    public GremlinQueryCreator(GremlinGraphFactory factory, GremlinSchemaFactory mapperfactory, Class<?> domainClass, PartTree tree, ParameterAccessor accessor) {
        super(tree, accessor);

        this.factory = factory;
        this.tree = tree;
        this.schemaFactory = mapperfactory;
        this.accessor = accessor;
        this.schema = schemaFactory.getSchema(domainClass);
    }

    @Override
    protected GraphTraversal create(Part part, Iterator<Object> iterator) {
        return toCondition(part, iterator);
    }

    @Override
    protected GraphTraversal and(Part part, GraphTraversal base, Iterator<Object> iterator) {
        Pipe lastPipe = (Pipe) base.getPipes().get(base.getPipes().size() - 1);
        if (lastPipe instanceof AndFilterPipe) {
            return base.add(toCondition(part, iterator));
        }
        GraphTraversal andPipeline = new GremlinPipeline();
        andPipeline.and(base, toCondition(part, iterator));
        return andPipeline;
    }

    @Override
    protected GraphTraversal or(GraphTraversal base, GraphTraversal criteria) {
        return new GremlinPipeline().or(base, criteria);
    }

    public boolean isCountQuery() {
        return tree.isCountProjection();
    }

    @Override
    protected GraphTraversal complete(GraphTraversal criteria, Sort sort) {
        Pageable pageable = accessor.getPageable();
        GraphTraversal pipeline = new GraphTraversal(factory.graph());
        if (schema.isEdgeSchema()) {
            pipeline = pipeline.V().add(criteria);
        } else if (schema.isVertexSchema()) {
            pipeline = pipeline.V().and(criteria);
        }
//        pipeline = pipeline.and(criteria);
//        pipeline = pipeline.add(criteria);
        return pipeline;
    }

    protected GraphTraversal toCondition(Part part, Iterator<Object> iterator) {

        GraphTraversal pipeline = new GremlinPipeline();
        PropertyPath path = part.getProperty();
        PropertyPath leafProperty = path.getLeafProperty();
        String leafSegment = leafProperty.getSegment();
//        Class<?> type = leafProperty.getOwningType().getType();
//
//        GremlinSchema schema = schemaFactory.getSchema(type);
//        if (schema.isVertexSchema()) {
//            pipeline = pipeline.V();
//        } else if (schema.isEdgeSchema()) {
//            pipeline = pipeline.E();
//        }


        if(schema.isEdgeSchema()) {
            includeCondition(part.getType(), leafSegment, pipeline, iterator);
        }
        while (path != null && path.hasNext()) {

            String segment = path.getSegment();
            Class<?> type = path.getOwningType().getType();
            schema = schemaFactory.getSchema(type);
            GremlinProperty gremlinProperty = schema.getPropertyForFieldname(segment);

            if (schema.isVertexSchema()) {
                if (gremlinProperty instanceof GremlinRelatedProperty) {

                    GremlinRelatedProperty adjacentProperty = (GremlinRelatedProperty) gremlinProperty;
                    Direction direction = adjacentProperty.getDirection();
                    if (direction == Direction.IN) {
                        pipeline.inE(gremlinProperty.getName()).outV();
                    } else {
                        pipeline.outE(gremlinProperty.getName()).inV();
                    }
                }

            } else if (schema.isEdgeSchema()) {
                if (gremlinProperty instanceof GremlinAdjacentProperty) {
                    GremlinAdjacentProperty adjacentProperty = (GremlinAdjacentProperty)gremlinProperty;
                    Direction direction = adjacentProperty.getDirection();
                    if (direction == Direction.IN) {
                        pipeline.inE(schema.getClassName());
                    } else {
                        pipeline.outE(schema.getClassName());
                    }
                }
            }

            path = path.next();

        }

        if(schema.isVertexSchema()) {
            includeCondition(part.getType(), leafSegment, pipeline, iterator);
        }

        return pipeline;

//        if (schema.isVertexSchema()) {
//            Spliterator<PropertyPath> it = path.spliterator();
//            it.forEachRemaining(new Consumer<PropertyPath>() {
//                @Override
//                public void accept(PropertyPath propertyPath) {
//
//                    if (propertyPath.hasNext()) {
//                        String segment = propertyPath.getSegment();
//                        Class<?> type = propertyPath.getOwningType().getType();
//                        GremlinSchema schema = schemaFactory.getSchema(type);
//                        if (schema.isVertexSchema()) {
//                            GremlinProperty gremlinProperty = schema.getPropertyForFieldname(segment);
//                            String projectedName = gremlinProperty.getName();
//                            //                            pipeline.outE(projectedName).inV();
//                        } else {
//                            //                            pipeline.outE(schema.getClassName());
//                        }
//                    }
//                }
//            });
//        }

    }

    private GraphTraversal includeCondition(Part.Type type, String property, GraphTraversal traversal, Iterator iterator) {
        switch (type) {
        case AFTER:
        case GREATER_THAN:
            traversal.has(property, P.gt(iterator.next()));
            break;
        case GREATER_THAN_EQUAL:
            traversal.has(property, P.gte(iterator.next()));
            break;
        case BEFORE:
        case LESS_THAN:
            traversal.has(property, P.lt(iterator.next()));
            break;
        case LESS_THAN_EQUAL:
            traversal.has(property, P.lte(iterator.next()));
            break;
        case BETWEEN:
            Object val = iterator.next();
            traversal.has(property, P.lt(val)).has(property,P.gt(val));
            break;
        case IS_NULL:
            traversal.has(property, P.eq(null));
            break;
        case IS_NOT_NULL:
            traversal.has(property);
            break;
        case IN:
            traversal.has(property, Contains.within, iterator.next());
            break;
        case NOT_IN:
            traversal.has(property, Contains.without, iterator.next());
            break;
        case LIKE:
            traversal.has(property, Like.IS, iterator.next());
            break;
        case NOT_LIKE:
            traversal.has(property, Like.NOT, iterator.next());
            break;
        case STARTING_WITH:
            traversal.has(property, StartsWith.DOES, iterator.next());
            break;
        case ENDING_WITH:
            traversal.has(property, EndsWith.DOES, iterator.next());
            break;
        case CONTAINING:
            traversal.has(property, Like.IS, iterator.next());
            break;
        case SIMPLE_PROPERTY:
            traversal.has(property, iterator.next());
            break;
        case NEGATING_SIMPLE_PROPERTY:
            traversal.not(__.has(property, iterator.next()));
            break;
        case TRUE:
            traversal.has(property, true);
            break;
        case FALSE:
            traversal.has(property, false);
            break;
        default:
            throw new IllegalArgumentException("Unsupported keyword!");
        }

//        return new GremlinPipeline().and(pipeline);
        return traversal;
    }


    private enum StartsWith implements Predicate {
        DOES,
        NOT;

        public boolean evaluate(final Object first, final Object second) {
            if (first instanceof String && second instanceof String) {
                return this == DOES && ((String) second).startsWith((String) first);
            }
            return false;
        }
    }

    private enum EndsWith implements Predicate {
        DOES,
        NOT;

        public boolean evaluate(final Object first, final Object second) {

            if (first instanceof String && second instanceof String) {
                return this == DOES && ((String) second).endsWith((String) first);
            }
            return false;
        }
    }

    private enum Like implements Predicate {

        IS,
        NOT;

        public boolean evaluate(final Object first, final Object second) {
            if (first instanceof String && second instanceof String) {
                return this == IS && first.toString().toLowerCase().contains(second.toString().toLowerCase());
            }
            return false;
        }
    }
}
