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
