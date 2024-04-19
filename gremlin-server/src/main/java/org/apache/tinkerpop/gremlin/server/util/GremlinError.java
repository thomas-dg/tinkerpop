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
package org.apache.tinkerpop.gremlin.server.util;

import org.apache.tinkerpop.gremlin.process.traversal.Failure;
import org.apache.tinkerpop.gremlin.util.ExceptionHelper;
import org.apache.tinkerpop.gremlin.util.Tokens;
import org.apache.tinkerpop.gremlin.util.message.RequestMessageV4;
import org.apache.tinkerpop.gremlin.util.message.ResponseStatusCode;

import java.util.Set;

public class GremlinError {
    private final ResponseStatusCode code;
    private final String message;
    private final String exception;

    private GremlinError(ResponseStatusCode code, String message, String exception) {

        this.code = code;
        this.message = message;
        this.exception = exception;
    }

    public ResponseStatusCode getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getException() {
        return exception;
    }

    // ------------ request validation errors

    // script type errors
    public static GremlinError invalidGremlinType(final RequestMessageV4 requestMessage ) {
        final String message = String.format("Message could not be parsed. Check the format of the request. [%s]",
                requestMessage);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError unknownGremlinType(final RequestMessageV4 requestMessage ) {
        final String message = String.format("Message with gremlin of type [%s] is not recognized.",
                requestMessage.getGremlinType());
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    // script errors
    public static GremlinError binding() {
        final String message = String.format("The [%s] message is using one or more invalid binding keys - they must be of type String and cannot be null",
                Tokens.OPS_EVAL);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError binding(final Set<String> badBindings) {
        final String message = String.format("The [%s] message supplies one or more invalid parameters key of [%s] - these are reserved names.",
                Tokens.OPS_EVAL, badBindings);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError binding(final int bindingsCount, final int allowedSize) {
        final String message = String.format("The [%s] message contains %s bindings which is more than is allowed by the server %s configuration",
                Tokens.OPS_EVAL, bindingsCount, allowedSize);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError binding(final String aliased) {
        final String message = String.format("Could not alias [%s] to [%s] as [%s] not in the Graph or TraversalSource global bindings",
                Tokens.ARGS_G, aliased, aliased);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    // bytecode errors

    public static GremlinError gremlinType() {
        final String message = String.format("A [%s] message requires a gremlin argument that is of type %s.",
                Tokens.OPS_BYTECODE, Tokens.ARGS_GREMLIN);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError traversalSource() {
        final String message = String.format("A [%s] message requires a [%s] argument.", Tokens.OPS_BYTECODE, Tokens.ARGS_G);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError traversalSource(final String traversalSourceName ) {
        final String message = String.format("The traversal source [%s] for alias [%s] is not configured on the server.",
                traversalSourceName, Tokens.VAL_TRAVERSAL_SOURCE_ALIAS);
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, message, "InvalidRequestException");
    }

    public static GremlinError lambdaNotSupported(final Throwable t) {
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, t.getMessage(), "InvalidRequestException");
    }

    public static GremlinError deserializeTraversal(final Throwable t) {
        return new GremlinError(ResponseStatusCode.BAD_REQUEST, t.getMessage(), "InvalidRequestException");
    }

    // execution errors
    public static GremlinError timeout(final RequestMessageV4 requestMessage ) {
        final String message = String.format("A timeout occurred during traversal evaluation of [%s] - consider increasing the limit given to evaluationTimeout",
                requestMessage);
        return new GremlinError(ResponseStatusCode.SERVER_ERROR, message, "ServerTimeoutExceededException");
    }

    public static GremlinError timedInterruptTimeout() {
        return new GremlinError(ResponseStatusCode.SERVER_ERROR,
                "Timeout during script evaluation triggered by TimedInterruptCustomizerProvider",
                "ServerTimeoutExceededException");
    }

    public static GremlinError rateLimiting() {
        return new GremlinError(ResponseStatusCode.TOO_MANY_REQUESTS,
                "Too many requests have been sent in a given amount of time.", "TooManyRequestsException");
    }

    public static GremlinError serialization(Exception ex) {
        final String message = String.format("Error during serialization: %s", ExceptionHelper.getMessageFromExceptionOrCause(ex));
        return new GremlinError(ResponseStatusCode.SERVER_ERROR, message, "ServerSerializationException");
    }

    public static GremlinError wrongSerializer(Exception ex) {
        final String message = String.format("Error during serialization: %s", ExceptionHelper.getMessageFromExceptionOrCause(ex));
        return new GremlinError(ResponseStatusCode.SERVER_ERROR, message, "ServerSerializationException");
    }

    public static GremlinError longFrame(Throwable t) {
        final String message = t.getMessage() + " - increase the maxContentLength";
        // todo: ResponseEntityTooLargeException? !!!
        return new GremlinError(ResponseStatusCode.PAYLOAD_TOO_LARGE, message, "RequestEntityTooLargeException");
    }

    public static GremlinError longRequest(final RequestMessageV4 requestMessage ) {
        final String message = String.format("The Gremlin statement that was submitted exceeds the maximum compilation size allowed by the JVM, please split it into multiple smaller statements - %s", requestMessage.trimMessage(1021));
        return new GremlinError(ResponseStatusCode.PAYLOAD_TOO_LARGE, message, "RequestEntityTooLargeException");
    }

    public static GremlinError temporary(final Throwable t) {
        return new GremlinError(ResponseStatusCode.SERVER_ERROR, t.getMessage(), "ServerEvaluationException");
    }

    public static GremlinError failStep(final Failure failure) {
        // todo: double check message
        return new GremlinError(ResponseStatusCode.SERVER_ERROR,
                failure.getMessage() + ". " + failure.format(), "ServerFailStepException");
    }

    public static GremlinError general(final Throwable t) {
        final String message = (t.getMessage() == null) ? t.toString() : t.getMessage();
        return new GremlinError(ResponseStatusCode.SERVER_ERROR, message, "ServerErrorException");
    }
}