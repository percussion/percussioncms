/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.exceptions;

import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.net.URL;

@Provider
public class PSUncaughtError extends Throwable implements ExceptionMapper<Throwable>
{
    private static final long serialVersionUID = 1L;

    private  static final Logger log = LogManager.getLogger(PSUncaughtError.class);

    @Context private HttpServletRequest request;

    @Context private HttpServletResponse response;

    @Override
    public Response toResponse(Throwable exception)
    {
        try {
            log.warn("Page redirecting to error {} : . Error code {} : . Error message {} : ", request.getHeader("referer"), response.getStatus(), exception.getLocalizedMessage());
            String referer = request.getHeader("referer");
            URL url = new URL(referer);
            String hostRedirect = url.getHost();
            Integer port = url.getPort();
            String errorRedirect = "";
            if(port != null && port>0){
                errorRedirect = request.getScheme()+"://"+hostRedirect+":"+port+"/error.html";
            }else{
                errorRedirect = request.getScheme()+"://"+hostRedirect+"/error.html";
            }
            response.sendRedirect(errorRedirect);
        } catch (IOException e) {
            log.error("Exception occurred while redirecting to error.html, Error: {}", PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
            return Response.status(500).entity("A server error happened. Please try your request again.").type("text/plain").build();
        }
        return null;
    }
}
