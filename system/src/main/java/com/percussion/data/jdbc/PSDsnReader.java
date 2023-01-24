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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The PSDsnReader class will be used as an interface to the
 *    odbc data source names (DSNs) listed under unix platforms.
 *    The DSNs are not available through standard ODBC api calls as
 *    they are under Microsoft Windows, so we have to use a different
 *    mechanism.  Under unix we will use a standard Java file parser 
 *    to parse through the ODBC.INI.<P>
 *
 * The ODBC.INI file has the following format:
 * <DL>
 * <LI>[ODBC Data Sources]</LI>
 * <LI>DATA_SOURCE_NAME1=DATA_SOURCE_DRIVER_NAME1</LI>
 * <LI>DATA_SOURCE_NAME2=DATA_SOURCE_DRIVER_NAME2</LI>
 * <LI></LI>
 * <LI>[DATA_SOURCE_NAME1]</LI>
 * <LI>Driver=DATA_SOURCE_DRIVER_LOCATION</LI>
 * <LI>OPTION1=</LI>
 * <LI>OPTION2=</LI>
 * <LI>...</LI>
 * <LI></LI>
 * <LI>[DATA_SOURCE_NAME2]</LI>
 * <LI>Driver=DATA_SOURCE_DRIVER_LOCATION</LI>
 * <LI>OPTION1=</LI>
 * <LI>OPTION2=</LI>
 * <LI>...</LI>
 * <LI></LI>
 * <LI>[ODBC System Data Sources]</LI>
 * <LI>SYSTEM_DATA_SOURCE_NAME1=DATA_SOURCE_DRIVER_NAME3</LI>
 * <LI></LI>
 * <LI>[SYSTEM_DATA_SOURCE_NAME1]</LI>
 * <LI>Driver=DATA_SOURCE_DRIVER_LOCATION</LI>
 * <LI>OPTION1=</LI>
 * <LI>OPTION2=</LI>
 * <LI>...</LI>
 * </DL>
 */
public class PSDsnReader
{
   private static final Logger log = LogManager.getLogger(PSDsnReader.class);

   /**
    * Debugging/Testing main method.
    */
   public static void main(String[] args)
   {
      if (args.length != 1)
      {
         log.info("Usage: java com.percussion.data.jdbc.PSDsnReader <odbciniFileName>");
      }
      PSDsnReader reader = new PSDsnReader(args[0]);
      String[] dsnArray = reader.getDsnList();
      if (dsnArray == null)
         log.info("NULL");
      else
         for (int i = 0; i < dsnArray.length; i++)
            log.info(dsnArray[i]);
   }

   /**
    * Construct a PSDsnReader, and
    *    retrieve the ODBC ini location from the server.
    *
    * @param   iniFile  The fully qualified pathname to the odbc ini
    *                   file.  Can be <code>null</code>.
    */
   public PSDsnReader(String iniFile)
   {
      m_odbcIni = iniFile;
   }

   /**
    * Get the list of DSNs from the ODBC ini file
    *
    * @return The list of DSNs or <code>null</code> if they cannot
    *          be determined or none are defined.  An empty array
    *          will never be returned.
    */
   public String[] getDsnList()
   {
      if ((m_odbcIni == null) || (m_odbcIni.length()<1))
         return null;

      File inputOdbcIniFile = new File(m_odbcIni);
      if (!inputOdbcIniFile.canRead())
         return null;

      BufferedReader iniReader = null;

      try {
         iniReader = 
            new BufferedReader(new FileReader(inputOdbcIniFile));
      } catch (FileNotFoundException e)
      {
         // Do nothing here, this file should be available.
         return null;
      }


      /* We have a file we can read, so find the DSNs */
      ArrayList dsnList = new ArrayList();
      
      String line = "";

      boolean processingDsnArea = false;

      try {
         /* Process Line by line */
         while (line != null)
         {
            String nextDsn = null;

            line = iniReader.readLine();
            if (line != null)
            {
               line = line.trim();

               if (processingDsnArea) {
                  if ((line.length() == 0) || (line.charAt(0) == '['))
                     processingDsnArea = false;
                  else
                  {
                     /* If the line is formatted correctly, get the
                        dsn, otherwise ignore the line altogether */
                     int equalCharIndex = line.indexOf("=");
                     if (equalCharIndex >= 1)
                        nextDsn = line.substring(0, line.indexOf("="));
                  }
               } else {
                  processingDsnArea = (line.equalsIgnoreCase(DSN_BEGIN) ||
                                       line.equalsIgnoreCase(SYSDSN_BEGIN));
               }

               if (nextDsn != null)
                  dsnList.add(nextDsn);
            }
         }
      } catch (IOException e)
      {
         /* stop here  -- something has gone wrong, but return any
            DSNs we've located (don't return error) */      
      }

      if (dsnList.size() > 0)
      {
         String[] retArray = new String[dsnList.size()];

         for (int i = 0; i < dsnList.size(); i++)
            retArray[i] = (String) dsnList.get(i);

         return retArray;
      }

      return null;
   }

   /** The path and filename that locate the odbc ini file
    *    can be <code>null</code>.
    */
   private String m_odbcIni = null;

   /** 
    *    The tag we expect for odbc data sources in the DSN file.
    */
   private static final String DSN_BEGIN = "[ODBC Data Sources]";

   /** 
    *  Another possible tag for odbc data sources (system)
    *    in the DSN file.
    */
   private static final String SYSDSN_BEGIN = "[ODBC System Data Sources]";
}

