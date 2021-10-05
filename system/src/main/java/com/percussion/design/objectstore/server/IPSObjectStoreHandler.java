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

package com.percussion.design.objectstore.server;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNonUniqueException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestHandler;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import org.w3c.dom.Document;


/**
 * The IPSObjectStoreHandler interface extends the IPSRequestHandler
 * interface, defining the mechanism by which an object store request 
 * should be handled by an object store handler.
 * 
 * @author     Tas Giakouminakis
 * @version        1.0
 * @since      1.0
 */
public interface IPSObjectStoreHandler extends IPSRequestHandler {
   
   /**
    * Initializes the object store, returning all applications (both
    * enabled and disabled) that are defined in the object store.
    *
    * @return  PSApplication[]
    * 
    * @throws  PSServerException
    * @throws  PSAuthorizationException
    */
   public abstract PSApplication[] init()
      throws PSServerException,
         PSAuthorizationException;

   /**
    * Add a listener for changes to application objects.
    *
    * @param     the listener object
    *
    * @exception PSAuthorizationException if the user is not permitted to
    *                            listen for the specified events
    */
   public abstract void addApplicationListener(IPSApplicationListener listener)
      throws PSAuthorizationException;
   
   /**
    * Remove a previously installed application event listener.
    *
    * @param     the listener object
    */
   public abstract void removeApplicationListener(IPSApplicationListener listener);
   
   /**
    * Add a listener for changes to server objects.
    *
    * @param     the listener object
    *
    * @exception PSAuthorizationException if the user is not permitted to
    *                            listen for the specified events
    */
   public abstract void addServerListener(
      IPSServerConfigurationListener listener)
      throws PSAuthorizationException;
   
   /**
    * Remove a previously installed server event listener.
    *
    * @param     the listener object
    */
   public abstract void removeServerListener(
      IPSServerConfigurationListener listener);
   
   /**
    * Extend the write lock on an application. Write locks are granted for a
    * maximum of 30 minutes. If the designer needs more time to complete the
    * task, an additional 30 minute extension can be requested.
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if the user is not permitted to
    *                              create applications on the
    *                              server
    *
    * @exception PSLockedException       if another user has acquired the
    *                              application lock. This usually 
    *                              occurs if the application was
    *                              not previously locked or the
    *                              lock was lost due to a timeout.
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract Document extendApplicationLock(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSLockedException,
         PSUnknownDocTypeException, PSNotFoundException;
   
   /**
    * Constructs an application object for the specified application. The
    * application is loaded from the object store when this method is called.
    * If the application is being loaded for editing, be sure to lock the
    * application.
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have designer
    *                              access to the application
    *
    * @exception PSLockedException       if lockApp is <code>true</code>
    *                              but another user already has the
    *                              application locked
    *
    * @exception PSNotFoundException        if an application be that name
    *                              does not exist
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract Document getApplication(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSLockedException,
         PSNotFoundException, PSUnknownDocTypeException;
   
   /**
    * Constructs an application object for the specified application. The
    * application is loaded from the object store when this method is called.
    * If the application is being loaded for editing, be sure to lock the
    * application.
    *
    * @param     app                  the name of the application
    *
    * @return                         the application object
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSNotFoundException        if an application be that name
    *                              does not exist
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract PSApplication getApplicationObject(String app)
      throws PSServerException, PSNotFoundException;

   /**
    * Gets the server configuration object for this server.
    *
    * @exception PSServerException  If any problems occur loading the data.
    *
    * @exception PSNotFoundException if the configuration information
    * cannot be found
    */
   public abstract PSServerConfiguration getServerConfigurationObject()
      throws PSServerException, PSNotFoundException;

   /**
    * Saves the server configuration information for the specified user
    * to the object store.
    *
    * @param      doc                        the XML document containing the
    *                                        server configuration data
    *
    * @param      req                        the request context
    *                                        (for security)
    *
    * @return                                the XML response document
    *
    * @throws PSUnknownDocTypeException   if doc does not contain the
    *                                        appropriate format for this
    *                                        request type
    *
    * @throws PSLockedException If the config is already locked by someone
    *    else.
    *
    * @see      com.percussion.design.objectstore.PSServerConfiguration
    */
   public Document saveServerConfiguration(Document inDoc, PSRequest req)
      throws PSUnknownDocTypeException, PSLockedException;

