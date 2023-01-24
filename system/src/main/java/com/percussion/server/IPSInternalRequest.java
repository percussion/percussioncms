/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.server;

import com.percussion.data.PSInternalRequestCallException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import org.w3c.dom.Document;

import java.sql.ResultSet;

/**
 * This interface provides the functionality for making an internal request, and
 * if appropriate, retrieving the results of that request. Each instance of this
 * interface represents a particular resource in an application, and may be used
 * to make a single request to that resource. Retrieving results from non-text
 * (binary) resources is not supported. This interface was created to provide a
 * public interface to the IPSInternalRequestHandler functionality for use by
 * exits.
 */
public interface IPSInternalRequest
{
   /**
    * @deprecated This method no longer needs to be called, as the get methods
    *             are now responsible for making their own requests. For
    *             requests that generate no results (for example, updates), use
    *             {@link #performUpdate()}.
    */
   public void makeRequest() throws PSInternalRequestCallException,
         PSAuthorizationException, PSAuthenticationFailedException;

   /**
    * Makes an internal request to the application resource represented by this
    * object, and frees the resources used by the request. As no results are
    * returned, this method is only useful for requests with side-effects (such
    * as database updates). There is no need to call <code>cleanUp()</code>
    * after calling this method.
    * 
    * @throws PSInternalRequestCallException if any error occurs processing the
    *            internal request call
    * @throws PSAuthorizationException if the user is not authorized
    * @throws PSAuthenticationFailedException if the user failed to authenticate
    */
   public void performUpdate() throws PSInternalRequestCallException,
         PSAuthorizationException, PSAuthenticationFailedException;

   /**
    * Makes an internal request to the application resource represented by this
    * object, collects the resulting XML document, and frees the resources used
    * by the request. There is no need to call <code>makeRequest()</code>
    * before calling this method, and there is no need to call <code>cleanUp()
    * </code>
    * after calling this method.
    * <p>
    * TODO: If the application resource represents an update resource with a
    * redirect, the XML document will be generated by making an internal request
    * to the resource indicated by the redirect.
    * 
    * @return The generated document from this request, never <code>null</code>.
    * 
    * @throws PSInternalRequestCallException if any error occurs processing the
    *            internal request call, or if the query was made against a
    *            non-text resource
    * 
    * @throws UnsupportedOperationException if this method is called on an
    *            update resource
    */
   public Document getResultDoc() throws PSInternalRequestCallException;

   /**
    * Makes an internal request to the application resource represented by this
    * object, collects the resulting output, and frees the resources used by the
    * request. A stylesheets is applied if so indicated by the specified by the
    * requested page, subject to page selection criteria evaluated against the
    * request parameters. There is no need to call <code>makeRequest()</code>
    * before calling this method, and there is no need to call
    * <code>cleanUp()</code> after calling this method.
    * <p>
    * TODO: If the application resource represents an update resource with a
    * redirect, the XML document will be generated by making an internal request
    * to the resource indicated by the redirect.
    * 
    * @return The generated output from this request, never <code>null</code>.
    * 
    * @throws PSInternalRequestCallException if any error occurs processing the
    *            internal request call, or if the query was made against a
    *            non-text resource
    * 
    * @throws UnsupportedOperationException if this method is called on an
    *            update resource
    */
   public byte[] getMergedResult() throws PSInternalRequestCallException;

   /**
    * Makes an internal request to the application resource represented by this
    * object, collects the resulting output, and frees the resources used by the
    * request. There is no need to call <code>makeRequest()</code> before
    * calling this method, and there is no need to call <code>cleanUp()</code>
    * after calling this method.
    * <p>
    * If the application resource represents a non-text resource or if the
    * mimecontent is null then throws PSInternalRequestCallException.
    * 
    * @return The generated output from this request, never <code>null</code>.
    * 
    * @throws PSInternalRequestCallException if any error occurs processing the
    *            internal request call, or if the query was made against a
    *            non-text resource
    * 
    * @throws UnsupportedOperationException if this method is called on an
    *            update resource
    */
   public byte[] getContent() throws PSInternalRequestCallException;

   /**
    * After {@link #makeRequest()} is called, this method can be called to get
    * the result set that resulted from the request. This method may only be
    * used with a query resource; if the request was for an update, binary, or
    * content editor resource, this method will throw an exception.
    * <p>
    * This method may only be called once, and may not be called if {@link
    * #getResultDoc()} has been called. It is the caller's responsiblity to call
    * {@link #cleanUp()} once they are finished with this request.
    * 
    * <I>Note: Since we are directly requesting the result set, no result
    * document or udf processing defined in the specified application will be
    * executed on this data.</I>
    * 
    * @return The result set generated by this request. The caller must call
    *         {@link #cleanUp()} to close this result set once they are finished
    *         with it.  This result set must be used and closed while this
    *         internal request is in scope otherwise it may be closed if the
    *         internal request is garbage collected resulting in an error the
    *         next time the result set is used.
    * 
    * @throws IllegalStateException if this method has already been called, if
    *            {@link #getResultDoc()} has been called or if
    *            {@link #makeRequest()} has not yet been called, or if this
    *            request is not being made against a query resource.
    * 
    * @throws PSInternalRequestCallException if any error occurs processing the
    *            internal request call
    */
   public ResultSet getResultSet() throws PSInternalRequestCallException;

   /**
    * Releases any resources held by this request. Must be called before leaving
    * scope if {@link #getResultSet()} has been called. It is safe to call this
    * more than once.
    */
   public void cleanUp();

   /**
    * Gets the request context used by this internal request.
    * 
    * @return The request context, never <code>null</code>.
    */
   public IPSRequestContext getRequestContext();

   /**
    * Returns the type of resource to which request can be made using this
    * object.
    * <p>
    * <table border=1>
    * <tr>
    * <th>Resource type</th>
    * <th>Returned value</th>
    * </tr>
    * <tr>
    * <td>File Request</td>
    * <td><code>REQUEST_TYPE_FILE_SYSYSTEM</code></td>
    * </tr>
    * <tr>
    * <td>Query Resource</td>
    * <td><code>REQUEST_TYPE_QUERY</code></td>
    * </tr>
    * <tr>
    * <td>Update Resource</td>
    * <td><code>REQUEST_TYPE_UPDATE</code></td>
    * </tr>
    * <tr>
    * <td>Content Editor Resource</td>
    * <td><code>REQUEST_TYPE_CONTENT_EDITOR</code></td>
    * </tr>
    * </table>
    * 
    * @return one of <code>REQUEST_TYPE_xxx</code> values
    */
   public int getRequestType();

   /**
    * If this associated internal request handler doesn't return a document,
    * this method returns <code>true</code>
    * 
    * @param req the request object, never <code>null</code>
    * @return <code>true</code> if the associate internal request handler does
    *         not return a document
    */
   public boolean isBinary(PSRequest req);

   /**
    * Constant for request made to obtain a file from the file system.
    */
   public static final int REQUEST_TYPE_FILE_SYSYSTEM = 0;

   /**
    * Constant for request made to a query resource.
    */
   public static final int REQUEST_TYPE_QUERY = 1;

   /**
    * Constant for request made to an update resource.
    */
   public static final int REQUEST_TYPE_UPDATE = 2;

   /**
    * Constant for request made to a content editor resource.
    */
   public static final int REQUEST_TYPE_CONTENT_EDITOR = 3;

}
