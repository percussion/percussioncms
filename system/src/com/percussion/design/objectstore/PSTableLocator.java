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

import com.percussion.design.objectstore.legacy.IPSComponentConverter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation for the PSXTableLocator DTD in BasicObjects.dtd.
 */
public class PSTableLocator extends PSComponent
{
   /**
    * Creates a new table locator for the provided credentials.
    *
    * @param credentials the backend credentials, not <code>null</code>.
    */
   public PSTableLocator(PSBackEndCredential credentials)
   {
      setCredentials(credentials);
   }

   /**
    * Creates a new table locator for the provided alias. The alias must
    * exist and can either reference another table locator or backend
    * credentials.
    *
    * @param alias a backend credential alias reference, not
    *    <code>null</code> or empty.
    */
   public PSTableLocator(String aliasRef)
   {
      setAliasRef(aliasRef);
   }

   /**
    * Construct a Java object from its XML representation.
    *
    * @param sourceNode   the XML element node to construct this object from,
    *    not <code>null</code>.
    * @param parentDoc the Java object which is the parent of this object,
    *    not <code>null</code>.
    * @param parentComponents   the parent objects of this object, not
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node is not of
    *    the appropriate type
    */
   public PSTableLocator(Element sourceNode, IPSDocument parentDoc,
                         ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      // check for a converter
      IPSComponentConverter converter = getComponentConverter(this.getClass());

      /*
       * If it's a forced conversion, then just convert the source and copy from
       * what we get back
       */
      if (converter != null && converter.isForcedConversion())
      {
         PSTableLocator convertedLocator = 
            (PSTableLocator) converter.convertComponent(sourceNode);
         copyFrom(convertedLocator);
      }
      else
      {
         // try to restore from xml as usual
         try
         {
            fromXml(sourceNode, parentDoc, parentComponents);
         }
         catch (PSUnknownNodeTypeException e)
         {
            if (converter == null)
            {
               // no converter, so it's just bad XML
               throw e;
            }
            else
            {
               /*
                * if we have a converter, then try to convert. If that succeeds,
                * then copy from the converted object. Otherwise it will throw
                * an exception
                */
               PSTableLocator convertedLocator = 
                  (PSTableLocator) converter.convertComponent(sourceNode);
               copyFrom(convertedLocator);
            }
         }        
      }
   }

   /**
    * Needed for serialization.
    */
   protected PSTableLocator()
   {
   }

   /**
    * Get the data locator alias.
    *
    * @return the locators alias, might be <code>null</code> but not
    *    empty.
    */
   public String getAlias()
   {
      return m_alias;
   }

   /**
    * Set a new data locator alias.
    *
    * @param alias the new alias, migth be <code>null</code> but not empty.
    */
   public void setAlias(String alias)
   {
      if (alias != null && alias.trim().length() == 0)
         throw new IllegalArgumentException("the alias cannot be empty");

      m_alias = alias;
   }

   /**
    * Get the data locator credetials.
    *
    * @return the data locator credentials, might be
    *    <code>null</code>.
    */
   public PSBackEndCredential getCredentials()
   {
      return m_credentials;
   }

   /**
    * Set new data locator credentials.
    *
    * @param credentials the new data locator credentials, not
    *    <code>null</code>.
    */
   private void setCredentials(PSBackEndCredential credentials)
   {
      if (credentials == null)
         throw new IllegalArgumentException("credentials cannot be null");

      m_credentials = credentials;
   }

   /**
    * Get the alias reference.
    *
    * @return the alias reference, might be <code>null</code> but
    *    not empty.
    */
   public String getAliasRef()
   {
      return m_aliasRef;
   }

