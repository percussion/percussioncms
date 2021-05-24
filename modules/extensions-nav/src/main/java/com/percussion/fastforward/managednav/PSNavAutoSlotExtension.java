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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSVariantSlotType;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class populates the Navigation slots on a page assembler. This Rhythmyx
 * post-exit is designed to be added to an assembler for a normal site page.
 * <p>
 * The first step is to select a Navon. This is normally done by finding a
 * folder which contains both this page and a Navon. If only one Navon is found,
 * it is used. If more than one folder and navon are found, the RXSITES table is
 * examined to determine if any of the folder is a descendent of the site folder
 * root.
 * <p>
 * It is also possible to supply an HTML parameter either as
 * <code>sys_folderid</code> or as <code>rx_folder</code>. If this
 * parameter is present, it is assumed to contain the content id of the
 * appropriate folder. This will normally happen only during site folder
 * publishing.
 * <p>
 * Once the Navon is found, the slots that are defined on the current variant
 * are examined. Each slot which is present on this page and is also defined
 * within the Navigation configuration, will receive a link to to the selected
 * Navon with variant ids as determined by the allowed variants in that slot.
 * <p>
 * This exit must be added to the assembler <b>after </b> the
 * <code>sys_casAddAssemblerInfo</code> exit. There are no parameters.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavAutoSlotExtension extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{
   /**
    * This exit will never modify the stylesheet.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Process the result document. Required by the post-exit interface.
    * 
    * @param params the exit parameter array. This exit has no parameters.
    * @param req the parent request context.
    * @param resultDoc the output document from the content assembler.
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      log.debug("Content id {} Variant id {} ", req.getParameter(IPSHtmlParameters.SYS_CONTENTID), req.getParameter(IPSHtmlParameters.SYS_VARIANTID));

      log.debug("User Session id {} ", req.getSecurityToken().getUserSessionId());

      try
      {
         Object recurse = req.getPrivateObject(RECURSION_DETECT);
         if (recurse != null && recurse instanceof Boolean
               && ((Boolean) recurse).booleanValue() == true)
         {
            log.error("Recursion violation in PSNavAutoSlot. Content Id= {} ", req.getParameter(IPSHtmlParameters.SYS_CONTENTID));
            //leave without adding any Navigation
            return resultDoc;
         }
         else
         {
            req.setPrivateObject(RECURSION_DETECT, true);
         }
         ms_config = PSNavConfig.getInstance(req);
         PSComponentSummary navon = findNavon(req);
         if (navon != null)
         {
            Document navonDoc = PSNavTreeLinkExtension.getTreeVariantXMLRaw(
                  req, navon.getCurrentLocator());
            if (navonDoc != null)
            {
               processSlots(req, navon, resultDoc, navonDoc);
               PSNavTreeXMLUtils.setNavVariable(req, resultDoc, navonDoc);
            }
         }
         else
         {
            log.debug("Navon not found ");
         }

         req.setPrivateObject(RECURSION_DETECT, null);
      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         log.error("PSNavException found: {}", e.getMessage());
         log.error(PSNavAutoSlotExtension.class, e);
         log.debug(e.getMessage(), e);

         throw new PSExtensionProcessingException(0, e.getMessage());

      }
      catch (Exception ex)
      {
         log.error("unexcepted exception");
         log.error(this.getClass().getName(), ex);
         log.debug(ex.getMessage(), ex);
         throw new PSExtensionProcessingException(this.getClass().getName(), ex);
      }

      return resultDoc;
   }

   /**
    * Finds a navon by lookig for a specified folder id parameter. If the caller
    * has specified a <code>sys_folderid</code> HTML parameter this will be
    * used to find the folder. If no <code>sys_folderid</code> exists, use the
    * <code>rx_folder</code> HTML parameter. If neither of these parameters
    * are specified, return a <code>null</code>
    * 
    * @param req the callers request.
    * @return the navon summary or <code>null</code>.
    * @throws PSNavException
    */
   private PSComponentSummary findNavonRx(IPSRequestContext req)
         throws PSNavException
   {
      String rxfolder = req.getParameter(IPSHtmlParameters.SYS_FOLDERID);
      log.debug("sys_folderid is {} ", rxfolder);
      if (rxfolder == null || rxfolder.length() == 0)
      {
         rxfolder = req.getParameter("rx_folder");
         log.debug("rx_folder is {}", rxfolder);
      }
      if (rxfolder != null && rxfolder.length() > 0)
      {
         log.debug("folder id specifed is {}", rxfolder);
         PSLocator folderLoc = new PSLocator(rxfolder);
         PSComponentSummary folderSummary = PSNavUtil.getItemSummary(req,
               folderLoc);
         if (folderSummary == null)
         {
            log.warn("rx_folder points to non-existant folder {}", rxfolder);
            return null;
         }
         if (!folderSummary.isFolder())
         {
            log.warn("rx_folder points to an invalid folder {}", rxfolder);
            return null;
         }
         log.debug("folder name is {}", folderSummary.getName());
         PSNavFolder ourNav = PSNavFolderUtils.getNavParentFolder(req,
               folderSummary);
         if (ourNav == null)
         {
            log.debug("rx_folder does not contain a Navon");
            return null;
         }
         return ourNav.getNavonSummary();
      }
      return null;
   }

   /**
    * Finds the navon for this page. There are 3 possibilities:
    * <ol>
    * <li>The caller has specified a folder ID (see findNavonRx)
    * <li>The page is in only one folder that contains a Navon.
    * <li>The page is in more that one folder that contain Navons.
    * </ol>
    * If the page is in more than one folder, use the callers
    * <code>sys_siteid</code> parameter to determine which of these folders
    * are part of the selected site.
    * <p>
    * If the site folder ID is supplied, and none of the foldes are under that
    * site, no folder will be selected.
    * <p>
    * If the filtering fails to resolve to a single folder, take the first
    * folder name alphabetically.
    * @param req reqiest context object, assumed not <code>null</code>.
    * @return
    * @throws PSNavException
    * 
    * @author DavidBenua
    *  
    */
   private PSComponentSummary findNavon(IPSRequestContext req)
         throws PSNavException
   {

      //first check if it's been specified in a parameter.
      PSComponentSummary navonById = findNavonRx(req);
      if (navonById != null)
      {
         return navonById;
      }
      PSNavFolder ourFolder = null;

      PSLocator itemLoc = new PSLocator(req
            .getParameter(IPSHtmlParameters.SYS_CONTENTID));
      PSNavFolderSet allFolders = PSNavFolderUtils
            .buildNavFolders(req, itemLoc);
      if (allFolders.isEmpty())
      { // no parent nav folders found
         log.debug("Item is not in any Nav folder");
         return null;
      }
      if (allFolders.size() == 1)
      {
         log.debug("Exactly 1 folder found");
         ourFolder = allFolders.getFirst();
      }
      else
      {
         log.debug("Item found in {}  folders ", allFolders.size());
         //find Site root -- use that to eliminate folders
         String siteId = req.getParameter(IPSHtmlParameters.SYS_SITEID);
         if (siteId != null && siteId.trim().length() > 0)
         {
            PSComponentSummary siteRoot = getSiteRoot(req, siteId);

            try
            {
               if (siteRoot != null)
               {
                  log.debug("Found Site Root folder");
                  PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();
                  PSLocator siteLoc = siteRoot.getCurrentLocator();
                  String cType = PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE;
                  PSNavFolderSet siteFolders = new PSNavFolderSet();
                  Iterator it = allFolders.iterator();

                  while (it.hasNext())
                  {
                     PSNavFolder tFolder = (PSNavFolder) it.next();
                     log.debug("Examining folder {}", tFolder.getName());
                     PSLocator lFolder = tFolder.getFolderSummary()
                           .getCurrentLocator();
                     if (relProxy.isDescendent(cType, siteLoc, lFolder,
                           PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT))
                     {
                        log.debug("Folder is a descendent of site");
                        siteFolders.add(tFolder);
                     }
                  }
                  allFolders = siteFolders;
               }
            }
            catch (PSCmsException e)
            {
               throw new PSNavException(e);
            }
         }
         ourFolder = allFolders.getFirst();
      }
      if (ourFolder != null)
      {
         log.debug("Selected folder is {} ", ourFolder.getName());
         return ourFolder.getNavonSummary();
      }
      log.debug("No valid folder found");
      return null;
   }

   /**
    * process the nav slots found on this variant. All nav variants defined for
    * each slot will be added to the result document.
    * 
    * @param req the parent request context
    * @param navon the Navon which will be added to the slots
    * @param resultDoc the assembler result document where the slot are added.
    * @param navonDoc the navon XML document. Used to retrieve common
    *           information about the Navon.
    * @throws PSNavException
    */
   private void processSlots(IPSRequestContext req, PSComponentSummary navon,
         Document resultDoc, Document navonDoc) throws PSNavException
   {
      log.debug("processing slots");
      //find the parent item
      PSLocator itemLoc = new PSLocator(req
            .getParameter(IPSHtmlParameters.SYS_CONTENTID), req
            .getParameter(IPSHtmlParameters.SYS_REVISION));
      PSComponentSummary itemSummary = PSNavUtil.getItemSummary(req, itemLoc);

      log.debug("Item name is {}", itemSummary.getName());

      Map themeParams = buildThemeParams(req, navonDoc);

      //find the variant we are assembling
      int variantId = Integer.parseInt(req
            .getParameter(IPSHtmlParameters.SYS_VARIANTID));
      //int contentTypeId = itemSummary.getContentTypeId();
      int contentTypeId = -1;
      PSContentTypeVariant variant = PSNavUtil.loadVariantInfo(req,
            contentTypeId, variantId);

      if (variant == null)
      {
         Object[] args = new Object[2];
         args[0] = new Integer(contentTypeId);
         args[1] = new Integer(variantId);
         String errMsg = MessageFormat.format(MSG_VARIANT, args);
         log.error(errMsg);
         return;
      }
      //list the slots on this variant
      Iterator slots = variant.getVariantSlots().iterator();
      PSNavSlotSet navSlots = ms_config.getNavSlots();

      Element relatedContent = findRelatedContentElement(resultDoc);
      if (relatedContent == null)
      {
         log.warn("No related content, cannot process slots");
         return;
      }
      String context = req.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      if (context == null || context.trim().length() == 0)
      {
         context = "0";
      }

      while (slots.hasNext())
      {
         PSVariantSlotType varSlot = (PSVariantSlotType) slots.next();
         int varSlotId = varSlot.getSlotId();
         log.debug("Processing Slot Id {}", String.valueOf(varSlotId));

         //look for the slot in our set of nav slots.
         PSNavSlot navSlot = navSlots.getSlotById(varSlotId);
         if (navSlot != null)
         { // the slot is a nav slot
            log.debug("Processing Slot Name {}", navSlot.getSlotName());
            Iterator navSlotVariants = navSlot.getVariantIterator();
            while (navSlotVariants.hasNext())
            {
               PSContentTypeVariant linkVar = (PSContentTypeVariant) navSlotVariants
                     .next();
               log.debug("Adding Variant {}", linkVar.getName());

               PSNavLink link = new PSNavLink();
               link.createLinkToDocument(req, navon, linkVar, themeParams);

               PSNavTreeXMLUtils.addLinkUrl(relatedContent, link, navSlot,
                     resultDoc, context);
            } //while variant
         } // if nav slot
         else
         { // this is not an error, we just skip over all other slots.
            log.debug("Not a Nav Slot");
         }
      } // while slot

   }

   /**
    * Finds the <code>&lt;RelatedContent&gt;</code> element in the result
    * document.
    * 
    * @param doc the assembler result document.
    * @return the Related content element or <code>null</code> if not found.
    */
   private static Element findRelatedContentElement(Document doc)
   {
      PSXmlTreeWalker walker = new PSXmlTreeWalker(doc.getDocumentElement());

      Element assemblerInfo = walker.getNextElement(
            ASSEMBLER_INFO_ELEM, true);
      if (assemblerInfo == null)
      { // no sys_AssemblerInfo element, can't go forward
         log.warn("No Assembler Info Element, cannot add Navigition Slots");
         return null;
      }
      Element relatedContent = walker.getNextElement(
            RELATED_CONTENT_ELEM, true);

      if (relatedContent == null)
      {
         log.warn("No Related Content node... invalid document structure");
      }

      return relatedContent;
   }

   /**
    * Builds the theme override parameter map. This map will be used to set the
    * theme in the root of the Nav Tree. If a theme name has been provided as an
    * HTML parameter, it will be used. Otherwise, the theme from the navon will
    * be used. If no Theme is provided, the map will be <code>empty</code>.
    * 
    * @param req the parent request
    * @param navonDoc the navon whitebox XML document.
    * @return a map of the theme override parameters. Never <code>null</code>
    *         but may be <code>empty</code>.
    * @throws PSNavException
    */
   private static Map buildThemeParams(IPSRequestContext req, Document navonDoc)
         throws PSNavException
   {
      ms_config = PSNavConfig.getInstance(req);
      Map params = new HashMap();

      //first see if the caller specified a Theme
      String themeParam = ms_config
            .getPropertyString(PSNavConfig.NAVTREE_PARAM_THEME);
      String oldNavTheme = req.getParameter(themeParam);
      if (oldNavTheme != null && oldNavTheme.trim().length() > 0)
      {
         //use caller's theme
         params.put(themeParam, oldNavTheme);
      }
      else
      {
         //otherwise we need to load it from the tree.
         //The Tree will be cached (if it isn't already).
         Element treeRoot = navonDoc.getDocumentElement();
         String theme = treeRoot.getAttribute(PSNavTree.XML_ATTR_THEME);
         if (theme != null && theme.trim().length() > 0)
         {
            params.put(themeParam, theme);
         }
      }
      return params;
   }

   /**
    * Gets the site root folder by site id. This method performs a query against
    * the RXSITES table to determine the site root folder. If the site root
    * folder is not specified or is invalid, return <code>null</code>.
    * 
    * @param req thet parent request context.
    * @param siteId the site id.
    * @return the site root folder or <code>null</code> if the folder name is
    *         not found.
    * @throws PSNavException when the site query cannot be performed.
    */
   private static PSComponentSummary getSiteRoot(IPSRequestContext req,
         String siteId) throws PSNavException
   {
      log.debug("searching for site {}", siteId);
      try
      {
         Map smap = new HashMap();
         smap.put(IPSHtmlParameters.SYS_SITEID, siteId);
         IPSInternalRequest iq = req.getInternalRequest(SITEQUERY, smap, false);
         if (iq == null)
         {
            log.error("Site Query not found");
            throw new PSNavException("Site Query " + SITEQUERY + " not found ");
         }
         Document sdoc = iq.getResultDoc();
         if (sdoc == null)
         {
            log.error("Internal Request Error");
            throw new PSNavException("Internal Request Error");
         }

         NodeList nl = sdoc.getElementsByTagName(SITE_ROOT_ELEM);
         if (nl.getLength() == 0)
         {
            log.error("Site not found {}", siteId);
            return null;
         }
         String rootFolderName = PSXmlTreeWalker.getElementData(nl.item(0));
         if (rootFolderName == null || rootFolderName.trim().length() == 0)
         {
            log.warn("Site has no root folder {}", siteId);
            return null;
         }
         log.debug("Site root is {}", rootFolderName);

         PSRelationshipProcessor relProxy = PSRelationshipProcessor.getInstance();

         PSComponentSummary rootFolder = relProxy.getSummaryByPath(PSFolder
               .getComponentType(PSFolder.class), rootFolderName,
               PSRelationshipConfig.TYPE_FOLDER_CONTENT);
         if (rootFolder == null)
         {
            log.warn("Root Folder {}  not found... check site configuration ", rootFolderName);
            return null;
         }

         return rootFolder;

      }
      catch (Exception ex)
      {
         log.error(PSNavAutoSlotExtension.class.getName(), ex);
      }
      return null;
   }

   /**
    * Writes the log for debugging purposes.
    */
   private static final Logger log = LogManager.getLogger(PSNavAutoSlotExtension.class);

   /**
    * Local pointer to the singleton configuration instance.
    */
   static PSNavConfig ms_config;

   /**
    * Name of the query resource for obtaining site information.
    */
   private static final String SITEQUERY = "../rxs_navSupport/rxsiteinfo.xml";

   /**
    * Name the XML element returned by the SITEQUERY
    */
   private static final String SITE_ROOT_ELEM = "folder_root";

   /**
    * Error message for missing variants.
    */
   private static final String MSG_VARIANT = 
      "Content Type {0} Variant {1} not found in the navigation config. Please "
      + "reset the navigation";

   /**
    * Flag used as privatee object to prevent the NavAutoSlot exit infinite
    * recursive loops.
    */
   private static final String RECURSION_DETECT = 
      "com.percussion.consulting.nav.PSNavAutoSlotExtensions.Recursion";

    /**
     * The element name for the assembler info added to the result document.
     */
    public static final String ASSEMBLER_INFO_ELEM = "sys_AssemblerInfo";

    /**
     * The element name for the element wrapping all related content
     * information.
     */
    public static final String RELATED_CONTENT_ELEM = "RelatedContent";

}
