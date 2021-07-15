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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.utils.container.jetty;

import com.percussion.utils.container.jboss.PSJBossJndiDatasource;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSEncryptor;
import com.percussion.utils.io.PathUtils;
import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.utils.xml.PSInvalidXmlException;

import java.util.Properties;

import org.w3c.dom.Element;

import static com.percussion.utils.container.IPSJdbcDbmsDefConstants.*;

/**
 * Represents a Jetty specific JNDI datasource file.
 * 
 * @author natechadwick
 *
 */
public class PSJettyJndiDatasource extends PSJBossJndiDatasource
{
   // additional fields for jetty only
   private String resourceName;

   private String connectionTestQuery;

   private boolean encrypted = false;

   public PSJettyJndiDatasource(Element source) throws PSInvalidXmlException
   {
      super(source);
   }

   public PSJettyJndiDatasource(Properties props)
   {
      this(props.getProperty(DB_NAME_PROPERTY), props
            .getProperty(DB_DRIVER_NAME_PROPERTY), props
            .getProperty(DB_DRIVER_CLASS_NAME_PROPERTY), props
            .getProperty(DB_SERVER_PROPERTY), props
            .getProperty(UID_PROPERTY), props.getProperty(PWD_PROPERTY));

      // add jetty only properties
      this.setName(props.getProperty(DB_RESOURCE_NAME));
      this.setConnectionTestQuery(props.getProperty(DB_CONNECTION_TEST_QUERY));

      String pwd = props.getProperty(PWD_PROPERTY);
      String encrypted = props.getProperty(PWD_ENCRYPTED_PROPERTY);
      if (encrypted != null && encrypted.equalsIgnoreCase("Y"))
      {
         this.encrypted = true;
         try{
            pwd = PSEncryptor.getInstance("AES",
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                            PSEncryptor.SECURE_DIR)
            ).decrypt(pwd);
         } catch (PSEncryptionException e) {
            pwd = PSLegacyEncrypter.getInstance(
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                    PSEncryptor.SECURE_DIR)
            ).decrypt(pwd, PSLegacyEncrypter.getInstance(
                    PathUtils.getRxPath().toAbsolutePath().toString().concat(
                            PSEncryptor.SECURE_DIR)
            ).getPartOneKey(),null);
         }

      }
      this.setPassword(pwd);

   }

   public PSJettyJndiDatasource(String name, String driverName, String driverClassName,
         String server, String userId, String password)
   {
      super(name, driverName, driverClassName, server, userId, password);

   }

   @Override
   public String getConnectionTestQuery()
   {
      return connectionTestQuery;
   }

   @Override
   public void setConnectionTestQuery(String connectionTestQuery)
   {
      this.connectionTestQuery = connectionTestQuery;
   }

   public boolean isEncrypted()
   {
      return encrypted;
   }

   public void setEncrypted(boolean encrypted)
   {
      this.encrypted = encrypted;
   }

}
