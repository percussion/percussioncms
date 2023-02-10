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
