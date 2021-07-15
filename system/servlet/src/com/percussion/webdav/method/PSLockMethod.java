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

package com.percussion.webdav.method;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.cms.objectstore.client.PSBinaryValueEx;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteException;
import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.webdav.PSWebdavServlet;
import com.percussion.webdav.PSWebdavStatus;
import com.percussion.webdav.error.IPSWebdavErrors;
import com.percussion.webdav.error.PSWebdavException;
import com.percussion.webdav.objectstore.PSWebdavContentType;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.StringReader;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This class implements the LOCK WebDAV method.
 *  
 * Note: (1) empty request body for "refresh lock" will be treated as 
 * BAD REQUEST since we only support "Infinite" timeout for the lock, there is 
 * no need to refresh a lock that has "Infinite" timeout.
 * (2) a lock on a collection (or folder) is forbidden.
 * (3) a lock on a resource which is locked by other owner is not allowed.
 * (4) a shared lock request is forbidden. 
 */
public class PSLockMethod extends PSWebdavMethod
{

   /**
    * Constructs an instance from the given parameters.
    *
    * @param req   The servlet request, it may not be <code>null</code>.
    * @param resp  The servlet response, it may not be <code>null</code>.
    * @param servlet The webdav servlet, it may not be <code>null</code>.
    */
   public PSLockMethod(HttpServletRequest req, HttpServletResponse resp,
         PSWebdavServlet servlet)
   {
      super(req, resp, servlet);
   }

   // Implements PSWebdavMethod.parseRequest()
   protected void parseRequest() throws PSWebdavException
   {
      // make sure the component exists
      String compPath = getRxVirtualPath();
      m_compSummary = getComponentByPath(compPath);
      if (m_compSummary == null)
      {
         // make sure the parent exists, it is does, then permit the lock
         // for an non-existing resource.
         m_parentSummary = getParentSummary(compPath);

      }

      m_requestOwner = getRequestOwner();
   }

   // Implements PSWebdavMethod.processRequest()
   protected void processRequest() throws PSWebdavException, IOException
   {
      if (m_compSummary == null)
      {
         createEmptyItem();
         setLockResponse();
      }
      else
      {
         if (m_compSummary.isFolder()) // lock on folder is not allowed
         {
            setResponseStatus(PSWebdavStatus.SC_FORBIDDEN);
            return;
         }

         String checkoutUser = m_compSummary.getCheckoutUserName();

         // the item has already been locked      
         if (checkoutUser != null && checkoutUser.trim().length() > 0)
         {
            String currUser = getRequest().getRemoteUser();
            if (currUser.equalsIgnoreCase(checkoutUser))
            {
               // Ignore the "If" header, which contains the lock token for now
               if (m_requestOwner == null)
               {
                  // If owner is not specified, get it from server.
                  m_requestOwner = getLockOwnerFromItem();
                  setLockResponse();
               }
               else
               // Is requested owner the same with the one in item
               {
                  // In case the WebDAV client LOCKed the item, but re-send the
                  // same LOCK again without request a "lockdiscovery" for the
                  // item. MS-Word 2002 behaves this way.
                  //
                  // However, Dreamweaver MX 2004 will never get here. It always 
                  // requests a "lockdiscovery" before it sends a LOCK request.

                  String itemOwner = getOwnerData(getLockOwnerFromItem());
                  String reqOwner = getOwnerData(m_requestOwner);
                  if (reqOwner.equals(itemOwner))
                  {
                     setLockResponse(); // locked by the same owner
                  }
                  else
                  // locked by different owner
                  {
                     setResponseStatus(PSWebdavStatus.SC_LOCKED);
                  }
               }
            }
            else
            // locked by different user
            {
               setResponseStatus(PSWebdavStatus.SC_LOCKED);
            }
         }
         else
         {
            try
            {
               PSWebdavUtils.transitionFromPublicToQuickEdit(m_compSummary,
                     getConfig(), getWorkflowInfo(), getRemoteAgent(),
                     getRequest());
            }
            catch (PSRemoteException pex)
            {
               throw new PSWebdavException(pex);
            }

            checkoutItem();
            setLockResponse();
         }
      }
   }

