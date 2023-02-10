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
import com.percussion.pathmanagement.service.IPSPathService;
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
@PSSiteManageBean("pathServiceExceptionMapper")
public class PSPathServiceExceptionMapper extends PSAbstractExceptionMapper<IPSPathService.PSPathServiceException> implements ExceptionMapper<IPSPathService.PSPathServiceException> {

    private static final String ERROR_MESSAGE = "PSPathServiceExceptionMapper exception mapper mapped exception:";

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected PSErrors createErrors(IPSPathService.PSPathServiceException exception) {
        log.debug(ERROR_MESSAGE, exception);
        PSErrors.PSObjectError poe = new PSErrors.PSObjectError();
        String cause = exception.getMessage();
        if(exception.getCause() != null ){
            if( exception.getCause().getLocalizedMessage() != null) {
                cause = exception.getCause().getLocalizedMessage();
            }else if (exception.getCause().getMessage() != null){
                cause = exception.getCause().getMessage();
            }
        }
        poe.setDefaultMessage(cause);
        PSErrors pe = new PSErrors();
        pe.setGlobalError(poe);
        return pe;

    }

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected Response.Status getStatus(IPSPathService.PSPathServiceException exception)
    {
        return super.getStatus(exception);
    }
    
}
