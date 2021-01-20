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


package com.percussion.deployer.client;

import com.percussion.deployer.catalog.PSCataloger;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.error.PSDeployNonUniqueException;
import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSAppPolicySettings;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.deployer.objectstore.PSValidationResults;
import com.percussion.deployer.server.PSDeploymentHandler;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.server.PSServerLockException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.codec.PSXmlEncoder;
import com.percussion.utils.collections.PSMultiValueHashMap;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Client-side manager for all deployment requests to the Rhythmyx server.
 */
public class PSDeploymentManager
{

   /**
    * Creates a deployment manager using the supplied connection.
    *
    * @param conn The connection, may not be <code>null</code>, must be
    * connected.
    *
    * @throws IllegalArgumentException if <code>conn</code> is invalid.
    */
   public PSDeploymentManager(PSDeploymentServerConnection conn)
   {
      if (conn == null)
         throw new IllegalArgumentException("conn may not be null");

      if (!conn.isConnected())
         throw new IllegalArgumentException("conn must be connected.");

      m_conn = conn;
      m_cataloger = new PSCataloger(conn);
   }

   /**
    * Adds all child dependencies to the supplied dependency object.
    *
    * @param dependency The dependency to get child dependencies for.  They
    * are added to this object and are available after the method completes.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dependency</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void loadDependencies(PSDependency dependency)
      throws PSDeployException
   {
      if (dependency == null)
         throw new IllegalArgumentException("dependency may not be null");

      String reqType = getDeployReqType("loadDependencies");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployLoadDependenciesRequest");
         root.appendChild(dependency.toXml(reqDoc));
         
         String maxCount = getMaxDepCount();
         if (maxCount != null)
            root.setAttribute("maxCount", maxCount);
         
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSDependency respDep = getDependencyFromResultDoc(reqType, respDoc);
         dependency.setDependencies(respDep.getDependencies());
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Adds all parent dependencies to the supplied dependency object, replacing
    * any that were already loaded.
    *
    * @param dependency The dependency to get parent dependencies for.  They
    * are added to this object and are available after the method completes.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dependency</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void loadAncestors(PSDependency dependency) throws PSDeployException
   {
      if (dependency == null)
         throw new IllegalArgumentException("dependency may not be null");

      String reqType = getDeployReqType("loadAncestors");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployLoadAncestorsRequest");
         root.appendChild(dependency.toXml(reqDoc));
         
         String maxCount = getMaxDepCount();
         if (maxCount != null)
            root.setAttribute("maxCount", maxCount);
         
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSDependency respDep = getDependencyFromResultDoc(reqType, respDoc);
         dependency.setAncestors(respDep.getAncestors());
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Gets all deployable elements of the specified type from the server.
    *
    * @param type The type of element, may not be <code>null</code> or empty,
    * and must be one of the element types defined by the server implementation.
    * Valid types may be cataloged using the
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call
    * to {@link #getCataloger()}.  
    * <p>
    * For Custom types, supply the custom object 
    * type ({@link IPSDeployConstants#DEP_OBJECT_TYPE_CUSTOM}) concatenated with 
    * the supported local dependency type using a forward slash as a delimeter
    * (e.g. "Custom/Application").  For each instance of the child dependency
    * type that exists, a custom deployable element will be returned with the
    * child dependency as a local child.
    *
    * @return Iterator over zero or more PSDeployableElement objects, never
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> is <code>null</code>
    * or empty.
    * @throws PSDeployException if the <code>type</code> provided is invalid, if
    * there are any other errors.
    */
   public Iterator getDeployableElements(String type) throws PSDeployException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      String reqType = getDeployReqType("getDeployableElements");

