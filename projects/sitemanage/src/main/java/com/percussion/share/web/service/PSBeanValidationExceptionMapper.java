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

package com.percussion.share.web.service;

import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSValidationException;
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
@PSSiteManageBean("beanValidationExceptionMapper")
public class PSBeanValidationExceptionMapper extends PSAbstractExceptionMapper<PSBeanValidationException> implements ExceptionMapper<PSBeanValidationException> {
    private static final Logger log = LogManager.getLogger(PSBeanValidationExceptionMapper.class);
    private static final String ERROR_MESSAGE = "REST exception mapper mapped exception:";

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected PSErrors createErrors(PSBeanValidationException exception) {
        log.debug(ERROR_MESSAGE, exception);
        return exception.getValidationErrors();

    }

    @Override
    @Produces(MediaType.APPLICATION_JSON)
    protected Response.Status getStatus(PSBeanValidationException exception)
    {
        return super.getStatus(exception);
    }
}
