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
package com.percussion.data.jdbc;

import java.sql.Driver;

/**
 * A common abstract base class for all of our drivers.
 *
 * Adds some functionality for driver identification, but leaves
 * most of the responsibility for implementing the driver up to
 * the derived classes.
 */
public abstract class PSJdbcDriver implements Driver
{
   
   /** 
    * @author   chadloder
    * 
    * Constructor, available to subclasses only.
    * 
    * @param   driverDisplayName The human-readable name of the driver,
    * will be the value returned from getName.
    * 
    * @param   majorVer The more significant version number.
    * 
    * @param   minorVer The less significant version number.
    * 
    * @since 1.1 1999/5/7
    *
    */
   protected PSJdbcDriver(String driverDisplayName, int majorVer, int minorVer)
   {
      m_driverDisplayName = driverDisplayName;
      m_majorVersion = majorVer;
      m_minorVersion = minorVer;
   }

   /**
    * The driver's major version number.
    *
    * @return      the driver's major version number
    */
   public int getMajorVersion()
   {
      return m_majorVersion;
   }
   
   /**
    * The driver's minor version number.
    *
    * @return     the driver's minor version number
    */
   public int getMinorVersion()
   {
      return m_minorVersion;
   }

   /**
    * Get the descriptive name of this driver.
    *
    * @return     the descriptive name of this driver
    */
   public String getName()
   {
      return m_driverDisplayName;
   }

   public String getVersionString()
   {
      return "" + m_majorVersion + "." + m_minorVersion;
   }

   // the human readable display name of the driver
   private String m_driverDisplayName;
   private int m_majorVersion;
   private int m_minorVersion;
}
