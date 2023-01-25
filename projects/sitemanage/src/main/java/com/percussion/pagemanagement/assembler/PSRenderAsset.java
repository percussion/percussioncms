/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
