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


import java.util.Iterator;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSAclHandler;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSThreadRequestUtils;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSServer;
import com.percussion.utils.request.PSRequestInfo;

/**
 * This class contains all the logic for determining the level of access
 * a user has on a specified object. The level of access can be determined
 * by using either of the following:
 * <p><br>
 * 1> Credentials of the user making the request and the security specified on
 * the securable object being accessed. This will typically be used on the
 * server, which will then create an access mask and associate it with the
 * securable object.
 * <p><br>
 * 2> Using the access mask set on the securable object. This will typically
 * be used on the client side. The client will use the access mask set on the
 * securable object by the server.
 * <p><br>
 * This class basically encapsulates an access mask. This access mask
 * determines all the permissions a particular user has on a specified
 * securable object. The encapsulated access mask cannot be modified once set
 * (in the constructor).
 *
 * This class has been made abstract. The derived classes need to override the
 * abstract methods <code>processAcl()</code> and <code>processAclEntry()</code>
 * Typically a new derived class will be created for a specific type of
 * securable object. For example, <code>PSFolderPermissions</code> class
 * represents permissions on folder objects.
 */
public abstract class PSObjectPermissions
{
   
   /**
    * This constructor will typically be used on the server side. This sets the
    * user's server access level based on the credentials in the request object.
    *
    * @param request request context information, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>request</code> is
    * <code>null</code>
    *
    * @throws PSCmsException if request to server to get the user info fails
    * for any reason
    *
    * @throws PSAuthorizationException if any error occurs getting the user's
    * server access level
    */
   protected PSObjectPermissions()
      throws PSAuthorizationException, PSCmsException
   {
      m_accessLevel=PSThreadRequestUtils.getUserAccessLevel();
      m_userInfo=PSThreadRequestUtils.getUserInfo();
      return;
   }


   /**
    * This method does the actual work of determining the user's permissions
    * on the object using the specified ACL and the user's information.
    * This creates the encapsulated access mask using the user credentials
    * contained in the request context object (passed as an argument to the
    * ctor) and permissions set on the securable object using the
    * specified <code>objectAcl</code>
    *
    * @param objectAcl contains the set of permissions specified on the
    * securable object (such as Folders), may not be <code>null</code>
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationRequiredException 
    * @throws PSCmsException 
    *
    * @throws IllegalArgumentException if <code>objectAcl</code> is
    * <code>null</code>
    */
   protected void init(PSObjectAcl objectAcl) throws PSAuthenticationRequiredException, PSAuthorizationException, PSCmsException
   {
      if (objectAcl == null)
         throw new IllegalArgumentException("objectAcl may not be null");
      m_objectAcl = objectAcl;

      if (!processAcl())
         return;

      boolean processLastAclEntry = true;
      Iterator<?> it = m_objectAcl.iterator();
      while (it.hasNext())
      {
         PSObjectAclEntry objectAclEntry = (PSObjectAclEntry)it.next();
         if (!processAclEntry(objectAclEntry))
         {
            processLastAclEntry = false;
            break;
         }
      }
      if (processLastAclEntry)
         processAclEntry(null);
   }


   /**
    * Abstract method to be overwritten by derived classes.
    * This method is called before the ACL entries are processed. This method
    * will always be called even if the ACL contains no ACL entry.
    *
    * @return <code>true</code> to process the ACL entries, <code>false</code>
    * otherwise. If <code>false</code> is returned then
    * <code>processAclEntry</code> will never be called.
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationRequiredException 
    * @throws PSCmsException 
    */
   protected abstract boolean processAcl() throws PSAuthenticationRequiredException, PSAuthorizationException, PSCmsException;

   /**
    * Abstract method to be overwritten by derived classes. This method is
    * guaranteed to be called at least once if <code>processAcl()</code>
    * returns <code>true</code>, otherwise this is never called.
    *
    * If <code>process()</code> method returns <code>true</code> then
    * this method is called once for each ACL entry in the ACL. After all the
    * ACL entries have been processed it is called once with <code>null</code>
    * value for <code>aclEntry</code>.
    * If the ACL does not contain any ACL entry then this is called only once
    * with <code>null</code> value for <code>aclEntry</code>.
    *
    * @param aclEntry the ACL entry currently being processed, is
    * <code>null</code> for the last call made to this method while processing
    * the ACLs, otherwise never <code>null</code>
    *
    * @return <code>true</code> if the ACL entry processing should continue
    * @throws PSAuthorizationException 
    * @throws PSAuthenticationRequiredException 
    */
   protected abstract boolean processAclEntry(PSObjectAclEntry aclEntry) throws PSCmsException, PSAuthenticationRequiredException, PSAuthorizationException;

