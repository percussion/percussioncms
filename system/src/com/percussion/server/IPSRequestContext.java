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
package com.percussion.server;

import com.percussion.data.PSDataExtractionException;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.security.PSSecurityToken;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The IPSRequestContext interface contains all the context of a given request.
 *
 * @author     DVG and Jian Huang
 * @version    1.1
 * @since      1.1
 */
public interface IPSRequestContext
{
   /**
    * Get the host address for this rhythmyx server
    *
    * @return  The host address.
    */
   public String getServerHostAddress();

   /**
    * Get the port that the Rhythmyx server is listening on.
    *
    * @return  The port that the listener has initialized with.
    */
   public int getServerListenerPort();

   /**
    * Get the session ID associated with this request's user session.
    *
    * @return the user's session ID, or empty if session management is disabled
    */
   public String getUserSessionId();

   /**
    * Get the host that was used by the request that originated
    * this session.
    *
    * @return the original host, never <code>null</code>, might be empty.
    */
   public String getOriginalHost();

   /**
    * Get the port that was used by the request that originated
    * this session.
    *
    * @return the original port.
    */
   public int getOriginalPort();

   /**
    * Get the protocol that was used by the request that originated
    * this session.
    *
    * @return the original protocol as a string; either "http" or "https"
    */
   public String getOriginalProtocol();

   /**
    * Get the file portion of the request URL which was specified
    * when making this request. The protocol and host are excluded.
    * For example, the URL <code>http://localhost/Rhythmyx/app1/req.xml</code>
    * will return <code>/Rhythmyx/app1/req.xml</code>
    *
    * @return      the file portion of the request URL
    */
   public String getRequestFileURL();

   /**
    * Get the request root, which consists of the Rhythmyx server's
    * request root and the application's request root, such as
    * <code>/Rhythmyx/app1</code>.
    *
    * @return      the Rhythmyx request root portion of the request URL
    */
   public String getRequestRoot();

   /**
    * Get the page name of the request URL, including the file extension.
    * The protocol, host and Rhythmyx root are excluded. For example, the URL
    * <code>http://localhost/Rhythmyx/app1/request.xml</code> will return
    * <code>request.xml</code>
    *
    * @return      the request page portion of the request URL
    */
   public String getRequestPage();

   /**
    * Get the page name of the request URL and whether the extension is included
    * depending on the input flag.  The protocol, host and Rhythmyx root
    * are excluded. For example, the URL
    * <code>http://localhost/Rhythmyx/app1/request.xml</code> will return
    * <code>request.xml</code> if includeExtension is <code>true</code>;
    * otherwise, <code>request</code> is returned.
    *
    * @param includeExtension  <code>true</code> to include file extension;
    *                          <code>false</code> otherwise
    *
    * @return the request page portion of the request URL
    */
   public String getRequestPage(boolean includeExtension);

   /**
    * Get the type of the request page.
    *
    * @return the <code>IPSRequest.PAGE_TYPE_xxx</code> request page type
    */
   public int getRequestPageType();

   /**
    * Get the file extension associated with the request page. The
    * returned extension includes the "." extension separator.
    *
    * @return      the file extension, may be empty
    */
   public String getRequestPageExtension();

   /**
    * Get the URL submitted through the hook of the request page.
    *
    * @return      the URL submitted through the hook, or <code>null</code>
    *              if a hook was not used
    */
   public String getHookURL();
   

   /**
    * Get the headers from the request
    * @return an enumeration, never <code>null</code>
    */
   public Enumeration getHeaders();   

   /**
    * Get value of a parameter that was passed in with the request.
    * <p>
    * Parameters are usually sent as part of the URL when issuing an HTTP
    * GET request.  They are also possibly able to be sent as the body
    * of an HTTP POST request.
    * <p>
    * When parsing the parameters specified in the URL (or in the POST data),
    * parameter names may be repeated. Rhythmyx stores the lists of data
    * as a <code>List</code> object. This method will only return the first list
    * entry for the matched item. If access to all the data in the list is
    * required, then use the {@link #getParameterList(String) getParameterList}
    * method.
    * <p>
    * For any non-string data, <code>toString()</code> will be called on the
    * object.
    * For objects such as file attachments, this will return the file name, not
    * the contents, which may not be desirable.
    *
    * @param name the name of the parameter to retrieve, may not be
    * <code>null</code>
    *
    * @param      defValue   the value to return if a parameter of the
    *  specified name does not exist, may be <code>null</code>
    *
    * @return the parameter's value, or <code>defValue.toString()</code> if it
    * is not found. May be <code>null</code> if the supplied default value is
    * <code>null</code>
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public String getParameter(String name, Object defValue);

   /**
    * Convenience method, calls {@link #getParameter(String, Object)
    * getParameter(name, null)}.
    */
   public String getParameter(String name);

   /**
    * Get a parameter that was passed in with the request, as an object.
    * <p>
    * Parameters are usually sent as part of the URL when issuing an HTTP
    * GET request.  They are also possibly able to be sent as the body
    * of an HTTP POST request.
    * <p>
    * When parsing the parameters specified in the URL (or in the POST data),
    * parameter names may be repeated. Rhythmyx stores the lists of data
    * as a <code>List</code> object. This method will only return the first list
    * entry for the matched item. If access to all the data in the list is
    * required, then use the {@link #getParameterList(String) getParameterList}
    * method.
    *
    * @param name the name of the parameter to retrieve, may not be
    * <code>null</code>
    * @param      defValue   the value to return if a parameter of the
    *  specified name does not exist, may be <code>null</code>
    *
    * @return the parameter's value, or <code>defValue</code>
    * if it is not found. May be <code>null</code> if <code>defValue</code> is
    * <code>null</code> and value is not found for the specified parameter.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public Object getParameterObject(String name, Object defValue);

   /**
    * Convenience method, calls {@link #getParameterObject(String, Object)
    * getParameterObject(name, null)}.
    */
   public Object getParameterObject(String name);

