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

package com.percussion.security;


/**
 * The IPSSecurityErrors inteface is provided as a convenient mechanism
 * for accessing the various security related error codes.
 * The error code ranges are:
 * <TABLE BORDER="1">
 * <TR><TH>Range</TH><TH>Component</TH></TR>
 * <TR><TD>9001 - 9300</TD><TD>general errors used all over</TD></TR>
 * <TR><TD>9301 - 9500</TD><TD>ACL errors</TD></TR>
 * <TR><TD>9501 - 9550</TD><TD>Host Address security provider</TD></TR>
 * <TR><TD>9551 - 9600</TD><TD>Web Server security provider</TD></TR>
 * <TR><TD>9601 - 9650</TD><TD>OS security provider</TD></TR>
 * <TR><TD>9651 - 9700</TD><TD>ODBC security provider</TD></TR>
 * <TR><TD>9701 - 9750</TD><TD>Role security provider</TD></TR>
 * <TR><TD>9751 - 9800</TD><TD>E2 security provider</TD></TR>
 * <TR><TD>9801 - 9850</TD><TD>Directory security provider</TD></TR>
 * <TR><TD>9851 - 9900</TD><TD>Directory security provider</TD></TR>
 * <TR><TD>9901 - 9950</TD><TD>Directory Cataloger</TD></TR>
 * <TR><TD>9951 - 10000</TD><TD>-unassigned-</TD></TR>
 * </TABLE>
 *
 * @author  Tas Giakouminakis
 * @version  1.0
 * @since  1.0
 */
public interface IPSSecurityErrors
{
   /**
    * message for PSAuthenticationUnsupportedException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int AUTHENTICATION_NOT_SUPPORTED = 9001;

   /**
    * message for PSAuthenticationFailedException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * <TR><TD>1</TD><TD>security provider instance name</TD></TR>
    * <TR><TD>2</TD><TD>user name</TD></TR>
    * </TABLE>
    */
   public static final int AUTHENTICATION_FAILED   = 9002;

   /**
    * message for PSUnsupportedProviderException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider class name</TD></TR>
    * <TR><TD>1</TD><TD>requested security provider type</TD></TR>
    * </TABLE>
    */
   public static final int PROVIDER_NOT_SUPPORTED_BY_CLASS = 9003;

   /**
    * message for PSFiltersNotSupportedException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int FILTERS_NOT_SUPPORTED   = 9004;

   /**
    * message for PSUsersNotSupportedException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int USERS_NOT_SUPPORTED    = 9005;

   /**
    * message for PSGroupsNotSupportedException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int GROUPS_NOT_SUPPORTED    = 9006;

   /**
    * message for PSAuthenticationRequiredException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>resource type</TD></TR>
    * <TR><TD>1</TD><TD>resource name</TD></TR>
    * </TABLE>
    */
   public static final int AUTHENTICATION_REQUIRED   = 9007;

   /**
    * session based message for PSAuthorizationException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>resource type</TD></TR>
    * <TR><TD>1</TD><TD>resource name</TD></TR>
    * <TR><TD>2</TD><TD>session id</TD></TR>
    * </TABLE>
    */
   public static final int SESS_NOT_AUTHORIZED    = 9008;

   /**
    * user based message for PSAuthorizationException
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>resource type</TD></TR>
    * <TR><TD>1</TD><TD>resource name</TD></TR>
    * <TR><TD>2</TD><TD>security provider</TD></TR>
    * <TR><TD>3</TD><TD>user name</TD></TR>
    * </TABLE>
    */
   public static final int USER_NOT_AUTHORIZED    = 9009;

   /**
    * initialization of the specified security provider failed due to
    * an exception
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * <TR><TD>1</TD><TD>instance name</TD></TR>
    * <TR><TD>2</TD><TD>exception information</TD></TR>
    * </TABLE>
    */
   public static final int PROVIDER_INIT_EXCEPTION   = 9010;

   /**
    * the security provider type is unknown and cannot be initialized
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * <TR><TD>1</TD><TD>instance name</TD></TR>
    * </TABLE>
    */
   public static final int PROVIDER_UNKNOWN     = 9011;