   /**
    * This constructor will typically be used on the client side (generally
    * using the access mask set on the securable object by the server). This
    * just stores the specified access mask.
    *
    * @param permissions the access mask to encapsulate, should be non-negative
    *
    * @throws IllegalArgumentException if <code>permissions</code> is invalid
    */
   protected PSObjectPermissions(int permissions)
   {
      if (permissions < 0)
         throw new IllegalArgumentException("Invalid permissions specified");
      accessSet = false;
      m_permissions = permissions;
   }

   /**
    * Compares the specified object with this object. Returns <code>true</code>
    * if the reference to this object itself is specified. Returns
    * <code>false</code> if the specified object is not an instance of this
    * class.
    *
    * @param obj the object with which this object should be compared,
    * may not be <code>null</code>
    *
    * @return <code>true</code> if the specified object is an instance of this
    * class and represents the same permissions on the securable object.
    * Returns <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>obj</code> is <code>null</code>
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
      if (obj == this)
         return true;

      boolean equals = true;
      if (!(obj instanceof PSObjectPermissions))
         equals = false;
      else
      {
         PSObjectPermissions other = (PSObjectPermissions)obj;

         if (m_permissions != other.m_permissions)
            equals = false;
      }
      return equals;
   }

   /**
    * Returns the hashcode of this object. This simply returns the permissions
    * set on the securable object.
    *
    * @return the hashcode of this object, always non-negative
    */
   public int hashCode()
   {
      return m_permissions;
   }

   /**
    * Returns the encapsulated access mask which indicates all the permissions
    * a particular user has on a specified securable object.
    * This access mask can then be used in the
    * {@link #hasAccess(int) hasAccess(int)} method to determine if a
    * particular type of access is allowed or not.
    *
    * @return an access mask which has the corresponding bit set if a particular
    * permission is granted, and the bit turned off if the permission is denied
    */
   public int getPermissions()
   {
      return m_permissions;
   }

   /**
    * Returns <code>true</code> if the encapsulated access mask allows the
    * desired level of access, <code>false</code> otherwise.
    *
    * @param accessLevel the desired level of access, must be one of the
    * following:
    * <code>PSObjectPermissions.ACCESS_DENY</code>
    * <code>PSObjectPermissions.ACCESS_READ</code>
    * <code>PSObjectPermissions.ACCESS_WRITE</code>
    * <code>PSObjectPermissions.ACCESS_ADMIN</code>
    * <code>PSObjectPermissions.ACCESS_SERVER_ADMIN_STR</code>
    *
    * @return <code>true</code> if the encapsulated accessMask has the bit set
    * corresponding to the permission specified by <code>desiredAcess</code>,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>accessLevel</code> is invalid
    */
   public boolean hasAccess(int accessLevel)
   {
      validateAccessLevel(accessLevel);
      return ((m_permissions & accessLevel) == accessLevel);
   }
   
   /**
    * Returns <code>true</code> if the given access level 
    * has access even if the the mask is set to lower value.
    *
    * @param accessLevel the desired level of access, must be one of the
    * following:
    * <code>PSObjectPermissions.ACCESS_DENY</code>
    * <code>PSObjectPermissions.ACCESS_READ</code>
    * <code>PSObjectPermissions.ACCESS_WRITE</code>
    * <code>PSObjectPermissions.ACCESS_ADMIN</code>
    * <code>PSObjectPermissions.ACCESS_SERVER_ADMIN_STR</code>
    *
    * @return <code>true</code> is the given access level is greater than 
    * or equal what is required.
    *
    * @throws IllegalArgumentException if <code>accessLevel</code> is invalid
    */   
   public boolean hasAccessOrHigher(int accessLevel)
   {
      validateAccessLevel(accessLevel);
      return m_permissions >= accessLevel;
   }

