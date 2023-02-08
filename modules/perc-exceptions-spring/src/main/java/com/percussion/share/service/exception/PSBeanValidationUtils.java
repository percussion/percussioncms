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
package com.percussion.share.service.exception;

import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.share.validation.PSValidationErrors;

public class PSBeanValidationUtils
{
    public static PSAbstractBeanValidator<Object> defaultValidator = new DefaultValidator();
    
    public static class DefaultValidator extends PSAbstractBeanValidator<Object> {

        @Override
        protected void doValidation(Object obj, PSBeanValidationException e)
        {
            //Do nothing.
        }
    }
    
    public static <FULL> PSValidationErrors getValidationErrorsOrFailIfInvalid(FULL obj) throws PSBeanValidationException{
       try {
           PSBeanValidationException e = defaultValidator.validate(obj);
           e.throwIfInvalid();
           return e.getValidationErrors();
       } catch (PSValidationException e) {
          throw new PSBeanValidationException(e);
       }
    }
    
    public static <FULL> PSBeanValidationException validate(FULL obj) {
        try {
            PSBeanValidationException e = defaultValidator.validate(obj);
            return e;
        } catch (PSValidationException e) {
            return new PSBeanValidationException(e);
        }

    }

    public static <FULL> PSValidationErrors getValidationErrors(FULL obj) {
        try {
            PSBeanValidationException e = defaultValidator.validate(obj);
            return e.getValidationErrors();
        } catch (PSValidationException e) {
            return new PSBeanValidationException(e).getValidationErrors();
        }
    }
    
    public static <FULL> void validate(FULL obj, PSBeanValidationException errors) {
        defaultValidator.validate(obj, errors);
    }
}
