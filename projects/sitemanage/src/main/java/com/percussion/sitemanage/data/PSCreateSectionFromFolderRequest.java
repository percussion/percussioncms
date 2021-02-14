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
package com.percussion.sitemanage.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Request to create a section from a folder and page
 * 
 * @author JaySeletz *
 */
@XmlRootElement(name="CreateSectionFromFolderRequest")
@JsonRootName("PSCreateSectionFromFolderRequest")
public class PSCreateSectionFromFolderRequest extends PSAbstractDataObject
{
    public String getSourceFolderPath()
    {
        return sourceFolderPath;
    }
    
    public void setSourceFolderPath(String folderPath)
    {
        this.sourceFolderPath = folderPath;
    }
    
    public String getPageName()
    {
        return pageName;
    }
    
    public void setPageName(String landingPageName)
    {
        this.pageName = landingPageName;
    }
    
    public String getParentFolderPath()
    {
        return parentFolderPath;
    }
    
    public void setParentFolderPath(String parentFolderPath)
    {
        this.parentFolderPath = parentFolderPath;
    }
    private String sourceFolderPath;
    private String pageName;
    private String parentFolderPath;
}
