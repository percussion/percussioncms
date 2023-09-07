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
package com.percussion.share.dao.impl;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ext.Provider;

/**
 * Implements {@link IPSIdMapper}.
 */
@Provider
@PSSiteManageBean("sys_idMapper")
public class PSIdMapper implements IPSIdMapper
{
   /**
    * Constructs an instance of the class.
    * 
    * @param guidMgr The GUID manager, never <code>null</code>.
    * @param contentDesignWs The content design webservice, never <code>null</code>.
    */
   @Autowired
   public PSIdMapper(IPSGuidManager guidMgr, IPSContentDesignWs contentDesignWs)
   {
       notNull(guidMgr);
       notNull(contentDesignWs);
      
      this.guidMgr = guidMgr;
      this.contentDesignWs = contentDesignWs;
   }
   
   public IPSGuid getGuid(String id)
   {
       notNull(id);
       notEmpty(id);
      
      return guidMgr.makeGuid(id);
   }

    public IPSGuid getGuid(String id, PSTypeEnum type)
    {
        notNull(id);
        notEmpty(id);

        return guidMgr.makeGuid(id,type);
    }

    /**
     * Converts a guid string into a valid guid for the specified type.
     *
     * @param id        String representation of a GUID
     * @param type      The type of guid to create
     * @param forceType When true, the specified type will be forced on the returned guid
     * @return A valid GUID
     */
    @Override
    public IPSGuid getGuid(String id, PSTypeEnum type, boolean forceType) {
        notNull(id);
        notEmpty(id);

        return guidMgr.makeGuid(id,type,forceType);
    }

    public int getContentId(IPSGuid guid)
   {
       notNull(guid);
       return ((PSLegacyGuid)guid).getContentId();
   }
   
   public int getContentId(String guid)
   {
       notEmpty(guid);
       return ((PSLegacyGuid)guidMgr.makeGuid(guid)).getContentId();
   }
   
   public IPSGuid getItemGuid(String id)
   {
       notEmpty(id);
       
       IPSGuid guid = guidMgr.makeGuid(id);
       return contentDesignWs.getItemGuid(guid);
   }
   
   
   /*
    * //see base interface method for details
    */
   public List<IPSGuid> getGuids(List<String> ids)
   {
       notNull(ids);
       
       List<IPSGuid> guids = new ArrayList<>();
       for (String id : ids)
       {
           guids.add(getGuid(id));
       }
       return guids;
   }
   
   public String getString(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id may not be null");
      
      return id.toString();
   }
   
   /*
    * //see base interface method for details
    */
   public List<String> getStrings(List<IPSGuid> ids)
   {
       notNull(ids);

       List<String> result = new ArrayList<>();
       for (IPSGuid id : ids)
       {
           result.add(getString(id));
       }
       return result;
   }
   
   public String getString(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");
      
      return getString(getGuid(locator));
   }
   
   public IPSGuid getGuid(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");
      
      return guidMgr.makeGuid(locator);
   }
   
   public PSLocator getLocator(String id) 
   {
       if (StringUtils.isBlank(id))
           throw new IllegalArgumentException("id may not be blank");
       
       IPSGuid guid = getGuid(id);
       
       return getLocator(guid); 
   }
   
   public PSLocator getLocator(IPSGuid id) 
   {
       if (id == null)
           throw new IllegalArgumentException("id may not be null");
       
       // get the correct revision
       IPSGuid guid = contentDesignWs.getItemGuid(id);
       
       return guidMgr.makeLocator(guid); 
   }
   
   public int getLocalContentId()
   {
       return guidMgr.createId(LOCAL_CONTENT_KEY);
   }

   /**
    * Constant for the key used to generate local content id's.
    */
   private static final String LOCAL_CONTENT_KEY = "PSX_LOCAL_CONTENT";
   
   /**
    * The GUID manager, initialized in constructor, never <code>null</code>
    * after that.
    */
   private IPSGuidManager guidMgr;
   
   /**
    * The content design webservice, initialized in constructor, never <code>null</code>
    * after that.
    */
   private IPSContentDesignWs contentDesignWs;

@Override
public IPSGuid getGuidFromContentId(long id) {
	return guidMgr.makeGuid(id, PSTypeEnum.LEGACY_CONTENT);
}
   
}