   /**
    * Translates integer based access level to the string based access level.
    *
    * @param accessLevel must be one of the following predefined levels of
    * access
    * <code>PSObjectPermissions.ACCESS_DENY</code>
    * <code>PSObjectPermissions.ACCESS_READ</code>
    * <code>PSObjectPermissions.ACCESS_WRITE</code>
    * <code>PSObjectPermissions.ACCESS_ADMIN</code>
    * <code>PSObjectPermissions.ACCESS_SERVER_ADMIN</code>
    *
    * @return string based access level, never <code>null</code> or empty
    *
    * @throws IllegaArgumentException if <code>accessLevel</code> is invalid
    */
   public static String translateAccess(int accessLevel)
   {
      validateAccessLevel(accessLevel);
      String strAccess = null;
      switch (accessLevel)
      {
         case ACCESS_DENY:
            strAccess = ACCESS_DENY_STR;
            break;

         case ACCESS_READ:
            strAccess = ACCESS_READ_STR;
            break;

         case ACCESS_WRITE:
            strAccess = ACCESS_WRITE_STR;
            break;

         case ACCESS_ADMIN:
            strAccess = ACCESS_ADMIN_STR;
            break;

         case ACCESS_SERVER_ADMIN:
            strAccess = ACCESS_SERVER_ADMIN_STR;
            break;
      }
      return strAccess;
   }

   /**
    * Translates string based access level to the integer based access level.
    *
    * @param accessLevel must be one of the following predefined levels of
    * access
    * <code>PSObjectPermissions.ACCESS_DENY_STR</code>
    * <code>PSObjectPermissions.ACCESS_READ_STR</code>
    * <code>PSObjectPermissions.ACCESS_WRITE_STR</code>
    * <code>PSObjectPermissions.ACCESS_ADMIN_STR</code>
    * <code>PSObjectPermissions.ACCESS_SERVER_ADMIN_STR</code>
    *
    * May not be <code>null</code> or empty
    *
    * @return integer based access level
    *
    * @throws IllegaArgumentException if <code>accessLevel</code> is invalid
    */
   public static int translateAccess(String accessLevel)
   {
      if ((accessLevel == null) || (accessLevel.trim().length() < 1))
         throw new IllegalArgumentException(
            "accessLevel may not be null or empty");

      int iAccess = 0;
      if (accessLevel.equalsIgnoreCase(ACCESS_DENY_STR))
         iAccess = ACCESS_DENY;
      else if (accessLevel.equalsIgnoreCase(ACCESS_READ_STR))
         iAccess = ACCESS_READ;
      else if (accessLevel.equalsIgnoreCase(ACCESS_WRITE_STR))
         iAccess = ACCESS_WRITE;
      else if (accessLevel.equalsIgnoreCase(ACCESS_ADMIN_STR))
         iAccess = ACCESS_ADMIN;
      else if (accessLevel.equalsIgnoreCase(ACCESS_SERVER_ADMIN_STR))
         iAccess = ACCESS_SERVER_ADMIN;
      else
         throw new IllegalArgumentException("Invalid access level");

      return iAccess;
   }

