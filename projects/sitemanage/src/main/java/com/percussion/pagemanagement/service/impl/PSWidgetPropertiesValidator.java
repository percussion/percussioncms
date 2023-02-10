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
package com.percussion.pagemanagement.service.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetProperties.PSWidgetProperty;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.impl.PSWidgetUtils.PSWidgetPropertyBlankStringCoercionException;
import com.percussion.pagemanagement.service.impl.PSWidgetUtils.PSWidgetPropertyCoercionException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.validation.PSAbstractPropertiesValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.percussion.pagemanagement.service.impl.PSWidgetUtils.coerceProperty;
import static com.percussion.pagemanagement.service.impl.PSWidgetUtils.getEnums;
import static com.percussion.pagemanagement.service.impl.PSWidgetUtils.isEnum;
import static java.util.Collections.sort;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Validates widget properties using the {@link UserPref} 
 * into a plain java object.
 * <p>
 * The converted java object is one of the following:
 * <ul>
 * <li>string - {@link String}</li>
 * <li>number - {@link Number}</li>
 * <li>enum - {@link String}</li>
 * <li>list of strings - {@link List}</li>
 * <li>boolean - {@link Boolean}</li>
 * <li>date - {@link Date}</li>
 * <li>hidden - {@link Object}</li>
 * </ul>
 *
 * @author adamgent
 * @param <T> There are different types of widget properties for example user and css properties.
 *
 */
public abstract class PSWidgetPropertiesValidator<T extends AbstractUserPref> extends PSAbstractPropertiesValidator<PSWidgetItem>
{

    
    private IPSWidgetService widgetService;
    
    public PSWidgetPropertiesValidator(IPSWidgetService widgetService)
    {
        super();
        this.widgetService = widgetService;
    }
    
    protected abstract Map<String, Object> getProperties(PSWidgetItem widgetItem);
    
    protected abstract Map<String, T> getPropertyDefinitions(PSWidgetDefinition definition);
    

    @Override
    protected void doValidation(PSWidgetItem widgetItem, PSPropertiesValidationException e)
    {
        try {
            PSWidgetDefinition definition = getDefinition(widgetItem);
            Map<String, Object> properties = getProperties(widgetItem);
            e.getProperties().putAll(properties);

            List<String> names = new ArrayList<>(properties.keySet());
            sort(names);

            Map<String, T> prefs = getPropertyDefinitions(definition);

            for (String name : names) {

                Object object = properties.get(name);

                T p = prefs.get(name);
                if (p == null) {
                    // The widget contains a preference which has been removed from the definition and is no longer used.
                    log.warn("Field '{}' does not exist in widget '{}'.",
                            name,
                            definition.getId() );

                } else {
                    validate(name, object, p, properties, e);
                }
            }
        } catch (PSDataServiceException dataServiceLoadException) {
            e.addSuppressed(dataServiceLoadException);
        }
    }

    @Override
    protected Class<PSWidgetItem> getType()
    {
        return PSWidgetItem.class;
    }
    
    private PSWidgetDefinition getDefinition(PSWidgetItem widgetItem) throws PSDataServiceException {
        PSBeanValidationUtils.validate(widgetItem).throwIfInvalid();
        return widgetService.load(widgetItem.getDefinitionId());
    }
    
    
    /**
     * Will convert the {@link PSWidgetProperty} to an object based on the {@link UserPref#getDatatype()}.
     * <p>
     * The abstract implementation will dispatch to <code>getXXX</code> where <code>XXX</code> is the 
     * datatype.
     * @param name never <code>null</code> or empty.
     * @param object never <code>null</code>.
     * @param userPref never <code>null</code>.
     * @param properties never <code>null</code>.
     * @param errors validation object used to keep tracking of validation errors, never <code>null</code>.
     */
    public void validate(String name, Object object, T userPref, Map<String, Object> properties, Errors errors) {
        notNull(name, "name");
        Class<?> klass = PSWidgetUtils.getJavaType(userPref);
        try
        {
            object = coerceProperty(name, object, klass);
            properties.put(name, object);
        }
        catch (PSWidgetPropertyBlankStringCoercionException e)
        {
            log.debug("Failed to coerce blank widget property:{}  Is is not a string and since its blank we will skip it. Error: {} "
                    , name
                    , PSExceptionUtils.getMessageForLog(e));
            return;
        }
        catch (PSWidgetPropertyCoercionException e)
        {
            log.warn("Failed coerce widget property: Error: {}", PSExceptionUtils.getMessageForLog(e));
        }
        boolean valid = validateType(klass, userPref.getDisplayName(), object, errors);
        if (valid &&  isEnum(userPref)) {
            valid = getEnums(userPref).contains(object);
            if ( ! valid ) {
                //If the enum value is invalid - this is probably an upgrade issue - just use the default value instead of erroring.
                Object obj = userPref.getDefaultValue();
                valid = getEnums(userPref).contains(obj);
                if ( ! valid ) {
                    //default value on the property is no good.
                    errors.rejectValue(name, "widgetItem.badEnumValue", "Bad enum value");
                }else{
                    properties.put(name,String.valueOf(obj));
                }
            }
        }
    }
    

    
    protected boolean validateType(final Class<?> klass, String name, Object data, Errors errors) {
        if ( ! klass.isInstance(data) ) {
            
            errors.rejectValue(name, "widgetItem.invalidDataType", "Data is invalid for the property: " + name);
            log.debug("Wrong data type is entered for the property : {}", name);
            return false;
        }
        return true;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSWidgetPropertiesValidator.class);

}
