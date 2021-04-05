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
package com.percussion.delivery.metadata.impl;



import java.util.Properties;

import com.percussion.delivery.metadata.IPSMetadataProperty.VALUETYPE;

/**
 * This class holds property to datatype mappings used to figure out which
 * datatype to store the property value as. It is intended to be instantiated
 * and injected via Spring. The mappings are defined in the beans.xml file.
 * 
 * @author erikserating
 * 
 */
public class PSPropertyDatatypeMappings
{
    protected Properties datatypeMappings;

    public PSPropertyDatatypeMappings()
    {

    }

    /**
     * @return the mappings
     */
    public Properties getDatatypeMappings()
    {
        return datatypeMappings;
    }

    /**
     * @param mappings the mappings to set
     */
    public void setDatatypeMappings(Properties mappings)
    {
        this.datatypeMappings = mappings;
    }

    /**
     * Given a datatype (key), it returns a VALUETYPE object according to it.
     * For example, "STRING" will return the VALUETYPE.STRING value.
     * 
     * @param key Datatype key (STRING, DATE, NUMBER, etc).
     * @return VALUETYPE va
     */
    public VALUETYPE getDatatype(String key)
    {
        if (datatypeMappings == null)
            throw new IllegalStateException(
                    "datatypeMappings is null, this class must be instantiated via Spring for data to be filled in.");

        String stringProp = datatypeMappings.getProperty(key);

        VALUETYPE prop = VALUETYPE.STRING;
        
        if (stringProp == null)
            return prop;
        
        try
        {
            prop = VALUETYPE.valueOf(stringProp);
        }
        catch (Exception ex)
        {
            
        }

        return prop;
    }
}
