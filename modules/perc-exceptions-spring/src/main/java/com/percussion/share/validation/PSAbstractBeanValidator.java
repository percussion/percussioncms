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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.validation;

import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSParameterValidationUtils;
import com.percussion.share.service.exception.PSValidationException;
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
        ovalValidator.validate(object, errors);
        if (errors instanceof PSBeanValidationException) {
            try {
                doValidation((FULL) object, (PSBeanValidationException) errors);
            } catch (PSValidationException e) {
                ((PSBeanValidationException) errors).addSuppressed(e);
            }
        }
    }
}
