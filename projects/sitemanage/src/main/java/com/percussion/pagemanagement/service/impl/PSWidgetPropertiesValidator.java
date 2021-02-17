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
package com.percussion.pagemanagement.service.impl;

import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref;
import com.percussion.pagemanagement.data.PSWidgetDefinition.UserPref;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetProperties.PSWidgetProperty;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.impl.PSWidgetUtils.PSWidgetPropertyBlankStringCoercionException;
import com.percussion.pagemanagement.service.impl.PSWidgetUtils.PSWidgetPropertyCoercionException;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSPropertiesValidationException;
import com.percussion.share.service.exception.PSValidationException;
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
                    log.debug("Field '{}' does not exist in widget '{}'.",
                            name,
                            definition.getId() );

                } else {
                    validate(name, object, p, properties, e);
                }
            }
        } catch (IPSDataService.DataServiceLoadException | PSValidationException | IPSDataService.DataServiceNotFoundException dataServiceLoadException) {
            e.addSuppressed(dataServiceLoadException);
        }
    }

    @Override
    protected Class<PSWidgetItem> getType()
    {
        return PSWidgetItem.class;
    }
    
    private PSWidgetDefinition getDefinition(PSWidgetItem widgetItem) throws PSValidationException, IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException {
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
            log.debug("Failed to coerce blank widget property: " 
                    + name 
                    + " Is is not a string and since its blank we will skip it.", e);
            return;
        }
        catch (PSWidgetPropertyCoercionException e)
        {
            log.warn("Failed coerce widget property: ", e);
        }
        boolean valid = validateType(klass, userPref.getDisplayName(), object, errors);
        if (valid &&  isEnum(userPref)) {
            valid = getEnums(userPref).contains(object);
            if ( ! valid )
                errors.rejectValue(name, "widgetItem.badEnumValue", "Bad enum value");
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
