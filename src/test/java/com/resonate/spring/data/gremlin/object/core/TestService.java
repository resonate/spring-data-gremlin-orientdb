package com.resonate.spring.data.gremlin.object.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import com.resonate.spring.data.gremlin.object.core.domain.Address;
import com.resonate.spring.data.gremlin.object.core.domain.Person;
import com.resonate.spring.data.gremlin.object.core.repository.AddressRepository;
import com.resonate.spring.data.gremlin.object.core.repository.NativePersonRepository;
import com.resonate.spring.data.gremlin.object.core.repository.PersonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by gman on 30/07/15.
 */
@Service
public class TestService {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired(required = false)
    private NativePersonRepository nativePersonRepository;

    @Transactional
    public void create(Person person) {
        repository.save(person);
    }

    @Transactional
    public void failCreate(Person person, Address address) {
        create(person);

        Person test = new Person("Graham", "Webber", address, true);
        repository.save(test);
        repository.findByAddress_Area_Name("asdf");
        repository.queryLastName("asdf", new PageRequest(0, 2));
        if (nativePersonRepository != null) {
            nativePersonRepository.findNear(-33d, 151d, 50, new PageRequest(0, 2));
        }
        addressRepository.save(address);
        throw new IllegalStateException();
    }

}
