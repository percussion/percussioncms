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
package com.percussion.share.web.service;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.service.exception.IPSValidationException;
import com.percussion.share.service.exception.PSErrorUtils;
import com.percussion.share.validation.PSErrors;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


/**
 * 
 * Maps All runtime exceptions into a valid {@link PSErrors error object} that can they be serialized
 * and used for the REST layer.
 * <p>
 * If the exception is a validation exception (implements {@link IPSValidationException}), an error code
 * of <code>400</code> will be returned with a {@link PSValidationErrors} object in the response. Otherwise
 * the error code is <code>500</code> with a {@link PSErrors} object in the response.
 * 
 * @author adamgent
 *
 */
@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
@PSSiteManageBean("runtimeExceptionMapper")
public class PSRuntimeExceptionMapper extends PSAbstractExceptionMapper<RuntimeException> implements ExceptionMapper<RuntimeException> {


    private static final String ERROR_MESSAGE = "REST exception mapper mapped exception:";

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected PSErrors createErrors(RuntimeException exception) {
        
        if( exception instanceof IPSValidationException ) {
            log.debug(ERROR_MESSAGE, exception);
            IPSValidationException ve = (IPSValidationException) exception;
            PSErrors errors = ve.getValidationErrors();
            if (errors != null) return errors;
        }
        else {

            log.error(ERROR_MESSAGE + PSExceptionUtils.getMessageForLog(exception));

            log.debug(exception);
        }
        
        PSErrors errors = PSErrorUtils.createErrorsFromException(exception);
        
        return errors;
    }
    
    
    
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected Status getStatus(RuntimeException exception)
    {
        if (exception instanceof IPSValidationException)
            return Status.BAD_REQUEST;
        return super.getStatus(exception);
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSRuntimeExceptionMapper.class);
}
