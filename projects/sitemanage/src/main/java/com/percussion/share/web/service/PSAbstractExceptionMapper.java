/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.share.web.service;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.percussion.share.validation.PSErrors;

/**
 * Mapping of Exceptions to a  {@link PSErrors serializable error object}.
 * 
 * @author adamgent
 *
 * @param <T> exception.
 */
@Provider
public abstract class PSAbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    public Response toResponse(T e) {
        Status status = getStatus(e);
        PSErrors errors = createErrors(e);
        return Response.status(status).entity(errors).build();
    }
    
    protected Status getStatus(T exception) {
        return Status.INTERNAL_SERVER_ERROR;
    }
    
    /**
     * 
     * Create a serializable errors object from the given exception.
     * 
     * @param exception never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected abstract PSErrors createErrors(T exception);

}
