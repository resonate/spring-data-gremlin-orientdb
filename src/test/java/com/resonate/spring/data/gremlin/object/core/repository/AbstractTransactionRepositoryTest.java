package com.resonate.spring.data.gremlin.object.core.repository;

import com.google.common.collect.Lists;
import com.resonate.spring.data.gremlin.object.core.domain.Address;
import com.resonate.spring.data.gremlin.object.core.domain.Area;
import com.resonate.spring.data.gremlin.object.core.domain.Country;
import com.resonate.spring.data.gremlin.object.core.domain.Person;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by gman on 30/07/15.
 */
@Ignore
public class AbstractTransactionRepositoryTest extends BaseRepositoryTest {

    @Test
    public void should_rollback() {

        List<Person> peopele = Lists.newArrayList(repository.findAll());
        int beforeRollback = peopele.size();
        testService.create(new Person("Graham", "Webber", null, true));

        peopele = Lists.newArrayList(repository.findAll());
        assertEquals(beforeRollback + 1, peopele.size());

        Address address1 = new Address(new Country("Australia"), "Newcastle", "Scenic Dr", new Area("2291"));

        Person graham1 = new Person("Graham", "Webber", address1, true);
        try {
            testService.failCreate(graham1, address1);
            fail("Should've thrown exception");
        } catch (RuntimeException e) {

        }

        peopele = Lists.newArrayList(repository.findAll());
        assertEquals(beforeRollback + 1, peopele.size());
    }

}
