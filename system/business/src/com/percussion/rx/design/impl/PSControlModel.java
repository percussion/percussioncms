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
package com.percussion.rx.design.impl;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.server.IPSLockerId;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSCustomControlManager;
import com.percussion.server.PSRequest;
import com.percussion.util.IOTools;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PSControlModel extends PSLimitedDesignModel
{
   @Override
   public Object load(IPSGuid guid)
   {
      throw new UnsupportedOperationException("load(IPSGuid) is not currently "
            + "implemented for design objects of type " + getType().name());
   }
   
   @Override
   public Long getVersion(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      Long version = null;
      
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(req);
      
      try 
      {
         PSRequest appReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         PSSecurityToken tok = appReq.getSecurityToken();
              
         File ctrlFile = PSCustomControlManager.getInstance().getControlFile(
               name);
         if (ctrlFile != null)
         {
            version = getControlVersion(name, "rx_resources",
                  new File("stylesheets/controls", ctrlFile.getName()), tok);
         }
         
         if (version == null)
         {       
            version = getControlVersion(name, "sys_resources",
                  new File("stylesheets", "sys_Templates.xsl"), tok);
         }
      }
      catch (Exception e) 
      {
         throw new RuntimeException(e);
      }
      finally
      {
         PSDesignModelUtils.resetRequestToOriginal(req, origUser);
      }
      
      if (version == null)
      {
         String msg = "Failed to get the design object version for name {0}";
         Object[] args = { name };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
            
      return version;
   }
   
   @Override
   public void delete(String name)
   {
      if (StringUtils.isBlank(name))
         throw new IllegalArgumentException("name may not be null or empty");
      
      IPSLockerId lockId = null;
      PSRequest req = (PSRequest) PSRequestInfo.getRequestInfo(
            PSRequestInfo.KEY_PSREQUEST);
      String origUser = (String) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_USER);
      PSDesignModelUtils.setRequestToInternalUser(req);
      PSServerXmlObjectStore os = null;
      String appName = "rx_resources";
      
      try 
      {
         PSRequest appReq = (PSRequest) PSRequestInfo.getRequestInfo(
               PSRequestInfo.KEY_PSREQUEST);
         PSSecurityToken tok = appReq.getSecurityToken();
         lockId = new PSXmlObjectStoreLockerId("ControlModel", true, true,
               tok.getUserSessionId());
         
         os = PSServerXmlObjectStore.getInstance();
         os.getApplicationLock(lockId, appName, 30);
              
         File ctrlFile = PSCustomControlManager.getInstance().getControlFile(
               name);
         if (ctrlFile != null)
         {
            PSApplication app = new PSApplication(os.getApplicationDoc(
                  appName, tok));
            os.removeApplicationFile(app, ctrlFile, lockId, tok);
            
            // update control imports
            PSCustomControlManager.getInstance().writeImports();
         }
      }
      catch (Exception e) 
      {
         String msg = "Failed to delete the object with name ({0}).";
         Object[] margs = { name };
         throw new RuntimeException(MessageFormat.format(msg, margs), e);
      }
      finally
      {
         PSDesignModelUtils.resetRequestToOriginal(req, origUser);
         
         if (lockId != null)
         {
            try
            {
               if (os != null)
               {
                  os.releaseApplicationLock(lockId, appName);
               }
            }
            catch(PSServerException e)
            {
               // not fatal
            }
         }
      }
   }
   
   /**
    * Get the version (checksum) of the supplied control from a given
    * application.
    *  
    * @param name The control name, assumed not <code>null</code>.
    * @param appName The application name, assumed not <code>null</code>.
    * @param appFile The application file, assumed not <code>null</code>.
    * @param tok The security token required to access the application,
    * assumed not <code>null</code>.
    * @return The version of the control or null if not found.
    * 
    * @throws PSAuthorizationException
    * @throws PSNotFoundException
    * @throws PSServerException
    * @throws IOException
    * @throws SAXException
    * @throws PSUnknownNodeTypeException
    */
   private Long getControlVersion(String name, String appName, File appFile,
         PSSecurityToken tok) throws PSAuthorizationException,
         PSNotFoundException, PSServerException, IOException, SAXException,
         PSUnknownNodeTypeException
   {
      Long version = null;
            
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      Document doc = PSXmlDocumentBuilder.createXmlDocument(
            os.getApplicationFile(appName, appFile, tok), false);
      NodeList nodes = doc.getElementsByTagName(
            PSControlMeta.XML_NODE_NAME);
      for (int i = 0; i < nodes.getLength(); i++) 
      {
         Element control = (Element) nodes.item(i);
         PSControlMeta meta = new PSControlMeta(control);
         if (meta.getName().equals(name))
         {
            version = IOTools.getChecksum(PSXmlDocumentBuilder.toString(
                  control));
            break;
         }
      }
      
      return version;
   }
}
