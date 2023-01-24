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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.percussion.share.service.exception.PSErrorUtils;
import com.percussion.share.validation.PSErrors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


@Provider
@Component
@Produces(MediaType.APPLICATION_JSON)
public class PSJsonProcessingExceptionMapper extends PSAbstractExceptionMapper<JsonProcessingException> implements ExceptionMapper<JsonProcessingException> {


    private static final String ERROR_MESSAGE = "JSON error: ";

    @Override
    protected PSErrors createErrors(JsonProcessingException exception) {
        String errorMessage = exception.getClass().getName();

        if (log.isDebugEnabled())
            log.debug(errorMessage, exception);

        PSErrors errors = PSErrorUtils.createErrorsFromException(exception);

        return errors;
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(JsonProcessingException.class);
}