   /**
    * Get the owner data from the supplied XML document in text.
    * 
    * @param ownerString
    *           the XML document contains an <code>owner</code> element. It
    *           may be <code>null</code> or empty.
    * 
    * @return the data in the <code>owner</code> element. Never
    *         <code>null</code>, but may be empty. It will be empty if the
    *         supplied string is invalid XML or it does not contain an
    *         <code>owner</code> element.
    */
   private String getOwnerData(String ownerString)
   {

      if (ownerString == null)
         return "";

      String owner = "";

      try
      {
         StringReader reader = new StringReader(ownerString);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(reader, false);
         Element ownerEl = doc.createElement(E_OWNER);

         if (ownerEl != null)
         {
            // this works for both MS-Office and Photoshop, the owner data
            // is current login user-name of Windows for MS-Office, but it is
            // the app server login-name with a timestamp for Photoshop.
            //
            // However, this may not work for Dreamweaver, it contains an
            // email address within a "href" element:
            // <D:owner><D:href>yubing_chen@percussion.com</D:href></D:owner>
            // We will not handle this case for now since Dreamweaver never 
            // gets here.

            return PSXMLDomUtil.getElementData(ownerEl);
         }
      }
      catch (Exception e)
      {
         // ignore
      }
      return owner;
   }

   /**
    * Creates an empty item for the requested resource. The created item
    * will be under its parent folder and it should be in checkout state.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   private void createEmptyItem() throws PSWebdavException
   {
      try
      {
         PSRemoteAgent agent = getRemoteAgent();
         PSClientItem item = null;
         String mimetype = getMimeTypeFromReqPathInfo();
         PSWebdavContentType contentType = null;

         contentType = getConfig().getContentType(mimetype);
         item = agent.newItemDefault(String.valueOf(contentType.getId()));

         // set the display name
         String filename = getSourceFileName();
         String fieldName = contentType.getFieldName(P_DISPLAYNAME);
         PSItemField itemField = item.getFieldByName(fieldName);
         itemField.addValue(new PSTextValue(filename));

         // set the sys_title if needed
         if ((!fieldName.equalsIgnoreCase(SYS_TITLE)) && m_compSummary == null)
         {
            itemField = item.getFieldByName(SYS_TITLE);
            itemField.addValue(new PSTextValue(filename));
         }

         // set mime type field
         fieldName = contentType.getFieldName(P_GETCONTENTTYPE);
         itemField = item.getFieldByName(fieldName);
         itemField.addValue(new PSTextValue(mimetype));

         // set the content field
         itemField = item.getFieldByName(contentType.getContentField());
         if (itemField.getItemFieldMeta().isBinary())
         {
            byte[] content = new byte[0];
            itemField
                  .addValue(new PSBinaryValueEx(content, filename, mimetype));
         }
         else
         {
            itemField.addValue(new PSTextValue(""));
         }

         // set the modified date if needed
         fieldName = contentType.getFieldName(P_GETLASTMODIFIED);
         itemField = item.getFieldByName(fieldName);
         if (itemField != null)
         {
            Date currentDate = new Date(System.currentTimeMillis());
            itemField.addValue(new PSDateValue(currentDate));
         }

         // set the creation date if needed
         fieldName = contentType.getFieldName(P_CREATIONDATE);
         itemField = item.getFieldByName(fieldName);
         if (itemField != null)
         {
            Date currentDate = new Date(System.currentTimeMillis());
            itemField.addValue(new PSDateValue(currentDate));
         }

         // set the community id 
         itemField = item.getFieldByName(IPSHtmlParameters.SYS_COMMUNITYID);
         if (itemField != null)
         {
            String commId = String.valueOf(getConfig().getCommunityId());
            itemField.addValue(new PSTextValue(commId));
         }

         // set the locale
         itemField = item.getFieldByName(IPSHtmlParameters.SYS_LANG);
         if (itemField != null)
         {
            itemField.addValue(new PSTextValue(getConfig().getLocale()));
         }

         // set the owner field if needed
         if (m_requestOwner != null && m_requestOwner.trim().length() > 0)
         {
            itemField = item.getFieldByName(contentType.getOwnerField());
            itemField.addValue(new PSTextValue(m_requestOwner));
         }

         // finally, save the item
         PSLocator locator = agent.updateItem(item, false);

         PSWebdavUtils.addToParentFolder(m_parentSummary, locator,
               getRemoteRequester());
      }
      catch (PSException e)
      {
         throw new PSWebdavException(e);
      }

   }

   /**
    * Set the response after successful lock the resource.
    * 
    * @throws IOException if I/O error occurs.
    * @throws PSWebdavException if any other error occurs.
    */
   private void setLockResponse() throws PSWebdavException, IOException
   {
      getResponse().setHeader(H_LOCK_TOKEN,
            "<" + S_LOCK_TOKEN + getRxVirtualPath() + ">");
      Element responseElem = createResponseElement();
      setResponse(responseElem, PSWebdavStatus.SC_OK);
   }

