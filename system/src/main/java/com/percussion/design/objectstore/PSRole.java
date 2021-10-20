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

package com.percussion.design.objectstore;

import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

/**
 * The PSRole class provides a way to group users within E2. Roles can be
 * used within ACLs to grant a specific type of access to all members of
 * the role.
 * <p>
 * Roles are named collections containing PSRelativeSubject objects, and as such
 * are derived from the PSDatabaseComponentCollection class. Simply use the
 * methods defined in PSDatabaseComponentCollection to gain access to the
 * PSSubject objects defined for the role.
 *
 * @see com.percussion.design.objectstore.PSApplication#getRoles
 * @see com.percussion.design.objectstore.PSRelativeSubject
 * @see PSCollectionComponent
 *
 */
public class PSRole extends PSDatabaseComponent implements Comparable, 
   IPSCatalogSummary
{
   /**
    * Searches for a matching subject in this role.
    *
    * @param   sub a subject, can't be <code>null<code>.
    *
    * @return  <code>true</code> if a matching subject is found (using the
    *          {@link PSSubject#isMatch(PSSubject)} method, <code>false</code>
    *          otherwise.
    * @throws  IllegalArgumentException if <code>sub</code> is
    *          <code>null</code>.
    */
   public boolean containsCorrespondingSubject(PSSubject sub)
   {
      if (sub == null)
      {
         throw new IllegalArgumentException(
                   "subject to check for must be specified.");
      } else
      {
         for (int i = 0 ; i < m_subjects.size() ; i++)
            if (sub.isMatch((PSSubject) m_subjects.get(i)))
               return true;
      }
      return false;
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode        the XML element node to construct this
    *                              object from
    *
    * @param   parentDoc         the Java object which is the parent of this
    *                              object
    *
    * @param   parentComponents  the parent objects of this object
    *
    * @throws  PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSRole(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   public PSRole()
   {
      PSRelativeSubject subj = new PSRelativeSubject();
      m_subjects = new PSDatabaseComponentCollection(subj.getClass(),
         subj.getDatabaseAppQueryDatasetName());
   }

   /**
    * Constructs an empty role with the specified name.
    *
    * @param name The name of the role, may not be <code>null</code>, empty,
    * or greater than <code>MAX_ROLE_NAME_LEN</code> in length.
    *
    * @throws IllegalArgumentException if role name is invalid
    */
   public PSRole(String name)
   {
      this();
      setName(name);
   }

   /**
    * Override to set contained subjects and attributes to unchanged.
    *
    * @see PSDatabaseComponent#setUnchanged()
    */
   void setUnchanged()
   {
      super.setUnchanged();
      m_attributes.setUnchanged();
      m_subjects.setUnchanged();
   }

   /**
    * This method is called to create one or more Action XML elements
    * containing the data described in this object that is used to update
    * the database. The Elements are appended to the root of the passed in doc.
    * This method then calls the <code>toDatabaseXml</code> method on any of
    * this object's children.
    * <p>
    * The structure of the XML element(s) appended to the document (using a
    * root element called 'root') is:
    * <pre><code>
    * &lt;!ELEMENT root (Action*)&gt;
    * &lt;!ELEMENT Action (PSXAttribute)
    * &lt;!ATTLIST Action
    * type (INSERT | UPDATE | DELETE | UNKNOWN) #REQUIRED
    * &gt;
    * &lt;!ELEMENT PSXRole (name)&gt;
    * &lt;!ATTLIST PSXRole
    *    id CDATA #REQUIRED
    *    DbComponentId CDATA #REQUIRED
    * &gt;
    *
    * &lt;!--
    *    the name of the role. This must be unique within the application.
    *    This is limited to 50 characters.
    * --&gt;
    * &lt;!ELEMENT name          (#PCDATA)&gt;
    * </code></pre>
    *
    * @see PSDatabaseComponent#toDatabaseXml
    * @see PSDatabaseComponentCollection#toDatabaseXml
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot,
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null || relationContext == null)
         throw new IllegalArgumentException("one or more params is null");

      // if we are new, generate a new id
      if (isInsert())
         createDBComponentId();

      // Add action element to root
      Element actionElement = getActionElement(doc, actionRoot);

      if (actionElement != null)
      {
         // just toXml ourselves to this root
         actionElement.appendChild(toXml(doc, false));
      }

      relationContext.addKey(getComponentType(), m_databaseComponentId);

      // add our subjects
      PSRelation myCtx = (PSRelation)relationContext.clone();
      m_subjects.toDatabaseXml(doc, actionRoot, myCtx);

      // add our attributes
      myCtx = (PSRelation)relationContext.clone();
      m_attributes.toDatabaseXml(doc, actionRoot, myCtx);
   }


   /**
    * Loads this object from the supplied element using {@link PSRole#fromXml
    * super.fromXml}, then loads all subjects and attributes for this subject
    * using the supplied loader.  See
    * {@link PSDatabaseComponent#fromDatabaseXml} for * more information.
    */
   public void fromDatabaseXml(Element e, PSDatabaseComponentLoader cl,
      PSRelation relationContext)
      throws PSUnknownNodeTypeException, PSDatabaseComponentException
   {
      if (e == null || cl == null)
         throw new IllegalArgumentException("one or more params is null");

      // restore this object, won't have any subjects or attributes
      fromXml(e, null, null);

      // get our subjects
      PSRelation myCtx = null;
      if ( null == relationContext )
         myCtx = new PSRelation();
      else
          myCtx = (PSRelation)relationContext.clone();
      myCtx.addKey(getComponentType(), m_databaseComponentId);

      PSRelativeSubject rs = new PSRelativeSubject();
      IPSDatabaseComponent[] subjects = cl.getRelatedDatabaseComponents(
         myCtx, PSRelativeSubject.class, rs.getComponentType());
      for (int i=0; i < subjects.length; i++)
      {
         m_subjects.addFromDb((PSRelativeSubject)subjects[i]);
      }
      m_subjects.setUnchanged();

      // get our attribute relations
      if ( null == relationContext )
         myCtx = new PSRelation();
      else
         myCtx = (PSRelation)relationContext.clone();
      myCtx.addKey(getComponentType(), m_databaseComponentId);

      PSAttribute attr = new PSAttribute();
      IPSDatabaseComponent[] attributes = cl.getRelatedDatabaseComponents(
         myCtx, PSAttribute.class, attr.getComponentType());

      for (int i=0; i < attributes.length; i++)
      {
         m_attributes.addFromDb((PSAttribute)attributes[i]);
      }
      m_attributes.setUnchanged();
      setUnchanged();
   }

   /**
    * Get the role subjects.  Any changes to this collection affect the
    * collection in the role.
    *
    * @return the subjects of this role, never <code>null</code>, may
    * be empty, the contents of the collection are of type
    * <code>PSRelativeSubject</code>.
    */
   public PSDatabaseComponentCollection getSubjects()
   {
      return m_subjects;
   }

   /**
    * Get the name of the role.
    *
    * @return The name of the role, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }


   /**
    * Returns a string representation of this PSRole, its name.
    *
    * @return the name of the role (never <code>null</code> or empty)
    */
   public String toString()
   {
      return getName();
   }

      
   /**
    * Compares this PSRole to another Object, fulfilling the Comparable contract.
    * If the Object isa PSRole, this method behaves like compareTo(PSRole).
    * Otherwise, a ClassCastException is thrown.
    *
    * @param o the Object to be compared. Cannot be <code>null</code>
    * @return 0 if the argument is a PSRole with the same name as this
    *    PSRole; a value less than 0 if the argument is "greater than" this
    *    PSRole; and a value greater than 0 if the argument is "less than"
    *    this PSRole
    * @throws ClassCastException - if the argument is not a PSRole.
    */
   public int compareTo(Object o)
   {
      return compareTo( (PSRole) o);
   }
 
   /**
    * Compares this PSRole to another PSRole.  The ordering is determined
    * by a String comparison of the PSRoles' names.
    *
    * @param testRole the PSRoles to be compared. Cannot be <code>null</code>
    * @return 0 if the argument has the same name as this PSRole;
    *    a value less than 0 if the argument is "greater than" this
    *    PSRole; and a value greater than 0 if the argument is "less than"
    *    this PSRole
    */
   public int compareTo(PSRole testRole)
   {
      return ( getName() ).compareTo( testRole.getName() );
   }


   /**
    * Set the name of the role.
    *
    * @param   name The name of the role, may not be <code>null</code>, empty,
    * or greater than <code>MAX_ROLE_NAME_LEN</code> in length.
    *
    * @throws IllegalArgumentException if name is null, empty or
    *                                  exceeds the specified size limit
    */
   void setName(String name)
   {
      if ((null == name) || (name.length() == 0))
         throw new IllegalArgumentException("Role name must be specified.");
      else if (name.length() > MAX_ROLE_NAME_LEN)
         throw new IllegalArgumentException("Role name is too long.");

      m_name = name;
   }

   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXRole XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXRole provides a way to group users within E2. Roles can be
    *       used within ACLs to grant a specific type of access to all
    *       members of the role.
    *
    *       Object References:
    *
    *       PSXSubject - roles are named collections containing PSXSubject
    *       objects.
    *    --&gt;
    *    &lt;!ELEMENT PSXRole (PSXRelativeSubject*, PSXAttributeList?)&gt;
    *    &lt;!ATTLIST PSXRole
    *       id CDATA #REQUIRED
    *       DbComponentId CDATA #REQUIRED
    *       componentState CDATA #REQUIRED
    *       name CDATA #REQUIRED
    *    &gt;
    *
    *    &lt;!--
    *       The name of the role must be unique.
    *       It is limited to 50 characters.
    *    --&gt;
    * </code></pre>
    *
    * @return     the newly created PSXRole XML element node
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      return toXml( doc, true );
   }


   /**
    * This method serializes all the properties of this object, optionally
    * including those properties that are PSDatabaseComponent objects. It
    * creates a node with a name unique to this object and adds attributes
    * and child elements to this node, which is returned when complete.
    *
    * @param doc The document to which the returned element will be added.
    *    Assumed not <code>null</code>.
    *
    * @param includeCompChildren A flag to indicate whether to include
    *    properties whose data type is PSDatabaseComponent. They are included
    *    if this is <code>true</code>, otherwise they aren't. It was designed
    *    to set this to <code>true</code> when calling from <code>toXml</code>
    *    and <code>false</code> when calling from <code>toDatabaseXml</code>.
    *    The db children are left out when saving to the db to make the
    *    update work correctly.
    *
    * @return An XML element containing some or all of the properties of this
    *    node, depending on the supplied flag, never <code>null</code>.
    */
   private Element toXml( Document doc, boolean includeCompChildren )
   {
      Element root = doc.createElement(ms_NodeType);

      root.setAttribute("id", String.valueOf(m_id));

      root.setAttribute("name", m_name);

      // add our db state
      addComponentState(root);

      if ( includeCompChildren )
      {
         // add the subjects
         root.appendChild(m_subjects.toXml(doc));

         // add the attributes
         root.appendChild(m_attributes.toXml(doc));
      }

      return root;
   }


   /**
    * This method is called to populate a PSRole Java object
    * from a PSXRole XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRole
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (!ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      sTemp = tree.getElementData("name");
      try
      {
         setName(sTemp);
      } catch (IllegalArgumentException e)
      {
         Object[] args = {ms_NodeType, "name", sTemp == null ? "empty" : sTemp};

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      // Restore our db state
      getComponentState(sourceNode);

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element curNode =
         tree.getNextElement(PSDatabaseComponentCollection.ms_NodeType,
            firstFlags);
      if (curNode != null)
      {
         m_subjects.fromXml(curNode, parentDoc, parentComponents);
      }

      // Get role attributes here.
      Element attListElement =
         tree.getNextElement(PSAttributeList.ms_NodeType, nextFlags);
      if (attListElement != null)
      {
         m_attributes = new PSAttributeList(attListElement);
      }
   }

   /**
    * Get the attribute map for this role.  Any modifications to
    * this map will affect the internal attributes of this role.
    * The map will contain entries keyed by the attribute name with
    * the associated value being the value of the attribute.
    *
    * @return The attribute list, never <code>null<code>.
    */
   public PSAttributeList getAttributes()
   {
      return m_attributes;
   }

   /**
    * Override {@link PSDatabaseComponent#setDelete()} to
    * inform our children that they are deletes as well.
    */
   public void setDelete()
   {
      super.setDelete();
      m_subjects.setDelete();
      m_attributes.setDelete();
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
      super.validate(cxt);

      if (!cxt.startValidation(this, null))
         return;

      /*
      All validation for children is done at construct time, so they
      must be valid.
      */
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getDescription()
    */
   public String getDescription()
   {
      // roles don't have a description
      return null;
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.ROLE, m_id);
   }

   /* (non-Javadoc)
    * @see com.percussion.services.catalog.IPSCatalogSummary#getLabel()
    */
   public String getLabel()
   {
      return getName();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSRole))
         return false;
      
      PSRole role = (PSRole) obj;

      return new EqualsBuilder()
         .append(m_id, role.m_id)
         .append(m_name, role.m_name)
         .append(m_attributes, role.m_attributes)
         .append(m_subjects, role.m_subjects).isEquals();
   }

   @Override
   public int hashCode()
   {
      return new HashCodeBuilder()
         .append(m_id) 
         .append(m_name)
         .append(m_attributes) 
         .append(m_subjects).toHashCode(); 
   }

   /**
    * The name of the role, never <code>null</code> or empty after construction.
    */
   private String m_name = "defaultRoleName";

   /**
    * The subjects (<code>PSRelativeSubject</code> objects) that are members of
    * this role, may be empty, but never <code>null</code> after construction.
    */
   private PSDatabaseComponentCollection m_subjects;

   /**
    * The attributes associated with this role, may be empty, but never
    * <code>null</code>.
    */
   private PSAttributeList  m_attributes  = new PSAttributeList();

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType      = "PSXRole";

   /**
    * The maximum number of characters that a role's name can be.
    */
   public static final int   MAX_ROLE_NAME_LEN      = 50;
}

