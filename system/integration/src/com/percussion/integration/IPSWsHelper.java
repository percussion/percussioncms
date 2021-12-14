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
package com.percussion.integration;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.xml.soap.SOAPException;

import org.w3c.dom.Document;

/**
 * This interface provides the public API to access Rhythmyx web services.
 * Examples of using these methods can be found in the tag library source
 * included with Rhythmyx. See the file <code>taglibsrc.jar</code>
 */
public interface IPSWsHelper
{
   /**
    * Get the Rhythmyx inbox.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param fieldList a list of strings to identify which fields
    *    to be returned with each content item found, may be <code>null</code>
    *    or empty in which case a default set of fields will be returned
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object with the result of content items, this is 
    *    defined in sys_SearchParameters.xsd schema
    *    
    * @throws RemoteException
    */
   PSSearch getInbox(
      HttpServletRequest req,
      List fieldList,
      boolean includeActionMenu)
      throws RemoteException, Exception;
   /**
    * Get the Rhythmyx inbox.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param fieldList a list of strings to identify which fields
    *    to be returned with each content item found, may be <code>null</code>
    *    or empty in which case a default set of fields will be returned
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object with the result of content items, this is 
    *    defined in sys_SearchParameters.xsd schema
    * 
    * @throws RemoteException
    */
   PSSearch getInbox(
      String username,
      String password,
      List fieldList,
      boolean includeActionMenu)
      throws RemoteException, Exception;
   /**
    * Call any Rhythmyx application resource and get the resultant data.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param appLocation the location of the Rhythmyx application resource, 
    *    assumed not <code>null</code> or empty
    * @param paramMap a map of parameters to be added as HTML paramters to
    *    the call, may be <code>null</code>
    *    
    * @return string representing the call to the resource (usually HTML)
    * 
    * @throws RemoteException
    */
   String executeCallDirect(
      HttpServletRequest req,
      String appLocation,
      Map paramMap)
      throws RemoteException, Exception;
   /**
    * Call any Rhythmyx application resource and get the resultant data.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param appLocation the location of the Rhythmyx application resource, 
    *    assumed not <code>null</code> or empty
    * @param paramMap a map of parameters to be added as HTML paramters to
    *    the call, may be <code>null</code>
    *    
    * @return string representing the call to the resource (usually HTML)
    * 
    * @throws RemoteException
    */
   String executeCallDirect(
      String username,
      String password,
      String appLocation,
      Map paramMap)
      throws RemoteException, Exception;
   /**
    * Search for content items within the Rhythmyx system.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param search contains the search information to decide what selection
    *    and criteria to build the search, may not be <code>null</code>
    * @param filter the filter interface to apply to the result set, may be
    *    <code>null</code> @not implemented yet
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object that contains the search results
    * 
    * @throws RemoteException
    */
   PSSearch search(
      HttpServletRequest req,
      PSSearch search,
      IPSSearchFilter filter,
      boolean includeActionMenu)
      throws RemoteException, Exception;
   /**
    * Search for content items within the Rhythmyx system.
    * 
    * @param username username for authentication header, may not be <code>null
    *    <code> or empty
    * @param password password for authentication header, may not be <code>null
    *    <code> or empty
    * @param search contains the search information to decide what selection
    *    and criteria to build the search, may not be <code>null</code>
    * @param filter the filter interface to apply to the result set, may be
    *    <code>null</code> @not implemented yet
    * @param includeActionMenu flag indicating whether or not to retrieve the 
    *    action menu for each item returned, if <code>false</code> do not
    *    retrieve the action menu
    *    
    * @return a PSSearch object that contains the search results
    * 
    * @throws RemoteException
    */
   PSSearch search(
      String username,
      String password,
      PSSearch search,
      IPSSearchFilter filter,
      boolean includeActionMenu)
      throws RemoteException, Exception;

   /**
    * Sets the image path for use in certain calls such as the action menu
    * call direct. Used to determine the location of the images for showing
    * the action menu triangle.
    * 
    * @param imagePath partial path indicating the location of the images,
    *    must not be <code>null</code> or empty
    */
   void setImagePath(String imagePath);

   /**
    * Formats the supplied content type list response into a map.
    * 
    * @param request that contains information to extract the content
    * list for, may not be <code>null</code>
    * @param sort <code>true</code> to return the content type map sorted
    *    in alpha order, <code>false</code> otherwise
    *    
    * @return a map of content types, the map key is the content type name,
    *    as <code>String</code>, the value is the <code>ContentTypeAnonType</code>
    *    object, never <code>null</code>, may be empty
    */
   Map formatContentTypeList(HttpServletRequest request, boolean sort)
      throws Exception, RemoteException;

