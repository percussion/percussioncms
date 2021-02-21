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
package com.percussion.cms;

import com.percussion.data.IPSInternalResultHandler;
import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSDisplayMapper;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.extension.PSExtensionException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSServer;
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This class leverages most of its behavior from the base class. It adds a
 * node to the output document that is only used while previewing documents.
 */
public class PSPreviewDocumentBuilder extends PSRowEditorDocumentBuilder
{
   /**
    * Processes the supplied editor definition, creating an efficient
    * representation of the object for runtime. Creates a 'preview' page
    * builder. A preview page includes the content fields which are marked as
    * <code>showInPreview</code>. The visibility rules will be used in the
    * same fashion as an editing document. It also displays child data in
    * read-only tables if they are marked for preview display.
    * <p>See the {@link PSModifyDocumentBuilder#PSModifyDocumentBuilder(
    * PSContentEditor, PSEditorDocumentContext, int, boolean) base} class
    * for a description of the params and exceptions.
    */
   public PSPreviewDocumentBuilder( PSContentEditor ce,
         PSEditorDocumentContext ctx, PSDisplayMapper dispMapper, int pageId,
         boolean isError )
      throws PSExtensionException, PSNotFoundException, PSSystemValidationException
   {
      super( ce, ctx, dispMapper, pageId, isError );
   }

   /**
    * See base for desc.
    *
    * @return Since we don't want any action buttons on a preview page, we
    *    always return an empty iterator.
    */
   protected Iterator getActionLinks( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params is null." );

      return PSIteratorUtils.emptyIterator();
   }


   // see base for desc
   boolean showField( PSField field )
   {
      return field.isShowInPreview();
   }


   // see base for desc
   protected Node createVariantList( Document doc, PSExecutionData data )
      throws PSDataExtractionException
   {
      if ( null == doc || null == data )
         throw new IllegalArgumentException( "One or more params were null." );

      PSExecutionData reqData = null;
      try
      {
         String reqName = IPSConstants.EDITOR_SUPPORT_APPNAME + "/" +
               VARIANTLIST_DATASET_NAME;
         IPSInternalResultHandler rh = (IPSInternalResultHandler)
               PSServer.getInternalRequestHandler( reqName );
         if ( null == rh )
         {
            throw new PSDataExtractionException(
                  IPSServerErrors.CE_NEEDED_APP_NOT_RUNNING, reqName );
         }
         reqData = rh.makeInternalRequest( data.getRequest());
         Document fragmentDoc = rh.getResultDoc( reqData );

         // could get back a null doc
         Element result = null;
         if (fragmentDoc != null)
         {
            PSXmlTreeWalker tree = new PSXmlTreeWalker(fragmentDoc);
            Element root = fragmentDoc.getDocumentElement();
            // could be an empty doc
            if (root != null)
            {
               // see if the VariantList element was returned
               tree.setCurrent(fragmentDoc.getDocumentElement());
               result = tree.getNextElement(VARIANTLIST_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            }
         }

         return result;
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
   }


   /**
    * The name of the dataset (not the request name, but the actual dataset
    * name), located in the <code>EDITOR_SUPPORT_APPNAME</code> application.
    * It is used to obtain the list of variants for the output doc. The root
    * node returned by this resource is just added to the output doc.
    */
   private static final String VARIANTLIST_DATASET_NAME = "variantlist";
}
