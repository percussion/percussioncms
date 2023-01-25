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