   /**
    * Get the lock owner from the item that is stored on the Rhythmyx Server.
    * Assume the item has been locked by the current user.
    * 
    * @return The retrieved lock owner. It may be <code>null</code> or empty.
    * 
    * @throws PSWebdavException if an error occurs 
    */
   private String getLockOwnerFromItem() throws PSWebdavException
   {
      String lockOwner = null;

      PSRemoteAgent agent = new PSRemoteAgent(getRemoteRequester());

      try
      {
         PSClientItem item = agent.openItem(m_compSummary.getEditLocator(),
               false, false);
         long id = m_compSummary.getContentTypeId();
         PSWebdavContentType contentType = getConfig().getContentType(id);
         if (contentType == null)
         {
            PSLocator locator = m_compSummary.getEditLocator();
            Object[] args = { new Long(id), m_compSummary.getName(),
                  new Integer(locator.getId()),
                  new Integer(locator.getRevision()) };
            throw new PSWebdavException(
                  IPSWebdavErrors.CONTENTTYPE_NOT_CONFIGURED, args,
                  PSWebdavStatus.SC_PRECONDITION_FAILED);
         }
         PSItemField field = item.getFieldByName(contentType.getOwnerField());
         PSTextValue text = (PSTextValue) field.getValue();
         if (text != null)
            lockOwner = text.getValueAsString();
      }
      catch (PSRemoteException e)
      {
         throw new PSWebdavException(e);
      }

      return lockOwner;
   }

   /**
    * Checkout the requested item.
    * 
    * @throws PSWebdavException if an error occurs.
    */
   private void checkoutItem() throws PSWebdavException
   {
      try
      {
         PSRemoteAgent agent = new PSRemoteAgent(getRemoteRequester());
         agent.checkOutItem(m_compSummary.getCurrentLocator());

         // update the owner if it is specified in the request
         if (m_requestOwner != null && m_requestOwner.trim().length() > 0)
         {
            // get the edit locator after checkout
            m_compSummary = getComponentByPathRqd(getRxVirtualPath());

            // set the lock owner
            PSClientItem item = agent.openItem(m_compSummary.getEditLocator(),
                  false, false);

            PSWebdavContentType contentType = getConfig().getContentType(
                  m_compSummary.getContentTypeId());
            // ERS: getContentType may return null, you should check for this
            // before attempting a method call. And yes I know the chances of
            // this actually being null are slight but just want to 
            // point it out.
            PSItemField field = item
                  .getFieldByName(contentType.getOwnerField());
            field.addValue(new PSTextValue(m_requestOwner));

            Element itemEl = item.toMinXml(PSXmlDocumentBuilder
                  .createXmlDocument(), true, true, true, true);
            agent.updateItem(itemEl, false);
         }
      }
      catch (PSRemoteException e)
      {
         throw new PSWebdavException(e);
      }

   }

   /**
    * Create the response XML element after a successful lock.
    *  
    * @return The created response element, never <code>null</code>.
    * 
    * @throws IOException if I/O error occurs.
    * @throws PSWebdavException if any other error occurs.
    */
   private Element createResponseElement() throws PSWebdavException,
         IOException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();

      Element propEl = createWebdavRootElement(doc, E_PROP);

      String lockOwner = m_requestOwner;
      if (lockOwner == null || lockOwner.trim().length() == 0)
         lockOwner = getRequest().getRemoteUser();