   /**
    * Get the entire set of parameters which were passed in with the request.<p>
    *
    * Modifying the returned hash map will directly modify the parameters
    * stored with the request.  Any subsequent use of the <code>getParameter
    * </code> methods will reflect the changes.
    *
    * @return the request parameters, never <code>null</code> may be empty.
    *
    * @deprecated Use {@link #getParametersIterator() } for walking through the
    * parameters.
    */
   public HashMap getParameters();

   /**
    * Get values of a parameter that was passed in with the request.
    * <br>
    * Note: {@link #getParameterObject(String)} gets only the first object
    * in the list if the parameter has multiple values.
    *
    * @param name the name of the parameter to retrieve, may not be
    * <code>null</code> or empty.
    *
    * @return the array of values for the specified parameter, or
    * <code>null</code> if it is not found. If it is not <code>null</code> it
    * will contain one or more elements.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
    public Object[] getParameterList(String name);

   /**
    * Removes a parameter with specified name from the request parameters if it
    * is found.
    *
    * @param name the name of the parameter, may be <code>null</code> or empty.
    *
    * @return the value of the parameter. If name is <code>null</code> or empty
    * or parameter is not found for the specified name, <code>null</code> will
    * be returned.
    */
   public Object removeParameter(String name);

   /**
    * Appends the supplied value to the list of values of the parameter with
    * supplied name. If the parameter does not exist it creates an entry for it
    * and sets the value as parameter value.
    *
    * @param name the name of the parameter, may not be <code>null</code> or
    * empty.
    * @param value object to be appended to the list of values, may be
    * <code>null</code>
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    * @throws IllegalStateException if the type of objects in the parameter
    * values does not match the type of object being appended.
    */
   public void appendParameter(String name, Object value);

   /**
    * Checks whether any of the parameters have multiple values.
    *
    * @return <code>true</code> if any of the parameters have multiple values,
    * otherwise <code>false</code>
    */
   public boolean hasMultiValuesForAnyParameter();

   /**
    * Gets the list of parameters with each element as an entry set with
    * parameter name as key and parameter value as value. Key is a
    * <code>String</code> object, whereas the value can be any object. This
    * iterator cannot be modified.
    *
    * @return the request parameters, never <code>null</code>, may be empty.
    */
   public Iterator getParametersIterator();

   /**
    * Get the character set associated with the request.  The value
    * returned will be:
    * <OL>
    *   <LI>the request-specific file upload character encoding for this
    *       request or</LI>
    *   <LI>the Rhythmyx server's default character encoding if one has not
    *       been set or</LI>
    *   <LI><code>null</code> which indicates the server will use the Java
    *       default encoding</LI>
    * </OL>
    *
    * @return The request's character encoding,
    *         never empty, <code>null</code> indicates the default
    *         java encoding.
    */
   public String getFileCharacterSet();

   /**
    * Set the "file-upload" character set associated with the request.  This
    * should be either the IANA MIME-preferred registry name of the character
    * set or Java's canonical name for the character set (preferred).
    *
    * @param encoding The request's character encoding, use <code>null</code>
    *          or empty to indicate to use the Rhythmyx server's default
    *          encoding.  This method will only affect character-based file
    *          uploads. ResultDoc extensions attempting
    *          to modify the character set for this request will have no
    *          effect since all file-related processing will be completed
    *          prior to the resultDoc call.
    */
   public void setFileCharacterSet(String encoding);

   /**
    * Set parameters associated with this request to the specified HashMap.
    * <br>
    * Note:If this method is used for setting the parameters after balancing by
    * the user directly in the existing code(in the exits before v4.1), server
    * treats the copies in the parameter values as new values rather than the
    * repeated values.
    *
    * @param params the parameters associated with this request, may not be
    * <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if <code>params</code> is <code>null
    * </code>
    *
    * @deprecated Use {@link #setParameter } and {@link #removeParameter} for
    * modifying the parameters.
    */
   public void setParameters(HashMap params);

   /**
    * Replaces the value of an existing entry or creates a new entry in the
    * parameter map.
    * <p>
    * Parameters are usually sent as part of the URL when issuing a
    * HTTP GET request. It is also possible to send parameters as
    * the body of a HTTP POST request. When parsing the parameters specified in
    * the URL (or in the POST data), parameter names may be repeated. Rhythmyx
    * stores the lists of data as a <code>List</code> object. So to set multiple
    * values for a parameter, pass a <code>List</code> object for the value.
    * <p>
    * Note: If this method is used in the existing code(in the exits before
    * v4.1) to set a parameter value which is already balanced, the server
    * treats the copies in the balanced list as new values rather than repeated
    * values.
    *
    * @param name The name of the parameter to add/modify.  May not be
    * <code>null</code> or empty.
    *
    * @param value An Object, the value to set it to.  May be <code>null</code>.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty.
    */
   public void setParameter(String name, Object value);

