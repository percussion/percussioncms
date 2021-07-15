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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSAuthTypes;
import com.percussion.data.PSInternalRequestCallException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.IPSServerErrors;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * <p>
 * This exit is to be used along with the NavTreeLink extension for generation
 * of a navigation tree for a specific navon. When this extension processes
 * subsequent to NavTreeLink, it will walk down the navtree and check the
 * info-url for each "ancestor" node. If it determines that the navon has
 * content in a specified slot, it will mark the navon element with a special
 * attribute set to "yes" which can then be leveraged in XSLT processing.
 * </p>
 * 
 * <p>
 * The purpose of this extension is to allow for links in custom slots on the
 * navon variants to propogate down the ancestor tree and appear on each child
 * navon. It can of course be used for other appropriate logic built into the
 * XSL stylesheets that process the result document.
 * </p>
 * 
 * <p>
 * This should process after the NavTreeLink extension. It can be used multiple
 * times to create a marker for more than one slot.
 * </p>
 * 
 */
public class PSNavTreeSlotMarker extends PSDefaultExtension
      implements
         IPSResultDocumentProcessor
{

   /**
    * This extension never modifies the stylesheet.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * @param params the parameters passed in: <table border="1" cellpadding="3"
    *           cellspacing="0">
    *           <tr>
    *           <td>markerName</td>
    *           <td>java.lang.String</td>
    *           <td>Name of the attribute to create on appropriate navon
    *           elements</td>
    *           </tr>
    *           <tr>
    *           <td>slotName</td>
    *           <td>java.lang.String</td>
    *           <td>Name of the slot for which the exit should check for
    *           content</td>
    *           </tr>
    *           </table>
    *           <p>
    * @param request the request context object
    * @param doc the result XML document
    * @throws PSExtensionProcessingException upon any error
    *            </p>
    */
   public Document processResultDocument(final Object[] params,
         final IPSRequestContext request, final Document doc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      String markerName = null;
      String slotName = null;
      List markerNames = new ArrayList();
      List slotNames = new ArrayList();

      // first validate the exit parameters
      int paramsLength = params.length;
      if (paramsLength == 0 || paramsLength % 2 != 0)
      {
         throw new PSParameterMismatchException(MSG_INVALID_PARAMSLENGTH);
      }
      for (int i = 0; i < paramsLength; i = i + 2)
      {
         // If markerName or slotName pair is null or empty
         // break at that place.
         if (params[i] == null || params[i + 1] == null)
         {
            request
                  .printTraceMessage("Reached empty slot or marker names at i="
                        + i);
            break;
         }
         markerName = params[i].toString().trim();
         request.printTraceMessage("Marker Name is " + markerName);
         slotName = params[i + 1].toString().trim();
         request.printTraceMessage("Slot Name to check is " + slotName);
         if (markerName.length() < 1 || slotName.length() < 1)
         {
            request
                  .printTraceMessage("Reached empty slot or marker names at i="
                        + i);
            break;
         }
         markerNames.add(markerName);
         slotNames.add(slotName);
      }
      // We should at least have a pair of markerName and slotName
      if (markerNames.size() < 1 || slotNames.size() < 1)
      {
         throw new PSParameterMismatchException(MSG_INVALID_PARAMS);
      }
      try
      {

         // initialize navRoot as the document root, <navTree>
         Element navRoot = doc.getDocumentElement();
         if (navRoot != null)
         {
            PSXmlTreeWalker walker = new PSXmlTreeWalker(navRoot);
            // set navRoot to the first <navon> which should have
            // @relation="root"
            navRoot = walker.getNextElement(PSNavon.XML_ELEMENT_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
            walkTreeForContent(request, navRoot, slotNames, markerNames);
         }
      }
      catch (Exception e)
      {
         ms_log.error("Unexpected Exception " + e.getLocalizedMessage(), e);
         throw new PSExtensionProcessingException(CLASSNAME, e);
      }

      return doc;
   }

   /**
    * Walks the Nav tree looking for content
    * 
    * @param req request the request context object, assumed not
    *           <code>null</code>
    * @param node the tree node that we start walking from, assumed not
    *           <code>null</code>
    * @param slotNames list of slot names, assumed not <code>null</code>
    * @param markerNames list of marker names, assumed not <code>null</code>
    * @throws PSNavException
    */
   private void walkTreeForContent(final IPSRequestContext req,
         final Element node, final List slotNames, final List markerNames)
         throws PSNavException
   {
      String relation = node.getAttribute(PSNavon.XML_ATTR_TYPE);
      ms_log.debug("Relationship is " + relation);
      if (relation.equalsIgnoreCase(PSNavonType.TYPENAME_SELF))
      {
         checkItemForContent(req, node, slotNames, markerNames);
      }
      if (relation.equalsIgnoreCase(PSNavonType.TYPENAME_ANCESTOR)
            || relation.equalsIgnoreCase(PSNavonType.TYPENAME_ROOT))
      {
         checkItemForContent(req, node, slotNames, markerNames);
         PSXmlTreeWalker subWalker = new PSXmlTreeWalker(node);
         Element subnode = subWalker.getNextElement(PSNavon.XML_ELEMENT_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (subnode != null)
         {
            walkTreeForContent(req, subnode, slotNames, markerNames);
            subnode = subWalker.getNextElement(PSNavon.XML_ELEMENT_NAME,
                  PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }
      }
   }

   /**
    * Validates that item is or is not a content item
    * 
    * @param req request the request context object, assumed not
    *           <code>null</code>
    * @param node the item node to be validated, assumed not <code>null</code>
    * @param slotNames list of slot names, assumed not <code>null</code>
    * @param markerNames list of marker names, assumed not <code>null</code>
    * @return <code>true</code> if the this is a content item
    * @throws PSNavException upon any error
    */
   private boolean checkItemForContent(final IPSRequestContext req,
         final Element node, final List slotNames, final List markerNames)
         throws PSNavException
   {
      int authType = PSNavUtil.getAuthType(req);
      String contentId = node.getAttribute(PSNavon.XML_ATTR_CONTENTID);
      if (contentId == null || contentId.trim().length() == 0)
      {
         ms_log.warn("Navon node has no content id");
         return false;
      }
      String revision = node.getAttribute(PSNavon.XML_ATTR_REVISION);
      if (revision == null || revision.trim().length() == 0)
      {
         ms_log.warn("Navon node has no revision");
         return false;
      }
      try
      {
         PSLocator loc = new PSLocator(contentId, revision);
         Set slots = getSlotNames(req, loc, authType);
         if (!slots.isEmpty())
         {
            for (int i = 0; i < slotNames.size(); i++)
            {
               if (slots.contains(slotNames.get(i)))
               {
                  node.setAttribute((String) markerNames.get(i), "Yes");
               }
            }
         }
         return true;
      }
      catch (Exception e)
      {
         ms_log.error("Unexpected exception in " + CLASSNAME, e);
         throw new PSNavException(e);
      }
   }

   /**
    * Get a set of slotnames for given the parent locator that has content in it
    * based on the supplied authorization type value. The set is obtained by
    * executing the internal request to "sys_casSupport/casSupport_x" Rhythmyx
    * resource, where x is the authorization type value.
    * 
    * @param req request the request context object, assumed not
    *           <code>null</code>
    * @param parent Locator for the patent item, assumed not <code>null</code>
    * @param authType authorization type value one of the valid authorization
    *           type valuess. This auth type must have been implemented in
    *           sys_casSupport application. It is an error if the requested auth
    *           type is not implemented.
    * @return A set of all slotnames with content in it. Never <code>null</code>
    *         may be empty.
    * @throws PSNavException upon any error
    */
   private Set getSlotNames(final IPSRequestContext req, PSLocator parent,
         int authType) throws PSNavException
   {
      String resource = PSAuthTypes.getInstance().getResourceForAuthtype(
            "" + authType);
      if (resource == null || resource.length() == 0)
      {
         String[] args =
         {"" + authType,
               PSAuthTypes.getInstance().getConfigFile().getAbsolutePath()};
         throw new PSNavException(new PSCmsException(
               IPSCmsErrors.INVALID_AUTHTYPE, args));
      }
      Set resultSet = new HashSet();
      Map params = new HashMap();
      params.put(IPSHtmlParameters.SYS_CONTENTID, "" + parent.getId());
      params.put(IPSHtmlParameters.SYS_REVISION, "" + parent.getRevision());
      IPSInternalRequest ir = req.getInternalRequest(resource, params, true);
      if (ir == null)
      {
         Object[] args =
         {resource, "No request handler found."};
         throw new PSNavException(new PSNotFoundException(
               IPSServerErrors.MISSING_INTERNAL_REQUEST_RESOURCE, args));
      }
      try
      {
         Document doc = ir.getResultDoc();
         NodeList nl = doc.getElementsByTagName("linkurl");
         for (int i = 0; nl != null && i < nl.getLength(); i++)
         {
            Element elem = (Element) nl.item(i);
            String contentid = elem.getAttribute("contentid").trim();
            if (contentid.length() > 0)
            {
               resultSet.add(elem.getAttribute("slotname").trim());
            }
         }

      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSNavException(e);
      }

      return resultSet;
   }

   /**
    * The logger for this class
    */
   private static final Logger ms_log = LogManager
         .getLogger(PSNavTreeSlotMarker.class);

   /**
    * This class's name
    */
   private static final String CLASSNAME = PSNavTreeSlotMarker.class.getName();

   /**
    * Error message used when parameters are missing
    */
   private static final String MSG_INVALID_PARAMSLENGTH = "Invalid parameters. Expects pairs of markerName and slotNames.";

   /**
    * Error message used when parameters are missing
    */
   private static final String MSG_INVALID_PARAMS = "At least one pair of markerName and slotName is expected.";
}
