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
 
package com.percussion.deployer.server.dependencies;

import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.IPSDependentObject;
import com.percussion.design.objectstore.PSControlMeta;
import com.percussion.design.objectstore.PSControlParameter;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSFileDescriptor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSCustomControlManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class to handle packaging and deploying a content editor control.
 */
public class PSControlDependencyHandler extends PSAppObjectDependencyHandler
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
   public PSControlDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }

   // see base class
   @SuppressWarnings("unchecked")
   @Override
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      String depId = dep.getDependencyId();
      String id = null;
      boolean isSystem = dep.getDependencyType() == PSDependency.TYPE_SYSTEM;
      if (isSystem)
         id = SYS_CONTROL_PATH;
      else
      {
         File ctrlFile = ms_ctrlMgr.getControlFile(depId);
         if (ctrlFile != null)
         {
            id = PSCustomControlManager.CUSTOM_CONTROLS_DIR + '/'
                  + ctrlFile.getName();
         }
         else
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
                  "Missing control file for dependency " + depId);
         }
      }
         
      Set deps = new HashSet();
      PSDependencyHandler libHandler = getFileDepHandler();
      
      if (id != null)
      {
         PSDependency child = libHandler.getDependency(tok, id);
         if (child != null)
         {
            if (child.getDependencyType() == PSDependency.TYPE_SHARED)
            {
               child.setIsAssociation(false);
            }

            deps.add(child);
         }
      }
           
      // get the control meta
      PSControlMeta meta;
      if (isSystem)
      {
         meta = getSysControl(tok, depId);
      }
      else
      {
         meta = ms_ctrlMgr.getControl(depId);
      }
      
      if (meta != null)
      {
         // add associated files
         PSDependency fileDep = null;
         Iterator files = meta.getAssociatedFiles().iterator();
         while (files.hasNext())
         {
            PSFileDescriptor file = (PSFileDescriptor)files.next();
            fileDep = getDepFromPath(tok, file.getFileLocation());
            if (fileDep != null)
            {
               if (fileDep.getDependencyType() == PSDependency.TYPE_SHARED)
               {
                  fileDep.setIsAssociation(false);
               }
               
               deps.add(fileDep);
            }
         }
         
         // check all params for app/file ref
         Iterator params = meta.getParams().iterator();
         while (params.hasNext())
         {
            PSControlParameter param = (PSControlParameter)params.next();
            String defVal = param.getDefaultValue();
            if (defVal.trim().length() != 0)
            {
               fileDep = getDepFromPath(tok, defVal);
               if (fileDep != null)
               {
                  if (fileDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  {
                     fileDep.setIsAssociation(false);
                  }
                  
                  deps.add(fileDep);
               }
            }
         }
         
         // now check for exits
         Iterator ctlDeps = meta.getDependencies().iterator();         
         while (ctlDeps.hasNext())
         {
            // have to qualify this cause of name collision!
            com.percussion.design.objectstore.PSDependency ctlDep = 
               (com.percussion.design.objectstore.PSDependency)ctlDeps.next();
            IPSDependentObject depObj = ctlDep.getDependent();
            if (depObj instanceof PSExtensionCall)
            {
               PSExtensionCall call = (PSExtensionCall)depObj;
               PSDependencyHandler exitHandler = getDependencyHandler(
                  PSExitDefDependencyHandler.DEPENDENCY_TYPE);
               PSDependency exitDep = exitHandler.getDependency(tok, 
                  call.getExtensionRef().getFQN());
               if (exitDep != null)
               {
                  if (exitDep.getDependencyType() == PSDependency.TYPE_SHARED)
                  {
                     exitDep.setIsAssociation(false);
                  }
                  
                  deps.add(exitDep);
               }
            }
         }
      }
            
      return deps.iterator();
    }

   // see base class
   @SuppressWarnings("unchecked")
   @Override
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      Map depMap = new HashMap();   
      
      // get the user and system control files, but don't take system dep if
      // overridden
      Iterator deps;
      // add system controls first
      deps = getControlDependencies(tok, true).iterator();
      while (deps.hasNext())
      {
         PSDependency dep = (PSDependency)deps.next();
         depMap.put(dep.getKey(), dep);
      }
      // add custom controls next, replacing system controls with same name
      deps = getControlDependencies(tok, false).iterator();
      while (deps.hasNext())
      {
         PSDependency dep = (PSDependency)deps.next();
         depMap.put(dep.getKey(), dep);
      }
                 
      return depMap.values().iterator();
   }

   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      PSDependency dep = null;
      
      // see if user control first
      dep = getControlDependency(tok, id, false);
      if (dep == null)
         dep = getControlDependency(tok, id, true);
      
      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li> 
    * <li>Extension</li> 
    * <li>SupportFile</li> 
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @SuppressWarnings("unchecked")
   @Override
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   public void installDependencyFiles(PSSecurityToken tok, 
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx) 
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
         
      //noop for this class - will always have library as local dependency
   }
   
   // see base class
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return getDependency(tok, id) != null;
   }

   /**
    * Gets dependencies for all controls of the specified type.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param system if <code>true</code>, gets system controls, otherwise gets
    * user controls.
    * 
    * @return A list of dependencies, never <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if any errors occur.
    */
   @SuppressWarnings("unchecked")
   private List getControlDependencies(PSSecurityToken tok, boolean system) 
      throws PSDeployException
   {
      List deps = new ArrayList();
            
      List<PSControlMeta> controls;
      if (system)
      {
         controls = getControls(getSysControlsDoc(tok));
      }
      else
      {
         controls = ms_ctrlMgr.getAllControls();
      }
               
      for (PSControlMeta meta : controls)
      {
         deps.add(createControlDependency(tok, meta, system));
      }
         
      return deps;
   }
   
   /**
    * Gets the specified control dependency.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param name The name of the control, assumed not <code>null</code> or 
    * empty.
    * @param system if <code>true</code>, checks system controls, otherwise 
    * checks user controls.
    * 
    * @return The control dependency, may be <code>null</code> if not found.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private PSDependency getControlDependency(PSSecurityToken tok, String name, 
      boolean system) throws PSDeployException
   {
      PSDependency dep = null;
      
      PSControlMeta meta;
      if (system)
      {      
         meta = getSysControl(tok, name);
      }
      else
      {
         meta = ms_ctrlMgr.getControl(name);
         if (meta == null)
         {
            meta = getRxControl(tok, name);
         }
      }
           
      if (meta != null)
      {
         dep = createControlDependency(tok, meta, system);
      }
         
      return dep;
   }
   
   
   /**
    * Create a control dependency from the supplied control meta
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param meta The control meta, assumed not <code>null</code>.
    * @param isSystemDep If <code>true</code>, create a system control, if
    * <code>false</code>, create a server user control if control exists in
    * rx_Templates.xsl.
    * 
    * @return The control dependency, never <code>null</code>.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private PSDependency createControlDependency(PSSecurityToken tok,
      PSControlMeta meta, boolean isSystemDep) throws PSDeployException
   {
      String id = meta.getName();
      String name = meta.getDisplayName();
      if (name.trim().length() == 0)
         name = id;
      PSDependency dep = createDependency(m_def, id, name);
      if (isSystemDep)
         dep.setDependencyType(PSDependency.TYPE_SYSTEM);
      else
      {
         if (getRxControl(tok, name) != null)
         {
            // set to server, indicating the control is from rx_Templates.xsl
            // and will be flagged as an error by content type handler
            dep.setDependencyType(PSDependency.TYPE_SERVER);
         }
      }
         
      return dep;
   }
   
   /**
    * Searches the supplied control document for all controls and
    * returns a list of controls.
    * 
    * @param controlDoc The xml doc containing the control definitions, assumed 
    * not <code>null</code>.
    * 
    * @return A list of <code>PSControlMeta</code> objects, never 
    * <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private List<PSControlMeta> getControls(Document controlDoc)
      throws PSDeployException
   {
      try 
      {
         List<PSControlMeta> controls = new ArrayList<PSControlMeta>();
         NodeList nodes = controlDoc.getElementsByTagName(
            PSControlMeta.XML_NODE_NAME);
         for (int i = 0; i < nodes.getLength(); i++) 
         {
            Element control = (Element)nodes.item(i);
            PSControlMeta meta = new PSControlMeta(control);
            controls.add(meta);
         }
         
         return controls;      
      }
      catch (PSUnknownNodeTypeException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Get the XML doc containing all system controls
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    *
    * @return The system controls document, never <code>null</code>.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private Document getSysControlsDoc(PSSecurityToken tok)
      throws PSDeployException
   {
      Document controls = getXmlFileFromApp(tok, SYS_CONTROL_APP, 
            SYS_CONTROL_FILE);
      
      return controls;
   }

   /**
    * Get the XML doc containing all user controls from rx_Templates.xsl.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    *
    * @return The user controls document, never <code>null</code>.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private Document getRxControlsDoc(PSSecurityToken tok)
      throws PSDeployException
   {
      Document controls = getXmlFileFromApp(tok, USER_CONTROL_APP, 
            USER_CONTROL_FILE);
      
      return controls;
   }
   
   /**
    * Gets the specified system control.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param name The control name, assumed not <code>null</code>.
    * 
    * @return The control object or <code>null</code> if not found.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private PSControlMeta getSysControl(PSSecurityToken tok, String name)
      throws PSDeployException
   {
      return getControl(getSysControlsDoc(tok), name);            
   }
   
   /**
    * Gets the specified rx control.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param name The control name, assumed not <code>null</code>.
    * 
    * @return The control object or <code>null</code> if not found.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private PSControlMeta getRxControl(PSSecurityToken tok, String name)
      throws PSDeployException
   {
      return getControl(getRxControlsDoc(tok), name);
   }
   
   /**
    * Gets the specified control from the specified document.
    * 
    * @param doc The control document, assumed not <code>null</code>.
    * @param name The control name, assumed not <code>null</code>.
    * 
    * @return The control object or <code>null</code> if not found.
    * 
    * @throws PSDeployException if any errors occur.
    */
   private PSControlMeta getControl(Document doc, String name)
      throws PSDeployException
   {
      PSControlMeta ctrl = null;
      
      Iterator<PSControlMeta> ctls = getControls(doc).iterator();
      while(ctls.hasNext() && ctrl == null)
      {
         PSControlMeta test = ctls.next();
         if (test.getName().equals(name))
            ctrl = test;
      }
            
      return ctrl;
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Control";

   /**
    * Constant for app file reference to user control library stylesheet
    */
   private static final File USER_CONTROL_FILE = new File("/stylesheets",
         "rx_Templates.xsl");
      
   /**
    * Constant for app file reference to system control library stylesheet
    */
   private static final File SYS_CONTROL_FILE = new File("/stylesheets",
         "sys_Templates.xsl");

   /**
    * Constant for app file reference to system control library stylesheet
    */
   private static final String SYS_CONTROL_PATH = 
         SYS_CONTROL_APP + "/stylesheets/sys_Templates.xsl";
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   /**
    * Get the custom control manager.
    */
   private static PSCustomControlManager ms_ctrlMgr =
         PSCustomControlManager.getInstance();
   
   static
   {
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }
}