   /**
    * Assigns the specified values to the named parameter, replacing any
    * existing value.
    *
    * @param name The name of the parameter to add/modify.  May not be
    * <code>null</code> or empty.
    * @param values An array of values to assign as the value for the named
    * parameter.  May be <code>null</code> or empty, all objects in this
    * array must be of the same type.
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty or
    *    the supplied value array contains objects of more than one type.
    */
   public void setParameterList(String name, Object[] values);

   /**
    * Gets a copy of the parameters which were passed in with the request
    * ensuring that they have single values.  If the parameter values are
    * Lists, they will be truncated to the first Object in the list.
    *
    * Modifying the returned Map will not directly modify this object.
    *
    * @return The request parameters map containing single values.  The key is
    * the String representation of the parameter name, and the value is an
    * Object, usually a String, but possibly something else (e.g. a File
    * object), but no entries should have List objects for their values.
    */
   public Map getTruncatedParameters();

   /**
    * Get a CGI variable associated with the request.
    * <p>
    * CGI variables are created by Rhythmyx (or the web server when a hook
    * such as an ISAPI extension is used).  These variables contain various
    * pieces of information associated with the request, such as the
    * request headers.
    *
    * @param      name      the name of the CGI variable to retrieve
    *
    * @return      the requested CGI variable's value, or <code>null</code>
    *              if the requested entry is not found
    */
   public String getCgiVariable(String name);

   /**
    * Set a CGI variable associated with the request.
    * 
    * @param cgiVarName the name of the variable, never <code>null</code> or empty
    * @param cgiVarValue the value, never <code>null</code> or empty
    */
   public void setCgiVariable(String cgiVarName, String cgiVarValue);

   /**
    * Get a cookie which was passed in the headers associated with the request.
    *
    * @param      name   the name of the cookie to retrieve
    *
    * @param      defValue   the default value to return if the requested
    *                        entry is not found
    *
    * @return      the requested cookie's value, or <code>defValue</code>
    *              if the requested entry is not found
    */
   public String getRequestCookie(String name, String defValue);

   /**
    * Get a cookie which was passed in the headers associated with the request.
    *
    * @param      name   the name of the cookie to retrieve
    *
    * @return      the requested cookie's value, or <code>null</code>
    *              if the requested entry is not found
    */
   public String getRequestCookie(String name);

   /**
    * Set a cookie which will be sent along with the response.
    *
    * @param      name     the name of the cookie
    *
    * @param      value    the value of the cookies
    *
    * @param      path     the URL path that the cookie is valid for
    *                      (for example, / for the entire site, /xyz for
    *                      requests in the /xyz path, etc.)
    *
    * @param      domain   the host that the cookie is valid for
    *                      (e.g., www.percussion.com)
    *
    * @param      expires  the date that the cookie expires
    *
    * @param      secure   <code>true</code> if the cookie should only be
    *                      sent over HTTPS connections
    */
   public void setResponseCookie(String name, String value, String path,
                                 String domain, java.util.Date expires,
                                 boolean secure);

   /**
    * Get the entire set of cookies which will be sent along with the response.
    * <p>
    * The name of a cookie is stored as a hash key.
    * Other components of a cookie are stored as the corresponding hash value.
    * When setting cookies, the permitted syntax for the value is:<p>
    *    <code>
    *    <value>[;path=<path>][;domain=<domain>][;expires=<expires>][;secure]
    *    </code><p>
    *
    *    where:<p>
    *
    *    value is the value of the cookie<p>
    *
    *    path is the URL path that the cookie is valid for (for example, / for
    *       the entire site, /xyz for requests in the /xyz path, etc.)<p>
    *
    *    domain is the host that the URL is valid for
    *       (e.g., www.percussion.com)<p>
    *
    *    expires is the date the cookie expires in
    *       "Day, DD-Mon-YYYY HH:MM:SS TZ" format, such as:
    *       "Fri, 01-Jan-1999 00:00:00 GMT"<p>
    *
    *    secure is used to signify the cookie should only be sent over HTTPS
    *       connections<p>
    *
    *    Modifying the returned HashMap will directly modify the cookies.  Any
    *       subsequent use of the <code>getResponseCookie</code> methods will
    *       reflect the changes.
    *
    * @return      the cookies;
    */
   public HashMap getResponseCookies();

   /**
    * Sets the cookies which will be sent along with the response to the
    * specified hash map.
    *
    * @param      cookies   the cookies to be send with the response (may be
    *                       <code>null</code>)
    */
   public void setResponseCookies(HashMap cookies);

   /**
    * Gets the XML document sent as input for the request.  If an XML
    * document is not sent with the request, <code>null</code> will be
    * returned.<p>
    *
    * Modifying the returned XML document will directly modify this object.
    * Any subsequent use of the XML document or the
    * <code>getInputDocument</code> method will see the changed data.
    *
    * @return the input document; or <code>null</code> if an XML document
    *         was not sent with the request
    */
   public org.w3c.dom.Document getInputDocument();

   /**
    * Sets the XML document to be used as input for the request.  If a call
    * to <code>getInputDocument</code> was made, any modifications of
    * that document will be reflected in the request. Therefore, a call
    * to this method would not be required in that case.
    *
    * @param      inDoc   the input document associated with this request
    *                     (may be <code>null</code>)
    */
   public void setInputDocument(org.w3c.dom.Document inDoc);

