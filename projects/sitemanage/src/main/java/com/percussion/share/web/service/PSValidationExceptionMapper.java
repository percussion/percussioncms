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

import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.util.PSSiteManageBean;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@PSSiteManageBean("validationExceptionMapper")
public class PSValidationExceptionMapper extends PSAbstractExceptionMapper<PSSpringValidationException> implements ExceptionMapper<PSSpringValidationException> {

    @Override
    protected PSValidationErrors createErrors(PSSpringValidationException exception) {
        return exception.getValidationErrors();
        
    }

    @Override
    protected Status getStatus(PSSpringValidationException exception) {
        return Status.BAD_REQUEST;
    }
    
    
    

}
