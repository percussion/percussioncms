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

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.server.IPSInternalRequest;
import com.percussion.server.IPSRequestContext;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Consists of a set of utility methods to build and mainipulate the NavTree XML
 * document.
 * 
 * @author DavidBenua
 *  
 */
public class PSNavTreeXMLUtils
{
   /**
    * static methods only, never constructed
    */
   private PSNavTreeXMLUtils()
   {
   }

   /**
    * Sets the value of the Nav specific variable. This is a context variable
    * whose value is copied from the context variable specified in the 
    * Nav Tree.  
    * <br>
    * The name of the source variable will taken from the lowest level
    * ancestor node with a non-null value of the selector.  This happens
    * during tree building, and the value at the root of the tree must be 
    * valid before we get to this method. 
    * <br> 
    * This method relies on the PSNavTree.XML_ATTR_VARIABLE name which 
    * must be specified in Navigation.properties.  The XML document must
    * contain an &lt;AssemblerProperties&gt; node, and this node must 
    * contain the source variable.  If either of these conditions is not 
    * met, this method returns without modifying the result document.
    * @param req the parent request context
    * @param resultDoc the result document is a standard assembler output 
    * @param navonDoc the Navigation document which represent the Nav Tree.
    * @throws PSNavException when any error occurs. 
    */
   public static void setNavVariable(IPSRequestContext req, Document resultDoc,
         Document navonDoc) throws PSNavException
   {
      log.debug("Setting Nav Variables");
      PSNavConfig config = PSNavConfig.getInstance(req);
      String varName = navonDoc.getDocumentElement().getAttribute(
            PSNavTree.XML_ATTR_VARIABLE);
      if (varName == null || varName.trim().length() == 0)
      { // nothing to do here
         log.debug("No variable name specified");
         return;
      }
      PSXmlTreeWalker walker = new PSXmlTreeWalker(resultDoc
            .getDocumentElement());
      Element assemblerInfo = walker.getNextElement(XML_ELEM_ASSEMBLERINFO,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (assemblerInfo == null)
      { // no sys_AssemblerInfo element, can't go forward
         log.warn("No Assembler Info Element, cannot add Assembler Properties");
         return;
      }
      Element assemblerProps = walker.getNextElement(
            XML_ELEM_ASSEMBLERPROPERTIES,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      if (assemblerProps == null)
      {
         log.warn("No Assembler Properties node");
         return;
      }
      copyAssemblerProperty(assemblerProps, varName, config);
   }

   /**
    * This method copies an assembler property.  The new
    * copy of the assembler property (aka Context) variable will have the 
    * name specified in the PSNavConfig.NAVTREE_VARIABLE 
    * as specified in Navigation.properties. 
    * @param assemblerProps the element that contains all assembler properties. 
    * @param srcName the name of the source property (or Context variable)
    * @param config the navigation config instance. 
    * @throws PSNavException
    */
   private static void copyAssemblerProperty(Element assemblerProps,
         String srcName, PSNavConfig config) throws PSNavException
   {
      Element originalProp = getPropertyByName(assemblerProps, srcName);
      if (originalProp == null)
      {
         log.warn("property not found {}", srcName);
         return;
      }
      String newPropName = config
            .getPropertyString(PSNavConfig.NAVTREE_VARIABLE);
      Element newProp = getPropertyByName(assemblerProps, newPropName);
      if (newProp == null)
      {
         newProp = (Element) originalProp.cloneNode(true); //deep clone
         newProp.setAttribute(XML_ATTR_NAME, newPropName);
         assemblerProps.appendChild(newProp);
      }
      else
      {
         PSXmlTreeWalker owalk = new PSXmlTreeWalker(originalProp);
         String originalValue = owalk.getElementData("Value/@current");
         log.debug("original data is {}", originalValue);
         PSXmlTreeWalker nwalk = new PSXmlTreeWalker(newProp);
         Element sValue = nwalk.getNextElement(XML_ELEM_VALUE,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (sValue == null)
         {
            throw new PSNavException(
                  "invalid document structure, no Value node");
         }
         sValue.setAttribute(XML_ATTR_CURRENT, originalValue);
      }

   }

   /**
    * gets the assembler property that matches the supplied name. Returns
    * <code>null</code> if no property with that name exists.
    * 
    * @param assemblerProps the <code>&lt;AssemblerProperties&gt;</code>
    *           element.
    * @param name the name of the element find.
    * @return the <code>&lt;Property&gt;</code> element that matches the name,
    *         or <code>null</code>
    */
   private static Element getPropertyByName(Element assemblerProps, String name)
   {
      PSXmlTreeWalker walk = new PSXmlTreeWalker(assemblerProps);
      Element property = walk.getNextElement(XML_ELEM_PROPERTY,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (property != null)
      {
         String pName = property.getAttribute(XML_ATTR_NAME);
         if (pName.equals(name))
         { // we found our property
            return property;
         }
         property = walk.getNextElement(XML_ELEM_PROPERTY,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      return null;
   }

   /**
    * Build the linkurl element and add to the supplied related content element.
    * 
    * @param relatedContent DOM element representing the related content, must
    *           not be <code>null</code>.
    * @param link Nav link object from which the URL is obtained, must not be
    *           <code>null</code>.
    * @param slot Nav slot object to set the slotid and slot name attributes the
    *           linkurl element being created, must not be <code>null</code>.
    * @param doc the parent DOM document, must not be <code>null</code>.
    * @param context context value to set the rxcontext atribute of the linkurl
    *           element being created, must not be <code>null</code>, may be
    *           empty..
    */
   public static void addLinkUrl(Element relatedContent, PSNavLink link,
         PSNavSlot slot, Document doc, String context)
   {

      Element linkElem = doc.createElement(XML_ELEM_LINK);
      Element valueElem = doc.createElement(XML_ELEM_VALUE);
      linkElem.appendChild(valueElem);
      valueElem.setAttribute(XML_ATTR_CURRENT, link.getURI());
      linkElem.setAttribute(XML_ATTR_CONTENT, String.valueOf(link.m_contentId));
      linkElem.setAttribute(XML_ATTR_SLOT_NAME, slot.getSlotName());
      linkElem.setAttribute(XML_ATTR_SLOT_ID, String.valueOf(slot.getSlotId()));
      linkElem.setAttribute(XML_ATTR_CONTEXT, context);
      linkElem.setAttribute(XML_ATTR_VARIANT_ID, String.valueOf(link
            .getVariantId()));

      if (log.isDebugEnabled())
      {
         String linkStr = PSXmlDocumentBuilder.toString(linkElem);
         log.debug("Link is {}", linkStr);
      }
      relatedContent.appendChild(linkElem);
   }

   /**
    * Make an internal request and get the XML document for the assmebler
    * properties for siteid and context from the request context. Then add the
    * assmbeler properties XML eleemnt from that document to the result
    * document.
    * 
    * @param req request context object, must not be <code>null</code>.
    * @param resultDoc result XML document wo which the assembler properties 
    * element needs to be added, must not be <code>null</code> and must have 
    * the root element.
    * @throws PSNavException
    */
   public static void AddAssemblerProperties(IPSRequestContext req,
         Document resultDoc) throws PSNavException
   {
      if (req == null)
      {
         throw new IllegalArgumentException("req must not be null");
      }
      if (resultDoc == null || resultDoc.getDocumentElement() == null)
      {
         throw new IllegalArgumentException(
               "resultDoc must not be null and must have a root eleemnt");
      }
      log.debug("adding assembler properties");
      PSNavConfig config = PSNavConfig.getInstance(req);
      Document aDoc = null;

      Element resElement = resultDoc.getDocumentElement();

      Element assemblerInfo = resultDoc.createElement(XML_ELEM_ASSEMBLERINFO);
      resElement.appendChild((Node) assemblerInfo);

      String context = req.getParameter(IPSHtmlParameters.SYS_CONTEXT);
      if (context == null || context.trim().length() == 0)
      {
         context = "0";
      }
      String site = req.getParameter(IPSHtmlParameters.SYS_SITEID);
      if (site == null || site.trim().length() == 0)
      {
         site = "0";
      }
      Map parm = new HashMap();
      parm.put(IPSHtmlParameters.SYS_CONTEXT, context);
      parm.put(IPSHtmlParameters.SYS_SITEID, site);

      IPSInternalRequest irq = req.getInternalRequest(
            QUERY_ASSEMBLER_PROPERTIES, parm, false);
      if (irq == null)
      {
         throw new PSNavException("Unable to load Assembler Properties");
      }
      try
      {
         aDoc = irq.getResultDoc();
      }
      catch (PSInternalRequestCallException e)
      {
         throw new PSNavException(e);
      }
      catch (Exception ex)
      {
         log.error("unexpected exception in {}, Error : {}", PSNavTreeXMLUtils.class, ex.getMessage());
         log.debug(ex.getMessage(),ex);
         throw new PSNavException(ex);
      }

      Element AssemblerProps = aDoc.getDocumentElement();
      if (AssemblerProps == null)
      {
         String[] msgParms =
         {site, context};
         String errMsg = MessageFormat.format(MSG_NO_PROPERTIES, msgParms);
         log.warn(errMsg);
         return;
      }

      Element resProps = (Element) resultDoc.importNode(AssemblerProps, true);
      assemblerInfo.appendChild(resProps);

      String varBaseName = resElement.getAttribute(PSNavTree.XML_ATTR_VARIABLE);
      if (varBaseName != null && varBaseName.trim().length() > 0)
      {
         copyAssemblerProperty(resProps, varBaseName, config);
      }
   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static Logger log = LogManager.getLogger(PSNavTreeXMLUtils.class);

   /**
    * Name of the Thythmyx resource to query the assembler properties.
    */
   private static final String QUERY_ASSEMBLER_PROPERTIES = 
      "sys_casSupport/AssemblerProperties.xml";

   /**
    * Error message to indicate that no assembler properties exist for a gven 
    * site and context.
    */
   private static final String MSG_NO_PROPERTIES = 
      "Unable to find AssemblerProperties for Site {0} in Context {1}";

   /**
    * String constant for "sys_AssemblerInfo" element name in the XML document.
    */
   public static final String XML_ELEM_ASSEMBLERINFO = "sys_AssemblerInfo";

   /**
    * String constant for "AssemblerProperties" element name in the XML document.
    */
   public static final String XML_ELEM_ASSEMBLERPROPERTIES = 
      "AssemblerProperties";

   /**
    * String constant for "Property" element name in the XML document.
    */
   public static final String XML_ELEM_PROPERTY = "Property";

   /**
    * String constant for "name" atribute name in the XML document.
    */
   public static final String XML_ATTR_NAME = "name";

   /**
    * String constant for "linkurl" element name in the XML document.
    */
   public static final String XML_ELEM_LINK = "linkurl";

   /**
    * String constant for "contentid" attribute name in the XML document.
    */
   public static final String XML_ATTR_CONTENT = "contentid";

   /**
    * String constant for "slotname" attribute name in the XML document.
    */
   public static final String XML_ATTR_SLOT_NAME = "slotname";

   /**
    * String constant for "slotid" attribute name in the XML document.
    */
   public static final String XML_ATTR_SLOT_ID = "slotid";

   /**
    * String constant for "variantid" attribute name in the XML document.
    */
   public static final String XML_ATTR_VARIANT_ID = "variantid";

   /**
    * String constant for "rxcontext" attribute name in the XML document.
    */
   public static final String XML_ATTR_CONTEXT = "rxcontext";

   /**
    * String constant for "sys_command" element name in the XML document.
    */
   public static final String XML_ATTR_COMMAND = "sys_command";

   /**
    * String constant for "relateditemid" attribute name in the XML document.
    */
   public static final String XML_ATTR_RELATEDITEM = "relateditemid";

   /**
    * String constant for "Value" element name in the XML document.
    */
   public static final String XML_ELEM_VALUE = "Value";

   /**
    * String constant for "current" attribute name in the XML document.
    */
   public static final String XML_ATTR_CURRENT = "current";
}