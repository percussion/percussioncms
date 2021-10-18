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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.PSInlineLinkField;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSField;
import com.percussion.design.objectstore.PSFieldSet;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSWebServicesRequestHandler;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSNodePrinter;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This processor fixes up inline links for newly created or updated 
 * relationships.
 */
public class PSInlineLinkProcessor
{
   private static final Logger logger = LogManager.getLogger(IPSConstants.ASSEMBLY_LOG);
   /**
    * Force users to use the static methods.
    */
   private PSInlineLinkProcessor()
   {
   }
   
   /**
    * Convenience method that calls {@link #processInlineLinkItem(PSRequest, 
    * PSLocator, Map, int) processInlineLinkItem(request, item, 
    * relationships, request.getSecurityToken().getCommunityId())}.
    */
   public static void processInlineLinkItem(PSRequest request,
      PSLocator item, Map relationships) throws PSException
   {
      //convenience methods don't need to validate contract
      processInlineLinkItem(request, item, relationships, 
         request.getSecurityToken().getCommunityId());
   }
      
   /**
    * Convenience method that calls {@link #processInlineLinkItem(PSRequest, 
    * PSLocator, Map, int, boolean) processInlineLinkItem(request, item, 
    * relationships, communityId, <code>true</code>)}.
    */
   public static void processInlineLinkItem(PSRequest request,
      PSLocator item, Map relationships, int communityId) throws PSException
   {
      //convenience methods don't need to validate contract
      processInlineLinkItem(request, item, relationships, communityId, true,
            true);
   }
   
   /**
    * Processes the supplied items inline links.
    * 
    * @param request the request for this process, never <code>null</code>.
    * @param item the locator of the item to be processed, never 
    *   <code>null</code>.
    * @param relationships a map that maps the old relationship id as 
    *    <code>Integer</code> to the new relationship as 
    *    <code>PSRelationship</code>), not <code>null</code>, may be empty.
    * @param communityId the community id for which to process the item, -1
    *    to allow all communities.
    * @param bCheckout this flag specifies whether to check-out the item before
    *    processing inline links.
    * @param checkin this flag specifies whether to check-in the item after
    *    the inline links were processed.
    * @throws PSException for any error.
    */
   public static void processInlineLinkItem(PSRequest request,
      PSLocator item, Map relationships, int communityId, boolean bCheckout, 
      boolean checkin) 
      throws PSException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (item == null)
         throw new IllegalArgumentException("item cannot be null");
      
      if (relationships == null)
         throw new IllegalArgumentException("relationships cannot be null");
      
      if (relationships.size() == 0)
         return;

      PSItemDefinition itemDef = PSItemDefManager.getInstance().getItemDef(
         item, -1);

