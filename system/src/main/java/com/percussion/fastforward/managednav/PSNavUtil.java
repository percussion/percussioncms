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
package com.percussion.fastforward.managednav;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSComponentProcessor;
import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSAaRelationshipList;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSContentTypeVariantSet;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSSlotType;
import com.percussion.cms.objectstore.PSSlotTypeSet;
import com.percussion.cms.objectstore.server.PSContentTypeVariantsMgr;
import com.percussion.cms.objectstore.server.PSServerItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of static routines for converting various CMS objects into other
 * objects. Do not put any application specific processing in this class.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavUtil
{

   /**
    * Static methods only. Never constructed
    *  
    */
   private PSNavUtil()
   {
   }

   /**
    * Returns item summaries given a locator.
    * 
    * @param request request, assumed never <code>null</code>.
    * @param locator locator, assumed never <code>null</code>.
    * @return component summary, never <code>null</code>.
    * @throws PSNavException if anything goes wrong.
    */
   public static PSComponentSummary getItemSummary(IPSRequestContext request,
         PSLocator locator) throws PSNavException
   {
      if (request == null)
      {
         throw new IllegalArgumentException("request must not be null");
      }
      if (locator == null)
      {
         throw new IllegalArgumentException("locator must not be null");
      }
      try
      {
         log.debug("loaded component summary");
         IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
         PSComponentSummary summary = cms.loadComponentSummary(locator.getId());
         if (summary == null)
         {
            throw new PSNavException("Could not get locator for contentid= "
                  + locator.getId());
         }
         return summary;
      }
      catch (PSNavException psx)
      {
         throw (PSNavException) psx.fillInStackTrace();
      }
      catch (Exception ex)
      {
         throw new PSNavException(MYNAME, ex);
      }

   }

   /**
    * Check of the supplied item is a Navon or Navtree type.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param summary component summary of the item to be checked, must not be
    *    <code>null</code>.
    * @return <code>true</code> if the component summary belongs to an item of
    *    Navon or Navtree content type, <code>false</code> otherwise.
    * @throws PSNavException if check could not be performed for any reason.
    */
   public static boolean isNavType(IPSRequestContext req,
      PSComponentSummary summary) throws PSNavException
   {
      if (req == null)
         throw new IllegalArgumentException("req must not be null");

      if (summary == null)
         throw new IllegalArgumentException("summary must not be null");
      
      return isNavonItem(req, summary) || isNavonTreeItem(req, summary);
   }

   /**
    * Convenience method that builds the component summary given the item
    * locator and calls
    * {@link #isNavType(IPSRequestContext, PSComponentSummary)} to check if the
    * item is of type Navon or Navtree.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param loc Locator of the item to be checked, must not be
    *    <code>null</code>.
    * @return <code>true</code> if the component summary belongs to an item of
    *    Navon or Navtree content type, <code>false</code> otherwise.
    * @throws PSNavException if check could not be performed for any reason.
    */
   public static boolean isNavType(IPSRequestContext req, PSLocator loc)
         throws PSNavException
   {
      if (req == null)
         throw new IllegalArgumentException("req must not be null");

      if (loc == null)
         throw new IllegalArgumentException("loc must not be null");

      PSComponentSummary summary = getItemSummary(req, loc);
      return isNavType(req, summary);
   }
   
   /**
    * Is the item for the supplied locator of contenttype <code>Navon</code>?
    * 
    * @param request the request used to execute the test, not 
    *    <code>null</code>.
    * @param locator the locator of the item to be tested, not 
    *    <code>null</code>.
    * @return <code>true</code> if the item of the supplied locator has
    *    contenttype <code>Navon</code>, false otherwise.
    * @throws PSNavException for eny error executing the test.
    */
   public static boolean isNavonItem(IPSRequestContext request, 
      PSLocator locator) throws PSNavException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");

      PSComponentSummary summary = getItemSummary(request, locator);
      return isNavonItem(request, summary);
   }
   
   /**
    * Is the item for the supplied summary of contenttype <code>Navon</code>?
    * 
    * @param request the request used to execute the test, not 
    *    <code>null</code>.
    * @param summary the summary of the item to be tested, not 
    *    <code>null</code>.
    * @return <code>true</code> if the item of the supplied summary has
    *    contenttype <code>Navon</code>, false otherwise.
    */
   public static boolean isNavonItem(IPSRequestContext request, 
      PSComponentSummary summary) throws PSNavException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (summary == null)
         throw new IllegalArgumentException("summary cannot be null");

      PSNavConfig config = PSNavConfig.getInstance();
      return summary.getContentTypeId() == config.getNavonType();
   }
   
   /**
    * Is the item for the supplied locator of contenttype 
    * <code>NavonTree</code>?
    * 
    * @param request the request used to execute the test, not 
    *    <code>null</code>.
    * @param locator the locator of the item to be tested, not 
    *    <code>null</code>.
    * @return <code>true</code> if the item of the supplied locator has
    *    contenttype <code>NavonTree</code>, false otherwise.
    */
   public static boolean isNavonTreeItem(IPSRequestContext request, 
      PSLocator locator) throws PSNavException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (locator == null)
         throw new IllegalArgumentException("locator cannot be null");

      PSComponentSummary summary = getItemSummary(request, locator);
      return isNavonTreeItem(request, summary);
   }
   
   /**
    * Is the item for the supplied summary of contenttype 
    * <code>NavonTree</code>?
    * 
    * @param request the request used to execute the test, not 
    *    <code>null</code>.
    * @param summary the summary of the item to be tested, not 
    *    <code>null</code>.
    * @return <code>true</code> if the item of the supplied summary has
    *    contenttype <code>NavonTree</code>, false otherwise.
    */
   public static boolean isNavonTreeItem(IPSRequestContext request, 
      PSComponentSummary summary) throws PSNavException
   {
      if (request == null)
         throw new IllegalArgumentException("request cannot be null");
      
      if (summary == null)
         throw new IllegalArgumentException("summary cannot be null");

      PSNavConfig config = PSNavConfig.getInstance();
      return summary.getContentTypeId() == config.getNavTreeType();
   }

   /**
    * returns a single string value from an item by field. The value will be
    * <code>Empty</code> if the field exists but has no values, and
    * <code>null</code> if the field does not exist.
    * 
    * @param item the item to examine.
    * @param fieldName the field name
    * @return the value or <code>null</code>
    * @throws PSNavException
    */
   public static String getSingleValueField(PSServerItem item, String fieldName)
         throws PSNavException
   {

      PSItemField itemField = item.getFieldByName(fieldName);
      if (itemField == null)
      {
         return null;
      }
      if (itemField.isMultiValue())
      {
         String msg = "Field" + fieldName + " is multi-value";
         log.error(msg);
         throw new PSNavException(msg);
      }
      IPSFieldValue fieldValue = itemField.getValue();
      if (fieldValue != null)
      {
         try
         {
            return fieldValue.getValueAsString();
         }
         catch (PSCmsException e)
         {
            throw new PSNavException(MYNAME, e);
         }
      }
      return null;
   }

   /**
    * Get content type variant object gven the content typeid and variantid.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param contentTypeId a valid content type id.
    * @param variantName name of the variant for the content type specified,
    *           must not be <code>null</code> or empty.
    * @return content type variant object if one found, <code>null</code>
    *         otherwise.
    * @throws PSNavException if information could not be obtained from the
    *            system for any reason.
    */
   public static PSContentTypeVariant loadVariantInfo(IPSRequestContext req,
         long contentTypeId, String variantName) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (variantName == null || variantName.length() < 1)
      {
         throw new IllegalArgumentException(
               "variantName must not be null or empty");
      }
      PSContentTypeVariantSet variants = loadVariantSet(req);

      Iterator iter = variants.iterator();
      while (iter.hasNext())
      {
         PSContentTypeVariant current = (PSContentTypeVariant) iter.next();
         if (current.supportsContentType((int) contentTypeId)
               && current.getName().equals(variantName))
         {
            return current;

         }
      }
      log.debug("Search for Variant {} in ContentType {} failed!", variantName, String.valueOf(contentTypeId));
      return null;
   }

   /**
    * Get content type variant object gven the content typeid and variantid.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param contentTypeId a valid content type id.
    * @param variantId a valid variantid of the variant for the content type
    *           specified.
    * @return content type variant object if one found, <code>null</code>
    *         otherwise.
    * @throws PSNavException if information could not be obtained from the
    *            system for any reason.
    */
   public static PSContentTypeVariant loadVariantInfo(IPSRequestContext req,
         long contentTypeId, int variantId) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      PSNavConfig config = PSNavConfig.getInstance(req);
      PSContentTypeVariantSet variants = config.getAllVariants();

      Iterator iter = variants.iterator();
      while (iter.hasNext())
      {
         PSContentTypeVariant current = (PSContentTypeVariant) iter.next();
         if ((contentTypeId == -1 || current.supportsContentType((int) contentTypeId))
               && current.getVariantId() == variantId)
         {
            return current;

         }
      }
      log.warn("Search for Variant ID {} in ContentType {} failed!", String.valueOf(variantId), String.valueOf(contentTypeId));
      return null;
   }

   /**
    * Method to load and return all content type variants registered in the
    * system.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @return content type variant set containing all variants registered with
    *         the system.
    * @throws PSNavException if loading of variants fails for any reason.
    */
   public static PSContentTypeVariantSet loadVariantSet(IPSRequestContext req)
         throws PSNavException
   {
      try
      {
         return PSContentTypeVariantsMgr.getAllContentTypeVariants(req);
      }
      catch (Exception ex)
      {
         throw new PSNavException(MYNAME, ex);
      }
   }

   /**
    * Load slot by name.
    * 
    * @param req request cntext object, must not be <code>null</code>.
    * @param parent Locator for the parent item, not used.
    * @param slotName name of the slot to lookup by, must not be
    *           <code>null</code> or empty.
    * @return slot object if found, <code>null</code> otherwise.
    * @throws PSNavException if lookup fails for any reason.
    */
   public static PSSlotType findSlotByName(IPSRequestContext req, PSKey parent,
         String slotName) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (slotName == null || slotName.length() < 1)
      {
         throw new IllegalArgumentException("slotName must not be null or empty");
      }
      // avoid eclipse warning
      if (parent == null);
      
      try
      {
         log.debug("Getting slot {} ", slotName);
         PSNavConfig config = PSNavConfig.getInstance(req);

         PSSlotTypeSet allSlots = config.getAllSlots();

         PSSlotType ourSlot = allSlots.getSlotTypeByName(slotName);
         log.debug("Got slot");
         return ourSlot;
      }
      catch (Exception e)
      {
         throw new PSNavException(MYNAME, e);
      }

   }

   /**
    * loads all slots in the system.
    * 
    * @param req the parent request, must not be <code>null</code>
    * @return the set of all slots. Never <code>null</code>
    * @throws PSNavException
    */
   public static PSSlotTypeSet loadAllSlots(IPSRequestContext req)
         throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      try
      {
         log.debug("loading proxy");
         
         PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
         IPSComponentProcessor compProxy = pf.getCompProxy();
         Element[] slotElems = compProxy.load(PSSlotTypeSet
               .getComponentType(PSSlotTypeSet.class), new PSKey[0]);
         PSSlotTypeSet allSlots = new PSSlotTypeSet(slotElems);
         log.debug("loaded slots");
         return allSlots;
      }
      catch (Exception e)
      {
         throw new PSNavException(MYNAME, e);
      }
   }

   /**
    * Helper method to build a map of all standard assembly parameters off of
    * the request context. Any parameter that has a value of <code>null</code>
    * will be excluded.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @return Map of the standard parameters as described above, never
    *         <code>null</code>, may be empty.
    */
   public static Map buildStandardParams(IPSRequestContext req)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      Map params = new HashMap();
      copyParam(req, params, IPSHtmlParameters.SYS_CONTENTID);
      copyParam(req, params, IPSHtmlParameters.SYS_REVISION);
      copyParam(req, params, IPSHtmlParameters.SYS_CONTEXT);
      copyParam(req, params, IPSHtmlParameters.SYS_AUTHTYPE);
      copyParam(req, params, IPSHtmlParameters.SYS_VIEW);
      copyParam(req, params, IPSHtmlParameters.SYS_VARIANTID);
      copyParam(req, params, IPSHtmlParameters.SYS_SITEID);
      copyParam(req, params, IPSHtmlParameters.SYS_FOLDERID);
      copyParam(req, params, IPSHtmlParameters.SYS_COMMAND);

      return params;
   }

   /**
    * Helper method to copy a specified parameter from the request context to
    * the supplied parameter map. If the value of the parameter in the request
    * is null it will not be added to the target map.
    * 
    * @param req requst context object, must not be <code>null</code>.
    * @param target Parameter map to which the new parameter from request
    *           context is to be copied, must not be <code>null</code>
    * @param paramName name of the parameter to copy, must not be
    *           <code>null</code> or empty.
    */
   public static void copyParam(IPSRequestContext req, Map target,
         String paramName)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (target == null)
      {
         throw new IllegalArgumentException("target must not be null");
      }
      if (paramName == null || paramName.length() < 1)
      {
         throw new IllegalArgumentException(
               "paramName must not be null or empty");
      }
      String paramValue = req.getParameter(paramName);

      if (paramValue != null)
      {
         log.debug("adding param {} - {}",paramName, paramValue);
         target.put(paramName, req.getParameter(paramName));
      }
      else
      {
         log.debug("param {} is null", paramName);
      }
   }

   /**
    * Sets appropriate sys_command parameter and relatediteid parameter in the
    * target parameter map if these are present in the request context.
    * 
    * @param req request context object, must not be <code>null</code>
    * @param target target parameter map, must not be <code>null</code>, may
    *           be empty.
    */
   public static void checkActiveAssembly(IPSRequestContext req, Map target)
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (target == null)
      {
         throw new IllegalArgumentException("target must not be null");
      }
      String sysCommand = req.getParameter(IPSHtmlParameters.SYS_COMMAND);
      if (sysCommand != null && sysCommand.equalsIgnoreCase("editrc"))
      {
         log.debug("sysCommand is {}", sysCommand);
         target.put(IPSHtmlParameters.SYS_COMMAND, sysCommand);
         target.put("relateditemid", req
               .getParameter(IPSHtmlParameters.SYS_CONTENTID));
      }
   }

   /**
    * Get the Navon XML document given the navon's component summary.
    * 
    * @param req request context object, must not be <code>null</code>
    * @param navon component summary of the navon item, must not be
    *           <code>null</code>
    * @return Navon XML document, never <code>null</code>.
    * @throws PSNavException if process fails for any reason.
    */
   public static Document getNavonDocument(IPSRequestContext req,
         PSComponentSummary navon) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (navon == null)
      {
         throw new IllegalArgumentException("navon must not be null");
      }
      PSNavComponentSummary navsum = new PSNavComponentSummary(navon);
      return getNavonDocument(req, navsum);
   }

   /**
    * Get the Navon XML document given the navon's component summary.
    * 
    * @param req request context object, must not be <code>null</code>
    * @param navon Nav component summary of the navon item, must not be
    *           <code>null</code>
    * @return Navon XML document, never <code>null</code>.
    * @throws PSNavException if process fails for any reason.
    */
   public static Document getNavonDocument(IPSRequestContext req,
         PSNavComponentSummary navon) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (navon == null)
      {
         throw new IllegalArgumentException("navon must not be null");
      }
      PSNavConfig config = PSNavConfig.getInstance(req);
      PSContentTypeVariant variant;
      long contentTypeId = navon.getContentTypeId();
      if (contentTypeId == config.getNavonType())
      {
         variant = config.getInfoVariant();
      }
      else if (contentTypeId == config.getNavTreeType())
      {
         variant = config.getNavtreeInfoVariant();
      }
      else
      {
         throw new PSNavException("This summary is not a Nav Type"
               + navon.getName());
      }
      return getVariantDocument(req, variant, navon.getCurrentLocator());
   }

   /**
    * Get the Navon XML document given the navon's component summary.
    * 
    * @param req request context object, must not be <code>null</code>
    * @param variant content variantfor the item, must not be <code>null</code>.
    * @param loc Locator of the navon item, must not be <code>null</code>
    * @return Navon XML document, never <code>null</code>.
    * @throws PSNavException if process fails for any reason.
    */
   public static Document getVariantDocument(IPSRequestContext req,
         PSContentTypeVariant variant, PSLocator loc) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (variant == null)
      {
         throw new IllegalArgumentException("variant must not be null");
      }
      if (loc == null)
      {
         throw new IllegalArgumentException("loc must not be null");
      }
      log.debug("loading variant XML result doc");
      try
      {
         Map pmap = buildStandardParams(req);
         String varInStr = String.valueOf(variant.getVariantId());
         pmap.put(IPSHtmlParameters.SYS_VARIANTID, varInStr);
         log.debug("variant id {}", varInStr);
         pmap.put(IPSHtmlParameters.SYS_CONTENTID, loc
               .getPart(PSLocator.KEY_ID));
         log.debug("content id {}", loc.getPart(PSLocator.KEY_ID));
         pmap.put(IPSHtmlParameters.SYS_REVISION, loc
               .getPart(PSLocator.KEY_REVISION));
         log.debug("revision {}", loc.getPart(PSLocator.KEY_REVISION));

         IPSInternalRequest ir = req.getInternalRequest(variant
               .getAssemblyUrl(), pmap, false);
         if (ir == null)
         {
            log.error("Variant Assembler not found {}", variant.getName());
            log.error("Assembly URL {}", variant.getAssemblyUrl());
            throw new PSNavException("Variant not found");
         }
         log.debug("loading xml document");
         return ir.getResultDoc();

      }
      catch (PSNavException ne)
      {

         throw (PSNavException) ne.fillInStackTrace();

      }
      catch (Exception e)
      {

         throw new PSNavException(PSNavUtil.class.getName(), e);

      }

   }

   /**
    * Walk through the XML document to locate the field specified by name and
    * return the field vale.
    * 
    * @param doc XML document that has the field, must not be <code>null</code>.
    * @param fieldName name of the field to get the value, must not be
    *           <code>null</code> or empty.
    * @return field value, may be <code>null</code> or empty.
    */
   public static String getFieldValueFromXML(Document doc, String fieldName)
   {
      if (doc == null)
      {
         throw new IllegalArgumentException("doc must not be null");
      }
      if (fieldName == null || fieldName.length() < 1)
      {
         throw new IllegalArgumentException("fieldName must not be null or empty");
      }
      log.debug("get field {}", fieldName);
      PSXmlTreeWalker walk = new PSXmlTreeWalker(doc.getDocumentElement());
      Element resElem = walk.getNextElement(fieldName,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (resElem == null)
      {
         log.warn("Field {} not found ",fieldName);
         return null;
      }
      String result = PSXmlTreeWalker.getElementData(resElem);
      log.debug("got field value {}", result);

      return result;
   }

   /**
    * Get AA relationships for the parent item via the specified slot by name.
    * @param req request context object, must not be <code>null</code>.
    * @param parentLoc Locator ofthe parent item, must not be <code>null</code>.
    * @param slotName name of the slot, must not be <code>null</code> or empty
    * @return List of relationships, never <code>null</code>may be empty.
    * @throws PSNavException
    */
   public static PSAaRelationshipList getSlotContents(IPSRequestContext req,
         PSLocator parentLoc, String slotName) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (parentLoc == null)
      {
         throw new IllegalArgumentException("parentLoc must not be null");
      }
      if (slotName == null || slotName.length() < 1)
      {
         throw new IllegalArgumentException("slotName must not be null or empty");
      }
      PSSlotType mySlot = PSNavUtil.findSlotByName(req, parentLoc, slotName);
      if (mySlot == null)
      {
         String errMsg = "Slot Not found " + slotName;
         log.error(errMsg);
         throw new PSNavException(errMsg);
      }

      return getSlotContents(req, parentLoc, mySlot);
   }

   /**
    * Get AA relationships for the parent item via the specified slot by name.
    * @param req request context object, must not be <code>null</code>.
    * @param parentLoc Locator ofthe parent item, must not be <code>null</code>.
    * @param slot slot object, must not be <code>null</code>.
    * @return List of relationships, never <code>null</code>may be empty.
    * @throws PSNavException
    */
   public static PSAaRelationshipList getSlotContents(IPSRequestContext req,
         PSLocator parentLoc, PSSlotType slot) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (parentLoc == null)
      {
         throw new IllegalArgumentException("parentLoc must not be null");
      }
      if (slot == null)
      {
         throw new IllegalArgumentException("slot must not be null");
      }
      log.debug("Loading Slot");
      PSNavConfig config = PSNavConfig.getInstance(req);
      PSNavSlotContents contents = config.getSlotContentsCache(req);
      PSAaRelationshipList rel = contents.getSlotContents(parentLoc, slot
            .getSlotId());
      log.debug("slot loaded");
      return rel;

   }

   /**
    * Old method kept for backward compatability which is similar to the new
    * counterpart
    * {@link #getSlotContents(IPSRequestContext, PSLocator, PSSlotType)}. The
    * new one does loads from cache wheras this one loads using the AA API.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param parentLoc Locator ofthe parent item, must not be <code>null</code>.
    * @param slot slot object, must not be <code>null</code>.
    * @return List of relationships, never <code>null</code> may be empty.
    * @throws PSNavException
    * @deprecated in favor of
    *        {@link #getSlotContents(IPSRequestContext, PSLocator, PSSlotType)}
    */
   public static PSAaRelationshipList getSlotContentsOld(IPSRequestContext req,
         PSLocator parentLoc, PSSlotType slot) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (parentLoc == null)
      {
         throw new IllegalArgumentException("parentLoc must not be null");
      }
      if (slot == null)
      {
         throw new IllegalArgumentException("slot must not be null");
      }
      PSNavProxyFactory pf = PSNavProxyFactory.getInstance(req);
      PSActiveAssemblyProcessorProxy aaProxy = pf.getAaProxy();
      PSAaRelationshipList slotContents;

      try
      {
         slotContents = aaProxy.getSlotRelationships(parentLoc, slot, PSNavUtil
               .getAuthType(req));
      }
      catch (PSCmsException e)
      {
         throw new PSNavException(PSNavUtil.class.getName(), e);
      }
      log.debug("Slot contents found");
      return slotContents;

   }

   /**
    * get the authtype parameter of the current request, or 0 if the authtype is
    * not specified (or invalid).
    * 
    * @param req the callers request context
    * @return the authtype parameter, or 0 if not found.
    */
   public static int getAuthType(IPSRequestContext req)
   {
      int authInt = 0;
      String authString = req.getParameter(IPSHtmlParameters.SYS_AUTHTYPE);
      if (authString != null && authString.trim().length() > 0)
      {
         try
         {
            authInt = Integer.parseInt(authString);
         }
         catch (NumberFormatException nfe)
         {
            log.warn("Authtype is not an integer: {}", authString);
            authInt = 0;
         }
      }

      return authInt;
   }

   /**
    * Gets the community id of the current session.
    * 
    * @param req the callers request context.
    * @return the community id, or -1 if there is no community id.
    */
   public static String getSessionCommunity(IPSRequestContext req)
   {
      int comm = req.getSecurityToken().getCommunityId();
      return String.valueOf(comm);
   }

   /**
    * sets the community id of the current session. A convenience method for use
    * when the community id is available only as an integer.
    * 
    * @param req the parents request context
    * @param commId the community id to set.
    */
   public static void setSessionCommunity(IPSRequestContext req, String commId)
   {
      req.setSessionPrivateObject(IPSHtmlParameters.SYS_COMMUNITY, commId);
   }

   /**
    * sets the community if of the current session.
    * 
    * @param req the parents request context
    * @param comm the community id to set
    */
   public static void setSessionCommunity(IPSRequestContext req, int comm)
   {
      String commId = String.valueOf(comm);
      setSessionCommunity(req, commId);
   }

   /**
    * log the contents of a Map, using the caller's logger
    * 
    * @param map the Map to log
    * @param mapName the name to display in the output
    * @param myLog the callers Logger.
    */
   public static void logMap(Map map, String mapName, Logger myLog)
   {
      if (!myLog.isDebugEnabled())
      {
         return;
      }
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);

      pw.println("Logging map " + mapName);
      Iterator iter = map.keySet().iterator();
      while (iter.hasNext())
      {
         String pName = (String) iter.next();
         Object pObject = map.get(pName);
         if (pObject != null)
         {
            String pValue = map.get(pName).toString();
            pw.println("key " + pName + " value " + pValue);
         }
         else
         {
            pw.println("key " + pName + " is null ");
         }
      }
      pw.flush();
      myLog.debug(sw.toString());
   }

   /**
    * String constant for the fully qualified name of this extensions class
    * </code>
    */
   private static final String MYNAME = "com.percussion.nav.PSNavUtil";

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   static Logger log = LogManager.getLogger(PSNavUtil.class);
   
   /**
    * Constant for preview authtype
    */
   public static final String PREVIEW_AUTHTYPE = "0";

}
