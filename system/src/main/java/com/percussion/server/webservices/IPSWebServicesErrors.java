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
package com.percussion.server.webservices;

/**
 * The IPSWebServicesErrors interface is provided as a convenient mechanism
 * for accessing the various web service related error codes. Web Service
 * errors are in the range 14001 - 15000.
 */
public interface IPSWebServicesErrors
{
   /**
    * Content item not found, contentId = xxx, revision = xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the content id</TD></TR>
    * <TR><TD>1</TD><TD>the revision number</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_CONTENT_ITEM_NOT_FOUND = 14001;

   /**
    * Content item could not be checked out, contentId = xxx, revision = xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the content id</TD></TR>
    * <TR><TD>1</TD><TD>the revision number</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_CHECKOUT_FAILURE = 14002;

   /**
    * Content type does not exist, contentTypeId = xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the content type id</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_CONTENT_TYPE_NOT_FOUND = 14003;

   /**
    * Content could not be inserted.
    *
    * <p>
    * No arguments are passed for this message.
    */
   public static final int WEB_SERVICE_INSERT_FAILURE = 14004;

   /**
    * Content item is checked out by someone else, contentId = xxx, 
    * checkedOutBy= xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the content id</TD></TR>
    * <TR><TD>1</TD><TD>the checked out username</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_CHECKOUT_USER_FAILURE = 14005;

   /**
    * Transition does not exist, transitionId = xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the tranisition id</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_TRANSITION_NOT_FOUND = 14006;

   /**
    * Transition comment required, transitionId = xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the tranisition id</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_TRANSITION_COMMENT_REQUIRED = 14007;

   /**
    * Content item could not be validated, contentId = xxx, fieldName=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the content id</TD></TR>
    * <TR><TD>1</TD><TD>the invalid field name</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_VALIDATION_FAILURE = 14008;

   /**
    * Internal search not found, searchName=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the internal search name</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INTERNAL_SEARCH_NOT_FOUND = 14009;

   /**
    * Invalid login credentials, userName=xxx, password=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the username</TD></TR>
    * <TR><TD>1</TD><TD>the password</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_LOGIN_FAILURE = 14010;

   /**
    * Invalid client access, service must be called by the SOAP dispatcher.
    *
    * <p>
    * The arguments passed in for this message are:
    * <p>
    * No arguments are passed for this message.
    */
   public static final int WEB_SERVICE_INVALID_CLIENT_ACESS = 14011;

   /**
    * Invalid search params, the content type id must be the same for all 
    * fields, contentTypeId=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid content type id</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INVALID_SEARCH_PARAMS = 14012;

   /**
    * Invalid search params, the content type id was not found, 
    * contentTypeId=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the invalid content type id</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INVALID_SEARCH_CONTENTTYPE = 14013;

   /**
    * Internal request failed, path=xxx.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the path</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INTERNAL_REQUEST_FAILED = 14014;

   /**
    * Action 'xxx' for port 'xxx' not found. Exception: xxx
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>action name</TD></TR>
    * <TR><TD>1</TD><TD>port name</TD></TR>
    * <TR><TD>2</TD><TD>exception text</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_ACTION_NOT_FOUND = 14015;

   /**
    * openChild action: a child by the specified name was not found in the
    * requested content type
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>child name</TD></TR>
    * <TR><TD>1</TD><TD>content type name</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_ITEM_CHILD_NOT_FOUND = 14016;

   /**
    * various actions: a certain element was expected as the first child of
    * the root, but wasn't found.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>Action name</TD></TR>
    * <TR><TD>1</TD><TD>Parent element name, w/o enclosing angle brackets</TD></TR>
    * <TR><TD>2</TD><TD>Comma separated list of possible child element names,
    *    each w/ enclosing angle brackets.</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_MISSING_ELEMENT = 14017;

   /**
    * A required html parameter was not supplied.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The parameter name</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_MISSING_PARAMETER = 14018;

   /**
    * While attempting to find and invoke the handler for an action, an error
    * occurred.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The action name</TD></TR>
    * <TR><TD>1</TD><TD>The port name</TD></TR>
    * <TR><TD>2</TD><TD>The original exception name</TD></TR>
    * <TR><TD>3</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_DISPATCH_ERROR = 14019;
   
   /**
    * Required content id is missing or invalid.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>action name</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_MISSING_ID = 14020;
   
   /**
    * Mixed ChildId elements are invalid.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>ChildId tag name</TD></TR>
    * <TR><TD>1</TD><TD>action name</TD></TR>
    * </TABLE>
    */
   public static final int INVALID_MIXED_CHILD_IDS = 14021;
   
   /**
    * The folder path submitted with a search does not exist.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The supplied folder path.</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INVALID_FOLDER = 14022;   
   
   /**
    * Promote revision failed because the item could not be checked out.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_PROMOTE_FAILED_CHECKOUT = 14023;

   /**
    * Promote revision failed to complete because the item could not be checked 
    * in.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>The error message</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_PROMOTE_FAILED_CHECKIN = 14024;
   
   /**
    * The specified request does not exist, path={0}.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the path</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_INTERNAL_REQUEST_NOT_FOUND = 14025;
   
   /**
    * The search resource '{0}' does not exist.
    *
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the path</TD></TR>
    * </TABLE>
    */
   public static final int WEB_SERVICE_SEARCH_RESOURCE_NOT_FOUND = 14026;

}

