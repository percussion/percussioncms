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

import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
import net.sf.oval.exception.ValidationFailedException;
import net.sf.oval.integration.spring.SpringValidator;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * See springs {@link Validator}.
 * @author adamgent
 *
 * @param <FULL> the class to be validated.
 */
public abstract class PSAbstractBeanValidator<FULL> implements Validator
{
    private SpringValidator ovalValidator = new SpringValidator();
    {
        ovalValidator.setValidator(new net.sf.oval.Validator());
    }
    
    public PSBeanValidationException validate(FULL obj) throws PSValidationException {

        PSParameterValidationUtils.rejectIfNull("validate", "object", obj);
        PSBeanValidationException e = new PSBeanValidationException(obj, obj.getClass().getCanonicalName());
        validate(obj, e);
        return e;
    }

    protected abstract void doValidation(FULL obj, PSBeanValidationException e) throws PSValidationException;

    public boolean supports(Class clazz)
    {
        return ovalValidator.supports(clazz);
    }


    public void validate(Object object, Errors errors)  {
        try {
            ovalValidator.validate(object, errors);
            if (errors instanceof PSBeanValidationException) {
                try {
                    doValidation((FULL) object, (PSBeanValidationException) errors);
                } catch (PSValidationException e) {
                    ((PSBeanValidationException) errors).addSuppressed(e);
                }
            }
        }catch(ValidationFailedException ex){
            ((PSBeanValidationException) errors).addSuppressed(ex);
        }
    }
}