   /**
    * Gets the requestor's preferred locale. This is done by checking the
    * language setting provided by the user agent (e.g., browser). The
    * best fit language will be returned (if available).
    *
    * @return      the requestor's preferred locale
    */
   public java.util.Locale getPreferredLocale();

   /**
    * Gets the access level assigned to this user for the application
    * processing this request. Access levels are defined by the
    * <code>com.percussion.design.objectstore.PSAclEntry.AACE_XXX</code> flags.
    * These <code>XXX</code> are:<p>
    *
    * <code>AACE_DATA_CREATE</code> which allows creating of back end rows
    *
    * <code>AACE_DATA_DELETE</code>   which allows deleting of existing back
    *    end rows
    *
    * <code>AACE_DATA_QUERY</code>   which allows querying of back end rows
    *    and reading of application files (such as style sheets, DTDs, etc.)
    *
    * <code>AACE_DATA_UPDATE</code>   which allows updating of existing back
    *    end rows
    *
    * @return      the access level in the current application
    */
   public int getCurrentApplicationAccessLevel();

   /**
    * Gets the current application name, or the application's request root,
    * such as <code>/app1</code>.
    *
    * @return the Rhythmyx application root portion of the request URL
    */
   public String getCurrentApplicationName();

   /**
    * Gets a private object associated with this execution context. It
    * is provided as a storage area for extension or exit handlers, etc., to
    * create context information once.
    *
    * @param   key      the key under which the object is stored
    *
    * @return            the private object associated with the key
    *
    * @exception   com.percussion.error.PSRuntimeException   if key is null
    */
   public Object getPrivateObject(Object key)
      throws com.percussion.error.PSRuntimeException;

   /**
    * Sets a private object associated with this execution context. This
    * is provided as a storage area for exits to create
    * context information once.
    *
    * @param   key      the unique key under which the object is stored
    *
    * @param   o         the private object associated with the key
    *
    * @throws  IllegalArgumentException if key is <code>null</code>
    */
   public void setPrivateObject(Object key, Object o)
      throws com.percussion.error.PSRuntimeException;


   /**
    * Gets a private object associated with this user session. This
    * is provided as a storage area for exits to create
    * context information once. This can then be retrieved across
    * requests throught the user's session.
    *
    * @param   key      The key under which the object is stored, must not
    *                   be <code>null</code>.  This name will allow the
    *                   object to be retrieved during the life of the
    *                   user session.
    *
    * @return            the private object associated with the key
    *
    * @throws           IllegalArgumentException if key is <code>null</code>
    */
   public Object getSessionPrivateObject(Object key);


   /**
    * Sets a private object associated with this user session. This
    * is provided as a storage area for exit handlers, etc. to create
    * context information once. This can then be retrieved across
    * requests throught the user's session.
    *
    * @param   key      the key under which the object is stored. Be sure to
    *                   to specify a unique name -- that is, something other
    *                   exits using this mechanism will not
    *                   likely use as a name
    *
    * @param   o         the private object associated with the key
    */
   public void setSessionPrivateObject(Object key, Object o);



   /**
    * Gets a system object associated with this user session. This
    * is provided as a storage area for exits to create
    * context information once. This can then be retrieved across
    * requests throught the user's session.
    *
    * @param   key      The key under which the object is stored, must not
    *                   be <code>null</code>.  This name will allow the
    *                   object to be retrieved during the life of the
    *                   user session.
    *
    * @return            the system object associated with the key
    *
    * @throws           IllegalArgumentException if key is <code>null</code>
    *                   or empty
    */
   public Object getSessionObject(String key);

   /**
    * Re-added for custom exit backwards compatiblity. Calls 
    * {@link #getSubjectRoles(PSSubject) getSubjectRoles(
    * new PSGlobalSubject(name, PSSubject.SUBJECT_TYPE_USER, null))}.
    *
    * @deprecated use {@link #getSubjectRoles(PSSubject)} instead.
    */
   public List getUserRoles(String provider, String providerInstance, 
      String name);

   /**
    * Re-added for custom exit backwards compatiblity. Calls 
    * {@link #getSubjectGlobalAttributes(String, int, String, String, boolean, 
    * String) getSubjectGlobalAttributes(subjectNameFilter, subjectType, 
    * roleName, attributeNameFilter, includeEmptySubjects, 
    * getPrivateObject(IPSHtmlParameters.SYS_ITEM_COMMUNITYID))}.
    *
    * @deprecated use {@link #getSubjectGlobalAttributes(String, int, String, 
    *    String, boolean, String)} instead.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter,
      int subjectType, int providerType, String providerInstance,
      String roleName, String attributeNameFilter, 
      boolean includeEmptySubjects);
   
   /**
    * Re-added for custom exit backwards compatiblity. Calls 
    * {@link #getSubjectRoleAttributes(String, int, String, String) 
    * getSubjectRoleAttributes(subjectNameFilter, subjectType, roleName, 
    * attributeNameFilter)}.
    *
    * @deprecated use {@link #getSubjectRoleAttributes(String, int, String, 
    *    String)} instead.
    */
   public List getSubjectRoleAttributes(String subjectNameFilter,
      int subjectType, int providerType, String providerInstance,
      String roleName, String attributeNameFilter);