      // do nothing if there is no inline link fields
      List inlinelinkFields = getInlineLinkFields(itemDef);
      if (inlinelinkFields.size() == 0)
         return;
      logger.debug("Processing inline Link for : {} ", item.getId() );
      processInlineLinkItem(request, item, itemDef, relationships,
         inlinelinkFields.iterator(), communityId, bCheckout, checkin);
   }

   /**
    * Get a list of inline link fields from the given item definition.
    *
    * @param itemDef the item definition, assumed not <code>null</code>.
    * @return a list over zero or more <code>PSInlineLinkField</code> objects,
    *    never <code>null</code>, may be empty.
    */
   private static List getInlineLinkFields(PSItemDefinition itemDef)
   {
      PSContentEditorPipe pipe = 
         (PSContentEditorPipe) itemDef.getContentEditor().getPipe();
      PSFieldSet fieldSet = pipe.getMapper().getFieldSet();

      return getInlineLinkFields(fieldSet, null);
   }

   /**
    * Get a list of inline link fields from the given field set.
    *
    * @param fieldSet the field set from which to get all inline link fileds, 
    *    assumed not <code>null</code>.
    * @param inlineFields the list for collecting the inline links, may be 
    *    <code>null</code> in which case a new list is created.
    * @return a list over zero or more <code>PSInlineLinkField</code> objects,
    *    never <code>null</code>, may be empty.
    */
   private static List getInlineLinkFields(PSFieldSet fieldSet, 
      List inlineFields)
   {
      if (inlineFields == null)
         inlineFields = new ArrayList();

      Iterator fields = fieldSet.getAll();
      while (fields.hasNext())
      {
         Object testFieldSet = fields.next();
         if (testFieldSet instanceof PSFieldSet)
         {
            getInlineLinkFields((PSFieldSet) testFieldSet,
               inlineFields);
         }
         else
         {
            PSField field = (PSField) testFieldSet;
            if (field.mayHaveInlineLinks())
               inlineFields.add(new PSInlineLinkField(field));
         }
      }

      return inlineFields;
   }

   /**
    * Process the specified items inline link fields.
    *
    * @param request the request for this process, assumed not 
    *    <code>null</code>.
    * @param locator the locator of the item to be processed, assumed not
    *    <code>null</code>.
    * @param itemDef the item definition of the processed item, assumed
    *    not <code>null</code>.
    * @param relationshipMap the map that maps the old relationship id as 
    * <code>Integer</code> to the new relationship as 
    * <code>PSRelationship</code>, assumed not <code>null</code>, may be empty.
    * @param fields the list of inline link fields, assumed it is one or more
    *    <code>PSInlineLinkField</code> objects.
    * @param communityId the community id for which to process the item, -1
    *    to allow all communities.
    * @param checkin this flag specifies whether the item is checked in after
    *    inline links have been processed or not.
    * @throws PSException for any error.
    */
   private static void processInlineLinkItem(PSRequest request, 
      PSLocator locator, PSItemDefinition itemDef, Map relationshipMap, 
      Iterator fields, int communityId, boolean bCheckout, boolean checkin) 
      throws PSException
   {
      PSWebServicesRequestHandler ws =
         PSWebServicesRequestHandler.getInstance();

      try
      {
         request.setParameter(IPSHtmlParameters.SYS_CONTENTID, 
            Integer.toString(locator.getId()));
         request.setParameter(IPSHtmlParameters.SYS_REVISION, 
            Integer.toString(locator.getRevision()));
         if (bCheckout)
         {
            request.setParameter(IPSHtmlParameters.SYS_CHECKOUT_SAME_REVISION,
                  IPSConstants.BOOLEAN_TRUE);
            ws.executeCheckInOut(request, IPSConstants.TRIGGER_CHECKOUT);
         }
   
         PSServerItem item = new PSServerItem(itemDef);
         item.load(locator, request, false, communityId);
   
         // process all inline link fields
         while (fields.hasNext())
         {
            PSInlineLinkField field = (PSInlineLinkField)fields.next();
            String name = field.getField().getSubmitName();
            PSItemField itemField = (PSItemField) item.getFieldByName(name);
            if (itemField == null)
            {
               Iterator childItems = item.getAllChildren();
               while (childItems.hasNext())
               {
                  PSItemChild itemChild = (PSItemChild) childItems.next();
                  Iterator entries = itemChild.getAllEntries();
                  while (entries.hasNext())
                  {
                     PSItemChildEntry entry = (PSItemChildEntry) entries.next();
                     PSItemField childField = entry.getFieldByName(name);
                     if (childField != null)
                     {
                        processInlineLinkField(childField, relationshipMap);
                        entry.setAction(PSItemChildEntry.CHILD_ACTION_UPDATE);
                     }
                  }
               }
            }
            else
               processInlineLinkField(itemField, relationshipMap);
         }
         
         /*
          * Indicate that the following update is for inline link data update 
          * and therfore inline links don't need to be processed again.
          */
         try
         {
            request.setParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE, 
               "yes");
            item.save(request, -1);
         }
         finally
         {
            request.removeParameter(
               IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);
         }
      }
      finally
      {
         if (bCheckout)
            request.removeParameter(IPSHtmlParameters.SYS_CHECKOUT_SAME_REVISION);
         if (checkin)
            ws.executeCheckInOut(request, IPSConstants.TRIGGER_CHECKIN);
      }
   }

   /**
    * Process the given inline link field.
    *
    * @param itemField the inline link field to be processed, assumed not
    *    <code>null</code>.
    * @param relationshipMap the map that maps the old relationship id as 
    * <code>Integer</code> to the new relationship as 
    * <code>PSRelationship</code>, assumed not <code>null</code>, may be empty.
    * @throws PSCmsException if an error occurs.
    */
   private static void processInlineLinkField(PSItemField itemField,
      Map relationshipMap) throws PSCmsException
   {
      IPSFieldValue fieldValue = itemField.getValue();
      if (!(fieldValue instanceof PSTextValue))
         return;


         String text = ((PSTextValue) fieldValue).getValueAsString();
         if (text.trim().length() == 0)
            return;

      try
      {
         // assume the text is a valid XML document, already tidied
         Document fieldDoc = PSXmlDocumentBuilder.createXmlDocument(
            new InputSource( new StringReader(text)), false);

         PSInlineLinkField.modifyField(fieldDoc.getDocumentElement(),
            relationshipMap);
         
         StringWriter swriter = new StringWriter();
         PSNodePrinter np = new PSNodePrinter(swriter);
         try
         {
            np.printNode(fieldDoc.getDocumentElement());
         }
         catch (IOException e1)
         {

            logger.warn("IOException occurred while writing the inline link field {} ERROR: {}.",itemField.getName(),e1.getMessage());
            logger.debug(e1.getMessage(),e1);
         }
         text = swriter.toString();
      }
      catch (Exception e)
      {
         logger.error("Error happened while parsing Field : {}, the inline link field value ERROR: {}.",itemField.getName(),e.getMessage());
         logger.debug(e.getMessage(),e);
      }
      itemField.addValue(new PSTextValue(text));
   }
}
