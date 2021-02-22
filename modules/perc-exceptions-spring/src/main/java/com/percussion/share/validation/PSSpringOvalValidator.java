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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.share.validation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.ValidationFailedException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * Implements spring's validator. Uses Oval {@link net.sf.oval.Validator} for
 * actually validating the objects. Supports validating nested complex
 * properties, which should be marked with {@link PSValidateNestedProperty}.
 * 
 * @author SergeyZ
 * 
 */
public class PSSpringOvalValidator implements Validator, InitializingBean
{

    private net.sf.oval.Validator validator;

    public PSSpringOvalValidator(net.sf.oval.Validator validator)
    {
        this.validator = validator;
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz)
    {
        return true;
    }

    public void validate(Object target, Errors errors)
    {
        doValidate(target, errors, "");
    }

    private void doValidate(Object target, Errors errors, String fieldPrefix)
    {
        try
        {
            for (ConstraintViolation violation : validator.












                    validate(target))





            
            {
                OValContext ctx = violation.getContext();
                String errorCode = violation.getErrorCode();
                String errorMessage = violation.getMessage();

                if (ctx instanceof FieldContext)
                {
                    String fieldName = fieldPrefix + ((FieldContext) ctx).getField().getName();
                    errors.rejectValue(fieldName, errorCode, errorMessage);
                }
                else
                {
                    errors.reject(errorCode, errorMessage);
                }
            }

            try
            {
                Field[] fields = getFields(target);
                for (Field field : fields)
                {
                    String name = field.getName();
                    PSValidateNestedProperty validate = field.getAnnotation(PSValidateNestedProperty.class);
                    if (validate != null)
                    {
                        if (!field.isAccessible())
                        {
                            field.setAccessible(true);
                        }
                        Object nestedProperty = field.get(target);
                        if (nestedProperty != null)
                        {
                            doValidate(nestedProperty, errors, name + ".");
                        }
                    }
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

        }
        catch (final ValidationFailedException ex)
        {
            errors.reject(ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Field[] getFields(Object target)
    {
        Class clazz = target.getClass();
        List<Field> fields = doGetFields(clazz);
        return fields.toArray(new Field[fields.size()]);
    }

    @SuppressWarnings("unchecked")
    private List<Field> doGetFields(Class clazz)
    {
        ArrayList<Field> list = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        list.addAll(Arrays.asList(fields));
        if (clazz.getSuperclass() != null)
        {
            list.addAll(doGetFields(clazz.getSuperclass()));
        }
        return list;
    }

    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(validator, "Property [validator] is not set");
    }

    public net.sf.oval.Validator getValidator()
    {
        return validator;
    }

    public void setValidator(net.sf.oval.Validator validator)
    {
        this.validator = validator;
    }

}
