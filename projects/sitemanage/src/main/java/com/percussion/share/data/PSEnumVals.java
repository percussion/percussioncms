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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang.Validate.notEmpty;

/**
 * Encapsulates enumerated values (value/display value pairs).
 * 
 * @author peterfrontiero
 */
@JsonRootName(value = "EnumVals")
public class PSEnumVals
{
    @NotNull
    private List<EnumVal> entries = new ArrayList<>();
   
    /**
     * @return list of entry objects, never <code>null</code>, may be empty.
     */
    public List<EnumVal> getEntries()
    {
        return entries;
    }

    public void setEntries(List<EnumVal> entries)
    {
        this.entries = entries;
    }

    /**
     * Adds an entry for the specified value/display value pair.
     * 
     * @param value never <code>null</code> or empty.
     * @param displayValue may be <code>null</code>.
     */
    public void addEntry(String value, String displayValue)
    {
        notEmpty(value);
        
        EnumVal val = new EnumVal();
        val.setValue(value);
        val.setDisplayValue(displayValue);
        entries.add(val);
    }
    
    /* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PSEnumVals)) {
			return false;
		}
		PSEnumVals other = (PSEnumVals) obj;
		if (entries == null) {
			if (other.entries != null) {
				return false;
			}
		} else if (!entries.equals(other.entries)) {
			return false;
		}
		return true;
	}
    
    public boolean hasSameValues(PSEnumVals other)
    {
        if (other == null)
            return false;
        
        if(entries.size() != other.entries.size())
        {
            return false;
        }
        
        for(EnumVal val : entries)
        {
            if(!other.hasValue(val.getValue()))
            {
                return false;
            }
        }
        
        return true;        
    }
    
    public boolean hasValue(String val)
    {
        boolean hasValue = false;
        
        for (EnumVal test : entries)
        {
            if (test.getValue().equals(val))
            {
                hasValue = true;
                break;
            }            
        }
        
        return hasValue;
    }
    
    private static final long serialVersionUID = 1496690238764003673L;

    /**
     * Encapsulates an enumerated value, which consists of a value and display value (label).
     * 
     * @author peterfrontiero
     */
    public static class EnumVal
    {
        @NotNull
        private String value;
        
        private String displayValue;
        
        public String getValue()
        {
            return value;
        }
        
        public void setValue(String value)
        {
            this.value = value;
        }
        
        public String getDisplayValue()
        {
            return displayValue;
        }
        
        public void setDisplayValue(String displayValue)
        {
            this.displayValue = displayValue;
        }
        
        //FB: HE_EQUALS_USE_HASHCODE NC 1-16-16
        
        /* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((displayValue == null) ? 0 : displayValue.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof EnumVal)) {
				return false;
			}
			EnumVal other = (EnumVal) obj;
			if (displayValue == null) {
				if (other.displayValue != null) {
					return false;
				}
			} else if (!displayValue.equals(other.displayValue)) {
				return false;
			}
			if (value == null) {
				if (other.value != null) {
					return false;
				}
			} else if (!value.equals(other.value)) {
				return false;
			}
			return true;
		}
    }
    
}
