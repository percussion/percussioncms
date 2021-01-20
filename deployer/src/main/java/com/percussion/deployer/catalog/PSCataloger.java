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

package com.percussion.deployer.catalog;

import com.percussion.conn.PSServerException;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSServerLockException;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
* This class is used to handle the catalog requests against a deployment server.
* 
* <p> To use the cataloger, a connection to the deployment server must first be
* established. Requests can then be made through this object. The catalog
* request results in a {@link PSCatalogResultSet resultset} object. The
* supported catalog request types are:
* <ol>
* <li>Archives</li>
* <li>DBMSDrivers</li>
* <li>CustomElementTypes</li>
* <li>DeployableElementTypes</li>
* <li>Descriptors<li>
* <li>LiteralIDTypes</li>
* <li>IDTypeObjects</li>
* <li>PackageLogs</li>
* <li>UserDependencies</li>
* <li>
* </ol>
*/
public class PSCataloger
{
   /**
    * Creates a cataloger that wil be connected to the server specified by the 
    * connection to serve the catalog requests.
    *
    * @param conn   the connection object for the desired deployment server, may 
    * not be <code>null</code> and must be connected.
    *
    * @throws IllegalArgumentException   if <code>conn</code> is <code>null</code>
    * or not connected.
    */
   public PSCataloger(PSDeploymentServerConnection conn)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");
         
      if(!conn.isConnected())
         throw new IllegalArgumentException("conn must be connected");