   /**
    * authentication failed through a security provider for the specified
    * reason
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * <TR><TD>1</TD><TD>security provider instance name</TD></TR>
    * <TR><TD>2</TD><TD>user name</TD></TR>
    * <TR><TD>3</TD><TD>the failure message</TD></TR>
    * </TABLE>
    */
   public static final int AUTHENTICATION_FAILED_WITH_MSG = 9012;

   /**
    * cataloging for the specified security provider has been disabled
    * due to an initialization exception
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * <TR><TD>1</TD><TD>instance name</TD></TR>
    * <TR><TD>2</TD><TD>exception information</TD></TR>
    * </TABLE>
    */
   public static final int PROVIDER_INIT_CATALOG_DISABLED = 9013;

   /**
    * when security provider instance name is used as key to put
    * objects into a hashmap, this integer is a warning message code.
    */
   public static final int PROVIDER_INSTANCE_NAME_DUPLICATED = 9014;

   /**
    * when calling a native method to process authentication, it failed to give
    * a normal result.
    */
   public static final int NATIVE_AUTHENTICATION_FAILURE = 9015;

   /**
    * The server requires a larger key strength than the one
    * associated with a request.
    *
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>session id for the request</TD></TR>
    * <TR><TD>1</TD><TD>required strength (in bits)</TD></TR>
    * <TR><TD>2</TD><TD>supplied strength (in bits)</TD></TR>
    * </TABLE>
    */
   public static final int SSL_KEY_STRENGTH_TOO_WEAK = 9016;

   /**
    * An error occurred in the data encryption handler.
    * <p>
    * No arguments for this message.
    */
   public static final int DATA_ENCRYPTION_ERROR_MSG = 9017;

   /**
    * The security subsystem has not been initialized, so no
    * authorizations can be performed.
    * <p>
    * No arguments for this message.
    */
   public static final int SECURITY_NOT_INITIALIZED = 9018;

   /**
    * The secret size passed in to the IPSSecretKey is an invalid length.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the expected secret size</TD></TR>
    * <TR><TD>1</TD><TD>the specified secret size</TD></TR>
    * </TABLE>
    */
   public static final int SECRET_KEY_INVALID_SIZE  = 9019;

   /**
    * If multiple security providers are used during login, and none of them
    * succeed, this error may be returned.<p/>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>A concatenation of all the error messages returned from
    * the providers that failed to authenticate.</TD></TR>
    * </TABLE>
    */
   public static final int MULTI_AUTHENTICATION_FAILED   = 9020;

   /**
    * When detailed error messages about failures is disabled, this message
    * is returned.
    * <p>
    * No arguments for this message.
    */
   public static final int GENERIC_AUTHENTICATION_FAILED = 9021;

   /**
    * If a security system can't access the data it needs to perform a
    * requested action, (say meta data was stored in a db and the db couldn't
    * be reaached), this error is returned.
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The detailed information describing the problem.</TD>
    *    </TR>
    * </TABLE>
    */
   public static final int METADATA_UNAVAILABLE   = 9022;

   /**
    * An invalid wildcard was supplied in a search filter
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The wildcard</TD></TR>
    * <TR><TD>1</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int FILTER_WILDCARD_INVALID = 9023;

   /**
    * Failed to get groups from the directory
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The group provider name</TD></TR>
    * <TR><TD>1</TD><TD>The directory name</TD></TR>
    * <TR><TD>2</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int GET_GROUPS_FAILURE = 9024;

   /**
    * Failed to check group membership with a security provider
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The provider type</TD></TR>
    * <TR><TD>1</TD><TD>The provider name</TD></TR>
    * <TR><TD>2</TD><TD>The user name</TD></TR>
    * <TR><TD>3</TD><TD>The group name</TD></TR>
    * <TR><TD>4</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int CHECK_GROUP_MEMBER_FAILURE = 9025;

   /**
    * Group provider named by a security provider is missing
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The group provider name</TD></TR>
    * </TABLE>
    */
   public static final int GROUP_PROVIDER_MISSING = 9026;

