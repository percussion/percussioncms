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
