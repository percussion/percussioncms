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
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.ws.PSWSExecutableSearch;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.search.IPSSearchResultRow;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSSearchException;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSURLEncoder;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.spring.PSUrlHandlerMapping;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSDateProperty;
import com.percussion.webdav.objectstore.PSPropertyFieldNameMapping;
import com.percussion.webdav.objectstore.PSWebdavContentType;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * This class implements the PROPFIND WebDAV method.
 */
public class PSPropFindMethod extends PSWebdavMethod
{
   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSPropFindMethod(
      HttpServletRequest req,
      HttpServletResponse resp,
      PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }
   /**
    * This should only be called by the GET method in the case of the requested
    * resource is a folder, not an item. This is the replacement of 
    * {@link #parseRequest()}. The caller should call {@link #processRequest()}
    * afterwards.
    */
   protected void prepareForGetMethod()
   {
      m_depth = 0;
      m_propFindType = FIND_ALL_PROP;
   }
   // Implements PSWebdavMethod.parseRequest() 
   @Override
   protected void parseRequest() throws PSWebdavException
   {
      m_depth = getDepth();
      try
      {
         Document doc = getRequestDocument();
         Element root = doc.getDocumentElement();
         if (root == null)
         {
            m_propFindType = FIND_ALL_PROP;
            return;
         }
         PSXMLDomUtil.checkNode(root, E_PROPFIND);
         Element childEl = PSXMLDomUtil.getFirstElementChild(root);
         if (childEl == null)
         {
            m_propFindType = FIND_ALL_PROP;
         }
         else
         {
            String nodeName = PSXMLDomUtil.getUnqualifiedNodeName(childEl);
            if (nodeName.equalsIgnoreCase(E_ALLPROP))
            {
               m_propFindType = FIND_ALL_PROP;
            }
            else if (nodeName.equalsIgnoreCase(E_PROPNAME))
            {
               m_propFindType = FIND_BY_PROPERTY_NAMES;
            }
            else if (nodeName.equalsIgnoreCase(E_PROP))
            {
               m_propFindType = FIND_BY_PROPERTY;
               m_requestedPropertyNames =
                  getRequestedPropertyNames(childEl);
            }
            else // unknown element
            {
               throw new PSWebdavException(
                  IPSWebdavErrors.XML_INVALID_FORMAT,
                  E_PROPFIND,
                  PSWebdavStatus.SC_BAD_REQUEST);
            }
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
    * Get a list of property names from the given XML element. Expected DTD:
    * <pre><code>
    * &lt;!ELEMENT prop ANY>
    * </code></pre>
    *
    * @param propEl The prop element, assume not <code>null</code>.
    *
    * @return A list over zero or more <code>Element</code> objects, never
    *    <code>null</code>.
    */
   private List getRequestedPropertyNames(Element propEl)
   {
      List names = new ArrayList();
      Element childEl = PSXMLDomUtil.getFirstElementChild(propEl);
      while (childEl != null)
      {
         names.add(childEl);
         childEl = PSXMLDomUtil.getNextElementSibling(childEl);
      }
      return names;
   }
   // Implements PSWebdavMethod.processRequest() 
   @Override
   protected void processRequest() throws PSWebdavException, IOException
   {
      if (m_depth == -1 || m_propFindType == -1)
         throw new IllegalStateException("m_depth or m_propFindType must been set");
      try
      {
         List<ComponentStatus> compList;
         
         String pattern = PSUrlHandlerMapping.getUrlPath();
         if (StringUtils.isBlank(pattern))
         {
            String path = getRequest().getPathInfo();
            if (StringUtils.isBlank(path))
               m_rxVirtualPath = "/";
            else
               m_rxVirtualPath = "/" + path;
         }
         compList = getComponents();
         if (compList == null)
         {
            setResponseStatus(PSWebdavStatus.SC_NOT_FOUND);
         }
         else
         {
            if (m_propFindType != FIND_BY_PROPERTY_NAMES)
               setComponentValues(compList);
            doResponse(compList);
         }
      }
      catch (PSSearchException e)
      {
         throw new PSWebdavException(e);
      }
   }
   
   /**
    * Create the response document and set the response on the request. If
    * the request is for a collection and the uri doesn't end in a slash,
    * correct that in the response header.
    * 
    * @param compList the list of components, assumed never 
    *   <code>null</code> or empty.
    * @throws IOException
    */
   private void doResponse(List<ComponentStatus> compList) throws IOException
   {
      Document doc = getMultiStatusDocument(compList.iterator());
      // Check to see if the response is for a directory, if so make
      // sure to set the content-location header
      ComponentStatus stat = compList.get(0);
      if (stat.getContentTypeId() == 101 /* folder */)
      {
         String uri = stat.getUri();
         if (! uri.endsWith("/"))
         {
            uri = uri + "/";
            this.getResponse().setHeader("Content-Location", uri);
         }
      }
      setResponse(
         doc.getDocumentElement(),
         PSWebdavStatus.SC_MULTI_STATUS);
   }
   
   /**
    * Get immediate children for the specified folder.
    * 
    * @param folder The folder summary, assume not <code>null</code>.
    * 
    * @return A list over zero or more <code>PSComponentSummary</code>, 
    *    never <code>null</code>.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private PSComponentSummary[] getChildSummaries(PSComponentSummary folder)
      throws PSCmsException
   {
      PSRelationshipProcessorProxy proxy = getFolderProxy();
      return proxy.getChildren(
         FOLDER_TYPE,
         folder.getLocator());
   }
   /**
    * Get the components according to current request.
    *
    * @return A list of zero or more <code>ComponentStatus</code> objects.
    *    It may be <code>null</code> if fail to get the request resource, 
    *    that means the resource may not exist.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   private List getComponents() throws PSWebdavException
   {
      List compList = new ArrayList();
      // get the component from the request path
      String compPath = getRxVirtualPath();
      String uri = getRequest().getRequestURI();
      PSComponentSummary summary = null;
      ComponentStatus comp = null;
      summary = getComponentByPath(compPath);
      if (summary == null) // the resource may not exist
         return null;
         
      if (summary.isFolder())
      {
         comp = new ComponentStatus(summary, null, uri);
         if (compPath.equalsIgnoreCase(getConfig().getRootPath()))
            comp.mi_isRoot = true;
      }
      else
      {
         PSWebdavContentType ct =
            getConfig().getContentType(summary.getContentTypeId());
         if (ct != null)
            comp = new ComponentStatus(summary, ct, uri);
         else // the item is an unsupported content type
            throw new PSWebdavException(
               IPSWebdavErrors.RESOURCE_NOT_FIND,
               compPath,
               PSWebdavStatus.SC_NOT_FOUND);
      }
      compList.add(comp);
      // get the sub-component if needed      
      if (m_depth > 0 && summary.isFolder())
      {
         try
         {
            compList.addAll(getFolderChildren(m_depth, summary, uri));
         }
         catch (PSCmsException e)
         {
            throw new PSWebdavException(e);
         }
      }
      return compList;
   }
   /**
    * Get the child components in a given depth for the specified folder
    * 
    * @param depth The depth of the child components. 
    * 
    * @param folder The folder component, assume not <code>null</code>.
    * 
    * @param parentURI The parent URI, assume not <code>null</code> or empty.
    * 
    * @return A list over zero or more <code>ComponentStatus</code> objects.
    * 
    * @throws PSCmsException if an error occurs.
    */
   private List getFolderChildren(
      int depth,
      PSComponentSummary folder,
      String parentURI)
      throws PSCmsException
   {
      List compList = new ArrayList();
      if ((depth > 0) && folder.isFolder())
      {
         PSComponentSummary[] children = getChildSummaries(folder);
         ComponentStatus comp = null;
         String uri = null;
         for (int i = 0; i < children.length; i++)
         {
            if (parentURI.length() > 1
               && parentURI.charAt(parentURI.length() - 1) == '/')
               uri = parentURI + children[i].getName();
            else
               uri = parentURI + "/" + children[i].getName();
            if (children[i].isFolder())
            {
               comp = new ComponentStatus(children[i], null, uri);
               compList.add(comp);
               List grandChildren =
                  getFolderChildren(depth - 1, children[i], uri);
               compList.addAll(grandChildren);
            }
            else // only add the items its content type is specified in the 
               { // configuration file
               PSWebdavContentType ct =
                  getConfig().getContentType(children[i].getContentTypeId());
               if (ct != null)
               {
                  comp = new ComponentStatus(children[i], ct, uri);
                  compList.add(comp);
               }
            }
         }
      }
      return compList;
   }
   /**
    * Set the values for the specified component list.
    * 
    * @param comps the component list, assume it is one or more
    *    <code>ComponentStatus</code> objects.
    * 
    * @throws PSSearchException if an error occurs.
    */
   private void setComponentValues(List compList) throws PSSearchException
   {
      SearchParameters parameters = new SearchParameters(compList);

      // search the values
      PSWSExecutableSearch search =
         (PSWSExecutableSearch) PSExecutableSearchFactory
               .createExecutableSearch(
            getRemoteRequester(),
            parameters.m_fieldNames,
            parameters.m_contentIds);
      search.setContentTypeIdList(parameters.m_contentTypeIds);
      
      PSWSSearchResponse searchResult = search.executeSearch();
      // set the values to its corresponding component
      Iterator rows = searchResult.getRows();
      while (rows.hasNext())
      {
         IPSSearchResultRow row = (IPSSearchResultRow) rows.next();
         String idStr = row.getColumnValue(IPSHtmlParameters.SYS_CONTENTID);
         int contentId = Integer.parseInt(idStr);
         ComponentStatus comp = getComponent(contentId, compList.iterator());
         if (comp != null)
            comp.mi_valueMap = row.getColumnValueMap();
      }
   }
   /**
    * Get the component for the specified id from the given component list.
    * 
    * @param contentId the searched component id
    * 
    * @param comps An interator over zero or more <code>ComponentStatus</code>,
    *    assmune not <code>null</code>.
    * 
    * @return The component with the specified id. It may be <code>null</code>
    *    if the component not exist in the component list.
    */
   private ComponentStatus getComponent(int contentId, Iterator comps)
   {
      while (comps.hasNext())
      {
         ComponentStatus comp = (ComponentStatus) comps.next();
         if (comp.mi_summary.getCurrentLocator().getId() == contentId)
            return comp;
      }
      return null;
   }
   
   /**
    * Add the field names (from the specified mappings) into the specified
    * field name set.
    * 
    * @param fieldNameSet The field name set, assume not <code>null</code>.
    * 
    * @param mappings An interator over zero or more 
    *    <code>PSPropertyFieldNameMapping</code> objects.
    */
   private void addFieldNames(Set fieldNameSet, Iterator mappings)
   {
      PSPropertyFieldNameMapping mapping = null;
      while (mappings.hasNext())
      {
         mapping = (PSPropertyFieldNameMapping) mappings.next();
         String fieldName = mapping.getFieldName();
         if (!fieldName.equals(ComponentStatus.FAKE_FIELDNAME))
            fieldNameSet.add(mapping.getFieldName());
      }
   }
   
   /**
    * Get the multi-status document for the specified component status list.
    * 
    * @param compList The component status list, a list of zero or more
    *    <code>ComponentStatus</code> objects, never <code>null</code>.
    * 
    * @return The multi-status document, never <code>null</code>. It is
    *    conformed with the <code>multistatus</code> XML element specified in
    *    the WebDAV spec, [RFC 2518].
    */
   private Document getMultiStatusDocument(Iterator compList)
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = createWebdavRootElement(doc, E_MULTISTATUS);
      
      while (compList.hasNext())
      {
         ComponentStatus comp = (ComponentStatus) compList.next();
         if (comp.mi_valueMap != null)
         {
            Element responseEl = getComponentResponse(comp, doc);
            root.appendChild(responseEl);
         }
         else
         {
            Logger logger = LogManager.getLogger(getClass());
            logger.error("The value of \"mi_valueMap\" for component (contentId, name, cotnentTypeId)=(" 
                  + comp.mi_summary.getName() + ", " 
                  + comp.mi_summary.getContentId() + ", "
                  + comp.mi_summary.getContentTypeId() + ")");
         }
      }
      return doc;
   }
   /**
    * Creates the <code>response</code> XML element for the specified 
    * component
    * 
    * @param comp The component for the created XML element, assume not 
    *    <code>null</code>.
    * 
    * @param doc The document to be used to create the XML element, assume 
    *    not <code>null</code>.
    * 
    * @return The created <code>response</code> XML element, never 
    *    <code>null</code>.
    */
   private Element getComponentResponse(ComponentStatus comp, Document doc)
   {
      Element responseEl = createWebdavElement(doc, E_RESPONSE);
      // create HREF (child) element
      Element hrefEl = createWebdavElement(doc, E_HREF);
      String uri = comp.mi_uri;
      if (comp.mi_summary.isFolder()) // add trailing '/' for folder 
      {
         if (uri.length() > 1 && uri.charAt(uri.length() - 1) != '/')
            uri = uri + "/";
      }
      uri = PSURLEncoder.encodePath(uri);
      hrefEl.appendChild(doc.createTextNode(uri));
      responseEl.appendChild(hrefEl);
      if (m_propFindType == FIND_BY_PROPERTY)
      {
         List unknownProperties = new ArrayList();
         List mappings = getKnownMappings(comp, unknownProperties);
         Element propstatEl = null;
         if (!mappings.isEmpty())
         {
            propstatEl = getPropstatElement(comp, mappings.iterator(), doc);
            responseEl.appendChild(propstatEl);
         }
         if (!unknownProperties.isEmpty())
         {
            propstatEl = getUnknownPropstatElement(unknownProperties, doc);
            responseEl.appendChild(propstatEl);
         }
      }
      else
      {
         Iterator mappings = comp.getPropertyFieldMappings().iterator();
         Element propstatEl = getPropstatElement(comp, mappings, doc);
         responseEl.appendChild(propstatEl);
      }
      return responseEl;
   }
   /**
    * Creates a propstat XML element for a list of properties which are 
    * not defined for the current component.
    * 
    * @param properties The list of properties that are not defined for the 
    *    current component, a list over zero or more <code>Element</code>
    *    objects. Assume not <code>null</code>.
    * 
    * @param doc The document used to create XML element, assume not
    *    <code>null</code>.
    * 
    * @return The created XML element, never <code>null</code>.
    */
   private Element getUnknownPropstatElement(List properties, Document doc)
   {
      // create the "prop" element first
      Element propEl = createWebdavElement(doc, E_PROP);
      Iterator requestedProps = properties.iterator();
      while (requestedProps.hasNext())
      {
         Element reqPropEl = (Element) requestedProps.next();
         String nsURI = reqPropEl.getNamespaceURI();
         Element unknownPropEl = null;
         if (nsURI != null)
            unknownPropEl =
               doc.createElementNS(nsURI, reqPropEl.getLocalName());
         else
            unknownPropEl = doc.createElement(reqPropEl.getNodeName());
         propEl.appendChild(unknownPropEl);
      }
      // get the "status" element
      Element statusEl = createWebdavElement(doc, E_STATUS);
      statusEl.appendChild(
         doc.createTextNode(
            "HTTP/1.1 "
               + PSWebdavStatus.SC_NOT_FOUND
               + " "
               + PSWebdavStatus.getStatusText(PSWebdavStatus.SC_NOT_FOUND)));
      // create the "propstat" with its child element 
      Element propstatEl = createWebdavElement(doc, E_PROPSTAT);
      propstatEl.appendChild(propEl);
      propstatEl.appendChild(statusEl);
      return propstatEl;
   }
   /**
    * Get a list of mappings that are defined or configured for the specified
    * component. It also get a list of properties which are not defined for
    * the specified component, but are specified in the request as part
    * of the <code>FIND_BY_PROPERTY</code> flavor.
    * 
    * @param comp The component object, assume not <code>null</code>.
    * 
    * @param unknownProps The returned unknown property list, a list of
    *    zero or more <code>Element</code> objects. Assume it is not
    *    <code>null</code>.
    * 
    * @return A list of <code>PSPropertyFieldNameMapping</code>, never
    *    <code>null</code>.
    */
   private List getKnownMappings(ComponentStatus comp, List unknownProps)
   {
      List mappingList = new ArrayList();
      Iterator requestProps = m_requestedPropertyNames.iterator();
      PSPropertyFieldNameMapping mapping = null;
      while (requestProps.hasNext())
      {
         Element propEl = (Element) requestProps.next();
         String nsURI = propEl.getNamespaceURI();
         if (nsURI != null
            && (nsURI.equalsIgnoreCase(S_DAV_NAMESPACE)
               || nsURI.equalsIgnoreCase(RX_NAMESPACE)))
         {
            mapping = comp.getMapping(propEl.getLocalName());
            if (mapping != null)
               mappingList.add(mapping);
            else
               unknownProps.add(propEl);
         }
         else
         {
            unknownProps.add(propEl);
         }
      }
      return mappingList;
   }
   /**
    * Get the <code>propstat</code> XML element for the specified component
    * and the mapping list.
    * 
    * @param comp The component for the created XML element, assume not
    *    <code>null</code>.
    * 
    * @param mappings An iterator over zero or more 
    *    <code>PSPropertyFieldNameMapping</code> objects, assume never 
    *    <code>null</code>.
    * 
    * @param doc The document to be used to create the XML element, assume 
    *    not <code>null</code>.
    * 
    * @return The created XML element, never <code>null</code>.
    */
   private Element getPropstatElement(
      ComponentStatus comp,
      Iterator mappings,
      Document doc)
   {
      // get the "prop" element first
      Element propEl = createWebdavElement(doc, E_PROP);
      PSPropertyFieldNameMapping mapping = null;
      Element propertyEl = null;
      while (mappings.hasNext())
      {
         mapping = (PSPropertyFieldNameMapping) mappings.next();
         propertyEl = getPropertyElement(mapping, comp, doc);
         propEl.appendChild(propertyEl);
      }
      // get the "status" element
      Element statusEl = createWebdavElement(doc, E_STATUS);
      statusEl.appendChild(
         doc.createTextNode(
            "HTTP/1.1 "
               + PSWebdavStatus.SC_OK
               + " "
               + PSWebdavStatus.getStatusText(PSWebdavStatus.SC_OK)));
      // create the "propstat" with its child element 
      Element propstatEl = createWebdavElement(doc, E_PROPSTAT);
      propstatEl.appendChild(propEl);
      propstatEl.appendChild(statusEl);
      return propstatEl;
   }
   /**
    * Creates the <code>supportedlock</code> XML element. This may be used for 
    * the <code>FIND_BY_PROPERTY_NAMES</code> flavor.
    *  
    * @param doc The document that is used to create the XML element,
    *    assume it is not <code>null</code>.
    * 
    * @param comp The component for the supportedlock element, assume not
    *    <code>null</code>.
    * 
    * @return The created XML element, never <code>null</code>.
    */
   private Element createSupportedlockElement(Document doc, ComponentStatus comp)
   {
      // support exclusive / write lock only
      // shared lock is not supported
      Element supportedlockEl = createWebdavElement(doc, E_SUPPORTEDLOCK);
      if (! comp.mi_summary.isFolder())
      {
         Element exclusiveEl = createLockentyElement(doc, E_EXCLUSIVE, E_WRITE);
         supportedlockEl.appendChild(exclusiveEl);
      }
      return supportedlockEl;
   }
   /**
    * Creates the <code>lockentry</code> XML element for the specified 
    * scope and type.
    *  
    * @param doc The document that is used to create the XML element,
    *    assume it is not <code>null</code>.
    * 
    * @param scope The scope of the lockentry, assume not <code>null</code>
    *    or empty.
    * 
    * @param type The type of the lockentry, assume not <code>null</code> or 
    *    empty. 
    * 
    * @return The created XML element, never <code>null</code>.
    */
   private Element createLockentyElement(
      Document doc,
      String scope,
      String type)
   {
      Element lockentryEl = createWebdavElement(doc, E_LOCKENTRY);
      Element lockscopeEl = createWebdavElement(doc, E_LOCKSCOPE);
      lockscopeEl.appendChild(createWebdavElement(doc, scope));
      Element locktypeEl = createWebdavElement(doc, E_LOCKTYPE);
      locktypeEl.appendChild(createWebdavElement(doc, type));
      lockentryEl.appendChild(lockscopeEl);
      lockentryEl.appendChild(locktypeEl);
      return lockentryEl;
   }
   /**
    * Get the property element for the specified component.
    *  
    * @param mapping One of the property-field mapping of the component,
    *    assume not <code>null</code>.
    * 
    * @param comp The component for the created XML element, assume not 
    *    <code>null</code>.
    * 
    * @param doc The document to be used to create the XML element, assume 
    *    not <code>null</code>.
    * 
    * @return The created XML element, may be <code>null</code> if skip the
    *    supplied component.
    */
   private Element getPropertyElement(
      PSPropertyFieldNameMapping mapping,
      ComponentStatus comp,
      Document doc)
   {
      // create the property element first
      Element propertyEl = null;
      if (WEBDAV_PROPERTY_LIST.contains(mapping.getPropertyName()))
      {
         propertyEl = createWebdavElement(doc, mapping.getPropertyName());
      }
      else
      {
         String elementName =
            RX_NAMESPACE_PREFIX + ":" + mapping.getPropertyName();
         propertyEl = doc.createElementNS(RX_NAMESPACE, elementName);
      }
      if (m_propFindType != FIND_BY_PROPERTY_NAMES)
      {
         // get the value of the property, then add the value to the property
         // element if the value exists.
         String propertyName = mapping.getPropertyName();
         String value = (String) comp.mi_valueMap.get(mapping.getFieldName());
         if (propertyName.equalsIgnoreCase(P_DISPLAYNAME))
         {
            if (comp.mi_isRoot)
               value = "";
            else
               value = comp.mi_summary.getName();
            propertyEl.appendChild(doc.createCDATASection(value));
         }
         else if (propertyName.equals(E_RESOURCETYPE))
         {
            if (comp.mi_summary.isFolder())
            {
               Element collectionEl = createWebdavElement(doc, E_COLLECTION);
               propertyEl.appendChild(collectionEl);
            }
         }
         else if (propertyName.equals(P_GETLASTMODIFIED))
         {
            PSDateProperty dateProp = null;
            if (value != null && value.trim().length() != 0)
            {
               dateProp = new PSDateProperty(value);
            }
            else
            { 
               Date date = comp.mi_summary.getContentLastModifiedDate();
               if (date != null)
                  dateProp = new PSDateProperty(date);
            }
            if (dateProp != null)
            {
               propertyEl.appendChild(
                  doc.createTextNode(dateProp.getHttpDate()));
            }
         }
         else if (propertyName.equals(P_CREATIONDATE))
         {
            PSDateProperty dateProp = null;
            if (value != null && value.trim().length() != 0)
            {
               dateProp = new PSDateProperty(value);
            }
            else
            {
               Date date = comp.mi_summary.getContentCreatedDate();
               if (date != null)
                  dateProp = new PSDateProperty(date);
            }
            if (dateProp != null)
            {
               propertyEl.appendChild(
                  doc.createTextNode(dateProp.getWebdavDate()));
            }
         }
         else if (propertyName.equalsIgnoreCase(P_LOCKDISCOVERY))
         {
            String chkoutUser = comp.mi_summary.getCheckoutUserName();
               
            if (chkoutUser != null
               && chkoutUser.trim().length() > 0
               && comp.mi_valueMap != null
               && comp.mi_contentType != null)
            {
               String lockOwner =
                  (String) comp.mi_valueMap.get(
                     comp.mi_contentType.getOwnerField());
                     
               if (lockOwner != null && lockOwner.trim().length() != 0)
                  chkoutUser = lockOwner;
            }
            try
            {
               propertyEl = getLockdiscoveryElement(doc, chkoutUser);
            }
            catch (Exception e)
            {
               propertyEl = createWebdavElement(doc, E_LOCKDISCOVERY);
            }
         }
         else if (propertyName.equalsIgnoreCase(P_SUPPORTEDLOCK))
         {
            propertyEl = createSupportedlockElement(doc, comp);
         }
         else if (value != null && value.trim().length() > 0)
         {
            propertyEl.appendChild(doc.createTextNode(value));
         }
      }
      return propertyEl;
   }
   /**
    * The requested properties, a list over zero or more <code>Element</code>
    * objects. This is only used when the propfind type is
    * <code>FIND_BY_PROPERTY</code>.
    */
   private List m_requestedPropertyNames;
   /**
    * The depth of the request.
    */
   private int m_depth = -1;
   /**
    * The type of the PROPFIND method. It can only be one of the
    * <code>FIND_XXX</code> vlaues.
    */
   private int m_propFindType = -1;
   // Constants for the types of find properties
   private static final int FIND_ALL_PROP = 1;
   private static final int FIND_BY_PROPERTY = 2;
   private static final int FIND_BY_PROPERTY_NAMES = 3;

   /**
    * This class contains the status for a single property.
    */
   private static class ComponentStatus
   {
      /**
       * Constructs an instance from the given parameters.
       *
       * @param summary The component summary, assume not <code>null</code>.
       *
       * @param contentType The content type of this component, it may be
       *    <code>null</code> if the component is a folder.
       *
       * @param uri The URI for this component, assume it is not
       *    <code>null</code> or empty.
       */
      private ComponentStatus(
         PSComponentSummary summary,
         PSWebdavContentType contentType,
         String uri)
      {
         mi_summary = summary;
         mi_contentType = contentType;
         mi_uri = uri;
         // initialize the mi_mappings
         if (summary.isFolder())
         {
            mi_mappings =
               new ArrayList(PSWebdavContentType.FOLDER_MAPPING_LIST);
         }
         else
         {
            if (contentType == null)
               throw new IllegalArgumentException("contentType may not be null for item component");
            mi_mappings = new ArrayList(mi_contentType.getMappingList());
         }
         // add the properties which does not map to a field name
         PSPropertyFieldNameMapping rtMapping =
            new PSPropertyFieldNameMapping(P_RESOURCETYPE, FAKE_FIELDNAME);
         mi_mappings.add(rtMapping);
         rtMapping =
            new PSPropertyFieldNameMapping(P_SUPPORTEDLOCK, FAKE_FIELDNAME);
         mi_mappings.add(rtMapping);
         rtMapping =
            new PSPropertyFieldNameMapping(P_LOCKDISCOVERY, FAKE_FIELDNAME);
         mi_mappings.add(rtMapping);
      }
      /**
       * Get a collection of supported property-field mapping list.
       * 
       * @return A collection over zero or more 
       *    <code>PSPropertyFieldNameMapping</code> objects.
       */
      private Collection getPropertyFieldMappings()
      {
         return mi_mappings;
      }
      /**
       * Get the mapping with the specified property name.
       * 
       * @param propName The property name, assume not <code>null</code>.
       * 
       * @return The related mapping object, it may be <code>null</code> if
       *    there is no mapping object with the property.
       */
      private PSPropertyFieldNameMapping getMapping(String propName)
      {
         return (PSPropertyFieldNameMapping) getPropertyMap().get(propName);
      }
      /**
       * Get a map that maps the property name to its related 
       * <code>PSPropertyFieldNameMapping</code> object.
       * 
       * @return the map with key in <code>String</code> and value in
       *    <code>PSPropertyFieldNameMapping</code> object, never 
       *    <code>null</code>.
       */
      private Map getPropertyMap()
      {
         if (mi_propertyMap == null)
         {
            mi_propertyMap = new HashMap();
            Iterator mappings = getPropertyFieldMappings().iterator();
            PSPropertyFieldNameMapping mapping = null;
            while (mappings.hasNext())
            {
               mapping = (PSPropertyFieldNameMapping) mappings.next();
               mi_propertyMap.put(mapping.getPropertyName(), mapping);
            }
         }
         return mi_propertyMap;
      }
      /**
       * Get the content type id for this component.
       * 
       * @return the content type id, never <code>null</code>.
       */
      Long getContentTypeId()
      {
         return new Long(mi_summary.getContentTypeId());
      }
      /**
       * Get the content status uri.
       * @return the uri, as supplied to the ctor.
       */
      String getUri()
      {
         return mi_uri;
      }
      /**
       * Fake field name as a workaround to create mapping object for
       * the properties which does not have a maped Rhythmyx field. 
       */
      private final static String FAKE_FIELDNAME = "Fake field name for DAV:";
      /**
       * A collection of zero or more <code>PSPropertyFieldNameMapping</code>,
       * initialized by ctor, never <code>null</code> after that.
       */
      private Collection mi_mappings;
      /**
       * See the description for {@link #getPropertyMap()}. It is initialized
       * by {@link #getPropertyMap()}, never <code>null</code> after that.
       */
      private Map mi_propertyMap = null;
      /**
       * The URI of the "href"
       */
      private String mi_uri;
      /**
       * The content type of this component, initialized by ctor, it
       * may be <code>null</code> if the component is a folder.
       */
      private PSWebdavContentType mi_contentType;
      /**
       * The component summary, initialized by ctor, never
       * <code>null</code> after that.
       */
      private PSComponentSummary mi_summary;
      /**
       * The map that maps field name to its corresponding value. The map key
       * is the field name as <code>String</code>, the map value is the value
       * of the field as <code>String</code>.
       */
      private Map mi_valueMap = null;
      /**
       * Indicates if this is the root component. There should be only one 
       * root component. Default to <code>false</code>.
       */
      private boolean mi_isRoot = false;
   }
   
   /**
    * Utility class used to get search parameters from a list of component 
    * summary.
    */
   private class SearchParameters
   {
      /**
       * Constructs the object from the supplied component list.
       * 
       * @param compList List of <code>PSComponentSummary</code> objects,
       *    assume not <code>null</code>, may be empty.
       */
      private SearchParameters(List comps)
      {
         // sets are used to prevent examining same content type more than once
         Set contentTypeIdSet = new HashSet();
         Set fieldNameSet = new HashSet();
         List idList = new ArrayList();

         Iterator compList = comps.iterator();
         while (compList.hasNext())
         {
            ComponentStatus comp = (ComponentStatus) compList.next();
            PSComponentSummary summary = comp.mi_summary;
            idList.add(new Integer(summary.getCurrentLocator().getId()));            
            Long contentTypeId = comp.getContentTypeId();
               
            if (!contentTypeIdSet.contains(contentTypeId))
            {
               addFieldNames(fieldNameSet,
                  comp.getPropertyFieldMappings().iterator());
               
               if (!summary.isFolder())
               {
                  PSWebdavContentType contentType =
                     getConfig().getContentType(contentTypeId.intValue());
                  fieldNameSet.add(contentType.getOwnerField());
               }
            
               contentTypeIdSet.add(contentTypeId);
            }
         }
      
         // always need the contentid
         fieldNameSet.add(IPSHtmlParameters.SYS_CONTENTID);
      
         m_fieldNames = fieldNameSet;
         m_contentTypeIds = contentTypeIdSet;
         m_contentIds = idList;
      }
      
      /**
       * A list of field names (as <code>String</code> object). Init by ctor,
       * never <code>null</code>, may be empty.
       */
      private Collection m_fieldNames;
      
      /**
       * A list of content ids (as <code>Integer</code> object). Init by ctor, 
       * never <code>null</code>, may be empty.
       */
      private Collection m_contentIds;
      
      /**
       * A list of content type ids (as <code>Integer</code> object). Init by 
       * ctor, never <code>null</code>, may be empty.
       */
      private Collection m_contentTypeIds;
   }
   
}
