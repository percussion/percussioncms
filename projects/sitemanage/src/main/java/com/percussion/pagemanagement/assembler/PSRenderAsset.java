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
package com.percussion.pagemanagement.assembler;

import javax.jcr.Node;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.utils.guid.IPSGuid;

/**
 * 
 * A special version of asset used in rendering mode.
 * 
 * @author adamgent
 *
 */
public class PSRenderAsset extends PSAsset implements IPSLinkableItem
{
    
    private Node node;
    private String folderPath;
    private IPSGuid ownerId;
    
    public Node getNode()
    {
        return node;
    }

    public void setNode(Node node)
    {
        this.node = node;
    }    
    
    public String getFolderPath()
    {
        return folderPath;
    }

    public void setFolderPath(String folderPath)
    {
        this.folderPath = folderPath;
    }

    public IPSGuid getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(IPSGuid id)
    {
        ownerId = id;
    }
    
    private static final long serialVersionUID = 1L;

}
