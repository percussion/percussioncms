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

package com.percussion.design.objectstore;

import com.percussion.error.PSException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URL;
import java.util.Objects;


/**
 * The PSCustomError class defines the page which will be returned
 * when a specific error is encountered. This allows the E2 error
 * handling to be customized on an application basis.
 * <p>
 * An HTML page or a style sheet can be specifed for return. When E2 hits
 * an error, it provdes error information in the form of an XML document.
 * To provide diagnostic information to the requestor, it may be preferrable
 * to use a style sheet which E2 can merge with the XML document to return
 * a descriptive error page.
 *
 * @see         PSErrorWebPages
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSCustomError extends PSComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSCustomError(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSCustomError()
   {
      super();
   }
   
   /**
    * Construct a custom error object.
    *
    * @param   error       the error code of the error to customize
    * @param   url         the URL of the error page to be returned
    *
    * @exception   PSIllegalArgumentException   if error or url is invalid
    */
   public PSCustomError(String error, java.net.URL url)
      throws PSIllegalArgumentException
   {
      super();
      setErrorCode(error);
      setURL(url);
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param req a valid PSCustomError. If null, a PSIllegalArgumentException is
    * thrown.
    *
    * @throws PSIllegalArgumentException if req is null
    */
   public void copyFrom(PSCustomError error) throws PSIllegalArgumentException
   {
      copyFrom((PSComponent) error);

      // assume error is in valid state
    m_url = error.m_url;
    m_error = error.m_error;
   }

   /**
    * Get the error code of the error being customized.
    *
    * @return      the error code of the error being customized.
    */
   public String getErrorCode()
   {
      return m_error;
   }
   
   /**
    * Set the error code of the error being customized.
    *
    * @param   error       the error code of the error to customize
    *
    * @exception   PSIllegalArgumentException   if error is invalid
    */
   public void setErrorCode(String error)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateErrorCode(error);
      if (ex != null)
         throw ex;

      m_error = error;
   }

   private static PSIllegalArgumentException validateErrorCode(String error)
   {
      if ((error == null) || (error.length() == 0))
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.CUSTOM_ERROR_CODE_EMPTY);

      return null;
   }
   
   /**
    * Get the URL of the error page to be returned.
    *
    * @return         the URL of the error page
    */
   public java.net.URL getURL()
   {
      return m_url;
   }
   
   /**
    * Set the URL of the error page to be returned.
    *
    * @param   url   the URL of the error page
    */
   public void setURL(java.net.URL url)
   {
      m_url = url;
   }
   
   
   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXCustomError XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXCustomError defines the page which will be returned when a
    *       specific error is encountered. This allows the E2 error handling
    *       to be customized on an application basis.
    *
    *       When E2 hits an error, it provdes error information in the form
    *       of an XML document. To provide diagnostic information to the
    *       requestor, it may be preferred to use style sheets which E2 can
    *       merge with the XML document to return a descriptive error page.
    *    --&gt;
    *    &lt;!ELEMENT PSXCustomError   (errorCode, url)&gt;
    *
    *    &lt;!--
    *       the error code of the error being customized.
    *    --&gt;
    *    &lt;!ELEMENT errorCode         (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the URL of the error page to be returned.
    *    --&gt;
    *    &lt;!ELEMENT url               (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXCustomError XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      
      PSXmlDocumentBuilder.addElement(doc, root, "errorCode", m_error);
      PSXmlDocumentBuilder.addElement(doc, root, "url", m_url.toExternalForm());
      
      return root;
   }
   
   /**
    * This method is called to populate a PSCustomError Java object
    * from a PSXCustomError XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXCustomError
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      
      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);
      
      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }
      
      try {      // get the error code
         setErrorCode(tree.getElementData("errorCode"));
      } catch (PSIllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "errorCode", e);
      }
      
      //private          URL          m_url = null;
      sTemp = tree.getElementData("url");
      if ((sTemp == null) || (sTemp.length() == 0)) {
         Object[] args = { ms_NodeType, "url", "" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      try {
         m_url = new URL(sTemp);
      } catch(java.net.MalformedURLException e) {
         Object[] args = { ms_NodeType, "url",
                           "(URL: " + sTemp + ") " + e.getMessage() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      PSException ex = validateErrorCode(m_error);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      if (m_url == null)
         cxt.validationError(this, IPSObjectStoreErrors.CUSTOM_ERROR_URL_EMPTY, null);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCustomError)) return false;
      if (!super.equals(o)) return false;
      PSCustomError that = (PSCustomError) o;
      return Objects.equals(m_url, that.m_url) &&
              Objects.equals(m_error, that.m_error);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_url, m_error);
   }

   private URL         m_url = null;
   private String      m_error;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXCustomError";
}