   /**
    * PSHostAddressFilterEntry constructor called with a filter string with
    * no filter (asterisk)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the specified filter</TD></TR>
    * </TABLE>
    */
   public static final int HOST_ADDR_FILTER_INVALID  = 9501;

   /**
    * PSOsProvider.impersonate was called to impersonate user id and password.
    * Pass-in arguments are not needed.
    */
   public static final int OS_IMPERSONATE_FAILURE   = 9601;

   /**
    * PSOsProviderMeta.getObjects was called but the calling of native
    * impersonate method failed. Pass-in arguments are not needed.
    */
   public static final int OSMETA_GET_OBJECTS_FAILURE  = 9602;

   /**
    * PSOsProviderMeta.getObjectTypes was called but the calling of native
    * impersonate method failed. Pass-in arguments are not needed.
    */
   public static final int OSMETA_GET_OBJECT_TYPES_FAILURE = 9603;

   /**
    * PSOsProviderMeta.getServers was called but the calling of native
    * impersonate method failed. Pass-in arguments are not needed.
    */
   public static final int OSMETA_GET_SERVERS_FAILURE  = 9604;

   /**
    * PSOsProviderMeta.getAttributes was called but the calling of native
    * impersonate method failed. Pass-in arguments are not needed.
    */
   public static final int OSMETA_GET_ATTRIBUTES_FAILURE = 9605;

   /**
    * PSRoleProvider.createRoleEntry was called with a role name which was
    * not previously defined using PSRoleProvider.defineRoleEntry
    * (for app ACL)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the role name</TD></TR>
    * <TR><TD>1</TD><TD>the application name</TD></TR>
    * </TABLE>
    */
   public static final int LOCAL_ROLE_NOT_DEFINED   = 9701;

   /**
    * PSRoleProvider.createRoleEntry was called with a role name which was
    * not previously defined using PSRoleProvider.defineRoleEntry
    * (for server ACL)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the role name</TD></TR>
    * </TABLE>
    */
   public static final int GLOBAL_ROLE_NOT_DEFINED   = 9702;

   /**
    * PSRoleProvider.defineRoleEntry was called with a role name which was
    * already defined (for private roles)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the role name</TD></TR>
    * <TR><TD>1</TD><TD>the application name</TD></TR>
    * </TABLE>
    */
   public static final int LOCAL_ROLE_ALREADY_DEFINED  = 9703;

   /**
    * PSRoleProvider.defineRoleEntry was called with a role name which was
    * already defined (for global roles)
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the role name</TD></TR>
    * </TABLE>
    */
   public static final int GLOBAL_ROLE_ALREADY_DEFINED = 9704;

   /**
    * PSRoleProvider/createRoleEntry was called with a null role name
    * <p>
    * No arguments for this message.
    */
   public static final int ROLE_NAME_REQD      = 9705;

   /**
    * PSRoleProvider/defineRoleEntry was called with a null role
    * <p>
    * No arguments for this message.
    */
   public static final int ROLE_DEF_REQD      = 9706;

   /**
    * PSRoleManager/addApplicationRoles is called with a role name
    * already defined, but overwrite is not specified.
    */
   public static final int ROLE_OVERWRITE_REQD = 9707;

   /**
    * Could not obtain a handler for the application that is used to
    * catalog role stuff. This could be because the app isn't running or
    * because it is missing a necessary resource.
    */
   public static final int ROLE_METADATA_RESOURCE_NOT_FOUND = 9708;

   /**
    * Authentication of user through directory provider failed.
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the user name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_AUTHENTICATION_FAILED = 9801;

   /**
    * Multiple entries were returned when trying to look up a single
    * user, therefore authentication failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the user name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_MULTIPLE_ENTRIES_RETURNED = 9802;

   /**
    * An error occurred trying to initialize the password filter
    * instance.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the password filter class name</TD></TR>
    * <TR><TD>1</TD><TD>additional error text</TD></TR>
    * </TABLE>
    */
   public static final int DIR_PASSWORD_FILTER_INIT_ERROR = 9803;

   /**
    * No password supplied for authentication using scheme other than "none".
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>security provider type</TD></TR>
    * </TABLE>
    */
   public static final int DIR_PASSWORD_REQUIRED = 9804;

