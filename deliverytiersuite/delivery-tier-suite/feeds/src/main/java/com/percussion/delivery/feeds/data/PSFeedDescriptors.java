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
package com.percussion.delivery.feeds.data;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

/**
 * A transfer class for feed descriptors and connection info.
 *
 */
public class PSFeedDescriptors
{
    @JsonDeserialize(as = ArrayList.class, contentAs = PSFeedDescriptor.class)
   private List<IPSFeedDescriptor> descriptors = new ArrayList<>();
   
   private String serviceUrl;
   private String serviceUser;
   private String servicePass;
   private boolean servicePassEncrypted;
   private String site;

   public PSFeedDescriptors(){
       super();
   }
/**
 * @return the descriptors
 */
public List<IPSFeedDescriptor> getDescriptors()
{
    return descriptors;
}

/**
 * @param descriptors the descriptors to set
 */
public void setDescriptors(List<IPSFeedDescriptor> descriptors)
{
    this.descriptors = descriptors;
}

/**
 * @return the serviceUrl
 */
public String getServiceUrl()
{
    return serviceUrl;
}

/**
 * @param serviceUrl the serviceUrl to set
 */
public void setServiceUrl(String serviceUrl)
{
    this.serviceUrl = serviceUrl;
}

/**
 * @return the serviceUser
 */
public String getServiceUser()
{
    return serviceUser;
}

/**
 * @param serviceUser the serviceUser to set
 */
public void setServiceUser(String serviceUser)
{
    this.serviceUser = serviceUser;
}

/**
 * @return the servicePass
 */
public String getServicePass()
{
    return servicePass;
}

/**
 * @param servicePass the servicePass to set
 */
public void setServicePass(String servicePass)
{
    this.servicePass = servicePass;
}

/**
 * @return the servicePassEncrypted
 */
public boolean isServicePassEncrypted()
{
    return servicePassEncrypted;
}

/**
 * @param servicePassEncrypted the servicePassEncrypted to set
 */
public void setServicePassEncrypted(boolean servicePassEncrypted)
{
    this.servicePassEncrypted = servicePassEncrypted;
}

/**
 * @return the site
 */
public String getSite()
{
    return site;
}

/**
 * @param site the site to set
 */
public void setSite(String site)
{
    this.site = site;
}

   
   
}
