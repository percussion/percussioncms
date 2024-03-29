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

package com.percussion.deployer.server.dependencies;

import com.percussion.cms.handlers.PSGlobalTemplateUpdateHandler;
import com.percussion.conn.PSServerException;
import com.percussion.debug.PSDebugManager;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSNotLockedException;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.design.objectstore.PSUrlRequest;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.error.IPSDeploymentErrors;
import com.percussion.error.PSDeployException;
import com.percussion.extension.PSExtensionRef;
import com.percussion.fastforward.globaltemplate.PSGlobalTemplate;
import com.percussion.fastforward.globaltemplate.PSRxGlobals;
import com.percussion.security.PSAuthorizationException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSCustomControlManager;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.IOTools;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.tools.PSPatternMatcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Base class for handlers that deploy application objects.
 */
public abstract class PSAppObjectDependencyHandler 
   extends PSIdTypeDependencyHandler
{
   /**
    * Construct a dependency handler.
    * 
    * @param def The def for the type supported by this handler.  May not be
    * <code>null</code> and must be of the type supported by this class.  See
    * {@link #getType()} for more info.
    * @param dependencyMap The full dependency map.  May not be 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSAppObjectDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   
   /**
    * Get the app object for the specified name.
    * 
    * @param tok The security token to use to get the app, assumed not 
    * <code>null</code>.
    * @param appName The name, assumed not <code>null</code> or empty.
    * 
    * @return The app, may not be <code>null</code>
    * 
    * @throws PSDeployException If there are any errors or if the app is not 
    * found.
    */
   public static PSApplication getApplication(PSSecurityToken tok, 
      String appName) throws PSDeployException
   {
      PSApplication app = null;
      // even though all we need is the id, we have to make sure it exists
      try 
      {
         app = PSServerXmlObjectStore.getInstance().getApplicationObject(
            appName, tok);
      }
      catch (PSNotFoundException e) 
      {
         // rethrow an exception
         Object[] args = {appName};
         throw new PSDeployException(
               IPSDeploymentErrors.APP_DEFINITION_DOESNOT_EXIST, args);
      }
      catch (Exception e)
      {
         // all others are bad
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }      
      return app;      
   }
   
   
   /**
    * Adds dependencies for all extensions, support files, and child 
    * applications to the supplied set.  Locates support files and apps that are 
    * referenced by any text literal or url request.
    * 
    * @param tok The security token to use for creating dependencies, may not be
    * <code>null</code>.
    * @param childDeps The set to add the dependencies, may not be
    * <code>null</code>.
    * @param srcNode The element to search, may not be <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors creating the 
    * dependencies.
    */
   protected void addApplicationDependencies(PSSecurityToken tok, Set childDeps, 
      Element srcNode) throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (childDeps == null)
         throw new IllegalArgumentException("childDeps may not be null");
      
      if (srcNode == null)
         throw new IllegalArgumentException("srcNode may not be null");
         
      childDeps.addAll(getExtensionDependencies(tok, srcNode));   
      childDeps.addAll(getUrlDependencies(tok, srcNode));   
      childDeps.addAll(getLiteralPathDependencies(tok, srcNode));   
   }
   
   /**
    * Get all extension dependencies from the supplied element.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param srcNode The element to search, may not be <code>null</code>.
    * 
    * @return A List of zero or more <code>PSDependency</code> objects,
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected List getExtensionDependencies(PSSecurityToken tok, 
      Element srcNode) throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (srcNode == null)
         throw new IllegalArgumentException("srcNode may not be null");
         
      List childDeps = new ArrayList();
         
      NodeList calls = srcNode.getElementsByTagName(PSExtensionCall.ms_NodeType);
      for (int i = 0; i < calls.getLength(); i++) 
      {
         Element callEl = (Element)calls.item(i);
         PSExtensionCall call = null;
         try
         {
             call = new PSExtensionCall(callEl, null, null);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // unlikely
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
         }
         
         PSExtensionRef ref = call.getExtensionRef();
         
         // add the extension dependency
         PSDependencyHandler handler = getDependencyHandler(
           PSExitDefDependencyHandler.DEPENDENCY_TYPE);
         PSDependency exitDep = handler.getDependency(tok, ref.getFQN());
         if (exitDep != null)
         {
            if (exitDep.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               exitDep.setIsAssociation(false);
            }
            
            childDeps.add(exitDep);
         }
            
         // now see if we have an app referenece
         String extName = ref.getExtensionName();
         if (extName.startsWith("sys_Make"))
         {
            if (extName.endsWith("Link") || extName.endsWith("LinkSecure"))
            {
               // first param is url that may contain appname in form 
               // "../appname/page"
               PSExtensionParamValue[] values = call.getParamValues();
               if (values.length > 0)
               {
                  String url = values[0].getValue().getValueText();
                  StringTokenizer toker = new StringTokenizer(url, "/");
                  if (toker.hasMoreTokens() && toker.nextToken().equals("..") && 
                     toker.hasMoreTokens())
                  {
                     // next token is appname
                     PSDependency dep = getAppDepHandler().getDependency(tok,
                           toker.nextToken());
                     if (dep != null)
                     {
                        if (dep.getDependencyType() == PSDependency.TYPE_SHARED)
                        {
                           dep.setIsAssociation(false);                           
                        }
                        
                        childDeps.add(dep);
                     }
                  }
               }
            }
         }
      }
         
      return childDeps;
   }
   
   /**
    * Get all url dependencies from the supplied element.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param srcNode The element to search, may not be <code>null</code>.
    * 
    * @return An List of zero or more <code>PSDependency</code> objects,
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected List getUrlDependencies(PSSecurityToken tok, 
      Element srcNode) throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (srcNode == null)
         throw new IllegalArgumentException("srcNode may not be null");
         
      List childDeps = new ArrayList();
         
      NodeList urls = srcNode.getElementsByTagName(PSUrlRequest.XML_NODE_NAME);
      for (int i = 0; i < urls.getLength(); i++) 
      {
         Element urlEl = (Element)urls.item(i);
         PSUrlRequest urlReq = null;
         try
         {
             urlReq = new PSUrlRequest(urlEl, null, null);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // unlikely
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
         }
         
         String href = urlReq.getHref();
         if (href.trim().length() > 0)
         {
            PSDependency childDep = getDepFromPath(tok, href);
            if (childDep != null)
            {
               if (childDep.getDependencyType() == PSDependency.TYPE_SHARED)
               {
                  childDep.setIsAssociation(false);
               }
               
               childDeps.add(childDep);
            }
         }
      }
         
      return childDeps;
   }
   
   /**
    * Get all literal path dependencies from the supplied element.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param srcNode The element to search, may not be <code>null</code>.
    * 
    * @return An List of zero or more <code>PSDependency</code> objects,
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected List getLiteralPathDependencies(PSSecurityToken tok, 
      Element srcNode) throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (srcNode == null)
         throw new IllegalArgumentException("srcNode may not be null");
         
      List childDeps = new ArrayList();
         
      NodeList lits = srcNode.getElementsByTagName(PSTextLiteral.ms_NodeType);
      for (int i = 0; i < lits.getLength(); i++) 
      {
         Element litEl = (Element)lits.item(i);
         PSTextLiteral lit = null;
         try
         {
             lit = new PSTextLiteral(litEl, null, null);
         }
         catch (PSUnknownNodeTypeException e)
         {
            // unlikely
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
               e.getLocalizedMessage());
         }
         
         String litVal = lit.getText();
         if (litVal.trim().length() > 0)
         {
            PSDependency childDep = getDepFromPath(tok, litVal);
            if (childDep != null)
            {
               if (childDep.getDependencyType() == PSDependency.TYPE_SHARED)
               {
                  childDep.setIsAssociation(false);
               }
               
               childDeps.add(childDep);
            }
         }
      }
         
      return childDeps;
   }
   
   
   /**
    * Get all dependencies from the supplied stylesheet.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param doc The stylesheet parsed as an XML document, may not be 
    * <code>null</code>.
    * 
    * @return An iterator over zero or more <code>PSDependency</code> objects,
    * never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected Iterator getStylesheetDependencies(PSSecurityToken tok, 
      Document doc) throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Set deps = new HashSet();
      
      // check includes and imports
      List sheetNodeList = new ArrayList();
      NodeList includes = doc.getElementsByTagName("xsl:include");
      for (int i = 0; i < includes.getLength(); i++) 
         sheetNodeList.add(includes.item(i));
         
      NodeList imports = doc.getElementsByTagName("xsl:import");
      for (int i = 0; i < imports.getLength(); i++) 
         sheetNodeList.add(imports.item(i));
      
      // check for global templates as we go
      boolean hasGlobals = false;
      PSRxGlobals rxGlobals = null;
      Exception ex = null;
      try
      {
         rxGlobals = new PSRxGlobals(null);
      }
      catch (IOException e)
      {
         ex = e;
      }
      catch (SAXException e)
      {
         ex = e;
      }
      if (ex != null)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Error instantiating PSRxGlobals: " + ex.getLocalizedMessage());
      }

      Iterator sheetNodes = sheetNodeList.iterator();
      while (sheetNodes.hasNext())
      {
         Element sheetEl = (Element)sheetNodes.next();
         String href = sheetEl.getAttribute("href");
         if (href.trim().length() > 0)
         {
            try
            {
               URL url = new URL(href);
               String path = url.getFile();
               
               // first try as stylesheet dep
               PSDependency dep = getDepFromPath(tok, path);
               if (dep != null)
               {
                  if (dep.getDependencyType() == PSDependency.TYPE_SHARED)
                  {
                     dep.setIsAssociation(false);
                  }
                  
                  deps.add(dep);
                  // see if path matches the rx_gobals stylesheet path, make
                  // sure path separators are normalize for the comparison
                  if (normalizePathSep(path).equals(normalizePathSep(
                     rxGlobals.getGlobalTemplateFilePath())))
                  {
                     hasGlobals = true;
                  }
               }
            }
            catch (MalformedURLException | com.percussion.services.error.PSNotFoundException e)
            {
               // not something we can deal with
            }
         }
      }
      
      // if globals is imported, then check for gobal template calls
      // for each global template name found, check for the template
      // file, and if at least one is found, then add the global
      // templates app as a dependency
      if (hasGlobals)
      {
         NodeList templateCalls = doc.getElementsByTagName("xsl:call-template");
         for (int i = 0; i < templateCalls.getLength(); i++)
         {
            Element callEl = (Element) templateCalls.item(i);
            String name = callEl.getAttribute("name");
            boolean gtCallExists = false;
            //New way of adding global template to a local template
            if (name.equals(PSRxGlobals.DISPATCHING_TEMPLATE_NAME))
            {
               gtCallExists = true;
            }
            //Support old way too
            else if (name.endsWith(
                  PSGlobalTemplate.GLOBAL_TEMPLATE_NAME_SUFFIX))
            {
               String templateName = name.substring(0, name.indexOf(
                     PSGlobalTemplate.GLOBAL_TEMPLATE_NAME_SUFFIX));
                  File templateFile = new File(
                     PSRxGlobals.ABS_GLOBAL_TEMPLATES_PATH, templateName
                     + ".xsl");
                  if (templateFile.exists())
                  {
                     gtCallExists = true;
                  }
            }
            if(gtCallExists)
            {
                  /*
                   * found the call to global template dispatcher, add app
                   * dependency on global templates app
                   */
                  String globalAppName = PSGlobalTemplateUpdateHandler.
                     DEFAULT_GLOBALTEMPLATE_APP_NAME;
                  PSDependency globalAppDep =
                     getAppDepHandler().getDependency(tok, globalAppName);
                  if (globalAppDep != null)
                     deps.add(globalAppDep);
                  break;
            }
         }
      }

      // build map of variables
      Map varMap = new HashMap();
      NodeList vars = doc.getElementsByTagName("xsl:variable");
      for (int i = 0; i < vars.getLength(); i++)
      {
         Element varEl = (Element)vars.item(i);
         String varName = varEl.getAttribute("name");
         String varVal = varEl.getAttribute("select");
         if (varName.trim().length() > 0)
            varMap.put(varName, varVal);
      }

      // get path dependencies using all vars
      Iterator paths = getVarPaths(doc, varMap);
      while (paths.hasNext())
      {
         String path = (String)paths.next();
         PSDependency pathDep = getDepFromPath(tok, path);
         if (pathDep != null)
            deps.add(pathDep);
      }

      return deps.iterator();
   }
   
   /**
    * Gets a dependency from the supplied path
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param path The path to check, may not be <code>null</code> or empty.
    * 
    * @return The dependency, may be <code>null</code> if the supplied path
    * does not represent one.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected PSDependency getDepFromPath(PSSecurityToken tok, String path)
           throws PSDeployException, com.percussion.services.error.PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
      
      PSDependency dep = null;
      // see if its a url
      try
      {
         URL url = new URL(path);
         path = url.getFile();
      }
      catch (MalformedURLException e)
      {
         // nope, leave it alone
      }

      // if now we don't have a path, we're done
      if (path != null && path.trim().length() > 0)
      {
         path = PSDeployComponentUtils.stripPathPrefix(path);
         if (path.trim().length() > 0) // strip could return an empty string
         {
            // first try as stylesheet
            dep = getSheetDepHandler().getDependency(tok, path);
         
            if (dep == null)
            {
               // now try as file
               dep = getFileDepHandler().getDependency(tok, path);
            }
         
            if (dep == null)
            {
               // now try as app dep
               String appName = PSDeployComponentUtils.getAppName(path);
               if (appName != null && appName.trim().length() > 0)
                  dep = getAppDepHandler().getDependency(tok, appName);
            }            
         }
      }
      
      return dep;
   }
   
   /**
    * Gets the file from the app specified by the supplied path if it exists.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param path The path to check, may not be  <code>null</code> or 
    * empty.
    * 
    * @return A <code>File</code> file with a path relative to the application 
    * root, may be <code>null</code> if the specified file does not exist.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the app does not exists or any errors occur.
    */
   protected File getAppFile(PSSecurityToken tok, String path)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
         
      File appFile = null;
      String appName = PSDeployComponentUtils.getAppName(path);
      if (appName != null)
      {
         int fileStart = path.indexOf(appName) + appName.length() + 1;
         if (fileStart < path.length())
         {
            String filePath = path.substring(fileStart);
            Iterator appFiles = getAppFiles(tok, appName);
            while (appFiles.hasNext() && appFile == null)
            {
               File test = (File)appFiles.next();
               if (compareFiles(test.getPath(), filePath))
                  appFile = test;
            }
         }
      }
      return appFile;
   }
   
   /**
    * Saves the app file to the specified application.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param archive The archive handler to use, may not be <code>null</code>.
    * @param dep The dependency being installed, may not be <code>null</code>.
    * @param ctx The import context to use, may not be <code>null</code>.
    * @param depFile The dependency file for the app file, may not be 
    * <code>null</code> and must be of the type 
    * {@link PSDependencyFile#TYPE_SUPPORT_FILE}.
    * 
    * @throws PSDeployException if any errors occur.
    */
   protected void saveAppFile(PSSecurityToken tok, PSArchiveHandler archive, 
      PSDependency dep, PSImportCtx ctx, PSDependencyFile depFile) 
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      
      if (depFile == null)
         throw new IllegalArgumentException("depFile may not be null");
      
      if (depFile.getType() != PSDependencyFile.TYPE_SUPPORT_FILE)
         throw new IllegalArgumentException("depFile wrong type");
         
      PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
      
      int transAction = PSTransactionSummary.ACTION_CREATED;
      
      File origFile = normalizePathSep(depFile.getOriginalFile());
      String appName = PSDeployComponentUtils.getAppName(dep.getDependencyId());
      if (appName == null)
      {
         // couldn't have gotten here in this case, but in case its a bug...
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            "Cannot get appname from path: " + dep.getDependencyId());
      }

         PSXmlObjectStoreLockerId lockId = null;
         boolean exists = false;
         try {
            // create lock, stealing lock if required
            lockId = new PSXmlObjectStoreLockerId(ctx.getUserId(), true, true,
                    tok.getUserSessionId());
            os.getApplicationLock(lockId, appName + "-" + origFile.getName(), 30);

            exists = (getAppFile(tok, dep.getDependencyId()) != null);

            os.saveApplicationFile(appName, origFile,
                    archive.getFileData(depFile), true, lockId, tok);
         } catch (PSLockedException | PSNotFoundException | PSAuthorizationException | PSServerException | PSNotLockedException e) {

            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                    "Cannot save application from path: " + dep.getDependencyId() + "Error: " + e.getMessage());
         } finally {
            try {
               os.releaseApplicationLock(lockId,appName + "-" + origFile.getName());
            } catch (PSServerException e) {
               throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                       "Cannot release Application lock  " + dep.getDependencyId() + "Error: " + e.getMessage());
            }
         }


         PSDependency parentDep = dep.getParentDependency();
         if (parentDep != null && 
               parentDep.getObjectType().equals(PSControlDependencyHandler.
               DEPENDENCY_TYPE))
         {
            // update control imports 
            PSCustomControlManager.getInstance().writeImports();
         }
         
         // check to see if overwriting   
         if (exists)
            transAction = PSTransactionSummary.ACTION_MODIFIED;
         addTransactionLogEntry(dep, ctx, origFile.getPath(), 
            PSTransactionSummary.TYPE_FILE, transAction);

   }
   
   /**
    * Gets the list of files from the specified application.  Does not include
    * files dynamically created and used by active assembly.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param appName The name of the app, may not be  <code>null</code> or 
    * empty.
    * 
    * @return An iterator over zero or more <code>File</code> objects, each 
    * specifying a path relative to the application root, never 
    * <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any errors occur.
    */
   protected Iterator getAppFiles(PSSecurityToken tok, String appName)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");
         
      try
      {
         List appFiles = new ArrayList();
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         Iterator files = os.getApplicationFiles(appName);
         while (files.hasNext())
         {
            File file = (File)files.next();
            
            // ignore files in AA temp dirs
            if (isValidAppFile(file, appName))
               appFiles.add(file);
         }
            
         return appFiles.iterator();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Gets the specified xml file from the specified application as a 
    * <code>Document</code>.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param appName The name of the app, may not be  <code>null</code> or 
    * empty.
    * @param appFile The xml file to get, relative to the approot directory,
    * may not be <code>null</code>.
    * 
    * @return The document, never <code>null</code>.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any errors occur.
    */
   protected Document getXmlFileFromApp(PSSecurityToken tok, String appName, 
      File appFile) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");
         
      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");
      
      try 
      {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         return createXmlDocument(os.getApplicationFile(appName, appFile, tok));
      }
      catch (Exception e) 
      {
         Object[] args = {appFile, appName, e.getLocalizedMessage()};
         throw new PSDeployException(IPSDeploymentErrors.APP_FILE_LOAD, args);
      }
   }
   
   /**
    * Gets the specified file from the specified application as a 
    * <code>File</code>.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param appName The name of the app, may not be  <code>null</code> or 
    * empty.
    * @param appFile The file to get, relative to the approot directory,
    * may not be <code>null</code>.
    * 
    * @return The File, never <code>null</code>.
    * 
    * @throws PSDeployException if any errors occur.
    */
   protected File getFileFromApp(PSSecurityToken tok, String appName, 
      File appFile) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (appName == null || appName.trim().length() == 0)
         throw new IllegalArgumentException("appName may not be null or empty");
         
      if (appFile == null)
         throw new IllegalArgumentException("appFile may not be null");

      try (PSPurgableTempFile tmpFile = new PSPurgableTempFile("dpl_", ".tmp",
            null)) {
         PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
         try (FileOutputStream out = new FileOutputStream(tmpFile)) {
            IOTools.copyStream(os.getApplicationFile(appName,
                    normalizePathSep(appFile), tok), out);
         }
         return tmpFile;
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }

   }

   /**
    * Gets a list of file paths using variables from the supplied stylesheet.
    *
    * @param doc The stylesheet parsed as an XML document, assumed not
    * <code>null</code>.
    * @param varMap The map for variables used in the supplied stylesheet,
    * assumed not <code>null</code>.
    *
    * @return An iterator over zero or more filepaths as <code>String</code>
    * objects, never <code>null</code>.
    */
   private Iterator getVarPaths(Document doc, Map varMap)
   {
      Set pathSet = new HashSet();

      if (!varMap.isEmpty())
      {
         // walk all nodes and check for var use
         NodeList nodes = doc.getElementsByTagName("*");
         for (int i = 0; i < nodes.getLength(); i++)
         {
            // look for elements with attr select="concat($<var>, <file>)
            Element el = (Element)nodes.item(i);
            String path = el.getAttribute("select");
            if (path.trim().length() == 0)
               path = el.getAttribute("src");
            if (path.trim().length() == 0)
               path = el.getAttribute("href");
            // if node is embeded in literal output text, value may have "{}"
            // around it, need to trim before pattern matching
            path = trim(path, "{", "}");
            if (!ms_concatPatMatch.doesMatchPattern(path))
               continue;

            int iSep = path.indexOf(CONCAT_SEP);

            // get the variable name
            String var = path.substring(path.indexOf(CONCAT_START) +
                    CONCAT_START.length(), iSep);

            // see if its in the map
            String replVal = (String)varMap.get(var);
            if (replVal == null)
               continue;

            // get the file
            String file = path.substring(iSep + CONCAT_SEP.length(),
                    path.lastIndexOf(CONCAT_END)).trim();
            if (file.length() == 0)
               continue;

            // trim any quotes
            file = trim(file, QUOTE_STRING, QUOTE_STRING);
            replVal = trim(replVal, QUOTE_STRING, QUOTE_STRING);

            path = replVal + file;
            pathSet.add(path);
         }
      }

      return pathSet.iterator();
   }

   /**
    * Trims the leading and trailing strings from the supplied string, only if
    * the supplied string starts with the leading string and also ends with the
    * trailing string.
    * @param string The string to trim, assumed not <code>null</code> or empty.
    * @param leading The leading string to trim from the <code>string</code>,
    * assumed not <code>null</code> or empty.
    * @param trailing The trailing string to trim from the <code>string</code>,
    * assumed not <code>null</code> or empty.
    *
    * @return The trimmed string, or the original string.
    */
   private String trim(String string, String leading, String trailing)
   {
      if (string.startsWith(leading) && string.endsWith(trailing))
         string = string.substring(leading.length(), string.length() -
            trailing.length());
      return string;
   }

   /**
    * Gets the variable table schema, caching after first call
    * 
    * @return The schema, never <code>null</code>.
    * 
    * @throws PSDeployException if it cannot be loaded.
    */
   private PSJdbcTableSchema getVarSchema() throws PSDeployException
   {
      if (m_varSchema == null)
      {
         m_varSchema = PSDbmsHelper.getInstance().catalogTable(VAR_TABLE, 
            false);
      }
      
      return m_varSchema;
   }
 
   /**
    * Gets the app dependency handler.
    * 
    * @return The handler, never <code>null</code>.
    */
   protected PSDependencyHandler getAppDepHandler()
   {
      if (m_appHandler == null)
         m_appHandler = getDependencyHandler(
            PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      
      return m_appHandler;
   }
   
   /**
    * Gets the stylesheet dependency handler.
    * 
    * @return The handler, never <code>null</code>.
    */
   protected PSDependencyHandler getSheetDepHandler()
   {
      if (m_sheetHandler == null)
         m_sheetHandler = getDependencyHandler(
            PSStylesheetDependencyHandler.DEPENDENCY_TYPE);
      
      return m_sheetHandler;
   }
   
   /**
    * Gets the support file dependency handler.
    * 
    * @return The handler, never <code>null</code>.
    */
   protected PSDependencyHandler getFileDepHandler()
   {
      if (m_fileHandler == null)
         m_fileHandler = getDependencyHandler(
            PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
      
      return m_fileHandler;
   }
   
   /**
    * Normalizes the path separator of the supplied file to that of the current
    * system.  Assumes that neither {@link #WIN_SEP_CHAR} nor 
    * {@link #UNIX_SEP_CHAR} are used in file or directory names.
    * 
    * @param file The file to normalize, may not be <code>null</code>.
    * 
    * @return The normalized file., never <code>null</code>.
    */
   protected File normalizePathSep(File file)
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
         
      File normFile = new File(normalizePathSep(file.getPath()));
         
      return normFile;
   }
   
   /**
    * Normalizes the path separator of the supplied path to that of the current
    * system.  Assumes that neither {@link #WIN_SEP_CHAR} nor 
    * {@link #UNIX_SEP_CHAR} are used in file or directory names.
    * 
    * @param path The path to normalize, may not be <code>null</code> or empty.
    * 
    * @return The normalized path, never <code>null</code>.
    */
   protected String normalizePathSep(String path)
   {
      if (path == null || path.trim().length() == 0)
         throw new IllegalArgumentException("path may not be null or empty");
         
      char curSep = File.separatorChar;
      if (curSep == WIN_SEP_CHAR)
         path = path.replace(UNIX_SEP_CHAR, WIN_SEP_CHAR);
      else
         path = path.replace(WIN_SEP_CHAR, UNIX_SEP_CHAR);
         
      return path;
   }

   /**
    * Get the name of the root directory of the supplied file's path.
    * 
    * @param file The file to check, may not be <code>null</code>.
    * 
    * @return The root name, or <code>null</code> if the file's path does not
    * specify any parent directories.  Never empty.
    */
   protected String getRootDir(File file)
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      
      String root = null;
      
      File parent = file.getParentFile();
      while (parent != null)
      {
         root = parent.getName();
         parent = parent.getParentFile();
      }
      
      return root;
   }
   
   /**
    * Compares two paths without regard to the path separator.
    * 
    * @param path1 The first path to compare, assumed not <code>null</code>.
    * @param path2 The second path to compare, assumed not <code>null</code>.
    * 
    * @return <code>true</code> if they are equal without regard to path 
    * separator, case-sensitive, <code>false</code> otherwise.
    */
   private boolean compareFiles(String path1, String path2)
   {
      // tranform forward slashes to backslashes
      path1 = path1.replace(UNIX_SEP_CHAR, WIN_SEP_CHAR);
      path2 = path2.replace(UNIX_SEP_CHAR, WIN_SEP_CHAR);
      
      return path1.equals(path2);
   }
 
   /**
    * Determine if the supplied file is one that should be considered.  Files
    * in the "edit" subdirectories created by active assembly are not valid.
    * 
    * @param file The file to check, assumed not <code>null</code> or empty.
    * @param appName The name of the app, assumed not <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> if it is valid, <code>false</code> if it should
    * be ignored.
    */
   private boolean isValidAppFile(File file, String appName)
   {
      boolean isValid = false;
      if (!AA_DIR.equalsIgnoreCase(getRootDir(file)))
      {
         File tmpFile = file;
         for (int i = 0; i < RESOURCE_AA_PATH.length; i++) 
         {
            File parent = tmpFile.getParentFile();
            if (parent == null || 
               !parent.getName().equalsIgnoreCase(RESOURCE_AA_PATH[i]))
            {
               isValid = true;
               break;
            }
            tmpFile = parent;
         }
         
         // if still think it's valid, test for a trace file
         if (isValid)
         {
            // test for file in the app root that matches the trace file name 
            if (file.getPath().equals(PSDebugManager.getTraceFileName(
               appName)))
            {
               isValid = false;
            }
         }
      }
      
      return isValid;
   }
 
   /**
    * The context variable table schema, <code>null</code> until first call to
    * {@link #getVarSchema()}, never <code>null</code> or modified after that.
    */
   private PSJdbcTableSchema m_varSchema = null;
 
   /**
    * The app dependency handler, <code>null</code> until first call to
    * {@link #getAppDepHandler()}, never <code>null</code> or modified after 
    * that.
    */
   private PSDependencyHandler m_appHandler = null;
   
   /**
    * The stylesheet dependency handler, <code>null</code> until first call to
    * {@link #getSheetDepHandler()}, never <code>null</code> or modified after 
    * that.
    */
   private PSDependencyHandler m_sheetHandler = null;
 
   /**
    * The support handler, <code>null</code> until first call to
    * {@link #getFileDepHandler()}, never <code>null</code> or modified after 
    * that.
    */
   private PSDependencyHandler m_fileHandler = null;
 
   /**
    * Constant for start of concat function pattern.
    */
   private static final String CONCAT_START = "concat($";

   /**
    * Constant for end of concat function pattern.
    */
   private static final String CONCAT_END = ")";

   /**
    * Constant for the comma separator of concat function pattern.
    */
   private static final String CONCAT_SEP = ",";

   /**
    * Constant for the single character pattern match.
    */
   private static final char MATCH_ONE = '?';

   /**
    * Constant for the multiple character pattern match.
    */
   private static final char MATCH_ALL = '*';

   /**
    * Constant for a quote character used in the concat function pattern.
    */
   private static final String QUOTE_STRING = "\'";

   /**
    * Constant for concat function pattern <code>concat($*,*)</code>.
    */
   private static final String CONCAT_PAT = CONCAT_START + MATCH_ALL +
      CONCAT_SEP + MATCH_ALL + CONCAT_END;

   /**
    * Pattern matcher for concat() function in sytlesheets, never
    * <code>null</code> or modified after construction.
    */
   private static PSPatternMatcher ms_concatPatMatch = new PSPatternMatcher(
      MATCH_ONE, MATCH_ALL, CONCAT_PAT);

   /**
    * Separator used in file paths on windows.
    */
   private static final char WIN_SEP_CHAR = '\\';
   
   /**
    * Separator used in file paths on unix.
    */
   private static final char UNIX_SEP_CHAR = '/';
   
   /**
    * Name of the temp directory created by active assembly below the app root.
    */
   private static final String AA_DIR = "edit";

   /**
    * Array of directory names constituting the path to the temp subdirectory
    * created by active assembly under the sys_resources application.  Names
    * are in order from bottom most directory up.
    */
   private static final String[] RESOURCE_AA_PATH = {AA_DIR, "assemblers", 
      "stylesheets"};
   
   // db constants
   private static final String VAR_TABLE = "RXASSEMBLERPROPERTIES";
   private static final String VAR_NAME_COL = "PROPERTYNAME";
   private static final String VAR_VALUE_COL = "PROPERTYVALUE";
   private static final String CONTEXT_ID_COL = "CONTEXTID";
}