   /**
    * Gets the role names to which the specified subject belongs, as a List of
    * Strings. Since application-specific roles have been deprecated, this
    * method will only return server-based role information.
    * 
    * @param subject The properties that uniquely identify someone within the Rx
    *           security system. If <code>null</code>, the subject that made
    *           the request is used. If anonymous, an empty list is returned.
    * 
    * @return A valid list of 0 or more role names, 1 for each role containing
    *         the specified user or group. List elements will be of type
    *         <code>String</code>. The list is sorted in ascending alpha
    *         order.
    * 
    * @throws IllegalArgumentException If any argument is invalid.
    * @since 4.0
    */
   public List getSubjectRoles( PSSubject subject );


   /**
    * Convenience method that gets the role names to which the subject making
    * the request belongs, as a List of Strings.
    * Convenience method. Calls {@link #getSubjectRoles(PSSubject)
    * getSubjectRoles(null)}.
    *
    * @since 4.0
    */
   public List getSubjectRoles();


   /**
    * Similar to {@link #getSubjectRoles(PSSubject)}, but ignores the
    * security provider information. If there is only a single subject that
    * has the supplied name, then it will return the same list as the
    * aforementioned method. If more than 1 name match, it will return all
    * roles from all matching subjects.
    *
    * @param subjectName The full name of the desired person. Never <code>null
    *    </code> or empty.
    *
    * @return A valid list of 0 or more Strings, each of which is the name
    *    of a role that contains a subject by the supplied name and has a
    *    type of PSSubject.SUBJECT_TYPE_USER. Each role name is unique within
    *    the list.
    *
    * @throws IllegalArgumentException if subjectName is <code>null</code>
    *    or empty.
    * @since 4.0
    */
   public List getSubjectRoles( String subjectName );


   /**
    * Gets all role names on the system, as a List of Strings.
    *
    * @return  A list of 0 or more role names, 1 for each role on the system.
    *    List elements will be of type <code>String</code>. The list is sorted
    *    in ascending alpha order.
    * @since 4.0
    */
   public List getRoles();


   /**
    * Convenience method that gets all the subjects for the specified role,
    * as a List of PSSubjects.
    * Calls {@link #getRoleSubjects(String,int,String)
    * getRoleSubjects( roleName, 0, null )}.
    *
    * @since 4.0
    */
   public List getRoleSubjects(String roleName);


   /**
    * Gets the subjects that match the supplied criteria (for the
    * specified role), as a List of PSSubjects.
    *
    * @param roleName  The name of the role. If <code>null</code> or empty,
    *    all subjects matching the criteria are returned, regardless of what
    *    roles they belong too.
    *
    * @param memberFlags One or more of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    These filter types may be or'ed together to indicate multiple types of
    *    subjects.  If 0 is specified, all types will be returned.
    *
    * @param subjectNameFilter  A single pattern for subject names. Filters
    *    use SQL LIKE syntax. If <code>null</code> or empty, no filtering
    *    will be done, all names will be considered a match.
    *
    * @return A list of 0 or more subjects in the role as PSSubjects sorted
    *    in ascending alpha order by subject name.
    *
    * @since 4.0
    */
   public List getRoleSubjects(String roleName, int memberFlags,
         String subjectNameFilter);


   /**
    * Convenience method that gets the subjects that match the supplied
    * criteria (from any role), as a List of PSSubjects.
    * Calls {@link #getRoleSubjects(String,int,String)
    * getRoleSubjects(null, PSSubject.SUBJECT_TYPE_USER, subjectNameFilter)}.
    *
    * @since 4.0
    */
   public List getSubjects( String subjectNameFilter );


   /**
    * Gets the attributes for the specified role, as a List of PSAttributes.
    *
    * @param roleName The name of the role to retrieve attributes for.
    * Can not be <code>null</code> or empty.
    *
    * @return A valid set of 0 or more PSAttribute objects. May be empty.
    *
    * @throws IllegalArgumentException If <code>roleName</code> is
    * <code>null</code> or empty.
    * @since 4.0
    */
   public List getRoleAttributes(String roleName );

   /**
    * Gets only the global attributes for a set of subjects.
    *
    * @param subjectNameFilter A single pattern used to find the individual(s)
    *    whose attributes you wish. Wildcards allowed following SQL LIKE
    *    syntax. If <code>null</code> or empty, all subjects are included.
    * @param subjectType One of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    Provide 0 to ignore this property.
    * @param roleName This is only useful if wildcards are used for the subject
    *    name. If provided, limits the subjects to those that appear in this
    *    role.
    * @param attributeNameFilter  A single pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    * @param includeEmptySubjects A flag to indicate whether subjects with
    *    no attributes should be included in the returned list. If <code>
    *    true</code>, they are included, otherwise, only subjects that
    *    have 1 or more attributes are included.
    * @param communityId the community to which the subjects must be a member
    *    of, may be <code>null</code> or empty in which case no filtering is
    *    done based on community
    * @return A valid list of 0 or more PSSubjects containing either 1 or more
    *    attributes (if includeEmptySubjects is <code>false</code>) or 0 or
    *    more attributes (if includeEmptySubjects is <code>true</code>),
    *    ordered in ascending alpha order by subject name. The caller
    *    takes ownership of the list.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter, 
      int subjectType, String roleName, String attributeNameFilter, 
      boolean includeEmptySubjects, String communityId);

   /**
    * Gets the global attributes for the specified subjects.
    *
    * Convenience method that gets the attributes of the subject that made the
    * current request. See {@link
    * #getSubjectGlobalAttributes(String, int, String, String, boolean, String)} 
    * for a full description.
    */
   public List getSubjectGlobalAttributes(String subjectNameFilter,
         int subjectType, String roleName, String attributeNameFilter, 
         boolean includeEmptySubjects);

