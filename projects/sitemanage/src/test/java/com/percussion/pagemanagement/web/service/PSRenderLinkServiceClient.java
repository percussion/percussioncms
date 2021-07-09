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
package com.percussion.pagemanagement.web.service;

import static org.apache.commons.lang.Validate.notEmpty;

import com.percussion.pagemanagement.data.PSInlineLinkRequest;
import com.percussion.pagemanagement.data.PSInlineRenderLink;
import com.percussion.share.test.PSObjectRestClient;

import java.io.InputStream;

public class PSRenderLinkServiceClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/pagemanagement/renderlink";
    {
        addAccept("text/html");
        addAccept("image/*");
    }

    public String getPath()
    {
        return path;
    }

    public PSInlineRenderLink getPreviewPageLink(String pageId) 
    {
        notEmpty(pageId,"pageId");
        return getObjectFromPath(concatPath(getPath(),"preview",pageId, "default"), PSInlineRenderLink.class);
    }
    
    public PSInlineRenderLink getPreviewLink(String itemId) 
    {
        notEmpty(itemId,"pageId");
        return getObjectFromPath(concatPath(getPath(),"preview",itemId, "default"), PSInlineRenderLink.class);
    }
    
    public PSInlineRenderLink getPreviewLink(String itemId, String resourceDefinitionId) 
    {
        notEmpty(itemId,"pageId");
        return getObjectFromPath(concatPath(getPath(),"preview", itemId, resourceDefinitionId), PSInlineRenderLink.class);
    }
    
    public PSInlineRenderLink previewPostLinkRequest(PSInlineLinkRequest renLink) 
    {
        return postObjectToPath(concatPath(getPath(),"preview"), renLink, 
                PSInlineRenderLink.class);
    }
    
    public InputStream followLink(String fullPath) {
        return GET_BINARY(fullPath);
    }

}
