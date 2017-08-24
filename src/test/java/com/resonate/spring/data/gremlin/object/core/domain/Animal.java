package com.resonate.spring.data.gremlin.object.core.domain;

import com.resonate.spring.data.gremlin.annotation.Link;

import java.util.Set;

/**
 * @author Gman
 * @created 21/04/2016
 */
public class Animal<T extends Place> {
    @Link
    private Set<T> stuff;

    @Link
    private T linkedStuff;

    public Set<T> getStuff() {
        return stuff;
    }

    public void setStuff(Set<T> stuff) {
        this.stuff = stuff;
    }
}
