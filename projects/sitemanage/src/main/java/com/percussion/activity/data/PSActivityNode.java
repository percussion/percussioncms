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
package com.percussion.activity.data;

import java.util.Collections;
import java.util.List;

/**
 * Name/path container for activity which also includes the content types of the active items.
 */
public class PSActivityNode
{
    String siteName;
    String name;
    String path;
    List<String> contentTypes;
    
    public PSActivityNode(String siteName, String name, String path, List<String> contentTypes)
    {
        this.siteName = siteName;
        this.name = name;
        this.path = path;
        this.contentTypes = contentTypes;
    }
    
    public PSActivityNode(String siteName, String name, String path, String contentType)
    {
        this(siteName, name, path, Collections.singletonList(contentType));
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * @return the contentTypes
     */
    public List<String> getContentTypes()
    {
        return contentTypes;
    }

    /**
     * @param contentTypes the contentTypes to set
     */
    public void setContentTypes(List<String> contentTypes)
    {
        this.contentTypes = contentTypes;
    }

    /**
     * @return the siteName
     */
    public String getSiteName()
    {
        return siteName;
    }

    /**
     * @param siteName the siteName to set
     */
    public void setSiteName(String siteName)
    {
        this.siteName = siteName;
    }

}
