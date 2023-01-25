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

package com.percussion.ant.install;

import com.percussion.install.PSLogger;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * PSUpdateLoginConfigXML updates the application policy definitions in
 * rx/conf/login-config.xml for the jmx and web consoles.  It appends "props/"
 * to the users and roles properties file names for each of these policies'
 * login module options.
 *
 *<br>
 * Example Usage:
 *<br>
 *<pre>
 *
 * First set the taskdef:
 *
 *  <code>
 *  &lt;taskdef name="updateLoginConfigXML"
 *              class="com.percussion.ant.install.PSUpdateLoginConfigXML"
 *              classpathref="INSTALL.CLASSPATH"/&gt;
 *  </code>
 *
 * Now use the task to perform the updates.
 *
 *  <code>
 *  &lt;updateLoginConfigXML/&gt;
 *  </code>
 *
 * </pre>
 *
 */
public class PSUpdateLoginConfigXML extends PSAction
{
   // see base class
   @Override
   public void execute()
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      try
      {
         String loginConfigPath = getRootDir() + File.separator
         + PSConfigureDatasource.getLoginConfigLocation();
         File loginConfigFile = new File(loginConfigPath);
         in = new FileInputStream(loginConfigFile);

         Document doc = PSXmlDocumentBuilder.createXmlDocument(
               in, false);

         //Get the application policies
         NodeList nodeList = doc.getElementsByTagName(APP_POLICY);

         for (int i = 0; i < nodeList.getLength(); i++)
         {
            Element policy = (Element) nodeList.item(i);
            String policyName = policy.getAttribute(NAME_ATTR);

            //Look for the jmx and web console policies
            if (policyName.equals(JMX_CONSOLE) ||
                  policyName.equals(WEB_CONSOLE))
            {
               //Get the authentication nodes
               NodeList authNodes = policy.getElementsByTagName(AUTHENTICATION);

               for (int idx = 0; idx < authNodes.getLength(); idx++)
               {
                  Element authNode = (Element) authNodes.item(idx);

                  //Get the login modules
                  NodeList loginModNodes = authNode.getElementsByTagName(
                        LOGIN_MODULE);

                  for (int idx2 = 0; idx2 < loginModNodes.getLength(); idx2++)
                  {
                     Element loginModNode = (Element) loginModNodes.item(idx2);
                     String className = loginModNode.getAttribute(CODE_ATTR);

                     if (!className.equals(LOGIN_MODULE_CLASS))
                        continue;

                     //Get the module options
                     NodeList modOptNodes =
                        loginModNode.getElementsByTagName(MODULE_OPTION);

                     for (int idx3 = 0; idx3 < modOptNodes.getLength(); idx3++)
                     {
                        Element modOptNode = (Element) modOptNodes.item(idx3);
                        String modOptName = modOptNode.getAttribute(NAME_ATTR);

                        //Modify the roles and users properties file paths if
                        //necessary
                        if (modOptName.equals(ROLES_PROPS) ||
                              modOptName.equals(USERS_PROPS))
                        {
                           String modOptVal = modOptNode.getTextContent();
                           if (!modOptVal.startsWith(PATH_PREFIX))
                              modOptNode.setTextContent(
                                    PATH_PREFIX + modOptVal);
                        }
                     }
                  }
               }
            }
         }

         out = new FileOutputStream(loginConfigFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         PSLogger.logError("PSUpdateLoginConfigXML: " + e.getMessage());
      }
      finally
      {
         try
         {
            if (in != null)
               in.close();

            if (out != null)
               out.close();
         }
         catch (Exception e)
         {

         }

      }
   }

   private static final String JMX_CONSOLE = "jmx-console";
   private static final String WEB_CONSOLE = "web-console";
   private static final String APP_POLICY = "application-policy";
   private static final String AUTHENTICATION = "authentication";
   private static final String LOGIN_MODULE = "login-module";
   private static final String MODULE_OPTION = "module-option";
   private static final String NAME_ATTR = "name";
   private static final String CODE_ATTR = "code";
   private static final String LOGIN_MODULE_CLASS =
      "org.jboss.security.auth.spi.UsersRolesLoginModule";
   private static final String USERS_PROPS = "usersProperties";
   private static final String ROLES_PROPS = "rolesProperties";
   private static final String PATH_PREFIX = "props/";
}






