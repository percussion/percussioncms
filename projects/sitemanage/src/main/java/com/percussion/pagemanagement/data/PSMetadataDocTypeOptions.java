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
package com.percussion.pagemanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains the structure of the doc type options for templates. The
 * object is composed of a option name and the value for it.
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "Options")
public class PSMetadataDocTypeOptions extends PSAbstractDataObject
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private String option;

    private String value;

    /**
     * Constructor
     */
    public PSMetadataDocTypeOptions()
    {
        super();
    }

    /**
     * @param option
     * @param value
     */
    public PSMetadataDocTypeOptions(String option, String value)
    {
        this.option = option;
        this.value = value;
    }

    /**
     * @return the option name
     */
    public String getOption()
    {
        return option;
    }

    /**
     * @param option the option name to set
     */
    public void setOption(String option)
    {
        this.option = option;
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
        this.value = value;
    }

}
