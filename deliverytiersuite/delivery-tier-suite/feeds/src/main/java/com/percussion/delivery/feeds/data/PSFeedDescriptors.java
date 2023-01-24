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
