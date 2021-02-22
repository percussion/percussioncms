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
