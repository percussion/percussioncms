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
    	if(!site.equals(FolderAdaptor.ASSETS)){
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
    		StringBuilder sb = new StringBuilder("//Assets/");
	        if (StringUtils.isNotEmpty(this.path)) {
				sb.append("/").append(this.path);
			}
	        if (StringUtils.isNotEmpty(this.name)) {
				sb.append("/").append(this.name);
			}
	        return sb.toString();
    	}
    }
}