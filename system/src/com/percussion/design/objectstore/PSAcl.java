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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * The PSAcl class defines an Access Control List (ACL). ACLs can be
 * associated with servers or applications.
 * <p>
 * The ACL contains one or more ACL entries (PSAclEntry). The entries define
 * the users, groups and roles who can access a resource, and the type of
 * access they have to the resource.
 *
 * @see PSApplication#getAcl
 * @see PSAclEntry
 *
 * @author        Tas Giakouminakis
 * @version   1.0
 * @since     1.0
 */
public class PSAcl extends PSComponent
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
   public PSAcl(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Construct an empty access control list (ACL).
    */
   public PSAcl()
   {
      super();
      m_entries = new PSCollection((new PSAclEntry()).getClass());
   }

   /**
    * Get the entries defined in this ACL.
    *
    * @return        a collection containing PSAclEntry objects representing
    *             the entries defined in this ACL
    *
    * @see        PSAclEntry
    */
   public com.percussion.util.PSCollection getEntries()
   {
      return m_entries;
   }

   /**
    * Overwrite the entries associated with this ACL with the
    * specified collection. If you only want to modify certain entries,
    * add a new entry, etc. use getEntries to get the existing collection
    * and modify the returned collection directly.
    * <p>
    * The PSCollection object supplied to this method will be stored
    * with the PSAcl object. Any subsequent changes made to the object
    * by the caller will also effect the ACL.
    *
    * @param     entries  the new entries to use for this ACL
    *
    * @see        PSAclEntry
    * @see        #getEntries
    */
   public void setEntries(com.percussion.util.PSCollection entries)
   {
      IllegalArgumentException ex = validateEntries(entries);
      if (ex != null)
         throw ex;

      m_entries = entries;
   }

   private static IllegalArgumentException validateEntries(
      PSCollection entries)
   {
      if (null == entries)
         return new IllegalArgumentException("acl entrylist is null");

      if   (0 == entries.size())
         return new IllegalArgumentException("acl entrylist is empty");

      if (!com.percussion.design.objectstore.PSAclEntry.class.isAssignableFrom(
         entries.getMemberClassType()))
      {
         return new IllegalArgumentException("coll bad content type, ACL Entry: " +
            entries.getMemberClassName());
      }

      // verify the entries are unique
      java.util.HashMap entryHash = new java.util.HashMap(entries.size());
      for (int i = 0; i < entries.size(); i++)
      {
         PSAclEntry entry = (PSAclEntry)entries.get(i);
         if (null != entryHash.put(entry.getName(),
               Boolean.TRUE))
         {
            // there was a duplicated entry
            return new IllegalArgumentException("acl entry list duplicate: " +
               entry.getName() );
         }
      }

      return null;
   }

   /**
    * Is maximum access granted when the user belongs to multiple
    * groups or roles?
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * When this option is enabled, the matching entry with the greatest
    * access rights is used.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    *
    * @return        <code>true</code> if this setting is in use,
    *             <code>false</code> otherwise
    */
   public boolean isAccessForMultiMembershipMaximum()
   {
      if(m_mergeAccess == MULTI_ACE_GETS_MAX)
         return true;
      return false;
   }

   /**
    * Grant maximum access when the user belongs to multiple groups or
    * roles. This is done by checking all the entries and using the one
    * with the highest access rights.
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    */
   public void setAccessForMultiMembershipMaximum()
   {
      m_mergeAccess = MULTI_ACE_GETS_MAX;
   }

   /**
    * Is minimum access granted when the user belongs to multiple
    * groups or roles?
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * When this option is enabled, the matching entry with the least
    * access rights is used.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    *
    * @return        <code>true</code> if this setting is in use,
    *             <code>false</code> otherwise
    */
   public boolean isAccessForMultiMembershipMinimum()
   {
      if(m_mergeAccess == MULTI_ACE_GETS_MIN)
         return true;
      return false;
   }

   /**
    * Grant minimum access when the user belongs to multiple groups or
    * roles. This is done by checking all the entries and using the one
    * with the lowest access rights.
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    */
   public void setAccessForMultiMembershipMinimum()
   {
      m_mergeAccess = MULTI_ACE_GETS_MIN;
   }

   /**
    * Is the combined maximum access granted when the user belongs to
    * multiple groups or roles?
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * When this option is enabled, the matching entries will be merged
    * for maximum access. If an access right is defined in any ACE, the
    * user will be granted that right.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    *
    * @return        <code>true</code> if this setting is in use,
    *             <code>false</code> otherwise
    */
   public boolean isAccessForMultiMembershipMergedMaximum()
   {
      if(m_mergeAccess == MULTI_ACE_GETS_MERGED_MAX)
         return true;
      return false;
   }

   /**
    * Grant the combined maximum access when the user belongs to multiple
    * groups or roles. This is done by checking all the entries and merging
    * them for maximum access. If an access right is defined in any ACE, the
    * user will be granted that right.
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    */
   public void setAccessForMultiMembershipMergedMaximum()
   {
      m_mergeAccess = MULTI_ACE_GETS_MERGED_MAX;
   }

   /**
    * Is the combined minimum access granted when the user belongs to
    * multiple groups or roles?
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * When this option is enabled, the matching entries will be merged
    * for minimum access. The access right must be defined on each ACE in
    * order for the user to be granted that right. When combining for
    * minimum access, it is possible the user will not be granted access.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    *
    * @return        <code>true</code> if this setting is in use,
    *             <code>false</code> otherwise
    */
   public boolean isAccessForMultiMembershipMergedMinimum()
   {
      if(m_mergeAccess == MULTI_ACE_GETS_MERGED_MIN)
         return true;
      return false;
   }

   /**
    * Grant the combined minimum access when the user belongs to multiple
    * groups or roles. This is done by checking all the entries and merging
    * them for minimum access. The access right must be defined on each
    * ACE in order for the user to be granted that right. When combining for
    * minimum access, it is possible the user will not be granted access.
    * <p>
    * When a user is included in the ACL due to group or role membership,
    * it is possible that the user may actually be a member of multiple
    * groups or roles. When this is the case, the access rights to grant
    * the user must be defined.
    * <p>
    * This does not effect users defined directly in the ACL, in which
    * case the ACE containing the user name will be used.
    */
   public void setAccessForMultiMembershipMergedMinimum()
   {
      m_mergeAccess = MULTI_ACE_GETS_MERGED_MIN;
   }

   /*
    * When multiple access control entries (ACEs) match a given user due
    * to role or group membership, grant access based upon the entry with
    * minimum access.
    * <p>
    * This does not effect users defined directly in the ACL, in which case
    * the ACE containing the user name will be used.
    */
   private static final int MULTI_ACE_GETS_MIN = 1;

   /*
    * When multiple access control entries (ACEs) match a given user due
    * to role or group membership, grant access based upon the entry with
    * maximum access.
    * <p>
    * This does not effect users defined directly in the ACL, in which case
    * the ACE containing the user name will be used.
    */
   private static final int MULTI_ACE_GETS_MAX = 2;

   /*
    * When multiple access control entries (ACEs) match a given user due
    * to role or group membership, grant access by merging all the ACEs
    * for minimum access. The access right must be defined on each ACE in
    * order for the user to be granted that right. When combining for
    * minimum access, it is possible the user will not be granted any
    * rights.
    * <p>
    * This does not effect users defined directly in the ACL, in which case
    * the ACE containing the user name will be used.
    */
   private static final int MULTI_ACE_GETS_MERGED_MIN = 3;

   /*
    * When multiple access control entries (ACEs) match a given user due
    * to role or group membership, grant access by merging all the ACEs
    * for maximum access. If an access right is defined in any ACE, the
    * user will be granted that right.
    * <p>
    * This does not effect users defined directly in the ACL, in which case
    * the ACE containing the user name will be used.
    */
   private static final int MULTI_ACE_GETS_MERGED_MAX = 4;


   /* **************   IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXAcl XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *    PSXAcl defines an Access Control List (ACL). ACLs can be associated
    *    with servers or applications.
    *
    *    The ACL contains one or more ACL entries (PSAclEntry). The entries
    *    define the users, groups and roles who can access a resource, and
    *    the type of access they have to the resource.
    *
    *    Object References:
    *
    *    PSAclEntry - the entries defined in this ACL.
    *    --&gt;
    *    &lt;!ELEMENT PSXAcl                       (PSXAclEntry*, multiMembershipBehavior)&gt;
    *
    *    &lt;!--
    *       useMaximumAccess - the matching entry with the greatest access
    *       rights is used.
    *
    *       useMinimumAccess - the matching entry with the least access
    *       rights is used.
    *
    *       mergeMaximumAccess - the matching entries will be merged for
    *       maximum access. If an access right is defined in any ACE, the
    *       user will be granted that right.
    *
    *       mergeMinimumAccess - the matching entries will be merged for
    *       minimum access. The access right must be defined on each ACE in
    *       order for the user to be granted that right. When combining for
    *       minimum access, it is possible the user will not be granted access.
    *      --&gt;
    *    &lt;!ENTITY % PSXMultiMembershipAclMethod "( useMaximumAccess, useMinimumAccess,
    *                                                 mergeMaximumAccess, mergeMinimumAccess)"&gt;
    *
    *    &lt;!--
    *       What level of access is granted when the user belongs to multiple
    *       groups or roles? When a user is included in the ACL due to group
    *       or role membership, it is possible that the user may actually be
    *       a member of multiple groups or roles. When this is the case, the
    *       access rights to grant the user must be defined.
    *
    *       This does not effect users defined directly in the ACL, in which
    *       case the ACE containing the user name will be used.
    *      --&gt;
    *    &lt;!ELEMENT multiMembershipBehavior  (%PSXMultiMembershipAclMethod)&gt;
    * </code></pre>
    *
    * @return      the newly created PSXAcl XML element node
    */
   public Element toXml(Document   doc)
   {
      Element   root = doc.createElement(ms_NodeType);

      root.setAttribute("id", String.valueOf(m_id));   //add id attribute

      PSComponent   entry;
      int   size = m_entries.size();
      for (int i = 0; i < size; i++) {
         entry = (PSComponent)m_entries.get(i);
         if(null == entry)
            continue;
         root.appendChild(entry.toXml(doc));
      }

      //create node for multiMembershipBehavior
      String      sTemp;
      switch(m_mergeAccess)
      {
      case MULTI_ACE_GETS_MAX:
         sTemp = XML_FLAG_USE_MAX;
         break;

      case MULTI_ACE_GETS_MIN:
         sTemp = XML_FLAG_USE_MIN;
         break;

      case MULTI_ACE_GETS_MERGED_MAX:
         sTemp = XML_FLAG_MERGE_MAX;
         break;

      case MULTI_ACE_GETS_MERGED_MIN:
         sTemp = XML_FLAG_MERGE_MIN;
         break;

      default:
         sTemp = "";
         break;
      }

      PSXmlDocumentBuilder.addElement(   doc, root, "multiMembershipBehavior",
         sTemp);

      return root;
   }

   /**
    * This method is called to populate a PSAcl Java object
    * from a PSXAcl XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception     PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXAcl
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         //make sure we got the ACL type node
         if (false == ms_NodeType.equals (sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);
         Element            node = null;

         String sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         //read ACL Entries. Make the m_entries empty before reading into it
         m_entries.clear();

         PSAclEntry   aclEntry = null;
         String curNodeType = PSAclEntry.ms_NodeType;
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         org.w3c.dom.Node cur = tree.getCurrent();   // cur = curNodeType = <PSXAcl>
         boolean hasLooped = false;
         for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
               curNode != null;
               curNode = tree.getNextElement(curNodeType, nextFlags))
         {
            node = (Element)tree.getCurrent();
            aclEntry = new PSAclEntry(node, parentDoc, parentComponents);
            m_entries.add(aclEntry);
            hasLooped = true;
         }

         //Read merge flag
         if (hasLooped)
            firstFlags = nextFlags;
         else
            tree.setCurrent(cur);

         if (tree.getNextElement("multiMembershipBehavior", firstFlags) != null) {
            sTemp = tree.getElementData((Element)tree.getCurrent());
            if (sTemp.equals(XML_FLAG_USE_MAX))
               m_mergeAccess = MULTI_ACE_GETS_MAX;
            else if (sTemp.equals(XML_FLAG_USE_MIN))
               m_mergeAccess = MULTI_ACE_GETS_MIN;
            else if (sTemp.equals(XML_FLAG_MERGE_MAX))
               m_mergeAccess = MULTI_ACE_GETS_MERGED_MAX;
            else if (sTemp.equals(XML_FLAG_MERGE_MIN))
               m_mergeAccess = MULTI_ACE_GETS_MERGED_MIN;
         }
      } finally {
         resetParentList(parentComponents, parentSize);
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

      IllegalArgumentException ex = validateEntries(m_entries);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());

      if (m_entries != null)
      {
         cxt.pushParent(this);
         try
         {
            for (int i = 0; i < m_entries.size(); i++)
            {
               PSComponent entry = (PSComponent)m_entries.get(i);
               entry.validate(cxt);
            }
         }
         finally
         {
            cxt.popParent();
         }
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAcl)) return false;
      if (!super.equals(o)) return false;
      PSAcl psAcl = (PSAcl) o;
      return m_mergeAccess == psAcl.m_mergeAccess &&
              Objects.equals(m_entries, psAcl.m_entries);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_mergeAccess, m_entries);
   }

   private      int                  m_mergeAccess = 0;      //what is merge access if multiple entries found
   private      PSCollection         m_entries = null;

   private static final String   XML_FLAG_USE_MAX      = "useMaximumAccess";
   private static final String   XML_FLAG_USE_MIN      = "useMinimumAccess";
   private static final String   XML_FLAG_MERGE_MAX   = "mergeMaximumAccess";
   private static final String   XML_FLAG_MERGE_MIN   = "mergeMinimumAccess";

   /* public access on this so they may reference each other in fromXml, 
    * including legacy classes */
   public static final String   ms_NodeType            = "PSXAcl";
}