      try
      {
         List results = new ArrayList();

         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployGetDeployableElementsRequest");
         root.setAttribute("type", type);
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element depEl = tree.getNextElement(PSDeployableElement.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         while (depEl != null)
         {
            results.add(new PSDeployableElement(depEl));
            depEl = tree.getNextElement(PSDeployableElement.XML_NODE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         return results.iterator();
      }
      catch (PSUnknownNodeTypeException e)
      {
         Object args[] = {reqType, PSDeployableElement.XML_NODE_NAME,
               e.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }
      catch (Exception e)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
      }
   }
   
   /**
    * Gets all existing dependencies of the specified type with the specified
    * parent id.
    * 
    * @param type The type to get, may not be <code>null</code> or empty.  Must 
    * support parent ids.
    * @param parentId Specifies the parent of all dependencies to return, may
    * not be <code>null</code> or empty.
    * 
    * @return an Iterator over zero or more PSDependency objects.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   public Iterator getDependencies(String type, String parentId) 
      throws PSDeployException
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      if (parentId == null || parentId.trim().length() == 0)
         throw new IllegalArgumentException(
            "parentId may not be null or empty");
      
      String reqType = getDeployReqType("getDependencies");

      try
      {
         List results = new ArrayList();

         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployGetDependenciesRequest");
         root.setAttribute("type", type);
         root.setAttribute("parentId", parentId);
         
         Document respDoc = m_conn.execute(reqType, reqDoc);
         
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element depEl = tree.getNextElement(
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         while (depEl != null)
         {
            results.add(getDependencyFromElement(reqType, depEl));
            depEl = tree.getNextElement(
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         return results.iterator();
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.toString());
      }
   }
   
   /**
    * Retrieve index of dependencies and elements to there owner
    * packages.
    * <code>
    * <pre>
    * The returned map contains the following properties:
    * <table border="1">
    * <tr>
    * <th>Name</th><th>Description</th>
    * </tr>
    * <tr><td>dependencyId</td><td>The dependency id</td></tr>
    * <tr><td>objectType</td><td>Object type of the dependency</td></tr>
    * <tr><td>project</td><td>The parent project name</td></tr>
    * <tr><td>version</td><td>The parent project version</td></tr>
    * </table>
    * </pre>
    * </code>
    * @return an Iterator over zero or more Map<String, String> objects.
    * @throws PSDeployException
    */
   public Iterator<Map<String, String>> getDependencyToPackageNameIndex()
            throws PSDeployException
   {

      String reqType = getDeployReqType("getDependencyToPackageNameIndex");

      try
      {
         List<Map<String, String>> results = new ArrayList<Map<String, String>>();
         // Create a dummy request doc as it is required.
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(reqDoc, "PSXDummy");

         Document respDoc = m_conn.execute(reqType, reqDoc);
         Element root = respDoc.getDocumentElement();
         NodeList nl = root
                  .getElementsByTagName("PSXDependencyToPackageNameEntry");
         int len = nl.getLength();
         for (int i = 0; i < len; i++)
         {
            Element current = (Element) nl.item(i);
            Map<String, String> entry = new HashMap<String, String>();
            entry.put("dependencyId", current.getAttribute("dependencyId"));
            entry.put("objectType", current.getAttribute("objectType"));
            entry.put("package", current.getAttribute("package"));
            entry.put("version", current.getAttribute("version"));
            results.add(entry);
         }
         return results.iterator();
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException) e;
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                     .toString());
      }
   }

   
   /**
    * Creates a descriptor guid.
    * 
    * @return the String representation of a guid's long value. Never
    *         <code>null</code> or empty.
    * @throws PSDeployException if any unexpected error occurs.
    */
   public String createDescriptorGuid()
      throws PSDeployException
   {
      String reqType = getDeployReqType("createDescriptorGuid");
      //Create a dummy request doc as it is required.
      Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(reqDoc, "PSXDummy");
      try
      {
         Document respDoc = m_conn.execute(reqType, reqDoc);
         return respDoc.getDocumentElement().getAttribute("longvalue");
         
      }
      catch(Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
                  e.toString());
      }
   }
   
   /**
    * Get a map of child and parent types for dependency types that support 
    * parent ids.
    * 
    * @return A Map where the key is the child type and the value is the parent 
    * type, both as non-<code>null</code>, non-empty <code>String</code> 
    * objects, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Map getParentTypes() throws PSDeployException
   {
      String reqType = getDeployReqType("getParentTypes");
      String searchEl = "entry";

      try
      {
         Map types = new HashMap();

         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployGetParentTypesRequest");
         
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element entryEl = tree.getNextElement(searchEl, 
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

         while (entryEl != null)
         {
            String childType = PSDeployComponentUtils.getRequiredAttribute(
               entryEl, "childType");
            String parentType = PSDeployComponentUtils.getRequiredAttribute(
               entryEl, "parentType");
            types.put(childType, parentType);
               
            entryEl = tree.getNextElement(searchEl,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         return types;
      }
      catch (PSUnknownNodeTypeException e)
      {
         Object args[] = {reqType, searchEl, e.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.toString());
      }
   }

   /**
    * Gets the connection used to construct this object.  If the connection
    * is disconnected, then any methods in this class executed against the
    * server will fail.
    *
    * @return The connection, never <code>null</code>.
    */
   public PSDeploymentServerConnection getConnection()
   {
      return m_conn;
   }

   /**
    * Gets the id map from the server for the specified source repository.
    *
    * @param source Identifies the source repository using the form
    * <driver>:<server>:<database>:<origin>.  May not be <code>null</code> or
    * empty.
    *
    * @return The map, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code> or empty.
    * @throws PSDeployException if there are any errors.
    */
   public PSIdMap getIdMap(String source) throws PSDeployException
   {
      if (source == null || source.trim().length() == 0)
         throw new IllegalArgumentException(
            "source may not be null or empty");

      PSIdMap map = (PSIdMap) getDeployComponent("getIdMap",
         "PSXDeployGetIdMapRequest", "sourceServer", source,
         PSIdMap.class, PSIdMap.XML_NODE_NAME);

      return map;
   }

   /**
    * Gets a Deployment Component (specified by the given parameters)
    * from the server.
    *
    * @param reqTypeName The request type, assume not <code>null</code> or empty
    * @param reqRootName The XML root name of the request, assume not
    * <code>null</code> or empty
    * @param attrName The attribute name of the request document, it may be
    * <code>null</code> if not need or no attribute. However, if it is not
    * <code>null</code>, then assume not empty.
    * @param attrValue The attribute value, which is the name of the specified
    * Deployment Component object, assume not <code>null</code> or empty if
    * <code>attrName</code> is not <code>null</code>.
    * @param compClass The <code>Class</code> of the deployment component,
    * assume not <code>null</code>.
    * @param xmlNodeName The XML node name of the specified object, assume not
    * <code>null</code> or empty.
    *
    * @return The retrieved <code>IPSDeployComponent</code> object, it never
    * <code>null</code>.
    *
    * @throws PSDeployException if there is any other error.
    */
   private IPSDeployComponent getDeployComponent(String reqTypeName,
      String reqRootName, String attrName, String attrValue, Class compClass,
      String xmlNodeName) throws PSDeployException
   {
      String reqType = getDeployReqType(reqTypeName);

      IPSDeployComponent comp = null;

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc, reqRootName);
         if ( attrName != null )
            root.setAttribute(attrName, attrValue);
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element childEl = tree.getNextElement(xmlNodeName,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (childEl == null)
         {
            Object args[] = {reqType, xmlNodeName};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
         }

         Constructor compCtor = compClass.getConstructor( new Class[]
            { Element.class });
         comp = (IPSDeployComponent) compCtor.newInstance(
            new Object[] {childEl} );
      }
      catch (Exception e)
      {
         if (e instanceof PSUnknownNodeTypeException)
         {
            Object args[] = {reqType, xmlNodeName, e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }
         else if (e instanceof PSDeployException)
         {
            throw (PSDeployException)e;
         }
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

      return comp;
   }
   
   /**
    * Gets the max dep count from the system property if available.
    * 
    * @return The count as a <code>String</code>, or <code>null</code> if
    * not found.
    */   
   private String getMaxDepCount()
   {
      return System.getProperty(IPSDeployConstants.PROP_MAX_DEPS);
   }

   /**
    * Get the dbms map from the server for the specified source Rx server name.
    *
    * @param source The name of the source server, may not be <code>null</code>
    * or empty.
    *
    * @return The map, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    * <code>null</code> or empty.
    * @throws PSDeployException if there are any errors.
    */
   public PSDbmsMap getDbmsMap(String source) throws PSDeployException
   {
      if (source == null || source.trim().length() == 0)
         throw new IllegalArgumentException(
            "source may not be null or empty");

      PSDbmsMap map = (PSDbmsMap) getDeployComponent("getDbmsMap",
         "PSXDeployGetDbmsMapRequest", "server", source,
         PSDbmsMap.class, PSDbmsMap.XML_NODE_NAME);

      return map;
   }

   /**
    * Gets an instance of the cataloger to use for catalog requests.
    *
    * @return The cataloger, never <code>null</code>, will contain a valid
    * connection.
    *
    * @throws IllegalStateException If the connection held by this manager has
    * been disconnected.
    */
   public PSCataloger getCataloger()
   {
      if (!m_conn.isConnected())
         throw new IllegalStateException("connection has been disconnected");

      return m_cataloger;
   }

   /**
    * Gets the specified export descriptor from the server.
    *
    * @param name The name of the descriptor, may not be <code>null</code> or
    * empty.  Must be the name of an existing descriptor on the server.  Valid
    * descriptor names may be cataloged using the
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call
    * to {@link #getCataloger()}.
    *
    * @return The descriptor, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>name</code> is <code>null</code>
    * or empty.
    * @throws PSDeployException If the descriptor cannot be located or there are
    * any other errors.
    */
   public PSExportDescriptor getExportDescriptor(String name)
      throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSExportDescriptor desc = (PSExportDescriptor) getDeployComponent(
         "getExportDescriptor", "PSXDeployGetExportDescriptorRequest",
         "descName", name, PSExportDescriptor.class,
         PSExportDescriptor.XML_NODE_NAME);

      return desc;
   }

   /**
    * Gets the export descriptor from the archive on the server referenced by
    * the supplied archive log id.  Any packages that have never been installed
    * will be removed, and listed in the package names returned by 
    * <code>PSExportDescriptor.getMissingPackages()</code>.
    *
    * @param archiveLogId The id of the archive log specifying the archive from
    * which the descriptor is to be retrieved.  Must be an existing archive log 
    * id.  Archive log ids can be cataloged from the server using the 
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call 
    * to {@link #getCataloger()}.
    *
    * @return The descriptor, never <code>null</code>.
    *
    * @throws PSDeployException If the descriptor cannot be located or there are
    * any other errors.
    */
   public PSExportDescriptor getExportDescriptor(int archiveLogId)
      throws PSDeployException
   {
      PSExportDescriptor desc = (PSExportDescriptor) getDeployComponent(
         "getExportDescriptor", "PSXDeployGetExportDescriptorRequest",
         "archiveLogId", String.valueOf(archiveLogId), PSExportDescriptor.class,
         PSExportDescriptor.XML_NODE_NAME);

      return desc;
   }

   /**
    * Deletes the specified export descriptor from the server.
    *
    * @param name The name of the descriptor, may not be <code>null</code> or
    * empty.  Must be the name of an existing descriptor on the server.  Valid
    * descriptor names may be cataloged using the
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call
    * to {@link #getCataloger()}.
    *
    * @throws IllegalArgumentException if <code>name</code> is <code>null</code>
    * or empty.
    * @throws PSDeployException If the descriptor cannot be located or there are
    * any other errors.
    */
   public void deleteExportDescriptor(String name) throws PSDeployException
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      sendSimpleRequst("deleteExportDescriptor",
         "PSXDeployDeleteExportDescriptorRequest", "descName", name);
   }

   /**
    * Sends a (simple) request to the server, the request is specified by
    * a set of parameters.
    *
    * @param reqTypeSuffix The suffix of the request type, assume not
    * <code>null</code> or empty.
    * @param reqNodeName The request XML node name, assume not
    * <code>null</code> or empty.
    * @param attrName The attribute name of the request XML node, assume not
    * <code>null</code> or empty.
    * @param attrValue The attribute value of the <code>attrName</code>,
    * assume not <code>null</code> or empty.
    *
    * @return The responsed XML document, never <code>null</code>.
    *
    * @throws PSDeployException if an error occures.
    */
   private Document sendSimpleRequst(String reqTypeSuffix, String reqNodeName,
      String attrName, String attrValue) throws PSDeployException
   {
      String reqType = getDeployReqType(reqTypeSuffix);
      Document respDoc = null;

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc, reqNodeName);
         root.setAttribute(attrName, attrValue);
         respDoc = m_conn.execute(reqType, reqDoc);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }
      return respDoc;
   }

   /**
    * Saves the supplied export descriptor to the server.  If a descriptor with
    * the same already exists, it will be overwritten.  To determine the names
    * already in use, descriptor names may be cataloged using the
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call
    * to {@link #getCataloger()}.
    *
    * @param descriptor The descriptor to save, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>descriptor</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveExportDescriptor(PSExportDescriptor descriptor)
      throws PSDeployException
   {
      if (descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null");

      saveDeployComponent(descriptor, "saveExportDescriptor",
         "PSXDeploySaveExportDescriptorRequest");
   }

   /**
    * Get a list of id type objects from the server for the specified
    * dependencies.
    *
    * @param deps An iterator over one or more <code>PSDeployableObject</code>s.
    * If <code>null</code>, idTypes for all dependencies on the server that
    * support id types are returned.
    *
    * @return An Iterator over one or more <code>PSApplicationIDTypes</code>
    * objects.  Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException If <code>deps</code> is invalid.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getIdTypes(Iterator deps) throws PSDeployException
   {
      String reqType = getDeployReqType("getIdTypes");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployGetIdTypesRequest");
         if (deps != null)
         {
            while (deps.hasNext())
            {
               Object o = deps.next();
               if (!(o instanceof PSDeployableObject))
                  throw new IllegalArgumentException(
                     "deps may only contain PSDeployableObjects");
               PSDeployableObject dep = (PSDeployableObject)o;
               root.appendChild(dep.toXml(reqDoc));
            }
         }

         Document respDoc = m_conn.execute(reqType, reqDoc);

         List retList = new ArrayList();
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element typeEl = tree.getNextElement(
            PSApplicationIDTypes.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         while (typeEl != null)
         {
            retList.add(new PSApplicationIDTypes(typeEl));

            typeEl = tree.getNextElement(PSApplicationIDTypes.XML_NODE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
         }

         return retList.iterator();
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Saves the supplied appliation id types to the server, replacing the id
    * types on the server for those appliations if they already exist.
    *
    * @param idTypes An iterator over one or more
    * <code>PSApplicationIDTypes</code> objects.  May not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException If <code>idTypes</code> is
    * <code>null</code> or empty.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveIdTypes(Iterator idTypes) throws PSDeployException
   {
      if (idTypes == null || (!idTypes.hasNext()))
         throw new IllegalArgumentException("idTypes may not be null or empty");

      String reqType = getDeployReqType("saveIdTypes");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeploySaveIdTypesRequest");
         while (idTypes.hasNext())
         {
            Object o = idTypes.next();
            if (!(o instanceof PSApplicationIDTypes))
               throw new IllegalArgumentException(
                  "deps may only contain PSApplicationIDTypes");
            PSApplicationIDTypes idType = (PSApplicationIDTypes)o;
            root.appendChild(idType.toXml(reqDoc));
         }

         m_conn.execute(reqType, reqDoc);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }
   }

   /**
    * Saves the supplied user dependency to the server
    *
    * @param dep The user dependency.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>dep</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveUserDependency(PSUserDependency dep) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      String reqType = getDeployReqType("saveUserDependency");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeploySaveUserDependencyRequest");
         root.appendChild(dep.toXml(reqDoc));

         m_conn.execute(reqType, reqDoc);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Deletes the supplied user dependency from the server
    *
    * @param dep The user dependency.  May not be <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public void deleteUserDependency(PSUserDependency dep) 
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      String reqType = getDeployReqType("deleteUserDependency");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployDeleteUserDependencyRequest");
         root.appendChild(dep.toXml(reqDoc));

         m_conn.execute(reqType, reqDoc);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }
   
   /**
    * Saves the provided ID map to the server.  All entries on the server with
    * same source repository as those in the provided map will be deleted and
    * replaced with those in the provided map.
    *
    * @param map The full set of mappings for a particular source repository.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>map</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public void saveIdMap(PSIdMap map) throws PSDeployException
   {
      if (map == null)
         throw new IllegalArgumentException("map may not be null");

      saveDeployComponent(map, "saveIdMap", "PSXSaveIdMapRequest");
   }

   /**
    * Saves the provided <code>IPSDeployComponent</code> object to the server.
    *
    * @param component The to be saved object, assume it is not
    * <code>null</code>.
    * @param reqTypeName The reqeust type, assume not <code>null</code> or empty
    * @param reqRootName The XML root name of the request, assume not
    * <code>null</code> or empty.
    *
    * @throws PSDeployException if there is any error.
    */
   private void saveDeployComponent(IPSDeployComponent component,
      String reqTypeName, String reqRootName)  throws PSDeployException
   {
      String reqType = getDeployReqType(reqTypeName);

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc, reqRootName);
         root.appendChild(component.toXml(reqDoc));
         m_conn.execute(reqType, reqDoc);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }
   }
   
   /**
    * Validate local config file specified against the localConfig.xsd.
    * @param localConfig
    * @return If valid returns <code>null</code>, else returns a list of
    * validation error strings.
    * @throws PSDeployException if any error occurs.
    */
   public List<String> validateLocalConfigFile(File localConfig) throws PSDeployException
   {
      
      if(localConfig == null || !localConfig.exists() || !localConfig.isFile())
         throw new IllegalArgumentException(
            "localConfig file cannot be null and must exist on file system.");
      String reqType = getDeployReqType("validateLocalConfig");
      try
      {
         String xmlContent = 
            com.percussion.util.IOTools.getFileContent(localConfig);
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = 
            PSXmlDocumentBuilder.createRoot(reqDoc, "PSXValidateLocalConfigRequest");
         Element content = reqDoc.createElement("xmlContent");
         PSXmlEncoder encoder = new PSXmlEncoder();
         Text data = reqDoc.createTextNode((String)encoder.encode(xmlContent));
         content.appendChild(data);
         root.appendChild(content);
         Document respDoc = m_conn.execute(reqType, reqDoc);
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element el = null;
         List<String> errors = new ArrayList<String>();
         while((el = tree.getNextElement("error")) != null)
         {
            errors.add(PSXmlTreeWalker.getElementData(el));
         }
         if(errors.isEmpty())
            return null;
         return errors;
      }
      catch(Exception e)
      {
         throw new PSDeployException(
                  IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
      }      
      
   }

   /**
    * Checks the server version and build compatiblity, and archive name for
    * uniqueness.  If there is an error (versions are not compatible, archive
    * name not unique), an exception is thrown.  If there is a warning (builds
    * are not compatible), a message is returned.  If there are no errors or
    * warnings, <code>null</code> is returned.
    *
    * @param archiveInfo The archive to validate, may not be <code>null</code>.
    * @param checkArchiveRef if <code>true</code>, the archive ref will be
    * checked against existing archives on the server to see if an existing
    * archive will be overwritten, otherwise the archive ref is not checked.
    *
    * @return PSMultiValueHashMap with all errors and warnings. 
    * Keys used:
    * <code>IPSDeployConstants.WARNING_KEY</code>
    * <code>IPSDeployConstants.ERROR_KEY</code>
    * value will have error message.
    *
    * @throws IllegalArgumentException if <code>archive</code> is
    * <code>null</code>.
    * @throws PSDeployNonUniqueException if the archive already exists.
    * @throws PSDeployException if there are any errors.
    */
   public PSMultiValueHashMap<String, String> validateArchive(PSArchiveInfo archiveInfo,
      boolean checkArchiveRef) throws PSDeployException
   {
      if (archiveInfo == null)
         throw new IllegalArgumentException("archiveInfo may not be null");

      String reqType = getDeployReqType("validateArchive");
      
      PSMultiValueHashMap<String, String> validationMap = 
         new PSMultiValueHashMap<String, String>();

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployValidateArchiveRequest");
         root.setAttribute("checkArchiveRef", checkArchiveRef ? "yes" : "no");
         root.appendChild(archiveInfo.toXml(reqDoc));

         String allowBuildMismatch = System.getProperty(
            IPSDeployConstants.PROP_ALLOW_BUILD_MISMATCH);
         // property defined is all that matters
         allowBuildMismatch = allowBuildMismatch != null ? "yes" : "no";
         root.setAttribute("warnOnBuidMismatch", allowBuildMismatch);
         
         //check for missing package dep flag
         String allowMissingPackageDep = System.getProperty(
               IPSDeployConstants.PROP_ALLOW_MISSING_PACKAGE_DEP);
         // property defined is all that matters
         allowMissingPackageDep = allowMissingPackageDep != null ? "yes" : "no";
            root.setAttribute("warnMissingPackageDep", allowMissingPackageDep);         
         
         Document respDoc = m_conn.execute(reqType, reqDoc);
         
         //Create error list from XML doc
         PSXmlTreeWalker valTree = 
            new PSXmlTreeWalker(respDoc.getDocumentElement());

         
         Element valElement = 
            valTree.getNextElement(PSDeploymentHandler.XML_AV_EL_NAME);

         while( valElement != null )
         {

            validationMap.put(
                  valElement.getAttribute(PSDeploymentHandler.ERROR_LEVEL),
                  valElement.getAttribute(PSDeploymentHandler.ERROR_MESSAGE));       
            valElement = 
               valTree.getNextElement(PSDeploymentHandler.XML_AV_EL_NAME);
         }

      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }


      return validationMap;
   }

   /**
    * Gets the specified archive summary from the server.
    *
    * @param archiveLogId The id of the archive log to retrieve.  Must be an
    * existing archive log id.  Archive log ids can be cataloged from the
    * server using the {@link com.percussion.deployer.catalog.PSCataloger} class
    * returned by a call to {@link #getCataloger()}.
    *
    * @return The summary, never <code>null</code>.
    *
    * @throws PSDeployException if the archive log cannot be found or there are
    * any other errors.
    */
   public PSArchiveSummary getArchiveSummary(int archiveLogId)
      throws PSDeployException
   {
      PSArchiveSummary archiveSummary = (PSArchiveSummary) getDeployComponent(
         "getArchiveSummary", "PSXDeployGetArchiveSummaryRequest",
         "archiveLogId", Integer.toString(archiveLogId),
         PSArchiveSummary.class,
         PSArchiveSummary.XML_NODE_NAME);

      return archiveSummary;
   }

   /**
    * Get the most recent archive summary for the specified archive ref from the
    * server.
    * 
    * @param archiveRef The name used to identify the archive for which the most
    * recent log will be retrieved.  It may not be <code>null</code> or empty, 
    * and must refer to an existing archive file on the server.  
    * 
    * @return The retrieved archive summary object. It will never be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>archiveRef</code> is 
    * <code>null</code> or empty.
    * @throws PSDeployException if an archive log cannot be found for the 
    * specified archive ref, or if any other errors occur.
    */
   public PSArchiveSummary getArchiveSummary(String archiveRef) 
      throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");
      
      PSArchiveSummary archiveSummary = (PSArchiveSummary) getDeployComponent(
         "getArchiveSummary", "PSXDeployGetArchiveSummaryRequest",
         "archiveRef", archiveRef,
         PSArchiveSummary.class,
         PSArchiveSummary.XML_NODE_NAME);

      return archiveSummary;
   }   

   /**
    * Gets the specified package log summary from the server.
    *
    * @param logId The id of the package log to retrieve.  Must be an
    * existing package log id.  Package log ids can be cataloged from the
    * server using the {@link com.percussion.deployer.catalog.PSCataloger} class
    * returned by a call to {@link #getCataloger()}.
    *
    * @return The summary, never <code>null</code>.  It is guaranteed that
    * {@link PSLogSummary#getLogDetail()} will not return <code>null</code> for
    * this instance.
    *
    * @throws PSDeployException if the package log cannot be found or there are
    * any other errors.
    */
   public PSLogSummary getLogSummary(int logId) throws PSDeployException
   {
      PSLogSummary logSummary = (PSLogSummary) getDeployComponent(
         "getLogSummary", "PSXDeployGetLogSummaryRequest",
         "logId", Integer.toString(logId),
         PSLogSummary.class,
         PSLogSummary.XML_NODE_NAME);

      return logSummary;
   }

   /**
    * Deletes the specified Archive file and all related archive and package
    * logs from the server.
    *
    * @param archiveLogId The id of an archive log related to the archive file
    * to delete.  Must be an existing archive log id.  Archive log ids can be
    * cataloged from the server using the
    * {@link com.percussion.deployer.catalog.PSCataloger} class returned by a call
    * to {@link #getCataloger()}.
    *
    * @throws PSDeployException if the archive log cannot be found or there are
    * any other errors.
    */
   public void deleteArchive(int archiveLogId) throws PSDeployException
   {
      sendSimpleRequst("deleteArchive",
         "PSXDeployDeleteArchiveRequest", "archiveLogId",
         Integer.toString(archiveLogId));
   }

   /**
    * Deletes the specified Archive file and all related archive and package
    * logs from the server.
    *
    * @param archiveRef The name of an archive file on the server. May not be
    * <code>null</code> or empty
    *
    * @throws IllegalArgumentException if <code>archiveRef</code> is 
    * <code>null</code> or empty.
    * @throws PSDeployException if there are any errors.
    */
   public void deleteArchive(String archiveRef) throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");
      
      sendSimpleRequst("deleteArchive", "PSXDeployDeleteArchiveRequest", 
         "archiveRef", archiveRef);
   }
   
   /**
    * Gets the archive info for the archive on the server specified by the
    * supplied archiveRef.  The returned object will include a
    * <code>PSArchiveDetail</code> object.
    *
    * @param archiveLogId The log id of the archive to retrieve info for.
    * Must be an existing archive log id.  Archive log ids can be cataloged from
    * the server using the {@link com.percussion.deployer.catalog.PSCataloger}
    * class returned by a call to {@link #getCataloger()}.
    *
    * @return The archive info, never <code>null</code>, will include a
    * <code>PSArchiveDetail</code> object.
    *
    * @throws PSDeployException if the archive log cannot be found or there are
    * any other errors.
    */
   public PSArchiveInfo getArchiveInfo(int archiveLogId)
      throws PSDeployException
   {
      PSArchiveInfo archiveInfo = (PSArchiveInfo) getDeployComponent(
         "getArchiveInfo", "PSXDeployGetArchiveInfoRequest",
         "archiveLogId", Integer.toString(archiveLogId),
         PSArchiveInfo.class,
         PSArchiveInfo.XML_NODE_NAME);

      return archiveInfo;
   }

   /**
    * Gets the application policy settings from the server.
    *
    * @return The app policy settings, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public PSAppPolicySettings getAppPolicySettings() throws PSDeployException
   {
      PSAppPolicySettings settings = (PSAppPolicySettings) getDeployComponent(
         "getAppPolicySettings", "PSXDeployGetAppPolicySettingsRequest", null,
         null, PSAppPolicySettings.class, PSAppPolicySettings.XML_NODE_NAME);

      return settings;
   }

   /**
    * Saves the application policy settings to the server.
    *
    * @param settings The settings to save, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>settings</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void saveAppPolicySettings(PSAppPolicySettings settings)
      throws PSDeployException
   {
      if (settings == null )
         throw new IllegalArgumentException("settings may not be null");

      saveDeployComponent(settings, "saveAppPolicySettings",
         "PSXDeploySaveAppPolicySettingsRequest");
   }

   /**
    * Saves the dbms map to the server.  Contents will replace the current
    * mapppings for the specified source server.
    *
    * @param map The map containing the mappings to save.  May contain both new
    * and existing mappings.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>settings</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public void saveDbmsMap(PSDbmsMap map) throws PSDeployException
   {
      if (map == null )
         throw new IllegalArgumentException("map may not be null");

      saveDeployComponent(map, "saveDbmsMap", "PSXSaveDbmsMapRequest");
   }

   /**
    * Starts copying the supplied file to the server in a separate thread,
    * returning a job controller used to track status and optionally cancel the
    * copy.
    *
    * @param archiveRef The name to use to identify the archive on the server.
    * May not be <code>null</code> or empty.
    * @param archiveFile The archive file to copy to the server.  May not be
    * <code>null</code> and must be an existing, valid, deployment archive
    * file.
    * 
    * @return The job control handle, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors initiating the copy.
    */
   public IPSDeployJobControl copyArchiveToServer(String archiveRef,
      final File archiveFile) throws PSDeployException
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      if (archiveFile == null || !archiveFile.exists())
         throw new IllegalArgumentException(
            "archiveFile may not be null and must exist");

      final String reqType = getDeployReqType("saveArchiveFile");

      final Map params = new HashMap();
      params.put("archiveRef", archiveRef);

      // create job controller
      final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

      // Create a thread to execute the request
      Thread thread = new Thread()
      {
         public void run()
         {
            try
            {
               m_conn.execute(reqType, params, archiveFile, ctl);
               ctl.setCompleted();
            }
            catch (Exception e)
            {
               ctl.setErrorMessage(e.getLocalizedMessage());
            }
            catch (Throwable t)
            {
               // record OutOfMemoryError on job control
               t.printStackTrace();
               ctl.setErrorMessage(t.toString());
            }
         }
      };

      thread.start();

      return ctl;
   }
   
   /**
    * Starts copying the supplied config file to the server in a separate thread,
    * returning a job controller used to track status and optionally cancel the
    * copy.
    *
    * @param configRef The name to use to identify the config file on the server.
    * May not be <code>null</code> or empty.
    * @param configFile The config file to copy to the server.  May not be
    * <code>null</code> and must be an existing, valid, config
    * file.
    * 
    * @return The job control handle, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors initiating the copy.
    */
   public IPSDeployJobControl copyConfigToServer(String configRef,
      final File configFile) throws PSDeployException
   {
      if (configRef == null || configRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      if (configFile == null || !configFile.exists())
         throw new IllegalArgumentException(
            "configFile may not be null and must exist");

      final String reqType = getDeployReqType("saveConfigFile");

      final Map params = new HashMap();
      params.put("configRef", configRef);
      params.put(IPSHtmlParameters.REQ_XML_DOC_FLAG,
         IPSHtmlParameters.XML_DOC_AS_TEXT);

      // create job controller
      final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

      // Create a thread to execute the request
      Thread thread = new Thread()
      {
         public void run()
         {
            try
            {
               m_conn.execute(reqType, params, configFile, ctl);
               ctl.setCompleted();
            }
            catch (Exception e)
            {
               ctl.setErrorMessage(e.getLocalizedMessage());
            }
            catch (Throwable t)
            {
               // record OutOfMemoryError on job control
               t.printStackTrace();
               ctl.setErrorMessage(t.toString());
            }
         }
      };

      thread.start();

      return ctl;
   }
   
   /**
    * Creates a configdef shell file from the specified export descriptor and
    * copies it to the specified target file.
    * @param descName The name of the export descriptor to create a config def
    * from.  May not be <code>null</code> or empty.  
    * @param targetFile The file on the local file system to which the configDef
    * is written. May not be <code>null</code>.
    * @return The job control handle, never <code>null</code>.
    * @throws PSDeployException
    */
   public IPSDeployJobControl createConfigDef(String descName,
            final File targetFile) throws PSDeployException
   {
      if (StringUtils.isBlank(descName))
         throw new IllegalArgumentException("descName may not be null or empty");

      if (targetFile == null)
         throw new IllegalArgumentException("targetFile may not be null");

      final String reqType = getDeployReqType("createConfigDef");

      FileOutputStream out = null;
      try
      {
         final Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
                  "PSXDeployCreateConfigDefRequest");
         root.setAttribute("descName", descName);
         
         out = new FileOutputStream(targetFile);
         final FileOutputStream fOut = out;

         // create job controller
         final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

         // Create a thread to execute the request
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  m_conn.execute(reqType, reqDoc, fOut, ctl);
                  ctl.setCompleted();
               }
               catch (Exception e)
               {
                  ctl.setErrorMessage(e.getLocalizedMessage());
               }
            }
         };

         thread.start();
         return ctl;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException) e;
         else
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                     .toString());
         }
      }
   }
   
   /**
    * Creates a local config and copies it to the specified target file.
    * 
    * @param descName The name of the export descriptor to create a config def
    * from.  May not be <code>null</code> or empty.  
    * @param targetFile The file on the local file system to which the configDef
    * is written. May not be <code>null</code>.
    * @param configDef The file on the local file system to which the default 
    * config will be based on. May not be <code>null</code>.
    * @return The job control handle, never <code>null</code>.
    * @throws PSDeployException
    */
   public IPSDeployJobControl createDefaultConfig(String descName,
            final File targetFile, final File configDef) throws PSDeployException
   {
      if (StringUtils.isBlank(descName))
         throw new IllegalArgumentException("descName may not be null or empty");

      if (targetFile == null)
         throw new IllegalArgumentException("targetFile may not be null");

      final String reqType = getDeployReqType("createDefaultConfig");

      FileOutputStream out = null;
      try
      {
         final Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
                  "PSXDeployCreateDefaultConfigRequest");

         copyConfigToServer(descName  + "_configDef", configDef);
         
         root.setAttribute("descName", descName);
         
         out = new FileOutputStream(targetFile);
         final FileOutputStream fOut = out;

         // create job controller
         final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

         // Create a thread to execute the request
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  m_conn.execute(reqType, reqDoc, fOut, ctl);
                  ctl.setCompleted();
               }
               catch (Exception e)
               {
                  ctl.setErrorMessage(e.getLocalizedMessage());
               }
            }
         };

         thread.start();
         return ctl;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException) e;
         else
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                     .toString());
         }
      }
   }
   
   /*
    * To convert the InputStream to String we use the
    * BufferedReader.readLine() method. We iterate until the BufferedReader
    * return null which means there's no more data to read. Each line will
    * appended to a StringBuilder and returned as String.
    */
   public String convertStreamToString(InputStream is)
   {

      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuilder sb = new StringBuilder();

      String line = null;
      try
      {
         while ((line = reader.readLine()) != null)
         {
            sb.append(line + "\n");
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      }

      return sb.toString();
   }
   
   /**
    * Creates a summary from the specified export descriptor and
    * copies it to the specified target file.
    * @param descName The name of the export descriptor to create a summary
    * from.  May not be <code>null</code> or empty.  
    * @param targetFile The file on the local file system to which the summary
    * is written. May not be <code>null</code>.
    * @return The job control handle, never <code>null</code>.
    * @throws PSDeployException
    */
   public IPSDeployJobControl createDescriptorSummary(String descName,
            final File targetFile) throws PSDeployException
   {
      if (StringUtils.isBlank(descName))
         throw new IllegalArgumentException("descName may not be null or empty");

      if (targetFile == null)
         throw new IllegalArgumentException("targetFile may not be null");

      final String reqType = getDeployReqType("createDescriptorSummary");

      FileOutputStream out = null;
      try
      {
         final Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
                  "PSXDeployCreateDescriptorSummaryRequest");
         root.setAttribute("descName", descName);

         out = new FileOutputStream(targetFile);
         final FileOutputStream fOut = out;

         // create job controller
         final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

         // Create a thread to execute the request
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  m_conn.execute(reqType, reqDoc, fOut, ctl);
                  ctl.setCompleted();
               }
               catch (Exception e)
               {
                  ctl.setErrorMessage(e.getLocalizedMessage());
               }
            }
         };

         thread.start();
         return ctl;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException) e;
         else
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                     .toString());
         }
      }
   }

   /**
    * Copies the specified archive from the server in a separate thread,
    * returning a job controller to track the status and optionally cancel the
    * copy.  The archive must have been created using the specified export
    * descriptor to run an export job to completion.  If the server has been
    * restarted since the export job was run, the archive will no longer be
    * available.  If the copy is cancelled or aborts due to an error, an invalid
    * file may have been written to the <code>targetFile</code>, and must be
    * deleted by the caller.
    *
    * @param descName The name of the export descriptor that was used to create
    * the archive.  May not be <code>null</code> or empty.  Will retrieve the
    * archive the was most recently created using this descriptor.
    * @param targetFile The file on the local file system to which the archive
    * is written. May not be <code>null</code>.
    * 
    * @return The job control handle, never <code>null</code>.
    *
    * @throws PSDeployException if there is no archive on the server that was
    * created using the specified descriptor, or if there are any errors
    * initiating the copy.
    */
   public IPSDeployJobControl copyArchiveFromServer(String descName,
      File targetFile) throws PSDeployException
   {
      if (descName == null || descName.trim().length() == 0)
         throw new IllegalArgumentException(
            "descName may not be null or empty");

      if (targetFile == null)
         throw new IllegalArgumentException("targetFile may not be null");

      final String reqType = getDeployReqType("getArchiveFile");

      FileOutputStream out = null;
      try
      {
         final Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXDeployGetArchiveFileRequest");
         root.setAttribute("descName", descName);

         out = new FileOutputStream(targetFile);
         final FileOutputStream fOut = out;

         // create job controller
         final PSDeployFileJobControl ctl = new PSDeployFileJobControl();

         // Create a thread to execute the request
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  m_conn.execute(reqType, reqDoc, fOut, ctl);
                  ctl.setCompleted();
               }
               catch (Exception e)
               {
                  ctl.setErrorMessage(e.getLocalizedMessage());
               }
            }
         };

         thread.start();
         return ctl;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Runs an export job asynchronously on the server using the supplied
    * descriptor, returning a job controller to track the status and optionally
    * cancel the job.
    *
    * @param desc The export descriptor defining the job, may not be
    * <code>null</code>.
    *
    * @return The job control handle, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors initiating the job.
    */
   public IPSDeployJobControl runExportJob(PSExportDescriptor desc)
      throws PSDeployException
   {
      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      try 
      {
         return runServerJob(desc, "export");
      }
      catch (PSServerLockException e) 
      {
         // this is not expected, but helper method throws it
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   /**
    * Gets the featureset from the server to support backward compatibility.
    * 
    * @return The featureset, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors retrieving the 
    * featureset.
    */
   public PSFeatureSet getFeatureSet() throws PSDeployException
   {
      String reqType = getDeployReqType("getFeatureSet");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.createRoot(reqDoc, 
            "PSXDeployGetFeatureSetRequest");
         Document respDoc = m_conn.execute(reqType, reqDoc);
         
         PSXmlTreeWalker tree = new PSXmlTreeWalker(respDoc);
         Element childEl = tree.getNextElement(PSFeatureSet.ms_nodeName,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (childEl == null)
         {
            Object args[] = {reqType, PSFeatureSet.ms_nodeName};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
         }
         
         PSXmlDocumentBuilder.replaceRoot(respDoc, childEl);
         PSFeatureSet fs = new PSFeatureSet();
         fs.fromXml(respDoc);
         return fs;
      }
      catch (Exception e)
      {
         if (e instanceof PSUnknownNodeTypeException || e instanceof 
            PSUnknownDocTypeException)
         {
            Object args[] = {reqType, "PSXDeployGetFeatureSetRequest", 
               e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }
         else if (e instanceof PSDeployException)
         {
            throw (PSDeployException)e;
         }
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }
   }
   
   /**
    * Runs an import job asynchronously on the server using the supplied
    * descriptor, returning a job controller to track the status and optionally
    * cancel the job. Note, use system property
    * <code>sys_pkgVisibleToAllCommunities</code> to control whether the
    * server will apply all existing communities to the imported package after
    * finished importing the package.
    * <p>
    * For example, <code>-Dsys_pkgVisibleToAllCommunities=true</code> java
    * option will cause server to apply all existing communities to the imported
    * package after finished importing the package.
    * 
    * @param desc The import descriptor defining the job, may not be
    * <code>null</code>.
    * 
    * @return The job control handle, never <code>null</code>.
    * 
    * @throws PSServerLockException if the publisher is locked.
    * @throws PSDeployException if there are any errors initiating the job.
    */
   public IPSDeployJobControl runImportJob(PSImportDescriptor desc)
      throws PSServerLockException, PSDeployException
   {
      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      return runServerJob(desc, "import");
   }

   /**
    * Runs an validation job asynchronously on the server using the supplied
    * descriptor, returning a job controller to track the status and optionally
    * cancel the job.  Results may be obtained by calling 
    * {@link #loadValidationResults(PSImportDescriptor)}
    *
    * @param desc The import descriptor to validate, may not be
    * <code>null</code>.
    *
    * @return The job control handle, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors initiating the job.
    */
   public IPSDeployJobControl runValidationJob(PSImportDescriptor desc)
      throws PSDeployException
   {
      if (desc == null)
         throw new IllegalArgumentException("desc may not be null");

      try 
      {
         return runServerJob(desc, "validation");
      }
      catch (PSServerLockException e) 
      {
         // this is not expected, but helper method throws it
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Loads the most recent validation results for the supplied descriptor.
    * 
    * @param desc The import descriptor to validate, may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>desc</code> is
    * <code>null</code>.
    * @throws PSDeployException if there are any errors obtaining the results.
    */
   public void loadValidationResults(PSImportDescriptor desc) 
      throws PSDeployException
   {
      // get the descriptor from the server containing the results
      PSImportDescriptor resultDesc = (PSImportDescriptor) getDeployComponent(
         "getValidationResults", "PSXDeployGetValidationResultsRequest",
         "archiveRef", desc.getArchiveInfo().getArchiveRef(), 
         PSImportDescriptor.class, PSImportDescriptor.XML_NODE_NAME);
      
      // create map of package names and validation results
      Map resultMap = new HashMap();
      Iterator pkgs = resultDesc.getImportPackageList().iterator();
      while (pkgs.hasNext())
      {
         PSImportPackage pkg = (PSImportPackage)pkgs.next();
         resultMap.put(pkg.getPackage().getKey(), pkg.getValidationResults());
      }
      
      // now walk supplied package list and set results on each
      pkgs = desc.getImportPackageList().iterator();
      while (pkgs.hasNext())
      {
         PSImportPackage pkg = (PSImportPackage)pkgs.next();
         PSValidationResults results = (PSValidationResults)resultMap.get(
            pkg.getPackage().getKey());
         if (results == null)
         {
            // this would be a bug, just throw unexpected exeption
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               "Missing validation results for pkg: " + 
               pkg.getPackage().getKey());
         }
         pkg.setValidationResults(results);
      }
   }

   /**
    * This method is used to get the string resources used for messages.
    *
    * @return the bundle, never <code>null</code>.
    * 
    * @throws java.util.MissingResourceException if the bundle cannot be loaded.
    */
   public static ResourceBundle getBundle()
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(
            "com.percussion.deployer.client.PSDeployStringResources");
      }

      return ms_bundle;
   }
   
   
   
   /**
    * Get the current job status for the specified job id.
    *
    * @param jobId The id of the job.
    * @param messageBuffer Buffer to use to return a the current status message.
    * Contents are appended with the status message.  May not be
    * <code>null</code>.
    *
    * @return The status,  a value between <code>1-100</code> to indicate the
    * % done.  <code>100</code> indicates that the job has completed. A result
    * of -1 indicates that there has been an error and the job completed
    * abnormally. In the case of an error the <code>messageBuffer</code> will
    * contain the error message.
    *
    * @throws IllegalArgumentException if <code>messageBuffer</code> is
    * <code>null</code>.
    * @throws PSDeployException if there is an error retrieving the status.
    */
   int getJobStatus(int jobId, StringBuffer messageBuffer)
      throws PSDeployException
   {
      if (messageBuffer == null)
         throw new IllegalArgumentException("messageBuffer may not be null");

      String reqType = getJobReqType("getJobStatus");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element reqRoot = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXJobGetStatus");
         reqRoot.setAttribute("id", String.valueOf(jobId));

         Document respDoc = m_conn.execute(reqType, reqDoc);
         Element respRoot = respDoc.getDocumentElement();

         int status;
         String statusAttr = "status";
         String strStatus = respRoot.getAttribute(statusAttr);
         try
         {
            status = Integer.parseInt(strStatus);
         }
         catch (NumberFormatException e)
         {
            Object[] msgArgs = {respRoot.getTagName(), statusAttr, strStatus};
            PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

            Object args[] = {reqType, respRoot.getTagName(),
                  une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }

         String message = respRoot.getAttribute("message");
         if (message != null)
            messageBuffer.append(message);

         return status;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }



   }

   /**
    * Attempts to stop the currently running job on the server.  Since the job
    * is running in its own thread, it may complete on its own before noticing
    * that it has been requested to stop.  This method may take some time to
    * return as it will wait for the job to respond to the cancel request
    * (or finish).
    *
    * @param jobId The id of the job.
    *
    * @return a code indicating whether or not the job was cancelled.  See
    * {@link IPSDeployJobControl#cancelDeployJob} for info on return codes.
    * 
    * @throws PSDeployException If there are any errors.
    */
   int cancelJob(int jobId) throws PSDeployException
   {
      String reqType = getJobReqType("cancelJob");

      try
      {
         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element reqRoot = PSXmlDocumentBuilder.createRoot(reqDoc,
            "PSXJobCancel");
         reqRoot.setAttribute("id", String.valueOf(jobId));

         Document respDoc = m_conn.execute(reqType, reqDoc);
         Element respRoot = respDoc.getDocumentElement();

         String resultAttr = "resultCode";
         String strResult = respRoot.getAttribute(resultAttr);
         int result;
         try
         {
            result = Integer.parseInt(strResult);
         }
         catch (NumberFormatException e)
         {
            Object[] msgArgs = {respRoot.getTagName(), resultAttr, strResult};
            PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

            Object args[] = {reqType, respRoot.getTagName(),
                  une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }

         return result;
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }

   }

   /**
    * Prepends the deploy request prefix to the supplied type.
    *
    * @param subType The sub-request type, assumed not <code>null</code> or
    * empty.
    *
    * @return The full request type string, not <code>null</code> or empty.
    */
   private String getDeployReqType(String subType)
   {
      return PSDeploymentServerConnection.DEPLOY_REQUEST + subType;
   }

   /**
    * Prepends the job request prefix to the supplied type.
    *
    * @param subType The sub-request type, assumed not <code>null</code> or
    * empty.
    *
    * @return The full request type string, not <code>null</code> or empty.
    */
   private String getJobReqType(String subType)
   {
      return PSDeploymentServerConnection.JOB_REQUEST + subType;
   }


   /**
    * Checks the root of the supplied doc for a child element that is the root
    * element of one of the <code>PSDependency</code> derived classes and
    * restores that object from its XML.
    *
    * @param reqType The request that was executed, assumed not <code>null</code>
    * or empty.
    * @param doc The request doc, assumed not <code>null</code>.
    *
    * @return The dependency object, never <code>null</code>.
    *
    * @throws PSDeployException If the doc is malformed.
    */
   private PSDependency getDependencyFromResultDoc(String reqType, Document doc)
      throws PSDeployException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element depEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (depEl == null)
      {
         Object args[] = {reqType, PSDependency.XML_NODE_NAME};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
      }

      return getDependencyFromElement(reqType, depEl);
   }

   /**
    * Gets a dependency object from the supplied element assuming it is the root
    * element of one of the <code>PSDependency</code> derived classes and
    * restores that object from its XML.
    *
    * @param reqType The request that was executed, assumed not <code>null</code>
    * or empty.
    * @param depEl The element, assumed not <code>null</code>.
    *
    * @return The dependency object, never <code>null</code>.
    *
    * @throws PSDeployException If the element does not represent a dependecy,
    * the element is malformed, or any other errors occur.
    */
   private PSDependency getDependencyFromElement(String reqType, Element depEl)
      throws PSDeployException
   {
      PSDependency dep = null;
      try
      {
         String elName = depEl.getTagName();
         if (PSDeployableElement.XML_NODE_NAME.equals(elName))
            dep = new PSDeployableElement(depEl);
         else if (PSDeployableObject.XML_NODE_NAME.equals(elName))
            dep = new PSDeployableObject(depEl);
         else if (PSUserDependency.XML_NODE_NAME.equals(elName))
            dep = new PSUserDependency(depEl);
      }
      catch (PSUnknownNodeTypeException e)
      {
         Object args[] = {reqType, PSDependency.XML_NODE_NAME,
               e.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
      }

      if (dep == null)
      {
         Object args[] = {reqType, PSDependency.XML_NODE_NAME};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_MISSING, args);
      }

      return dep;
   }

   /**
    * Runs the specified job on the server.
    *
    * @param desc The job descriptor, assumed not <code>null</code>.
    * @param jobType The job type, assumed not <code>null</code> or empty.
    *
    * @return The job control, never <code>null</code>.
    *
    * @throws PSServerLockException if a required server resource cannot be
    * locked.
    * @throws PSDeployException if there are any errors initiating the job.
    */
   private IPSDeployJobControl runServerJob(PSDescriptor desc, String jobType)
      throws PSServerLockException, PSDeployException
   {
      try
      {
         String reqType = getJobReqType("runJob");

         Document reqDoc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(reqDoc, desc.toXml(reqDoc));

         Map params = new HashMap();
         params.put("sys_jobCategory", "deployer");
         params.put("sys_jobType", jobType);
         
         // handle import job specific parameters
         if ("import".equals(jobType))
         {
            String applyToComms = System
                  .getProperty(IPSDeployConstants.APPLY_TO_ALL_COMMS);
            if ("true".equalsIgnoreCase(applyToComms))
            {
               params.put(IPSDeployConstants.APPLY_TO_ALL_COMMS, "true");
            }
         }

         // extend the lock just before submitting the job
         m_conn.extendLock();
         
         // run the job
         Document respDoc = m_conn.execute(reqType, reqDoc, params);

         Element root = respDoc.getDocumentElement();
         String idAttr = "jobId";
         String strId = root.getAttribute(idAttr);
         int jobId = -1;
         try
         {
            jobId = Integer.parseInt(strId);
         }
         catch (NumberFormatException e)
         {
            Object[] msgArgs = {root.getTagName(), idAttr, strId};
            PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

            Object args[] = {reqType, root.getTagName(),
                  une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_RESPONSE_ELEMENT_INVALID, args);
         }

         return new PSDeployServerJobControl(jobId, this);
      }
      catch (Exception e)
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
         else if (e instanceof PSServerLockException)
            throw (PSServerLockException)e;
         else
         {
            throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, e.toString());
         }
      }
   }

   /**
    * Connection to the server used to execute all requests.  Never
    * <code>null</code> after ctor, may retrieved by a call to
    * {@link #getConnection()}. If disconnected, any attempt to use it to
    * execute requests will result in an exception.
    * <p>
    * Assigned protected visiblity to give the Threads spawned by this class
    * effecient access to the field.
    */
   protected PSDeploymentServerConnection m_conn;

   /**
    * The cataloger used to make catalog requests to the server.  Initialized
    * during construction, never <code>null</code> or modified after that.
    */
   private PSCataloger m_cataloger;

   /**
    * String bundle used for messages.  <code>null</code> until loaded
    * by a call to {@link #getBundle()}, never <code>null</code> or modified 
    * after that.
    */
   private static ResourceBundle ms_bundle = null;
}


