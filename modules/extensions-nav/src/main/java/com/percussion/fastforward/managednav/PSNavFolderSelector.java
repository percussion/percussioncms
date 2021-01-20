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
package com.percussion.fastforward.managednav;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

/**
 * A Rhythmyx extension that selects the folder based on a path. The extension
 * can be either a pre-exit or a post-exit. In either case the functionality is
 * identical. If the folder path name exists, the HTML parameter
 * <code>sys_folderid</code> will be appended to the parent request.
 * <p>
 * The folder path name is the only parameter.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavFolderSelector extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor,
         IPSUdfProcessor,
         IPSRequestPreProcessor
{
   /**
    * Default constructor.
    */
   public PSNavFolderSelector()
   {
      super();
      m_log = Logger.getLogger(getClass());
   }

   /**
    * This extension never modifies the stylesheet.
    * <p>
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Handles the request at pre-processor time. Used when this extension is
    * called as a pre-exit.
    * 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext req)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params.length > 0 && params[0] != null)
      {
         String pathname = params[0].toString().trim();
         if (pathname.length() > 0)
         {
            try
            {
               setSiteFolderByPath(pathname, req);
            }
            catch (Exception e)
            {
               m_log.error("Unexpected Exception", e);
               throw new PSExtensionProcessingException(0, e);
            }
         }

      }

   }

   /**
    * Handles the request after the result document has been generated. Used
    * when this extension is called as a post-exit.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      if (params.length > 0 && params[0] != null)
      {
         String pathname = params[0].toString().trim();
         if (pathname.length() > 0)
         {
            try
            {
               setSiteFolderByPath(pathname, req);
            }
            catch (Exception e)
            {
               m_log.error("Unexpected Exception", e);
               throw new PSExtensionProcessingException(0, e);
            }
         }

      }
      return resultDoc;
   }

   /**
    * Generates a value. Used when this extension is called as a UDF.
    * 
    * @see com.percussion.extension.IPSUdfProcessor#processUdf(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public Object processUdf(Object[] params, IPSRequestContext req)
         throws PSConversionException
   {
      if (params.length > 0 && params[0] != null)
      {
         String path = params[0].toString().trim();
         if (path.length() > 0)
         {
            try
            {
               return setSiteFolderByPath(path, req);
            }
            catch (Exception e)
            {
               m_log.error("Unexpected Exception", e);
               throw new PSConversionException(0, e);
            }
         }
      }
      return null;

   }

   /**
    * Sets the site folder id based on the path. This method actually does the
    * work of translating the folder path into an id.
    * 
    * @param path the folder path
    * @param req the parent request
    * @return the folder id or <code>null</code> if the path is invalid.
    * @throws PSNavException
    */
   private String setSiteFolderByPath(String path, IPSRequestContext req)
         throws PSNavException
   {
      try
      {
         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
         PSComponentSummary summary = relProxy.getSummaryByPath(PSFolder
               .getComponentType(PSFolder.class), path,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         if (summary != null)
         {
            String folderId = summary.getCurrentLocator().getPart(
                  PSLocator.KEY_ID);
            m_log.debug("Folder id is " + folderId);
            req.appendParameter("sys_folderid", folderId);
            return folderId;

         }
      }
      catch (Exception e)
      {
         m_log.error("Unexpected Exception", e);
         throw new PSNavException(e);
      }
      return null;

   }

   /**
    * Writes the log for debugging.
    */
   Logger m_log;
}