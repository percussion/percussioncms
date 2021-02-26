/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.data;

import com.percussion.design.objectstore.PSAcl;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUserContext;
import com.percussion.security.PSAclHandler;
import com.percussion.security.PSAuthenticationRequiredException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSRoleManager;
import com.percussion.security.PSSecurityException;
import com.percussion.security.PSSecurityToken;
import com.percussion.security.PSUserAttributes;
import com.percussion.security.PSUserEntry;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.PSUserSession;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang3.time.FastDateFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The PSUserContextExtractor class is used to extract data from the
 * user session associated with the request.
 * <P>
 * The user context object uses the following XML structure:
 * <P>
 * <TABLE BORDER="1">
 *    <TR><TH>Element</TH><TH>Description</TH></TR>
 *    <TR>
 *       <TD>SessionId</TD>
 *       <TD>The session identifier associated with this request</TD>
 *    </TR>
 *    <TR>
 *       <TD>SessionCreateTime</TD>
 *       <TD>The create time stamp for the session associated with this request</TD>
 *    </TR>
 *    <TR>
 *       <TD>User</TD>
 *       <TD>An entry for each user the requestor has been authenticated as</TD>
 *    </TR>
 *    <TR>
 *       <TD>User/Name</TD>
 *       <TD>The name of this user</TD>
 *    </TR>
  *    <TR>
 *       <TD>User/Attributes</TD>
 *       <TD>The attributes associated with this user (security provider
 *       specific.</TD>
 *    </TR>
 *    <TR>
 *       <TD>Roles</TD>
 *       <TD>The roles the requestor is a member of</TD>
 *    </TR>
 *    <TR>
 *       <TD>Roles/RoleName</TD>
 *       <TD>The name of the role the requestor is a member of</TD>
 *    </TR>
 *    <TR>
 *       <TD>Roles/RoleNameinclause</TD>
 *       <TD>The list of the role the requestor is a member of, formatted
 *        as a SQL &quot;IN&quot; clause string</TD>
 *    </TR>
 *    <TR>
 *       <TD>DataAccessRights</TD>
 *       <TD>The data access rights assigned to the requestor</TD>
 *    </TR>
 *    <TR>
 *       <TD>DataAccessRights/query</TD>
 *       <TD>1 if the requestor can query; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DataAccessRights/insert</TD>
 *       <TD>1 if the requestor can insert; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DataAccessRights/update</TD>
 *       <TD>1 if the requestor can update; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DataAccessRights/delete</TD>
 *       <TD>1 if the requestor can delete; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DesignAccessRights</TD>
 *       <TD>The data access rights assigned to the requestor</TD>
 *    </TR>
 *    <TR>
 *       <TD>DesignAccessRights/modifyAcl</TD>
 *       <TD>1 if the requestor can modify the applciation's ACL; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DesignAccessRights/readDesign</TD>
 *       <TD>1 if the requestor can read the application design; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DesignAccessRights/updateDesign</TD>
 *       <TD>1 if the requestor can update the application design; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>DesignAccessRights/deleteDesign</TD>
 *       <TD>1 if the requestor can delete the application design; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>ServerAccessRights/dataAccess</TD>
 *       <TD>1 if the requestor has data access on the server; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>ServerAccessRights/designAccess</TD>
 *       <TD>1 if the requestor has design access on the server; 0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>ServerAccessRights/createApplications</TD>
 *       <TD>1 if the requestor can create new applications on the server; 
 *          0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>ServerAccessRights/deleteApplications</TD>
 *       <TD>1 if the requestor can delete applications on the server; 
 *          0 otherwise</TD>
 *    </TR>
 *    <TR>
 *       <TD>ServerAccessRights/administerServer</TD>
 *       <TD>1 if the requestor has server administration rights; 0 otherwise</TD>
 *    </TR>
 * </TABLE>
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSUserContextExtractor extends PSDataExtractor
{
   /**
    * Construct an object from its object store counterpart.
    *
    * @param   source      the object defining the source of this value
    */
   public PSUserContextExtractor( PSUserContext source )
   {
      super(source);

      m_sourceName = source.getName();
      m_sourceType = getType(m_sourceName.toLowerCase());

      if (m_sourceType == TOK_USER_ATTRIBUTE)
      {
         // store the attribute name only
         m_sourceName = m_sourceName.substring(PREFIX_USER_ATTRIBUTES.length());
      }
      else if (m_sourceType == TOK_USER_SESSIONOBJECT)
      {
         // store the object key only
         m_sourceName = m_sourceName.substring(
            PREFIX_USER_SESSION_OBJECT.length());
      }
   }

   /**
    * Get the integer type code based on the name of the field.
    *
    * @param  contextFieldName  the name of the user context/attribute desired
    *
    * @return the integer type code representing the type of context
    *    information, or <code>0</code> if it is unknown.
    */
   private static int getType(String contextFieldName)
   {
      int sourceType = TOK_UNKNOWN;

      if ("sessionid".equals(contextFieldName))
         sourceType = TOK_SESSION_ID;
      else if ("sessioncreatetime".equals(contextFieldName))
         sourceType = TOK_SESSION_CREATE_TIME;
      else if ("user/name".equals(contextFieldName))
         sourceType = TOK_USER_NAME;
      else if ("user/securityprovider".equals(contextFieldName))
         sourceType = TOK_SEC_PROVIDER;
      else if ("user/securityprovidertypeid".equals(contextFieldName))
         sourceType = TOK_SEC_PROVIDER_TYPE_ID;
      else if ("user/securityproviderinstance".equals(contextFieldName))
         sourceType = TOK_SEC_PROVIDER_INST;
      else if (contextFieldName.startsWith(PREFIX_USER_ATTRIBUTES) &&
         contextFieldName.length() > PREFIX_USER_ATTRIBUTES.length())
      {
         sourceType = TOK_USER_ATTRIBUTE;
      }
      else if (contextFieldName.startsWith(PREFIX_USER_SESSION_OBJECT) &&
         contextFieldName.length() > PREFIX_USER_SESSION_OBJECT.length())
      {
         sourceType = TOK_USER_SESSIONOBJECT;
      }
      else if ("roles/rolename".equals(contextFieldName))   // fixed bug id Rx-99-10-0208 (was using role/rolename)
         sourceType = TOK_ROLE_NAME;
      else if ("roles/rolenameinclause".equals(contextFieldName))
         sourceType = TOK_ROLE_NAME_INCLAUSE;
      else if ("dataaccessrights/query".equals(contextFieldName))
         sourceType = TOK_DATA_ACC_QUERY;
      else if ("dataaccessrights/insert".equals(contextFieldName))
         sourceType = TOK_DATA_ACC_INSERT;
      else if ("dataaccessrights/update".equals(contextFieldName))
         sourceType = TOK_DATA_ACC_UPDATE;
      else if ("dataaccessrights/delete".equals(contextFieldName))
         sourceType = TOK_DATA_ACC_DELETE;
      else if ("designaccessrights/modifyacl".equals(contextFieldName))
         sourceType = TOK_DESIGN_ACC_MOD_ACL;
      else if ("designaccessrights/readdesign".equals(contextFieldName))
         sourceType = TOK_DESIGN_ACC_READ;
      else if ("designaccessrights/updatedesign".equals(contextFieldName))
         sourceType = TOK_DESIGN_ACC_UPDATE;
      else if ("designaccessrights/deletedesign".equals(contextFieldName))
         sourceType = TOK_DESIGN_ACC_DELETE;
      else if ("serveraccessrights/noaccess".equals(contextFieldName))
         sourceType = TOK_SERVER_NO_ACCESS;
      else if ("serveraccessrights/dataaccess".equals(contextFieldName))
         sourceType = TOK_SERVER_DATA_ACCESS;
      else if ("serveraccessrights/designaccess".equals(contextFieldName))
         sourceType = TOK_SERVER_DESIGN_ACCESS;
      else if ("serveraccessrights/createapplications".equals(contextFieldName))
         sourceType = TOK_SERVER_CREATEAPPLICATIONS_ACCESS;
      else if ("serveraccessrights/deleteapplications".equals(contextFieldName))
         sourceType = TOK_SERVER_DELETEAPPLICATIONS_ACCESS;
      else if ("serveraccessrights/administerserver".equals(contextFieldName))
         sourceType = TOK_SERVER_ADMIN_ACCESS;

      return sourceType;
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   data    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @return              the associated value; <code>null</code> if a
    *                      value is not found
    *
    * @throws  PSDataExtractionException
    *                      if an error condition causes the extraction to
    *                      fail. This is not thrown if the requested data
    *                      does not exist.
    */
   public Object extract(PSExecutionData data)
      throws PSDataExtractionException
   {
      return extract(data, null);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   data    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue    the default value to use if a value is not found
    *
    * @return              the associated value; <code>defValue</code> if a
    *                      value is not found
    *
    * @throws  PSDataExtractionException
    *                      if an error condition causes the extraction to
    *                      fail. This is not thrown if the requested data
    *                      does not exist.
    */
   public Object extract(PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      return getUserContextInfo(m_sourceType, m_sourceName, data, defValue);
   }

   /**
    * Extract a data value using the run-time data.
    *
    * @param   sourceType  the source type code for the context information
    *                      requested, must be one of the values returned by
    *                      {@link #getType(String)}
    *
    * @param   sourceName  the name representing the context information
    *                         desired
    *
    * @param   data    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue    the default value to use if a value is not found
    *
    * @return              the associated value; <code>defValue</code> if a
    *                      value is not found
    *
    * @throws  PSDataExtractionException
    *                      if an error condition causes the extraction to
    *                      fail. This is not thrown if the requested data
    *                      does not exist.
    */
   public static Object getUserContextInfo(int sourceType, String sourceName, PSExecutionData data, Object defValue)
      throws PSDataExtractionException
   {
      PSRequest request = (data == null) ? null : data.getRequest();

      return getUserContextInfo(sourceType, sourceName, request, defValue);
   }

   /**
    * Extract a data value using the request and user session data.
    *
    * @param   sourceType  the source type code for the context information
    *                      requested, must be one of the values returned by
    *                      {@link #getType(String)}
    *
    * @param   sourceName  the name representing the context information
    *                      desired
    *
    * @param   request     the request information associates with the current
    *                      execution data; if <code>null</code>, it is not
    *                      possible to extract data and the defValue will be
    *                      returned
    *
    * @param   defValue    the default value to use if a value is not found
    *
    * @return              the associated value; <code>defValue</code> if a
    *                      value is not found
    *
    * @throws  PSDataExtractionException
    *                      if an error condition causes the extraction to
    *                      fail, or if an invalid type is specified. This is not
    *                      thrown if the requested data does not exist.
    */
   public static Object getUserContextInfo(int sourceType,
         String sourceName, PSRequest request,
         Object defValue)
      throws PSDataExtractionException
   {
      Object value = null;
      Object[] valueArray = null;
      int size;

      PSUserSession sess = (request == null) ? null : request.getUserSession();

      if (sess != null) {
         switch (sourceType) {
            case TOK_SESSION_ID:
               value = sess.getId();
               break;

           case TOK_SESSION_CREATE_TIME:
                FastDateFormat sdf = FastDateFormat.getInstance(SESSION_CREATE_DATE_FORMAT);
                value = sdf.format(sess.getCreateTimeStamp());
                break;


            case TOK_USER_NAME:
            case TOK_SEC_PROVIDER:
            case TOK_SEC_PROVIDER_TYPE_ID:
            case TOK_SEC_PROVIDER_INST:
            case TOK_USER_ATTRIBUTE:
            {
               PSUserEntry[] users = sess.getAuthenticatedUserEntries();
               size = (users == null) ? 0 : users.length;
               if (size != 0)
               {
                  valueArray = new Object[size];
                  for (int i = 0; i < size; i++)
                  {
                     if (sourceType == TOK_USER_NAME)
                        valueArray[i] = users[i].getName();
                     else if (sourceType == TOK_SEC_PROVIDER)
                        valueArray[i] = "";
                     else if (sourceType == TOK_SEC_PROVIDER_TYPE_ID)
                        valueArray[i] = "";
                     else if (sourceType == TOK_SEC_PROVIDER_INST)
                        valueArray[i] = "";
                     else if (sourceType == TOK_USER_ATTRIBUTE)
                     {
                        try
                        {
                           /* the attribute can be global or role specific,
                              if role specific, then the format is <rolename>/
                              <attributename> */
                           int pos = sourceName.indexOf('/');
                           if ( pos > 0 )
                           {
                              String roleName = sourceName.substring(0, pos);
                              String attribName = sourceName.substring(pos+1);
                              List values = getRoleSubjectAttributeValues(
                                 users[i], roleName, attribName);
                              // if attr has more than one value, only return
                              // the first (CSV would not be safe)
                              Iterator iter = values.iterator();
                              if ( iter.hasNext())
                              {
                                 Object objVal = iter.next();
                                 if (objVal != null)
                                    valueArray[i] = objVal.toString();
                              }
                           }
                           else
                           {
                              List values = new ArrayList();

                              /* First check subject for the attibute value.  If
                               * not found, check the provider next (subject
                               * overrides the provider on a collision).
                               */
                              values = getSubjectAttributeValues(users[i], 
                                 sourceName);

                              if (values.isEmpty())
                              {
                                 // get security provider specific attributes
                                 PSUserAttributes attribs =
                                       users[i].getAttributes();
                                 if (attribs != null)
                                 {
                                    values.add(attribs.getString(sourceName));
                                 }
                              }

                              // if attr has more than one value, only return
                              // the first (CSV would not be safe)
                              Iterator iter = values.iterator();
                              if ( iter.hasNext())
                              {
                                 Object objVal = iter.next();
                                 if (objVal != null)
                                    valueArray[i] = objVal.toString();
                              }
                           }

                           if (valueArray[i] == null)
                              valueArray[i] = defValue;
                        }
                        catch ( PSSecurityException e )
                        {
                           throw new PSDataExtractionException(
                                 e.getErrorCode(), e.getErrorArguments());
                        }
                     }
                  }

                  value = (Object)(convertToLiteralSet(valueArray));
               }
               break;
            }
            case TOK_USER_SESSIONOBJECT:
                value = sess.getPrivateObject(sourceName);
                break;
            case TOK_ROLE_NAME:
               try
               {
                  PSUserEntry [] users = sess.getAuthenticatedUserEntries();
                  if ( null == users || users.length == 0)
                  {
                     valueArray = new Object[1];
                     valueArray[0] = defValue;
                  }
                  else
                  {
                     List members = sess.getUserRoles();
                     valueArray = members.toArray();
                  }
                  value = (Object)(convertToLiteralSet(valueArray));
               }
               catch ( PSSecurityException e )
               {
                  throw new PSDataExtractionException( e.getErrorCode(),
                        e.getErrorArguments());
               }
               break;

            case TOK_ROLE_NAME_INCLAUSE:
               try
               {
                  PSUserEntry [] users = sess.getAuthenticatedUserEntries();
                  String inclause = "";
                  if ( null == users || users.length == 0)
                  {
                    inclause = "'"+defValue+"'";
                  }
                  else
                  {
                     List members = sess.getUserRoles();


                     // build the "IN" string
                     StringBuffer buf = new StringBuffer();
                     Iterator values = members.iterator();
                     while (values.hasNext())
                     {
                        Object o = values.next();
                        if (o != null)
                        {
                           String val = o.toString();
                           if (val.trim().length() != 0)
                           {
                              if (buf.length() != 0)
                                 buf.append(", ");
                              buf.append('\'');
                              buf.append(o.toString());
                              buf.append('\'');
                           }
                        }
                     }

                     if (buf.length() > 0)
                        inclause = buf.toString();

                  }
                  value = inclause;
               }
               catch ( PSSecurityException e )
               {
                  throw new PSDataExtractionException( e.getErrorCode(),
                        e.getErrorArguments());
               }
               break;

            case TOK_DATA_ACC_QUERY:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DATA_QUERY) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DATA_ACC_INSERT:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DATA_CREATE) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DATA_ACC_UPDATE:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DATA_UPDATE) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DATA_ACC_DELETE:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DATA_DELETE) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DESIGN_ACC_MOD_ACL:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DESIGN_MODIFY_ACL) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DESIGN_ACC_READ:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DESIGN_READ) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DESIGN_ACC_UPDATE:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DESIGN_UPDATE) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;

            case TOK_DESIGN_ACC_DELETE:
               if ((request.getCurrentApplicationAccessLevel() &
                  PSAclEntry.AACE_DESIGN_DELETE) != 0)
               {
                  value = "1";
               }
               else
                  value = "0";
               break;
            case TOK_SERVER_DATA_ACCESS:
            case TOK_SERVER_DESIGN_ACCESS:
            case TOK_SERVER_CREATEAPPLICATIONS_ACCESS:
            case TOK_SERVER_DELETEAPPLICATIONS_ACCESS:
            case TOK_SERVER_ADMIN_ACCESS:
               {
                  PSServerConfiguration config =
                     PSServer.getServerConfiguration();
                  PSAcl acl = config.getAcl();
                  PSAclHandler aclHandler = new PSAclHandler(acl);
                  PSSecurityToken tok = request.getSecurityToken();
                  int accessLevel;
                  try
                  {
                     accessLevel = aclHandler.getUserAccessLevel(tok);
                  }
                  catch (PSAuthenticationRequiredException e1)
                  {
                     throw new PSDataExtractionException(
                        e1.getErrorCode(),
                        e1.getErrorArguments());
                  }
                  catch (PSAuthorizationException e1)
                  {
                     throw new PSDataExtractionException(
                        e1.getErrorCode(),
                        e1.getErrorArguments());
                  }
                  switch(sourceType)
                  {
                     case TOK_SERVER_DATA_ACCESS:
                        if((accessLevel&PSAclEntry.SACE_ACCESS_DATA)!=0)
                           value = "1";
                        else
                           value = "0";
                        break;
                     case TOK_SERVER_DESIGN_ACCESS:
                        if((accessLevel&PSAclEntry.SACE_ACCESS_DESIGN)!=0)
                           value = "1";
                        else
                           value = "0";
                        break;
                     case TOK_SERVER_CREATEAPPLICATIONS_ACCESS:
                        if((accessLevel&PSAclEntry.SACE_CREATE_APPLICATIONS)!=0)
                           value = "1";
                        else
                           value = "0";
                        break;
                     case TOK_SERVER_DELETEAPPLICATIONS_ACCESS:
                        if((accessLevel&PSAclEntry.SACE_DELETE_APPLICATIONS)!=0)
                           value = "1";
                        else
                           value = "0";
                        break;
                     case TOK_SERVER_ADMIN_ACCESS:
                        if((accessLevel&PSAclEntry.SACE_ADMINISTER_SERVER)!=0)
                           value = "1";
                        else
                           value = "0";
                        break;
                  }
               }
               break;
            default:
               Object args[] = {String.valueOf(sourceType), sourceName};
               throw new PSDataExtractionException(
                  IPSDataErrors.USER_CTX_INVALID_TYPE, args);
         }
      }

      return (value == null) ? defValue : value;
   }


   /**
    * Returns an xml element containing all user context information extracted
    * from the given request.  Format of the xml is:
    * <pre><code>
    * &lt;!--
    * PSXUserContextExtractor defines all data that can be
    * extracted by the PSUserContextExtractor
    * --&gt;
    * &lt;!ELEMENT PSXUserContextExtractor (SessionId, User*, Roles*,
    * DataAccessRights, DesignAccessRights)&gt;
    * &lt;!--
    * SessionId - The session identifier associated with this request
    * --&gt;
    * &lt;!ELEMENT SessionId (#PCDATA)&gt;
    * &lt;!--
    * SessionCreateTime - The create time stamp for the session associated with this request
    * --&gt;
    * &lt;!ELEMENT SessionCreateTime (#PCDATA)&gt;
    * &lt;!--
    * User - An entry for each user the requestor has been authenticated as
    * --&gt;
    * &lt;!ELEMENT User (Name, SecurityProvider, SecurityProviderTypeId,
    * SecurityProviderInstance, * Attribute*)&gt;
    * &lt;!--
    * User/Name - The name of this user
    * --&gt;
    * &lt;!ELEMENT Name (CDATA)&gt;
    * User/Attributes - The attributes associated with this user (security
    * provider specific).
    * --&gt;
    * &lt;!ELEMENT Attribute (CDATA)&gt;
    * &lt;!ATTLIST Attribute
    * name CDATA #REQUIRED
    * value CDATA #REQUIRED&gt;
    * &lt;!--
    * Roles - The roles the requestor is a member of
    * --&gt;
    * &lt;!ELEMENT Roles (RoleName*)&gt;
    * &lt;!--
    * Roles/RoleName - The name of the role the requestor is a member of
    * --&gt;
    * &lt;!ELEMENT RoleName (CDATA)&gt;
    * &lt;!--
    * DataAccessRights The data access rights assigned to the requestor
    * Attributes:
    * query - 1 if the requestor can query; 0 otherwise
    * insert - 1 if the requestor can insert; 0 otherwise
    * update - 1 if the requestor can update; 0 otherwise
    * delete - 1 if the requestor can delete; 0 otherwise
    * --&gt;
    * &lt;!ELEMENT DataAccessRights EMPTY&gt;
    * &lt;!ATTLIST DataAccessRights
    * query (1 | 0) '0'
    * insert (1 | 0) '0'
    * update (1 | 0) '0'
    * delete (1 | 0) '0'&gt;
    * &lt;!--
    * DesignAccessRights The data access rights assigned to the requestor
    * Attributes:
    * modifyAcl - 1 if the requestor can modify the applciation's ACL; 0
    * otherwise
    * readDesign - 1 if the requestor can read the application design; 0
    * otherwise
    * updateDesign - 1 if the requestor can update the application design; 0
    * otherwise
    * deleteDesign - 1 if the requestor can delete the application design; 0
    * otherwise
    * --&gt;
    * &lt;!ELEMENT DesignAccessRights EMPTY&gt;
    * &lt;!ATTLIST DesignAccessRights
    * modifyAcl (1 | 0) '0'
    * readDesign (1 | 0) '0'
    * updateDesign (1 | 0) '0'
    * deleteDesign (1 | 0) '0'&gt;
    * </code></pre>
    * @param request the request from which to extract the user context
    * information.
    * May not be <code>null</code>.
    */
   public static Element toXml(PSRequest request)
   {
      if (request == null)
         throw new IllegalArgumentException("request may not be null");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc, NODE_NAME);

      // add session id node
      PSUserSession sess = request.getUserSession();
      if (sess == null)
         return root;

      PSXmlDocumentBuilder.addElement(doc, root, "SessionId", sess.getId());


      //add session create time node
       FastDateFormat sdf = FastDateFormat.getInstance(SESSION_CREATE_DATE_FORMAT);
       PSXmlDocumentBuilder.addElement(doc, root, "SessionCreateTime",
                                 sdf.format(sess.getCreateTimeStamp()));


      // add User nodes
      PSUserEntry[] users = sess.getAuthenticatedUserEntries();

      for (int i=0; i < users.length; i++)
      {
         Element user = PSXmlDocumentBuilder.addEmptyElement(doc, root, "User");

         // add Name, SecurityProvider, SecurityProviderTypeId,
         //SecurityProviderInstance, Attributes to User
         PSXmlDocumentBuilder.addElement(doc, user, "Name", users[i].getName());

         // add an attribute node for each user
         PSUserAttributes attributes = users[i].getAttributes();
         if (attributes != null )
         {
            Iterator keys = attributes.keySet().iterator();
            while (keys.hasNext())
            {
               String key = (String)keys.next();
               Element attrib = PSXmlDocumentBuilder.addEmptyElement(doc,
                                                         user, "Attribute");
               attrib.setAttribute("name", key);

               PSXmlDocumentBuilder.addElement(doc, user, "Value",
                     attributes.getString(key));
            }
         }

         try
         {
            List subjects = 
               PSRoleManager.getInstance().getSubjectGlobalAttributes(
                  users[i].getName(), PSSubject.SUBJECT_TYPE_USER, null, null);

            if ( subjects.size() > 0 )
            {
               /**
                * The call made to get the subject list should only return
                * one entry.
                */
               PSSubject subject = (PSSubject) subjects.get(0);
               PSAttributeList attributeList = subject.getAttributes();
               Iterator attrs = attributeList.iterator();
               while (attrs.hasNext())
               {

                  PSAttribute attr = (PSAttribute) attrs.next();
                  Element attrib = PSXmlDocumentBuilder.addEmptyElement(doc,
                     user, "Attribute");
                  attrib.setAttribute("name", attr.getName());

                  List values = attr.getValues();
                  Iterator valueIter = values.iterator();
                  while ( valueIter.hasNext())
                  {
                     String value = valueIter.next().toString();
                     PSXmlDocumentBuilder.addElement(doc, user, "Value", value );
                  }
               }
            }
         }
         catch ( PSSecurityException se )
         {
            PSXmlDocumentBuilder.addElement(doc, user, "Error",
                  "Unable to obtain global attribs for the following reason: "
                  + se.getLocalizedMessage());
         }
      }

      // add Roles node,
      Element roles = PSXmlDocumentBuilder.addEmptyElement(doc, root, "Roles");
      try
      {
         if (users.length > 0)
         {
            List roleList = sess.getUserRoles();
            Iterator roleIter = roleList.iterator();
            while ( roleIter.hasNext())
            {
               PSXmlDocumentBuilder.addElement(doc, roles, "Role",
                  roleIter.next().toString());
            }
         }
      }
      catch ( PSSecurityException e )
      {
         roles.appendChild( doc.createTextNode(
            "Unable to obtain roles for the following reason: "
            + e.getLocalizedMessage() ) );
      }

      Element rights = null;
      try
      {
         // add DataAccessRights node
         rights = PSXmlDocumentBuilder.addEmptyElement(doc, root, "DataAccessRights");
         rights.setAttribute("query",
            (String)getUserContextInfo(TOK_DATA_ACC_QUERY, null, request, null));
         rights.setAttribute("insert",
            (String)getUserContextInfo(TOK_DATA_ACC_INSERT, null, request, null));
         rights.setAttribute("update",
            (String)getUserContextInfo(TOK_DATA_ACC_UPDATE, null, request, null));
         rights.setAttribute("delete",
            (String)getUserContextInfo(TOK_DATA_ACC_DELETE, null, request, null));


         // add DesignAccessRights node
         rights = PSXmlDocumentBuilder.addEmptyElement(doc, root, "DesignAccessRights");
         rights.setAttribute("modifyAcl",
            (String)getUserContextInfo(TOK_DESIGN_ACC_MOD_ACL, null, request, null));
         rights.setAttribute("readDesign",
            (String)getUserContextInfo(TOK_DESIGN_ACC_READ, null, request, null));
         rights.setAttribute("updateDesign",
            (String)getUserContextInfo(TOK_DESIGN_ACC_UPDATE, null, request, null));
         rights.setAttribute("deleteDesign",
            (String)getUserContextInfo(TOK_DESIGN_ACC_DELETE, null, request, null));

         // add ServerAccessRights node
         rights = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
            "ServerAccessRights");
         rights.setAttribute("data", (String)getUserContextInfo(
            TOK_SERVER_DATA_ACCESS, null, request, null));
         rights.setAttribute("design", (String)getUserContextInfo(
            TOK_SERVER_DESIGN_ACCESS, null, request, null));
         rights.setAttribute("createApplications", (String)getUserContextInfo(
            TOK_SERVER_CREATEAPPLICATIONS_ACCESS, null, request, null));
         rights.setAttribute("deleteApplications", (String)getUserContextInfo(
            TOK_SERVER_DELETEAPPLICATIONS_ACCESS, null, request, null));
         rights.setAttribute("admin", (String)getUserContextInfo(
            TOK_SERVER_ADMIN_ACCESS, null, request, null));
      }
      catch (PSDataExtractionException e)
      {
         if (rights != null)
         {
            rights.appendChild( doc.createTextNode(
               "Unable to obtain all attributes of this element for the " +
               "following reason: " + e.getLocalizedMessage() ) );
         }
      }

      return root;
   }

   /**
    * Gets the role specific subject attribute values that meet the supplied
    * criteria.
    *
    * @param entry An authenticated  Assumed not <code>null</code>.
    * @param roleName The role which refers to the specific attributes.
    *    Assumed not <code>null</code>.
    * @param attribName The particular attribute. Assumed not <code>null
    *    </code>.
    * @return A valid list containing 0 or more values assigned to the
    *    specified attribute.
    * @throws PSSecurityException If any problems occur trying to get the
    *    meta data.
    */
   private static List getRoleSubjectAttributeValues(PSUserEntry entry, 
      String roleName, String attribName) throws PSSecurityException
   {
      List returnList = new ArrayList();
      List singleSubjectList = 
         PSRoleManager.getInstance().getSubjectRoleAttributes(entry.getName(), 
            PSSubject.SUBJECT_TYPE_USER, roleName, attribName);

      // PSRoleManager.getSubjectRoleAttributes() returns a List of
      // PSSubjects.  At most one PSSubject with one PSAttribute will be in
      // this List.
      if ( singleSubjectList.size() > 0 )
      {
         PSSubject subject = (PSSubject) singleSubjectList.get(0);
         returnList.addAll (
               subject.getAttributes().getAttribute(attribName).getValues() );
      }
      return returnList;
   }

   /**
    * Gets the global subject attribute values that meet the supplied
    * criteria.
    *
    * @param entry An authenticated  Assumed not <code>null</code>.
    * @param attribName The particular attribute. Assumed not <code>null
    *    </code>.
    * @return A valid list containing 0 or more values (Strings) assigned to
    *    the specified attribute.
    * @throws PSSecurityException If any problems occur trying to get the
    *    meta data.
    */
   public static List<String> getSubjectAttributeValues(PSUserEntry entry,
                                                String attribName) throws PSSecurityException
   {
      List returnList = new ArrayList();
      List singleSubjectList = 
         PSRoleManager.getInstance().getSubjectGlobalAttributes(entry.getName(),
            PSSubject.SUBJECT_TYPE_USER, null, attribName);

      // PSRoleManager.getSubjectGlobalAttributes() returns a List of
      // PSSubjects.  At most one PSSubject with one PSAttribute will be in
      // this List.
      if ( singleSubjectList.size() > 0 )
      {
         PSSubject subject = (PSSubject) singleSubjectList.get(0);
         returnList.addAll (
               subject.getAttributes().getAttribute(attribName).getValues() );
      }
      return returnList;
   }


   /**
    * Convert an object array into a PSLiteralSet object. This result will be returned
    * by method extract() and makes data comparison relatively easier.
    */
   private static PSLiteralSet convertToLiteralSet(Object[] objArray)
   {
      if (objArray == null)
         return null;

      int arraySize = objArray.length;
      PSTextLiteral textLiteral = null;
      PSLiteralSet literalSet = new PSLiteralSet(com.percussion.design.objectstore.PSTextLiteral.class);

      for (int i = 0; i < arraySize; i++){
         textLiteral = new PSTextLiteral((String)(objArray[i]));
         literalSet.add(textLiteral);
      }

      return literalSet;
   }

   /**
    * Static method to directly return user context information
    *    to an outside calling class.
    *
    * @param  userContextField the name of the user context/attribute desired
    *
    * @param   request    the execution data associated with this request.
    *                      This includes all context data, result sets, etc.
    *
    * @param   defValue    the default value to use if a value is not found
    *
    * @return              the associated value; <code>defValue</code> if a
    *                      value is not found
    *
    */
   public static Object getUserContextInformation( String userContextField,
                                                   PSRequest request,
                                                   Object defValue)
      throws PSDataExtractionException
   {
      int type = getType(userContextField.toLowerCase());
      String field = userContextField;

      if (type == TOK_USER_ATTRIBUTE)
         field = userContextField.substring(PREFIX_USER_ATTRIBUTES.length());
      else if (type == TOK_USER_SESSIONOBJECT)
         field = userContextField.substring(
            PREFIX_USER_SESSION_OBJECT.length());

      return getUserContextInfo(type, field, request, defValue);
   }

   /**
    * session create time date format
    */
    public static final String SESSION_CREATE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

   /**
    * root node name for toXml
    */
   public static final String NODE_NAME = "PSXUserContextExtractor";

   /**
    * Prefix of context token used to specify a particular user attribute.
    */
   public static final String PREFIX_USER_ATTRIBUTES = "user/attributes/";

   /**
    * Prefix of context token used to specify a session object.
    */
   public static final String PREFIX_USER_SESSION_OBJECT =
      "user/sessionobject/";

   /**
    * Unkown - represents an undefined or unrecognized token.
    */
   private static final int   TOK_UNKNOWN          = 0;

   /**
    * SessionId - The session identifier associated with this request.
    */
   private static final int   TOK_SESSION_ID          = 1;

   /**
    * User/Name - The name of this user.
    */
   private static final int   TOK_USER_NAME           = 2;

   /**
    * User/SecurityProvider - The security provider this user has
    * authenticated through.
    */
   private static final int   TOK_SEC_PROVIDER        = 3;

   /**
    * User/SecurityProviderTypeId - The security provider type id this user has
    * authenticated through.
    */
   private static final int   TOK_SEC_PROVIDER_TYPE_ID  = 4;

   /**
    * User/SecurityProviderInstance - The security provider instance name
    * this user has authenticated through.
    */
   private static final int   TOK_SEC_PROVIDER_INST   = 5;

   /**
    * User/Attributes - The attributes associated with this user
    * (security provider specific.
    * <P>
    * This type requires we also store the attribute name as attributes
    * are security provider specific so we haven't defined them all here.
    */
   private static final int   TOK_USER_ATTRIBUTE      = 6;

   /**
    * Roles/RoleName - The name of the role the requestor is a member of.
    */
   private static final int   TOK_ROLE_NAME           = 7;

   /**
    * DataAccessRights/query - 1 if the requestor can query; 0 otherwise.
    */
   private static final int   TOK_DATA_ACC_QUERY      = 8;

   /**
    * DataAccessRights/insert - 1 if the requestor can insert; 0 otherwise.
    */
   private static final int   TOK_DATA_ACC_INSERT     = 9;

   /**
    * DataAccessRights/update - 1 if the requestor can update; 0 otherwise.
    */
   private static final int   TOK_DATA_ACC_UPDATE     = 10;

   /**
    * DataAccessRights/delete - 1 if the requestor can delete; 0 otherwise.
    */
   private static final int   TOK_DATA_ACC_DELETE     = 11;

   /**
    * DesignAccessRights/modifyAcl - 1 if the requestor can modify the
    * applciation's ACL; 0 otherwise.
    */
   private static final int   TOK_DESIGN_ACC_MOD_ACL  = 12;

   /**
    * DesignAccessRights/readDesign - 1 if the requestor can read the
    * application design; 0 otherwise.
    */
   private static final int   TOK_DESIGN_ACC_READ     = 13;

   /**
    * DesignAccessRights/updateDesign - 1 if the requestor can update the
    * application design; 0 otherwise.
    */
   private static final int   TOK_DESIGN_ACC_UPDATE   = 14;

   /**
    * DesignAccessRights/deleteDesign - 1 if the requestor can delete the
    * application design; 0 otherwise.
    */
   private static final int   TOK_DESIGN_ACC_DELETE   = 15;

   /**
    * User/SessionObject - The session private objects associated with this user
    * (security provider specific).
    */
   private static final int   TOK_USER_SESSIONOBJECT    = 16;

   /**
    * SessionCreateTime - The create time stamp for the session associated
    * with this request
    */
   private static final int   TOK_SESSION_CREATE_TIME   = 17;

  /**
   * Roles/RoleNameinclause - The list of roles the requestor is a member of,
   * formatted into an SQL "IN" clause string.
   */
   private static final int   TOK_ROLE_NAME_INCLAUSE    = 18;

   private int m_sourceType;
   private String m_sourceName;
   
   /**
    * ServerAccessRights/noaccess is 1 if the requestor has no access to 
    * server; 0 otherwise. This flag will not be used as it is not required
    * since user would not have come this far without server access.  
    */
   private static final int TOK_SERVER_NO_ACCESS = 20;

   /**
    * ServerAccessRights/dataAccess is 1 if the requestor has data access on the 
    * server; 0 otherwise.
    */
   private static final int TOK_SERVER_DATA_ACCESS = 21;

   /**
    * ServerAccessRights/designAccess is 1 if the requestor has design access on 
    * the server; 0 otherwise.
    */
   private static final int TOK_SERVER_DESIGN_ACCESS = 22;

   /**
    * ServerAccessRights/createApplications is 1 if the requestor can create new 
    * applications on the server; 0 otherwise.
    */
   private static final int TOK_SERVER_CREATEAPPLICATIONS_ACCESS = 23;

   /**
    * ServerAccessRights/deleteApplications is 1 if the requestor can delete 
    * applications on the server; 0 otherwise.
    */
   private static final int TOK_SERVER_DELETEAPPLICATIONS_ACCESS = 24;

   /**
    * ServerAccessRights/administerServer is 1 if the requestor has 
    * administrator access to the server; 0 otherwise.
    */
   private static final int TOK_SERVER_ADMIN_ACCESS = 25;

}

