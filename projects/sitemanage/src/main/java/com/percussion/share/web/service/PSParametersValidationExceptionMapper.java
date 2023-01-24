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
import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.share.validation.PSErrors;
import com.percussion.util.PSSiteManageBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
@PSSiteManageBean("parametersValidationExceptionMapper")
public class PSParametersValidationExceptionMapper extends PSAbstractExceptionMapper<PSParametersValidationException> implements ExceptionMapper<PSParametersValidationException> {
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);
    private static final String ERROR_MESSAGE = "PSParametersValidationExceptionMapper exception mapper mapped exception:";

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected PSErrors createErrors(PSParametersValidationException exception) {
        log.debug(ERROR_MESSAGE, exception);
        return exception.getValidationErrors();

    }

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected Response.Status getStatus(PSParametersValidationException exception)
    {
        return super.getStatus(exception);
    }
}
