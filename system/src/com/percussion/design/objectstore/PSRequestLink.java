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

import java.util.Objects;


/**
 * The PSRequestLink class is used to define a link between a result page
 * and a data set. E2 can be used to generate the appopriate URL links in
 * the result page.
 * <p>
 * When defining pages with relationships to other pages, E2 can be used
 * to dynamically generate the appropriate URL to access the desired
 * data set. For instance, one data set may return a list of orders.
 * From this result page, it may be desirable to get the order details.
 * By linking the order list result page to the order detail data tank,
 * E2 can automatically generate the appropriate URL to access a
 * specific order.
 *
 * @see PSResultPage
 * @see PSResultPage#getRequestLinks
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSRequestLink extends PSComponent implements IPSResults
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
   public PSRequestLink(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSRequestLink() {
      super();
   }

   /**
    * Construct a request link object used to perform a query. The type
    * of link can be changed by using the appropriate setLinkTypeXXX method.
    *
    * @param   targetDataSet   the name of the data set to link to
    * @see      #setLinkTypeQuery
    * @see      #setLinkTypeInsert
    * @see      #setLinkTypeUpdate
    * @see      #setLinkTypeDelete
    * @see      #setLinkTypeNone
    */
   public PSRequestLink(java.lang.String targetDataSet)
      throws PSIllegalArgumentException
   {
      super();
      setTargetDataSet(targetDataSet);
   }
   
   /**
    * Is this link being used to generate query URLs?
    *
    * @return      <code>true</code> if query URLs are being generated with
    *             this link, <code>false</code> otherwise
    */
   public boolean isLinkTypeQuery()
   {
      return (RL_TYPE_QUERY == m_requestType);
   }
   
   /**
    * Set this link to generate query URLs.
    */
   public void setLinkTypeQuery()
   {
      m_requestType = RL_TYPE_QUERY;
   }
   
   /**
    * Is this link being used to generate insert URLs?
    *
    * @return      <code>true</code> if insert URLs are being generated with
    *             this link, <code>false</code> otherwise
    */
   public boolean isLinkTypeInsert()
   {
      return (RL_TYPE_INSERT == m_requestType);
   }
   
   /**
    * Set this link to generate insert URLs.
    */
   public void setLinkTypeInsert()
   {
      m_requestType = RL_TYPE_INSERT;
   }
   
   /**
    * Is this link being used to generate update URLs?
    *
    * @return      <code>true</code> if update URLs are being generated with
    *             this link, <code>false</code> otherwise
    */
   public boolean isLinkTypeUpdate()
   {
      return (RL_TYPE_UPDATE == m_requestType);
   }
   
   /**
    * Set this link to generate update URLs.
    */
   public void setLinkTypeUpdate()
   {
      m_requestType = RL_TYPE_UPDATE;
   }
   
   /**
    * Is this link being used to generate delete URLs?
    *
    * @return      <code>true</code> if delete URLs are being generated with
    *             this link, <code>false</code> otherwise
    */
   public boolean isLinkTypeDelete()
   {
      return (RL_TYPE_DELETE == m_requestType);
   }
   
   /**
    * Set this link to generate delete URLs.
    */
   public void setLinkTypeDelete()
   {
      m_requestType = RL_TYPE_DELETE;
   }
   
   /**
    * Is this link for information purposes only (not URL generation)?
    *
    * @return      <code>true</code> if URLs are not being generated with
    *             this link, <code>false</code> otherwise
    */
   public boolean isLinkTypeNone()
   {
      return (RL_TYPE_NONE == m_requestType);
   }
   
   /**
    * Disable the generation of URLs for this link.
    */
   public void setLinkTypeNone()
   {
      m_requestType = RL_TYPE_NONE;
   }
   
   /**
    * Get the name of the data set to link to.
    *
    * @return      the name of the data set to link to
    */
   public java.lang.String getTargetDataSet()
   {
      return m_dataSet;
   }
   
   /**
    * Set the name of the data set to link to.
    * <p>
    * When the application is saved, the name of the data set will be
    * checked to verify it exists in the same application as this link.
    * Linking across applications is not supported at this time.
    *
    * @param   name      the name of the data set to link to.
    *
    * @exception   PSIllegalArgumentException   if name is null or empty
    */
   public void setTargetDataSet(java.lang.String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateTargetDataSet(name);
      if (ex != null)
         throw ex;

      m_dataSet = name;
   }

   private static PSIllegalArgumentException validateTargetDataSet(String name)
   {
      if (null == name || name.length() == 0)
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.REQLINK_DATA_SET_NULL);

      return null;
   }
   
   /**
    * Get the fully qualified XML field name (element or attribute) which
    * will be used to store the link. XML field names are separated with
    * the forward slash character. For instance, the id element of the
    * following XML data is "Product/lookupUrl":
    * <p>
    * <code><Product lookupUrl="http://myserver/myurl"/></code>
    *
    * @return     the fully qualified XML field name (may be null)
    */
   public java.lang.String getXmlField()
   {
      return m_xmlField;
   }
   
   /**
    * Get the fully qualified XML field name (element or attribute) which
    * will be used to store the link. XML field names are separated with
    * the forward slash character. For instance, the id element of the
    * following XML data is "Product/lookupUrl":
    * <p>
    * <code><Product lookupUrl="http://myserver/myurl"/></code>
    * <P>
    * This is limited to 255 characters.
    *
    * @param name    the fully qualified XML field name. This may be null if
    *                generating URLs is not desired.
    *
    * @exception   PSIllegalArgumentException   if name exceeds the specified
    *                                        size limit
    */
   public void setXmlField(java.lang.String name)
      throws PSIllegalArgumentException
   {
      PSIllegalArgumentException ex = validateXmlField(name);
      if (ex != null)
         throw ex;

      m_xmlField = name;
   }
   
   private static PSIllegalArgumentException validateXmlField(String name)
   {
      if ((name != null) && (name.length() > MAX_XML_FIELD_NAME_LEN)) {
         Object[] args = { new Integer(MAX_XML_FIELD_NAME_LEN),
                           new Integer(name.length()) };
         return new PSIllegalArgumentException(
            IPSObjectStoreErrors.REQLINK_XML_FIELD_TOO_BIG, args);
      }

      return null;
   }

   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXRequestLink XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXRequestLink is used to define a link between a result page
    *       and a data set. E2 can be used to generate the appopriate URL
    *       links in the result page.
    *
    *       When defining pages with relationships to other pages, E2 can
    *       be used to dynamically generate the appropriate URL to access
    *       the desired data set. For instance, one data set may return a
    *       list of orders. From this result page, it may be desirable to
    *       get the order details. By linking the order list result page to
    *       the order detail data tank, E2 can automatically generate the
    *       appropriate URL to access a specific order.
    *    --&gt;
    *    &lt;!ELEMENT PSXRequestLink   (targetDataSet, xmlField?)&gt;
    *
    *    &lt;!--
    *       the type of link:
    *
    *       none - this link is for information purposes only (not URL
    *       generation).
    *
    *       query - this link is being used to generate query URLs.
    *
    *       insert - this link is being used to generate insert URLs.
    *
    *       update - this link is being used to generate update URLs.
    *
    *       delete - this link is being used to generate delete URLs.
    *    --&gt;
    *    &lt;!ENTITY % PSXRequestLinkType "(none, query, insert, update, delete)"&gt;
    *    &lt;!ATTLIST PSXRequestLink
    *       type                 %PSXRequestLinkType  #REQUIRED
    *       useHttpForRedirect   "(yes, no)"          #OPTIONAL
    *    &gt;
    *
    *    &lt;!--
    *       the name of the data set to link to. When the application is
    *       saved, the name of the data set will be checked to verify it
    *       exists in the same application as this link. Linking across
    *       applications is not supported at this time.
    *    --&gt;
    *    &lt;!ELEMENT targetDataSet    (#PCDATA)&gt;
    *
    *    &lt;!--
    *       the fully qualified XML field name (element or attribute)
    *       which will be used to store the link. XML field names are
    *       separated with the forward slash character. For instance,
    *       the id element of the following XML data is "Product/lookupUrl":
    *          &lt;Product lookupUrl="http://myserver/myurl"/&gt;
    *    --&gt;
    *    &lt;!ELEMENT xmlField         (#PCDATA)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXRequestLink XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      root.setAttribute("useHttpResponseForRedirect", 
         m_useHttpResponseForRedirect ? "yes" : "no");      

      //private          int         m_requestType = RL_TYPE_NONE;
      if (m_requestType == RL_TYPE_QUERY)
         root.setAttribute("type", "query");
      else if (m_requestType == RL_TYPE_INSERT)
         root.setAttribute("type", "insert");
      else if (m_requestType == RL_TYPE_UPDATE)
         root.setAttribute("type", "update");
      else if (m_requestType == RL_TYPE_DELETE)
         root.setAttribute("type", "delete");
      else
         root.setAttribute("type", "none");
      
      //private          String      m_dataSet = "";
      PSXmlDocumentBuilder.addElement(doc, root, "targetDataSet", m_dataSet);
      
      //private          String      m_xmlField = "";
      PSXmlDocumentBuilder.addElement(doc, root, "xmlField", m_xmlField);
      
      return root;
   }
   
   /**
    * Set whether or not this link should use http responses for redirection
    *
    * @param useHttpResponseForRedirect   <code>true</code> indicates to use
    * http responses for redirection <code>false</code> indicates to call the
    * linked resource's app handler directly.
    */
   public void setUseHttpResponseForRedirect(boolean useHttpResponseForRedirect)
   {
      m_useHttpResponseForRedirect = useHttpResponseForRedirect;
   }
       
   /**
    * Does this link use http responses for redirection?
    *
    * @return <code>true</code> indicates to use http responses for redirection,
    * <code>false</code> indicates to call the linked resource's app handler 
    * directly.
    */
   public boolean useHttpResponseForRedirection()
   {
      return m_useHttpResponseForRedirect;
   }

   /**
    * This method is called to populate a PSRequestLink Java object
    * from a PSXRequestLink XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRequestLink
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
      
      sTemp = tree.getElementData("useHttpResponseForRedirect");

      // If it's not "no" it's yes by default
      m_useHttpResponseForRedirect = 
         (sTemp == null || !sTemp.equalsIgnoreCase("no"));

      //private          int         m_requestType = RL_TYPE_NONE;
      sTemp = tree.getElementData("type");
      if (sTemp == null) {
         Object[] args = { ms_NodeType, "type", "" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else if (sTemp.equalsIgnoreCase("none"))
         m_requestType = RL_TYPE_NONE;
      else if (sTemp.equalsIgnoreCase("query"))
         m_requestType = RL_TYPE_QUERY;
      else if (sTemp.equalsIgnoreCase("insert"))
         m_requestType = RL_TYPE_INSERT;
      else if (sTemp.equalsIgnoreCase("update"))
         m_requestType = RL_TYPE_UPDATE;
      else if (sTemp.equalsIgnoreCase("delete"))
         m_requestType = RL_TYPE_DELETE;
      else {
         Object[] args = { ms_NodeType, "type", sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      try {      //private          String      m_dataSet = "";
         setTargetDataSet(tree.getElementData("targetDataSet"));
      } catch (PSIllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "targetDataSet", e);
      }
      
      try {      //private          String      m_xmlField = "";
         setXmlField(tree.getElementData("xmlField"));
      } catch (PSIllegalArgumentException e) {
         throw new PSUnknownNodeTypeException(ms_NodeType, "xmlField", e);
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

      PSException ex = validateTargetDataSet(m_dataSet);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      ex = validateXmlField(m_xmlField);
      if (ex != null)
         cxt.validationError(this, ex.getErrorCode(), ex.getErrorArguments());

      switch (m_requestType)
      {
      case RL_TYPE_NONE:
         // fall through
      case RL_TYPE_QUERY:
         // fall through
      case RL_TYPE_INSERT:
         // fall through
      case RL_TYPE_UPDATE:
         // fall through
      case RL_TYPE_DELETE:
         // ok
         break;
      default:
         cxt.validationError(this, 0, "Invalid request type: " + m_requestType);
      }
   }   
   
   /**
    * Return the request type.
    *
    * @return      request type
    */
   public int getType()
   {
      return m_requestType;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param req a valid PSRequestLink.
    *
    * @throws PSIllegalArgumentException if req is null
    */
   public void copyFrom(PSRequestLink link)
         throws PSIllegalArgumentException
   {
      copyFrom((PSComponent) link );

      m_xmlField = link.m_xmlField;
      m_requestType = link.m_requestType;
      m_dataSet = link.m_dataSet;
      m_useHttpResponseForRedirect = link.m_useHttpResponseForRedirect;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSRequestLink)) return false;
      if (!super.equals(o)) return false;
      PSRequestLink that = (PSRequestLink) o;
      return m_useHttpResponseForRedirect == that.m_useHttpResponseForRedirect &&
              m_requestType == that.m_requestType &&
              Objects.equals(m_xmlField, that.m_xmlField) &&
              Objects.equals(m_dataSet, that.m_dataSet);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_useHttpResponseForRedirect, m_xmlField, m_requestType, m_dataSet);
   }

   public static final int   RL_TYPE_NONE       =    0;
   public static final int   RL_TYPE_QUERY      =    1;
   public static final int   RL_TYPE_INSERT     =    2;
   public static final int   RL_TYPE_UPDATE     =    3;
   public static final int   RL_TYPE_DELETE     =    4;
   
   /**
    * If this request link is used for redirection, should it use
    * an http response to do the redirect?  <code>true</code> indicates
    * that it should, <code>false</code> indicates to use the old
    * Rhythmyx method of indirection (use the app handler directly).
    *
    * Set when fromXml() contstructor is called.  Default is <code>true</code>.
    */
   private boolean  m_useHttpResponseForRedirect = true;

   private          String      m_xmlField = "";
   private          int         m_requestType = RL_TYPE_NONE;
   private          String      m_dataSet = "";

   private static final int      MAX_XML_FIELD_NAME_LEN   = 255;


   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXRequestLink";
}

