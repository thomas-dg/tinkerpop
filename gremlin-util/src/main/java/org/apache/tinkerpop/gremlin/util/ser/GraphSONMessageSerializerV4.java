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
package org.apache.tinkerpop.gremlin.util.ser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;
import org.apache.tinkerpop.gremlin.structure.io.Buffer;
import org.apache.tinkerpop.gremlin.structure.io.binary.GraphBinaryWriter;
import org.apache.tinkerpop.gremlin.structure.io.binary.Marker;
import org.apache.tinkerpop.gremlin.structure.io.graphson.AbstractObjectDeserializer;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONMapper;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONUtil;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONVersion;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONXModuleV3;
import org.apache.tinkerpop.gremlin.util.message.RequestMessage;
import org.apache.tinkerpop.gremlin.util.message.RequestMessageV4;
import org.apache.tinkerpop.gremlin.util.message.ResponseMessage;
import org.apache.tinkerpop.gremlin.util.message.ResponseStatus;
import org.apache.tinkerpop.gremlin.util.message.ResponseStatusCode;
import org.apache.tinkerpop.shaded.jackson.core.JsonGenerator;
import org.apache.tinkerpop.shaded.jackson.core.JsonProcessingException;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.shaded.jackson.databind.SerializerProvider;
import org.apache.tinkerpop.shaded.jackson.databind.jsontype.TypeSerializer;
import org.apache.tinkerpop.shaded.jackson.databind.module.SimpleModule;
import org.apache.tinkerpop.shaded.jackson.databind.ser.std.StdSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serialize results to JSON with version 4.0.x schema and the extended module.
 */
public final class GraphSONMessageSerializerV4 extends AbstractGraphSONMessageSerializerV4 {

    public final static class GremlinServerModuleV4 extends SimpleModule {
        public GremlinServerModuleV4() {
            super("graphsonV4-gremlin-server");

            // SERIALIZERS
            addSerializer(ResponseMessage.class, new ResponseMessageSerializer());
            addSerializer(ResponseMessage.ResponseMessageHeader.class, new ResponseMessageHeaderSerializer());
            addSerializer(ResponseMessage.ResponseMessageFooter.class, new ResponseMessageFooterSerializer());
            addSerializer(RequestMessageV4.class, new GraphSONMessageSerializerV4.RequestMessageV4Serializer());

            // DESERIALIZERS
            addDeserializer(ResponseMessage.class, new ResponseMessageV4Deserializer());
            addDeserializer(RequestMessageV4.class, new GraphSONMessageSerializerV4.RequestMessageV4Deserializer());
        }
    }

    private static final String MIME_TYPE = SerTokens.MIME_GRAPHSON_V4;

    private static byte[] header;

    static {
        final ByteBuffer buffer = ByteBuffer.allocate(MIME_TYPE.length() + 1);
        buffer.put((byte) MIME_TYPE.length());
        buffer.put(MIME_TYPE.getBytes());
        header = buffer.array();
    }

    /**
     * Creates a default GraphSONMessageSerializer.
     * <p>
     * By default this will internally instantiate a {@link GraphSONMapper} and register
     * a {@link GremlinServerModule} and {@link GraphSONXModuleV3} to the mapper.
     *
     * @see #GraphSONMessageSerializerV4(GraphSONMapper.Builder)
     */
    public GraphSONMessageSerializerV4() {
        super();
    }

    /**
     * Create a GraphSONMessageSerializer with a provided {@link GraphSONMapper.Builder}.
     *
     * Note that to make this mapper usable in the context of request messages and responses,
     * this method will automatically register a {@link GremlinServerModule} to the provided
     * mapper.
     */
    public GraphSONMessageSerializerV4(final GraphSONMapper.Builder mapperBuilder) {
        super(mapperBuilder);
    }

    @Override
    public String[] mimeTypesSupported() {
        return new String[]{MIME_TYPE, SerTokens.MIME_JSON};
    }

    @Override
    GraphSONMapper.Builder configureBuilder(final GraphSONMapper.Builder builder) {
        // override the 2.0 in AbstractGraphSONMessageSerializerV2
        return builder.version(GraphSONVersion.V4_0).addCustomModule(new GremlinServerModuleV4());
    }

    @Override
    byte[] obtainHeader() {
        return header;
    }
}