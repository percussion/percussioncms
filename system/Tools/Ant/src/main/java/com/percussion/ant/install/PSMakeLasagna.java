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
package com.percussion.ant.install;

import com.percussion.server.PSServer;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSProperties;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * This task is used to encrypt the password found in the repository properties
 * file.
 * <br>
 * Example Usage:
 * <br>
 * <pre>
 * 
 * First set the taskdef:
 * 
 *  <code>  
 *  &lt;taskdef name="makeLasagna"
 *              class="com.percussion.ant.install.PSMakeLasagna"
 *              classpath="${INSTALL.CLASSPATH}"/&gt;
 *  </code>
 * 
 * Now use the task to encrypt the repository password:
 * 
 *  <code>  
 *  &lt;makeLasagna root="${install.dir}"/&gt;
 *  </code>
 * 
 * </pre>
 */
public class PSMakeLasagna extends Task
{
   
   /**
    * Sets the root directory.
    * 
    * @param root the installation directory, cannot be <code>null</code> or
    * empty.
    */
   public void setRoot(String root)
   {
      if (StringUtils.isBlank(root))
      {
         throw new IllegalArgumentException("root may not be null or empty");
      }
      
      m_root = root;
   }
   
   /**
    * Gets the root directory
    * 
    * @return the installation directory.
    */
   public String getRoot()
   {
      return m_root;
   }
   
   // see base class
   @Override
   public void execute() throws BuildException
   {
      FileOutputStream out = null;
      try
      {
         PSProperties props = new PSProperties(m_root + File.separator +
         "rxconfig/Installer/rxrepository.properties");
         String encryptedPWDProp = props.getProperty(
               PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY,"N");
         String pwd = props.getProperty(PSJdbcDbmsDef.PWD_PROPERTY);
         if (!StringUtils.isEmpty(pwd) && encryptedPWDProp.equalsIgnoreCase("Y"))
         {
            String decryptPwd = "";
             try{
                decryptPwd = PSEncryptor.getInstance().decrypt(pwd);
             }catch(PSEncryptionException | java.lang.IllegalArgumentException e){
                decryptPwd = PSLegacyEncrypter.getInstance().decrypt(pwd, PSServer.getPartOneKey());
             }

             if (decryptPwd.equals(pwd))
             {
                 encryptedPWDProp="N";
             }
         }
         if (encryptedPWDProp.equals("N"))
         {
            pwd = PSEncryptor.getInstance().encrypt(pwd);
            props.setProperty(PSJdbcDbmsDef.PWD_PROPERTY, pwd);
            props.setProperty(PSJdbcDbmsDef.PWD_ENCRYPTED_PROPERTY,"Y");
            out = new FileOutputStream(m_root + File.separator +
            "rxconfig/Installer/rxrepository.properties");
            props.store(out, null);
         }
      }
      catch (Exception e)
      {
         throw new BuildException(e.getMessage());
      }
      finally
      {
         if (out != null)
         {
            try
            {
               out.close();
            }
            catch (IOException e)
            {
               throw new BuildException(e.getMessage());
            }
         }
      }
   }
   
   /**
    * The root installation directory, should not be <code>null</code> or empty.
    */
   private String m_root;
   
}
