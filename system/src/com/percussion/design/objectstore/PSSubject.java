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

import com.percussion.design.objectstore.server.PSDatabaseComponentLoader;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The PSSubject class defines a subject (group or user) to be stored
 * in some context, including attributes specific to that context for
 * that subject.  See {@link #toXml(Document) toXml} for a more
 * complete definition of this class.
 *
 * @since 4.0
 */
public abstract class PSSubject extends PSDatabaseComponent
{
   /* The SUBJECT_TYPE_xxx values must be flags so they can be OR'd together
      by users of this class. */
   
   /**
    * This subject represents a user.
    */
   public static final int SUBJECT_TYPE_USER = 1;

   /**
    * This subject represents a group.
    */
   public static final int SUBJECT_TYPE_GROUP = 2;


   /**
    * Retrieve a comparator to sort and store subjects by
    * their identification data.
    *
    * @return the comparator, never <code>null</code>
    */
   public static Comparator getSubjectIdentifierComparator()
   {
      return new Comparator ()
      {
         // we want to sort ignoring case, and that means a Collator
         // (may be able to improve performance by using CollatorKeys)
         private Collator m_collator = Collator.getInstance();

        

         public int compare(Object o1, Object o2)
         {
            PSSubject s1 = (PSSubject) o1;
            PSSubject s2 = (PSSubject) o2;
            
            if (s1 == null)
            {
               if (s2 == null)
                  return 0;
               else
                  return -1;
            } else if (s2 == null)
               return 1;

            m_collator.setStrength(Collator.SECONDARY);
            int ret = compare(s1, s2);
            
            // can't return 0 if case is different (or set will think its a dup)
            if (0 == ret)
            {
               m_collator.setStrength(Collator.IDENTICAL);
               ret = compare(s1, s2);
            }
            return ret;
         }
         
         private int compare(PSSubject s1, PSSubject s2)
         {
            int ret = m_collator.compare(s1.getName(), s2.getName());
            if (ret == 0)
            {
               ret = s1.m_type - s2.m_type;
            }
            return ret;
         }
      };
   }
   
   /**
    * Test if this subject is equal to the supplied subject. Two subjects are
    * equal if their names, types and attributes are equal.
    * 
    * @param obj the object to test against, may be <code>null</code>. 
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj instanceof PSSubject)
      {
         PSSubject subject = (PSSubject) obj;
         if (!m_name.equals(subject.m_name))
            return false;
         if (m_type != subject.m_type)
            return false;
         if (!m_attributes.equals(subject.m_attributes))
            return false;
            
         return true;
      }
      
      return false;
   }

   /**
    * Does the object specified match this subject?  The result is true if and 
    * only if the argument is not <code>null</code> and has the same name and
    * type as this subject.
    * Case must match.  The attributes of the two subjects are not considered.
    *
    * @param sub the subject to compare with, may be <code>null</code>.
    * @return <code>true</code> if this subject matches the specified subject,
    * <code>false</code> otherwise.
    */
   public boolean isMatch(PSSubject sub)
   {
      if (null == sub) return false;

      return ((m_name.equals(sub.m_name)) && (m_type == sub.m_type));
   }

   /**
    * Construct a complete subject.
    * @param name the subject name, not <code>null</code> or empty.
    * @param type the subject type, one of <code>SUBJECT_TYPE_USER</code> or
    *    <code>SUBJECT_TYPE_GROUP</code>.
    * @param atts the subject's attributes, may be <code>null</code> in which
    *    case an empty attribute list will be assigned.
    */
   protected PSSubject(String name, int type, PSAttributeList atts)
   {
      setName(name);
      setType(type);
      setAttributes(atts);
   }

   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from, never <code>null</code>.
    *
    * @param   parentDoc       the Java object which is the parent of this
    *                             object, may be <code>null</code>.
    *
    * @param   parentComponents  the parent objects of this object, may be
    *                               <code>null</code>.
    *
    * @throws   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    *
    * @throws  IllegalArgumentException if sourceNode is <code>null</code>.
    */
   public PSSubject(Element sourceNode,
      IPSDocument parentDoc, ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Empty constructor for creating from serialization, fromXml() etc
    */
   PSSubject()
   {
   }

   /**
    * Gets the name of the user or group associated with this subject.
    *
    * @return the name of the user or group associated with this subject,
    *    never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns a string representation of this PSSubject, using the format:
    * NAME{TYPE_NAME}.
    *
    * @return the String representation (never <code>null</code>)
    */
   @Override
   public String toString()
   {
      StringBuffer buf = new StringBuffer(getName());
      buf.append("{").append(getSubjectTypeName()).append("}");

      return buf.toString();
   }

   /**
    * Sets the name of the user or group subject.
    *
    * @param   name the name of the user or group associate
    *                with this subject. May not be <code>null</code> or empty,
    *                must be less than {@link #SUBJECT_MAX_NAME_LEN} in length.
    *
    * @throws  IllegalArgumentException if name is invalid
    */
   void setName(String name)
   {
      if ((null == name) || (name.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "Subject name must be specified.");
      }
      else if (name.length() > SUBJECT_MAX_NAME_LEN)
         throw new IllegalArgumentException(
            "Supplied subject name is too long, length is limited to " +
            SUBJECT_MAX_NAME_LEN + " characters");

      m_name = name;
   }

   /**
    * Set the attribute map for this subject.  See {@link #getAttributes()}
    * for more information about the HashMap entries.
    *
    * @param attributes the set of attributes to associate with this subject,
    *    if <code>null</code> an empty attribute list will be set.
    */
   void setAttributes(PSAttributeList attributes)
   {
      if (attributes == null)
         m_attributes = new PSAttributeList();
      else
         m_attributes = attributes;
   }

   /**
    * Get the attribute list for this subject.  Any modifications to
    * this list will affect the internal attributes of this subject.
    *
    * @return the attribute list, never <code>null</code>.
    */
   public PSAttributeList getAttributes()
   {
      return m_attributes;
   }

   /**
    * Is this a user subject?
    *
    * @return  <code>true</code> if this is a user subject,
    *          <code>false</code> otherwise
    */
   public boolean isUser()
   {
      return (SUBJECT_TYPE_USER == m_type);
   }

   /**
    * Mark this subject as defining a user.
    */
   protected void setUser()
   {
      m_type = SUBJECT_TYPE_USER;
   }

   /**
    * Is this a group subject?
    *
    * @return  <code>true</code> if this is a group subject,
    *          <code>false</code> otherwise
    */
   public boolean isGroup()
   {
      return (SUBJECT_TYPE_GROUP == m_type);
   }

   /**
    * Mark this subject as defining a group.
    */
   protected void setGroup()
   {
      m_type = SUBJECT_TYPE_GROUP;
   }

   /**
    * Override setDelete so that we can delete our attributes as well.
    */
   @Override
   protected void setDelete()
   {
      super.setDelete();
      m_attributes.setDelete();
   }

   // **************   IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXSubject XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *  &lt;!--
    *     PSXSubject defines a specific user or group entry in a specific
    *     context.  The user or group entry is defined by its type (user or
    *     group), its name, and its security provider information.  The context
    *     is determined by where the subject appears, currently it can appear as
    *     an entry on the server or as a role member.  PSXSubject is used to
    *     represent a user or group in a role, and it is also used to
    *     store user specific attributes in any context where it is needed.
    *  --&gt;
    *  &lt;!ELEMENT PSXSubject               (name, securityProviderType,
    *                                           securityProviderInstance,
    *                                           PSXAttributeList?)&gt;
    *
    *  &lt;!--
    *     The Subject type - a number one of:
    *
    *     <code>SUBJECT_TYPE_USER</code> - this subject represents a user.
    *
    *     <code>SUBJECT_TYPE_GROUP</code> - this subject represents a group.
    *  --&gt;
    *  &lt;!ENTITY % PSXSubjectType CDATA&gt;
    *  &lt;!ATTLIST PSXSubject
    *     id CDATA #REQUIRED
    *     DbComponentId CDATA #REQUIRED
    *     type    %PSXSubjectType   #REQUIRED
    *     componentState CDATA #REQUIRED
    *  &gt;
    *
    *  &lt;!--
    *     the name of the user or group associated with this subject.
    *  --&gt;
    *  &lt;!ELEMENT name                   (#PCDATA)&gt;
    *
    *  &lt;!--
    *     the type of security provider associated with this subject.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderType   (#PCDATA)&gt;
    *
    *  &lt;!--
    *     the security provider associated with this subject.
    *     Since there may be several instances of a given security
    *     provider (for example, multiple LDAP servers), it is necessary to 
    *     specify the specific instance.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderInstance  (#PCDATA)&gt;
    * </code></pre>
    *
    * @return   the newly created PSXSubject XML element node
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
    * Derived classes may need to call this in their <code>toDatabaseXml
    * </code>.
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
   protected Element toXml( Document doc, boolean includeCompChildren )
   {
      Element   root = doc.createElement(ms_NodeType);

      root.setAttribute("id", String.valueOf(m_id));
      root.setAttribute("type", String.valueOf(getType()));

      // add our db state
      addComponentState(root);

      PSXmlDocumentBuilder.addElement( doc, root, "name", m_name);

      if ( includeCompChildren )
         root.appendChild(m_attributes.toXml(doc));

      return root;
   }


   /**
    * This method is called to populate a PSSubject
    * from a PSXSubject XML element node. See the
    * {@link #toXml(Document) toXml} method
    * for a description of the XML object.
    *
    * @throws PSUnknownNodeTypeException if the XML element
    *                node is not of type PSXSubject
    */
   public void fromXml(Element sourceNode,
         @SuppressWarnings("unused") IPSDocument parentDoc,
         @SuppressWarnings("unused") ArrayList parentComponents)
                       throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      //make sure we got the Subject type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

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

      //get the Subject type element from attribute
      sTemp = tree.getElementData("type");
      if ((sTemp == null) || (sTemp.length() == 0))
      {
            Object[] args = { ms_NodeType, "type", "empty" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      } else
      {
         try
         {
            setType(Integer.valueOf(sTemp).intValue());
         } catch (NumberFormatException nfe)
         {
            Object[] args = { ms_NodeType, "type", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         } catch (IllegalArgumentException ie)
         {
            Object[] args = { ms_NodeType, "type", sTemp };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }
      }

      // Restore our db state
      getComponentState(sourceNode);

      //Read name element of the Subject
      sTemp = tree.getElementData("name");
      try
      {
         setName(sTemp);
      } catch (IllegalArgumentException e)
      {
         Object[] args = { ms_NodeType, "name", e.toString() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element attListElement =
         tree.getNextElement(PSAttributeList.ms_NodeType, firstFlags);

      // Get the attributes
      if (attListElement != null)
      {
         m_attributes = new PSAttributeList(attListElement);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param cxt The validation context.
    */
   @Override
   public void validate(IPSValidationContext cxt)
   {
      if (!cxt.startValidation(this, null))
         return;

      /*
      All validation for children is done at construct time, so they
      must be valid.
      */
   }

   /**
    * Get the subject type.
    *
    * @return the subject type, one of the SUBJECT_TYPE_XXX constant values.
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Get the string representation of the subject type.
    *
    * @return the name of the subject type, or "" if it is
    *    invalid.
    */
   public String getSubjectTypeName()
   {
      String sRet = "";
      if (m_type == SUBJECT_TYPE_USER)
         sRet = SUBJECT_TYPE_USER_STRING;
      else if (m_type == SUBJECT_TYPE_GROUP)
         sRet = SUBJECT_TYPE_GROUP_STRING;

      return sRet;
   }

   /**
    * Set the subject type (<code>SUBJECT_TYPE_XXX</code> value).
    *
    * @param type type of subject, must be <code>SUBJECT_TYPE_USER</code>
    * or <code>SUBJECT_TYPE_GROUP</code>.
    *
    * @throws IllegalArgumentException if the type is not valid.
    */
   private void setType(int type)
   {
      if ((type != SUBJECT_TYPE_USER) && (type != SUBJECT_TYPE_GROUP))
         throw new IllegalArgumentException(
               "The subject type supplied is invalid.");

      m_type = type;
   }

   /**
    * Override to set contained attributes to unchanged as well.
    *
    * @see PSDatabaseComponent#setUnchanged()
    */
   @Override
   void setUnchanged()
   {
      super.setUnchanged();
      m_attributes.setUnchanged();
   }

   /**
    * Loads this object from the supplied element using {@link
    * PSSubject#fromXml(Element, IPSDocument, ArrayList) fromXml},
    * then loads all attributes for this subject using the supplied
    * loader.  See {@link PSDatabaseComponent#fromDatabaseXml} for
    * more information.
    */
   @Override
   public void fromDatabaseXml(Element e, PSDatabaseComponentLoader cl,
      PSRelation relationContext)
      throws PSUnknownNodeTypeException, PSDatabaseComponentException
   {
      if (e == null || cl == null)
         throw new IllegalArgumentException("one or more params is null");

      // restore this object
      fromXml(e, null, null);

      // get our attribute relations
      PSRelation myCtx = null;
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

      setUnchanged();
   }


   /**
    * Returns a hash code value for the object. The hash value generated comes
    * from taking the hash code of the string representation of this object.
    * It does not factor attributes into the computation.
    *
    * @return a hash code value for this object.
    */
   @Override
   public int hashCode()
   {
      return this.toString().hashCode();
   }


   /**
    * The name of this subject, initialized at construct time,
    * never <code>null</code> or empty, initially "defaultSubjectName".
    */
   protected String m_name = "defaultSubjectName";

   /**
    * The type of this subject (<code>SUBJECT_TYPE_XXX</code>),
    * Initially <code>SUBJECT_TYPE_USER</code>.
    */
   protected int    m_type = SUBJECT_TYPE_USER;

   /**
    * The string representation of the subject type associated with
    * <code>SUBJECT_TYPE_USER</code>.
    */
   protected static final String SUBJECT_TYPE_USER_STRING  = "user";

   /**
    * The string representation of the subject type associated with
    * <code>SUBJECT_TYPE_GROUP</code>.
    */
   protected static final String SUBJECT_TYPE_GROUP_STRING = "group";

   /**
    * The maximum allowable subject name length, in characters.
    */
   public static final int      SUBJECT_MAX_NAME_LEN    = 255;

   /**
    * The maximum allowable security provider instance name length, in
    * characters.
    */
   public static final int      SUBJECT_MAX_SP_INST_LEN = 50;

   /**
    * The attributes associated with this subject, never
    * <code>null</code>.
    */
   protected PSAttributeList m_attributes = new PSAttributeList();

   /**
    * The xml element tag name associated with this class.
    */
   public static final String ms_NodeType = "PSXSubject";
}


