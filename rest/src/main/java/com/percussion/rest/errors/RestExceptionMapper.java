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

package com.percussion.rest.errors;

import java.util.List;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestExceptionMapper implements ExceptionMapper<RestExceptionBase>
{
    @Context
    private HttpHeaders headers;

    public Response toResponse(RestExceptionBase e)
    {

        ResponseBuilder rb = Response.status(e.getStatus()).entity(
                new RestError(e.getErrorCode().getNumVal(), e.getClass().getSimpleName(), e.getMessage(), e.getDetailMessage(), e
                        .getErrorData()));

        List<MediaType> accepts = headers.getAcceptableMediaTypes();
        MediaType mt = null;
        if (accepts != null && accepts.size() > 0)
        {
            for (MediaType accept : accepts)
            {
                if (accept.equals(MediaType.APPLICATION_JSON) || accept.equals(MediaType.APPLICATION_XML))
                {
                    mt = accept;
                    break;
                }
            }
        }
        if (mt == null)
            mt = MediaType.valueOf(MediaType.APPLICATION_XML);
        // if not specified, use the default json
        rb = rb.type(mt); // set the response type to the entity type.

        return rb.build();
    }
}