   /**
    * Convenience method that gets the attributes of the subject that made the
    * current request. Calls {@link
    * #getSubjectGlobalAttributes(PSSubject) getSubjectGlobalAttributes(null)}.
    *
    * @since 4.0
    */
   public List getSubjectGlobalAttributes();


   /**
    * Convenience method that gets all of the global attributes for the
    * supplied subject. See {@link
    * #getSubjectGlobalAttributes(String, int, String, String, boolean)
    * here} for a full description.
    *
    * @param subject The definition of a subject in the Rx security system. If
    *    <code>null</code>, the subject of the current request is used.
    * @since 4.0
    */
   public List getSubjectGlobalAttributes( PSSubject subject );


   /**
    * Gets the role specific attributes for the specified subjects.
    *
    * @param subjectNameFilter A single pattern used to find the individual(s)
    *    whose attributes you wish. Wildcards allowed following SQL LIKE
    *    syntax. If <code>null</code> or empty, all subjects are included.
    *
    * @param subjectType One of the PSSubject.SUBJECT_TYPE_xxx flags.
    *    Provide 0 to ignore this property.
    *
    * @param roleName A valid role. Can't be <code>null</code> or empty. If the
    *    role doesn't exist, an empty list is returned.
    *
    * @param attributeNameFilter  A pattern used to select the desired
    *    attributes. Use SQL LIKE syntax. Supply empty or <code>null</code> to
    *    get all attributes.
    *
    * @return A valid List of 0 or more PSSubject objects, each containing 1 or
    *    more attributes that match the supplied criteria.  May be empty.
    *    Subjects without matching attributes will not be included in the List.
    *
    * @throws IllegalArgumentException If <code>roleName</code> is
    *    <code>null</code> or empty.
    * @since 4.0
    */
   public List getSubjectRoleAttributes(String subjectNameFilter,
      int subjectType, String roleName, String attributeNameFilter);


   /**
    * Convenience method that gets the role attributes of the subject that
    * made the current request. Calls {@link
    * #getSubjectRoleAttributes(PSSubject,String) getSubjectRoleAttributes(
    * null, roleName)}.
    *
    * @since 4.0
    */
   public List getSubjectRoleAttributes( String roleName );
   
   /**
    * Get the subject who made the original request.
    * 
    * @return the subject who made the original request, never 
    *    <code>null</code>.
    */
   public PSSubject getOriginalSubject();
   
   /**
    * @return get the authenticated user name from the request
    */
   public String getUserName();
   
   /**
    * Convenience method that gets all of the role specific attributes for the
    * supplied subject. See {@link
    * #getSubjectRoleAttributes(String, int, String, String) here}
    * for a full description.
    *
    * @param subject The definition of a subject in the Rx security system. If
    *    <code>null</code>, the subject of the current request is used. If the
    *    request is being made by an anonymous user, an empty list is returned.
    *
    * @param roleName The name of the role to which the supplied subject
    *    belongs and may have attributes. Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if roleName is <code>null</code> or
    *    empty.
    * @since 4.0
    */
   public List getSubjectRoleAttributes( PSSubject subject, String roleName );

   /**
    * Get all email addresses from all subjects that belong to the supplied 
    * role.
    * Convenience method that calls {@link #getRoleEmailAddresses(String, 
    * String, String, Set) roleName, emailAttributeName, community, null}.
    */
   public Set getRoleEmailAddresses(String roleName, String emailAttributeName,
      String community);

   /**
    * Get all email addresses from all subjects that belong to the supplied 
    * role for all defined security providers.
    * 
    * @param roleName the role for which to get all subject emails, not
    *    <code>null</code> or empty.
    * @param emailAttributeName the email attribute name used for backend 
    *    security providers, may be empty or <code>null</code>. If supplied 
    *    all emails defined through backend security providers are included, 
    *    otherwise backend security provider lookups will be skipped.
    * @param community the community for which to filter the result, may
    *    be <code>null</code> or empty to ignore the community filter.
    * @param subjectsWithoutEmail an empty set in which all subjects (as 
    *    <code>PSSubject</code> objects) without an email address will be 
    *    returned. May be <code>null</code> if users without email are not of
    *    interest.
    * @return a set of email addresses as <code>String</code> objects in alpha 
    *    ascending order, never <code>null</code>, may be empty.
    */
   public Set getRoleEmailAddresses(String roleName, String emailAttributeName,
      String community, Set subjectsWithoutEmail);
      
   /**
    * Get all email addresses from the supplied subject.
    * 
    * @param subjectName the subject from which to get all emails, not
    *    <code>null</code> or empty.
    * @param emailAttributeName the email attribute name, may be empty or 
    *    <code>null</code>. If supplied all emails defined in the backend
    *    tables are included, otherwise backend lookups will be skipped.
    * @param community the community for which to filter the result, may
    *    be <code>null</code> or empty to ignore the community filter.
    * @return a set of email addresses as <code>String</code> objects, 
    *    never <code>null</code>, may be empty.
    */
   public Set getSubjectEmailAddresses(String subjectName, 
      String emailAttributeName, String community);

   /**
    * Now behaves the same as calling {@link #getRoleSubjects(String,int,
    * String) getRoleSubjects(roleName, memberFlags, null)}. If an exception
    * occurs while getting the members, an empty list is returned.
    *
    * @param flags No longer supported.
    *
    * @deprecated use {@link #getRoleSubjects(String, int, String)} to obtain
    * information on role members
    */
   public List getRoleMembers(String roleName, int flags, int memberFlags);

