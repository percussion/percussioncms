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
package com.percussion.share.validation;

import static org.apache.commons.lang3.Validate.*;

import org.apache.commons.lang3.Validate;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.percussion.share.service.exception.PSPropertiesValidationException;

/**
 * An adapter to validate property like objects.
 * 
 * @author adamgent
 *
 * @param <PROPERTIES> property like object.
 */
public abstract class PSAbstractPropertiesValidator<PROPERTIES> implements Validator
{

    protected abstract Class<PROPERTIES> getType();
    
    public PSPropertiesValidationException validate(PROPERTIES obj) {
        PSPropertiesValidationException e = new PSPropertiesValidationException(obj, obj.getClass().getCanonicalName());
        validate(obj, e);
        return e;
    }
    
    @SuppressWarnings("unchecked")
    public boolean supports(Class klass)
    {
        Validate.notNull(getType());
        if (klass == getType()) return true;
        return false;
    }

    protected abstract void doValidation(PROPERTIES properties, PSPropertiesValidationException e);
    
    @SuppressWarnings("unchecked")
    public void validate(Object properties, Errors errors)
    {
        doValidation((PROPERTIES) properties, (PSPropertiesValidationException) errors);
    }

}
