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
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Objects;

/**
 * The PSAclEntry class defines an entry in an Access Control List (ACL).
 * Access control entries (ACEs) define the users, groups and roles who
 * can access a resource, and the type of access they have to the resource.
 * ACEs are used in the context of server or application ACLs. Both ACL
 * types are defined using the PSAcl class. The ACL contains one or more
 * ACL entries (PSAclEntry).
 *
 * @see   PSAcl#getEntries
 * @see   PSAcl
 *
 * @author   Tas Giakouminakis
 * @version   1.0
 * @since   1.0
 */
public class PSAclEntry extends PSComponent
{
   /**
    * This entry can be used in calls to setName or the constructor to
    * create an entry for anonynmous users (not logged in).
    */
   public static final String ANONYMOUS_USER_NAME   = "Anonymous";

   /**
    * This entry can be used in calls to setName or the constructor to
    * create an entry for logged in users not explicitly defined in the ACL.
    */
   public static final String DEFAULT_USER_NAME      = "Default";


   // **************************** ACE TYPES ****************************

   /**
    * This entry represents a user. Entries may define access for users,
    * groups or roles.
    */
   public static final int ACE_TYPE_USER = 1;

   /**
    * This entry represents a group. Entries may define access for users,
    * groups or roles.
    */
   public static final int ACE_TYPE_GROUP = 2;

   /**
    * This entry represents a role. Entries may define access for users,
    * groups or roles.
    */
   public static final int ACE_TYPE_ROLE = 3;

   // *********************   APPLICATION ACL LEVELS *********************

   /**
    * This is an application ACE with no access to the application.
    * <p>
    * No other flags should be used in combination with this flag. If
    * any other flags are specified, no access will be ignored and the
    * user will be granted access.
    */
   public static final int AACE_NO_ACCESS         = 0x00000000;

   /**
    * This is an application ACE permitted to query data.
    */
   public static final int AACE_DATA_QUERY      = 0x00000001;

   /**
    * This is an application ACE permitted to create new data.
    */
   public static final int AACE_DATA_CREATE      = 0x00000002;

   /**
    * This is an application ACE permitted to update existing data.
    */
   public static final int AACE_DATA_UPDATE      = 0x00000004;

   /**
    * This is an application ACE permitted to delete existing data.
    */
   public static final int AACE_DATA_DELETE      = 0x00000008;

   /**
    * This is an application ACE permitted to read the application's
    * design.
    */
   public static final int AACE_DESIGN_READ      = 0x00000010;

   /**
    * This is an application ACE permitted to update the application's
    * design.
    */
   public static final int AACE_DESIGN_UPDATE   = 0x00000020;

   /**
    * This is an application ACE permitted to delete the application
    * (permanently removing it from the server).
    */
   public static final int AACE_DESIGN_DELETE   = 0x00000040;

   /**
    * This is an application ACE permitted to modify the application's
    * ACL.
    */
   public static final int AACE_DESIGN_MODIFY_ACL   = 0x00000080;

   // ************************ SERVER ACL LEVELS ************************

   /**
    * This is an server ACE with no access to the server.
    * <p>
    * No other flags should be used in combination with this flag. If
    * any other flags are specified, no access will be ignored and the
    * user will be granted access.
    */
   public static final int SACE_NO_ACCESS               = 0x00000000;

   /**
    * This is a server ACE allowed to make application data requests.
    * This only means that a request for application data submitted by
    * the user will be permitted on the server. The server will determine
    * which application the request is for. It will then check the
    * application's ACL. If the user is not defined in the application's
    * ACL, access will be denied at that point.
    */
   public static final int SACE_ACCESS_DATA            = 0x00010000;

   /**
    * This is a server ACE allowed to make application design requests.
    * This only means that a request to read, update or delete an
    * application submitted by the user will be permitted on the server.
    * The server will determine which application the request is for.
    * It will then check the application's ACL. If the user does not
    * have the appropriate design access in the application's ACL,
    * access will be denied at that point.
    */
   public static final int SACE_ACCESS_DESIGN         = 0x00020000;

   /**
    * This is a server ACE allowed to create new applications on the
    * server.
    */
   public static final int SACE_CREATE_APPLICATIONS   = 0x00040000;

