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

import com.percussion.cms.objectstore.PSContentTypeVariant;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestParsingException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSMutableUrl;
import com.percussion.util.PSUrlUtils;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A collection of static methods for manipulating the XML tree after it has
 * been produced. If the tree has been retrieved from the cache, it will have
 * incorrect values for the theme, color selector, variable selector and other
 * values that are passed as HTML parameters. These routines fix those
 * attributes.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavXMLUtils
{
   /**
    * Never constructed, static methods only.
    */
   private PSNavXMLUtils()
   {

   }

   /**
    * Set the attributes for the root node of a document. Convenience method for
    * setAttributes(Element, Map).
    * 
    * @param doc the document
    * @param params the parameter map.
    */
   public static void setAttributes(Document doc, Map params)
   {
      Element rootElem = doc.getDocumentElement();
      setAttributes(rootElem, params);
   }

   /**
    * Set the attributes of an element. The Parameter map contains the names and
    * values of the attributes to be added. The value must be a string or other
    * object whose toString() method returns the correct value. If the attribute
    * already exists, its value will be replaced.
    * 
    * @param elem the XML Element where the attributes are to be added.
    * @param params a map of attributes to add. Must not be <code>null</code>
    *           but may be <code>empty</code>.
    */
   public static void setAttributes(Element elem, Map params)
   {
      Iterator it = params.keySet().iterator();
      while (it.hasNext())
      {
         String pName = it.next().toString();
         String pValue = params.get(pName).toString();
         log.debug("setting attribute {} value {}",pName, pValue);
         elem.setAttribute(pName, pValue);
      }
   }

   /**
    * Convenience method for setRootAttributes(IPSRequestContext, Element,
    * PSNavonStack, Map)
    * 
    * @param req the parent request context
    * @param doc the XML Document to be fixed
    * @param stack the Navon stack.
    * @throws PSNavException
    */
   public static void setRootAttributes(IPSRequestContext req, Document doc,
         PSNavonStack stack) throws PSNavException
   {
      Element docElem = doc.getDocumentElement();
      setRootAttributes(req, docElem, stack, Collections.EMPTY_MAP);
   }

   /**
    * Set the attributes in the root node of our document. These attributes
    * always include image color selector and the variable selector, which will
    * be added to the list of other attributes.
    * 
    * @param req the parent request context
    * @param docElem the XML Document Element
    * @param stack the Navon Stack
    * @param extraAttribs the extra attributes that need to be added.
    * @throws PSNavException
    */
   public static void setRootAttributes(IPSRequestContext req, Element docElem,
         PSNavonStack stack, Map extraAttribs) throws PSNavException
   {
      Map pMap = PSNavUtil.buildStandardParams(req);
      if (stack.getImageSelector() != null)
      {
         pMap.put(PSNavTree.XML_ATTR_SELECTOR, stack.getImageSelector());
      }
      if (stack.getVarSelector() != null)
      {
         pMap.put(PSNavTree.XML_ATTR_VARIABLE, stack.getVarSelector());
      }
      pMap.putAll(extraAttribs);
      setAttributes(docElem, pMap);
   }

   /**
    * Process the XML tree, resetting the attributes and the link URLs. The
    * attributes include the root attributes and the general attributes. The
    * links include the landing page and image links.
    * 
    * @param req the parent request
    * @param doc the result document to be processed.
    * @param stack the Navon Stack
    * @param landingPageParams any added parameters to be applied to the landing
    *           page links.
    * @throws PSNavException
    */
   public static void processXMLTree(IPSRequestContext req, Document doc,
         PSNavonStack stack, Map landingPageParams) throws PSNavException
   {
      Element docElem = doc.getDocumentElement();

      setRootAttributes(req, docElem, stack, landingPageParams);
      PSNavTreeXMLUtils.setNavVariable(req, doc, doc);

      PSXmlTreeWalker walker = new PSXmlTreeWalker(docElem);
      Element rootNavon = walker
            .getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (rootNavon == null)
      {
         log.debug("the navigation tree is empty");
         return;
      }
      rootNavon.setAttribute(PSNavon.XML_ATTR_REL_LEVEL, String.valueOf(stack
            .getRelLevel()));
      if (stack.getRelLevel() == 0)
      {
         rootNavon.setAttribute(PSNavon.XML_ATTR_TYPE,
               PSNavonType.TYPENAME_SELF);
         log.debug("Root node is type {}", PSNavonType.TYPENAME_SELF);
      }
      else
      {
         rootNavon.setAttribute(PSNavon.XML_ATTR_TYPE,
               PSNavonType.TYPENAME_ROOT);
         log.debug("Root node is type {}", PSNavonType.TYPENAME_ROOT);
      }
      buildLandingPage(req, rootNavon, landingPageParams);
      fixInfoLink(req, rootNavon, landingPageParams);
      fixImageLinks(req, rootNavon, landingPageParams);
      processChildXML(req, doc, rootNavon, stack, stack.getRelLevel(), 0,
            landingPageParams);

   }

   /**
    * Update the attributes and links of child nodes. This method recursively
    * calls itself to process all child nodes in the Nav Tree XML document.
    * 
    * @param req the parent request context
    * @param doc the nav tree XML document
    * @param parentElem the parent element whose children will be processed.
    * @param stack the Navon stack. Used to determine which nodes are
    *           "ancestors" of the self node.
    * @param rLevel the relative level.
    * @param aLevel the absolute level. The Root Navon is is absolute level 0.
    * @param landingPageParams the list of other arributes to be added to the
    *           landing page link.
    * @throws PSNavException
    */
   private static void processChildXML(IPSRequestContext req, Document doc,
         Element parentElem, PSNavonStack stack, int rLevel, int aLevel,
         Map landingPageParams) throws PSNavException
   {

      String rLevelStr = String.valueOf(rLevel + 1);
      String parentType = parentElem.getAttribute(PSNavon.XML_ATTR_TYPE);
      String childType = "";
      String selChildType = "";
      String selChildId = "";
      boolean compareFlag = false;
      if (parentType.equals(PSNavonType.TYPENAME_ROOT)
            || parentType.equals(PSNavonType.TYPENAME_ANCESTOR))
      {
         compareFlag = true;
         if (rLevel == -1)
         {
            childType = PSNavonType.TYPENAME_SIBLING;
            selChildType = PSNavonType.TYPENAME_SELF;
         }
         else
         {
            childType = PSNavonType.TYPENAME_ANCESTOR_SIBLING;
            selChildType = PSNavonType.TYPENAME_ANCESTOR;
         }
         selChildId = stack.getId(aLevel + 1);
      }
      else
      {
         childType = getNextType(parentType);
      }
      PSXmlTreeWalker walker = new PSXmlTreeWalker(parentElem);
      Element childElem = walker.getNextElement(PSNavon.XML_ELEMENT_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      while (childElem != null)
      {
         log.debug("setting child element {}", childElem.getAttribute(PSNavon.XML_ATTR_NAME));
         childElem.setAttribute(PSNavon.XML_ATTR_REL_LEVEL, rLevelStr);
         log.debug("setting relative level {}", rLevelStr);
         if (compareFlag)
         {
            String contentId = childElem
                  .getAttribute(PSNavon.XML_ATTR_CONTENTID);
            if (contentId.equals(selChildId))
            {
               childElem.setAttribute(PSNavon.XML_ATTR_TYPE, selChildType);
               log.debug("setting relation {}", selChildType);
            }
            else
            {
               childElem.setAttribute(PSNavon.XML_ATTR_TYPE, childType);
               log.debug("setting relation {}", childType);
            }
         }
         else
         {
            childElem.setAttribute(PSNavon.XML_ATTR_TYPE, childType);
            log.debug("setting relation {}", childType);
         }
         //

         buildLandingPage(req, childElem, landingPageParams);
         fixInfoLink(req, childElem, landingPageParams);
         fixImageLinks(req, childElem, landingPageParams);

         processChildXML(req, doc, childElem, stack, rLevel + 1, aLevel + 1,
               landingPageParams);
         childElem = walker.getNextElement(PSNavon.XML_ELEMENT_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Rebuilds the link to a landing page.
    * 
    * @param req the parent request context
    * @param navonElem the Navon XML element where the landing page resides.
    * @param landingPageParams the parameters to add to the link.
    * @throws PSNavException
    */
   private static void buildLandingPage(IPSRequestContext req,
         Element navonElem, Map landingPageParams) throws PSNavException
   {
      PSXmlTreeWalker lpWalker = new PSXmlTreeWalker(navonElem);
      Element lpElement = lpWalker.getNextElement(
            PSNavon.XML_ELEMENT_LANDINGPAGE,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (lpElement != null)
      {
         log.debug("setting landing page attributes");
         setAttributes(lpElement, landingPageParams);

         String variantIdStr = lpElement
               .getAttribute(IPSHtmlParameters.SYS_VARIANTID);
         int variantid = Integer.parseInt(variantIdStr);
         PSContentTypeVariant variant = 
            PSNavConfig.getInstance(req).getAllVariants()
               .getContentVariantById(variantid);
         log.debug("found variant {}", variant.getName());
         Map linkParams = new HashMap();
         String contentId = lpElement
               .getAttribute(IPSHtmlParameters.SYS_CONTENTID);
         linkParams.put(IPSHtmlParameters.SYS_CONTENTID, contentId);
         String revision = lpElement
               .getAttribute(IPSHtmlParameters.SYS_REVISION);
         linkParams.put(IPSHtmlParameters.SYS_REVISION, revision);

         String folderId = lpElement
         .getAttribute(IPSHtmlParameters.SYS_FOLDERID);
         if(folderId.trim().length() > 0)
         {
            linkParams.put(IPSHtmlParameters.SYS_FOLDERID, folderId);
         }
   
         linkParams.put(IPSHtmlParameters.SYS_VARIANTID, variantIdStr);
         log.debug("landing page contentid {} revision {}",contentId, revision);

         linkParams.putAll(landingPageParams);

         linkParams.put(IPSHtmlParameters.SYS_SESSIONID, req.getSecurityToken()
               .getUserSessionId());

         URL intURL;
         try
         {
            intURL = PSUrlUtils.createUrl("127.0.0.1", new Integer(req
                  .getOriginalPort()), variant.getAssemblyUrl(), linkParams
                  .entrySet().iterator(), (String) null, //no Anchor
                  req);
         }
         catch (MalformedURLException e)
         {
            log.error("Cannot build Landing Page URL {}", e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSNavException(e);
         }
         log.debug("Build URL {}", intURL.toString());
         replaceNodeText(lpElement, intURL.toString());

      }
   }

   /**
    * fixes the info link in a Navon XML element to use the specified
    * parameters.
    * 
    * @param req the parent request context
    * @param navonElem the Navon XML element to fix.
    * @param landingPageParams the extra parameters to be applied to the info
    *           link.
    * @throws PSNavException
    */
   private static void fixInfoLink(IPSRequestContext req, Element navonElem,
         Map landingPageParams) throws PSNavException
   {
      log.debug("fixing Info Link");
      PSXmlTreeWalker infoWalker = new PSXmlTreeWalker(navonElem);
      Element infoElem = infoWalker.getNextElement(
            PSNavon.XML_ELEMENT_INFOLINK,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (infoElem != null)
      {
         try
         {
            String oldUrl = infoWalker.getElementData();
            log.debug("Old Url was: {}", oldUrl);
            PSMutableUrl nUrl = new PSMutableUrl(oldUrl);
            //log.debug("Base URL is:" + nUrl.getBase());
            //nUrl.dropParam("?sys_siteid"); // can't fix this now
            //PSNavUtil.logMap(nUrl.getParamMap(), "URL Params", log);
            nUrl.setParamList(landingPageParams);
            String replaceUrl = nUrl.toString();
            log.debug("New Url is: {}", replaceUrl);
            replaceNodeText(infoElem, replaceUrl);
         }
         catch (Exception e)
         {
            log.error("unexpected exception {}", e.getMessage());
            log.debug(e.getMessage(),e);
            throw new PSNavException(e);
         }

      }
      else
      {
         log.debug("Info Link not found");
      }

   }

   /**
    * Fix the image links in a Navon XML element.
    * 
    * @param req the parent request context
    * @param navonElem the Navon XML Element to fix.
    * @param landingPageParams the extra parameters to apply to the image link.
    * @throws PSNavException
    */
   private static void fixImageLinks(IPSRequestContext req, Element navonElem,
         Map landingPageParams) throws PSNavException
   {
      log.debug("fixing Images Link");
      PSXmlTreeWalker imageListWalker = new PSXmlTreeWalker(navonElem);
      Element imageListElem = imageListWalker.getNextElement(
            PSNavon.XML_ELEMENT_IMAGELIST,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (imageListElem != null)
      {
         PSXmlTreeWalker imageWalker = new PSXmlTreeWalker(imageListElem);
         Element imageElement = imageWalker.getNextElement(
               PSNavImageLink.XML_ELEM_IMAGELINK,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (imageElement != null)
         {
            try
            {
               String oldUrl = imageWalker.getElementData();
               //log.debug("Old Url was:" + oldUrl);
               PSMutableUrl nUrl = new PSMutableUrl(oldUrl);
               //log.debug("Base URL is:" + nUrl.getBase());
               //nUrl.dropParam("?sys_siteid"); // can't fix this now
               //PSNavUtil.logMap(nUrl.getParamMap(), "URL Params", log);
               nUrl.setParamList(landingPageParams);
               String replaceUrl = nUrl.toString();
               log.debug("New Url is: {}", replaceUrl);
               replaceNodeText(imageElement, replaceUrl);
            }
            catch (PSRequestParsingException e)
            {
               log.error("unexpected exception {}", e.getMessage());
               log.debug(e.getMessage(),e);
               throw new PSNavException(e);
            }
            imageElement = imageWalker.getNextElement(
                  PSNavImageLink.XML_ELEM_IMAGELINK,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }
      else
      {
         log.debug("Image List not found");
      }
   }

   /**
    * Gets the next node type. Used for determining the node type for descendent
    * nodes.
    * 
    * @param pType the node type of the parent node.
    * @return
    */
   private static String getNextType(String pType)
   {
      if (pType.equals(PSNavonType.TYPENAME_DESCENDENT))
      {
         return PSNavonType.TYPENAME_DESCENDENT;
      }
      else if (pType.equals(PSNavonType.TYPENAME_SELF))
      {
         return PSNavonType.TYPENAME_DESCENDENT;
      }
      else
      {
         return PSNavonType.TYPENAME_OTHER;
      }
   }

   /**
    * Overrides the Theme parameter
    * 
    * @param req the parent request context
    * @param doc the NavTree XML document
    * @throws PSNavException
    */
   public static void overrideTheme(IPSRequestContext req, Document doc)
         throws PSNavException
   {
      PSNavConfig config = PSNavConfig.getInstance(req);
      String themeParam = config.getNavThemeParamName();
      String parentTheme = req.getParameter(themeParam);
      log.debug(" overrideTheme - parent theme is {}", parentTheme);
      if (parentTheme == null || parentTheme.trim().length() == 0)
      {
         parentTheme = getThemeFromQuery(req);
         req.setParameter(themeParam, parentTheme);
      }
      if (parentTheme != null && parentTheme.trim().length() > 0)
      {
         log.debug("Overriding Nav Theme {}", parentTheme);
         Element rootElem = doc.getDocumentElement();
         rootElem.setAttribute(PSNavTree.XML_ATTR_THEME, parentTheme);
      }

   }

   /**
    * Overrides the theme based on an external query. This query is used in
    * special implementations where the theme needs to be determined based on
    * the folder or other criteria.
    * 
    * @param req the parent request context.
    * @return the theme name.
    * @throws PSNavException
    */
   private static String getThemeFromQuery(IPSRequestContext req)
         throws PSNavException
   {
      log.debug("Getting theme from query");
      try
      {
         IPSInternalRequest irq = req.getInternalRequest(NAV_THEME_QUERY,
               new HashMap(), true);
         if (irq == null)
         {
            throw new PSNavException("Application not found " + NAV_THEME_QUERY);
         }
         Document result = irq.getResultDoc();
         PSXmlTreeWalker walk = new PSXmlTreeWalker(result.getDocumentElement());
         String themedata = walk.getElementData("./nav_theme", false);
         log.debug("Theme data is {}", themedata);
         return themedata;

      }
      catch (PSNavException pxne)
      {
         log.error(pxne.getMessage());
         log.debug(pxne.getMessage(),pxne);
         throw (PSNavException) pxne.fillInStackTrace();
      }
      catch (Exception ex)
      {
         log.error(ex.getMessage());
         log.debug(ex.getMessage(),ex);
         throw new PSNavException(ex);
      }
   }

   /**
    * Builds the initial landing page parameter map. This map always contains
    * the context, siteid and session id. If the parent request contains
    * <code>sys_comand=editrc</code> (which implies that this page is in
    * active assembly mode) then the <code>relateditemid</code> parameter will
    * also be added.
    * 
    * @param req the parent request context
    * @return a new map of landing page parameters.
    */
   public static Map getLandingPageMap(IPSRequestContext req)
   {
      Map landingPageParams = new HashMap();
      PSNavUtil
            .copyParam(req, landingPageParams, IPSHtmlParameters.SYS_CONTEXT);
      PSNavUtil.copyParam(req, landingPageParams, IPSHtmlParameters.SYS_SITEID);
      PSNavUtil.copyParam(req, landingPageParams,
            IPSHtmlParameters.SYS_AUTHTYPE);
      landingPageParams.put(IPSHtmlParameters.SYS_SESSIONID, req
            .getSecurityToken().getUserSessionId());
      String cmd = req.getParameter(IPSHtmlParameters.SYS_COMMAND);

      if (cmd != null)
      {
         landingPageParams.put(IPSHtmlParameters.SYS_COMMAND, cmd);
         if (cmd.equalsIgnoreCase("editrc"))
         {
            log.debug("adding relateditemid");
            landingPageParams.put("relateditemid", req
                  .getParameter(IPSHtmlParameters.SYS_CONTENTID));
         }
      }

      PSNavUtil.logMap(landingPageParams, "Landing Page Map", log);
      return landingPageParams;
   }

   /**
    * Replaces all text nodes underneath a specified element. This affects Text,
    * Comment, Entity Reference and CDATA nodes. Attributes are not affected.
    * 
    * @param elem the element to replace.
    * @param value the new text value of the element.
    */
   public static void replaceNodeText(Element elem, String value)
   {
      Document parentDoc = elem.getOwnerDocument();
      Node textNode = parentDoc.createTextNode(value);
      NodeList nlist = elem.getChildNodes();
      for (int i = 0; i < nlist.getLength(); i++)
      {
         Node child = nlist.item(i);
         switch (child.getNodeType())
         {
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
            case Node.COMMENT_NODE :
            case Node.ENTITY_REFERENCE_NODE :
               elem.removeChild(child);
               otherwise :
               //nothing
               continue;
         }
      }
      elem.appendChild(textNode);

   }

   /**
    * Name of the query to use for Nav Theme overrides.
    */
   public static final String NAV_THEME_QUERY = 
      "../rxs_navSupport/rxnavtheme.xml";

   /**
    * Logger for these classes.
    */
   private static final Logger log = LogManager.getLogger(PSNavXMLUtils.class);
}