      m_conn = conn;
   }

   /**
   * Perform a catalog request against the connected deployment server. Formats 
   * the request as an xml document with root element referring to the request 
   * type and any properties supplied as its immediate children with parameter 
   * name as element name and value as the element value. Builds the {@link  
   * PSCatalogResultSet results} object from the result document and returns. 
   * <p>      
   * The following table describes the request type, corresponding properties  
   * required if it requires properties. The validation for required properties 
   * is done by server.
   * <table border=1>
   * <tr>
   * <th>Request Type</th><th>Require Properties?</th><th>Property Names</th>
   * </tr>
   * <tr><td>Archives</td><td>No</td><td></td></tr>
   * <tr><td>DBMSDrivers</td><td>No</td><td></td></tr>
   * <tr><td>CustomElemen Types</td><td>No</td><td></td></tr>
   * <tr><td>DeployableElementTypes</td><td>No</td><td></td></tr>
   * <tr><td>Descriptors</td><td>No</td><td></td></tr>
   * <tr><td>LiteralIDTypes</td><td>No</td><td></td></tr>
   * <tr><td>IDTypeObjects</td><td>Yes</td><td>Type (One of the Literal 
   * ID Types)</td></tr>
   * <tr><td>PackageLogs</td><td>No</td><td></td></tr>
   * <tr><td>UserDependencies</td><td>optional</td><td>directory path (if this 
   * is not provided, it gives the top-level directory to catalog further down 
   * for user dependency files)</td>
   * </tr>
   * </table> 
   * The following table represents the result structure for each request.
   * <table border=1>
   * <tr>
   * <th>Request Type</th><th>ID</th><th>DisplayText</th><th>hasColumns?</th>
   * </tr>
   * <tr><td>Archives</td><td>archiveLogID</td><td>archive Name</td>Yes</tr>
   * <tr><td>DBMSDrivers</td><td>driver name</td><td>driver name</td><td>No
   * </td></tr>
   * <tr><td>CustomElementTypes</td><td>element type</td><td>element type name
   * </td><td>No</td></tr>
   * <tr><td>DelpoyableElementTypes</td><td>element type</td><td>element type
   * name</td><td>No</td></tr>
   * <tr><td>Descriptors</td><td>Descriptor Name</td><td>Descriptor Name</td>
   * <td>Yes</td></tr>
   * <tr><td>LiteralIDTypes</td><td>id type</td><td>id type</td><td>No</td></tr>
   * <tr><td>IDTypeObjects</td><td>Object ID</td><td>Object Name</td><td>No</td>
   * </tr>
   * <tr><td>PackageLogs</td><td>packageLogID</td><td>package name</td><td>Yes
   * </td></tr>
   * <tr><td>UserDependencies</td><td>the absolute path of file/directory</td>
   * <td>the directory/file name</td><td>No</td></tr>
   * </table>    
   * 
   * @param requestType the catalog request type must be one of the 
   * TYPE_REQ_xxx values
   * @param props the additional parameters and values to be supplied with the 
   * request to the server, may be <code>null</code> or empty.
   * 
   * @return the result set object constructed from the result document, never
   * <code>null</code>
   * 
   * @throws IOException if an IO error occurs
   * @throws PSAuthorizationException if deployment access to the server is 
   * denied.
   * @throws PSAuthenticationFailedException if the user cannot be authenticated
   * by the server.
   * @throws PSServerException if the server is not responding or invalid 
   * parameters are supplied to the request .
   * @throws PSDeployException if the connection is not valid or any other 
   * errors occur executing the request.
   * @throws IllegalArgumentException if the requestType is <code>null</code> or
   * not one of the supported types.
   */
   public PSCatalogResultSet catalog(String requestType, Properties props)
      throws PSServerException, PSAuthenticationFailedException,
         PSAuthorizationException, PSDeployException, IOException
   {
      if(requestType == null)
         throw new IllegalArgumentException("requestType may not be null.");
      
      if(!ms_supportedReqTypes.contains(requestType))
         throw new IllegalArgumentException(
            "requestType is not a valid request type.");
            
      Document reqDoc = formatRequest(requestType, props);
      Document respDoc;
      try 
      {
         respDoc = m_conn.execute(CATALOG_REQUEST_TYPE, reqDoc);
      }
      catch (PSServerLockException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      PSCatalogResultSet set = null;
      try {
         set = new PSCatalogResultSet(respDoc.getDocumentElement());      
      }
      catch(PSUnknownNodeTypeException e)
      {
         //we should not get here as this document is constructed by server
         //from catalog result set object.
         e.printStackTrace();
         Object[] args = {reqDoc.getDocumentElement().getTagName(), 
            respDoc.getDocumentElement().getTagName(), e.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }
      
      return set;
   }      
   
   /**
    * Creates the request document to the server from the supplied request type 
    * and properties.
    * 
    * @param requestType the catalog request type, assumed to be one of the 
    * supported types.
    * @param requestProps the additional properties to be set with the request,
    * may be <code>null</code> if the request does not need any additional 
    * parameters to fulfill the request.
    * 
    * @return the request document, never <code>null</code>
    */
   private Document formatRequest(String requestType, Properties requestProps)
   {
      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(   reqDoc,
         "PSXCatalog" + requestType);
         
      if(requestProps != null && !requestProps.isEmpty())
      {
         Iterator props = requestProps.entrySet().iterator();
         while(props.hasNext())
         {
            Map.Entry entry = (Map.Entry)props.next();
            PSXmlDocumentBuilder.addElement(   reqDoc, root,
               (String)entry.getKey(), (String)entry.getValue());            
         }
      }      
      return reqDoc;
   }
   
   /**
    * The prefix of the catalog request document root element.
    */
   public static final String ROOT_PREFIX = "PSXCatalog";

   /**
    * The constant to indicate catalog request.
    */   
   public static final String CATALOG_REQUEST_TYPE = "deploy-catalog";

   /**
    * The constant to represent 'Archives' request type.
    */
   public static final String TYPE_REQ_ARCHIVES = "Archives";
   
   /**
    * The constant to represent 'DBMS Drivers' request type.
    */
   public static final String TYPE_REQ_DRIVERS = "DBMSDrivers";   
   
   /**
    * The constant to represent 'DBMS Drivers' request type.
    */
   public static final String TYPE_REQ_DATASOURCES = "DataSources";   
   
   /**
    * The constant to represent 'Custom Element Types' request type.
    */
   public static final String TYPE_REQ_CUSTOM_TYPES = "CustomElementTypes";
   
   /**
    * The constant to represent 'Deployable Element Types' request type.
    */
   public static final String TYPE_REQ_DEPLOY_TYPES = "DeployableElementTypes";
   
   /**
    * The constant to represent 'Descriptors' request type.
    */
   public static final String TYPE_REQ_DESCRIPTORS = "Descriptors";
   
   /**
    * The constant to represent 'Literal ID Types' request type.
    */
   public static final String TYPE_REQ_LITERAL_ID_TYPES = "LiteralIDTypes";   
   
   /**
    * The constant to represent 'Objects by ID Type' request type.
    */
   public static final String TYPE_REQ_TYPE_OBJECTS = "IDTypeObjects";
   
   /**
    * The constant to represent 'Package Logs' request type.
    */
   public static final String TYPE_REQ_PACKAGELOGS = "PackageLogs";
   
   /**
    * The constant to represent 'User Dependencies' request type.
    */
   public static final String TYPE_REQ_USER_DEP = "UserDependencies";   

   public static List<String> ms_supportedReqTypes = new ArrayList<String>();
   static 
   {
      ms_supportedReqTypes.add(TYPE_REQ_ARCHIVES);
      ms_supportedReqTypes.add(TYPE_REQ_DATASOURCES);
      ms_supportedReqTypes.add(TYPE_REQ_CUSTOM_TYPES);            
      ms_supportedReqTypes.add(TYPE_REQ_DEPLOY_TYPES);
      ms_supportedReqTypes.add(TYPE_REQ_DESCRIPTORS);
      ms_supportedReqTypes.add(TYPE_REQ_LITERAL_ID_TYPES);      
      ms_supportedReqTypes.add(TYPE_REQ_TYPE_OBJECTS);            
      ms_supportedReqTypes.add(TYPE_REQ_PACKAGELOGS);                  
      ms_supportedReqTypes.add(TYPE_REQ_USER_DEP);
   }
   
   /**
    * The connection to deployment server that to be cataloged for requests. 
    * Initialized in the constructor and never modified after that.
    */
   private PSDeploymentServerConnection m_conn;
}