   /**
    * MetaData failed to retrieve objects from the directory.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error</TD></TR>
    * </TABLE>
    */
   public static final int DIR_GET_OBJECTS_FAILED = 9805;

   /**
    * Referenced directory set not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The referenced directory set name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_REFERENCED_DIRECTORYSET_NOT_FOUND = 9806;

   /**
    * Referenced directory not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The referenced directory name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_REFERENCED_DIRECTORY_NOT_FOUND = 9807;

   /**
    * Referenced authentication not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The referenced authentication name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_REFERENCED_AUTHENTICATION_NOT_FOUND = 9808;

   /**
    * Referenced role provider not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The referenced role provider name</TD></TR>
    * </TABLE>
    */
   public static final int DIR_REFERENCED_ROLEPROVIDER_NOT_FOUND = 9809;

   /**
    * An error occurred trying to shut down the back end table
    *   resources.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the provider instance</TD></TR>
    * <TR><TD>1</TD><TD>additional error text from jdbc</TD></TR>
    * </TABLE>
    */
   public static final int BETABLE_ERROR_CLOSING_RESOURCES = 9851;

   /**
    * The back end table found multiple user entries with the same
    *   user id.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the provider instance</TD></TR>
    * <TR><TD>1</TD><TD>the user id that has duplicates</TD></TR>
    * </TABLE>
    */
   public static final int BETABLE_ERROR_UID_NOT_UNIQUE = 9852;

   /**
    * If no email attribute name is specified, an unsupported exception will
    * be thrown with this error message if one makes a request for a users
    * email address.
    */
   public static final int NO_EMAIL_ATTRIBUTE_NAME = 9853;

   /**
    * Unknown error occurred during a directory catalog request to the
    * backend directory cataloger.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception message</TD></TR>
    * </TABLE>
    */
   public static final int BETABLE_DIRECTORY_CATALOGER_ERROR = 9854;

   /**
    * The requested catalog provider class was not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_PROVIDER_CLASS_NOT_FOUND = 9855;

   /**
    * The requested catalog provider class instantiation failed.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_PROVIDER_INSTANTIATION_FAILED = 9856;

   /**
    * The insufficient access for the requested catalog provider class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_PROVIDER_ILLEGAL_ACCESS = 9857;

   /**
    * The invocation target error while instantiating the requested catalog
    * provider class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_PROVIDER_INVOCATION_TARGET_ERROR = 9858;

   /**
    * A required constructor was not found while instantiating the requested
    * catalog provider class.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the requested class</TD></TR>
    * <TR><TD>1</TD><TD>the stack trace</TD></TR>
    * </TABLE>
    */
   public static final int CATALOG_PROVIDER_NO_SUCH_METHOD = 9859;

   /**
    * Referenced directory not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the directory name</TD></TR>
    * </TABLE>
    */
   public static final int REFERENCED_DIRECTORY_NOT_FOUND = 9860;

   /**
    * Referenced authentication not found.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the authentication name</TD></TR>
    * </TABLE>
    */
   public static final int REFERENCED_AUTHENTICATION_NOT_FOUND = 9861;

   /**
    * Authentication failed for directory request.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the directory name</TD></TR>
    * <TR><TD>1</TD><TD>the authentication name</TD></TR>
    * <TR><TD>2</TD><TD>the error message</TD></TR>
    * </TABLE>
    */
   public static final int DIRECTORY_AUTHENTICATION_FAILED = 9862;

   /**
    * A required attribute name is missing.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the name of the required attribute</TD></TR>
    * </TABLE>
    */
   public static final int MISSING_REQUIRED_ATTRIBUTE = 9901;

   /**
    * An unknown naming exception was caught.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception message</TD></TR>
    * </TABLE>
    */
   public static final int UNKNOWN_NAMING_ERROR = 9902;
   
   /**
    * An error occurred trying to parse the provider url.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exception message</TD></TR>
    * </TABLE>
    */
   public static final int PARSE_JNDI_PROVIDER_URL_ERROR = 9903;
}



