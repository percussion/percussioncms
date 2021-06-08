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
package com.percussion.inlinelinkconverter;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.IPSRemoteRequester;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class PSInlineLinkClearAttribs extends PSInlineLinkConverter
{

   private static final Logger log = LogManager.getLogger(PSInlineLinkClearAttribs.class);

   public PSInlineLinkClearAttribs(Properties props, Document xslDoc)
   {
      super(props, xslDoc);
   }

   @Override
   protected List getContentIds(String contentType) throws PSCmsException
   {
      String siteRoot = m_props.getProperty("siteRoot");
      if(siteRoot == null || siteRoot.trim().length()==0)
         return super.getContentIds(contentType);
      List<ContentKey> resultList = new ArrayList<ContentKey>();
      // Filter the contentids that are not in the site folder
      try
      {
         PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();
         String folderType = PSDbComponent.getComponentType(PSFolder.class);
         List cidList = super.getContentIds(contentType);
         for (Object object : cidList)
         {
            ContentKey ck = (ContentKey) object;
            String cid = ck.getContentId();
            String rev = ck.getRevision();
            PSLocator loc = new PSLocator(Integer.parseInt(cid), Integer
                  .parseInt(rev));
            
            // Testing getRelationshipOwnerPaths()
            String[] paths = relProxy.getRelationshipOwnerPaths(folderType,
                  loc, PSRelationshipConfig.TYPE_FOLDER_CONTENT);
            for (String path : paths)
            {
               if (path.startsWith(siteRoot))
               {
                  resultList.add(ck);
                  break;
               }
            }
         }
      }
      catch (Exception e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
      return resultList;
   }

   /**
    * Get relationship processor proxy for remote processor.
    * 
    * @return The remote proxy, never <code>null</code>.
    * @throws Exception
    */
   private PSRelationshipProcessorProxy getRemoteRelationshipProxy()
         throws Exception
   {
      IPSRemoteRequester requester = m_rtAgent.getRemoteRequester();

      PSRelationshipProcessorProxy proxy = new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE, requester,
            PSDbComponent.getComponentType(PSFolder.class));

      return proxy;
   }

   public static void main(String[] args)
   {

      // no args required
      if (args.length > 0)
      {
         printUsage();
         System.exit(-1);
      }

      // load the properties file
      FileInputStream in = null;
      Properties props = new Properties();
      try
      {
         in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
         props.load(in);
      }
      catch (FileNotFoundException e)
      {
         System.out
               .println("Unable to locate file: " + DEFAULT_PROPERTIES_FILE);
         printUsage();
         System.exit(-1);
      }
      catch (IOException e)
      {
         System.out.println("Error loading properties from file ("
               + DEFAULT_PROPERTIES_FILE + "): " + e.toString());
         printUsage();
         System.exit(-1);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (Exception e)
            { /* ignore */
            }
         }
      }

      FileInputStream cvXSL = null;
      Document xslDoc = null;
      try
      {
         cvXSL = new FileInputStream(INLINE_LINK_CLEARATTRIBS_XSL);
         xslDoc = PSXmlDocumentBuilder.createXmlDocument(cvXSL, false);
      }
      catch (FileNotFoundException e)
      {
         System.out.println("Unable to locate file: "
               + INLINE_LINK_CLEARATTRIBS_XSL);
         printUsage();
         System.exit(-1);
      }
      catch (IOException e)
      {
         System.out.println("Error loading xsl file ("
               + INLINE_LINK_CLEARATTRIBS_XSL + "): " + e.toString());
         printUsage();
         System.exit(-1);
      }
      catch (SAXException e)
      {
         System.out.println("Error parsing xsl file ("
               + INLINE_LINK_CLEARATTRIBS_XSL + "): " + e.toString());
         printUsage();
         System.exit(-1);
      }
      finally
      {
         if (cvXSL != null)
         {
            try
            {
               cvXSL.close();
            }
            catch (Exception e)
            { /* ignore */
            }
         }
      }

      try
      {
         // start the conversion process
         PSInlineLinkConverter ilc = new PSInlineLinkClearAttribs(props, xslDoc);
         ilc.doConvert();
      }
      catch (Exception e)
      {
         System.out.println("Error - caught unknown exception: "
               + e.getMessage());
         System.exit(-1);
      }

      System.exit(0);
   }

   private static final String DEFAULT_PROPERTIES_FILE = 
      "InlineLinkClearAttribs.properties";

   private static final String INLINE_LINK_CLEARATTRIBS_XSL = 
      "InlineLinkClearAttribs.xsl";
}