   /**
    * Extend the write lock on a server configuration. Write locks are granted for a
    * maximum of 30 minutes. If the administrator needs more time to complete the
    * task, an additional 30 minute extension can be requested.
    *
    * @param     doc                         the XML document containing the
    *                                        server config data
    *
    * @param      req                        the request context 
    *                                        (for security)
    *
    * @return                                the XML response document
    *
    * @exception PSServerException           if the server is not responding
    *
    * @exception PSAuthorizationException    if the user is not permitted to
    *                                        create applications on the
    *                                        server
    *
    * @exception PSLockedException           if another user has acquired the
    *                                        server config lock. This usually 
    *                                        occurs if the server config lock
    *                                        was broken or expired.
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                                        appropriate format for this
    *                                        request type
    *
    * @see       com.percussion.design.objectstore.PSServerConfiguration
    */
   public Document extendServerConfigurationLock(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
             PSAuthenticationFailedException, PSLockedException,
             PSUnknownDocTypeException;

   /**
    * Get all the applications defined in the object store.
    *
    * @param      enabledOnly             <code>true</code> to return
    *                                         only enabled applications
    *
    * @return                          an array of PSApplication
    *                                         objects
    *
    * @exception PSServerException     if the server is not responding
    *
    * @exception PSAuthorizationException    if the user does not have design
    *                               access to the server
    */
   public abstract PSApplication[] getApplicationObjects(boolean enabledOnly)
      throws PSServerException, PSAuthorizationException;
   
   /**
    * Creates an enumeration containing the requested properties of each
    * application for which the user has designer access.
    * <p>
    * The application properties which can be retrieved are:
    * <table border="1">
    * <tr><th>Key</th><th>Value</th></tr>
    * <tr><td>name</td>
    *    <td>the application name</td>
    * </tr>
    * <tr><td>description</td>
    *    <td>the application's description</td>
    * </tr>
    * <tr><td>isEnabled</td>
    *    <td>is the application currently enabled</td>
    * </tr>
    * <tr><td>isActive</td>
    *    <td>is the application currently active</td>
    * </tr>
    * <tr><td>createdBy</td>
    *    <td>the name of the user who created the application</td>
    * </tr>
    * <tr><td>createdOn</td>
    *    <td>the date the application was created</td>
    * </tr>
    * </table>
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if the user does not have design
    *                              access to the server
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    */
   public abstract Document getApplicationSummaries(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSUnknownDocTypeException;

   /**
    * Creates an array of application summary objects. If the user has
    * read access to the application, or is a server administrator, they
    * will be shown the application in the summaries. However, an attempt
    * to access the design of an application they are not readers on will
    * fail.
    *
    * @param     req                the request context (for security)
    *
    * @return                       an array of PSApplicationSummary
    *                               objects which the user is allowed
    *                               to see (may be empty)
    */
   public abstract PSApplicationSummary[] getApplicationSummaryObjects(
      PSRequest req);

   /**
    * Remove the specified application from the object store. This 
    * permanently deletes the application, which cannot be recovered.
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if the user does not have delete
    *                              access to the application
    *
    * @exception PSLockedException       if another user has the
    *                              application locked
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract Document removeApplication(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, PSLockedException,
         PSUnknownDocTypeException;
   
   /**
    * Rename the specified application defined in this object store.
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have update
    *                              access to the application
    *
    * @exception PSLockedException       if another user has the
    *                              application locked
    *
    * @exception PSNonUniqueException       if an application with the new
    *                              name already exists
    *
    * @exception PSNotFoundException        if an application with the old
    *                              name does not exist
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract Document renameApplication(Document doc, PSRequest req)
      throws PSServerException,     PSAuthorizationException,
         PSAuthenticationFailedException, PSLockedException,
         PSNonUniqueException,
         PSNotFoundException,   PSUnknownDocTypeException;
   
   /**
    * Saves the specified application to the object store. If the application
    * was newly created, or it references a different object store, it will
    * be created in this object store. If the application represents an
    * existing application in this object store, it will be updated. This 
    * behavior can also be overriden by using the createNewApp parameter.
    *
    * @param     doc                  the XML document containing the
    *                              application data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if creating a new application,
    *                              the user does not have create
    *                              access on the server. If
    *                              updating an existing
    *                              application, the user does not
    *                              have update access on the
    *                              application.
    *
    * @exception PSNotLockedException       when updating an existing
    *                              application and a lock is not 
    *                              currently held (the timeout
    *                              already expired or
    *                              getApplication was not used
    *                              to lock the application)
    *
    * @exception PSNonUniqueException       if creating an application and
    *                              an application by the same name
    *                              already exists
    *
    * @exception PSSystemValidationException      if validate is <code>true</code>
    *                              and a validation error is
    *                              encountered
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplication
    */
   public abstract Document saveApplication(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSNotLockedException, PSNonUniqueException,
           PSSystemValidationException, PSUnknownDocTypeException;
   
   /**
    * Loads the previously saved user configuration object for the specified
    * user. If configuration information does not exist on the server,
    * an empty object will be returned.
    *
    * @param     doc                  the XML document containing the
    *                              user configuration data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have designer
    *                              access to the server
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSUserConfiguration
    */
   public abstract Document getUserConfiguration(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSUnknownDocTypeException;
   
   /**
    * Loads the character encoding map from the installation root. If the
    * character encoding map does not exist an exception will be thrown.
    *
    * @param     doc                  the XML document containing the
    *                                 character encoding data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have designer
    *                              access to the server
    *
    * @exception PSAuthenticationFailedException if the user is not
    *                                            authenticated to perform this
    *                                            command
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @exception PSNotFoundException the character encoding map does not exist.
    *
    * @see    com.percussion.design.objectstore.PSUserConfiguration
    */
   public abstract Document getCharacterSetMap(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException,
         PSUnknownDocTypeException, PSNotFoundException;

   /**
    * Remove the user configuration information for the specified user.
    * This permanently deletes all the user configuration information,
    * which cannot be recovered.
    *
    * @param     doc                  the XML document containing the
    *                              user configuration data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have designer
    *                              access to the server
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @exception PSNotFoundException        if user configuration
    *                              information does not exist for
    *                              the specified user
    *
    * @see    com.percussion.design.objectstore.PSUserConfiguration
    */
   public abstract Document removeUserConfiguration(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSUnknownDocTypeException, PSNotFoundException;
   
   /**
    * Saves the user configuration information for the specified user
    * to the object store.
    *
    * @param     doc                  the XML document containing the
    *                              user configuration data
    *
    * @param     req                  the request context (for security)
    *
    * @return                         the XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException   if user does not have designer
    *                              access to the server
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSUserConfiguration
    */
   public abstract Document saveUserConfiguration(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSUnknownDocTypeException;
   
   /**
    * Saves the specified application file to the object store.
    *
    * @param doc The XML document containing the application file
    * data.
    *
    * @param     req                  the request context (for security)
    *
    * @return The XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException
    *
    * @exception PSNotLockedException
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @exception PSNotFoundException If the application could not be found.
    *
    * @see    com.percussion.design.objectstore.PSApplicationFile
    */
   public abstract Document saveApplicationFile(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSNotLockedException, PSUnknownDocTypeException, PSNotFoundException;

   /**
    * Loads the specified application file from the object store.
    *
    * @param doc The XML document containing the application file
    * data.
    *
    * @param     req                  the request context (for security)
    *
    * @return The XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException
    *
    * @exception PSNotLockedException
    *
    * @exception PSNotFoundException
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplicationFile
    */
   public abstract Document loadApplicationFile(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSNotLockedException, PSUnknownDocTypeException,
         PSNotFoundException;

   /**
    * Removes the specified application file from the object store.
    *
    * @param doc The XML document containing the application file
    * data.
    *
    * @param     req                  the request context (for security)
    *
    * @return The XML response document
    *
    * @exception PSServerException       if the server is not responding
    *
    * @exception PSAuthorizationException
    *
    * @exception PSNotLockedException
    *
    * @exception PSNotFoundException
    *
    * @exception PSUnknownDocTypeException   if doc does not contain the
    *                              appropriate format for this
    *                              request type
    *
    * @see    com.percussion.design.objectstore.PSApplicationFile
    */
   public abstract Document removeApplicationFile(Document doc, PSRequest req)
      throws PSServerException, PSAuthorizationException,
         PSAuthenticationFailedException, 
         PSNotLockedException, PSUnknownDocTypeException,
         PSNotFoundException;


   /**
    * Validates the given application object with the default validator
    * for the object store. Returns <CODE>true</CODE> if validation
    * passed, returns <CODE>false</CODE> if validation fails or if validation
    * could not be performed.
    *
    * @author  chadloder
    * 
    * @version 1.9 1999/06/30
    * 
    * @param   app The application to be validated.
    * 
    * 
    * @throws  PSServerException
    * @throws  PSAuthorizationException
    * @throws PSSystemValidationException ;
    * 
    */
   public boolean validateApplicationObject(PSApplication app)
      throws PSServerException, PSAuthorizationException, PSSystemValidationException;
   
   /**
    * Checks that the given session's security matches the given security for
    * the application with the given request root.
    *
    * @author  chadloder
    * 
    * @version 1.11 1999/07/12
    * 
    * @param   requestRoot The request root for the application.
    * @param   accessLevel The access level.
    * @param   session The user session.
    * 
    * @return  boolean <CODE>true</CODE> if the session has the requested
    * permissions for the app with the given request root, <CODE>false</CODE>
    * otherwise.
    * 
    * @throws  PSServerException
    * @throws  PSNotFoundException
    * 
    */
   public boolean checkApplicationSecurity(
      String requestRoot,
      int accessLevel,
      PSUserSession session
      )
      throws PSNotFoundException, PSServerException;

   /**
    * Get the request processing statistics for this object store.
    *
    * @return     the statistics
    */
   public PSObjectStoreStatistics getStatistics();


   /* ************ IPSRequestHandler Interface Implementation ************ */
   
   /**
    * Process the request using the input context information and data.
    * The results must be written to the specified output stream.
    * 
    * @param   request  the request object containing all context
    *                data associated with the request
    */
   public abstract void processRequest(PSRequest request);
}

