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
package com.percussion.ui.service.impl;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.ui.service.IPSListViewHelper;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/**
 * A file-system based implementation of {@link IPSListViewHelper}.
 * 
 * @author miltonpividori
 *
 */
@Component("fileSystemListViewHelper")
@Lazy
public class PSFileSystemListViewHelper extends PSBaseListViewHelper
{
    private IPSFileSystemService fileSystemService;

    public PSFileSystemListViewHelper(IPSFileSystemService fileSystemService)
    {
        this.fileSystemService = fileSystemService;
    }

    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#getDisplayProperties(com.percussion.pathmanagement.data.PSPathItem)
     */
    @Override
    protected Map<String, String> getDisplayProperties(PSPathItem pathItem)
    {
        Map<String, String> displayProperties = new HashMap<>();
        
        File relatedFile = (File) pathItem.getRelatedObject();
        
        displayProperties.put(TITLE_NAME, fileSystemService.getNameFromFile(relatedFile));
        
        if (!relatedFile.isDirectory())
        {
            displayProperties.put(SIZE, String.valueOf(relatedFile.length()));
        }

        displayProperties.put(CONTENT_LAST_MODIFIED_DATE_NAME,
                PSDateUtils.getDateToString(new Date(relatedFile.lastModified())));
        
        return displayProperties;
    }

    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#expectedRelatedObjectType()
     */
    @Override
    protected Class<?> expectedRelatedObjectType()
    {
        return File.class;
    }

    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#isEmptyRelatedObjectSupported()
     */
    @Override
    protected boolean areEmptyRelatedObjectsSupported()
    {
        return false;
    }
}
