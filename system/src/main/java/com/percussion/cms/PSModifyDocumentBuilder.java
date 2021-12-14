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
package com.percussion.cms;

import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSServer;
import com.percussion.util.PSIteratorUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The output document is built up over a number of steps. Most of the work
 * is performed by the base class. The main purpose of this class is to create
 * the appropriate build step objects needed for document editor that modifies
 * an existing content item.
 */
abstract public class PSModifyDocumentBuilder extends PSEditorDocumentBuilder
{
   /**
    * Processes the supplied editor definition, creating an executable plan
    * that will be used when requests are made. Adds the system params
    * sys_contentid and sys_revision as hidden fields.
    * <p>See base class for a description of params and exceptions.
    *
    * @param isError For future use. Indicates this document was created based
    *    on a validation failure.
    */
   public PSModifyDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, int pageId, boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, pageId );
   }


   // see base for desc
   protected Node createWorkflowInfo( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params were null." );

      Element workflow = doc.createElement( WORKFLOW_NAME );

      Object o = getExtractor( CONTENT_ID_EXTRACTOR_KEY ).extract( data );
      String contentId = null;
      if ( null != o )
         contentId = o.toString();
      if ( null != contentId )
      {
         try
         {
            int id = Integer.parseInt( contentId );
         }
         catch ( NumberFormatException e )
         {
            String [] args =
            {
               contentId,
               "content id",
               e.getLocalizedMessage()
            };
            throw new PSDataExtractionException(
                  IPSServerErrors.CE_BAD_NUMBER_FORMAT, args );
         }
      }

      else
         return workflow;

      workflow.setAttribute( CONTENTID_NAME, contentId );

      // the BasicInfo child will be added by a workflow exit
      Iterator handlers = getWorkflowResources().iterator();
      PSExecutionData reqData = null;
      try
      {
         while ( handlers.hasNext())
         {
            IPSInternalResultHandler rh =
                  (IPSInternalResultHandler) handlers.next();
            reqData = rh.makeInternalRequest( data.getRequest());
            Document fragmentDoc = rh.getResultDoc( reqData );
            reqData.release();
            reqData = null;   // flag so we won't free again in finally block
            if ( null == fragmentDoc || null == fragmentDoc.getDocumentElement())
               continue;
            Node importNode = doc.importNode(
               fragmentDoc.getDocumentElement().getFirstChild(), true);
            workflow.appendChild(importNode);
         }
      }
      catch ( PSInternalRequestCallException e )
      {
         throw new PSDataExtractionException( e.getErrorCode(),
               e.getErrorArguments());
      }
      catch ( PSAuthorizationException e )
      {
         /* We'll consider this a configuration problem. These apps should
            never cause this exception to be thrown. */
         throw new PSDataExtractionException( e.getErrorCode(),
               e.getErrorArguments());
      }
      catch ( PSAuthenticationFailedException e )
      {
         /* We'll consider this a configuration problem. These apps should
            never cause this exception to be thrown. */
         throw new PSDataExtractionException( e.getErrorCode(),
               e.getErrorArguments());
      }
      finally
      {
         if ( null != reqData )
            reqData.release();
      }

      return workflow;
   }


   /**
    * Creates an internal request handler for each resource that needs to be
    * queried while creating the result document. They are stored in document
    * order.
    *
    * @return A list of 2 IPSInternalResultHandler objects, sequenced
    *    in the order needed to generate the document. Never <code>null</code>.
    *
    * @throws PSDataExtractionException If a needed resource cannot be found.
    */
   private List getWorkflowResources()
      throws PSDataExtractionException
   {
      String [] datasetNames =
      {
         HISTORY_DATASET_NAME,
         CONTENTSTATUS_DATATSET_NAME
      };

      List handlers = new ArrayList();

      Iterator names = PSIteratorUtils.iterator( datasetNames );
      while ( names.hasNext())
      {
         String reqName = IPSConstants.EDITOR_SUPPORT_APPNAME + "/"
               + (String) names.next();
         IPSInternalResultHandler rh = (IPSInternalResultHandler)
               PSServer.getInternalRequestHandler( reqName );
         if ( null == rh )
         {
            throw new PSDataExtractionException(
                  IPSServerErrors.CE_NEEDED_APP_NOT_RUNNING, reqName );
         }
         handlers.add( rh );
      }

      return handlers;
   }


   /**
    * Tag name for the Workflow element in the ContentEditor dtd. Never empty.
    */
   /** XML document element name. */
   public static final String WORKFLOW_NAME = "Workflow";
   private static final String CONTENTID_NAME = "contentId";

   /**
    * The name of the dataset (not the request name, but the actual dataset
    * name), located in the <code>EDITOR_SUPPORT_APPNAME</code> application.
    * It is used to obtain the history entries for the output doc.
    */
   private static final String HISTORY_DATASET_NAME = "history";

   /**
    * The name of the dataset (not the request name, but the actual dataset
    * name), located in the <code>EDITOR_SUPPORT_APPNAME</code> application.
    * It is used to obtain the content status info for the output doc.
    */
   private static final String CONTENTSTATUS_DATATSET_NAME = "contentstatus";
}