   /**
    * Returns the fully qualified url for each content editor type specified
    * by the type parameter.
    * 
    * @param req the original request from the portlet, may not be 
    *    <code>null</code>
    * @param types the returned map from formatContentTypeList that contains
    * information about the types, may not be <code>null</code>
    * @param type the name of the content type to get the qualified url for, may not be
    *    </code>null</code>
    * @throws MalformedURLException if the url contained in the 
    * <code>types</code> map is bad
    *    
    * @return the string of the qualified location of the editor
    */
   String formatEditorUrl(
      HttpServletRequest req,
      Map types,
      String type) throws MalformedURLException;

   /**
    * Returns the content type description out of the map created by formatContentTypeList
    * 
    * @param type the name of the type of interest, may not be <code>null</code>
    * @param types the returned map from formatContentTypeList that contains
    * information about the types, may not be <code>null</code>
    * @return the string description for the given type
    */
   String getTypeDescription(String type, Map types);
   
   /**
    * This method tells the caller if the attached rhythmyx host is using
    * full text search. Note that the results of this are cached on the 
    * client.
    * 
    * @param request a valid request that must never be <code>null</code>.
    * @return <code>true</code> if the server is using full text search
    * 
    */
   boolean isFtsEnabled(HttpServletRequest request) 
   throws SOAPException, RemoteException;

   /**
    * Return the loaded properties map.
    * 
    * @return a map of the properties loaded at init time, 
    *    never <code>null</code>.
    */
   Properties getProperties();

   /**
    * Get the protocol used for rhythmyx requests.
    * 
    * @return the protocol, never <code>null</code> or empty. Defaults to 
    *    <code>http</code> if not specified in the properties file.
    */
   String getProtocol();
   /**
    * Get the host for rhythmyx requests.
    * 
    * @return the rhythmyx host, never <code>null</code> or empty. Defaults to
    *    <code>localhost</code> if not specified in the properties file.
    */
   String getHost();

   /**
    * Get the port used for rhythmyx requests.
    * 
    * @return teh rhythmyx port. Defaults to <code>7501</code> if not specified
    *    in the properties file.
    */
   int getPort();

   /**
    * Gets the rhythmyx root defined in the properties file loaded at startup.
    * 
    * @return the Rhythmyx root, never <code>null</code> or empty. Defaults to
    *    <code>Rhythmyx</code> if not specified in the properties file.
    */
   String getRhythmyxRoot();
   
   /**
    * Return the rhythmyx root with an appropriate starting and ending 
    * slash for use in resource lookups.
    * 
    * @return The rhythmyx root, never <code>null</code>.
    */
   String getAbsRxRoot();
   
   /**
    * Get the complete rhythmyx url, something like 
    *    <code>protocol://host:port/RhythmyxRoot</code>.
    *    
    * @return the rhythmyx url, never <code>null</code> or empty. The port
    *    is skipped if the default port 80 is specified.
    */
   String getRhythmyxUrl();

   /**
    * Gets the target enpoint for SOAP requests. This is the location where all
    * SOAP messages will be sent.
    * 
    * @return a url to the target endpoint for the SOAP messages, will not be
    *    <code>null</code>
    */
   URL getPortURL();

   /**
    * Helper to convert from a string to a DOM.
    * 
    * @param data the string to be converted to a DOM, 
    *    assumed not <code>null</code>
    *    
    * @return a document based on the data specified
    */
   Document toDOM(String data);
   
   /**
    * Create the appropriate url to bring up the action page for a 
    * given contentid.
    * 
    * @param contentid The content id to get the action page for, must
    * be valid.
    * @return The action page javascript for the given content 
    * id, never <code>null</code>. The nature of the javascript may depend 
    * on the target portal.
    */
   String getActionPageLink(int contentid);
   
   /**
    * Create the appropriate javascript to bring up the action page for a 
    * given contentid.
    * 
    * @param contentid The content id to get the action page for, must
    * be valid.
    * @param sessionid the rhythmyx session id, must never be <code>null</code>
    * or empty
    * @return The action page javascript for the given content id, never 
    * <code>null</code>.
    */
   String getActionPageLink(int contentid, String sessionid);
   
   /**
    * Return an item viewer search
    * @param req http request, must never be <code>null</code>.
    * @param itemList list of items, must never be <code>null</code>.
    * @param viewUrl view url, must never be <code>null</code>.
    * @param fieldList list of fields, must never be <code>null</code>.
    * @return The search results for an item viewer
    * @throws RemoteException when an error occurs communicating with the
    * Rhythmyx server. 
    * @throws SOAPException when an error occurs in the web services 
    */
   PSSearch getItemViewer(
      HttpServletRequest req,
      String itemList,
      String viewUrl,
      List fieldList)
      throws RemoteException, SOAPException;   
}
