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
package com.percussion.share.validation;

import static org.apache.commons.lang.Validate.*;

import org.apache.commons.lang.StringUtils;

import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.share.validation.PSErrors.PSObjectError;
import com.percussion.share.validation.PSValidationErrors.PSFieldError;


/**
 * A fluent patterned validation errors builder.
 * http://en.wikipedia.org/wiki/Fluent_interface
 * 
 * @author adamgent
 *
 */
public class PSValidationErrorsBuilder {
    
    private PSValidationErrors validationErrors;
    
    
    
    public PSValidationErrorsBuilder(String objectName) {
        super();
        this.validationErrors = new PSValidationErrors();
        this.validationErrors.setMethodName(objectName);
    }

    public  PSValidationErrorsBuilder reject(String code, String defaultMessage) {
        PSObjectError objectError = new PSObjectError();
        objectError.setCode(code);
        objectError.setDefaultMessage(defaultMessage);
        validationErrors.getGlobalErrors().add(objectError);
        return this;
    }
    
    public  PSValidationErrorsBuilder rejectField(String field, String code, String defaultMessage, Object value) {
        notNull(field);
        notNull(code);
        notNull(defaultMessage);
        PSFieldError e = new PSFieldError();
        e.setCode(code);
        //e.setCode(validationErrors.getMethodName() + "#" + field);
        e.setDefaultMessage(defaultMessage);
        e.setField(field);
        validationErrors.getFieldErrors().add(e);
        return this;
    }
    
    
    public PSValidationErrorsBuilder rejectIfNull(String field, Object value) {
        if (value == null)
            return rejectField(field, field + " cannot be null", value);
        return this;
    }
    
    public PSValidationErrorsBuilder rejectIfBlank(String field, String value) {
        if (StringUtils.isBlank(value))
            return rejectField(field, field + " cannot be blank", value);
        return this;
    }
    
    public  PSValidationErrorsBuilder rejectField(String field, String defaultMessage, Object value) {
        rejectField(field, validationErrors.getMethodName() + "#" +  field, defaultMessage, value);
        return this;
    }
    
    
    public PSValidationErrors build() {
        return validationErrors;
    }
    
    public PSValidationErrorsBuilder throwIfInvalid() {
        new PSParametersValidationException(validationErrors).throwIfInvalid();
        return this;
    }
    
    
}
