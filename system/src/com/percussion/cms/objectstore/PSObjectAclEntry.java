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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class represents an ACL entry. An ACL entry defines the permissions
 * for a particular user or group or virtual entry (Folder Community/Everyone).
 * Security on a securable object such as Folder is set using a set of ACL
 * entries.
 * <p>
 * If the ACL entry is for a user, then it contains the following information:
 * user name, security provider instance, security provider type, access mask
 * defining the user privileges on the securable object.
 * If the ACL entry is for a role or a virtual entry, then it contains the
 * following information : role name or virtual entry name, access mask
 * defining the user privileges on the securable object. The security provider
 * instance is <code>null</code> and the security provider type is
 * <code>-1</code>.
 * <p>
 * For an existing ACL entry the following cannot be modified:
 * name, type(user, role or virtual), security provider type,
 * security provider instance
 * This implies that only the permissions can be modified on an existing
 * ACL entry. However new ACL entries can be created and added to the
 * <code>PSObjectAcl</code> object which is a collection of ACL entries.
 * Similarly existing ACL emtries can be removed from the
 * <code>PSObjectAcl</code> object.
 * <p>
 * This class needs to extend <code>PSDbComponent</code> to use the
 * functionality of storing state (new, modified, unmodified, markedfordelete).
 * The state of ACL entries is used while serializing it to the database
 * (in the <code>PSServerFolderProcessor</code> class). The ACL entry
 * should be in one of the DBSTATE_xxx states. This state decides whether it
 * will be inserted, updated or deleted from the database on serialization.
 */
public class PSObjectAclEntry extends PSDbComponent
{
   /**
    * Use this for creating a new (non-persisted) ACL entry.
    * This constructor should generally be used for specifying ACL entries for
    * users.
    *
    * @param type the type of ACL entries. This specifies whether the ACL
    *    entry is for a user, role or virtual entry. It should be one of the
    *    following values:
    *    <code>PSObjectAclEntry.ACL_ENTRY_TYPE_USER</code>
    *    <code>PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE</code>
    *    <code>PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL</code>
    * @param name name of the user, role or virtual entry, may not be
    *    <code>null</code> or empty
    * @param permissions specifies the level of access on the securable object
    *    for the user, role or virtual entry, should be non-negative
    */
   public PSObjectAclEntry(int type, String name, int permissions)
   {
      super(PSObjectAclEntry.createKey(new String [] {}));
      
      init(type, name, permissions);
   }

   /**
    * Use this for creating a persisted ACL entry.
    * This constructor should be used for specifying ACL entries for
    * roles and virtual entry. This constructor cannot be used to define
    * ACL entry for users.
    *
    * Same as {@link #PSObjectAclEntry(int, String, int)
    * PSObjectAclEntry(int, String, int)} except for the one
    * additional parameter described below.
    *
    * @param id The id of this ACL entry as obtained from the database,
    *    may not be less than <code>0</code>
    */
   public PSObjectAclEntry(int id, int type, String name, int permissions)
   {
      super(PSObjectAclEntry.createKey(new String [] {"" + id}));
      
      init(type, name, permissions);
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return "name=" + m_name + ", type=" + m_type + ", permissions="
            + m_permissions;
   }
   
   /**
    * Initializes the member variables.
    * See {@link #PSObjectAclEntry(int, String, int)}
    * for details.
    */
   private void init(int type, String name, int permissions)
   {
      validateType(type);
      m_type = type;

      if ((name == null) || (name.trim().length() == 0))
         throw new IllegalArgumentException("name may not be null or empty");
      m_name = name.trim();

      if (permissions < 0)
         throw new IllegalArgumentException("Invalid permissions specified");
      m_permissions = permissions;
   }

   /**
    * Constructs the ACL entry from the supplied element. See {@link
    * #toXml(Document) toXml(Document)} for the expected form of xml.
    *
    * @param element the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>element</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException if <code>element</code> is not of
    * expected format.
    */
   public PSObjectAclEntry(Element element) throws PSUnknownNodeTypeException
   {
      fromXml(element);
   }

   // see base class for description
   public static PSKey createKey(String[] values)
   {
      if (values == null || values.length == 0)
         return new PSKey(new String[] {KEY_COL_ID});

      return new PSKey(new String[] {KEY_COL_ID}, values, true);
   }

   /**
    * Returns whether this ACL entry is for a user, role or virtual entry
    *
    * @return the type of ACL entry, is one of the following values:
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_USER</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL</code>
    */
   public int getType()
   {
      return m_type;
   }

