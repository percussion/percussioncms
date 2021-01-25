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

package com.percussion.deploy.server;

import com.percussion.conn.PSServerException;
import com.percussion.deploy.catalog.server.PSCatalogHandler;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.client.PSDeploymentServerConnection;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.error.PSDeployNonUniqueException;
import com.percussion.deploy.error.PSLockedException;
import com.percussion.deploy.objectstore.IPSDeployComponent;
import com.percussion.deploy.objectstore.PSAppPolicySettings;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSArchive;
import com.percussion.deploy.objectstore.PSArchiveInfo;
import com.percussion.deploy.objectstore.PSArchiveSummary;
import com.percussion.deploy.objectstore.PSDbmsInfo;
import com.percussion.deploy.objectstore.PSDbmsMap;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.objectstore.PSDeployableObject;
import com.percussion.deploy.objectstore.PSExportDescriptor;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSImportDescriptor;
import com.percussion.deploy.objectstore.PSLogSummary;
import com.percussion.deploy.objectstore.PSUserDependency;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerBrand;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IOTools;
import com.percussion.util.IPSBrandCodeConstants;
import com.percussion.utils.security.deprecated.PSCryptographer;
import com.percussion.util.PSFormatVersion;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Class to handle all requests from Deployment client.  Loosely implements the
 * Singleton pattern in that a single instance is created by the server, and
 * other classes should use {@link #getInstance()} to obtain a reference, but
 * due to the fact that this is a loadable handler, the pattern is not enforced
 * with a private ctor.
 */
public class PSDeploymentHandler  implements IPSLoadableRequestHandler
{

   /**
    * Parameterless ctor used by server to construct this loadable handler.
    * Should not be used otherwise, as a single instance of this class should
    * exist and a reference to it be held by the server.  All other classes
    * should use {@link #getInstance()}.
    */
   public PSDeploymentHandler()
   {
      ms_deploymentHandler = this;
   }

   /**
    * Creates a session and authorizes the user, returning the server's version
    * information.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * <!ELEMENT PSXDeployConnectRequest (PSXFormatVersion)>
    * <!ATTLIST PSXDeployConnectRequest
    *    userId CDATA #REQUIRED
    *    password CDATA #REQUIRED
    *    overrideLock (yes|no) "no"
    *    enforceLicense (yes|no) "yes"
    * >
    *
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployConnectResponse (PSXFormatVersion, PSXDbmsInfo)>
    * &lt;!ATTLIST PSXDeployConnectResponse
    *    deployVersion CDATA #REQUIRED
    *    sessionId     CDATA #REQUIRED
    *    licensed (yes|no) "no"
    * >
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    * the server.
    * @throws IOException If such an exception occurs during authentication
    * @throws PSDeployException if there are any other errors.
    */
   public Document connect(PSRequest req)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException, IOException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String sessionId = null;

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the credentials
      Element root = doc.getDocumentElement();
      String uid = root.getAttribute("userId");
      String pwd = decryptPwd(uid, root.getAttribute("password"));
      String lock = root.getAttribute("overrideLock");
      boolean overrideLock = "yes".equalsIgnoreCase(lock);

      // by default server will do the license enforcement
      // only if the client request has attribute "enforceLicense" set to "no"
      // then the server will let the client do the license enforcement, and
      // in this case it will set the attribute "licensed" in the response doc
      // This is to support connection from older clients. Request from older
      // clients will not have the "enforceLicense" attribute. So for backwards
      // compatability the server will enforce the licensing if
      // "enforceLicense" attribute is missing.
      boolean bEnforceLic = true;
      String strEnforceLic = root.getAttribute("enforceLicense");
      if ((strEnforceLic != null) && (strEnforceLic.equalsIgnoreCase("no")))
         bEnforceLic = false;

      // check if the Multi-Server Manager is licensed or not
      String licensed = "no";
      PSServerBrand brand = new PSServerBrand();
      if (brand.isComponentLicensed(IPSBrandCodeConstants.MULTI_SERVER_MANANGER))
      {
         // Multi-Server manager is licensed
         licensed = "yes";
      }
      else if (bEnforceLic)
      {
         // multi-server manager is not licensed and server should enforce
         // the license
         throw new PSDeployException(
            IPSDeploymentErrors.MULTISERVER_MANAGER_DISABLED);
      }

      req.setCgiVariable(IPSCgiVariables.CGI_AUTH_USER_NAME, uid);
      req.setCgiVariable(IPSCgiVariables.CGI_AUTH_PASSWORD, pwd);

      // authenticate the user and create a session if required
      Document respdoc = PSXmlDocumentBuilder.createXmlDocument();
      try
      {
         PSSecurityFilter.authenticate(req.getServletRequest(), 
            req.getServletResponse(), uid, pwd);
      }
      catch (LoginException e)
      {
         throw new PSAuthenticationFailedException(
            IPSSecurityErrors.GENERIC_AUTHENTICATION_FAILED, null);
      }
      catch (ServletException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }

      PSServer.checkAccessLevel(req, PSAclEntry.SACE_ADMINISTER_SERVER);
      sessionId = req.getUserSessionId();

      acquireLock(uid, sessionId, overrideLock);

      root = PSXmlDocumentBuilder.createRoot(respdoc,
         "PSXDeployConnectResponse");
      root.setAttribute("deployVersion", String.valueOf(
         PSDeploymentServerConnection.DEPLOYMENT_INTERFACE_VERSION));
      root.setAttribute(SESSION_ID_ATTR, sessionId);
      root.setAttribute(LICENSED_ATTR, licensed);

      PSFormatVersion version = getServerVersion();
      root.appendChild(version.toXml(respdoc));
      PSDbmsHelper dbHelper = PSDbmsHelper.getInstance();
      PSDbmsInfo repository = dbHelper.getServerRepositoryInfo();
      root.appendChild(repository.toXml(respdoc));

      return respdoc;
   }

   /**
    * Gets all deployable elements of the specified type.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetDeployableElementsRequest
    *    type CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsResponse
    *    (PSXDeployableElement*)>
    * </code></pre>
    *
    * Never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document getDeployableElements(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the element type
      Element root = doc.getDocumentElement();
      String type = root.getAttribute("type");
      if (type == null || type.trim().length() == 0)
      {
         Object[] msgArgs = {root.getTagName(), "type", ""};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args = {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      // create the response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = respDoc.createElement(
         "PSXDeployGetDeployableElementsResponse");
      respDoc.appendChild(newRoot);

      // get the elements
      Iterator deps = m_depMgr.getDependencies(req.getSecurityToken(), type);
      while (deps.hasNext())
      {
         Object o = deps.next();

         if (o instanceof PSDeployableElement)
         {
            PSDeployableElement de = (PSDeployableElement)o;
            newRoot.appendChild(de.toXml(respDoc));
         }
      }

      return respDoc;

   }

   /**
    * Gets all deployable elements of the specified type.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDependenciesRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetDependenciesRequest
    *    type CDATA #REQUIRED
    *    parentId CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsResponse
    *    (PSXDeployableElement | PSXDeployableObject)*>
    * </code></pre>
    *
    * Never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document getDependencies(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the inputs
      String type = getRequiredAttrFromRequest(req, "type");
      String parentId = getRequiredAttrFromRequest(req, "parentId");

      // create the response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = respDoc.createElement(
         "PSXDeployGetDeployableElementsResponse");
      respDoc.appendChild(newRoot);

      // get the elements
      Iterator deps = m_depMgr.getDependencies(req.getSecurityToken(), type,
         parentId);
      while (deps.hasNext())
      {
         PSDependency dep = (PSDependency)deps.next();
         newRoot.appendChild(dep.toXml(respDoc));
      }

      return respDoc;

   }

   /**
    * Gets the specified export descriptor stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetExportDescriptorRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetExportDescriptorRequest
    *    descName CDATA #IMPLIED
    *    archiveLogId CDATA #IMPLIED
    * </code></pre>
    *
    * Either <code>descName</code> or <code>archiveLogId</code> must be 
    * supplied. 
    * <p>
    * If the <code>descName</code> attribute is supplied, then the 
    * export descriptor with that name is returned.
    * <p>
    * If the <code>archiveLogId</code> attribute is supplied, then the export 
    * descriptor from the archive referenced by the supplied archive log id will 
    * be returned.  Any packages that have never been installed will be removed, 
    * and listed in the package names returned by 
    * <code>PSExportDescriptor.getMissingPackages()</code>.
    * 
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetExportDescriptorResponse (PSXExportDescriptor)>
    * </code></pre> 
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if the descriptor cannot be located or there are 
    * any errors.
    */
   public Document getExportDescriptor(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      
      // get the root of the doc in request
      Document doc = req.getInputDocument();
      if (doc == null)
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      Element root = doc.getDocumentElement();
      
      String name = null;
      int logId = -1;
      name = root.getAttribute("descName");
      if (name == null || name.trim().length() == 0)
      {
         name = null;
         logId = getAttrNumberFromRequest(req, "archiveLogId");
      }
      
      PSExportDescriptor exportDesc;
      Element exportDescEl;
      if (name != null)
      {
         // load from disk
         File descFile = new File(EXPORT_DESC_DIR, name + ".xml");
   
         Document exportDescDoc = getDocumentFromFile(descFile, "Export Desc");
   
         // get the export descriptor from XML
         exportDescEl = exportDescDoc.getDocumentElement();
         try
         {
            exportDesc = new PSExportDescriptor(exportDescEl);
         }
         catch (PSUnknownNodeTypeException une)
         {
            Object[] args = {exportDescEl.getTagName(), 
               une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }
         exportDesc.clearMissingPackages();
      }
      else
      {
         // get it from the archive file
         PSArchiveSummary sum = getArchiveSummary(logId);
         
         // can't use info from the summary as that has no detail, and thus no
         // export descriptor
         PSArchiveInfo info = getArchiveInfo(
            sum.getArchiveInfo().getArchiveRef());
         
         // if source and target dbms are different, pass in source id map.
         PSIdMap idMap = getIdMap(info);
            
         exportDesc = m_depMgr.convertExportDescriptor(info, m_logHandler, 
            idMap);
      }

      // validate the export descriptor
      m_depMgr.validatePackages(req.getSecurityToken(), exportDesc);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      root = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetExportDescriptorResponse");
      exportDescEl = exportDesc.toXml(respDoc);
      root.appendChild(exportDescEl);

      return respDoc;

   }

   /**
    * Gets the specified validation results for specified archive ref stored on
    * the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetValidationResultsRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetValidationResultsRequest
    *    archiveRef CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetValidationResponse (PSXImportDescriptor)>
    * </code></pre>
    *
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document getValidationResults(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String name = getRequiredAttrFromRequest(req, "archiveRef");

      // load the validation results
      File descFile = new File(VALIDATION_RESULTS_DIR, name + ".xml");

      Document importDescDoc = getDocumentFromFile(descFile,
         "Validation Results");

      // get the import descriptor from XML
      Element importDescEl = importDescDoc.getDocumentElement();
      PSImportDescriptor importDesc;
      try
      {
         importDesc = new PSImportDescriptor(importDescEl);
      }
      catch (PSUnknownNodeTypeException une)
      {
         throw new PSDeployException(
            IPSDeploymentErrors.UNEXPECTED_ERROR, une.getLocalizedMessage());
      }

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetValidationResponse");
      importDescEl = importDesc.toXml(respDoc);
      root.appendChild(importDescEl);

      return respDoc;
   }

   /**
    * Gets the specified id type map(s) stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!--
    *    If there are no deployable objects, then all idtype maps are returned.
    *    Otherwise the corresponding map for each deployable object is returned.
    * -->
    * &lt;!ELEMENT PSXDeployGetIdTypesRequest (PSXDeployableObject*)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdTypesResponse
    *    (PSXApplicationIdTypes+)>
    * </code></pre>
    *
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document getIdTypes(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // Get the list of objects
      List depList = new ArrayList();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element depEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (depEl != null)
      {
         try
         {
            depList.add(new PSDeployableObject(depEl));
         }
         catch (PSUnknownNodeTypeException une)
         {
            Object[] args = {depEl.getTagName(), une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

         depEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      // if no dependencies, get all dependencies that support id types and are
      // deployable.
      Iterator deps;
      if (depList.isEmpty())
         deps = m_depMgr.getDependencies(req.getSecurityToken(),
            PSDependencyManager.TYPE_SUPPORTS_ID_TYPES |
               PSDependencyManager.TYPE_DEPLOYABLE);
      else
         deps = depList.iterator();

      // get the types for all dependencies in our list
      Iterator types = PSIdTypeManager.loadIdTypes(req.getSecurityToken(), 
         deps);

      // create the response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetIdTypesResponse");
      while (types.hasNext())
      {
         PSApplicationIDTypes type = (PSApplicationIDTypes)types.next();
         root.appendChild(type.toXml(respDoc));
      }

      return respDoc;
   }

   /**
    * Stores the supplied ID types on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdTypesRequest (PSXApplicationIdTypes+)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdTypesResponse EMPTY>
    * </code></pre>
    *
    * never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document saveIdTypes(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the PSIdType Element
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element idTypeEl = tree.getNextElement(PSApplicationIDTypes.XML_NODE_NAME,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (idTypeEl == null)
      {
         Object[] msgArgs = {PSApplicationIDTypes.XML_NODE_NAME};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

         Object[] args = {doc.getDocumentElement().getTagName(),
               une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      while (idTypeEl != null)
      {

         PSApplicationIDTypes idTypes = null;

         try
         {
            idTypes = new PSApplicationIDTypes(idTypeEl);
         }
         catch (PSUnknownNodeTypeException ne)
         {
            Object[] args = {idTypeEl.getTagName(), ne.toString()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

         PSIdTypeManager.saveIdTypes(idTypes);

         idTypeEl = tree.getNextElement(PSApplicationIDTypes.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }


      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeploySaveIdTypesResponse");

      return respDoc;
   }

   /**
    * Checks the server version and build compatiblity, and archive name for
    * uniqueness.  If there is an error (versions are not compatible, archive
    * name not unique), an exception is thrown.  If there is a warning (builds
    * are not compatible), a warning attribute is set on the response element.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployValidateArchiveRequest (PSXArchiveInfo)>
    * &lt;!ATTLIST PSXDeployValidateArchiveRequest
    *    checkArchiveRef (yes | no) #REQUIRED
    *    warnOnBuidMismatch (yes | no) "no"
    * >
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployValidateArchiveResponse EMPTY>
    * &lt;!ATTLIST PSXDeployValidateArchiveResponse
    *    warning CDATA #IMPLIED
    * >
    * </code></pre>
    *
    * Never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document validateArchive(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      try
      {
         // get check archive flag
         String sTemp = getRequiredAttrFromRequest(req, "checkArchiveRef");
         boolean checkArchiveRef = sTemp.equals("yes");

         sTemp = getRequiredAttrFromRequest(req, "warnOnBuidMismatch");
         boolean warnOnBuildMismatch = sTemp.equals("yes");

         // get the PSArchiveInfo Element
         PSXmlTreeWalker tree = new PSXmlTreeWalker(req.getInputDocument());

         Element infoEl = tree.getNextElement(PSArchiveInfo.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (infoEl == null)
         {
            Object[] msgArgs = {PSArchiveInfo.XML_NODE_NAME};
            PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

            Object[] args = {doc.getDocumentElement().getTagName(),
                  une.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

         // validate server version and build compatiblity, and archive name for
         // uniqueness.
         PSArchiveInfo info = new PSArchiveInfo(infoEl);
         PSFormatVersion version = getServerVersion();
         if (!version.getVersion().equals(info.getServerVersion()))
         {
            Object[] args = {info.getServerVersion(), version.getVersion()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_VERSION_MISMATCH, args);
         }

         if (checkArchiveRef && getImportArchiveFile(
            info.getArchiveRef()).exists())
         {
            throw new PSDeployNonUniqueException(
               IPSDeploymentErrors.ARCHIVE_REF_FOUND, info.getArchiveRef());
         }

         String message = null;
         if (!version.getVersionString().equals(
            info.getServerInfo().getVersionString()))
         {
            Object[] args = {info.getServerInfo().getVersionString(),
                  version.getVersionString()};
            PSDeployException ex = new PSDeployException(
               IPSDeploymentErrors.SERVER_BUILD_MISMATCH, args);
            if (warnOnBuildMismatch)
               message = ex.getLocalizedMessage();
            else
               throw ex;
         }

         // create the response doc
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeployValidateArchiveResponse");
         if (message != null)
            root.setAttribute("warning", message);

         return respDoc;
      }
      catch (PSUnknownNodeTypeException une)
      {
         Object[] args =
            {req.getInputDocument().getDocumentElement().getTagName(),
            une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }
   }

   /**
    * Gets the specified <code>PSDbmsMap</code> object stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDbmsMapRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetDbmsMapRequest
    *    server CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDbmsMapResponse (PSXIdMap)>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
   */
   public Document getDbmsMap(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String server = getRequiredAttrFromRequest(req, "server");

      PSDbmsMap map = PSDbmsMapManager.getDbmsMap(server);

      // create the response document for the PSIdMap
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element mapEl = map.toXml(respDoc);
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetDbmsMapResponse");
      newRoot.appendChild(mapEl);

      return respDoc;
   }

   /**
    * Stores the supplied DBMS-Map on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveDbmsMapRequest (PSXDbmsMap)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveDbmsMapResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
   */
   public Document saveDbmsMap(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSDbmsMap map = (PSDbmsMap) getRequiredComponentFromRequest(req,
         PSDbmsMap.class, PSDbmsMap.XML_NODE_NAME);

      PSDbmsMapManager.saveDbmsMap(map);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc, "PSXDeploySaveDbmsMapResponse");

      return respDoc;
   }

   /**
    * Gets the specified <code>PSIdMap</code> stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdMapRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetIdMapRequest
    *    sourceServer CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdMapResponse (PSXIdMap)>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    * @throws PSDeployException if an error errors.
    */
   public Document getIdMap(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String sourceServer = getRequiredAttrFromRequest(req, "sourceServer");

      // load the PSIdMap
      PSIdMap idmap = getIdMapMgr().getIdmap(sourceServer);

      // create the response document for the PSIdMap
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element idmapEl = idmap.toXml(respDoc);
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetIdMapResponse");
      newRoot.appendChild(idmapEl);

      return respDoc;
   }

   /**
    * Stores the supplied ID Map on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdMapRequest (PSXIdMap)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdMapResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document saveIdMap(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSIdMap idmap = (PSIdMap) getRequiredComponentFromRequest(req,
         PSIdMap.class, PSIdMap.XML_NODE_NAME);

      getIdMapMgr().saveIdMap(idmap);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc, "PSXDeploySaveIdMapResponse");

      return respDoc;
   }

   /**
    * Get the required attribute value of the <code>attrName</code> from the
    * input document element of the request object.
    *
    * @param req The <code>PSRequest</code> object, assume not
    * <code>null</code>.
    * @param attrName The attribute name, it need to be exist in the
    * <code>PSRequest</code> object, assume not <code>null</code> or empty.
    *
    * @return The retrieved value for the specified attribute. It never
    * be <code>null</code> or empty.
    *
    * @throws PSDeployException if the attribute does not exist or there is any
    * other errors.
    */
   private String getRequiredAttrFromRequest(PSRequest req, String attrName)
      throws PSDeployException
   {
      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }
      Element root = doc.getDocumentElement();
      String attrValue = root.getAttribute(attrName);
      if (attrValue == null || attrValue.trim().length() == 0)
      {
         Object[] msgArgs = {root.getTagName(), attrName, ""};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args = {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }
      return attrValue;
   }

   /**
  * Gets the specified <code>PSArchiveSummary</code> stored on the server.
  *
  * @param req The request, may not be <code>null</code>.  Input document is
  * expected to contain the following format:
  *
  * <pre><code>
  * &lt;!ELEMENT PSXDeployGetArchiveSummaryRequest EMPTY>
  * &lt;!ATTLIST PSXDeployGetArchiveSummaryRequest
  *    archiveLogId CDATA #IMPLIED
  *    archiveRef CDATA #IMPLIED
  * </code></pre>
  *
  * @return A document containing the following format:
  *
  * <pre><code>
  * &lt;!ELEMENT PSXDeployGetArchiveSummaryResponse (PSXArchiveSummary)>
  * </code></pre>
  * or an error, never <code>null</code>.
  *
  * @throws PSDeployException if the archive log cannot be found or there are
  * any other errors.
  * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
  */
 public Document getArchiveSummary(PSRequest req) throws PSDeployException
 {
    if (req == null)
       throw new IllegalArgumentException("req may not be null");

    // get the root of the doc in request
    Document doc = req.getInputDocument();
    if (doc == null)
       throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
    Element root = doc.getDocumentElement();

    int logId = -1;

    // retrieve one of the attribute in the request document
    String attrValue = root.getAttribute("archiveLogId");
    if (attrValue != null && attrValue.trim().length() != 0)
       logId = getAttrNumberFromRequest(req, "archiveLogId");
    else
       attrValue = getRequiredAttrFromRequest(req, "archiveRef");

    // retrieve the archive summary from the log handler
    PSArchiveSummary archiveSummary = null;
    if (logId != -1)
       archiveSummary = m_logHandler.getArchiveSummary(logId);
    else
       archiveSummary = m_logHandler.getArchiveSummary(attrValue);

    if ( archiveSummary == null ) // cannot find one
    {
       Object[] args = {"PSArchiveSummary", Integer.toString(logId)};
       throw new PSDeployException(
          IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
    }

    // create the response document for the PSArchiveSummary
    Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
    Element archiveEl = archiveSummary.toXml(respDoc);

    Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
       "PSXDeployGetArchiveSummaryResponse");
    newRoot.appendChild(archiveEl);

    return respDoc;
 }


   /**
    * Gets the archive info from the archive file referenced by the archive
    * summary id specified by the request.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveInfoRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetArchiveInfoRequest
    *    archiveLogId CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveInfoResponse (PSXArchiveInfo)>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws PSDeployException if the archive log cannot be found or there are
    * any other errors.
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    */
   public Document getArchiveInfo(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      int logId = getAttrNumberFromRequest(req, "archiveLogId");
      PSArchiveSummary sum = getArchiveSummary(logId);
      PSArchiveInfo archiveInfo = getArchiveInfo(
         sum.getArchiveInfo().getArchiveRef());

      // create the response document for the PSArchiveInfo
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element archiveEl = archiveInfo.toXml(respDoc);
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetArchiveInfoResponse");
      newRoot.appendChild(archiveEl);

      return respDoc;
   }
   
   /**
    * Get the archive summary for the specified archive log id.
    * 
    * @param logId The log id of the archive summary to retrieve.
    * 
    * @return The summary, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive summary cannot be located, or if 
    * there are any other errors.
    */
   private PSArchiveSummary getArchiveSummary(int logId) 
      throws PSDeployException
   {
      PSArchiveSummary archiveSummary = m_logHandler.getArchiveSummary(logId);

      // make sure the archive summary object exists
      if ( archiveSummary == null ) // cannot find one
      {
         Object[] args = {"PSArchiveSummary", Integer.toString(logId)};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      
      return archiveSummary;
   }
   
   /**
    * Get the archive from the specified archive file .
    * 
    * @param archiveRef The archive ref that specifies the archive file from 
    * which the archive info is to be extracted, assumed not <code>null</code> 
    * or empty.
    * 
    * @return The archive info, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive file cannot be located, or if 
    * there are any other errors.
    */
   private PSArchiveInfo getArchiveInfo(String archiveRef)
      throws PSDeployException 
   {
      File archiveFile = getImportArchiveFile(archiveRef);

      // make sure the archive file exists
      if ( ! archiveFile.exists() )
      {
         Object[] args = {"PSArchive", archiveRef};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      
      PSArchive archive = new PSArchive(archiveFile);
      PSArchiveInfo archiveInfo = archive.getArchiveInfo(true);
      archive.close();
      
      return archiveInfo;
   }

   /**
    * Gets the id map to use with the supplied source archive info.
    * 
    * @param info The info to use, assumed not <code>null</code>.
    * 
    * @return The map, or <code>null</code> if the source repository specified 
    * by the <code>info</code> is the same as the local server's repository
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSIdMap getIdMap(PSArchiveInfo info) throws PSDeployException
   {
      PSIdMap idMap = null;
      
      PSDbmsHelper dbHelper = PSDbmsHelper.getInstance();
      PSDbmsInfo sourceDb = info.getRepositoryInfo();
      if (!sourceDb.isSameDb(dbHelper.getServerRepositoryInfo()))
         idMap = m_idmapMgr.getIdmap(sourceDb.getDbmsIdentifier());
      
      return idMap;
   }

   /**
    * Get a (required) number from a attribute which is specified by a
    * given attribute name and request document.
    *
    * @param req The request which contains the attribute <code>attrName</code>,
    * assume not <code>null</code>.
    * @param attrName The name of the attribute, assume not <code>null</code>
    * or empty.
    *
    * @return The retrieved attribute value in <code>int</code>.
    *
    * @throws PSDeployException if an error occures.
    */
   private int getAttrNumberFromRequest(PSRequest req, String attrName)
      throws PSDeployException
   {
      String sNumber = getRequiredAttrFromRequest(req, attrName);

      int number;
      try
      {
         number = Integer.parseInt(sNumber);
      }
      catch (NumberFormatException e)
      {
         Element root = req.getInputDocument().getDocumentElement();
         Object[] msgArgs = {root.getTagName(), attrName, sNumber};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args = {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      return number;
   }

   /**
    * Gets the specified <code>PSLogSummary</code> stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetLogSummaryRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetLogSummaryRequest
    *    logId CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetLogSummaryResponse (PSXLogSummary)>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws PSDeployException if the archive log cannot be found or there are
    * any other errors.
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    */
   public Document getLogSummary(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      int logId = getAttrNumberFromRequest(req, "logId");
      PSLogSummary logSummary = m_logHandler.getLogSummary(logId);

      if ( logSummary == null ) // cannot find one
      {
         Object[] args = {"PSLogSummary", Integer.toString(logId)};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }

      String archiveRef =
         logSummary.getArchiveSummary().getArchiveInfo().getArchiveRef();
      File archiveFile = getImportArchiveFile(archiveRef);
      logSummary.setDoesArchiveExist(archiveFile.exists());

      // create the response document for the PSLogSummary
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element logSummaryEl = logSummary.toXml(respDoc);
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetLogSummaryResponse");
      newRoot.appendChild(logSummaryEl);

      return respDoc;
   }

   /**
    * Deletes the specified Archive file and all related archive summary and
    * package logs.  An archive log id or the archive ref may be supplied.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteArchiveRequest EMPTY>
    * &lt;!ATTLIST PSXDeployDeleteArchiveRequest
    *    logId CDATA #IMPLIED
    *    archiveRef CDATA #IMPLIED
    * >
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteArchiveResponse EMPTY>
    * </code></pre>
    * never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>req</code> is <code>null</code>.
    * @throws PSDeployException if any other error occures.
    */
   public Document deleteArchive(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }
      Element root = doc.getDocumentElement();
      String archiveRef = root.getAttribute("archiveRef");
      if (archiveRef == null || archiveRef.trim().length() == 0)
      {
         int logId = getAttrNumberFromRequest(req, "archiveLogId");
         PSArchiveSummary archiveSummary = m_logHandler.getArchiveSummary(
            logId);

         if ( archiveSummary != null )
         {
            archiveRef = archiveSummary.getArchiveInfo().getArchiveRef();
         }
      }

      m_logHandler.deleteAllLogs(archiveRef);
      File archiveFile = getImportArchiveFile(archiveRef);
      archiveFile.delete();

      // create the response document for the deletion
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployDeleteArchiveResponse");

      return respDoc;
   }

   /**
    * Stores the supplied export descriptor on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveExportDescriptorRequest (PSXExportDescriptor)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveExportDescriptorResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public Document saveExportDescriptor(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSExportDescriptor desc = (PSExportDescriptor)
         getRequiredComponentFromRequest(req, PSExportDescriptor.class,
         PSExportDescriptor.XML_NODE_NAME);

      String name = desc.getName();
      File descFile = new File(EXPORT_DESC_DIR, name + ".xml");

      saveComponentToFile(descFile, EXPORT_DESC_DIR, desc);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeploySaveExportDescriptorResponse");

      return respDoc;
   }

   /**
    * Gets a deployable component from the given request document. The
    * component is a child element of the root of the document. Validating the
    * parameters if needed.
    *
    * @param req The request which contains the document that the deployable
    * component is retrieved from, assume not <code>null</code>.
    * @param compClass The <code>Class</code> of the deployable component,
    * assume not <code>null</code>.
    * @param xmlNodeName The name of the XML node that is going to be retrieved,
    * assume not <code>null</code> or empty.
    *
    * @return The retrieved <code>IPSDeployComponent</code> object, never
    * <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   private IPSDeployComponent getRequiredComponentFromRequest(PSRequest req,
      Class compClass, String xmlNodeName) throws PSDeployException
   {
      Document doc = req.getInputDocument();
      if (doc == null)
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);

      // get the component
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element compEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (compEl == null)
      {
         Object[] msgArgs = {xmlNodeName};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

         Object[] args = {doc.getDocumentElement().getTagName(),
               une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      IPSDeployComponent comp = null;
      try
      {
         Constructor compCtor = compClass.getConstructor( new Class[]
            { Element.class });
         comp = (IPSDeployComponent) compCtor.newInstance(
            new Object[] {compEl} );
      }
      catch (Exception e)
      {
         if (e instanceof PSUnknownNodeTypeException)
         {
            Object[] args = {compEl.getTagName(), e.toString()};
               throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
      }

      return comp;
   }

   /**
    * Deletes the specified export descriptor stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteExportDescriptorRequest EMPTY>
    * &lt;!ATTLIST PSXDeployDeleteExportDescriptorRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteExportDescriptorResponse EMPTY>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document deleteExportDescriptor(PSRequest req)
      throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the descriptor name
      String name = getRequiredAttrFromRequest(req, "descName");

      // delete the descriptor
      File descFile = new File(EXPORT_DESC_DIR, name + ".xml");
      if (!descFile.exists())
      {
         Object[] args = {"Export Descriptor", name};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      descFile.delete();

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployDeleteExportDescriptorResponse");

      return respDoc;
   }

   /**
    * Gets the <code>PSAppPolicySettings</code> object stored on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetAppPolicySettingsRequest EMPTY>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetAppPolicySettingsResponse (PSXAppPolicySettings)>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
   */
   public Document getAppPolicySettings(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // load the descriptor
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployGetAppPolicySettingsResponse");
      newRoot.appendChild(getAppPolicySettings().toXml(respDoc));

      return respDoc;
   }

   /**
    * Gets the app policy settings object.
    *
    * @return The app policy settings, never be <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   PSAppPolicySettings getAppPolicySettings() throws PSDeployException
   {
      PSAppPolicySettings settings = null;

      if (POLICY_SETTINGS_FILE.exists())
      {
         Document policyDoc = getDocumentFromFile(POLICY_SETTINGS_FILE,
            "Policy settings");
         try
         {
            settings = new PSAppPolicySettings(policyDoc.getDocumentElement());
         }
         catch (PSUnknownNodeTypeException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }
      }
      else
         settings = new PSAppPolicySettings();

      return settings;
   }

   /**
    * Saves the application policy settings to the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveAppPolicySettingsRequest (PSXAppPolicySettings)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveAppPolicySettingsResponse EMPTY>
    * </code></pre>
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public Document saveAppPolicySettings(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSAppPolicySettings policySettings = (PSAppPolicySettings)
         getRequiredComponentFromRequest(req, PSAppPolicySettings.class,
         PSAppPolicySettings.XML_NODE_NAME);

      saveComponentToFile(POLICY_SETTINGS_FILE, new File(PSServer.getRxDir()
            .getAbsolutePath()
            + "/" + OBJECTSTORE_DIR), policySettings);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeploySaveAppPolicySettingsResponse");

      return respDoc;
   }

   /**
    * Save a deployable component to a file.
    *
    * @param compFile The file need to saved to, assume not <code>null</code>.
    * @param parentDir The parent direcotry of the <code>compFile</code>,
    * assume not <code>null</code>.
    * @param comp The to be saved component, assume not <code>null</code>
    *
    * @throws PSDeployException if an error occures.
    */
   private void saveComponentToFile(File compFile, File parentDir,
      IPSDeployComponent comp) throws PSDeployException
   {
      FileOutputStream out = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element compEl = comp.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, compEl);
         parentDir.mkdirs();
         out = new FileOutputStream(compFile);
         PSXmlDocumentBuilder.write(doc, out);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch(IOException ex){}
      }
   }

   /**
    * Acquires the lock for deployment.
    *
    * @param userId user seeking the lock. Assumed to be not <code>null</code>
    * or empty.
    *
    * @param sessionId session id of the user. Assumed to be not <code>null
    * </code> or empty.
    *
    * @param overrideLock If <code>true</code> then the lock is acquired
    * regardless of the locker, if <code>false</code> <code>m_lockingTime</code>
    * is checked for expiration, if that's <code>true</code>, lock is acquired,
    * if not, then sessionId is compared with the locker's session id <code>
    * m_sessionId</code>, if that's <code>true</code> lock is acquired or else
    * <code>PSLockedException</code> is thrown.
    *
    * @throws PSLockedException if the lock could not be acquired.
    */
   private void acquireLock(String userId, String sessionId,
      boolean overrideLock) throws PSLockedException
   {
      synchronized(m_mutexObject)
      {
         //lock if user is empty or the lock time is reset to 0 or the override
         // lock is true.
         if (m_lockedUser.equals("") || m_lockingTime == 0 || overrideLock)
         {
            setLockedValues(userId, sessionId,
               System.currentTimeMillis() + LOCKING_DURATION);
         }
         else
         {
            //if lock hasn't expired
            long oneMinute = 1000*60;  // one min of millisecs
            long remainder = m_lockingTime - System.currentTimeMillis();
            if (remainder >= oneMinute)
            {
               if (sessionId.equals(m_lockedSessionId))
                  setLockedValues(userId, sessionId,
                     System.currentTimeMillis() + LOCKING_DURATION);
               else
               {
                  String timeleft = String.valueOf((remainder)/oneMinute);
                  Object args[] = new Object[]{m_lockedUser, timeleft};
                  throw new PSLockedException(
                     IPSDeploymentErrors.LOCK_ALREADY_HELD, args);
               }
            }
            else
            {
               setLockedValues(userId, sessionId,
                  System.currentTimeMillis() + LOCKING_DURATION);
            }
         }
      }
   }

   /**
    * A simple setter for setting values for locking. All changes to the locking
    * values should be made through this method.
    *
    * @param userId  user getting the lock. Assumed to be not <code>null</code>.
    * @param sessionId session id of the user getting the lock. Assumed to be
    * not <code>null</code>.
    * @param lockTime time after which lock expires.
    */
   private void setLockedValues(String userId, String sessionId, long lockTime)
   {
      m_lockedUser = userId;
      m_lockedSessionId = sessionId;
      m_lockingTime = lockTime;

      if (m_lockedUser.trim().length() > 0)
         m_lastLockedUser = m_lockedUser;
   }

  /**
   * Releases the lock on the server only if session id of the user requesting
   * the release matches the one holding the lock.
   *
   * @param sessionId user session id of the user attempting to release the
   * lock. Assumed to be not <code>null</code> or empty.
   *
   * @return If <code>true</code> then the lock has been successfully released,
   * if not then either the lock has been overriden or was never acquired.
   */
   private boolean releaseLock(String sessionId)
   {
      synchronized(m_mutexObject)
      {
         if (sessionId.equals(m_lockedSessionId))
         {
            setLockedValues("", "", 0);
            return true;
         }
         else
            return false;
      }
   }


   /**
    * Matches the session id of the locked user with the
    * one holding the lock, if they are equal the lock is extended or else not.
    *
    * @param req The request, may not be <code>null</code>.
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployExtendLockResponse>
    *
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    * the server.
    * @throws PSDeployException if there are any other errors.
    */
   public Document extendLock(PSRequest req)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");


      Document doc = req.getInputDocument();
      String sessionId = req.getUserSessionId();
      String lockSessId = sessionId;

      // input doc is null when copying archive to server
      if (doc != null)
      {
         Element root = doc.getDocumentElement();
         String clientSessId = root.getAttribute(SESSION_ID_ATTR);
         if (clientSessId != null && clientSessId.trim().length() > 0)
            lockSessId = clientSessId;
      }


      // reset the values if session id matches or if not locked
      synchronized(m_mutexObject)
      {
         if (lockSessId.equals(m_lockedSessionId))
         {
            setLockedValues(m_lockedUser, sessionId,
                            System.currentTimeMillis() + LOCKING_DURATION);
         }
         else if (m_lockedUser.trim().length() == 0)
         {
            // lock has been aquired by someone else and released since this
            // user's last request
            Object args[] = new Object[]{m_lastLockedUser};
            throw new
               PSLockedException(
                  IPSDeploymentErrors.LOCK_NOT_EXTENSIBLE_TAKEN_RELEASED, args);
         }
         else
         {
            // lock held by another, check to see if expired
            long oneMinute = 1000*60;  // one min of millisecs
            long remainder = m_lockingTime - System.currentTimeMillis();
            if (remainder < oneMinute)
            {
               // lock has been aquired by someone else and has expired since
               // this user's last request
               Object args[] = new Object[]{m_lastLockedUser};
               throw new PSLockedException(
                  IPSDeploymentErrors.LOCK_NOT_EXTENSIBLE_TAKEN_RELEASED, args);
            }
            else
            {
               String timeleft = String.valueOf((remainder)/oneMinute);
               Object args[] = new Object[]{m_lockedUser, timeleft};
               throw new PSLockedException(
                  IPSDeploymentErrors.LOCK_NOT_EXTENSIBLE_TAKEN, args);
            }
         }
      }

      Document respdoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respdoc, "PSXDeployExtendLockResponse");
      return respdoc;
   }

   /**
    * Authorizes the user before disconnecting and releases the lock on the
    * server. If the release is succesful an empty document is returned or else
    * the document contains the optional parameters, see the response format.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * <!ELEMENT PSXDeployDisconnectRequest (PSXFormatVersion)>
    * <!ATTLIST PSXDeployDisconnectRequest
    *    userId CDATA #REQUIRED
    *    password CDATA #REQUIRED
    *
    * >
    *
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDisconnectResponse>
    * <!ATTLIST PSXDeployDisconnectRequest
    *    lockedUser CDATA #OPTIONAL
    *    lockedUntil CDATA #OPTIONAL
    * >
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    * the server.
    * @throws PSDeployException if there are any other errors.
    */
   public Document disconnect(PSRequest req)
      throws PSAuthenticationFailedException, PSAuthorizationException,
         PSServerException, PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String sessionId = null;

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the credentials
      Element root = doc.getDocumentElement();
      Document respdoc = PSXmlDocumentBuilder.createXmlDocument();
      // should be able to get the last session id used by the client.  If the
      // session has timed out since their last request, this request will have
      // generated a new session id, so we need to get the old one to release
      // the lock.
      sessionId = root.getAttribute(SESSION_ID_ATTR);
      if (sessionId == null || sessionId.trim().length() == 0)
         sessionId = req.getUserSessionId();
      root = PSXmlDocumentBuilder.createRoot(respdoc,
               "PSXDeployDisconnectResponse");
      if(!releaseLock(sessionId))
      {
         root.setAttribute("lockedUser", m_lockedUser);
         root.setAttribute("lockedUntil", String.valueOf(m_lockingTime));
      }
      return respdoc;
   }

   /**
    * Locates the immediate child dependencies of the supplied dependency and
    * adds them to it.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadDependenciesRequest (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)>
    * &lt;!ATTLIST PSXDeployLoadDependenciesRequest
    *    maxCount CDATA #IMPLIED
    * >
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadDependenciesResponse (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)>
    * </code></pre>
    *
    * never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document loadDependencies(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the dependency
      PSDependency dep = getDependencyFromRequestDoc(doc);
      List deps = PSDeployComponentUtils.cloneList(m_depMgr.getDependencies(
         req.getSecurityToken(), dep));

      // check max count to return
      checkDepCount(deps.size(), getMaxDepCount(doc.getDocumentElement()));
      dep.setDependencies(deps.iterator());

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployLoadDependenciesResponse");
      newRoot.appendChild(dep.toXml(respDoc));

      return respDoc;
   }

   /**
    * Locates the immediate parent dependencies of the supplied dependency and
    * adds them to it.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadAncestorsRequest (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)>
    * &lt;!ATTLIST PSXDeployLoadAncestorsRequest
    *    maxCount CDATA #IMPLIED
    * >
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadAncestorsResponse (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)>
    * </code></pre>
    *
    * never <code>null</code>.
    *
    * @throws PSDeployException if there are any errors.
    */
   public Document loadAncestors(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // get the dependency
      PSDependency dep = getDependencyFromRequestDoc(doc);
      List ancs = PSDeployComponentUtils.cloneList(m_depMgr.getAncestors(
         req.getSecurityToken(), dep));

      // check max count to return
      checkDepCount(ancs.size(), getMaxDepCount(doc.getDocumentElement()));
      dep.setAncestors(ancs.iterator());

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployLoadAncestorsResponse");
      newRoot.appendChild(dep.toXml(respDoc));

      return respDoc;
   }

   /**
    * Stores the supplied user dependency on the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveUserDependencyRequest (PSXUserDependency)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveUserDependencyResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException if an error errors.
    */
   public Document saveUserDependency(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSUserDependency dep = (PSUserDependency)
         getRequiredComponentFromRequest(req, PSUserDependency.class,
         PSUserDependency.XML_NODE_NAME);

      // save dep as Xml file in directory named using its parent's key
      m_depMgr.saveUserDependency(dep);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeploySaveUserDependencyResponse");

      return respDoc;
   }

   /**
    * Deletes the supplied user dependency from the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteUserDependencyRequest (PSXUserDependency)>
    * </code></pre>
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteUserDependencyResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws PSDeployException if an error errors.
    */
   public Document deleteUserDependency(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSUserDependency dep = (PSUserDependency)
         getRequiredComponentFromRequest(req, PSUserDependency.class,
         PSUserDependency.XML_NODE_NAME);

      // delete saved dep 
      m_depMgr.deleteUserDependency(dep);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeployDeleteUserDependencyResponse");

      return respDoc;
   }
   
   /**
    * Gets the archive file created by the export descritor specified by the
    * request and sets it as the content of the response.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveFileRequest EMPTY>
    * &lt;!ATTLIST PSXDeployGetArchiveFileRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    *
    * @return <code>null</code> always, as the content has been set on the
    * response.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException If any errors occur - the status code of the
    * response will also be set to <code>500</code>.
    */
   public Document getArchiveFile(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the descriptor name
      String name = getRequiredAttrFromRequest(req, "descName");

      // use descriptor name as archive ref
      File archiveFile = getExportArchiveFile(name);
      FileInputStream in = null;
      try
      {
         in = new FileInputStream(archiveFile);
         req.getResponse().setContent(in, archiveFile.length(),
            "application/octet-stream");
         in = null;

      }
      catch (FileNotFoundException e)
      {
         Object[] args = {"Archive File", name};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException ex){}
      }


      return null;
   }

   /**
    * Saves an archive file to disk.
    *
    * @param req The request, may not be <code>null</code>.  Must contain
    * at least one parameter that is a <code>File</code> object, but may contain
    * other parameters.  The first file found is saved using the value of the
    * <code>"archiveRef"</code> parameter as the archiveRef.
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveArchiveFileResponse EMPTY>
    * </code></pre>
    *
    * or an error, never <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>req</code> is <code>null</code>.
    * @throws PSDeployException If at least one file is not found in the request
    * params, or any other errors occur.
    */
   public Document saveArchiveFile(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the file to save
      Iterator params = req.getParametersIterator();

      String archiveRef = req.getParameter("archiveRef");
      if (archiveRef == null || archiveRef.trim().length() == 0)
      {
         Object[] args = {"archiveRef", archiveRef == null ? "null" :
               archiveRef};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_PARAM_INVALID, args);
      }

      File inFile = null;
      while (params.hasNext() && inFile == null)
      {
         Map.Entry entry = (Map.Entry)params.next();
         Object val = entry.getValue();
         if (val instanceof File)
         {
            inFile = (File)val;
         }
      }

      FileInputStream in = null;
      FileOutputStream out = null;
      try
      {
         File archiveFile = getImportArchiveFile(archiveRef);
         archiveFile.getParentFile().mkdirs();
         out = new FileOutputStream(archiveFile);
         in = new FileInputStream(inFile);
         IOTools.copyStream(in, out);
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException ex){}
         if (out != null)
            try {out.close();} catch(IOException ex){}
      }

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
         "PSXDeploySaveArchiveFileResponse");

      return respDoc;
   }

   /**
    * Gets the featureset from the server.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetFeatureSetRequest EMPTY>
    * </code></pre>
    *
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetFeatureSetResponse (PSXFeatureSet)>
    * </code></pre>
    *
    * Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>req</code> is invalid.
    * @throws PSDeployException if there are any unexpected errors.
    */
   public Document getFeatureSet(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // build the response doc
      File fsFile = new File(CFG_DIR, PSFeatureSet.FEATURE_SET_FILE);

      FileInputStream fIn = null;
      Document respDoc = null;

      try
      {
         respDoc = PSXmlDocumentBuilder.createXmlDocument();

         // build our response node
         Element respRoot = PSXmlDocumentBuilder.createRoot(
            respDoc, "PSXDeployGetFeatureSetResponse");

         /* now try to load the file - if we don't find it, we'll return
          * an empty featureset
          */
         if (fsFile.exists())
         {
            fIn = new FileInputStream(fsFile);
            Document featureDoc = PSXmlDocumentBuilder.createXmlDocument(fIn,
               false);
            Node importNode = respDoc.importNode(
               featureDoc.getDocumentElement(), true);
            respRoot.appendChild(importNode);
         }
         else
         {
            PSXmlDocumentBuilder.addEmptyElement(respDoc, respRoot,
               PSFeatureSet.ms_nodeName);
         }
      }
      catch (Exception e)
      {
         // wrap exception
         Object[] args = {e.getLocalizedMessage()};
         PSServerException se = new PSServerException(
            IPSObjectStoreErrors.FEATURE_SET_LOAD_EXCEPTION, args);
         PSDeployException de = new PSDeployException(se);
         throw de;
      }
      finally
      {
         if (fIn != null)
            try { fIn.close(); } catch (Exception e) { /* ignore */ }
      }

      return respDoc;

   }

   /**
    * Gets the parent types for each dependency type that supports parent ids.
    *
    * @param req The request, may not be <code>null</code>.  Input document is
    * expected to contain the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetParentTypesRequest EMPTY>
    * </code></pre>
    *
    *
    * @return A document containing the following format:
    *
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetParentTypesResponse (entry*)>
    * &lt;!ELEMENT entry  EMPTY>
    * &lt;!ATTLIST entry
    *    childType CDATA #REQUIRED
    *    parentType CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * Never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>req</code> is invalid.
    * @throws PSDeployException if there are any unexpected errors.
    */
   public Document getParentTypes(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }

      // build the response doc
      Document respDoc = null;

      respDoc = PSXmlDocumentBuilder.createXmlDocument();

      // build our response node
      Element respRoot = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXDeployGetParentTypesResponse");
      Map types = m_depMgr.getParentTypes();
      Iterator entries = types.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         Element entryEl = PSXmlDocumentBuilder.addEmptyElement(respDoc,
            respRoot, "entry");
         entryEl.setAttribute("childType", (String)entry.getKey());
         entryEl.setAttribute("parentType", (String)entry.getValue());
      }


      return respDoc;

   }

   /**
    * Get this instance of the deployment handler
    *
    * @return The single instance of the deployment handler running on the
    * server, or <code>null</code> if one has not been created.
    */
   public static PSDeploymentHandler getInstance()
   {
      return ms_deploymentHandler;
   }

   /**
    * Get the ID Map manager from this object.
    *
    * @return The ID Map Manager, it can never be <code>null</code>
    */
   public PSIdMapManager getIdMapMgr()
   {
      return m_idmapMgr;
   }

   /**
    * Get the log handler from this object.
    *
    * @return The log handler, never <code>null</code>.
    */
   public PSLogHandler getLogHandler()
   {
      return m_logHandler;
   }

   /**
    * Get the dependency manager from this object.
    *
    * @return The dependency manager, never <code>null</code>.
    */
   public PSDependencyManager getDependencyManager()
   {
      return m_depMgr;
   }

   /**
    * Get the archive file reference for the specified import archive ref.
    *
    * @param archiveRef The archive ref, may not be <code>null</code> or empty.
    *
    * @return The file, may or may not exist, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>archiveRef</code> is invalid.
    */
   public File getImportArchiveFile(String archiveRef)
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      File archiveDir = new File(IMPORT_ARCHIVE_DIR);

      return new File(archiveDir, archiveRef +
         IPSDeployConstants.ARCHIVE_EXTENSION);
   }

   /**
    * Get the archive file reference for the specified export archive ref.
    *
    * @param archiveRef The archive ref, may not be <code>null</code> or empty.
    *
    * @return The file, may or may not exist.
    *
    * @throws IllegalArgumentException if <code>archiveRef</code> is invalid.
    */
   public File getExportArchiveFile(String archiveRef)
   {
      if (archiveRef == null || archiveRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "archiveRef may not be null or empty");

      File archiveDir = new File(EXPORT_ARCHIVE_DIR);

      return new File(archiveDir, archiveRef +
         IPSDeployConstants.ARCHIVE_EXTENSION);
   }

   //Methods generated from interface IPSLoadableRequestHandler
   public void init(Collection requestRoots, InputStream cfgFileIn)
      throws PSServerException
   {
      PSConsole.printMsg(DEPLOY_SUBSYSTEM, "Initializing Deployment Handler");
      m_requestRoots = requestRoots;

      try
      {
         m_depMgr = new PSDependencyManager();
         m_idmapMgr = new PSIdMapManager();
         m_logHandler = new PSLogHandler();
      }
      catch (PSDeployException e)
      {
         Object[] args = {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
            IPSServerErrors.LOADABLE_HANDLER_UNEXPECTED_EXCEPTION, args);
      }

   }

   //Methods generated from implementation of interface IPSRootedHandler
   /**
   * Get the name of the rooted handler. Used by the server to identify the
   * handler during intilization and when reporting information about all
   * rooted handlers. All rooted handlers will be served by rhythmyx at
   * runtime.
   *
   * @return the handler name, should be unique across all rooted
   * handlers, never <code>null</code> or empty. If <code>null</code>
   * or empty the server will ignore this handler. If not unique, the
   * results will be unpredictable as to which handler will receive the
   * request for processing.
   */
   public String getName()
   {
      return DEPLOY_SUBSYSTEM;
   }

   /**
   * Get all request roots of the rooted handler.  Called by the server when
   * it is initializing the handler.
   *
   * @return an iterator over one or more request roots as Strings. The
   * iterator must contain at least one entry, and should not contain
   * duplicates. Never <code>null</code> or empty. If <code>null</code> or
   * empty the server will ignore this handler.
   */
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   //Methods generated from implementation of interface IPSRequestHandler
   public void processRequest(PSRequest request)
   {
      Document respDoc = null;

      String reqType = request.getCgiVariable(
         IPSCgiVariables.CGI_PS_REQUEST_TYPE);
      String subReqType;
      
      try
      {
         if (reqType == null || !reqType.startsWith("deploy-"))
         {
            throw new PSDeployException(
               IPSDeploymentErrors.INVALID_REQUEST_TYPE, reqType == null ? "" :
                  reqType);
         }
         else
         {
            subReqType = reqType.substring("deploy-".length());
         }

         if (subReqType.equals("connect"))
         {
            // this will authenticate and check security
            respDoc = connect(request);
         }
         else
         {
            Document tempDoc = null;
            boolean isDisconnect = subReqType.equals("disconnect");

            // all requests other than disconnect have security checked and
            // lock extended by default
            if (!isDisconnect)
            {
               PSServer.checkAccessLevel(request,
                  PSAclEntry.SACE_ADMINISTER_SERVER);
               tempDoc = extendLock(request);
            }

            if (subReqType.equals("extendlock"))
               respDoc = tempDoc;
            else if (subReqType.equals("disconnect"))
               respDoc = disconnect(request);
            else if(subReqType.equals("catalog"))
               respDoc = PSCatalogHandler.processRequest(request);
            else if(subReqType.equals("getExportDescriptor"))
               respDoc = getExportDescriptor(request);
            else if(subReqType.equals("saveExportDescriptor"))
               respDoc = saveExportDescriptor(request);
            else if(subReqType.equals("saveArchiveFile"))
               respDoc = saveArchiveFile(request);
            else if(subReqType.equals("getArchiveFile"))
               respDoc = getArchiveFile(request);
            else if(subReqType.equals("saveUserDependency"))
               respDoc = saveUserDependency(request);
            else if(subReqType.equals("deleteUserDependency"))
               respDoc = deleteUserDependency(request);
            else if(subReqType.equals("deleteExportDescriptor"))
               respDoc = deleteExportDescriptor(request);
            else if(subReqType.equals("loadDependencies"))
               respDoc = loadDependencies(request);
            else if(subReqType.equals("loadAncestors"))
               respDoc = loadAncestors(request);
            else if(subReqType.equals("getIdMap"))
               respDoc = getIdMap(request);
            else if(subReqType.equals("saveIdMap"))
               respDoc = saveIdMap(request);
            else if(subReqType.equals("getArchiveSummary"))
               respDoc = getArchiveSummary(request);
            else if(subReqType.equals("getArchiveInfo"))
               respDoc = getArchiveInfo(request);
            else if(subReqType.equals("getLogSummary"))
               respDoc = getLogSummary(request);
            else if(subReqType.equals("deleteArchive"))
               respDoc = deleteArchive(request);
            else if(subReqType.equals("getDbmsMap"))
               respDoc = getDbmsMap(request);
            else if(subReqType.equals("saveDbmsMap"))
               respDoc = saveDbmsMap(request);
            else if(subReqType.equals("getDeployableElements"))
               respDoc = getDeployableElements(request);
            else if(subReqType.equals("getIdTypes"))
               respDoc = getIdTypes(request);
            else if(subReqType.equals("saveIdTypes"))
               respDoc = saveIdTypes(request);
            else if(subReqType.equals("validateArchive"))
               respDoc = validateArchive(request);
            else if(subReqType.equals("saveAppPolicySettings"))
               respDoc = saveAppPolicySettings(request);
            else if(subReqType.equals("getAppPolicySettings"))
               respDoc = getAppPolicySettings(request);
            else if(subReqType.equals("getFeatureSet"))
               respDoc = getFeatureSet(request);
            else if(subReqType.equals("getDependencies"))
               respDoc = getDependencies(request);
            else if(subReqType.equals("getParentTypes"))
               respDoc = getParentTypes(request);
            else if(subReqType.equals("getValidationResults"))
               respDoc = getValidationResults(request);
            else
            {
               throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_REQUEST_TYPE, reqType);
            }
         }
      }
      catch (Exception e)
      {
         // its possible that session has timed out, so
         // release the lock if the session id sent by the client doesn't match
         // the real session id
         Document reqDoc = request.getInputDocument();
         if (reqDoc != null)
         {
            Element root = reqDoc.getDocumentElement();
            if (root != null)
            {
               String clientSessId = root.getAttribute(SESSION_ID_ATTR);
               if (clientSessId != null && !clientSessId.equals(
                  request.getUserSessionId()))
               {
                  releaseLock(clientSessId);
               }
            }
         }


         // Convert to xml response
         PSDeployException de = null;
         if (e instanceof PSDeployException)
            de = (PSDeployException)e;
         else if (e instanceof PSException)
            de = new PSDeployException((PSException)e);
         else
         {
            de = new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }

         respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element respEl = de.toXml(respDoc);
         PSXmlDocumentBuilder.replaceRoot(respDoc, respEl);
         request.getResponse().setStatus(500);
      }

      PSResponse resp = request.getResponse();
      if (respDoc != null)
         resp.setContent(respDoc);
   }

   /**
    * Shutdown the request handler, freeing any associated resources.
    */
   public void shutdown()
   {
      ms_deploymentHandler = null;
   }

   /**
    * Decrypts the supplied password if it is non-<code>null</code> and not
    * empty.
    *
    * @param uid The user id, may be <code>null</code> or empty.
    * @param pwd The password to decrypt, may be <code>null</code> or empty.
    *
    * @return The decrypted password, or an empty string if the supplied
    * password is <code>null</code> or empty.
    */
   public String decryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      String key = uid == null || uid.trim().length() == 0 ? PSLegacyEncrypter.INVALID_CRED() :
         uid;

      return decryptPwd(pwd, PSLegacyEncrypter.INVALID_CRED(), key);
   }

   /**
    * Decrypts the supplied password if it is non-<code>null</code> and not
    * empty.
    *
    * @param pwd The password to decrypt, may be <code>null</code> or empty.
    * @param key1 The part one key to use, assumed not <code>null</code> or
    * empty.
    * @param key2 The part two key to use, assumed not <code>null</code> or
    * empty.
    *
    * @return The decrypted password, or an empty string if the supplied
    * password is <code>null</code> or empty.
    */
   private String decryptPwd(String pwd, String key1, String key2)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      return PSCryptographer.decrypt(key1, key2, pwd);
   }

   /**
    * Get the document containing the content of the specified file from disk.
    *
    * @param docFile The <code>File</code> object that the document is
    * retrieved from, may not  be <code>null</code>.
    * @param docDescription The description of the document, may not be
    * <code>null</code>.
    *
    * @return The retrieved document, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if the file does not exist or there is an
    * error.
    */
   public static Document getDocumentFromFile(File docFile,
      String docDescription) throws PSDeployException
   {
      if(docFile == null)
         throw new IllegalArgumentException("docFile may not be null.");

      if(docDescription == null || docDescription.trim().length()==0)
         throw new IllegalArgumentException(
            "docDescription may not be null or empty.");

      if (!docFile.exists())
      {
         Object[] args = {docDescription, docFile.getAbsolutePath()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }

      FileInputStream in = null;
      try
      {
         in = new FileInputStream(docFile);
         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try {in.close();} catch(IOException ex){}
      }

   }

   /**
    * Checks the supplied count to see if it exceeds the max limit that can be
    * returned.
    *
    * @param depCount The count to check
    * @param maxCount The maximum allowed.
    *
    * @throws PSDeployException if the limit is exceeded.
    */
   private void checkDepCount(int depCount, int maxCount)
      throws PSDeployException
   {
      if (depCount > maxCount)
      {
         throw new PSDeployException(IPSDeploymentErrors.MAX_DEP_COUNT_EXCEEDED,
            String.valueOf(maxCount));
      }
   }

   /**
    * Get the max dependency return count.  Checks the supplied element for an
    * attribute <code>maxCount</code> and tries to parse that value.  If so,
    * that value is returned, otherwise the default value
    * {@link IPSDeployConstants#MAX_DEPS} is returned.
    *
    * @param srcNode The node to check, may be <code>null</code>.
    *
    * @return The count.
    */
   private int getMaxDepCount(Element srcNode)
   {
      int maxCount = IPSDeployConstants.MAX_DEPS;
      if (srcNode != null)
      {
         try
         {
            maxCount = Integer.parseInt(srcNode.getAttribute("maxCount"));
         }
         catch (NumberFormatException e)
         {
            // do nothing
         }
      }

      return maxCount;
   }


   /**
    * Checks the root of the supplied doc for a child element that is the root
    * element of one of the <code>PSDependency</code> derived classes and
    * restores that object from its XML.
    *
    * @param doc The request doc, assumed not <code>null</code>.
    *
    * @return The dependency object, never <code>null</code>.
    *
    * @throws PSDeployException If the doc is malformed.
    */
   private PSDependency getDependencyFromRequestDoc(Document doc)
      throws PSDeployException
   {
      PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
      Element depEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      PSDependency dep = null;
      if (depEl != null)
      {
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
            Object[] args = {doc.getDocumentElement().getTagName(),
                  e.getLocalizedMessage()};
            throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

      }

      if (dep == null)
      {
         String badEl = depEl == null ? "null" : depEl.getTagName();
         Object[] msgArgs = {PSDependency.XML_NODE_NAME, badEl};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, msgArgs);

         Object[] args = {doc.getDocumentElement().getTagName(),
               une.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      return dep;
   }

   /**
    * Get the server's version info.
    *
    * @return The info, never <code>null</code>.
    */
   private PSFormatVersion getServerVersion()
   {
      String pkg = "com.percussion.util";
      return new PSFormatVersion(pkg);
   }

   /**
    * Constant for deployment subsystem to use for console and logging messages.
    */
   public static final String DEPLOY_SUBSYSTEM = "MultiServerMgr";

   /**
    * Directory below the server directory containing all objectstore files.
    */
   public static final String OBJECTSTORE_DIR = IPSDeployConstants.SERVER_DIR +
      "/objectstore";

   /**
    * Directory below the objectstore directory containing all DbmsMap files.
    */
   public static final File DBMSMAP_DIR = new File( 
         PSServer.getRxDir().getAbsolutePath()+"/"+ OBJECTSTORE_DIR, "DbmsMap");

   /**
    * Directory below the SERVER directory containing all config files.
    */
   public static final File CFG_DIR = new File(
         PSServer.getRxDir()
         .getAbsolutePath()+"/"+ IPSDeployConstants.SERVER_DIR, "cfg");

   /**
    * Directory below the objectstore directory containing all export
    * descriptors.
    */
   public static final File EXPORT_DESC_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()+"/"
         + OBJECTSTORE_DIR, "ExportDescriptors");

   /**
    * Directory below the server directory containing all temporary
    * export archive files.
    */
   public static final String EXPORT_ARCHIVE_DIR = PSServer.getRxDir()
   .getAbsolutePath() + "/" + IPSDeployConstants.SERVER_DIR
      + "/ExportArchives";

   /**
    * Directory below the server directory containing all archive files.
    */
   public static final String IMPORT_ARCHIVE_DIR = PSServer.getRxDir()
   .getAbsolutePath() + "/"+IPSDeployConstants.SERVER_DIR
      + "/ImportArchives";

   /**
    * Directory below the objectstore directory containing all idTypes files.
    */
   public static final File IDTYPE_DIR = new File(
         PSServer.getRxDir().getAbsolutePath()+"/"+ OBJECTSTORE_DIR,
      "IdTypes");

   /**
    * The <code>File</code> object for policy settings.
    */
   public static final File POLICY_SETTINGS_FILE = new File(
         PSServer.getRxDir().getAbsolutePath()+"/"+ OBJECTSTORE_DIR,
         "DeploymentPolicySettings.xml");

   /**
    * Directory below the server directory containing all validation results.
    */
   public static final String VALIDATION_RESULTS_DIR =
      IPSDeployConstants.SERVER_DIR + "/ValidationResults";

   /**
    * Mutex preventing concurrent access to a critical section by having threads
    * synchronize on it. Never modified.
    */
    private Object m_mutexObject = new Object();

    /**
     * Duration for which lock is held in milliseconds, currently 30 minutes.
     */
    private static long LOCKING_DURATION =
       IPSDeployConstants.LOCK_EXPIRATION_DURATION * 60 * 1000;

   /**
    * Attribute used to pass session id in the request doc as well as the
    * cookie.  Used to determine the previous session id of the client if the
    * session has timed out and a new session id is generated by the request.
    */
   private static final String SESSION_ID_ATTR = "sessionId";

   /**
    * Attribute used to inform the client whether the server is licensed for
    * Multi-Server manager or not. Valid values for this attribute are
    * "yes" or "no".
    */
   private static final String LICENSED_ATTR = "licensed";

    /**
     * Represents user holding the lock. Set and modified in
     * {@link #setLockedValues(String, String, long)}. Never <code>null</code>.
     * Initialised to empty String.
     */
    private String m_lockedUser = "";

    /**
     * Represents the last user to hold the lock. Set and modified in
     * {@link #setLockedValues(String, String, long)}. Never <code>null</code>.
     * Initialised to empty String, never empty after the first time the server
     * is locked.
     */
    private String m_lastLockedUser = "";

    /**
     * Represents time until the expiration for which the lock will be held.
     * Lock expires 30 minutes after the acquisition of the lock. If lock is
     * acquired at time t in milliseconds then lock will be held until this
     * (t+30*60*1000) time. Set and modified in {@link #acquireLock(String,
     * String, boolean) execute(userName, sessionId, overrideLock)}.
     */
    private long m_lockingTime;

    /**
     * Represents session id of the locked user <code>m_lockedUser</code>. Set
     * and modified in {@link #acquireLock(String, String, boolean)
     * execute(userName, sessionId, overrideLock)}. Never <code>null</code>.
     */
    private String m_lockedSessionId;

    /**
    * Request roots this handler will support, initialized during the
    * <code>init()</code> method, never <code>null</code>, empty, or modified
    * after that.
    */
   private Collection m_requestRoots;

   /**
    * Singleton instance of the deployment handler.  Not <code>null</code> after
    * call to ctor by the server.  Does not stricly enforce the singleton
    * pattern, but this handler should only be instantiated by the server.
    */
   private static PSDeploymentHandler ms_deploymentHandler = null;

   /**
    * Dependency manager used to handle all dependency related operations.
    * Initialized during <code>init()</code>, never <code>null</code> after
    * that.
    */
   private PSDependencyManager m_depMgr;

   /**
    * IdMap manager used to handle all PSIdMap related operations.
    * Initialized during <code>init()</code>, never <code>null</code> after
    * that.
    */
   private PSIdMapManager m_idmapMgr;

   /**
    * The log handler for processing log table related operations.
    */
   PSLogHandler m_logHandler;
}
