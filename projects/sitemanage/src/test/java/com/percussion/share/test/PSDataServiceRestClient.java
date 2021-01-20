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
/**
 * 
 */
package com.percussion.share.test;

import static org.apache.commons.lang.Validate.*;

import java.util.List;

public class PSDataServiceRestClient<T> extends PSObjectRestClient
{
    
    private Class<T> type;

    private String path;

    public PSDataServiceRestClient(Class<T> type, String baseUrl, String path)
    {
        
        super(baseUrl);
        this.type = type;
        this.path = path;
    }

    public T save(T data)
    {
        notNull(data);
        return postObjectToPath(getSavePath(), data, this.type);
    }

    public void delete(String id)
    {
        notNull(id);
        DELETE(getDeletePath(id));
    }

    public T get(String id)
    {
        notNull(id);
        return getObjectFromPath(getGetPath(id));
    }

    protected T getObjectFromPath(String path)
    {
        notNull(path);
        return getObjectFromPath(path, this.type);
    }
    
    public List<T> getAll()
    {
        return getObjectsFromPath(getAllPath());
    }
    
    protected List<T> getObjectsFromPath(String path) {
        return getObjectsFromPath(path, this.type);
    }

    protected String getDeletePath(String id)
    {
        return getPath() + id;
    }

    protected String getGetPath(String id)
    {
        return getPath() + id;
    }

    protected String getAllPath()
    {
        return getPath();
    }

    protected String getSavePath()
    {
        return getPath();
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

}