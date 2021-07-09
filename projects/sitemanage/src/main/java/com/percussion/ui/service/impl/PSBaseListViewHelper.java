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
package com.percussion.ui.service.impl;

import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.ui.service.IPSListViewProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for all {@link IPSListViewHelper} implementations. It validates
 * the {@link PSDisplayPropertiesCriteria} object, checks the "relatedObject"
 * type in the {@link PSPathItem} object to see if it's the one expected by the
 * underlying implementation and properly sets the display properties on the
 * {@link PSPathItem} objects (this implementation doesn't update already
 * existing entries in the current display properties, just adds new ones).
 * 
 * @author miltonpividori
 * 
 */
public abstract class PSBaseListViewHelper implements IPSListViewHelper
{
    protected static final Logger log = LogManager.getLogger(PSBaseListViewHelper.class);
    
    protected List<IPSListViewProcessor> postProcessors = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.ui.service.IPSListViewHelper#fillDisplayProperties(com
     * .percussion.ui.data.PSDisplayPropertiesCriteria)
     */
    public void fillDisplayProperties(PSDisplayPropertiesCriteria criteria)
    {
        Validate.notNull(criteria, "criteria cannot be null");

        if (criteria.getFormat() == null && criteria.isDisplayFormatRequired())
            return;

        Validate.notNull(criteria.getItems(), "criteria.items cannot be null");

        Map<String, String> displayProperties;

        for (PSPathItem pathItem : criteria.getItems())
        {
            // are null relatedObject supported by the subclass?
            if (getRelatedObject(pathItem) == null && !areEmptyRelatedObjectsSupported())
                continue;

            displayProperties = getDisplayProperties(pathItem);

            setDisplayProperties(pathItem, displayProperties);
        }
        
        if (postProcessors != null)
        {
            for (IPSListViewProcessor processor : postProcessors)
            {
                processor.process(criteria);
            }
                
        }
    }

    @Override
    public void setPostProcessors(List<IPSListViewProcessor> processors)
    {
        postProcessors = processors;
    }

    /**
     * Sets the display properties map given to the {@link PSPathItem}
     * parameter. It gets the current display properties of the
     * {@link PSPathItem} object and sets the new ones, skipping already present
     * fields (i.e., it doesn't overwrite already set fields).
     * 
     * @param pathItem A {@link PSPathItem} object where the new display
     *            properties will be set. Assumed not <code>null</code>.
     * @param displayProperties The new display properties map to set to the
     *            {@link PSPathItem} object.
     */
    private void setDisplayProperties(PSPathItem pathItem, Map<String, String> newDisplayProperties)
    {
        Map<String, String> currentDisplayProperties = pathItem.getDisplayProperties();

        if (currentDisplayProperties == null)
            currentDisplayProperties = new HashMap<>();

        for (Entry<String, String> entry : newDisplayProperties.entrySet())
        {
            // If the field is already present in the old display properties,
            // then it's not overwritten.
            if (currentDisplayProperties.containsKey(entry.getKey()))
                continue;

            currentDisplayProperties.put(entry.getKey(), entry.getValue());
        }

        pathItem.setDisplayProperties(currentDisplayProperties);
    }

    /**
     * Returns the relatedObject property of the {@link PSPathItem} parameter,
     * if and only if it's not null and it's type is the expected.
     * <code>null</code> otherwise.
     * 
     * @param pathItem
     * @return
     */
    protected Object getRelatedObject(PSPathItem pathItem)
    {
        if (pathItem.getRelatedObject() == null
                || !expectedRelatedObjectType().isAssignableFrom(pathItem.getRelatedObject().getClass()))
            return null;

        return pathItem.getRelatedObject();
    }

    /**
     * Returns a Map with the display format column values according to the
     * {@link PSPathItem} object given as parameter. The underlying
     * implementation should fill that Map with all the fields it's aware of.
     * For example, a File system based implementation may fill the name, the
     * file size and it's latest modified date, whereas a "CM1 objects"
     * implementation may fill the name, author, state, created date, etc.
     * <p>
     * Note that the underlying implementations MUST NOT modify the
     * {@link PSPathItem} object.
     * 
     * @param pathItem A PSPathItem object to get display properties from.
     * @return A Map<String, String> object with field name as keys and their
     *         corresponding values. All supported keys resides in the
     *         {@link IPSListViewHelper} interface, for example,
     *         {@link IPSListViewHelper#TITLE_NAME} is used for the name.
     */
    protected abstract Map<String, String> getDisplayProperties(PSPathItem pathItem);

    /**
     * Returns the expected type of {@link PSPathItem#getRelatedObject()}.
     * 
     * @return The expected type for {@link PSPathItem#getRelatedObject()}.
     */
    protected abstract Class<?> expectedRelatedObjectType();

    /**
     * Indicates whether null values for {@link PSPathItem#getRelatedObject()}
     * are supported or not.
     * 
     * @return <code>true</code> in the case null values for
     *         {@link PSPathItem#getRelatedObject()} are supported.
     *         <code>false</code> otherwise. If this method returns false then
     *         {@link PSPathItem} with null relatedObject properties are not
     *         passed in to the underlying implementation.
     */
    protected abstract boolean areEmptyRelatedObjectsSupported();
}