   /**
    * Checks if this ACL entry is for a role.
    *
    * @return <code>true</code> if this ACL entry is for a role,
    * <code>false</code> otherwise.
    */
   public boolean isRole()
   {
      return (m_type == ACL_ENTRY_TYPE_ROLE);
   }

   /**
    * Checks if this ACL entry is for a user.
    *
    * @return <code>true</code> if this ACL entry is for a user,
    * <code>false</code> otherwise.
    */
   public boolean isUser()
   {
      return (m_type == ACL_ENTRY_TYPE_USER);
   }

   /**
    * Checks if this ACL entry is for a virtual entry.
    *
    * @return <code>true</code> if this ACL entry is for a virtual entry,
    * <code>false</code> otherwise.
    */
   public boolean isVirtual()
   {
      return (m_type == ACL_ENTRY_TYPE_VIRTUAL);
   }

   /**
    * Returns the name of the user, role or virtual entry for which this
    * ACL entry is specified.
    *
    * @return the user, role or virtual entry name, never <code>null</code>
    * or empty
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Returns an access mask representing all the permissions the user, role
    * or virtual entry has on the securable object.
    *
    * @return an access mask in which the bits are set to <code>1</code> if the
    * corresponding level of access is allowed. For levels of access which
    * are denied the corresponding bit is set to <code>0</code>
    */
   public int getPermissions()
   {
      return m_permissions;
   }

   /**
    * Sets an access mask representing all the permissions the user, role
    * or virtual entry has on the securable object.
    *
    * @param permissions an access mask which must have the bits set to
    * <code>1</code> if the corresponding level of access is allowed. For
    * levels of access which are denied the corresponding bit must be set to
    * <code>0</code>.
    */
   public void setPermissions(int permissions)
   {
      if (permissions < 0)
         throw new IllegalArgumentException("Invalid permissions specified");
      m_permissions = permissions;
      if (getState() == DBSTATE_UNMODIFIED)
         setState(DBSTATE_MODIFIED);
   }

   /**
    * Returns <code>true</code> if this ACL entry allows the
    * desired level of access, <code>false</code> otherwise.
    *
    * @param desiredAccess the desired level of access, should be one of the
    * following:
    * <code>PSObjectAclEntry.ACCESS_DENY</code>
    * <code>PSObjectAclEntry.ACCESS_READ</code>
    * <code>PSObjectAclEntry.ACCESS_WRITE</code>
    * <code>PSObjectAclEntry.ACCESS_ADMIN</code>
    *
    * @return <code>true</code> if the acccess mask encapsulated by this
    * ACL entry has the bit set corresponding to the permission specified by
    * <code>desiredAcess</code>, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>desiredAccess</code> is invalid
    */
   public boolean hasAccess(int desiredAccess)
   {
      validateAccessLevel(desiredAccess);
      return ((m_permissions & desiredAccess) == desiredAccess);
   }

