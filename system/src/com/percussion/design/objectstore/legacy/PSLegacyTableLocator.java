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

package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.IPSComponent;
import com.percussion.design.objectstore.IPSDocument;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.IPSValidationContext;
import com.percussion.design.objectstore.PSComponent;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Implementation for the PSXTableLocator DTD in BasicObjects.dtd.
 */
public class PSLegacyTableLocator extends PSComponent
{

   private static final long serialVersionUID = 1L;
   
   /**
    * Creates a new table locator for the provided credentials.
    *
    * @param credentials the backend credentials, not <code>null</code>.
    */
   public PSLegacyTableLocator(PSLegacyBackEndCredential credentials)
   {
      setCredentials(credentials);
   }

   /**
    * Creates a new table locator for the provided alias. The alias must
    * exist and can either reference another table locator or backend
    * credentials.
    *
    * @param aliasRef a backend credential alias reference, not
    *    <code>null</code> or empty.
    */
   public PSLegacyTableLocator(String aliasRef)
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
   public PSLegacyTableLocator(Element sourceNode, IPSDocument parentDoc,
                         ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Needed for serialization.
    */
   protected PSLegacyTableLocator()
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
    * Get the locator database.
    *
    * @return the locators database, might be <code>null</code>.
    */
   public String getDatabase()
   {
      return m_database;
   }

   /**
    * Set a new locator database.
    *
    * @param database the new data locator database, might be <code>null</code>.
    */
   public void setDatabase(String database)
   {
      m_database = database;
   }

   /**
    * Get the data locator origin.
    *
    * @return the data locator origin, might be <code>null</code>.
    */
   public String getOrigin()
   {
      return m_origin;
   }

   /**
    * Set a new data locator origin.
    *
    * @param origin the new data locator origin, might be <code>null</code>.
    */
   public void setOrigin(String origin)
   {
      m_origin = origin;
   }

   /**
    * Get the data locator credetials.
    *
    * @return the data locator credentials, might be
    *    <code>null</code>.
    */
   public PSLegacyBackEndCredential getCredentials()
   {
      return m_credentials;
   }

   /**
    * Set new data locator credentials.
    *
    * @param credentials the new data locator credentials, not
    *    <code>null</code>.
    */
   private void setCredentials(PSLegacyBackEndCredential credentials)
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
   public void copyFrom(PSLegacyTableLocator c)
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
      m_database = c.getDatabase();
      m_origin = c.getOrigin();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSLegacyTableLocator)) return false;
      if (!super.equals(o)) return false;
      PSLegacyTableLocator that = (PSLegacyTableLocator) o;
      return Objects.equals(m_alias, that.m_alias) &&
              Objects.equals(m_credentials, that.m_credentials) &&
              Objects.equals(m_aliasRef, that.m_aliasRef) &&
              Objects.equals(m_database, that.m_database) &&
              Objects.equals(m_origin, that.m_origin);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_alias, m_credentials, m_aliasRef, m_database, m_origin);
   }

   /**
    * Compares this locator to the supplied locator to see if they define the
    * same location.  Specifically it compares the database, origin, and the
    * backend credential's driver and server.
    *
    * @param locator The locator to compare to.  May not be <code>null</code>.
    *
    * @return <code>true</code> if the database, origin, and the backend
    * credential's driver and server all match (ignoring case), <code>false
    * </code> if not.
    */
   public boolean isSameLocation(PSLegacyTableLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");

      boolean equal = true;
      PSLegacyBackEndCredential targetCred = locator.getCredentials();
      PSLegacyBackEndCredential thisCred = this.getCredentials();

      // check database
      if (((locator.getDatabase() == null ^ this.getDatabase() == null)
         || (locator.getDatabase() != null &&
            !locator.getDatabase().equalsIgnoreCase(this.getDatabase())))
         || ((locator.getOrigin() == null ^ this.getOrigin() == null)
            || (locator.getOrigin() != null &&
               !locator.getOrigin().equalsIgnoreCase(this.getOrigin())))
         || ((targetCred.getDriver() == null ^ thisCred.getDriver() == null)
            || (targetCred.getDriver() != null &&
               !targetCred.getDriver().equalsIgnoreCase(thisCred.getDriver())))
         || ((targetCred.getServer() == null ^ thisCred.getServer() == null)
            || (targetCred.getServer() != null &&
               !targetCred.getServer().equalsIgnoreCase(thisCred.getServer()))))
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
            node = tree.getNextElement(PSLegacyBackEndCredential.ms_NodeType, firstFlags);
            if (node == null)
            {
               Object[] args =
               {
                  XML_NODE_NAME,
                  PSLegacyBackEndCredential.ms_NodeType + " and " + ALIAS_ELEM,
                  "null"
               };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
            }

            m_credentials = new PSLegacyBackEndCredential(
               node, parentDoc, parentComponents);
         }

         // OPTIONAL: get the database name
         m_database = tree.getElementData(DATABASE_ELEM);

         // OPTIONAL: get the origin
         m_origin = tree.getElementData(ORIGIN_ELEM);
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

      // OPTIONAL: create the database
      if (m_database != null)
         PSXmlDocumentBuilder.addElement(doc, root, DATABASE_ELEM, m_database);

      // OPTIONAL: create the origin
      if (m_origin != null)
         PSXmlDocumentBuilder.addElement(doc, root, ORIGIN_ELEM, m_origin);

      return root;
   }

   // see IPSComponent
   public void validate(IPSValidationContext context)
      throws PSSystemValidationException
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
   private PSLegacyBackEndCredential m_credentials = null;

   /** An alias reference to an existing locator or backend credential. */
   private String m_aliasRef = null;

   /** The database for this table locator, might be <code>null</code>. */
   private String m_database = null;

   /** The origin for this data locator, might be <code>null</code>. */
   private String m_origin = null;

   /*
    * The following strings define all elements/attributes used to create the
    * XML output for this object. No Java documentation will be added to this.
    */
   private static final String ALIAS_ATTR = "alias";
   private static final String ALIAS_ELEM = "Alias";
   private static final String DATABASE_ELEM = "Database";
   private static final String ORIGIN_ELEM = "Origin";
}

