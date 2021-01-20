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
 
package com.percussion.deployer.objectstore.idtypes;

import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;

/**
 * ID Context to represent a <code>PSUrlRequest</code> object
 */
public class PSAppUrlRequestIdContext extends PSApplicationIdContext
{
   /**
    * Construct this context from the object it represents
    * 
    * @param req The url request, may not be <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>req</code> is 
    * <code>null</code>.
    */
   public PSAppUrlRequestIdContext(PSUrlRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
         
      m_name = req.getName();
      m_href = req.getHref() == null ? "" : req.getHref();
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSAppUrlRequestIdContext(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Get the name of the url request if one was supplied.
    * 
    * @return The name, may be <code>null</code>, never empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the base href of the url request if one was supplied.
    * 
    * @return The href, never <code>null</code>, may be empty.
    */
   public String getHref()
   {
      return m_href;
   }
   
   /**
    * Determine if this context represents the supplied request
    * @param req The request to check, may not be <code>null</code>.
    * 
    * @return <code>true</code> if this context represents the supplied request,
    * <code>false</code> otherwise.
    * 
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    */
   public boolean isSameRequest(PSUrlRequest req)
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      boolean isSame = true;
      
      if (req.getName() == null ^ m_name == null)
         isSame = false;
      else if (m_name != null && !m_name.equals(req.getName()))
         isSame = false;
      else if (!m_href.equals(req.getHref()))
         isSame = false;
         
      return isSame;
   }
   
   //see PSApplicationIdContext
   public String getDisplayText()
   {
      String key;
      String arg;
      if (m_name != null)
      {
         key = "appIdUrlRequestName";
         arg = m_name;
      }
      else if (m_href.trim().length() > 0)
      {
         key = "appIdUrlRequestHref";
         arg = m_href;
      }
      else
      {
         key = "appIdUrlRequest";
         arg = null;
      }
      
      String text = getBundle().getString(key);
      text = MessageFormat.format(text, new Object[] {arg});
      text = addParentDisplayText(text);
      
      return text;
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <!--
    *    PSXApplicationIdContext is a place holder for the root node of the XML
    *    representation of any class derived from PSApplicationIdContext that
    *    is this context's parent context.
    * -->
    * <pre><code>
    * &lt;!ELEMENT PSXAppUrlRequestIdContext (PSXApplicationIDContext?)>
    * &lt;!ATTLIST PSXAppUrlRequestIdContext
    *    name CDATA #IMPLIED
    *    href CDATA #IMPLIED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      if (m_name != null)
         root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_HREF, m_href);
      PSApplicationIdContext parent = getParentCtx();
      if (parent != null)
         root.appendChild(parent.toXml(doc));

      return root;
   }
   
   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      String name = sourceNode.getAttribute(XML_ATTR_NAME);
      if (name != null && name.trim().length() > 0)
         m_name = name;
      else
         m_name = null;
         
      m_href = sourceNode.getAttribute(XML_ATTR_HREF);
      if (m_href == null)
         m_href = "";
         
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element ctxEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (ctxEl != null)
         setParentCtx(PSApplicationIDContextFactory.fromXml(ctxEl));
   }
   
   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj may not be null");

      if (!(obj instanceof PSAppUrlRequestIdContext))
         throw new IllegalArgumentException("obj wrong type");

      PSAppUrlRequestIdContext other = (PSAppUrlRequestIdContext)obj;
      m_name = other.m_name;
      m_href = other.m_href;
      super.copyFrom(other);
   }
   
   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = true;

      if (!(obj instanceof PSAppUrlRequestIdContext))
         isEqual = false;
      else 
      {
         PSAppUrlRequestIdContext other = (PSAppUrlRequestIdContext)obj;
         if (m_name == null ^ other.m_name == null)
            isEqual = false;
         else if (m_name != null && !m_name.equals(other.m_name))
            isEqual = false;
         else if (!m_href.equals(other.m_href))
            isEqual = false;
         else if (!super.equals(other))
            isEqual = false;
      }

      return isEqual;
   }
   
   public int hashCode()
   {
      return m_href.hashCode() + m_name.hashCode() + super.hashCode();
   }

   /**
    * Name of the url request, may be <code>null</code>, never emtpy.
    */
   private String m_name;
   
   /**
    * Href of the url request, never <code>null</code>, may be empty.
    */
   private String m_href;
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppUrlRequestIdContext";

   // private xml constants
   private static final String XML_ATTR_NAME = "name";
   private static final String XML_ATTR_HREF = "href";
}
