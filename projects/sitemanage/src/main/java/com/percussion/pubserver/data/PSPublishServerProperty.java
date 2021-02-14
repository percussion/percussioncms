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
package com.percussion.pubserver.data;


/**
 * @author ignacioerro
 *
 */
public class PSPublishServerProperty
{
    private static final long serialVersionUID = 1L;
    
    private String key;
    
    private String value;

    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value.trim();
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid()
    {
        return serialVersionUID;
    }
    
    @Override
    public int hashCode() {
    	final int prime = 31;
    	int result = 50;
    	result = prime * result + ((key == null) ? 0 : key.hashCode());
    	result = prime * result + ((value == null) ? 0 : value.hashCode());
    	return result;
    }

    @Override
    public boolean equals(Object obj) {
    	if (this == obj)
    		return true;
    	if (obj == null)
    		return false;
    	if (getClass() != obj.getClass())
    		return false;
    	PSPublishServerProperty other = (PSPublishServerProperty) obj;
    	if (key == null) {
    		if (other.key != null)
    			return false;
    	} else if (!key.equals(other.key))
    		return false;
    	if (value == null) {
    		if (other.value != null)
    			return false;
    	} else if (!value.equals(other.value))
    		return false;
    	return true;
    }
}
