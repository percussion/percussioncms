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
package com.percussion.cms.objectstore;

import com.percussion.cms.objectstore.ws.PSRemoteFolderProcessor;
import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Object store implementation for the <code>CloneSiteFolderRequest</code> 
 * object as defined in schema sys_FolderParameters.xsd.
 */
public class PSCloneSiteFolderRequest extends PSComponent
{
   /**
    * Construct a new clone site folder request object.
    * 
    * @param source the source folder locator to be cloned, not
    *    <code>null</code>.
    * @param target the target folder locator into which the source will be 
    *    cloned, not <code>null</code>.
    * @param options the cloning options, not <code>null</code>.
    */
   public PSCloneSiteFolderRequest(PSLocator source, PSLocator target, 
      PSCloningOptions options)
   {
      if (source == null)
         throw new IllegalArgumentException("source cannot be null");
      
      if (target == null)
         throw new IllegalArgumentException("target cannot be null");
      
      if (options == null)
         throw new IllegalArgumentException("options cannot be null");
      
      m_source = source;
      m_target = target;
      m_options = options;
   }
   
   /**
    * Constructs the clond site fodler request from its XML representation.
    * 
    * @see IPSComponent#fromXml(Element, IPSDocument, List) for parameter
    *    descriptions.
    */
   public PSCloneSiteFolderRequest(Element source, IPSDocument parent,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      fromXml(source, parent, parentComponents);
   }
   
   /**
    * Get the source folder locator.
    * 
    * @return the source folder locator, never <code>null</code>.
    */
   public PSLocator getSource()
   {
      return m_source;
   }
   
   /**
    * Get the target folder locator.
    * 
    * @return the target folder locator, never <code>null</code>.
    */
   public PSLocator getTarget()
   {
      return m_target;
   }
   
   /**
    * Get the cloning options.
    * 
    * @return the cloning options, never <code>null</code>.
    */
   public PSCloningOptions getOptions()
   {
      return m_options;
   }
   
   /* (non-Javadoc)
    * @see PSComponent#copyFrom(PSComponent) for documentation.
    */
   public void copyFrom(PSCloneSiteFolderRequest c)
   {
      super.copyFrom(c);
      
      m_source = c.m_source;
      m_target = c.m_target;
      m_options = c.m_options;
   }
   
   /* (non-Javadoc)
    * @see IPSComponent#clone() for documentation.
    */
   public Object clone()
   {
      PSCloneSiteFolderRequest clone = (PSCloneSiteFolderRequest) super.clone();
      clone.m_source = (PSLocator) m_source.clone();
      clone.m_target = (PSLocator) m_target.clone();
      clone.m_options = (PSCloningOptions) m_options.clone();
      
      return clone;
   }
   
   /**
    * Must be overridden to properly fulfill the contract.
    *
    * @return a value computed by adding the hash codes of all members.
    */
   public int hashCode()
   {
      int hash = m_source.hashCode() + m_target.hashCode() + 
         m_options.hashCode();

      return hash;
   }
   
   /**
    * Tests if the supplied object is equal to this one.
    * 
    * @param o the object to test, may be <code>null</code>.
    * @return <code>true</code> if the supplied object is equal to this one,
    *    <code>false</code> otherwise.
    */
   public boolean equals(Object o)
   {
      if (!(o instanceof PSCloneSiteFolderRequest))
         return false;

      PSCloneSiteFolderRequest t = (PSCloneSiteFolderRequest) o;
      if (!compare(t.m_source, m_source))
         return false;
      if (!compare(t.m_target, m_target))
         return false;
      if (!compare(t.m_options, m_options))
         return false;

      return true;
   }
   
   /* (non-Javadoc)
    * @see IPSComponent#fromXml(Element, IPSDocument, ArrayList)
    */
   public void fromXml(Element source, IPSDocument parent,
      List parentComponents) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker walker = new PSXmlTreeWalker(source);
      
      Element sourceElem = walker.getNextElement(true);
      if (sourceElem == null)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            SOURCE_ELEM,
            "null"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      Node current = walker.getCurrent();
      m_source = new PSLocator(walker.getNextElement(true));
      walker.setCurrent(current);

      Element targetElem = walker.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (targetElem == null)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            TARGET_ELEM,
            "null"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      current = walker.getCurrent();
      m_target = new PSLocator(walker.getNextElement(true));
      walker.setCurrent(current);
      
      Element optionsElem = walker.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (optionsElem == null)
      {
         Object[] args =
         {
            XML_NODE_NAME,
            PSCloningOptions.XML_NODE_NAME,
            "null"
         };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_options = new PSCloningOptions(optionsElem, parent, parentComponents);
   }

   /* (non-Javadoc)
    * @see IPSComponent#toXml(Document)
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);
      
      Element source = doc.createElement(SOURCE_ELEM);
      source.appendChild(m_source.toXml(doc));
      root.appendChild(source);
      
      Element target = doc.createElement(TARGET_ELEM);
      target.appendChild(m_target.toXml(doc));
      root.appendChild(target);
      
      root.appendChild(m_options.toXml(doc));
      
      return root;
   }
   
   /**
    * The source folder locator to be cloned, intialized while constructed, 
    * never <code>null</code> or changed after that.
    */
   private PSLocator m_source = null;
   
   /**
    * The target folder locator into which to clone the source, intialized 
    * while constructed, never <code>null</code> or changed after that.
    */
   private PSLocator m_target = null;
   
   /**
    * The cloning options, intialized while constructed, never <code>null</code>
    * or changed after that.
    */
   private PSCloningOptions m_options = null;
   
   /**
    * The XML document node name.
    */
   public static final String XML_NODE_NAME = 
      PSRemoteFolderProcessor.CLONE_SITEFOLDER_REQUEST;
   
   // XML element and attribute names
   private static final String SOURCE_ELEM = "Source";
   private static final String TARGET_ELEM = "Target";
}
