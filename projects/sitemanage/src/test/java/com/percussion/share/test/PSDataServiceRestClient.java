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
