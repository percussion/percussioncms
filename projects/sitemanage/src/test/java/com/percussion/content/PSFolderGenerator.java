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
