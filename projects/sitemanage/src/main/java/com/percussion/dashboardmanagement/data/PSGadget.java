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
package com.percussion.dashboardmanagement.data;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.FilenameUtils;

@XmlRootElement(name = "gadget")
@XmlAccessorType(XmlAccessType.FIELD)
public class PSGadget extends PSAbstractDataObject
{
    private Integer instanceId;

    private String url;

    private Integer col, row;

    @XmlTransient
    private boolean expanded = true;
    
    private Map<String, String> settings = new HashMap<>();

    public Integer getInstanceId()
    {
        return instanceId;
    }

    public void setInstanceId(Integer instanceId)
    {
        this.instanceId = instanceId;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public Integer getCol()
    {
        return col;
    }

    public void setCol(Integer col)
    {
        this.col = col;
    }

    public Integer getRow()
    {
        return row;
    }

    public void setRow(Integer row)
    {
        this.row = row;
    }

    public boolean isExpanded()
    {
        return expanded;
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }

    public Map<String, String> getSettings()
    {
        return settings;
    }

    public void setSettings(Map<String, String> settings)
    {
        this.settings = settings;
    }

    /* (non-Javadoc)
     * @see com.percussion.share.data.PSAbstractDataObject#toString()
     */
    @Override
    public String toString()
    {
        return String.format("File: %s - Column: %s - Row: %s - Settings: %s",
                FilenameUtils.getName(url), col, row, settings);
    }
}
