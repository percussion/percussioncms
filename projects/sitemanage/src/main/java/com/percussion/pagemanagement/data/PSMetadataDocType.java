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
package com.percussion.pagemanagement.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains the structure of the doc type information for templates. The object is
 * composed of a selected doc type, and a list of
 * {@link PSMetadataDocTypeOptions}.
 * 
 * @author leonardohildt
 * 
 */
@XmlRootElement(name = "DocType")
@JsonRootName("DocType")
public class PSMetadataDocType extends PSAbstractDataObject
{
    private static final long serialVersionUID = 1L;

    private String selected = "";

    private List<PSMetadataDocTypeOptions> options = new ArrayList<PSMetadataDocTypeOptions>();

    /**
     * Constructor for new doc type. The default doc type set is html5.
     * Used when a new template is added into the system.
     */
    public PSMetadataDocType()
    {
        super();
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(String selected)
    {
        this.selected = selected;
    }

    /**
     * @return the selected type
     */
    public String getSelected()
    {
        return selected;
    }

    /**
     * @return the doc type options, may be empty but never <code>null</code>
     */
    public List<PSMetadataDocTypeOptions> getOptions()
    {
        return this.options;
    }

    /**
     * @param options the doc type options to set, may be empty but never <code>null</code>
     */
    public void setOptions(List<PSMetadataDocTypeOptions> options)
    {
        this.options = options;
    }
}