   /**
    * Gets the next higher level of access than the specified access level.
    * For example, if
    *
    * <code>accessLevel</code> equals <code>ACCESS_DENY</code> then
    * <code>ACCESS_READ</code> is returned,
    *
    * <code>accessLevel</code> equals <code>ACCESS_READ</code> then
    * <code>ACCESS_WRITE</code> is returned,
    *
    * <code>accessLevel</code> equals <code>ACCESS_WRITE</code> then
    * <code>ACCESS_ADMIN</code> is returned,
    *
    * <code>accessLevel</code> equals <code>ACCESS_ADMIN</code> then
    * <code>ACCESS_SERVER_ADMIN</code> is returned,
    *
    * <code>accessLevel</code> equals <code>ACCESS_SERVER_ADMIN</code> then
    * <code>ACCESS_SERVER_ADMIN</code> itself is returned,
    *
    * @param accessLevel the access level whose next higher access level is to
    * be returned, must be valid access level
    *
    * @return the next higher level of access as described above.
    *
    * @throws IllegaArgumentException if <code>accessLevel</code> is invalid
    */
   public static int getHigherAccessLevel(int accessLevel)
   {
      validateAccessLevel(accessLevel);
      int iAccess = 0;
      switch (accessLevel)
      {
         case ACCESS_DENY:
            iAccess = ACCESS_READ;
            break;

         case ACCESS_READ:
            iAccess = ACCESS_WRITE;
            break;

         case ACCESS_WRITE:
            iAccess = ACCESS_ADMIN;
            break;

         case ACCESS_ADMIN:
         case ACCESS_SERVER_ADMIN:
            iAccess = ACCESS_SERVER_ADMIN;
            break;
      }
      return iAccess;
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_DENY)}
    */
   public boolean hasNoAccess()
   {
      return hasAccess(ACCESS_DENY);
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_READ)}
    */
   public boolean hasReadAccess()
   {
      return hasAccess(ACCESS_READ) || hasWriteAccess();
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_WRITE)}
    */
   public boolean hasWriteAccess()
   {
      return hasAccess(ACCESS_WRITE) || hasAdminAccess();
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_ADMIN)}
    */
   public boolean hasAdminAccess()
   {
      return hasAccess(ACCESS_ADMIN) || hasServerAdminAccess();
   }

   /**
    * Convenience method that calls
    * {@link #hasAccess(int) hasAccess(ACCESS_SERVER_ADMIN)}
    */
   public boolean hasServerAdminAccess()
   {
      return hasAccess(ACCESS_SERVER_ADMIN);
   }

   /**
    * Validates that the access level is one of the following:
    * <code>PSObjectPermissions.ACCESS_DENY</code>
    * <code>PSObjectPermissions.ACCESS_READ</code>
    * <code>PSObjectPermissions.ACCESS_WRITE</code>
    * <code>PSObjectPermissions.ACCESS_ADMIN</code>
    * <code>PSObjectPermissions.ACCESS_SERVER_ADMIN_STR</code>
    *
    * @param accessLevel specifies the level of access, should be non-negative
    * and a valid access level.
    *
    * @throws IllegalArgumentException if <code>accessLevel</code> is invalid
    */
   protected static void validateAccessLevel(int accessLevel)
   {
      switch (accessLevel)
      {
         case ACCESS_DENY:
         case ACCESS_READ:
         case ACCESS_WRITE:
         case ACCESS_ADMIN:
         case ACCESS_SERVER_ADMIN:
            break;

         default:
            throw new IllegalArgumentException("Invalid access level");
      }
   }


 
   private static PSAclHandler systemAclHandler = null;
   /**
    * This is an access mask for allowing no access to the securable object.
    */
   public static final int ACCESS_DENY = 0;

   /**
    * This is a string based ACCESS_DENY
   */
   public static final String ACCESS_DENY_STR = "deny";

   /**
    * This is an access mask for allowing read access to the securable object.
    */
   public static final int ACCESS_READ = 1;

   /**
    * This is a string based ACCESS_READ
   */
   public static final String ACCESS_READ_STR = "read";

   /**
    * This is an access mask for allowing write access to the securable object.
    */
   public static final int ACCESS_WRITE = 2;

   /**
    * This is a string based ACCESS_WRITE
   */
   public static final String ACCESS_WRITE_STR = "write";

   /**
    * This is an access mask for allowing admin access to the securable object.
    */
   public static final int ACCESS_ADMIN = 4;

   /**
    * This is a string based ACCESS_ADMIN
   */
   public static final String ACCESS_ADMIN_STR = "admin";

   /**
    * This is an access mask for allowing server admin access to the
    * securable object.
    */
   public static final int ACCESS_SERVER_ADMIN = Integer.MAX_VALUE;

   /**
    * This is a string based ACCESS_SERVER_ADMIN
   */
   public static final String ACCESS_SERVER_ADMIN_STR = "serverAdmin";

   /**
    * This is an access mask for allowing all permissions on the securable object.
    * This does not add the server admin access.
    */
   public static final int ACCESS_ALL =
      ACCESS_ADMIN | ACCESS_WRITE | ACCESS_READ;

   /**
    * Access mask for storing all the permissions a particular user has on a
    * securable object, initialized in the constructor, never modified after
    * initialization. Defaults to deny access to everyone.
    */
   protected int m_permissions = ACCESS_DENY;

   /**
    * Contains the set of permissions specified on the securable object
    * (such as Folders), may be <code>null</code> if single arg constructor is
    * used, otherwise initialized in the constructor, never modified after
    * initializartion.
    */
   protected PSObjectAcl m_objectAcl = null;

   /**
    * Contains the credentials of the user accessing the securable object,
    * may be <code>null</code> if single arg constructor is used, otherwise
    * initialized in the constructor, never modified after initialization.
    */
   protected PSUserInfo m_userInfo = null;

   /**
    * server access level for the user, initialized to
    * <code></code> which implies no access to the server, set in the two arg
    * constructor
    */
   protected int m_accessLevel = PSAclEntry.SACE_NO_ACCESS;
   protected boolean accessSet;
}




















