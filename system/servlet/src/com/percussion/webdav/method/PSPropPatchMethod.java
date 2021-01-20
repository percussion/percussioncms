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
package com.percussion.webdav.method;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * This class implements the PROPPATCH WebDAV method. Current implementaion
 * does not allow set or remove any properties.
 */
public class PSPropPatchMethod extends PSWebdavMethod
{
   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSPropPatchMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest()
   protected void parseRequest() throws PSWebdavException
   {
      try
      {
         Document doc = getRequestDocument();
         Element root = doc.getDocumentElement();
         if (root == null)
         {
            throw new PSWebdavException(
               IPSWebdavErrors.XML_INVALID_FORMAT,
               E_PROPERTYUPDATE,
               PSWebdavStatus.SC_BAD_REQUEST);
         }
         PSXMLDomUtil.checkNode(root, E_PROPERTYUPDATE);
         
         Element childEl = PSXMLDomUtil.getFirstElementChild(root);
         if (childEl == null)
         {
            throw new PSWebdavException(
               IPSWebdavErrors.XML_ELEMENT_CANNOT_BE_EMPTY,
               E_PROPERTYUPDATE,
               PSWebdavStatus.SC_BAD_REQUEST);
         }

         while (childEl != null)
         {         
            String nodeName = PSXMLDomUtil.getUnqualifiedNodeName(childEl);
            if (nodeName.equalsIgnoreCase(E_SET)
               || nodeName.equalsIgnoreCase(E_REMOVE))
            {
               Element propEl =
                  PSXMLDomUtil.getFirstElementChild(childEl, E_PROP);
               getRequestProperties(propEl);
            }
            else
            {
               throw new PSWebdavException(
                  IPSWebdavErrors.XML_INVALID_FORMAT,
                  E_PROPERTYUPDATE,
                  PSWebdavStatus.SC_BAD_REQUEST);
            }
            childEl = PSXMLDomUtil.getNextElementSibling(childEl);            
         }
      }
      catch (PSWebdavException we)
      {
         throw we;
      }
      catch (PSUnknownNodeTypeException ue)
      {
         throw new PSWebdavException(ue, PSWebdavStatus.SC_BAD_REQUEST);
      }
   }
   
   /**
    * Fetch the requested properties from the given "prop" element. 
    * The list of properties will be added to the <code>m_propList</code>.
    * 
    * @param propEl The "set" or "remove" element, assume not <code>null</code>.
    */
   private void getRequestProperties(Element propEl)
   {
      Element propertyEl = PSXMLDomUtil.getFirstElementChild(propEl);
      while (propertyEl != null)
      {
         m_propList.add(propertyEl);
         propertyEl = PSXMLDomUtil.getNextElementSibling(propertyEl);
      }
   }
   
   // Implements PSWebdavMethod.processRequest()
   protected void processRequest() throws PSWebdavException, IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = createWebdavRootElement(doc, E_MULTISTATUS);
      
      // create RESPONSE element
      Element responseEl = createWebdavElement(doc, E_RESPONSE);
      root.appendChild(responseEl);
      
      // create HREF (child) element
      Element hrefEl = createWebdavElement(doc, E_HREF);
      hrefEl.appendChild(doc.createTextNode(getRequest().getRequestURI()));
      responseEl.appendChild(hrefEl);
      
      // create PROPSTAT elements
      Iterator propList = m_propList.iterator();
      while (propList.hasNext())
      {
         Element reqPropEl = (Element) propList.next();
         Element propstatEl = getPropstat(reqPropEl, doc);
         responseEl.appendChild(propstatEl);
      }
      
      setResponse(
         doc.getDocumentElement(),
         PSWebdavStatus.SC_MULTI_STATUS);
   }
   
   /**
    * Creates the 'propstat' element for the given request property.
    * 
    * @param reqPropEl The request property element, assume not 
    *    <code>null</code>.
    * 
    * @param doc The document that is used to create the 'propstat' element,
    *    assume not <code>null</code>.
    * 
    * @return The created element, never <code>null</code>.
    */
   private Element getPropstat(Element reqPropEl, Document doc)
   {
      Element propstatEl = createWebdavElement(doc, E_PROPSTAT);
      
      // creates the "prop" element, which contains the EMPTY 
      // requested property element
      Element propEl = createWebdavElement(doc, E_PROP);
      
      // get the requested property element
      Element propertyEl = null;
      String nsURI = reqPropEl.getNamespaceURI();
      if (nsURI != null)
         propertyEl = doc.createElementNS(nsURI, reqPropEl.getLocalName());
      else
         propertyEl = doc.createElement(reqPropEl.getNodeName());
      
      propEl.appendChild(propertyEl);
      
      // creates the status element
      Element statusEl = createWebdavElement(doc, E_STATUS);
      statusEl.appendChild(
         doc.createTextNode(
            "HTTP/1.1 "
               + PSWebdavStatus.SC_FORBIDDEN
               + " "
               + PSWebdavStatus.getStatusText(PSWebdavStatus.SC_FORBIDDEN)));
               
      // finish up the "propstat" with its child element
      propstatEl.appendChild(propEl);
      propstatEl.appendChild(statusEl);
      
      return propstatEl;
   }
   
   /**
    * A list of requested properties. It is a list of zero or more 
    * <code>Element</code> objects. Set by <code>parseRequest()</code>.
    */
   private List m_propList = new ArrayList();
}