   /**
    * Set a new alias reference.
    *
    * @param aliasRef the new alias refernece, not <code>null</code> or empty.
    */
   private void setAliasRef(String aliasRef)
   {
      if (aliasRef == null || aliasRef.trim().length() == 0)
         throw new IllegalArgumentException("aliasRef cannot be null or empty");

      m_aliasRef = aliasRef;
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param c a valid PSTableLocator, not <code>null</code>.
    */
   public void copyFrom(PSTableLocator c)
   {
      try
      {
         super.copyFrom(c);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getMessage());
      };

      m_alias = c.getAlias();
      m_aliasRef = c.getAliasRef();
      m_credentials = c.getCredentials();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSTableLocator)) return false;
      if (!super.equals(o)) return false;
      PSTableLocator that = (PSTableLocator) o;
      return Objects.equals(m_alias, that.m_alias) &&
              Objects.equals(m_credentials, that.m_credentials) &&
              Objects.equals(m_aliasRef, that.m_aliasRef);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_alias, m_credentials, m_aliasRef);
   }

   /**
    * Compares this locator to the supplied locator to see if they define the
    * same location.  Specifically it compares the backend credential's 
    * datasource.
    *
    * @param locator The locator to compare to.  May not be <code>null</code>.
    *
    * @return <code>true</code> if it is the same location, <code>false</code>
    * if not..
    */
   public boolean isSameLocation(PSTableLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      boolean equal = true;
      PSBackEndCredential targetCred = locator.getCredentials();
      PSBackEndCredential thisCred = this.getCredentials();

      // check database
      if (((targetCred.getDataSource() == null ^ 
            thisCred.getDataSource() == null) || 
         (targetCred.getDataSource() != null && 
            !targetCred.getDataSource().equalsIgnoreCase(
               thisCred.getDataSource()))))
      {
         equal = false;
      }

      return equal;
   }


   /**
    *
    * @see IPSComponent
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_NODE_NAME);

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      String data = null;
      Element node = null;
      try
      {
         PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

         // OPTIONAL: get the href attribute
         data = tree.getElementData(ALIAS_ATTR);
         if (data != null && data.trim().length() > 0)
            m_alias = data;

         // REQUIRED: PSBackEndCredential or aliasRef
         m_aliasRef = tree.getElementData(ALIAS_ELEM);
         if (m_aliasRef == null)
         {
            node = tree.getNextElement(PSBackEndCredential.ms_NodeType, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSBackEndCredential.ms_NodeType + " and " + ALIAS_ELEM,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            m_credentials = new PSBackEndCredential(
               node, parentDoc, parentComponents);
         }
      }
      finally
      {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    *
    * @see IPSComponent
    */
   public Element toXml(Document doc)
   {
      // create root and its attributes
      Element root = doc.createElement(XML_NODE_NAME);
      if (m_alias != null)
         root.setAttribute(ALIAS_ATTR, m_alias);

      // REQUIRED: create PSBackEndCredential or alias
      if (m_credentials == null)
         PSXmlDocumentBuilder.addElement(doc, root, ALIAS_ELEM, m_aliasRef);
      else
         root.appendChild(m_credentials.toXml(doc));

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSValidationException
   {
      if (!context.startValidation(this, null))
         return;

      if (m_aliasRef == null && m_credentials == null)
         context.validationError(this,
            IPSObjectStoreErrors.INVALID_TABLE_LOCATOR, null);

      // do children
      context.pushParent(this);
      try
      {
         if (m_credentials != null)
            m_credentials.validate(context);
      }
      finally
      {
         context.popParent();
      }
   }

   /** the XML node name */
   public static final String XML_NODE_NAME = "PSXTableLocator";

   /**
    * An alias to share this object, might be <code>null</code>. If
    * <code>null</code> this locator cannot be shared.
    */
   private String m_alias = null;

   /**
    * The backend credentials for this data locator, might be
    * <code>null</code>.
    */
   private PSBackEndCredential m_credentials = null;

   /** An alias reference to an existing locator or backend credential. */
   private String m_aliasRef = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String ALIAS_ATTR = "alias";
   private static final String ALIAS_ELEM = "Alias";
}

