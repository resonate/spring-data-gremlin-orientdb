package com.resonate.spring.data.gremlin.object.core.repository;

import com.resonate.spring.data.gremlin.object.core.domain.Likes;
import com.resonate.spring.data.gremlin.annotation.Query;
import com.resonate.spring.data.gremlin.repository.GremlinRepository;

import java.util.List;

/**
 * Created by gman on 4/06/15.
 */
public interface LikesRepository extends GremlinRepository<Likes> {


    List<Likes> findByPerson1_FirstName(String firstName);

    @Query(value = "graph.E().has('date')")
    List<Likes> findByHasDate();

    @Query(value = "graph.V().has('firstName', ?).outE('Likes').as('x').inV.filter{it.firstName == ?}.back('x')")
    List<Likes> findByLiking(String liker, String liked);



}
