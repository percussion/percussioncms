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
package com.percussion.share.validation;

import static org.apache.commons.lang3.Validate.*;

import com.percussion.share.service.exception.PSValidationException;
import org.apache.commons.lang3.StringUtils;

import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.share.validation.PSErrors.PSObjectError;
import com.percussion.share.validation.PSValidationErrors.PSFieldError;
import org.apache.commons.lang3.Validate;


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
        Validate.notNull(field);
        Validate.notNull(code);
        Validate.notNull(defaultMessage);
        PSFieldError e = new PSFieldError();
        e.setCode(code);
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
    
    public PSValidationErrorsBuilder throwIfInvalid() throws PSValidationException {
        new PSParametersValidationException(validationErrors).throwIfInvalid();
        return this;
    }
    
    
}
