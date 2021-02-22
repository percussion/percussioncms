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
package com.percussion.licensemanagement.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.json.JSONObject;

import org.apache.commons.lang.Validate;

@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name = "", propOrder =
{"id", "methodName", "methodType", "url"})
@XmlRootElement(name = "method")
public class PSLicenseNetSuiteMethod 
{
    private String id; 
    
    private String methodName;
    
    private String methodType;

    private String url;
    
    public PSLicenseNetSuiteMethod()
    {
    }
    
    public PSLicenseNetSuiteMethod(JSONObject methodInfo)
    {
        Validate.notNull(methodInfo);
        
        id = methodInfo.getString("id");
        methodName = methodInfo.getString("methodName");
        methodType = methodInfo.getString("methodType");
        url = methodInfo.getString("url");
    }
    
    public String getId()
    {
        return String.valueOf(id);
    }

    public void setId(String id)
    {
        Validate.notEmpty(id);
        this.id = id;
    }

    public String getMethodName()
    {
        return methodName;
    }

    public void setMethodName(String methodName)
    {
        Validate.notEmpty(methodName);
        this.methodName = methodName;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        Validate.notEmpty(url);
        this.url = url;
    }

    public void setMethodType(String methodType)
    {
        Validate.notEmpty(methodType);
        this.methodType = methodType;        
    }

    public String getMethodType()
    {
        return methodType;
    }
}
