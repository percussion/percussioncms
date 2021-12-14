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

package com.percussion.cloudservice.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CloudServiceInfo")
public class PSCloudServiceInfo
{
    protected String clientIdentity;
    protected String uiProvider;
    
    public PSCloudServiceInfo() { }
    
    public PSCloudServiceInfo(String clientIdentity, String uiProvider)
    {
        this.clientIdentity = clientIdentity;
        this.uiProvider = uiProvider;
    }
    
    public String getClientIdentity()
    {
        return clientIdentity;
    }
    
    public void setClientIdentity(String clientIdentity)
    {
        this.clientIdentity = clientIdentity;
    }
    
    public String getUiProvider()
    {
        return uiProvider;
    }
    
    public void setUiProvider(String uiProvider)
    {
        this.uiProvider = uiProvider;
    }
}