   /**
    * Retrieve user context information from the current request context.
    *
    *
    * @param   contextItem    The name of the desired aspect of the user
    *                         context.  See the following table for a list
    *                         of the supported items.
    *
    * <TABLE BORDER="1">
    *    <TR ALIGN="center"><TH COLSPAN="2">Context Item Identifiers</TH></TR>
    *    <TR><TH>Element</TH><TH>Description</TH></TR>
    *    <TR>
    *         <TD>SessionId</TD>
    *       <TD>The session identifier associated with this request</TD>
    *    </TR>
    *    <TR>
    *         <TD>User/Name</TD>
    *       <TD>An entry for each user the requestor has been
    *              authenticated as</TD>
    *    </TR>
    *    <TR>
     *    <TR>
    *         <TD>User/Attributes</TD>
    *       <TD>The attributes associated with this user (security provider
    *         specific).</TD>
    *    </TR>
    *    <TR>
    *         <TD>Roles/RoleName</TD>
    *       <TD>The roles the requestor is a member of</TD>
    *    </TR>
    *    <TR>
    *         <TD>DataAccessRights/query</TD>
    *       <TD>1 if the requestor can query; 0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DataAccessRights/insert</TD>
    *       <TD>1 if the requestor can insert; 0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DataAccessRights/update</TD>
    *       <TD>1 if the requestor can update; 0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DataAccessRights/delete</TD>
    *       <TD>1 if the requestor can delete; 0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DesignAccessRights/modifyAcl</TD>
    *       <TD>1 if the requestor can modify the applciation's ACL;
    *           0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DesignAccessRights/readDesign</TD>
    *       <TD>1 if the requestor can read the application design;
    *           0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DesignAccessRights/updateDesign</TD>
    *       <TD>1 if the requestor can update the application design;
    *           0 otherwise</TD>
    *    </TR>
    *    <TR>
    *         <TD>DesignAccessRights/deleteDesign</TD>
    *       <TD>1 if the requestor can delete the application design;
    *           0 otherwise</TD>
    *    </TR>
    * </TABLE>
    *
    * @param   defValue A default object to be returned, in the case where
    *          the desired aspect is <code>null</code>
    *
    * @throws PSDataExtractionException   if an error occurs extracting the
    *                                     user context information
    *
    * @throws IllegalArgumentException    if roleName is invalid
    */
   public Object getUserContextInformation(String contextItem, Object defValue)
      throws PSDataExtractionException;

   /**
    * Sends a trace message for debugging purposes.  The message will be written
    * to the trace output specified by the application.  The message may contain
    * new lines, but if the lines are longer than the trace output column width
    * setting for the application, they will be wrapped.
    *
    * @param message The text of the message.
    */
   public void printTraceMessage(String message);

   /**
    * Checks to see if tracing for the current application is enabled, and
    * also if tracing option for Exit trace messages is enabled.
    *
    * @return <code>true</code> if both tracing is enabled for the application
    *          and for the exit tracing option.
    */
   public boolean isTraceEnabled();


   /**
    * Adds an HTTP header to the list of headers that will be written when
    * the response is sent. There are 3 types of headers: general, response
    * and entity. Depending on type, the server may limit what values can
    * be set. Only the following headers can be set with this method:
    * <ol>
    *    <li>Cache-Control</li>
    *    <li>Connection</li>
    *    <li>Date</li>
    *    <li>Pragma</li>
    *    <li>Transfer-Encoding</li>
    *    <li>Upgrade</li>
    *    <li>Via</li>
    * </ol>
    * The headers are written in the following order: general, response,
    * cookies and entity headers.
    *
    * @param name The name of the header, case insensitive. May not be <code>
    *    null</code>.
    *
    * @param value The value for this header. May not be <code>null</code>.
    *
    * @return <code>true</code> if the header is set, <code>false</code> if
    *    it is not recognized.
    *
    * @throws IllegalArgumentException if name or value is <code>null</code>
    */
   public boolean setGeneralHeader( String name, String value );


   /**
    * Adds an HTTP header to the list of headers that will be written when
    * the response is sent. Any header can be written using this method. If
    * the header is described in the doc for either {@link
    * #setGeneralHeader(String,String) setGeneralHeader} or {@link
    * #setResponseHeader(String,String) setResponseHeader}, those methods
    * should be used to set those header variables.
    *
    * @param name The name of the header, case insensitive. May not be <code>
    *    null</code> or empty.
    *
    * @param value The value for this header. May not be <code>null</code>.
    *
    * @return always <code>true</code>
    *
    * @see #setGeneralHeader
    *
    * @throws IllegalArgumentException if name is <code>null</code> or empty
    *    or value is <code>null</code>.
    */
   public boolean setEntityHeader( String name, String value );


   /**
    * Adds an HTTP header to the list of headers that will be written when
    * the response is sent. Only the following headers can be set with this
    * method:
    * <ol>
    *    <li>Age</li>
    *    <li>Location</li>
    *    <li>Proxy-Authenticate</li>
    *    <li>Public</li>
    *    <li>Retry-After</li>
    *    <li>Server</li>
    *    <li>Vary</li>
    *    <li>Warning</li>
    *    <li>Set-Cookie</li>
    *    <li>WWW-Authenticate</li>
    * </ol>
    *
    * @param name The name of the header, case insensitive. May not be <code>
    *    null</code>.
    *
    * @param value The value for this header. May not be <code>null</code>.
    *
    * @return <code>true</code> if the header is set, <code>false</code> if
    *    it is not recognized.
    *
    * @throws IllegalArgumentException if name or value is <code>null</code>
    */
   public boolean setResponseHeader( String name, String value );


