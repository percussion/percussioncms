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

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.share.service.exception.IPSValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSErrorUtils;
import com.percussion.share.validation.PSErrors;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Singleton;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@PSSiteManageBean("dataserviceExceptionMapper")
public class PSDataServiceExceptionMapper extends PSAbstractExceptionMapper<PSDataServiceException> implements ExceptionMapper<PSDataServiceException> {


    private static final String ERROR_MESSAGE = "PSDataServiceExceptionMapper exception mapper mapped exception:";

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected PSErrors createErrors(PSDataServiceException exception) {

        if( exception instanceof IPSValidationException) {
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
    protected Response.Status getStatus(PSDataServiceException exception)
    {
        if (exception instanceof IPSValidationException)
            return Response.Status.BAD_REQUEST;
        return super.getStatus(exception);
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);
}