      try
      {
         Element lockElem = getLockdiscoveryElement(doc, lockOwner);
         propEl.appendChild(lockElem);
         return propEl;
      }
      catch (SAXException sa)
      {
         throw new RuntimeException(sa.getLocalizedMessage());
      }
   }

   /**
    * Get the value of the <code>owner</code> element from the request body
    * 
    * @return the value of the owner element. It may be <code>null</code>
    *    if the owner is not specified in the request. 
    * 
    * @throws PSWebdavException if the request body is not conform to the
    *    <code>lockinfo</code> element that is specified in the WebDAV spec.
    */
   private String getRequestOwner() throws PSWebdavException
   {
      // <!ELEMENT lockinfo (lockscope, locktype, owner?)>
      Document doc = getRequestDocument();
      Element lockinfoEl = doc.getDocumentElement();
      if (lockinfoEl == null)
      {
         return null;
      }

      Element ownerEl = null;
      try
      {
         PSXMLDomUtil.checkNode(lockinfoEl, E_LOCKINFO);

         // some WebDAV client is in the sequence of: locktype, lockscope
         // but others is in the sequence of: lockscope, locktype 

         Element elem_1 = PSXMLDomUtil.getFirstElementChild(lockinfoEl);
         Element elem_2 = PSXMLDomUtil.getNextElementSibling(elem_1);

         Element lockscopeEl = null;
         Element locktypeEl = null;
         if (PSXMLDomUtil.getUnqualifiedNodeName(elem_1).equals(E_LOCKSCOPE))
         {
            lockscopeEl = elem_1;
            locktypeEl = elem_2;
         }
         else
         {
            lockscopeEl = elem_2;
            locktypeEl = elem_1;
         }
         PSXMLDomUtil.checkNode(lockscopeEl, E_LOCKSCOPE);
         PSXMLDomUtil.checkNode(locktypeEl, E_LOCKTYPE);

         // Make sure both lockscope and locktype exist
         // and we only support exclusive & write lock

         // check the lockscope, make sure it is "exclusive"
         Element exclusiveEl = PSXMLDomUtil.getFirstElementChild(lockscopeEl);
         if (exclusiveEl == null)
         {
            throw new PSWebdavException(
                  IPSWebdavErrors.XML_ELEMENT_CANNOT_BE_EMPTY, E_LOCKSCOPE,
                  PSWebdavStatus.SC_BAD_REQUEST);
         }
         String scopeName = PSXMLDomUtil.getUnqualifiedNodeName(exclusiveEl);
         if (!scopeName.equalsIgnoreCase(E_EXCLUSIVE))
         {
            String[] args = { scopeName, E_EXCLUSIVE };
            throw new PSWebdavException(IPSWebdavErrors.LOCKSCOPE_NOT_ALLOWED,
                  args, PSWebdavStatus.SC_FORBIDDEN);
         }

         // check the lockstype, make sure it is "write"
         Element typeEl = PSXMLDomUtil.getFirstElementChild(locktypeEl);
         if (typeEl == null)
         {
            throw new PSWebdavException(
                  IPSWebdavErrors.XML_ELEMENT_CANNOT_BE_EMPTY, E_LOCKTYPE,
                  PSWebdavStatus.SC_BAD_REQUEST);
         }

         String typeName = PSXMLDomUtil.getUnqualifiedNodeName(typeEl);
         if (!typeName.equalsIgnoreCase(E_WRITE))
         {
            String[] args = { typeName, E_WRITE };
            throw new PSWebdavException(IPSWebdavErrors.LOCKTYPE_NOT_ALLOWED,
                  args, PSWebdavStatus.SC_FORBIDDEN);
         }

         // finally, retrieve the "owner"       
         ownerEl = PSXMLDomUtil.getNextElementSibling(elem_2);
         if (ownerEl != null)
         {

            PSXMLDomUtil.checkNode(ownerEl, E_OWNER);
            return serializeElement(ownerEl);
         }
      }
      catch (PSUnknownNodeTypeException e)
      {
         PSWebdavException ex = new PSWebdavException(e);
         ex.setStatusCode(PSWebdavStatus.SC_BAD_REQUEST);
      }

      return null;
   }

   /**
    * Utility method to serialize the supplied element.
    * 
    * NOTE: With saxon transformer, this could be as simple as a call to
    * PSXmlDocumentBuilder.toString(elem). However, it does not work
    * in BEA Weblogic 8.1, it uses Xalan transformer, the toString() method
    * will not retain namespace. This method is a workaround to the issue
    * in BEA Weblogic 8.1 (or the transformer of Xalan).
    * 
    * @param elem The to be serialized element, assume not 
    *    <code>null</code>.
    * 
    * @return The serialized string which will retain namespace for various
    *    transformers, never <code>null</code> or empty.
    */
   private String serializeElement(Element elem)
   {
      String namespaceURI = elem.getNamespaceURI();
      if (namespaceURI == null || namespaceURI.trim().length() == 0)
         return PSXmlDocumentBuilder.toString(elem);

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element newElem = (Element) doc.importNode(elem, true);
      doc.appendChild(newElem);

      String prefix = elem.getPrefix();
      if (prefix == null || prefix.trim().length() == 0)
         newElem.setAttribute("xmlns", namespaceURI);
      else
         newElem.setAttribute("xmlns:" + prefix, namespaceURI);

      return PSXmlDocumentBuilder.toString(doc);
   }

   /**
    * The request owner. It is set by {@link #getRequestOwner()}, it may be  
    * <code>null</code> if the owner is not specified in the request.
    */
   private String m_requestOwner = null;

   /**
    * The component summary of the requested resource. It may be 
    * <code>null</code> if the resource does not exist.
    */
   private PSComponentSummary m_compSummary = null;

   /**
    * The parent summary of the requeted object. It is initialized by 
    * {@link #parseRequest()}, never <code>null</code> if m_compSummary is
    * <code>null</code> after that.
    */
   private PSComponentSummary m_parentSummary;

}

