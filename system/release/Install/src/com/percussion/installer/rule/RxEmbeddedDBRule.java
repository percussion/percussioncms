/******************************************************************************
 *
 * [ RxEmbeddedDBRule.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.installer.rule;

import com.percussion.installanywhere.RxIARule;
import com.percussion.installer.RxVariables;
import com.percussion.installer.action.RxLogger;
import com.percussion.installer.model.RxProtocolModel;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.utils.jdbc.PSJdbcUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;


/**
 * This rule will evaluate to <code>true</code> if an embedded database is being used as repository.
 */
public class RxEmbeddedDBRule extends RxIARule
{
   @Override   
   protected boolean evaluate()
   {
      //we will get the database from the protocol panel.
      String protocol = RxProtocolModel.fetchDriver();
      if (StringUtils.isNotBlank(protocol))
      {
         return protocol.equalsIgnoreCase(PSJdbcUtils.DERBY);
      }
      
      String strRootDir = null;
      String strRepPropFile = null;
      Properties repProps = new Properties();
      String strDriver = null;
      InputStream repIn = null;
            
      try
      {
         strRootDir = getInstallValue(RxVariables.INSTALL_DIR);
         
         if (!(strRootDir.endsWith(File.separator)))
            strRootDir += File.separator;
         
         strRepPropFile = strRootDir + m_repositoryPropertyFile;
                  
         File repPropFile = new File(strRepPropFile);
         if (repPropFile.exists())
         {
            repIn = new FileInputStream(strRepPropFile);
            repProps.load(repIn);
            strDriver = repProps.getProperty(
                  PSJdbcDbmsDef.DB_DRIVER_NAME_PROPERTY, "");
         }
         if (!((strDriver == null) || (strDriver.trim().length() == 0)))
         {
            return strDriver.equalsIgnoreCase(PSJdbcUtils.DERBY);
         }
      }
      catch (Exception e)
      {
         RxLogger.logInfo("exception : " + e.getMessage());
         RxLogger.logInfo(e);
      }
      finally
      {
         if (repIn != null)
         {
            try
            {
               repIn.close();
            }
            catch (IOException e)
            {
            }
         }
      }
      
      return false;
   }
   
   /**************************************************************************
    * Bean property Accessors and Mutators
    **************************************************************************/
   
   /**
    * Returns the repository property file name.
    * @return the repository property file name, never <code>null</code>
    * or empty.
    */
   public String getRepositoryPropertyFile()
   {
      return m_repositoryPropertyFile;
   }
   
   /**
    * Sets the repository property file name.
    * @param repositoryPropertyFile the repository property file name,
    * may not be <code>null</code> or empty
    */
   public void setRepositoryPropertyFile(String repositoryPropertyFile)
   {
      if ((repositoryPropertyFile == null) ||
            (repositoryPropertyFile.trim().length() == 0))
         throw new IllegalArgumentException(
         "repositoryPropertyFile may not be null or empty");
      m_repositoryPropertyFile = repositoryPropertyFile;
   }
   
   /**************************************************************************
    * Bean properties
    **************************************************************************/
   
   /**
    * relative file path of the repository properties file, never
    * <code>null</code> or empty.
    */
   private String m_repositoryPropertyFile =
      "rxconfig/Installer/rxrepository.properties";
   
}