   /**
    * This is a server ACE allowed to delete applications. Any
    * application on the server can be deleted when this access level is
    * granted. This is true even if the specified user is not in the
    * application's ACL.
    */
   public static final int SACE_DELETE_APPLICATIONS   = 0x00080000;

   /**
    * This is a server ACE allowed to submit remote server
    * administration requests. Some of the facilities available to remote
    * server administrators include starting and stopping applications,
    * checking server statistics, etc.
    */
   public static final int SACE_ADMINISTER_SERVER      = 0x00100000;


   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param   sourceNode     the XML element node to construct this
    *                             object from
    *
    * @param   parentDoc       the Java object which is the parent of this
    *                             object
    *
    * @param   parentComponents  the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                             if the XML element node is not of the
    *                             appropriate type
    */
   public PSAclEntry(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, java.util.ArrayList parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Empty constructor for creating from serialization, fromXml() etc
    *
    */
   PSAclEntry()
   {
      super();
      m_name = "";
      m_type = ACE_TYPE_USER;
   }

   /**
    * Creates an acl entry with the specified name and type. The
    * security provider is set to PSSecurityProvider.SP_TYPE_ANY.
    * No data or design access is granted.
    *
    * @param   name  the name of the user, group or role to associate
    *                with this entry
    *
    * @param   type  the type of entry this represents (use the appropriate
    *                PSAclEntry.ACE_TYPE_xxx flag)
    */
   public PSAclEntry(java.lang.String name, int type)
   {
      super();
      setName(name);
      m_type = type;
   }

   /**
    * Get the name of the user, group or role associated with this entry.
    *
    * @return   the name of the user, group or role associated with
    *            this entry
    */
   public java.lang.String getName()
   {
      return m_name;
   }

   /**
    * Set the name of the user, group or role associated with this entry.
    *
    * @param   name      the name of the user, group or role to associate
    *                  with this entry. This is limited to 255 characters.
    */
   public void setName(String name)
   {
      IllegalArgumentException ex = validateName(name);
      if (ex != null)
         throw ex;

      m_name = name;
   }

   /**
    * Private utility method to validate an ACL entry name. If the
    * return value is 0, the name is valid. Otherwise, the return value
    * is an object store error code.
    *
    * @author   chadloder
    *
    * @version   1.22 1999/06/17
    *
    * @param   name The name.
    *
    * @return   int The error code, or 0 if the name is valid.
    */
   private static IllegalArgumentException validateName(String name)
   {
      if ((null == name) || (name.trim().length() == 0))
      {
         return new IllegalArgumentException("acl entry name is empty");
      }
      else if (name.length() > ACE_MAX_NAME_LEN)
         return new IllegalArgumentException("acl entry name is too big" +
           " " + ACE_MAX_NAME_LEN + " " + name.length() );
      return null;
   }

   /**
    * Is this a user entry?
    *
    * @return   <code>true</code> if this is a user entry,
    *           <code>false</code> otherwise
    */
   public boolean isUser()
   {
      return (ACE_TYPE_USER == m_type);
   }

   /**
    * Mark this entry as defining a user.
    */
   public void setUser()
   {
      m_type = ACE_TYPE_USER;
   }

   /**
    * Is this a group entry?
    *
    * @return   <code>true</code> if this is a group entry,
    *           <code>false</code> otherwise
    */
   public boolean isGroup()
   {
      return (ACE_TYPE_GROUP == m_type);
   }

   /**
    * Mark this entry as defining a group.
    */
   public void setGroup()
   {
      m_type = ACE_TYPE_GROUP;
   }

   /**
    * Is this a role entry?
    *
    * @return   <code>true</code> if this is a role entry,
    *           <code>false</code> otherwise
    */
   public boolean isRole()
   {
      return (ACE_TYPE_ROLE == m_type);
   }

   /**
    * Mark this entry as defining a role.
    */
   public void setRole()
   {
      m_type = ACE_TYPE_ROLE;
   }

   /**
    * Get the access level for this entry.
    *
    * @return   if this is a server ACLE, zero or more PSAclEntry.SACE_
    *           flags will be returned. If this is an application ACE,
    *           zero or more PSAclEntry.AACE_ flags will be returned.
    */
   public int getAccessLevel()
   {
      return m_accessLevel;
   }

   /**
    * Set the access level for this entry.
    * <p>
    * The specified access level flag(s) will not be validated until
    * the ACL is saved as part of an application or server configuration.
    *
    * @param   level    if this is a server ACE, specify one or more
    * PSAclEntry.SACE_ flag. If this is an application
    * ACE, specify one or more PSAclEntry.AACE_ flag. Server and application
    * flags cannot be combined.
    */
   public void setAccessLevel(int level)
   {
      boolean isServer = false;
      boolean isApplication = false;

      if ((level & 0x11110000) != 0) // server in high word
         isServer = true;

      if ((level & 0x00001111) != 0) // application in low word
         isApplication = true;

      if (isServer && isApplication)
      {
         throw new IllegalArgumentException("acl security level invalid" +
                 " " + level);
      }

      if (isServer)
         m_isServerAcl = true;
      else if (isApplication)
         m_isServerAcl = false;
      // ELSE: is a NO ACCESS (0) flag, how do we know which is which ?

      m_accessLevel = level;
   }


   // **************   IPSComponent Interface Implementation **************

   /**
    * This method is called to create a PSXAclEntry XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *  &lt;!--
    *     PSXAclEntry defines an entry in an Access Control List (ACL).
    *     Access control entries (ACEs) define the users, groups and roles
    *     who can access a resource, and the type of access they have to
    *     the resource. ACEs are used in the context of server or
    *     application ACLs. Both ACL types are defined using the PSXAcl
    *     object, which is the container for PSXAclEntry objects. The ACL
    *     contains one or more ACL entries.
    *  --&gt;
    *  &lt;!ELEMENT PSXAclEntry               (name, securityProviderType,
    *                                           securutiyProviderInstance?,
    *                                           (serverAccessLevel |
    *                                           applicationAccessLevel))&gt;
    *
    *  &lt;!--
    *     The ACL Entry type:
    *
    *     user - this entry represents a user.
    *
    *     group - this entry represents a group.
    *
    *     role - this entry represents a role.
    *  --&gt;
    *  &lt;!ENTITY % PSXAclEntryType "(user, group, role)"&gt;
    *  &lt;!ATTLIST PSXAclEntry
    *     type    %PSXAclEntryType   #REQUIRED
    *  &gt;
    *
    *  &lt;!--
    *     the name of the user, group or role associated with this entry.
    *  --&gt;
    *  &lt;!ELEMENT name                   (#PCDATA)&gt;
    *
    *  &lt;!--
    *     the type of security provider to use when authenticating this
    *     entry.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderType      (%PSXSecurityProviderType)&gt;
    *
    *  &lt;!--
    *     the security provider instance to use when authenticating this
    *     entry. Since there may be several instances of a given security
    *     provider (eg, multiple LDAP servers), specifying the instance to
    *     use may be necessary.
    *  --&gt;
    *  &lt;!ELEMENT securityProviderInstance  (#PCDATA)&gt;
    *
    *  &lt;!--
    *     The access levels permitted for server access:
    *
    *     dataAccess - application data requests are permitted. This only
    *     means that a request for application data submitted by the user
    *     will be permitted on the server. The server will determine which
    *     application the request is for. It will then check the
    *     application's ACL. If the user is not defined in the
    *     application's ACL, access will be denied at that point.
    *
    *     designAccess - application design requests are permitted. This
    *     only means that a request to read, update or delete an application
    *     submitted by the user will be permitted on the server. The server
    *     will determine which application the request is for. It will then
    *     check the application's ACL. If the user does not have the
    *     appropriate design access in the application's ACL, access will be
    *     denied at that point.
    *
    *     createApplications - creating new applications on the server is
    *     permitted.
    *
    *     deleteApplications - deleting applications from the server is
    *     permitted. Any application on the server can be deleted when this
    *     access level is granted. This is true even if the specified user
    *     is not in the application's ACL.
    *
    *     administerServer - submitting remote server administration
    *     requests is permitted. Some of the facilities available to remote
    *     server administrators include starting and stopping applications,
    *     checking server statistics, etc.
    *  --&gt;
    *  &lt;!ELEMENT serverAccessLevel      EMPTY&gt;
    *  &lt;!ATTLIST serverAccessLevel
    *     dataAccess          %PSXIsEnabled   #OPTIONAL
    *     designAccess         %PSXIsEnabled   #OPTIONAL
    *     createApplications %PSXIsEnabled   #OPTIONAL
    *     deleteApplications %PSXIsEnabled   #OPTIONAL
    *     administerServer       %PSXIsEnabled   #OPTIONAL
    *  &gt;
    *
    *  &lt;!--
    *     dataQuery - querying data through the application is permitted.
    *
    *     dataCreate - creating new data through the application is
    *     permitted.
    *
    *     dataUpdate - updating existing data through the application is
    *     permitted.
    *
    *     dataDelete - deleting existing data through the application is
    *     permitted.
    *
    *     designRead - reading the application's design is permitted.
    *
    *     designUpdate - updating the application's design is permitted.
    *
    *     designDelete - deleting the application is permitted (permanently
    *     removing it from the server).
    *
    *     modifyAcl - modifying the application's ACL is permitted.
    *  --&gt;
    *  &lt;!ELEMENT applicationAccessLevel    EMPTY&gt;
    *  &lt;!ATTLIST applicationAccessLevel
    *     dataQuery            %PSXIsEnabled   #OPTIONAL
    *     dataCreate          %PSXIsEnabled   #OPTIONAL
    *     dataUpdate          %PSXIsEnabled   #OPTIONAL
    *     dataDelete          %PSXIsEnabled   #OPTIONAL
    *     designRead          %PSXIsEnabled   #OPTIONAL
    *     designUpdate         %PSXIsEnabled   #OPTIONAL
    *     designDelete         %PSXIsEnabled   #OPTIONAL
    *     modifyAcl            %PSXIsEnabled   #OPTIONAL
    *  &gt;
    * </code></pre>
    *
    * @return   the newly created PSXAclEntry XML element node
    */
   public Element toXml(Document   doc)
   {
      //create PSXAclEnty element and add type attribute
      Element   root = doc.createElement(ms_NodeType);

      root.setAttribute("id", String.valueOf(m_id));   //add id attribute
      root.setAttribute("type", getEntryTypeName());   //add ACE type attribute

      //create name element
      PSXmlDocumentBuilder.addElement(   doc, root, "name", m_name);

      Element node;
      if (m_isServerAcl)
      {      // create serverAccessLevel element
         node = doc.createElement("serverAccessLevel");

         if ((m_accessLevel & SACE_ACCESS_DATA) == SACE_ACCESS_DATA)
            node.setAttribute("dataAccess", "yes");
         else
            node.setAttribute("dataAccess", "no");

         if ((m_accessLevel & SACE_ACCESS_DESIGN) == SACE_ACCESS_DESIGN)
            node.setAttribute("designAccess", "yes");
         else
            node.setAttribute("designAccess", "no");

         if ((m_accessLevel & SACE_CREATE_APPLICATIONS) == SACE_CREATE_APPLICATIONS)
            node.setAttribute("createApplications", "yes");
         else
            node.setAttribute("createApplications", "no");

         if ((m_accessLevel & SACE_DELETE_APPLICATIONS) == SACE_DELETE_APPLICATIONS)
            node.setAttribute("deleteApplications", "yes");
         else
            node.setAttribute("deleteApplications", "no");

         if ((m_accessLevel & SACE_ADMINISTER_SERVER) == SACE_ADMINISTER_SERVER)
            node.setAttribute("administerServer", "yes");
         else
            node.setAttribute("administerServer", "no");
      }
      else
      {                  // create applicationAccessLevel element.
         node = doc.createElement("applicationAccessLevel");

         if ((m_accessLevel & AACE_DATA_QUERY) == AACE_DATA_QUERY)
            node.setAttribute("dataQuery", "yes");
         else
            node.setAttribute("dataQuery", "no");

         if ((m_accessLevel & AACE_DATA_CREATE) == AACE_DATA_CREATE)
            node.setAttribute("dataCreate", "yes");
         else
            node.setAttribute("dataCreate", "no");

         if ((m_accessLevel & AACE_DATA_UPDATE) == AACE_DATA_UPDATE)
            node.setAttribute("dataUpdate", "yes");
         else
            node.setAttribute("dataUpdate", "no");

         if ((m_accessLevel & AACE_DATA_DELETE) == AACE_DATA_DELETE)
            node.setAttribute("dataDelete", "yes");
         else
            node.setAttribute("dataDelete", "no");

         if ((m_accessLevel & AACE_DESIGN_READ) == AACE_DESIGN_READ)
            node.setAttribute("designRead", "yes");
         else
            node.setAttribute("designRead", "no");

         if ((m_accessLevel & AACE_DESIGN_UPDATE) == AACE_DESIGN_UPDATE)
            node.setAttribute("designUpdate", "yes");
         else
            node.setAttribute("designUpdate", "no");

         if ((m_accessLevel & AACE_DESIGN_DELETE) == AACE_DESIGN_DELETE)
            node.setAttribute("designDelete", "yes");
         else
            node.setAttribute("designDelete", "no");

         if ((m_accessLevel & AACE_DESIGN_MODIFY_ACL) == AACE_DESIGN_MODIFY_ACL)
            node.setAttribute("modifyAcl", "yes");
         else
            node.setAttribute("modifyAcl", "no");
      }
      root.appendChild(node);

      return root;
   }

   /**
    * This method is called to populate a PSAclEntry Java object
    * from a PSXAclEntry XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                      of type PSXAclEntry
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                       java.util.ArrayList parentComponents)
                       throws PSUnknownNodeTypeException
   {
      if (sourceNode == null){
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      //make sure we got the ACL type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName())){
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try{
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      //get the ACLEntry type element from attribute
      sTemp = tree.getElementData("type");
      if ((sTemp == null) || (sTemp.length() == 0)){
         Object[] args = { ms_NodeType, "type", "empty" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      else if (sTemp.equals(XML_FLAG_TYPE_USER))
         m_type = ACE_TYPE_USER;
      else if (sTemp.equals(XML_FLAG_TYPE_GROUP))
         m_type = ACE_TYPE_GROUP;
      else if (sTemp.equals(XML_FLAG_TYPE_ROLE))
         m_type = ACE_TYPE_ROLE;
      else{
         Object[] args = { ms_NodeType, "type", sTemp };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      //Read name element of the ACLEntry
      sTemp = tree.getElementData("name");
      try{
         setName(sTemp);
      } catch (IllegalArgumentException e){
         throw new PSUnknownNodeTypeException(ms_NodeType, "name",
            new PSException (e.getLocalizedMessage()));
      }

      // initially all bits are off, we will OR them below
      m_accessLevel = 0;

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      // this contains either a server or application access level
      if (tree.getNextElement("serverAccessLevel", firstFlags) != null)
      {
         m_isServerAcl = true;

         sTemp = tree.getElementData("dataAccess", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= SACE_ACCESS_DATA;

         sTemp = tree.getElementData("designAccess", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= SACE_ACCESS_DESIGN;

         sTemp = tree.getElementData("createApplications", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= SACE_CREATE_APPLICATIONS;

         sTemp = tree.getElementData("deleteApplications", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= SACE_DELETE_APPLICATIONS;

         sTemp = tree.getElementData("administerServer", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= SACE_ADMINISTER_SERVER;
      }
      else if (tree.getNextElement("applicationAccessLevel", firstFlags) != null)
      {
         m_isServerAcl = false;

         sTemp = tree.getElementData("dataQuery", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DATA_QUERY;

         sTemp = tree.getElementData("dataCreate", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DATA_CREATE;

         sTemp = tree.getElementData("dataUpdate", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DATA_UPDATE;

         sTemp = tree.getElementData("dataDelete", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DATA_DELETE;

         sTemp = tree.getElementData("designRead", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DESIGN_READ;

         sTemp = tree.getElementData("designUpdate", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DESIGN_UPDATE;

         sTemp = tree.getElementData("designDelete", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DESIGN_DELETE;

         sTemp = tree.getElementData("modifyAcl", false);
         if ((sTemp != null) && sTemp.equalsIgnoreCase("yes"))
            m_accessLevel |= AACE_DESIGN_MODIFY_ACL;
      }
      else
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.ACL_ENTRY_LEVEL_NOT_FOUND);
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
    * @param   cxt The validation context.
    *
    * @throws   PSValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      if (m_id < 0)
      {
         Object[] args = { ms_NodeType, "" + m_id };
         cxt.validationWarning(this, IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID,
            args);
      }

      // validate that only server access level flags are set if this is a
      // server acl, or that only non-server access level flags are set if this
      // is not a server ACL
      if (m_isServerAcl)
      {
         // turn on all invalid bits
         final int all_flags_compliment = ~0 &
            (AACE_DATA_QUERY    | AACE_DATA_CREATE | AACE_DATA_UPDATE   |
            AACE_DATA_DELETE   | AACE_DESIGN_READ | AACE_DESIGN_UPDATE |
            AACE_DESIGN_DELETE | AACE_DESIGN_MODIFY_ACL);

         if (0 != (m_accessLevel & all_flags_compliment))
         {
            Object[] args = { "" + m_accessLevel };
            cxt.validationError(this,
               IPSObjectStoreErrors.ACL_SECURITY_LEVEL_INVALID, args);
         }
      }
      else
      {
         // turn on all invalid bits
         final int all_flags_compliment = ~0 &
            (SACE_ACCESS_DATA | SACE_ACCESS_DESIGN | SACE_CREATE_APPLICATIONS |
            SACE_DELETE_APPLICATIONS | SACE_ADMINISTER_SERVER);

         if (0 != (m_accessLevel & all_flags_compliment))
         {
            Object[] args = { "" + m_accessLevel };
            cxt.validationError(this,
               IPSObjectStoreErrors.ACL_SECURITY_LEVEL_INVALID, args);
         }
      }

      switch (m_type)
      {
      case ACE_TYPE_USER:
         // fall through
      case ACE_TYPE_GROUP:
         // fall through
      case ACE_TYPE_ROLE:
         break; // end valid types
      default:

         cxt.validationError(this, IPSObjectStoreErrors.ACL_TYPE_INVALID,
            new Object[] { m_name, "" + m_type} );
      }

      IllegalArgumentException ex = validateName(m_name);
      if (ex != null)
         cxt.validationError(this, 0, ex.getLocalizedMessage());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAclEntry)) return false;
      if (!super.equals(o)) return false;
      PSAclEntry that = (PSAclEntry) o;
      return m_type == that.m_type &&
              m_isServerAcl == that.m_isServerAcl &&
              m_accessLevel == that.m_accessLevel &&
              Objects.equals(m_name, that.m_name);
   }

   @Override
   public int hashCode() {
      return Objects.hash(super.hashCode(), m_name, m_type, m_isServerAcl, m_accessLevel);
   }

   private String getEntryTypeName()
   {
      String sRet = "";
      if (m_type == ACE_TYPE_USER)
         sRet = XML_FLAG_TYPE_USER;
      else if (m_type == ACE_TYPE_GROUP)
         sRet = XML_FLAG_TYPE_GROUP;
      else if (m_type == ACE_TYPE_ROLE)
         sRet = XML_FLAG_TYPE_ROLE;

      return sRet;
   }


   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param   c a valid PSAclEntry. 
    */
   public void copyFrom( PSAclEntry c )
   {
      if ( null == c )
         throw new IllegalArgumentException( "Invalid object for copy");

      copyFrom((PSComponent) c );

      m_name = c.m_name;
      m_type = c.m_type;
      m_isServerAcl = c.m_isServerAcl;
      m_accessLevel = c.m_accessLevel;
   }


   private               String      m_name = "";
   private               int         m_type = ACE_TYPE_USER;
   private               boolean      m_isServerAcl = false;
   private               int         m_accessLevel = 0; //opearate on booleans

   private static final String XML_FLAG_TYPE_USER      = "user";
   private static final String XML_FLAG_TYPE_GROUP      = "group";
   private static final String XML_FLAG_TYPE_ROLE      = "role";

   private static final int      ACE_MAX_NAME_LEN      = 255;

   // package access on this so they may reference each other in fromXml
   static final String   ms_NodeType            = "PSXAclEntry";
}

