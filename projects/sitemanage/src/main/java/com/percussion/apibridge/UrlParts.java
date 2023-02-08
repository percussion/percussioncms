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

package com.percussion.apibridge;

import org.apache.commons.lang.StringUtils;

class UrlParts
{
    String site = "";

    String path = "";

    String name = "";

    public String getSite()
    {
        return site;
    }

    public void setSite(String site)
    {
        this.site = site;
    }

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    UrlParts(String site, String path, String name)
    {
        this.site = site;
        this.path = path;
        this.name = name;
    }

    UrlParts(String fullUrl)
    {
    	if(site.equals(FolderAdaptor.ASSETS)){
	        String right = StringUtils.substringAfter(fullUrl, "/Assets/");
	        this.path = StringUtils.substringBefore(right, "/");
	        right = StringUtils.substringAfter(right, "/");
	        if (StringUtils.isNotEmpty(path))
	        {
	            if (StringUtils.contains(right, "/"))
	            {
	                this.name = StringUtils.substringAfterLast(right, "/");
	            }
	            else
	            {
	                this.path = "";
	                this.name = right;
	            }
	        }
    	}else{
            String right = StringUtils.substringAfter(fullUrl, "/Sites/");
	        this.site = StringUtils.substringBefore(right, "/");
	        right = StringUtils.substringAfter(right, "/");
	        if (StringUtils.isNotEmpty(site))
	        {
	            if (StringUtils.contains(right, "/"))
	            {
	                this.name = StringUtils.substringAfterLast(right, "/");
	                this.path = StringUtils.substringBeforeLast(right, "/");
	            }
	            else
	            {
	                this.path = "";
	                this.name = right;
	            }
	        }
    	}

    }

    public String getUrl()
    {
    	if(!site.equalsIgnoreCase(FolderAdaptor.ASSETS)){
	        StringBuilder sb = new StringBuilder("//Sites/");
	        sb.append(this.site);
	        if (StringUtils.isNotEmpty(this.path)) {
				sb.append("/").append(this.path);
			}
	        if (StringUtils.isNotEmpty(this.name)) {
				sb.append("/").append(this.name);
			}
	        return sb.toString();
    	}else{
    		StringBuilder sb = new StringBuilder("//Assets");
	        if (StringUtils.isNotEmpty(this.path)) {
				if(!this.path.startsWith("/"))
					sb.append("/");
				sb.append(this.path);
			}
	        if (StringUtils.isNotEmpty(this.name)) {
				if(!this.name.startsWith("/"))
					sb.append("/");
				sb.append(this.name);
			}
	        return sb.toString();
    	}
    }
}
