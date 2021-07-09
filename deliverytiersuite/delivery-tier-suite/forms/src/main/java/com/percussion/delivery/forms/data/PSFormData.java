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

package com.percussion.delivery.forms.data;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.Validate.notNull;


/**
 * This object represents a form with its fields and data. It does not contain
 * any information about rendering. A form is immutable once constructed.
 * 
 * @author PaulHoward
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSFormData1")
@Table(name = "PERC_FORMS")
public class PSFormData implements IPSFormData
{
    /**
     * The form's db id
     */
    @SuppressWarnings("unused")
    @TableGenerator(
        name="formId", 
        table="PERC_ID_GEN", 
        pkColumnName="GEN_KEY", 
        valueColumnName="GEN_VALUE", 
        pkColumnValue="formId", 
        allocationSize=1)
    @Id
    @GeneratedValue(strategy=GenerationType.TABLE, generator="formId")
    @Column(name = "ID")
    private long id;

    /**
     * The hibernate object version
     */
    @SuppressWarnings("unused")
    @Version
    private Integer version;

    @Basic
    private String name;

    @Basic
    private Date created;
    
    @SuppressWarnings("unused")
    @Column(name = "EXPORTED") 
    private char isExported = 'n';

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "PERC_FORM_FIELDS", joinColumns = @JoinColumn(name = "PARENT_FORM_ID",
            referencedColumnName = "ID"))
    @MapKeyColumn(name = "FIELD_NAME")
    @Column(name = "VALUE", length = 2048)
    private Map<String, String> properties = new HashMap<>();

    /**
     * 
     * @param name The name of the form. Never <code>null</code> or empty. Max length is 50 chars.
     * 
     * @param props The fields of the form with their values. The key is the
     * name of the field, case-sensitive. If the value is a
     * <code>String[]</code>, then all the entries in the array are merged
     * into a single string, using the {@link #FIELD_VALUES_SEPARATOR} as a
     * separator. If the entry contains separators, the caller is responsible for escaping them.
     */
    public PSFormData(String name, Map<String, String[]> props)
    {
        notNull(name);
        notNull(props);

        this.name = name;
        created = new Date();
        for (String key : props.keySet())
        {
            String value;
            String[] val = props.get(key);
            if (val == null)
                value = StringUtils.EMPTY;
            else if (val.length == 1)
                value = (String)escapeForJoin(val[0]);
            else
                value = convertArrayToString(val);
            this.properties.put(key, value);
        }
    }

    /**
     * 
     * @param val Assumed not <code>null</code>.
     * @return Never <code>null</code>.
     */
    private String convertArrayToString(String[] entries)
    {
        StringBuffer result = new StringBuffer();
        for (String s : entries)
        {
            if (result.length() > 0)
                result.append(FIELD_VALUES_SEPARATOR);
            if (s != null)
                result.append(escapeForJoin(s));
        }
        return result.toString();
    }

    /**
     * Escapes any {@link #FIELD_VALUES_SEPARATOR} by inserting the {@link #FIELD_VALUES_SEPARATOR_ESCAPE} char before it.
     * Any instances of the escape char are escaped in the same way with the same escape char.
     * 
     * @param s Assumed not <code>null</code>;
     * @return Never <code>null</code>.
     */
    private Object escapeForJoin(String s)
    {
        if (s.indexOf(FIELD_VALUES_SEPARATOR_ESCAPE) >= 0)
            s = s.replace(FIELD_VALUES_SEPARATOR_ESCAPE, FIELD_VALUES_SEPARATOR_ESCAPE + FIELD_VALUES_SEPARATOR_ESCAPE);
        if (s.indexOf(FIELD_VALUES_SEPARATOR) >= 0)
            s = s.replace(FIELD_VALUES_SEPARATOR, FIELD_VALUES_SEPARATOR_ESCAPE + FIELD_VALUES_SEPARATOR);
        return s;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getName()
	 */
    public String getName()
    {
        return name;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getCreateDate()
	 */
    public Date getCreateDate()
    {
        return created;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#isExported()
	 */
    public char isExported()
    {
        return isExported;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getCreated()
	 */
    public Date getCreated()
    {
        return created;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getFieldNames()
	 */
    public Set<String> getFieldNames()
    {
        Set<String> result = Collections.unmodifiableSet(properties.keySet());
        if (result == null)
            result = Collections.emptySet();
        return result;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getFields()
	 */
    public Map<String, String> getFields()
    {
        return Collections.unmodifiableMap(properties);
    }
    
    protected PSFormData()
    {}

    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#getId()
	 */
    public String getId()
    {
        return String.valueOf(id);
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.forms.data.IPSFormData#setId(long)
	 */
    public void setId(String id)
    {
        if(id == null)
        {
        	this.id = 0;
        }
        else
        {
        	this.id = Long.valueOf(id);
        }
    	
    }
}
