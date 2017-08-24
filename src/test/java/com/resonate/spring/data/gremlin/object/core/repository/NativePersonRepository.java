package com.resonate.spring.data.gremlin.object.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.resonate.spring.data.gremlin.annotation.Query;
import com.resonate.spring.data.gremlin.object.core.domain.Person;
import com.resonate.spring.data.gremlin.repository.GremlinRepositoryWithNativeSupport;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by gman on 25/06/15.
 */
public interface NativePersonRepository extends GremlinRepositoryWithNativeSupport<Person> {

    @Transactional
    @Query(value = "delete vertex from (select from Person where firstName <> ?)", nativeQuery = true, modify = true)
    Integer deleteAllExceptUser(String firstName);


    @Transactional
    @Query(value = "SELECT expand(in('was_located')) FROM (SELECT FROM Location WHERE [latitude,longitude,$spatial] NEAR [?,?,{\"maxDistance\":?}])", nativeQuery = true)
    Page<Person> findNear(double latitude, double longitude, double radius, Pageable pageable);

}
