/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.utils.security;

import com.percussion.utils.io.PathUtils;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.derby.authentication.UserAuthenticator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The PSDerbyAuthenticator class provides the "user defined authentication" 
 * mechanism for Apache Derby RDBMS. It is called from the Derby Server
 * when authentication is attempted and the implemented methods of the 
 * UserAuthenticator interface provide the authentication. 
 * <p>
 * For more info see the Derby Developers Guide under "Derby and Security".
 *
 * TODO: Is this class actually used / necessary
 */

public class PSDerbyAuthenticator implements UserAuthenticator
{
   private static final Logger logger = LogManager.getLogger(PSDerbyAuthenticator.class);

   /**
    * Constructor
    */
   public PSDerbyAuthenticator() 
   {
      // nothing for now, but must be implemented for interface
   }

   /**
   * Authenticate the passed-in user's credentials.
   * 
   * @param userName The user's name, never <code>null</code>.
   * @param userPassword The user's password, never <code>null</code>.
   * @param databaseName The database, may be <code>null</code>. 
   * Currently ignored.
   * @param info jdbc connection info, may be <code>null</code>. 
   * Currently ignored.
   * @return true if user credentials authenticate, false otherwise.
   * @exception SQLException on failure
   */
   public boolean authenticateUser(String userName,String userPassword,
      String databaseName, Properties info) throws SQLException
   {
      FileInputStream cfgIn = null;

      if (StringUtils.isBlank(userName))
         return false;
      
      if (StringUtils.isBlank(userPassword))
         return false;
      
      // Build name of "derby.properties" file
      // -first,look for Rhythmyx root directory and add on Repository directory
      //  and Derby Property file name.
      // -if that doesn't work, look for Derby property "derby.system.home"
      //  which must have been set by the "-D" option when starting the JVM.
      // -next look for a "DERBY_HOME" environment variable.
      // -lastly, assume the current directory and hope there is a 
      //  "derby.properties" file there.
      String derbyHome = null;
      String derbyPropertiesFile = null;
      String rxRootDir = PathUtils.getRxDir().getAbsolutePath();
      String slash = System.getProperty("file.separator");
      
      if (!StringUtils.isBlank(rxRootDir) && !rxRootDir.equals("."))
      {
         derbyPropertiesFile = rxRootDir + slash + DERBY_REPOSITORY_DIR + 
                               slash + DERBY_PROPERTIES_FILE; 
      }
      else
      {
         derbyHome = System.getProperty("derby.system.home");
         if (derbyHome == null)
         {
            derbyHome = System.getenv("DERBY_HOME");
         }
         if (derbyHome == null)
         {
            derbyHome = "." ;
         }
         derbyPropertiesFile  = derbyHome + slash + DERBY_PROPERTIES_FILE;
      }
   
      try
      {
        // read "derby.properties" file into a property set
        Properties derbyProperties = new Properties();        
        cfgIn = new FileInputStream(derbyPropertiesFile);
        derbyProperties.load(cfgIn);
         
        // look for user in properties
        // - if its not there, return
        String encPw = 
            derbyProperties.getProperty(DERBY_USER_PROPERTY + userName);
        if (encPw == null)
            return false;
         
        // userId and password found, test the password (from property) 
        // -see if the password (argument), when encrypted, 
        //  matches the encrypted password stored on the system.
         String tmp = "";
         try {
            tmp = PSEncryptor.getInstance().encrypt(userPassword);
         } catch (PSEncryptionException e) {
            logger.error("Error encrypting password: " + e.getMessage(),e);
            tmp = encPw;
         }
         if (!tmp.equals(encPw))
        {
           return false;
        }  
      }
      catch (IOException e)
      {
         String embExpStr = "IOException: " + e.getLocalizedMessage();
         throw new SQLException( embExpStr );
      }
      finally
      {
         if (cfgIn != null)
         {
            try
            {
               cfgIn.close();
            }
            catch (IOException e)
            {
            }
         }
       }      
       return true;
   }



   /**
    * The directory (under Server Root) which holds Derby Repository.
    */
   private static final String DERBY_REPOSITORY_DIR = "Repository";

   /**
    * The name of the file holding derby properties 
    * (specifically, the username and password for authentication).
    */
   private static final String DERBY_PROPERTIES_FILE = "derby.properties";

   /**
    * The name of the derby property holding the username for authentication.
    */
   private static final String DERBY_USER_PROPERTY = "derby.user.";

}
