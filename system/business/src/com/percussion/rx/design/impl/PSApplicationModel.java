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
package com.percussion.rx.design.impl;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSRevisionHistory;
import com.percussion.design.objectstore.server.IPSLockerId;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.server.PSRequest;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

public class PSApplicationModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException("load(IPSGuid) is not currently "
            + "implemented for design objects of type " + getType().name());
   }
   
   @Override
   public Object load(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(req);
      
      try 
      {
         PSRequest appReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         return PSServerXmlObjectStore.getInstance().getApplicationObject(
            name, appReq.getSecurityToken());
      }
      catch (Exception e) 
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      finally
      {
         PSDesignModelUtils.resetRequestToOriginal(req, origUser);
      }
   }
   
   @Override
   public Long getVersion(String name)
   {
      Long version = null;
      
      PSApplication app = (PSApplication) load(name);
      PSRevisionHistory history = app.getRevisionHistory();
      if (history != null)
      {
         int majVer = history.getLatestMajorVersion();
         int minVer = history.getLatestMinorVersion();
         String strVersion = String.valueOf(majVer) + String.valueOf(minVer);
         version = Long.valueOf(strVersion);
      }
      
      return version;
   }
   
   @Override
   public void delete(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");

      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(req);
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      boolean locked = false;
      IPSLockerId lockId = null;
      try
      {
         PSRequest appReq = (PSRequest) PSRequestInfo
               .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
         String userName = req.getUserSession()
               .getRealAuthenticatedUserEntry();
         lockId = new PSXmlObjectStoreLockerId(userName, false, req
               .getUserSessionId());
         os.getApplicationLock(lockId, name, 30);
         locked = true;
         os.deleteApplication(name, lockId, appReq.getSecurityToken());
      }
      catch (Exception e)
      {
         String msg = "Failed to get the design object for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      finally
      {
         // release the lock
         if (locked)
         {
            try
            {
               os.releaseApplicationLock(lockId, name);
            }
            catch (PSServerException e)
            {
               // ignore if failed to release the lock as we have already
               // deleted the app
            }
         }
         PSDesignModelUtils.resetRequestToOriginal(req, origUser);
      }
   }
}
