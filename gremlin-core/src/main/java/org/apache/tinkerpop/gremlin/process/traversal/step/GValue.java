/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.traversal.step;

import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * A {@code GValue} is a variable or literal value that is used in a {@link Traversal}. It is composed of a key-value
 * pair where the key is the name given to the variable and the value is the object that the variable resolved to. If
 * the name is not given, the value was provided literally in the traversal. The value of the variable can be any
 * object. The {@code GValue} also includes the {@link GType} that describes the type it contains.
 */
public class GValue<V> {
    private final String name;
    private final GType type;

    private final V value;

    private GValue(final GType type, final V value) {
        this(null, type, value);
    }

    private GValue(final String name, final GType type, final V value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public boolean isVariable() {
        return this.name != null;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public GType getType() {
        return this.type;
    }

    public V get() {
        return this.value;
    }

    @Override
    public String toString() {
        return isVariable() ?
                String.format("%s&%s", name, value) : Objects.toString(value);
    }

    /**
     * Create a new {@code Var} with the specified name and value.
     *
     * @param name the name of the variable
     * @param value the value of the variable
     */
    public static <V> GValue<V> of(final String name, final V value) {
        return new GValue<>(name, GType.getType(value), value);
    }

    public static GValue<String> ofString(final String name, final String value) {
        return new GValue<>(name, GType.STRING, value);
    }

    public static GValue<Integer> ofInteger(final String name, final Integer value) {
        return new GValue<>(name, GType.INTEGER, value);
    }

    public static GValue<Boolean> ofBoolean(final String name, final Boolean value) {
        return new GValue<>(name, GType.BOOLEAN, value);
    }

    public static GValue<Double> ofDouble(final String name, final Double value) {
        return new GValue<>(name, GType.DOUBLE, value);
    }

    public static GValue<Double> ofDouble(final Double value) {
        return new GValue<>(GType.DOUBLE, value);
    }

    public static GValue<BigInteger> ofBigInteger(final String name, final BigInteger value) {
        return new GValue<>(name, GType.BIG_INTEGER, value);
    }

    public static GValue<BigDecimal> ofBigDecimal(final String name, final BigDecimal value) {
        return new GValue<>(name, GType.BIG_DECIMAL, value);
    }

    public static GValue<Long> ofLong(final String name, final Long value) {
        return new GValue<>(name, GType.LONG, value);
    }

    public static GValue<Map> ofMap(final String name, final Map value) {
        return new GValue<>(name, GType.MAP, value);
    }

    public static GValue<List> ofList(final String name, final List value) {
        return new GValue<>(name, GType.LIST, value);
    }

    public static GValue<Set> ofSet(final String name, final Set value) {
        return new GValue<>(name, GType.SET, value);
    }

    public static GValue<Vertex> ofVertex(final String name, final Vertex value) {
        return new GValue<>(name, GType.VERTEX, value);
    }

    public static GValue<Edge> ofEdge(final String name, final Edge value) {
        return new GValue<>(name, GType.EDGE, value);
    }

    public static GValue<Path> ofPath(final String name, final Path value) {
        return new GValue<>(name, GType.PATH, value);
    }

    public static GValue<Property> ofProperty(final String name, final Property value) {
        return new GValue<>(name, GType.PROPERTY, value);
    }
}
