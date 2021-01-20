/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
