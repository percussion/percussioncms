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

/**
 * Interface for the uninstall results. Package name and type are required and
 * rest of the data is filled based on the stage at which the failure occurred.
 * 
 * @author bjoginipally
 * 
 */
public interface IPSUninstallResult
{
   /**
    * Returns the package guid, may be <code>null</code>, if the supplied
    * package name does have a corresponding guid.
    * 
    * @return IPSGuid of the package may be <code>null</code>.
    */
   public IPSGuid getPackageGuid();

   /**
    * Returns the name of the package.
    * 
    * @return name of the package, never <code>null</code>.
    */
   public String getPackageName();

   /**
    * Returns the type of the result.
    * 
    * @return type of the result never <code>null</code>.
    */
   public PSUninstallResultType getResultType();

   /**
    * Returns the message.
    * 
    * @return the message of the result, may be <code>null</code> or empty.
    */
   public String getMessage();

   /**
    * Exception associated with the result.
    * 
    * @return may be <code>null</code>. Incase of no exceptions.
    */
   public Exception getException();

   /**
    * Gets the guid of the object that caused the error.
    * 
    * @return IPSGuid object guid object guid may be <code>null</code>.
    */
   public IPSGuid getObjectGuid();

   /**
    * Gets the name of the object that caused the error.
    * 
    * @return String object name may be <code>null</code>.
    */
   public String getObjectName();

   /**
    * The uninstall message type enum.
    */
   public enum PSUninstallResultType
   {
      SUCCESS(1), INFO(2), WARN(3), ERROR(4);
      PSUninstallResultType(int type)
      {
         m_type = type;
      }
      public int getValue()
      {
         return m_type;
      }
      
      private int m_type;
   }

}