   /**
    * Convenience method for getting an internal request without adding to the
    * request parameters (which are cloned from the current request).
    * Calls {@link #getInternalRequest(String, Map, boolean)
    * getInternalRequest(resource, null, true)}.
    */
   public IPSInternalRequest getInternalRequest(String resource);

   /**
    * Gets the internal request object used to execute a request against the
    * specified resource.  The internal request object will have its own
    * request context seeded with the values of, but independent from, this
    * request context.  (Changes made to the request context during the
    * internal request will not be reflected in the original request context.)
    * Any parameters specified in a query string as part of
    * <code>resource</code> or included <code>extraParams</code> will be added
    * to the internal request's context.
    *
    * <p>TODO:- Update this documentation when category support is
    * implemented</p>
    *
    * @param resource specifies the application and page of the dataset to make
    * an internal request of.  Specifies the application and page of the dataset
    * to which the internal request is to be made.  May optionally include a
    * "query string" -- name/value pairs, separated by equals, delimited by
    * ampersand, and identified as the portion of the path following a question
    * mark.  May be as little as "<code>appName/pageName</code>" or as much as
    * "<code>http://127.0.0.1:9992/Rhythmyx/AppTest/nov.xml?alpha=bravo&test=5
    * </code>".  Not <code>null</code> or empty.
    *
    * @param extraParams an optional group of parameters to be added to the
    * internal request's context.  Skipped if <code>null</code>.
    *
    * @param inheritCurrentParams If <code>true</code>, the internal request
    * context will contain copies of all of the parameters in the current
    * request in addition to any parameters from the query string in
    * <code>resource</code> and <code>extraParams</code>.
    * <p>
    * If <code>false</code>, the internal request context will only
    * have those parameters supplied by the query string in
    * <code>resource</code> and <code>extraParams</code>.
    *
    * @return the internal request object, <code>null</code> if no suitable
    * handler was found.
    *
    * @throws IllegalArgumentException if the request string is
    * <code>null</code>, empty, or not in the correct format.
    */
   public IPSInternalRequest getInternalRequest(String resource,
                                                Map extraParams,
                                                boolean inheritCurrentParams);

  /**
    * Gets the content header override string.  If this value is
    * <code>null</code>, no override is to be made.  If this value
    * is the empty string, then the content header will be cleared.
    *
    * @return     the full content header to use for this request
    */
   public String getContentHeaderOverride();

   /**
    * Sets the content header override string.  This string will be returned
    * in query responses instead of computing a mimetype/encoding based on the
    * request extension.  To disable overriding the content header, set to
    * <code>null</code>.  To use no content header, set to the empty string.
    * All other strings will be passed as the complete content header as
    * specified.
    *
    * @param   header   The string to use as the content header for this
    *                   request, or <code>null</code> if no override is
    *                   to be used.
    */
   public void setContentHeaderOverride(String header);


   /**
    * Get the security token associated with this request.  This represents the
    * user's session and can be used to make requests in certain situations
    * where a full request context is not required.
    *
    * @return The token, never <code>null</code>.
    */
   public PSSecurityToken getSecurityToken();

   /**
    * Returns the locale orlanguage string associated with this user request,
    * Never <code>null</code> or empty.
    */
   public String getUserLocale();

   /**
    * Each request is created with a request timer that allows us to track the
    * time spent in that individual request. This method allows the exclusion
    * of time from that total time. Pause can be called any number of times, 
    * but should be matched with calls to {@link #continueRequestTimer()} to
    * restart the timer. Note that the timer does not actually do any 
    * processing while running, it simply tracks the start time, end time and
    * any accumulated pause time to provide statistics. See 
    * {@link com.percussion.util.PSStopwatch PSStopwatch} for more details on
    * this type of timer.
    */
   public void pauseRequestTimer();
   
   /**
    * Attempts to replace any subjects in the supplied list that are goups with
    * subjects representing the members of the groups. Handling of nested groups
    * is implementation specific depending on the cataloger that resolves the
    * group. Any supplied subjects that are users are simply added to the
    * returned set.
    * 
    * @param subjects A set of subjects, may contain both users and groups.
    * Never <code>null</code>, may be empty.
    * 
    * @return A new set containing all users supplied, and any users that were
    * the result of expanding the membership of any groups supplied, and any
    * groups that could not be expanded or that were returned as members of a
    * supplied group.
    */
   public Set<PSSubject> expandGroups(Set<PSSubject> subjects);

   /**
    * Turns the timer back "on" for the request. See 
    * {@link #pauseRequestTimer()} for the details of this.
    */
   public void continueRequestTimer();
   
   /**
    * The request page type is unknown.
    */
   public static final int      PAGE_TYPE_UNKNOWN  = 0x00000000;

   /**
    * The desired format for the request page is XML.
    */
   public static final int      PAGE_TYPE_XML      = 0x00000001;

   /**
    * The desired format for the request page is HTML.
    */
   public static final int      PAGE_TYPE_HTML     = 0x00000002;

   /**
    * The desired format for the request page is text.
    */
   public static final int      PAGE_TYPE_TEXT     = 0x00000004;

 


}
