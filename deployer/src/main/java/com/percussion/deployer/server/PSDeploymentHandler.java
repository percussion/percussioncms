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

package com.percussion.deployer.server;

import com.percussion.conn.PSServerException;
import com.percussion.deployer.catalog.server.PSCatalogHandler;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentServerConnection;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.error.PSDeployNonUniqueException;
import com.percussion.deployer.error.PSLockedException;
import com.percussion.deployer.objectstore.IPSDeployComponent;
import com.percussion.deployer.objectstore.PSAppPolicySettings;
import com.percussion.deployer.objectstore.PSApplicationIDTypes;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.deployer.server.uninstall.IPSUninstallResult;
import com.percussion.deployer.server.uninstall.PSPackageUninstaller;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSAclEntry;
import com.percussion.design.objectstore.PSFeatureSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.rx.config.data.PSDescriptorSummaryReport;
import com.percussion.rx.config.impl.PSConfigDefGenerator;
import com.percussion.rx.config.impl.PSDefaultConfigGenerator;
import com.percussion.security.IPSSecurityErrors;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSCgiVariables;
import com.percussion.server.IPSLoadableRequestHandler;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSConsole;
import com.percussion.server.PSRequest;
import com.percussion.server.PSResponse;
import com.percussion.server.PSServer;
import com.percussion.server.PSServerBrand;
import com.percussion.server.PSUserSessionManager;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.util.IOTools;
import com.percussion.util.IPSBrandCodeConstants;
import com.percussion.utils.security.PSEncryptionException;
import com.percussion.utils.security.PSEncryptor;
import com.percussion.utils.security.deprecated.PSCryptographer;
import com.percussion.util.PSFormatVersion;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.codec.PSXmlDecoder;
import com.percussion.utils.collections.PSMultiValueHashMap;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.deprecated.PSLegacyEncrypter;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import com.percussion.xml.PSXmlValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle all requests from Deployment client. Loosely implements the
 * Singleton pattern in that a single instance is created by the server, and
 * other classes should use {@link #getInstance()} to obtain a reference, but
 * due to the fact that this is a loadable handler, the pattern is not enforced
 * with a private ctor.
 */
public class PSDeploymentHandler implements IPSLoadableRequestHandler
{

   private final static Logger ms_log = Logger.getLogger(PSDeploymentHandler.class);
   
   /**
    * Parameterless ctor used by server to construct this loadable handler.
    * Should not be used otherwise, as a single instance of this class should
    * exist and a reference to it be held by the server. All other classes
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployConnectRequest (PSXFormatVersion)&gt;
    * &lt;!ATTLIST PSXDeployConnectRequest
    *    userId CDATA #REQUIRED
    *    password CDATA #REQUIRED
    *    overrideLock (yes|no) &quot;no&quot;
    *    enforceLicense (yes|no) &quot;yes&quot;
    * &gt;
    * 
    * 
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployConnectResponse (PSXFormatVersion, PSXDbmsInfo)&gt;
    * &lt;!ATTLIST PSXDeployConnectResponse
    *    deployVersion CDATA #REQUIRED
    *    sessionId     CDATA #REQUIRED
    *    licensed (yes|no) &quot;no&quot;
    * &gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    *             the server.
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
      if (brand
            .isComponentLicensed(IPSBrandCodeConstants.MULTI_SERVER_MANANGER))
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
         PSSecurityFilter.authenticate(req.getServletRequest(), req
               .getServletResponse(), uid, pwd);
      }
      catch (LoginException e)
      {
         throw new PSAuthenticationFailedException(
               IPSSecurityErrors.GENERIC_AUTHENTICATION_FAILED, null);
      }
      catch (ServletException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }

      PSServer.checkAccessLevel(req, PSAclEntry.SACE_ADMINISTER_SERVER);
      sessionId = req.getUserSessionId();

      acquireLock(uid, sessionId, overrideLock);

      root = PSXmlDocumentBuilder.createRoot(respdoc,
            "PSXDeployConnectResponse");
      root
            .setAttribute(
                  "deployVersion",
                  String
                        .valueOf(PSDeploymentServerConnection.DEPLOYMENT_INTERFACE_VERSION));
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetDeployableElementsRequest
    *    type CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsResponse
    *    (PSXDeployableElement*)&gt;
    * </code></pre>
    * 
    * Never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public Document getDeployableElements(PSRequest req)
         throws PSDeployException
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
         Object[] msgArgs =
         {root.getTagName(), "type", ""};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args =
         {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      // create the response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element newRoot = respDoc
            .createElement("PSXDeployGetDeployableElementsResponse");
      respDoc.appendChild(newRoot);

      // get the elements
      Iterator deps = m_depMgr.getDependencies(req.getSecurityToken(), type);
      while (deps.hasNext())
      {
         Object o = deps.next();

         if (o instanceof PSDeployableElement)
         {
            PSDeployableElement de = (PSDeployableElement) o;
            newRoot.appendChild(de.toXml(respDoc));
         }
      }

      return respDoc;

   }

   /**
    * Gets all deployable elements of the specified type.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDependenciesRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetDependenciesRequest
    *    type CDATA #REQUIRED
    *    parentId CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDeployableElementsResponse
    *    (PSXDeployableElement | PSXDeployableObject)*&gt;
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
      Element newRoot = respDoc
            .createElement("PSXDeployGetDeployableElementsResponse");
      respDoc.appendChild(newRoot);

      // get the elements
      Iterator deps = m_depMgr.getDependencies(req.getSecurityToken(), type,
            parentId);
      while (deps.hasNext())
      {
         PSDependency dep = (PSDependency) deps.next();
         newRoot.appendChild(dep.toXml(respDoc));
      }

      return respDoc;

   }

   /**
    * Gets the specified export descriptor stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetExportDescriptorRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetExportDescriptorRequest
    *    descName CDATA #IMPLIED
    *    archiveLogId CDATA #IMPLIED
    * </code></pre>
    * 
    * Either <code>descName</code> or <code>archiveLogId</code> must be
    * supplied.
    * <p>
    * If the <code>descName</code> attribute is supplied, then the export
    * descriptor with that name is returned.
    * <p>
    * If the <code>archiveLogId</code> attribute is supplied, then the export
    * descriptor from the archive referenced by the supplied archive log id will
    * be returned. Any packages that have never been installed will be removed,
    * and listed in the package names returned by
    * <code>PSExportDescriptor.getMissingPackages()</code>.
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetExportDescriptorResponse (PSXExportDescriptor)&gt;
    * </code></pre>
    * 
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException if the descriptor cannot be located or there are
    *             any errors.
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
         exportDesc = getDescriptor(name);
      }
      else
      {
         // get it from the archive file
         PSArchiveSummary sum = getArchiveSummary(logId);

         // can't use info from the summary as that has no detail, and thus no
         // export descriptor
         PSArchiveInfo info = getArchiveInfo(sum.getArchiveInfo()
               .getArchiveRef());

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
    * Gets the directory below the server directory containing all validation 
    * results.
    * 
    * @return the directory path, never <code>null</code> or empty.
    */
   public static String getValidationDir()
   {
      return PSServer.getRxDir() + "/" + VALIDATION_RESULTS_DIR;
   }
   
   /**
    * Gets the specified validation results for specified archive ref stored on
    * the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetValidationResultsRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetValidationResultsRequest
    *    archiveRef CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetValidationResponse (PSXImportDescriptor)&gt;
    * </code></pre>
    * 
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document getValidationResults(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      String name = getRequiredAttrFromRequest(req, "archiveRef");

      // load the validation results
      File descFile = new File(getValidationDir(), name + ".xml");

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
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, une
               .getLocalizedMessage());
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!--
    *    If there are no deployable objects, then all idtype maps are returned.
    *    Otherwise the corresponding map for each deployable object is returned.
    * --&gt;
    * &lt;!ELEMENT PSXDeployGetIdTypesRequest (PSXDeployableObject*)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdTypesResponse
    *    (PSXApplicationIdTypes+)&gt;
    * </code></pre>
    * 
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
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
      Element depEl = tree
            .getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (depEl != null)
      {
         try
         {
            depList.add(new PSDeployableObject(depEl));
         }
         catch (PSUnknownNodeTypeException une)
         {
            Object[] args =
            {depEl.getTagName(), une.getLocalizedMessage()};
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
               PSDependencyManager.TYPE_SUPPORTS_ID_TYPES
                     | PSDependencyManager.TYPE_DEPLOYABLE);
      else
         deps = depList.iterator();

      // get the types for all dependencies in our list
      Iterator types = PSIdTypeManager
            .loadIdTypes(req.getSecurityToken(), deps);

      // create the response
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeployGetIdTypesResponse");
      while (types.hasNext())
      {
         PSApplicationIDTypes type = (PSApplicationIDTypes) types.next();
         root.appendChild(type.toXml(respDoc));
      }

      return respDoc;
   }

   /**
    * Stores the supplied ID types on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdTypesRequest (PSXApplicationIdTypes+)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdTypesResponse EMPTY&gt;
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
      Element idTypeEl = tree.getNextElement(
            PSApplicationIDTypes.XML_NODE_NAME,
            PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (idTypeEl == null)
      {
         Object[] msgArgs =
         {PSApplicationIDTypes.XML_NODE_NAME};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

         Object[] args =
         {doc.getDocumentElement().getTagName(), une.getLocalizedMessage()};
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
            Object[] args =
            {idTypeEl.getTagName(), ne.toString()};
            throw new PSDeployException(
                  IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

         PSIdTypeManager.saveIdTypes(idTypes);

         idTypeEl = tree.getNextElement(PSApplicationIDTypes.XML_NODE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc, "PSXDeploySaveIdTypesResponse");

      return respDoc;
   }

   /**
    * Validates local config xml from client against the localConfig.xsd.
    * Returns a list of errors if not valid.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * 
    * &lt;!ELEMENT PSXValidateLocalConfigRequest (xmlContent)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXValidateLocalConfigResponse
    *    (error*)&gt;
    * </code></pre>
    * 
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public Document validateLocalConfig(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");
      Document doc = req.getInputDocument();
      if (doc == null)
      {
         throw new PSDeployException(IPSDeploymentErrors.NULL_INPUT_DOC);
      }
      PSXmlDecoder decoder = new PSXmlDecoder();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(req.getInputDocument());
      Element xmlContent = tree.getNextElement("xmlContent");

      File tempFile = null;
      FileWriter fw = null;
      try
      {
         String content = (String) decoder.encode(PSXmlTreeWalker
               .getElementData(xmlContent));
         tempFile = PSPurgableTempFile.createTempFile("PSX", null);
         fw = new FileWriter(tempFile);
         fw.write(content);
         fw.flush();

         File xsdFile = new File(PSServer.getRxDir(),
               IPSDeployConstants.DEPLOYMENT_ROOT + "/schema/localConfig.xsd");
         List<Exception> errors = new ArrayList<Exception>();
         boolean isValid = PSXmlValidator.validateXmlAgainstSchema(tempFile,
               xsdFile, errors);

         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.createRoot(respDoc,
               "PSXValidateLocalConfigResponse");
         for (Exception ex : errors)
         {
            Element error = respDoc.createElement("error");
            error.appendChild(respDoc.createTextNode(ex.getLocalizedMessage()));
            root.appendChild(error);
         }
         return respDoc;
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .toString());
      }
      finally
      {
         if (fw != null)
            try
            {
               fw.close();
            }
            catch (IOException ignore)
            {
            }
         tempFile.delete();
      }
   }

   /**
    * Checks the server version range, package version validation, 
    * package dependencies, and verifies package wasn't built on install server.
    * All errors and warnings are returned in response doc.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployValidateArchiveRequest (PSXArchiveInfo)&gt;
    * &lt;!ATTLIST PSXDeployValidateArchiveRequest
    *    checkArchiveRef (yes | no) #REQUIRED
    *    warnOnBuidMismatch (yes | no) &quot;no&quot;
    * &gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployValidateArchiveResponse EMPTY&gt;
    * &lt;!ATTLIST ArchiveValidation
    *    warning CDATA #IMPLIED
    * &gt;
    * </code></pre>
    * 
    * Never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
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

         sTemp = getRequiredAttrFromRequest(req, "warnMissingPackageDep");
         boolean warnMissingPackageDep = sTemp.equals("yes");

         // get the PSArchiveInfo Element
         PSXmlTreeWalker tree = new PSXmlTreeWalker(req.getInputDocument());

         Element infoEl = tree.getNextElement(PSArchiveInfo.XML_NODE_NAME,
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         if (infoEl == null)
         {
            Object[] msgArgs =
            {PSArchiveInfo.XML_NODE_NAME};
            PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

            Object[] args =
            {doc.getDocumentElement().getTagName(), une.getLocalizedMessage()};
            throw new PSDeployException(IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }
         PSArchiveInfo info = new PSArchiveInfo(infoEl);
         
         PSMultiValueHashMap<String, String> validationMap = validateArchive(info, checkArchiveRef, warnOnBuildMismatch, warnMissingPackageDep);

         // create the response doc
         Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
         Element root = PSXmlDocumentBuilder.
            createRoot(respDoc, XML_AV_ROOT_NAME);
         
         
         List<String> errorList = new ArrayList<String>();
         List<String> warningList = new ArrayList<String>();
         errorList.addAll(validationMap.get(IPSDeployConstants.ERROR_KEY));
         warningList.addAll(validationMap.get(IPSDeployConstants.WARNING_KEY));
         
         Element valElement = PSXmlDocumentBuilder.addElement(
               doc, root, XML_AV_EL_NAME, null);
         if (!errorList.isEmpty())
         {   
            for (String errMessage:errorList)
            {
               valElement.setAttribute(ERROR_LEVEL,
                     IPSDeployConstants.ERROR_KEY);
               valElement.setAttribute(ERROR_MESSAGE, errMessage);     
            }
         }
         if (!warningList.isEmpty())
         {   
            for (String warMessage:warningList)
            {
               valElement.setAttribute(ERROR_LEVEL,
                     IPSDeployConstants.WARNING_KEY);
               valElement.setAttribute(ERROR_MESSAGE, warMessage);     
            }
         }
         

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
    * Checks the server version range, package version validation, 
    * package dependencies, and verifies package wasn't built on install server.
    * 
    * @param info The archive info to use
    * @param checkArchiveRef <code>true</code> to ensure the archive file 
    * @param warnOnBuildMismatch <code>true</code> to ensure the build of server that created the archive matches that of the server
    * @param warnMissingPackageDep <code>true</code> to warn if dependencies of the package are missing.
    * 
    * @return A map of validation warnings and errors, where the key is either {@link IPSDeployConstants#ERROR_KEY} or {@link IPSDeployConstants#WARNING_KEY}
    * and the value is a list of validation results, not <code>null</code>, may be empty.
    */
    public PSMultiValueHashMap<String, String> validateArchive(PSArchiveInfo info, boolean checkArchiveRef, boolean warnOnBuildMismatch,
            boolean warnMissingPackageDep)
    {
	    return validateArchive(info, checkArchiveRef, warnOnBuildMismatch, warnMissingPackageDep, false);
    }
   
   /**
    * Checks the server version range, package version validation, 
    * package dependencies, and verifies package wasn't built on install server.
    * 
    * @param info The archive info to use
    * @param checkArchiveRef <code>true</code> to ensure the archive file 
    * @param warnOnBuildMismatch <code>true</code> to ensure the build of server that created the archive matches that of the server
    * @param warnMissingPackageDep <code>true</code> to warn if dependencies of the package are missing.
    * @param shouldValidateVersion <code>false</code> if the version validation should be skipped for reverted package entries on uninstall of patch
    * 
    * @return A map of validation warnings and errors, where the key is either {@link IPSDeployConstants#ERROR_KEY} or {@link IPSDeployConstants#WARNING_KEY}
    * and the value is a list of validation results, not <code>null</code>, may be empty.
    */
    public PSMultiValueHashMap<String, String> validateArchive(PSArchiveInfo info, boolean checkArchiveRef, boolean warnOnBuildMismatch,
            boolean warnMissingPackageDep, boolean shouldValidateVersion)
    {
         Validate.notNull(info);
        
         PSMultiValueHashMap<String, String> validationMap = new PSMultiValueHashMap<String, String>();
         String message = "";
    
         // Checks if the package is already installed and if version is greater
         // then or equal to installing package.
    
         PSExportDescriptor expDesc = info.getArchiveDetail()
               .getExportDescriptor();
         IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
               .getPkgInfoService();
         PSPkgInfo pkgInfo = pkgService.findPkgInfo(expDesc.getName());
         if (pkgInfo != null)
         {
            if ((pkgInfo.getLastAction() != PSPkgInfo.PackageAction.UNINSTALL) && shouldValidateVersion)
            {
    
               // Can't install on server that package was created on
            	/* Relaxing this restriction as it doesn't make sense to me
                Developers (Percussion or 3rd Party) build packages and need to be able to upgrade.
                This rule would seem to prevent a Package Developer from ever upgrading if their package 
                is shipped with the product. I don't know if there was a technical reason for this validation error. NC - 2/27/16
            	if (pkgInfo.isCreated())
               {
                  Object[] args =
                  {expDesc.getName()};
                  message = new PSDeployException(
                        IPSDeploymentErrors.PACKAGE_CREATED_ON_SYSTEM, args)
                        .getMessage();
                  validationMap.put(IPSDeployConstants.ERROR_KEY, message);
               }
               */
               
                // check version of previously installed package
                String[] v1Split = expDesc.getVersion().split("\\.");
                String[] v2Split = pkgInfo.getPackageVersion().split("\\.");
                boolean isLowerVersion = true;
                
                ms_log.debug("Package is: " + pkgInfo.getPackageDescriptorName());
                ms_log.debug("New pkg version: " + Arrays.toString(v1Split));
                ms_log.debug("Installed pkg version: " + Arrays.toString(v2Split));
                
                if (expDesc.getVersion().equals(pkgInfo.getPackageVersion())
                        || Integer.parseInt(v1Split[0]) > Integer.parseInt(v2Split[0])
                        || Integer.parseInt(v1Split[1]) > Integer.parseInt(v2Split[1])
                        || Integer.parseInt(v1Split[2]) > Integer.parseInt(v2Split[2])) {
                    isLowerVersion = false;
                }
               
               // if the version is lower, we cannot install 
               if (isLowerVersion)
               {
                  Object[] args =
                  {expDesc.getVersion(), pkgInfo.getPackageVersion()};
                  message = new PSDeployException(
                        IPSDeploymentErrors.VERSION_LOWER_THEN_INSTALLED, args)
                        .getMessage();
                  validationMap.put(IPSDeployConstants.ERROR_KEY, message);
               }
            }
         }
    
         // validate server version and build compatibility, and archive name
         // for uniqueness.
         PSFormatVersion version = getServerVersion();
         String systemVersion = version.getVersion();
         String packageSystemMin = expDesc.getCmsMinVersion();
         String packageSystemMax = expDesc.getCmsMaxVersion();
    
         if (checkArchiveRef
               && getImportArchiveFile(info.getArchiveRef()).exists())
         {
            message = new PSDeployNonUniqueException(
                  IPSDeploymentErrors.ARCHIVE_REF_FOUND, 
                  info.getArchiveRef()).getMessage();
            validationMap.put(IPSDeployConstants.ERROR_KEY, message);
         }
    
         if (isLowerVersion(systemVersion, packageSystemMin))
         {
            Object[] args =
            {systemVersion, packageSystemMin};
            message = new PSDeployException(
                  IPSDeploymentErrors.SERVER_VERSION_LOWER, args).getMessage();
            if (warnOnBuildMismatch)
               validationMap.put(IPSDeployConstants.WARNING_KEY, message);
            else
               validationMap.put(IPSDeployConstants.ERROR_KEY, message);
         }
    
         if (message == null
               && isHigherVersion(systemVersion, packageSystemMax))
         {
            Object[] args =
            {systemVersion, packageSystemMax};
            message = new PSDeployException(
                  IPSDeploymentErrors.SERVER_VERSION_HIGHER, args).getMessage();
            if (warnOnBuildMismatch)
               validationMap.put(IPSDeployConstants.WARNING_KEY, message);
            else
               validationMap.put(IPSDeployConstants.ERROR_KEY, message);
         }
    
         // Check Package dependencies have been meet
        validatePkgDep(expDesc, warnMissingPackageDep, validationMap);
        
        return validationMap;
    }

   /**
    * Validates Package dependencies exist and if they are the save version.
    * 
    * @param expDesc - PSExportDescriptor
    * @param warnMissingPackageDep - flag to warn on missing packages
    * @param validationMap Map to add errors and warnings to
    */
   private void validatePkgDep(PSExportDescriptor expDesc,
         boolean warnMissingPackageDep, PSMultiValueHashMap<String, String> validationMap)
   {
      String message = "";
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();

      List<Map<String, String>> pkgDepList = expDesc.getPkgDepList();
      ArrayList<String> pkgNotInstalled = new ArrayList<String>();
      ArrayList<String> pkgVersionMismatch = new ArrayList<String>();

      // Loop through all dependency packages
      for (Map<String, String> pkgDep : pkgDepList)
      {
         String pkgName = pkgDep.get(PSDescriptor.XML_PKG_DEP_NAME);
         String pkgVersion = pkgDep
               .get(PSDescriptor.XML_PKG_DEP_VERSION);         

         PSPkgInfo pkgInfo = pkgService.findPkgInfo(pkgName);

         if (pkgInfo == null)
         {
            pkgNotInstalled.add(pkgName);
         }
         else if (!pkgInfo.isSuccessfullyInstalled())
         {
            pkgNotInstalled.add(pkgName);
         }
         else if (isLowerVersion(pkgInfo.getPackageVersion(), pkgVersion))
         {
            pkgVersionMismatch.add(pkgName + " " + pkgVersion);
         }         

      }

      // Error if validation fails
      if (!pkgNotInstalled.isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         for(String s : pkgNotInstalled)
         {
            sb.append(s);
            sb.append("\n");
         }
         Object[] args =
         {sb.toString()};
         message = new PSDeployException(
               IPSDeploymentErrors.PKG_DEP_VALIDATION, args).getMessage();
         if (warnMissingPackageDep)
            validationMap.put(IPSDeployConstants.WARNING_KEY, message);
         else
            validationMap.put(IPSDeployConstants.ERROR_KEY, message);
      }
      else if (!pkgVersionMismatch.isEmpty())
      {
         StringBuilder sb = new StringBuilder();
         for(String s : pkgVersionMismatch)
         {
            sb.append(s);
            sb.append("\n");
         }
         Object[] args =
         {sb.toString()};
         message = new PSDeployException(
               IPSDeploymentErrors.PKG_DEP_VERSION_VALIDATION, args).getMessage();
         validationMap.put(IPSDeployConstants.WARNING_KEY, message);
      }
   }

   /**
    * Gets the specified <code>PSDbmsMap</code> object stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDbmsMapRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetDbmsMapRequest
    *    server CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDbmsMapResponse (PSXIdMap)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveDbmsMapRequest (PSXDbmsMap)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveDbmsMapResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdMapRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetIdMapRequest
    *    sourceServer CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetIdMapResponse (PSXIdMap)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdMapRequest (PSXIdMap)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveIdMapResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
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
    *            <code>null</code>.
    * @param attrName The attribute name, it need to be exist in the
    *            <code>PSRequest</code> object, assume not <code>null</code>
    *            or empty.
    * 
    * @return The retrieved value for the specified attribute. It never be
    *         <code>null</code> or empty.
    * 
    * @throws PSDeployException if the attribute does not exist or there is any
    *             other errors.
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
         Object[] msgArgs =
         {root.getTagName(), attrName, ""};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args =
         {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }
      return attrValue;
   }

   /**
    * Gets the specified <code>PSArchiveSummary</code> stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveSummaryRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetArchiveSummaryRequest
    *    archiveLogId CDATA #IMPLIED
    *    archiveRef CDATA #IMPLIED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveSummaryResponse (PSXArchiveSummary)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive log cannot be found or there are
    *             any other errors.
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
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

      if (archiveSummary == null) // cannot find one
      {
         Object[] args =
         {"PSArchiveSummary", Integer.toString(logId)};
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveInfoRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetArchiveInfoRequest
    *    archiveLogId CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveInfoResponse (PSXArchiveInfo)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive log cannot be found or there are
    *             any other errors.
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
    */
   public Document getArchiveInfo(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      int logId = getAttrNumberFromRequest(req, "archiveLogId");
      PSArchiveSummary sum = getArchiveSummary(logId);
      PSArchiveInfo archiveInfo = getArchiveInfo(sum.getArchiveInfo()
            .getArchiveRef());

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
    *             there are any other errors.
    */
   private PSArchiveSummary getArchiveSummary(int logId)
         throws PSDeployException
   {
      PSArchiveSummary archiveSummary = m_logHandler.getArchiveSummary(logId);

      // make sure the archive summary object exists
      if (archiveSummary == null) // cannot find one
      {
         Object[] args =
         {"PSArchiveSummary", Integer.toString(logId)};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }

      return archiveSummary;
   }

   /**
    * Get the archive from the specified archive file .
    * 
    * @param archiveRef The archive ref that specifies the archive file from
    *            which the archive info is to be extracted, assumed not
    *            <code>null</code> or empty.
    * 
    * @return The archive info, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive file cannot be located, or if
    *             there are any other errors.
    */
   private PSArchiveInfo getArchiveInfo(String archiveRef)
         throws PSDeployException
   {
      File archiveFile = getImportArchiveFile(archiveRef);

      // make sure the archive file exists
      if (!archiveFile.exists())
      {
         Object[] args =
         {"PSArchive", archiveRef};
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
    * @return The map, never <code>null</code>
    */
   private PSIdMap getIdMap(PSArchiveInfo info)
   {
      PSDbmsInfo sourceDb = info.getRepositoryInfo();
      
      return m_idmapMgr.getIdmap(sourceDb.getDbmsIdentifier());
   }

   /**
    * Get a (required) number from a attribute which is specified by a given
    * attribute name and request document.
    * 
    * @param req The request which contains the attribute <code>attrName</code>,
    *            assume not <code>null</code>.
    * @param attrName The name of the attribute, assume not <code>null</code>
    *            or empty.
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
         Object[] msgArgs =
         {root.getTagName(), attrName, sNumber};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, msgArgs);

         Object[] args =
         {root.getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      return number;
   }

   /**
    * Gets the specified <code>PSLogSummary</code> stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetLogSummaryRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetLogSummaryRequest
    *    logId CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetLogSummaryResponse (PSXLogSummary)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSDeployException if the archive log cannot be found or there are
    *             any other errors.
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
    */
   public Document getLogSummary(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      int logId = getAttrNumberFromRequest(req, "logId");
      PSLogSummary logSummary = m_logHandler.getLogSummary(logId);

      if (logSummary == null) // cannot find one
      {
         Object[] args =
         {"PSLogSummary", Integer.toString(logId)};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }

      String archiveRef = logSummary.getArchiveSummary().getArchiveInfo()
            .getArchiveRef();
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
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDescriptorGuid&gt;
    * &lt;!ATTLIST PSXDescriptorGuid
    *    longValue CDATA #REQUIRED
    * </code></pre> , never <code>null</code>.
    */
   public Document createDescriptorGuid()
   {
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDescriptorGuid");
      IPSGuidManager mgr = PSGuidManagerLocator.getGuidMgr();
      IPSGuid guid = mgr.createGuid(PSTypeEnum.DEPLOYER_DESCRIPTOR_ID);
      root.setAttribute("longvalue", String.valueOf(guid.longValue()));
      return respDoc;
   }
   
   /**
    * Retrieve an index of all deployable elements and dependencies
    * to which package name they belong. Will not return index entries
    * for uninstalled packages.
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetDependencyToPackageNameIndexResponse&gt;
    * &lt;!ELEMENT PSXDependencyToPackageNameEntry&gt;
    * &lt;!ATTLIST PSXDependencyToPackageNameEntry
    *    dependencyId CDATA #REQUIRED
    *    objectType CDATA #REQUIRED
    *    package CDATA #REQUIRED
    *    version CDATA #REQUIRED
    * </code></pre> , never <code>null</code>.
    */
   public Document getDependencyToPackageNameIndex()
   {
      PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
      PSDependencyManager dm = dh.getDependencyManager();
      
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeployGetDependencyToPackageNameIndexResponse");
      
      IPSPkgInfoService pkgInfoSvc = 
         PSPkgInfoServiceLocator.getPkgInfoService();
      List<PSPkgInfo> pkgInfos = pkgInfoSvc.findAllPkgInfos();
      List<String[]> index = new ArrayList<String[]>();
      for(PSPkgInfo info : pkgInfos)
      {
        if(PackageAction.UNINSTALL.equals(info.getType()))
           continue;
        // Get elements
        List<PSPkgElement> pkgEls = pkgInfoSvc.findPkgElements(info.getGuid());
        for(PSPkgElement el : pkgEls)
        {
           addPackageIndexEntry(dm, index, 
              info.getPackageDescriptorName(), info.getPackageVersion(), 
              el.getObjectGuid());
        }
      }
      for(String[] entry : index)
      {
         Element entryEl = 
            PSXmlDocumentBuilder.addElement(
               respDoc, root, "PSXDependencyToPackageNameEntry", null);
         entryEl.setAttribute("dependencyId", entry[0]);
         entryEl.setAttribute("objectType", entry[1]);
         entryEl.setAttribute("package", entry[2]);
         entryEl.setAttribute("version", entry[3]);
      }
      return respDoc;
   }
   
   /**
    * Helper method to add a element/depend to package index
    * entry.
    * @param dm the deployment manager, assumed not <code>null</code>.
    * @param index the list of index entries, assumed not
    * <code>null</code>.
    * @param packageName the package name, assumed not 
    * <code>null</code> or empty.
    * @param packageVersion the package version, assumed not 
    * <code>null</code> or empty.
    * @param guid the object guid, assumed not <code>null</code>.
    */
   private void addPackageIndexEntry(PSDependencyManager dm,
      final List<String[]> index, final String packageName,
      final String packageVersion, final IPSGuid guid)
   {
      String dID = String.valueOf(guid.longValue());
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      List<String> dTypes = dm.getDeploymentType(type);
      if(PSIdNameHelper.isSupported(type))
      {
         dID = PSIdNameHelper.getName(guid);
      }
      for(String dtype : dTypes)
         index.add(new String[]{dID, dtype, packageName, packageVersion});
   }
   
   

   /**
    * Deletes the specified Archive file and all related archive summary and
    * package logs. An archive log id or the archive ref may be supplied.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteArchiveRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployDeleteArchiveRequest
    *    logId CDATA #IMPLIED
    *    archiveRef CDATA #IMPLIED
    * &gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteArchiveResponse EMPTY&gt;
    * </code></pre>
    * 
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>req</code> is
    *             <code>null</code>.
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
         PSArchiveSummary archiveSummary = m_logHandler
               .getArchiveSummary(logId);

         if (archiveSummary != null)
         {
            archiveRef = archiveSummary.getArchiveInfo().getArchiveRef();
         }
      }

      m_logHandler.deleteAllLogs(archiveRef);
      File archiveFile = getImportArchiveFile(archiveRef);
      archiveFile.delete();

      // create the response document for the deletion
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder
            .createRoot(respDoc, "PSXDeployDeleteArchiveResponse");

      return respDoc;
   }

   /**
    * Stores the supplied export descriptor on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveExportDescriptorRequest (PSXExportDescriptor)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveExportDescriptorResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.g
    * @throws PSDeployException if there are any other errors.
    */
   public Document saveExportDescriptor(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSExportDescriptor desc = (PSExportDescriptor) getRequiredComponentFromRequest(
            req, PSExportDescriptor.class, PSExportDescriptor.XML_NODE_NAME);

      saveExportDescToFileSystem(desc);

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeploySaveExportDescriptorResponse");

      PSUserEntry[] userEntries = PSUserSessionManager.getUserSession(req)
            .getAuthenticatedUserEntries();
      String userId;
      if (userEntries.length > 0)
      {
         // take first user entry name
         userId = userEntries[0].getName();
      }
      else
         userId = "unknown";

      updateCreatePackageInfoService(desc, userId);

      return respDoc;
   }

   /**
    * Stores the supplied export descriptor on the server.
    * 
    * @param desc descriptor to save
    * 
    * @throws PSDeployException if any error saving.
    */
   public void saveExportDescToFileSystem(PSExportDescriptor desc) throws PSDeployException
   {
      String name = desc.getName();
      File descFile = new File(EXPORT_DESC_DIR, name + ".xml");

      saveComponentToFile(descFile, EXPORT_DESC_DIR, desc);
   }
   
   /**
    * Gets a deployable component from the given request document. The component
    * is a child element of the root of the document. Validating the parameters
    * if needed.
    * 
    * @param req The request which contains the document that the deployable
    *            component is retrieved from, assume not <code>null</code>.
    * @param compClass The <code>Class</code> of the deployable component,
    *            assume not <code>null</code>.
    * @param xmlNodeName The name of the XML node that is going to be retrieved,
    *            assume not <code>null</code> or empty.
    * 
    * @return The retrieved <code>IPSDeployComponent</code> object, never
    *         <code>null</code>.
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
      Element compEl = tree
            .getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (compEl == null)
      {
         Object[] msgArgs =
         {xmlNodeName};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, msgArgs);

         Object[] args =
         {doc.getDocumentElement().getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      IPSDeployComponent comp = null;
      try
      {
         Constructor compCtor = compClass.getConstructor(new Class[]
         {Element.class});
         comp = (IPSDeployComponent) compCtor.newInstance(new Object[]
         {compEl});
      }
      catch (Exception e)
      {
         if (e instanceof PSUnknownNodeTypeException)
         {
            Object[] args =
            {compEl.getTagName(), e.toString()};
            throw new PSDeployException(
                  IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }
         else
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                  .getLocalizedMessage());
      }

      return comp;
   }

   /**
    * Deletes the specified export descriptor stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteExportDescriptorRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployDeleteExportDescriptorRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteExportDescriptorResponse EMPTY&gt;
    * </code></pre>
    * 
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
         Object[] args =
         {"Export Descriptor", name};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      descFile.delete();

      // delete the package from the repository
      IPSPkgInfoService srv = PSPkgInfoServiceLocator.getPkgInfoService();
      srv.deletePkgInfo(name);
      
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeployDeleteExportDescriptorResponse");

      return respDoc;
   }

   /**
    * Gets the <code>PSAppPolicySettings</code> object stored on the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetAppPolicySettingsRequest EMPTY&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetAppPolicySettingsResponse (PSXAppPolicySettings)&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
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
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
                  .getLocalizedMessage());
         }
      }
      else
         settings = new PSAppPolicySettings();

      return settings;
   }

   /**
    * Saves the application policy settings to the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveAppPolicySettingsRequest (PSXAppPolicySettings)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveAppPolicySettingsResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   public Document saveAppPolicySettings(PSRequest req)
         throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSAppPolicySettings policySettings = (PSAppPolicySettings) getRequiredComponentFromRequest(
            req, PSAppPolicySettings.class, PSAppPolicySettings.XML_NODE_NAME);

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
    *            assume not <code>null</code>.
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
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try
            {
               out.close();
            }
            catch (IOException ex)
            {
            }
      }
   }

   /**
    * Acquires the lock for deployment.
    * 
    * @param userId user seeking the lock. Not <code>null</code>
    *            or empty.
    * 
    * @param sessionId session id of the user. Not <code>null
    * </code>
    *            or empty.
    * 
    * @param overrideLock If <code>true</code> then the lock is acquired
    *            regardless of the locker, if
    *            <code>false</code> <code>m_lockingTime</code> is checked
    *            for expiration, if that's <code>true</code>, lock is
    *            acquired, if not, then sessionId is compared with the locker's
    *            session id <code>
    * m_sessionId</code>, if that's
    *            <code>true</code> lock is acquired or else
    *            <code>PSLockedException</code> is thrown.
    * 
    * @throws PSLockedException if the lock could not be acquired.
    */
   public void acquireLock(String userId, String sessionId,
         boolean overrideLock) throws PSLockedException
   {
      Validate.notEmpty(userId);
      Validate.notEmpty(sessionId);
      synchronized (m_mutexObject)
      {
         // lock if user is empty or the lock time is reset to 0 or the override
         // lock is true.
         if (m_lockedUser.equals("") || m_lockingTime == 0 || overrideLock)
         {
            setLockedValues(userId, sessionId, System.currentTimeMillis()
                  + LOCKING_DURATION);
         }
         else
         {
            // if lock hasn't expired
            long oneMinute = 1000 * 60; // one min of millisecs
            long remainder = m_lockingTime - System.currentTimeMillis();
            if (remainder >= oneMinute)
            {
               if (sessionId.equals(m_lockedSessionId))
                  setLockedValues(userId, sessionId, System.currentTimeMillis()
                        + LOCKING_DURATION);
               else
               {
                  String timeleft = String.valueOf((remainder) / oneMinute);
                  Object args[] = new Object[]
                  {m_lockedUser, timeleft};
                  throw new PSLockedException(
                        IPSDeploymentErrors.LOCK_ALREADY_HELD, args);
               }
            }
            else
            {
               setLockedValues(userId, sessionId, System.currentTimeMillis()
                     + LOCKING_DURATION);
            }
         }
      }
   }

   /**
    * A simple setter for setting values for locking. All changes to the locking
    * values should be made through this method.
    * 
    * @param userId user getting the lock. Assumed to be not <code>null</code>.
    * @param sessionId session id of the user getting the lock. Assumed to be
    *            not <code>null</code>.
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
    *            lock. Not <code>null</code> or empty.
    * 
    * @return If <code>true</code> then the lock has been successfully
    *         released, if not then either the lock has been overriden or was
    *         never acquired.
    */
   public boolean releaseLock(String sessionId)
   {
      Validate.notEmpty(sessionId);
      synchronized (m_mutexObject)
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
    * Matches the session id of the locked user with the one holding the lock,
    * if they are equal the lock is extended or else not.
    * 
    * @param req The request, may not be <code>null</code>.
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployExtendLockResponse&gt;
    * 
    * 
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    *             the server.
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
      synchronized (m_mutexObject)
      {
         if (lockSessId.equals(m_lockedSessionId))
         {
            setLockedValues(m_lockedUser, sessionId, System.currentTimeMillis()
                  + LOCKING_DURATION);
         }
         else if (m_lockedUser.trim().length() == 0)
         {
            // lock has been aquired by someone else and released since this
            // user's last request
            Object args[] = new Object[]
            {m_lastLockedUser};
            throw new PSLockedException(
                  IPSDeploymentErrors.LOCK_NOT_EXTENSIBLE_TAKEN_RELEASED, args);
         }
         else
         {
            // lock held by another, check to see if expired
            long oneMinute = 1000 * 60; // one min of millisecs
            long remainder = m_lockingTime - System.currentTimeMillis();
            if (remainder < oneMinute)
            {
               // lock has been aquired by someone else and has expired since
               // this user's last request
               Object args[] = new Object[]
               {m_lastLockedUser};
               throw new PSLockedException(
                     IPSDeploymentErrors.LOCK_NOT_EXTENSIBLE_TAKEN_RELEASED,
                     args);
            }
            else
            {
               String timeleft = String.valueOf((remainder) / oneMinute);
               Object args[] = new Object[]
               {m_lockedUser, timeleft};
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDisconnectRequest (PSXFormatVersion)&gt;
    * &lt;!ATTLIST PSXDeployDisconnectRequest
    *    userId CDATA #REQUIRED
    *    password CDATA #REQUIRED
    * 
    * &gt;
    * 
    * 
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDisconnectResponse&gt;
    * &lt;!ATTLIST PSXDeployDisconnectRequest
    *    lockedUser CDATA #OPTIONAL
    *    lockedUntil CDATA #OPTIONAL
    * &gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws PSAuthenticationFailedException if the user is not authenticated.
    * @throws PSAuthorizationException if the user is not authorized.
    * @throws PSServerException if there are any errors generated by calls to
    *             the server.
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
      // should be able to get the last session id used by the client. If the
      // session has timed out since their last request, this request will have
      // generated a new session id, so we need to get the old one to release
      // the lock.
      sessionId = root.getAttribute(SESSION_ID_ATTR);
      if (sessionId == null || sessionId.trim().length() == 0)
         sessionId = req.getUserSessionId();
      root = PSXmlDocumentBuilder.createRoot(respdoc,
            "PSXDeployDisconnectResponse");
      if (!releaseLock(sessionId))
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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadDependenciesRequest (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)&gt;
    * &lt;!ATTLIST PSXDeployLoadDependenciesRequest
    *    maxCount CDATA #IMPLIED
    * &gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadDependenciesResponse (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)&gt;
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
      List deps = PSDeployComponentUtils.cloneList(m_depMgr.getDependencies(req
            .getSecurityToken(), dep));

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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadAncestorsRequest (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)&gt;
    * &lt;!ATTLIST PSXDeployLoadAncestorsRequest
    *    maxCount CDATA #IMPLIED
    * &gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployLoadAncestorsResponse (PSXDeployableElement |
    *    PSXDeployableObject | PSXUserDependency)&gt;
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
      List ancs = PSDeployComponentUtils.cloneList(m_depMgr.getAncestors(req
            .getSecurityToken(), dep));

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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveUserDependencyRequest (PSXUserDependency)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveUserDependencyResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException if an error errors.
    */
   public Document saveUserDependency(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      PSUserDependency dep = (PSUserDependency) getRequiredComponentFromRequest(
            req, PSUserDependency.class, PSUserDependency.XML_NODE_NAME);

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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteUserDependencyRequest (PSXUserDependency)&gt;
    * </code></pre>
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployDeleteUserDependencyResponse EMPTY&gt;
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

      PSUserDependency dep = (PSUserDependency) getRequiredComponentFromRequest(
            req, PSUserDependency.class, PSUserDependency.XML_NODE_NAME);

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
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetArchiveFileRequest EMPTY&gt;
    * &lt;!ATTLIST PSXDeployGetArchiveFileRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    * 
    * @return <code>null</code> always, as the content has been set on the
    *         response.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If any errors occur - the status code of the
    *             response will also be set to <code>500</code>.
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
         Object[] args =
         {"Archive File", name};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_OBJECT_NOT_FOUND, args);
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
      }

      return null;
   }
   
   /**
    * Create a config defintion shell from the specified descriptor and return
    * it.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXCreateConfigDefRequest EMPTY&gt;
    * &lt;!ATTLIST PSXCreateConfigDefRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    * 
    * @return <code>null</code> always, as the content has been set on the
    *         response.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If any errors occur - the status code of the
    *             response will also be set to <code>500</code>.
    */
   public Document createConfigDef(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the descriptor name
      String name = getRequiredAttrFromRequest(req, "descName");
      PSExportDescriptor exportDesc = getDescriptor(name);
      
      ByteArrayInputStream in = null;
      String def = PSConfigDefGenerator.getInstance().generate(exportDesc);
      try
      {
         in = new ByteArrayInputStream(def.getBytes("utf8"));
         req.getResponse().setContent(in, def.length(),
               "application/octet-stream");
         in = null;

      }
      catch (Exception e)
      {
         Object[] args =
         {e.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, args);
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
      }
      return null;
      
   }
   
   /**
    * Create a default config def based on def config
    * it.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXCreateConfigDefRequest EMPTY&gt;
    * &lt;!ATTLIST PSXCreateConfigDefRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    * 
    * @return <code>null</code> always, as the content has been set on the
    *         response.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If any errors occur - the status code of the
    *             response will also be set to <code>500</code>.
    */
   public Document createDefaultConfig(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the descriptor name
      String name = getRequiredAttrFromRequest(req, "descName");
      PSExportDescriptor exportDesc = getDescriptor(name);
      
      // Get info from descriptor
      String publisherName = exportDesc.getPublisherName();
      String publisherPrefix = StringUtils.substringBefore(
            exportDesc.getName(), ".");
      String solutionName = StringUtils.substringAfter(
            exportDesc.getName(), ".");
      
      String configDefPath = TEMP_CONFIG_DIR + "/" + exportDesc.getName() + "_configDef.xml";
      
      ByteArrayInputStream in = null;
      String defCon = PSDefaultConfigGenerator.getInstance().generateDefaultConfig(
            publisherName, publisherPrefix, solutionName, configDefPath);
      try
      {
         in = new ByteArrayInputStream(defCon.getBytes("utf8"));
         req.getResponse().setContent(in, defCon.length(),
               "application/octet-stream");
         in = null;

      }
      catch (Exception e)
      {
         Object[] args =
         {e.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, args);
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
      } 
      return null;
      
   }
   
   /**
    * Create a summary from the specified descriptor and return
    * it.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXCreateDescriptorSummaryRequest EMPTY&gt;
    * &lt;!ATTLIST PSXCreateDescriptorSummaryRequest
    *    descName CDATA #REQUIRED
    * </code></pre>
    * 
    * @return <code>null</code> always, as the content has been set on the
    *         response.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If any errors occur - the status code of the
    *             response will also be set to <code>500</code>.
    */
   public Document createDescriptorSummary(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the descriptor name
      String name = getRequiredAttrFromRequest(req, "descName");
      PSExportDescriptor exportDesc = getDescriptor(name);
      
      
      
      ByteArrayInputStream in = null;
      PSDescriptorSummaryReport summary = new PSDescriptorSummaryReport();
      String report = summary.getReport(exportDesc);
      try
      {
         in = new ByteArrayInputStream(report.getBytes("utf8"));
         req.getResponse().setContent(in, report.length(),
               "application/octet-stream");
         in = null;

      }
      catch (Exception e)
      {
         Object[] args =
         {e.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.UNEXPECTED_ERROR, args);
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
      }
      return null;
      
   }


   /**
    * Uninstalls all the elements from the supplied packages, if there are no
    * package dependencies. Returns one result for each failed package element.
    * 
    * @param packageNames list of the packages that need to be uninstalled. Must
    * not be <code>null</code> or empty.
    * @return results of the uninstallation.
    */
   public List<IPSUninstallResult> uninstallPackages(List<String> packageNames)
   {
      return uninstallPackages(packageNames, false);
   }
   
   /**
    * Uninstalls all the elements from the supplied packages, if there are no
    * package dependencies. Returns one result for each failed package element.
    * 
    * @param packageNames list of the packages that need to be uninstalled. Must
    * not be <code>null</code> or empty.
    * @param isRevertEntry <code>true</code> if the package has been marked for REVERT
    * @return results of the uninstallation.
    */
   public List<IPSUninstallResult> uninstallPackages(List<String> packageNames, boolean isRevertEntry)
   {
      if (packageNames == null || packageNames.isEmpty())
         throw new IllegalArgumentException(
               "packageNames must not be null or empty");
      PSPackageUninstaller uninstaller = new PSPackageUninstaller();
      return uninstaller.uninstallPackages(packageNames, isRevertEntry);
   }
   
   /**
    * Saves an archive file to disk.
    * 
    * @param req The request, may not be <code>null</code>. Must contain at
    *            least one parameter that is a <code>File</code> object, but
    *            may contain other parameters. The first file found is saved
    *            using the value of the <code>"archiveRef"</code> parameter as
    *            the archiveRef.
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveArchiveFileResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If at least one file is not found in the request
    *             params, or any other errors occur.
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
         Object[] args =
         {"archiveRef", archiveRef == null ? "null" : archiveRef};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_PARAM_INVALID, args);
      }

      File inFile = null;
      while (params.hasNext() && inFile == null)
      {
         Map.Entry entry = (Map.Entry) params.next();
         Object val = entry.getValue();
         if (val instanceof File)
         {
            inFile = (File) val;
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
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
         if (out != null)
            try
            {
               out.close();
            }
            catch (IOException ex)
            {
            }
      }

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeploySaveArchiveFileResponse");

      return respDoc;
   }
   
   /**
    * Saves a config file to disk.
    * 
    * @param req The request, may not be <code>null</code>. Must contain at
    *            least one parameter that is a <code>File</code> object, but
    *            may contain other parameters. The first file found is saved
    *            using the value of the <code>"configRef"</code> parameter as
    *            the configRef.
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeploySaveConfigFileResponse EMPTY&gt;
    * </code></pre>
    * 
    * or an error, never <code>null</code>.
    * 
    * @throws IllegalArgumentException If <code>req</code> is
    *             <code>null</code>.
    * @throws PSDeployException If at least one file is not found in the request
    *             params, or any other errors occur.
    */
   public Document saveConfigFile(PSRequest req) throws PSDeployException
   {
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      // get the file to save
      Iterator params = req.getParametersIterator();

      String configRef = req.getParameter("configRef");
      if (configRef == null || configRef.trim().length() == 0)
      {
         Object[] args =
         {"configRef", configRef == null ? "null" : configRef};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_PARAM_INVALID, args);
      }

      File inFile = null;
      while (params.hasNext() && inFile == null)
      {
         Map.Entry entry = (Map.Entry) params.next();
         Object val = entry.getValue();
         if (val instanceof File)
         {
            inFile = (File) val;
         }
      }

      FileInputStream in = null;
      FileOutputStream out = null;
      try
      {
         File configFile = getConfigTempFile(configRef);
         configFile.getParentFile().mkdirs();
         out = new FileOutputStream(configFile);
         in = new FileInputStream(inFile);
         IOTools.copyStream(in, out);
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
         if (out != null)
            try
            {
               out.close();
            }
            catch (IOException ex)
            {
            }
      }

      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeploySaveCOnfigFileResponse");

      return respDoc;
   }

   /**
    * Gets the featureset from the server.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetFeatureSetRequest EMPTY&gt;
    * </code></pre>
    * 
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetFeatureSetResponse (PSXFeatureSet)&gt;
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
         Element respRoot = PSXmlDocumentBuilder.createRoot(respDoc,
               "PSXDeployGetFeatureSetResponse");

         /*
          * now try to load the file - if we don't find it, we'll return an
          * empty featureset
          */
         if (fsFile.exists())
         {
            fIn = new FileInputStream(fsFile);
            Document featureDoc = PSXmlDocumentBuilder.createXmlDocument(fIn,
                  false);
            Node importNode = respDoc.importNode(featureDoc
                  .getDocumentElement(), true);
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
         Object[] args =
         {e.getLocalizedMessage()};
         PSServerException se = new PSServerException(
               IPSObjectStoreErrors.FEATURE_SET_LOAD_EXCEPTION, args);
         PSDeployException de = new PSDeployException(se);
         throw de;
      }
      finally
      {
         if (fIn != null)
            try
            {
               fIn.close();
            }
            catch (Exception e)
            { /* ignore */
            }
      }

      return respDoc;

   }

   /**
    * Gets the parent types for each dependency type that supports parent ids.
    * 
    * @param req The request, may not be <code>null</code>. Input document is
    *            expected to contain the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetParentTypesRequest EMPTY&gt;
    * </code></pre>
    * 
    * 
    * @return A document containing the following format:
    * 
    * <pre><code>
    * &lt;!ELEMENT PSXDeployGetParentTypesResponse (entry*)&gt;
    * &lt;!ELEMENT entry  EMPTY&gt;
    * &lt;!ATTLIST entry
    *    childType CDATA #REQUIRED
    *    parentType CDATA #REQUIRED
    * &gt;
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
      Element respRoot = PSXmlDocumentBuilder.createRoot(respDoc,
            "PSXDeployGetParentTypesResponse");
      Map types = m_depMgr.getParentTypes();
      Iterator entries = types.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry) entries.next();
         Element entryEl = PSXmlDocumentBuilder.addEmptyElement(respDoc,
               respRoot, "entry");
         entryEl.setAttribute("childType", (String) entry.getKey());
         entryEl.setAttribute("parentType", (String) entry.getValue());
      }

      return respDoc;

   }

   /**
    * Get this instance of the deployment handler
    * 
    * @return The single instance of the deployment handler running on the
    *         server, or <code>null</code> if one has not been created.
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
    * @param archiveRef The archive ref, may not be <code>null</code> or
    *            empty.
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

      return new File(archiveDir, archiveRef
            + IPSDeployConstants.ARCHIVE_EXTENSION);
   }
   
   /**
    * Get the config temp file reference for the specified config.
    * 
    * @param configRef The config ref, may not be <code>null</code> or
    *            empty.
    * 
    * @return The file, may or may not exist, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if <code>configRef</code> is invalid.
    */
   public File getConfigTempFile(String configRef)
   {
      if (configRef == null || configRef.trim().length() == 0)
         throw new IllegalArgumentException(
               "archiveRef may not be null or empty");

      File configTempDir = new File(TEMP_CONFIG_DIR);

      return new File(configTempDir, configRef
            + ".xml");
   }

   /**
    * Get the archive file reference for the specified export archive ref.
    * 
    * @param archiveRef The archive ref, may not be <code>null</code> or
    *            empty.
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

      return new File(archiveDir, archiveRef
            + IPSDeployConstants.ARCHIVE_EXTENSION);
   }

   // Methods generated from interface IPSLoadableRequestHandler
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
         Object[] args =
         {getName(), e.getLocalizedMessage()};
         throw new PSServerException(
               IPSServerErrors.LOADABLE_HANDLER_UNEXPECTED_EXCEPTION, args);
      }

   }

   // Methods generated from implementation of interface IPSRootedHandler
   /**
    * Get the name of the rooted handler. Used by the server to identify the
    * handler during intilization and when reporting information about all
    * rooted handlers. All rooted handlers will be served by rhythmyx at
    * runtime.
    * 
    * @return the handler name, should be unique across all rooted handlers,
    *         never <code>null</code> or empty. If <code>null</code> or
    *         empty the server will ignore this handler. If not unique, the
    *         results will be unpredictable as to which handler will receive the
    *         request for processing.
    */
   public String getName()
   {
      return DEPLOY_SUBSYSTEM;
   }

   /**
    * Get all request roots of the rooted handler. Called by the server when it
    * is initializing the handler.
    * 
    * @return an iterator over one or more request roots as Strings. The
    *         iterator must contain at least one entry, and should not contain
    *         duplicates. Never <code>null</code> or empty. If
    *         <code>null</code> or empty the server will ignore this handler.
    */
   public Iterator getRequestRoots()
   {
      return m_requestRoots.iterator();
   }

   // Methods generated from implementation of interface IPSRequestHandler
   public void processRequest(PSRequest request)
   {
      Document respDoc = null;

      String reqType = request
            .getCgiVariable(IPSCgiVariables.CGI_PS_REQUEST_TYPE);
      String subReqType;

      try
      {
         if (reqType == null || !reqType.startsWith("deploy-"))
         {
            throw new PSDeployException(
                  IPSDeploymentErrors.INVALID_REQUEST_TYPE, reqType == null
                        ? ""
                        : reqType);
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
            else if (subReqType.equals("catalog"))
               respDoc = PSCatalogHandler.processRequest(request);
            else if (subReqType.equals("getExportDescriptor"))
               respDoc = getExportDescriptor(request);
            else if (subReqType.equals("saveExportDescriptor"))
               respDoc = saveExportDescriptor(request);
            else if (subReqType.equals("saveArchiveFile"))
               respDoc = saveArchiveFile(request);
            else if (subReqType.equals("saveConfigFile"))
               respDoc = saveConfigFile(request);
            else if (subReqType.equals("getArchiveFile"))
               respDoc = getArchiveFile(request);
            else if (subReqType.equals("saveUserDependency"))
               respDoc = saveUserDependency(request);
            else if (subReqType.equals("deleteUserDependency"))
               respDoc = deleteUserDependency(request);
            else if (subReqType.equals("deleteExportDescriptor"))
               respDoc = deleteExportDescriptor(request);
            else if (subReqType.equals("loadDependencies"))
               respDoc = loadDependencies(request);
            else if (subReqType.equals("loadAncestors"))
               respDoc = loadAncestors(request);
            else if (subReqType.equals("getIdMap"))
               respDoc = getIdMap(request);
            else if (subReqType.equals("saveIdMap"))
               respDoc = saveIdMap(request);
            else if (subReqType.equals("getArchiveSummary"))
               respDoc = getArchiveSummary(request);
            else if (subReqType.equals("getArchiveInfo"))
               respDoc = getArchiveInfo(request);
            else if (subReqType.equals("getLogSummary"))
               respDoc = getLogSummary(request);
            else if (subReqType.equals("deleteArchive"))
               respDoc = deleteArchive(request);
            else if (subReqType.equals("getDbmsMap"))
               respDoc = getDbmsMap(request);
            else if (subReqType.equals("saveDbmsMap"))
               respDoc = saveDbmsMap(request);
            else if (subReqType.equals("getDeployableElements"))
               respDoc = getDeployableElements(request);
            else if (subReqType.equals("getIdTypes"))
               respDoc = getIdTypes(request);
            else if (subReqType.equals("saveIdTypes"))
               respDoc = saveIdTypes(request);
            else if (subReqType.equals("validateLocalConfig"))
               respDoc = validateLocalConfig(request);
            else if (subReqType.equals("validateArchive"))
               respDoc = validateArchive(request);
            else if (subReqType.equals("saveAppPolicySettings"))
               respDoc = saveAppPolicySettings(request);
            else if (subReqType.equals("getAppPolicySettings"))
               respDoc = getAppPolicySettings(request);
            else if (subReqType.equals("getFeatureSet"))
               respDoc = getFeatureSet(request);
            else if (subReqType.equals("getDependencies"))
               respDoc = getDependencies(request);
            else if (subReqType.equals("getParentTypes"))
               respDoc = getParentTypes(request);
            else if (subReqType.equals("getValidationResults"))
               respDoc = getValidationResults(request);
            else if (subReqType.equals("createDescriptorGuid"))
               respDoc = createDescriptorGuid();
            else if (subReqType.equals("getDependencyToPackageNameIndex"))
               respDoc = getDependencyToPackageNameIndex();
            else if (subReqType.equals("createConfigDef"))
               respDoc = createConfigDef(request);
            else if (subReqType.equals("createDefaultConfig"))
               respDoc = createDefaultConfig(request);
            else if (subReqType.equals("createDescriptorSummary"))
               respDoc = createDescriptorSummary(request);
            else
            {
               throw new PSDeployException(
                     IPSDeploymentErrors.INVALID_REQUEST_TYPE, reqType);
            }
         }
      }
      catch (Exception e)
      {
    	  String msg="An unexpected error occurred while processing the Request.";
    	  if(respDoc != null){
    		  try{
    			  msg = msg + " Request Source is: " + PSXMLDomUtil.toString(respDoc.getDocumentElement());
    		  }catch(Exception ex){}
    	  }
    	  
    	  ms_log.warn(msg, e);
          
    	  try{
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
	               if (clientSessId != null
	                     && !clientSessId.equals(request.getUserSessionId()))
	               {
	                  try{
	                	  releaseLock(clientSessId);
	                  }catch(Exception exSession){
	                	  msg = "An unexpected exception occurred while releasing a Session lock.";
	                	  if(clientSessId!= null)
	                		  msg = msg + " For Session Id: " + clientSessId;
	                	  
	                	  ms_log.warn(msg,exSession);
	                  }
	               }
	            }
	         }
	
	         // Convert to xml response
	         PSDeployException de = null;
	         if (e instanceof PSDeployException)
	            de = (PSDeployException) e;
	         else if (e instanceof PSException)
	            de = new PSDeployException((PSException) e);
	         else
	         {
	            de = new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
	                  .getLocalizedMessage());
	         }
	
	         respDoc = PSXmlDocumentBuilder.createXmlDocument();
	         Element respEl = de.toXml(respDoc);
	         PSXmlDocumentBuilder.replaceRoot(respDoc, respEl);
	         request.getResponse().setStatus(500);
          }catch(RuntimeException ex){
        	  ms_log.warn("An unexpected error occurred while handling an error in the request.",ex);
        	  //At this point this is an actual runtime exception, so we should throw it up the chain.
        	  throw ex;
          }
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
    *         password is <code>null</code> or empty.
    */
   public String decryptPwd(String uid, String pwd)
   {
      if (pwd == null || pwd.trim().length() == 0)
         return "";

      String key = uid == null || uid.trim().length() == 0
            ? PSLegacyEncrypter.INVALID_DRIVER()
            : uid;

      return decryptPwd(pwd, PSLegacyEncrypter.INVALID_CRED(), key);
   }

   /**
    * Decrypts the supplied password if it is non-<code>null</code> and not
    * empty.
    * 
    * @param pwd The password to decrypt, may be <code>null</code> or empty.
    * @param key1 The part one key to use, assumed not <code>null</code> or
    *            empty.
    * @param key2 The part two key to use, assumed not <code>null</code> or
    *            empty.
    * 
    * @return The decrypted password, or an empty string if the supplied
    *         password is <code>null</code> or empty.
    */
   private String decryptPwd(String pwd, String key1, String key2)
   {
      String ret = pwd;

      if (pwd == null || pwd.trim().length() == 0)
         return "";

      try{
         ret = PSEncryptor.getInstance().decrypt(pwd);
      } catch (PSEncryptionException e) {
        ret = PSCryptographer.decrypt(key1, key2, pwd);
      }
      return ret;
   }

   /**
    * Get the document containing the content of the specified file from disk.
    * 
    * @param docFile The <code>File</code> object that the document is
    *            retrieved from, may not be <code>null</code>.
    * @param docDescription The description of the document, may not be
    *            <code>null</code>.
    * 
    * @return The retrieved document, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any parameter is invalid.
    * @throws PSDeployException if the file does not exist or there is an error.
    */
   public static Document getDocumentFromFile(File docFile,
         String docDescription) throws PSDeployException
   {
      if (docFile == null)
         throw new IllegalArgumentException("docFile may not be null.");

      if (docDescription == null || docDescription.trim().length() == 0)
         throw new IllegalArgumentException(
               "docDescription may not be null or empty.");

      if (!docFile.exists())
      {
         Object[] args =
         {docDescription, docFile.getAbsolutePath()};
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
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
               .getLocalizedMessage());
      }
      finally
      {
         if (in != null)
            try
            {
               in.close();
            }
            catch (IOException ex)
            {
            }
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
         throw new PSDeployException(
               IPSDeploymentErrors.MAX_DEP_COUNT_EXCEEDED, String
                     .valueOf(maxCount));
      }
   }

   /**
    * Get the max dependency return count. Checks the supplied element for an
    * attribute <code>maxCount</code> and tries to parse that value. If so,
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
      Element depEl = tree
            .getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
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
            Object[] args =
            {doc.getDocumentElement().getTagName(), e.getLocalizedMessage()};
            throw new PSDeployException(
                  IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }

      }

      if (dep == null)
      {
         String badEl = depEl == null ? "null" : depEl.getTagName();
         Object[] msgArgs =
         {PSDependency.XML_NODE_NAME, badEl};
         PSUnknownNodeTypeException une = new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, msgArgs);

         Object[] args =
         {doc.getDocumentElement().getTagName(), une.getLocalizedMessage()};
         throw new PSDeployException(
               IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
      }

      return dep;
   }
   
   /**
    * Helper method to return an export descriptor by name.
    * @param name may be <code>null</code>.
    * @return the descriptor or <code>null</code> if not found.
    * @throws PSDeployException
    */
   private PSExportDescriptor getDescriptor(String name) throws PSDeployException
   {
      PSExportDescriptor exportDesc = null;
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
            Object[] args =
            {exportDescEl.getTagName(), une.getLocalizedMessage()};
            throw new PSDeployException(
                  IPSDeploymentErrors.SERVER_REQUEST_MALFORMED, args);
         }
         exportDesc.clearMissingPackages();
      }
      return exportDesc;
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
    * Checks version 1 is a higher version the version 2. Allowed formats: n.n.n
    * The comparison is based on major and minor versions only.
    * 
    * @param s1 version 1 to compare, assumed not <code>null</code>.
    * @param s2 version 2 to compare, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if version 1 is higher than version 2,
    * <code>false</code> otherwise.
    */
   private boolean isHigherVersion(String s1, String s2)
   {
      String delims = "[.]";
      String[] sv1 = s1.split(delims);
      String[] sv2 = s2.split(delims);
      int sv1int = Integer.parseInt(sv1[0]);
      int sv2int = Integer.parseInt(sv2[0]);

      if (sv1int > sv2int)
      {
         return true;
      }
      else if (sv1int == sv2int && 
            Integer.parseInt(sv1[1]) > Integer.parseInt(sv2[1]))
      {
         return true;
      }
    
      return false;
   }

   /**
    * Checks version 1 is a lower version the version 2. Allowed format: n.n.n
    * 
    * @param s1 version 1 to compare, assumed not <code>null</code>.
    * @param s2 version 2 to compare, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if version 1 is lower than version 2,
    * <code>false</code> otherwise.
    */
   private boolean isLowerVersion(String s1, String s2)
   {
      String delims = "[.]";
      String[] sv1 = s1.split(delims);
      String[] sv2 = s2.split(delims);
      int sv1int0 = Integer.parseInt(sv1[0]);
      int sv2int0 = Integer.parseInt(sv2[0]);
      int sv1int1 = Integer.parseInt(sv1[1]);
      int sv2int1 = Integer.parseInt(sv2[1]);

      if (sv1int0 < sv2int0)
      {
         return true;
      }
      else if (sv1int0 == sv2int0 && sv1int1 < sv2int1)
      {
         return true;
      }
      else if (sv1int0 == sv2int0 && sv1int1 == sv2int1 &&
            Integer.parseInt(sv1[2]) < Integer.parseInt(sv2[2]))
      {
         return true;
      }

      return false;
   }

   /**
    * Adds created package information to the PKG tables.
    * 
    * @param desc - PSExportDescriptor, assumed not <code>null</code>.
    * @param installerName, current logged in user. assumed not <code>null</code>.
    * 
    */
   private void updateCreatePackageInfoService(PSExportDescriptor desc,
         String installerName)
   {

      IPSPkgInfoService pkgInfoService = PSPkgInfoServiceLocator
            .getPkgInfoService();

      // make sure all dependencies are in the descriptor
      PSPkgInfo info = pkgInfoService.findPkgInfo(desc.getName());
      
      PSPkgInfo pkgInfo;
      if (info == null)
         pkgInfo = pkgInfoService.createPkgInfo(desc.getName());
      else
      {
         pkgInfo = pkgInfoService.loadPkgInfoModifiable(info.getGuid());
         //clear existing children - move to pkg business svc
         List<IPSGuid> elems = pkgInfoService.findPkgElementGuids(pkgInfo
               .getGuid());
         for (IPSGuid guid : elems)
            pkgInfoService.deletePkgElement(guid);
         List<PSPkgDependency> deps = pkgInfoService.loadPkgDependencies(pkgInfo
               .getGuid(), true);
         for (PSPkgDependency dep : deps)
            pkgInfoService.deletePkgDependency(dep.getId());
      }
      pkgInfo.setPublisherName(desc.getPublisherName());
      pkgInfo.setPublisherUrl(desc.getPublisherUrl());
      pkgInfo.setCmVersionMinimum(desc.getCmsMinVersion());
      pkgInfo.setCmVersionMaximum(desc.getCmsMaxVersion());
      pkgInfo.setPackageDescription(desc.getDescription());
      pkgInfo.setPackageVersion(desc.getVersion());
      pkgInfo.setLastActionByUser(installerName);
      pkgInfo.setPackageDescriptorGuid(new PSGuid(new Long(desc.getId())));

      // Save package info object
      pkgInfoService.savePkgInfo(pkgInfo);

      // Create and save all elements
      // N.B. - these 'packages' are the old MSM concept, not the new concept
      Set<IPSGuid> savedObjs = new HashSet<IPSGuid>();
      Iterator<PSDeployableElement> it = desc.getPackages();
      while (it.hasNext())
      {
         PSDeployableElement elem = it.next();
         Collection<PSDependency> deps = new ArrayList<PSDependency>();
         getIncludedDependencies(elem.getDependencies(), deps);
         for (PSDependency d : deps)
         {
            if (d.supportsParentId())
            {
               // only care about parent
               continue;
            }
            
            IPSGuid guid = PSIdNameHelper.getGuid(d.getDependencyId(), 
                  m_depMgr.getGuidType(d.getObjectType()));
            if (savedObjs.contains(guid))
               continue; // package element already exists for object
            
            PSPkgElement pElem = pkgInfoService
                  .createPkgElement(pkgInfo.getGuid());
            pElem.setObjectGuid(guid);
            
            pkgInfoService.savePkgElement(pElem);
            savedObjs.add(guid);  
         }
         
      }
      
      List<Map<String, String>> pkgDeps = desc.getPkgDepList();
      Map<String, IPSGuid> pkgNameToGuid = new HashMap<String, IPSGuid>();
      if (pkgDeps.size() > 0)
         pkgNameToGuid = getPkgNameToGuidMap();
      for (Map<String, String> pkgDep : pkgDeps)
      {
         PSPkgDependency dep = pkgInfoService.createPkgDependency();
         dep.setOwnerPackageGuid(pkgInfo.getGuid());
         dep.setDependentPackageGuid(
               pkgNameToGuid.get(pkgDep.get(PSDescriptor.XML_PKG_DEP_NAME)));
         dep.setImpliedDep(
               new Boolean(pkgDep.get(PSDescriptor.XML_PKG_DEP_IMPLIED)));
         pkgInfoService.savePkgDependency(dep);
      }
      
      // Set Action status to Success
      PSPkgInfo pkgInfoModify = pkgInfoService.loadPkgInfoModifiable(pkgInfo
            .getGuid());
      pkgInfoModify.setLastActionStatus(PackageActionStatus.SUCCESS);
      pkgInfoService.savePkgInfo(pkgInfoModify);
   }

   /**
    * Queries the package service to get all packages and builds a map of the
    * name to the package guid.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   private Map<String, IPSGuid> getPkgNameToGuidMap()
   {
      IPSPkgInfoService svc = PSPkgInfoServiceLocator
         .getPkgInfoService();
      List<PSPkgInfo> infos = svc.findAllPkgInfos();
      Map<String, IPSGuid> results = new HashMap<String, IPSGuid>();
      for (PSPkgInfo info : infos)
      {
         results.put(info.getPackageDescriptorName(), info.getGuid());
      }
      return results;
   }

   /**
    * Recursive method that walks the dependency graph of each supplied dep and
    * adds each one found to the supplied <code>results</code> collection, if
    * and only if it's <code>isIncluded</code> flag is <code>true</code>.
    * 
    * @param deps Assumed not <code>null</code>.
    * @param results Each supplied and found dependency is added to this set.
    * Assumed not <code>null</code>.
    */
   private void getIncludedDependencies(Iterator<PSDependency> deps,
         Collection<PSDependency> results)
   {
      while (deps.hasNext())
      {
         PSDependency dep = deps.next();
         //prevent infinite loops
         if (results.contains(dep))
            continue;
         if (dep.isIncluded())
            results.add(dep);
         if (dep.getDependencies() != null)
            getIncludedDependencies(dep.getDependencies(), results);
      }
   }

   /**
    * Constant for deployment subsystem to use for console and logging messages.
    */
   public static final String DEPLOY_SUBSYSTEM = "Deployer";

   /**
    * Directory below the server directory containing all objectstore files.
    */
   public static final String OBJECTSTORE_DIR = IPSDeployConstants.SERVER_DIR
         + "/objectstore";

   /**
    * Directory below the objectstore directory containing all DbmsMap files.
    */
   public static final File DBMSMAP_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + OBJECTSTORE_DIR, "DbmsMap");

   /**
    * Directory below the SERVER directory containing all config files.
    */
   public static final File CFG_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + IPSDeployConstants.SERVER_DIR, "cfg");

   /**
    * Directory below the objectstore directory containing all export
    * descriptors.
    */
   public static final File EXPORT_DESC_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + OBJECTSTORE_DIR, "ExportDescriptors");

   /**
    * Directory below the server directory containing all temporary export
    * archive files.
    */
   public static final String EXPORT_ARCHIVE_DIR = PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + IPSDeployConstants.SERVER_DIR + "/ExportArchives";

   /**
    * Directory below the server directory containing all archive files.
    */
   public static final String IMPORT_ARCHIVE_DIR = PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + IPSDeployConstants.SERVER_DIR + "/ImportArchives";
   
   /**
    * Directory below the server directory containing all temp config files.
    */
   public static final String TEMP_CONFIG_DIR = PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + IPSDeployConstants.SERVER_DIR + "/TempConfigs";

   /**
    * Directory below the objectstore directory containing all idTypes files.
    */
   public static final File IDTYPE_DIR = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + OBJECTSTORE_DIR, "IdTypes");

   /**
    * The <code>File</code> object for policy settings.
    */
   public static final File POLICY_SETTINGS_FILE = new File(PSServer.getRxDir()
         .getAbsolutePath()
         + "/" + OBJECTSTORE_DIR, "DeploymentPolicySettings.xml");

   /**
    * Directory below the server directory containing all validation results.
    */
   public static final String VALIDATION_RESULTS_DIR = IPSDeployConstants.SERVER_DIR
         + "/ValidationResults";

   /**
    * Mutex preventing concurrent access to a critical section by having threads
    * synchronize on it. Never modified.
    */
   private Object m_mutexObject = new Object();

   /**
    * Duration for which lock is held in milliseconds, currently 30 minutes.
    */
   private static long LOCKING_DURATION = IPSDeployConstants.LOCK_EXPIRATION_DURATION * 60 * 1000;

   /**
    * Attribute used to pass session id in the request doc as well as the
    * cookie. Used to determine the previous session id of the client if the
    * session has timed out and a new session id is generated by the request.
    */
   private static final String SESSION_ID_ATTR = "sessionId";

   /**
    * Attribute used to inform the client whether the server is licensed for
    * Multi-Server manager or not. Valid values for this attribute are "yes" or
    * "no".
    */
   private static final String LICENSED_ATTR = "licensed";
   
   /**
    * Error message Key
    */
   public final static String ERROR_MESSAGE = "errormessage"; 
   
   /**
    * Error Level Key
    */
   public final static String ERROR_LEVEL = "errorlevel";
   
   /**
    * XML validation element name
    */
   public final static String XML_AV_EL_NAME = "ArchiveValidation";
   
   /**
    * XML validation root name
    */
   public final static String XML_AV_ROOT_NAME = 
      "PSXDeployValidateArchiveResponse";

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
    * Represents time until the expiration for which the lock will be held. Lock
    * expires 30 minutes after the acquisition of the lock. If lock is acquired
    * at time t in milliseconds then lock will be held until this (t+30*60*1000)
    * time. Set and modified in {@link #acquireLock(String, String, boolean)
    * execute(userName, sessionId, overrideLock)}.
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
    * <code>init()</code> method, never <code>null</code>, empty, or
    * modified after that.
    */
   private Collection m_requestRoots;

   /**
    * Singleton instance of the deployment handler. Not <code>null</code>
    * after call to ctor by the server. Does not stricly enforce the singleton
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
    * IdMap manager used to handle all PSIdMap related operations. Initialized
    * during <code>init()</code>, never <code>null</code> after that.
    */
   private PSIdMapManager m_idmapMgr;

   /**
    * The log handler for processing log table related operations.
    */
   PSLogHandler m_logHandler;

}
