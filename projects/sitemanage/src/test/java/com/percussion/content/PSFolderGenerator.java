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
package com.percussion.content;

import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.web.service.PSPathServiceRestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Given a path of the form /Sites/... or /Assets/... (the internal form of cm1
 * paths), creates all folders that are not already present.
 * 
 * @author paulhoward
 */
public class PSFolderGenerator extends PSGenerator<PSPathServiceRestClient>
{
    
    public PSFolderGenerator(String baseUrl, String uid, String pw)
    {
        super(PSPathServiceRestClient.class, baseUrl, uid, pw);
    }    
    
    /**
	 * Creates all parts of the supplied path if they don't exist.
	 * 
	 * @param path Assumed not <code>null</code>. Of the form /Assets/... or /Sites/...
	 * @return The descriptor for the last path part, whether created or
	 *         existing.
	 *         
	 * @throws RuntimeException If a part of the path already exists as an item.
	 */
    public PSPathItem createFolderPath(String path)
    {
        path = path.trim();
        if (path.startsWith("/"))
            path = path.substring(1);
        StringTokenizer toker = new StringTokenizer(path, "/");
        int parts = toker.countTokens();
        StringBuilder partialPath = new StringBuilder(100);
        PSPathItem result = null;
        for (int i = 0; i < parts; i++)
        {
            partialPath.append("/");
            partialPath.append(toker.nextToken());
            String curPath = partialPath.toString();
            try {
                result = getRestClient().find(curPath);
                if (result.isLeaf())
                {
                	//change name?
                	throw new RuntimeException("Part of path already exists as item: " + result.getPath());
                }
                log.info("Folder '" + curPath + "' already exists.");
            }
            catch (Exception e) {
                result = getRestClient().addFolder(curPath);
                log.info("Created folder '" + curPath + "'");
            }
        }
        return result;
    }

    public Collection<PSPathItem> getFolderPaths(String path)
    {
        Collection<PSPathItem> results = new ArrayList<PSPathItem>();
        results.add(getRestClient().find(path));
        results.addAll(getChildFolderPaths(path));
        return results;
    }

    /**
     * Used for recursive processing of folder paths.
     * @param path
     * @return
     */
    private Collection<PSPathItem> getChildFolderPaths(String path)
    {
        Collection<PSPathItem> results = new ArrayList<PSPathItem>();
        List<PSPathItem> children = getRestClient().findChildren(path);
        for (PSPathItem itemPath : children)
        {
            if (itemPath.isFolder())
            {
                results.add(itemPath);
                results.addAll(getChildFolderPaths(itemPath.getPath()));
            }
        }
        return results;
    }
}
