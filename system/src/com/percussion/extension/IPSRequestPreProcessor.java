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
package com.percussion.extension;

import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * The IPSRequestPreProcessor interface must be implemented by
 * extensions which pre-process the request.
 * <P>
 * When Rhythmyx receives the request, it breaks the request down into the
 * CGI variables, cookies, HTML parameters and the input XML document
 * which were sent with the request. The information stored with the
 * request can then be validated or authenticated. If a validation
 * exception is thrown, the Rhythmyx server will continue to check if
 * another request handler is defined for this request which does pass the
 * selection criteria. If no other handler is found, or if an authentication
 * exception was thrown, an error response will be returned.
 * <P>
 * Any changes to the data (CGI variables, input XML document, etc.)
 * will be made available to the application during processing.
 *
 * @author     Tas Giakouminakis
 * @version    1.1
 * @since      1.0
 */
public interface IPSRequestPreProcessor extends IPSExtension
{
   /**
    * Performs any pre-processing on the request.
    * <p>
    * When Rhythmyx receives the request, it breaks the request down
    * into components sent with the request: CGI variables, cookies,
    * HTML parameters, and the input XML document. The information
    * stored with the request can then be validated or authenticated.
    * If a validation exception (PSRequestValidationException) or an
    * authentication exception (PSAuthorizationException) is thrown,
    * an error response will be returned.
    * <p>
    * Any changes to the data (CGI variables, input XML document, etc.)
    * is available to the application during processing.
    * <p>
    * <em>NOTE:</em> The implementation of this method must be
    * safe for multi-threaded use. One instance of the extension will
    * be defined for each usage of the handler across all applications.
    * <P>
    * When processing requests, the same instance may be accessed by
    * several request threads, each thread with its own set of data. As such,
    * any variables used during run-time execution should be defined within
    * the method (not the class). Another alternative is to use variables of
    * type java.lang.ThreadLocal to define thread specific copies of the
    * variable.
    *
    * @param      params         the parameter values supplied with the
    *                            request in the appropriate order, as
    *                            specified by the parameter definitions
    *                            returned by the
    *                            IPSExtensionDef.getRuntimeParameter method.
    *
    * @param      request        the request context object
    *
    * @throws PSAuthorizationException If the user is not authorized to
    * perform this request.
    *
    * @throws PSRequestValidationException If the request does not meet the
    * required validation rules.
    *
    * @throws PSParameterMismatchException If a call to setParamValues was
    * never made, or the runtime parameters specified in that call are
    * incorrect for the usage of this extension.
    *
    * @throws PSExtensionProcessingException If any other exception occurs which
    * prevents the proper handling of this request
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws
         PSAuthorizationException,
         PSRequestValidationException,
         PSParameterMismatchException,
         PSExtensionProcessingException;
}