   /**
    * Returns whether this this ACL entry denies access to the securable
    * object.
    *
    * @return <code>true</code> if this ACL entry denies access to securable
    * object, <code>false</code> otherwise.
    */
   public boolean hasNoAccess()
   {
      return m_permissions == ACCESS_DENY ? true : false;
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_READ)}
    */
   public boolean hasReadAccess()
   {
      return hasAccess(ACCESS_READ);
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_WRITE)}
    */
   public boolean hasWriteAccess()
   {
      return hasAccess(ACCESS_WRITE);
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_ADMIN)}
    */
   public boolean hasAdminAccess()
   {
      return hasAccess(ACCESS_ADMIN);
   }

   /**
    * Sets whether to allow or disallow the specified level of access.
    * If <code>level</code> is specified as
    * <code>PSObjectAclEntry.ACCESS_DENY</code>, then specifying
    * <code>grant</code> as <code>false</code> does not affect the permissions
    * in any way.
    *
    * @param level the level of acccess, should be one of the following:
    * <code>PSObjectAclEntry.ACCESS_DENY</code>
    * <code>PSObjectAclEntry.ACCESS_READ</code>
    * <code>PSObjectAclEntry.ACCESS_WRITE</code>
    * <code>PSObjectAclEntry.ACCESS_ADMIN</code>
    *
    * @param grant if <code>true</code> the specified level of access is
    * granted to the user, otherwise the access is denied.
    */
   public void setAccess(int level, boolean grant)
   {
      validateAccessLevel(level);
      if (grant)
      {
         if (level == ACCESS_DENY)
            m_permissions = ACCESS_DENY;
         else
            m_permissions = m_permissions | level;
      }
      else
         m_permissions = m_permissions & ~level;

      if (getState() == DBSTATE_UNMODIFIED)
         setState(DBSTATE_MODIFIED);
   }

   /**
    * Denies any access to the securable object.
    */
   public void setNoAccess()
   {
      m_permissions = ACCESS_DENY;
      if (getState() == DBSTATE_UNMODIFIED)
         setState(DBSTATE_MODIFIED);
   }

   /**
    * Convenience method that calls
    * {@link #setAccess(int, boolean) setAccess(ACCESS_READ, grant)}
    */
   public void setReadAccess(boolean grant)
   {
      setAccess(ACCESS_READ, grant);
   }

   /**
    * Convenience method that calls
    * {@link #setAccess(int, boolean) setAccess(ACCESS_WRITE, grant)}
    */
   public void setWriteAccess(boolean grant)
   {
      setAccess(ACCESS_WRITE, grant);
   }

   /**
    * Convenience method that calls
    * {@link #setAccess(int, boolean) setAccess(ACCESS_ADMIN, grant)}
    */
   public void setAdminAccess(boolean grant)
   {
      setAccess(ACCESS_ADMIN, grant);
   }

   /**
    * The name of the element returned by the <code>toXml</code> and
    * expected by the <code>fromXml</code> methods.
    *
    * @return A name valid for an xml element name. Never empty or
    * <code>null</code>.
    */
   public String getNodeName()
   {
      return XML_NODE_ROOT;
   }

   /**
    * Constructs the ACL entry from the supplied element. See {@link
    * #toXml(Document) toXml(Document)} for the expected form of xml.
    *
    * @param sourceNode the element to load from, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>
    * @throws PSUnknownNodeTypeException if element is not of expected format.
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      super.fromXml(sourceNode);

      Element el = null;
      String temp = null;

      // attributes
      // type of ACL entry
      temp = PSComponentUtils.getRequiredAttribute(sourceNode, XML_ATTR_TYPE);
      try 
      {
         m_type = Integer.parseInt(temp);
      }
      catch (Exception ex) 
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_TYPE, temp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      validateType(m_type);
      
      // permissions
      temp = PSComponentUtils.getRequiredAttribute(sourceNode, 
         XML_ATTR_PERMISSIONS);
      try 
      {
         m_permissions = Integer.parseInt(temp);
      }
      catch (Exception ex) 
      {
         Object[] args = {XML_NODE_NAME, XML_ATTR_PERMISSIONS, temp};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      
      // child elements
      // name
      el = PSComponentUtils.getChildElement(sourceNode, XML_NODE_NAME, true);
      temp = PSXmlTreeWalker.getElementData(el);
      if ((temp == null) || (temp.trim().length() < 1))
      {
         Object[] args = { el.getTagName(), "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      m_name = temp.trim();
   }



   /**
    * Produce XML representation of this object.
    * <p>
    * The xml format is:
    * <pre><code>
    * &lt;!ELEMENT PSXObjectAclEntry (name, securityProviderType?,
    *    securityProviderInstance?)> <br/>
    * &lt;!ATTLIST PSXObjectAclEntry <br/>
    *    type (role | user | community | everyone) "everyone" <br/>
    *    permissions CDATA #REQUIRED
    * > <br/>
    * &lt;!ELEMENT name (#PCDATA)> <br/>
    * &lt;!ELEMENT securityProviderInstance (#PCDATA)> <br/>
    * &lt;!ELEMENT securityProviderType (#PCDATA)> <br/>
    *
    * </code></pre>
    *
    * @param doc the Xml document to use for creating elements, may not be
    * <code>null</code>
    *
    * @return the element containing the Xml representation of this object,
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      // Base class shot
      Element root = super.toXml(doc);
      root.setAttribute(XML_ATTR_TYPE, String.valueOf(m_type));
      root.setAttribute(XML_ATTR_PERMISSIONS, String.valueOf(m_permissions));

      // name
      PSXmlDocumentBuilder.addElement(doc, root, XML_NODE_NAME, m_name);

      return root;
   }

   /*
    *  (non-Javadoc)
    * @see java.lang.Object#clone()
    */
   public Object clone()
   {
      PSObjectAclEntry copy = (PSObjectAclEntry) super.clone();
      copy.m_name = m_name;
      copy.m_type = m_type;
      copy.m_permissions = m_permissions;
      
      return copy;
   }
   
   /*
    *  (non-Javadoc)
    * @see com.percussion.cms.objectstore.IPSDbComponent#cloneFull()
    */
   public Object cloneFull()
   {
      PSObjectAclEntry copy = (PSObjectAclEntry) super.cloneFull();
      copy.m_name = m_name;
      copy.m_type = m_type;
      copy.m_permissions = m_permissions;
      
      return copy;
      
   }

   /**
    * Compares the specified object with this object. Returns <code>true</code>
    * if the reference to this object itself is specified. Returns
    * <code>false</code> if the specified object is not an instance of this
    * class.
    * Excludes the permissions when comparing this object with the specified
    * object. If you need to include the permissions in the comparison then
    * use the <code>equalsFull()</code> method instead.
    * Includes security provider type and instance in the comparison only if
    * this object represents an ACL entry for a user. Security provider type
    * and instance is ignored for ACL entries for roles and virtual entry.
    * The comparison of name and security provider instance is done in
    * case-insensitive manner.
    *
    * @param obj the object with which this object should be compared,
    * may not be <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and represents an ACL entry for the same user or role or virtual
    * entry. Returns <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    */
   public boolean equals(Object obj)
   {
      // Threshold - base class shot
      if (!super.equals(obj))
         return false;

      boolean equals = true;
      if (!(obj instanceof PSObjectAclEntry))
         equals = false;
      else
      {
         PSObjectAclEntry other = (PSObjectAclEntry)obj;

         if (m_type != other.m_type)
            equals = false;
         else if (!m_name.equalsIgnoreCase(other.m_name))
            equals = false;
      }
      return equals;
   }

   /**
    * Compares this object with another object. Returns <code>true</code> if
    * the specified object is a reference to this object itself. Returns
    * <code>false</code> if the specified object is not an instance of this
    * class.
    * Includes the permissions when comparing this object with the specified
    * object. If you need to exclude the permissions in the comparison then
    * use the <code>equals()</code> method instead.
    * Includes security provider type and instance in the comparison only if
    * this object represents an ACL entry for a user. Security provider type
    * and instance is ignored for ACL entries for roles and virtual entry.
    * The comparison of name and security provider instance is done in
    * case-insensitive manner.
    *
    * @param obj the object with which this object should be compared, may not
    * be <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and and represents an ACL entry for the same user, role or
    * virtual entry and has the same permissions, <code>false</code> otherwise
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    */
   public boolean equalsFull(Object obj)
   {
      boolean equals = equals(obj);
      if (equals)
      {
         PSObjectAclEntry other = (PSObjectAclEntry)obj;
         if (m_permissions != other.m_permissions)
            equals = false;
      }
      return equals;
   }

   /**
    * Returns the hashcode of this object.
    * Ignores the permissions in calculating the hashcode.
    * The hashcode is computed using the type and the lowercase name.
    *
    * @return the hashcode of this object, always non-negative
    */
   public int hashCode()
   {
      return super.hashCode() + m_type + m_name.toLowerCase().hashCode();
   }

   /**
    * Returns the hashcode of this object.
    * Includes the permissions in calculating the hashcode.
    * The hashcode is computed using the type and the lowercase name.
    *
    * @return the hashcode of this object, always non-negative
    */
   public int hashCodeFull()
   {
      return super.hashCodeFull() + m_type + m_name.toLowerCase().hashCode() + 
         m_permissions;
   }

   /**
    * Validates that the ACL entry type is one of the following:
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_USER</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL</code>
    *
    * @param type specifies whether this ACL entry is for a user, role or
    * virtual entry
    *
    * @throws IllegalArgumentException if <code>type</code> is not a valid
    * ACL entry type
    */
   private void validateType(int type)
   {
      switch(type)
      {
         case ACL_ENTRY_TYPE_USER:
         case ACL_ENTRY_TYPE_ROLE:
         case ACL_ENTRY_TYPE_VIRTUAL:
            break;

         default:
            throw new IllegalArgumentException("Invalid ACL entry type");
      }
   }

   /**
    * Validates that the ACL entry type is one of the following:
    * "user", "role", "community" or "everyone". The comparison is done in
    * case-insensitive manner.
    *
    * @param type specifies whether this ACL entry is for a user, role or
    * virtual entry, may not be <code>null</code> or empty, must be one of the
    * following values: "user", "role", "community" or "everyone"
    *
    * @return the index of <code>ACL_ENTRY_TYPES</code> array which matches
    * the specified type
    *
    * @throws IllegalArgumentException if <code>type</code> is invalid
    */
   private int validateType(String type)
   {
      for (int i=0; i < ACL_ENTRY_TYPES.length; i++)
      {
         if (type.equalsIgnoreCase(ACL_ENTRY_TYPES[i]))
            return i;
      }
      throw new IllegalArgumentException("Invalid ACL entry type");
   }


   /**
    * Validates that the access level is one of the following:
    * <code>PSObjectAclEntry.ACCESS_DENY</code>
    * <code>PSObjectAclEntry.ACCESS_READ</code>
    * <code>PSObjectAclEntry.ACCESS_WRITE</code>
    * <code>PSObjectAclEntry.ACCESS_ADMIN</code>
    *
    * @param accessLevel specifies the level of access, should be non-negative
    * and a valid access level.
    *
    * @throws IllegalArgumentException if <code>accessLevel</code> is invalid
    */
   private void validateAccessLevel(int accessLevel)
   {
      switch (accessLevel)
      {
         case ACCESS_DENY:
         case ACCESS_READ:
         case ACCESS_WRITE:
         case ACCESS_ADMIN:
            break;

         default:
            throw new IllegalArgumentException("Invalid access level");
      }
   }

   /**
    * Constant for an ACL entry for a user
    */
   public static final int ACL_ENTRY_TYPE_USER = 0;

   /**
    * Constant for an ACL entry for a role
    */
   public static final int ACL_ENTRY_TYPE_ROLE = 1;

   /**
    * Constant for an ACL entry for a Virtual Entry
    */
   public static final int ACL_ENTRY_TYPE_VIRTUAL = 2;

   /**
    * String constants for the ACL entry types
    */
   public static final String[] ACL_ENTRY_TYPES =
      {"user", "role", "virtual"};

   /**
    * String constant used in the name column for a virtual ACL entry for
    * folder community
    */
   public static final String ACL_ENTRY_FOLDER_COMMUNITY = "Folder Community";

   /**
    * String constant used in the name column for a virtual ACL entry for
    * everyone
    */
   public static final String ACL_ENTRY_EVERYONE = "Everyone";

   /**
    * Constant for allowing no access to the securable object.
    */
   public static final int ACCESS_DENY =
      PSObjectPermissions.ACCESS_DENY;

   /**
    * Constant for allowing read access to the securable object.
    */
   public static final int ACCESS_READ =
      PSObjectPermissions.ACCESS_READ;

   /**
    * Constant for allowing write access to the securable object.
    */
   public static final int ACCESS_WRITE =
      PSObjectPermissions.ACCESS_WRITE;

   /**
    * Constant for allowing admin access to the securable object.
    */
   public static final int ACCESS_ADMIN =
      PSObjectPermissions.ACCESS_ADMIN;

   /**
    * name of the user, role or virtual entry for which this ACL entry is
    * specified, initialized in the constructor, never <code>null</code>
    * after that. Modified by <code>fromXml()</code> method.
    */
   private String m_name = null;

   /**
    * Specifies whether this ACL entry is for a user, role or virtual entry,
    * should be one of the following values:
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_USER</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_ROLE</code>
    * <code>PSObjectAclEntry.ACL_ENTRY_TYPE_VIRTUAL</code>
    *
    * Initialized in the constructor. Modified by <code>fromXml()</code> method.
    */
   private int m_type = -1;

   /**
    * Access mask for storing all the allowed and disallowed permissions. This
    * has all the bits set to <code>1</code> if the permission corresponding
    * to the bit is granted. Similarly all the bits whose corresponding
    * permission is denied is set to <code>0</code>.
    * Initialized in the constructor, always non-negative, modified using
    * one of the following methods:
    *
    * {@link #setPermissions(int) setPermissions(int)}
    * {@link #setAccess(int, boolean) setAccess(int, boolean)}
    * {@link #setNoAccess() setNoAccess()}
    * {@link #setReadAccess(boolean) setReadAccess(boolean)}
    * {@link #setWriteAccess(boolean) setWriteAccess(boolean)}
    *
    */
   private int m_permissions = ACCESS_DENY;

   /**
    * constant used in defining the key
    */
   public static final String KEY_COL_ID = "SYSID";

   //
   // Constant for Xml node names, attribute names and values
   //
   private static final String XML_NODE_ROOT = "PSXObjectAclEntry";
   private static final String XML_NODE_NAME = "name";
   private static final String XML_ATTR_PERMISSIONS = "permissions";
   private static final String XML_ATTR_TYPE = "type";
}



