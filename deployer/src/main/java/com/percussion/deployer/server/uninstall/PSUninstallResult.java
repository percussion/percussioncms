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
package com.percussion.deployer.server.uninstall;

import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

/**
 * Implementation of {@link IPSUninstallResult}, see interface for details.
 */
public class PSUninstallResult implements IPSUninstallResult
{
   /**
    * 
    * @param pkgName
    * @param resultType
    */
   public PSUninstallResult(String pkgName, PSUninstallResultType resultType)
   {
      if (StringUtils.isBlank(pkgName))
      {
         throw new IllegalArgumentException("pkgName must not be empty");
      }
      if (resultType == null)
         throw new IllegalArgumentException("type must not be null");
      setPackageName(pkgName);
      setResultType(resultType);
   }

   public String getMessage()
   {
      return m_message;
   }

   public Exception getException()
   {
      return m_exception;
   }

   public PSUninstallResultType getResultType()
   {
      return m_resultType;
   }

   public IPSGuid getObjectGuid()
   {
      return m_objectGuid;
   }

   public String getObjectName()
   {
      return m_objectName;
   }

   public IPSGuid getPackageGuid()
   {
      return m_packageGuid;
   }

   public String getPackageName()
   {
      return m_packageName;
   }

   public void setException(Exception exception)
   {
      m_exception = exception;
   }

   public void setResultType(PSUninstallResultType errorType)
   {
      m_resultType = errorType;
   }

   public void setObjectGuid(IPSGuid objectGuid)
   {
      m_objectGuid = objectGuid;
   }

   public void setObjectName(String objectName)
   {
      m_objectName = objectName;
   }

   public void setPackageGuid(IPSGuid packageGuid)
   {
      m_packageGuid = packageGuid;
   }

   public void setPackageName(String packageName)
   {
      m_packageName = packageName;
   }

   public void setMessage(String message)
   {
      m_message = message;
   }

   private IPSGuid m_packageGuid;

   private IPSGuid m_objectGuid;

   private String m_packageName;

   private String m_objectName;

   private Exception m_exception;

   private PSUninstallResultType m_resultType;

   private String m_message;
}
