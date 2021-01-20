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
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDeployableObject;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.server.PSLogHandler;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.util.PSIteratorUtils;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.tools.IPSUtilsConstants;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Base class for all dependency handlers.  A dependency handler is the runtime
 * handler for an instance of a <code>PSDependencyDef</code> representing a
 * particular dependency type.
 */
public abstract class PSDependencyHandler
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
   public PSDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      if (!getType().equals(def.getObjectType()))
         throw new IllegalArgumentException("invalid def type; expected " +
            getType() + ", supplied " + def.getObjectType());

      if (dependencyMap == null)
         throw new IllegalArgumentException("dependencyMap may not be null");

      m_def = def;
      m_map = dependencyMap;
   }

   /**
    * Gets a handler instance using the supplied def
    *
    * @param def The def for which the appropriate handler type should be
    * returned.  May not be <code>null</code>.
    * @param map The dependency map, may not be <code>null</code>.
    *
    * @return The handler, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>def</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   public static PSDependencyHandler getHandlerInstance(PSDependencyDef def,
      PSDependencyMap map) throws PSDeployException
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      if (map == null)
         throw new IllegalArgumentException("map may not be null");

      String className = def.getHandlerClassName();
      PSDependencyHandler handler = null;

      try
      {
         Class handlerClass = Class.forName(className);
         Constructor handlerCtor = handlerClass.getConstructor( new Class[]
            { PSDependencyDef.class, PSDependencyMap.class });
         handler = (PSDependencyHandler)handlerCtor.newInstance(
            new Object[] {def, map} );

         return handler;
      }
      catch (ClassNotFoundException cnfe)
      {
         Object[] args = {className, cnfe.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
      }
      catch (InstantiationException ie)
      {
         Object[] args = {className, ie.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
      }
      catch (IllegalAccessException iae)
      {
         Object[] args = {className, iae.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
      }
      catch (InvocationTargetException ite)
      {
         Throwable origException = ite.getTargetException();
         String msg = origException.getLocalizedMessage();
         Object[] args = {className, origException.getClass().getName() + ": " + 
            msg};
         throw new PSDeployException(
            IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
      }
      catch (NoSuchMethodException nsme)
      {
         Object[] args = {className, nsme.getLocalizedMessage()};
         throw new PSDeployException(
            IPSDeploymentErrors.DEPENDENCY_HANDLER_INIT, args);
      }
      catch (IllegalArgumentException iae)
      {
         //this should never happen because we checked ahead of time
         throw new RuntimeException("Ctor args failed validation: " +
            iae.getLocalizedMessage());
      }
   }

   /**
    * Determines if the supplied child dependency is a child type supported
    * by this handler.
    *
    * @param child The child dependency to check, may not be <code>null</code>.
    *
    * @return <code>true</code> if the supplied <code>child</code>'s object type
    * is a supported child type of this handler, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>child</code> is
    * <code>null</code>.
    */
   public boolean isChildTypeSupported(PSDependency child)
   {
      if (child == null)
         throw new IllegalArgumentException("child may not be null");

      boolean isChildTypeOk = false;

      String childType = child.getObjectType();
      Iterator childTypes = getChildTypes();
      while (childTypes.hasNext() && !isChildTypeOk)
      {
         if (childType.equals(childTypes.next()))
            isChildTypeOk = true;
      }
      
      if (!isChildTypeOk && m_def.supportsIdMapping())
         isChildTypeOk = getIdTypes().contains(child.getObjectType());

      return isChildTypeOk;
   }

   /**
    * Gets all dependencies that are child dependecies of the supplied
    * dependency.
    * Note: Add IDType dependencies this method
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dep A dependency of the type defined by this handler, may not be
    * <code>null</code>.
    *
    * @return iterator over zero or more <code>PSDependency</code> objects, 
    * never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if dep is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public abstract Iterator getChildDependencies(PSSecurityToken tok,
      PSDependency dep)
      throws PSDeployException;

   /**
    * Gets all deployable files that define this dependency from the Rhythmyx
    * server.  Default implementation returns an empty iterator.  Derived
    * classes that need to return files should override this method.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param dep A dependency of the type defined by this handler, may not be
    * <code>null</code>.
    *
    * @return An iterator over zero or more PSDependencyFile objects.
    *
    * @throws IllegalArgumentException if dep is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      return PSIteratorUtils.emptyIterator();
   }

   /**
    * Installs the supplied dependency files objects in the Rhythmyx server.
    * If a user dependency, the defintion is saved on the target server.
    * Handles all ID and dbms credential transformations and transaction
    * logging.  Base class implementation will throw
    * <code>UnsupportedOperationException</code> as not all dependencies can
    * install files.  Derived classes that support this must override this
    * method.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param archive The archive handler to use to retrieve the required files
    * from the archive.  May not be <code>null</code>.
    * @param dep The dependency for which files are to be installed.  May not be
    * <code>null</code> and must be of the type supported by the handler.
    * @param ctx The import context to aid in the installation.  May not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws UnsupportedOperationException if not overriden by a derived class.
    * @throws PSDeployException if there are any errors.
    */
   public void installDependencyFiles(PSSecurityToken tok,
      PSArchiveHandler archive, PSDependency dep, PSImportCtx ctx)
      throws PSDeployException
   {
      throw new UnsupportedOperationException("method not supported");
   }

   /**
    * Gets all dependencies of this type that exist on the Rhythmyx server.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    *
    * @return An iterator over zero or more <code>PSDependency</code> objects.
    *
    * @throws IllegalArgumentException if <code>tok</code> is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public abstract Iterator<PSDependency> getDependencies(PSSecurityToken tok)
      throws PSDeployException;
      
   /**
    * Gets all dependencies of this type that exist on the Rhythmyx server with
    * the specified parent id.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param parentType The type of the dependency's parent.  May not be 
    * <code>null</code> or empty.
    * @param parentId The id of a parent supported by the handler.  May not be
    * <code>null</code> or empty.
    *
    * @return An iterator over zero or more <code>PSDependency</code> objects.
    *
    * @throws IllegalArgumentException if <code>tok</code> is invalid.
    * @throws IllegalStateException if the derived handler does not support
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler supports 
    * parent ids but has not overriden this method.
    * @throws PSDeployException if there are any errors.
    */
   public Iterator getDependencies(PSSecurityToken tok, String parentType, 
      String parentId) throws PSDeployException
   {
      if (!m_def.supportsParentId())
      {
         throw new IllegalStateException("type must support parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * Gets the dependency object for the specified instance.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param id The id of the specific dependency instance to return.  May not
    * be <code>null</code> or empty.
    *
    * @return The dependency, or <code>null</code> if the specified dependency
    * cannot be located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if the derived handler supports
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler does not
    * support parent ids and has not overridden this method.
    * @throws PSDeployException if there are any errors.
    */
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (m_def.supportsParentId())
      {
         throw new IllegalStateException("type supports parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * Gets the dependency object for the specified id and parent.  Derived 
    * classes that support parent id's must override this method.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param id The id of the specific dependency instance to return.  May not
    * be <code>null</code> or empty.
    * @param parentType The type of the dependency's parent.  May not be 
    * <code>null</code> or empty.
    * @param parentId The id of the dependency's parent.  May not be 
    * <code>null</code> or empty.
    *
    * @return The dependency, or <code>null</code> if the specified dependency
    * cannot be located.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if the derived handler does not support
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler supports 
    * parent ids but has not overriden this method.
    * @throws PSDeployException if there are any errors.
    */
   public PSDependency getDependency(PSSecurityToken tok, String id, 
      String parentType, String parentId)
         throws PSDeployException
   {
      if (!m_def.supportsParentId())
      {
         throw new IllegalStateException("type must support parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * init method to get ACL service
    *
    */
   private void initAclService()
   {
      if ( m_aclSvc == null )
         m_aclSvc = PSAclServiceLocator.getAclService();
   }
   
   
   /**
    * A convenience method to add any acl dependencies to the Rhythmyx Element
    * this element must be a guid type.
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param key the GUID type, never <code>null</code>
    * @param dep A dependency of the type defined by this handler, may not be
    * <code>null</code>.
    * @param childDeps a collection to which an acl dependency must be added
    * @throws PSDeployException
    */
   public void addAclDependency(PSSecurityToken tok, PSTypeEnum key,
         PSDependency dep, Collection<PSDependency> childDeps)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dependency  may not be null");
      if (childDeps == null)
         throw new IllegalArgumentException(
               "child dependencies  may not be null");
      
      initAclService();
      PSDependencyHandler h = 
               getDependencyHandler(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
      PSGuid guid = new PSGuid(key, PSDependencyUtils.getGuidValFromString(dep
            .getDependencyId(), m_def.getObjectTypeName()));
      PSAclImpl acl = (PSAclImpl) m_aclSvc.loadAclForObject(guid); 
      
      if ( acl != null )
      {
         PSDependency aclDep = null;
         aclDep = h.getDependency(tok, String.valueOf(acl.getGUID().longValue()));
         if (aclDep != null)
            childDeps.add(aclDep);
      }    
      return;
   }
   /**
    * Derived classes must override this method to provide the list of child
    * dependency types they can discover.
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   public abstract Iterator getChildTypes();

   /**
    * Must be overriden by derived classes to supply the correct type.
    *
    * @return the type of dependency supported by this handler, never
    * <code>null</code> or empty.
    */
   public abstract String getType();

   /**
    * Must be overriden by derived classes that support parent ids to supply the 
    * correct parent type.
    *
    * @return the type of parent dependency supported by this handler, never
    * <code>null</code> or empty.
    * 
    * @throws IllegalStateException if the derived handler does not support
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler supports 
    * parent ids but has not overriden this method.
    */
   public String getParentType()
   {
      if (!m_def.supportsParentId())
      {
         throw new IllegalStateException("type must support parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * Determine if the server object represented by the supplied dependency id
    * of the type supported by this handler exists.  The derived handler may not
    * support parent ids
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param id The id of the dependency to check, may not be <code>null</code>
    * or empty.
    *
    * @return <code>true</code> if the server object can be found,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if the derived handler supports
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler does not
    * support parent ids and has not overriden this method.
    * @throws PSDeployException if there are any errors.
    */
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (m_def.supportsParentId())
      {
         throw new IllegalStateException("type supports parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * Determine if the server object represented by the supplied dependency and
    * parent id of the type supported by this handler exists. The derived
    * handler must support parent ids.
    *
    * @param tok The security token to use if objectstore access is required,
    * may not be <code>null</code>.
    * @param id The id of the dependency to check, may not be <code>null</code>
    * or empty.
    * @param parentId The parent id of the dependency to check, may not be 
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the server object can be found,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws IllegalStateException if the derived handler does not support
    * parent ids.
    * @throws UnsupportedOperationException if the derived handler supports 
    * parent ids but has not overriden this method.
    * @throws PSDeployException if there are any errors.
    */
   public boolean doesDependencyExist(PSSecurityToken tok, String id, 
      String parentId) throws PSDeployException
   {
      if (!m_def.supportsParentId())
      {
         throw new IllegalStateException("type must support parent id");
      }
      throw new UnsupportedOperationException(
         "Derived Handler must provide implementation.");
   }

   /**
    * If the supplied dependency is listed in the supplied ID map as a new
    * object, and a new target id has not yet been set, then creates a new id
    * and sets it on the map along with a target name.  Derived classes that
    * support ID mapping must override this method.
    *
    * @param dep The dependency to check, may not be <code>null</code> and
    * must be of the type defined by this handler.
    * <code>supportsIdMapping()</code> must return <code>true</code> for this
    * dependency.
    * @param idMap The ID map, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws UnsupportedOperationException if
    * <code>dep.supportsIDMapping()</code> returns <code>true</code> but this
    * method has not been overriden by the derived class.
    * @throws PSDeployException if there are any errors.
    */
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException(" may not be null");

      if (idMap == null)
         throw new IllegalArgumentException(" may not be null");

      if (!dep.supportsIDMapping())
      {
         throw new IllegalArgumentException("dep must support id mapping");
      }
      else
      {
         throw new UnsupportedOperationException(
            "Derived Handler must provide implementation.");
      }
   }

   /**
    * Determines if this handler's dependencies should not be installed until
    * afert all other dependencies have been installed.  All dependencies whose
    * installation is deferred will not be installed until after all other
    * dependencies that are not deferred are installed. Among those that are
    * deferred, the order in which they are installed is not defined.
    *
    * @return <code>true</code> if installation should be deferred,
    * <code>false</code> if not.  Base class returns <code>false</code> by
    * default, derived classes should override if they need to return
    * <code>true</code>.
    */
   public boolean shouldDeferInstallation()
   {
      return false;
   }
   
   /**
    * Determines if the handler's type represents a deployable element with a 
    * numeric id that must be tranformed on installation, or a type that has a
    * pair id (see {@link PSPairDependencyId}).  Derived classes
    * should override this method to return <code>true</code> if that is the
    * case, and must also override {@link #getIdMappingType()} and possibly
    * {@link #getParentIdMappingType()} as well.
    * 
    * @return <code>true</code> if id mapping is delegated to a child dependency
    * handler, <code>false</code> otherwise.  Base class always returns
    * <code>false</code>.
    */
   public boolean delegatesIdMapping()
   {
      return false;
   }
   
   /**
    * Gets the type to use when retrieving mappings from the id map.  Base class
    * method returns the handler's type.  Derived classes should override this
    * method if they represent a deployable element with a numeric id that must
    * be tranformed on installation, and return the type of their child 
    * dependency that supports id mapping and should actually be mapped.  They
    * must also override {@link #delegatesIdMapping()} to return 
    * <code>true</code>.
    * 
    * @return The type, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if this method is not overriden and the
    * handler's type does not support id mapping.
    */
   public String getIdMappingType()
   {
      if (!m_def.supportsIdMapping())
         throw new IllegalArgumentException("type must support id mapping");
         
      return getType();
   }
   
   /**
    * Gets the parent type to use when retrieving mappings from the id map.  See
    * {@link #getIdMappingType()} for more info.
    * 
    * @return The parent id mapping type, never <code>null</code> or empty.
    * 
    * @throws IllegalStateException if this method is not overriden and the
    * handler's type does not support id mapping or parent ids.
    */
   public String getParentIdMappingType()
   {
      if (!m_def.supportsIdMapping())
         throw new IllegalArgumentException("type must support id mapping");
      
      if (!m_def.supportsParentId())
         throw new IllegalArgumentException("type must support parent id");
      
      return getParentType();
   }
   
   /**
    * Determines if the supplied type is a required child type.  Used for 
    * validating missing child dependencies during installation.
    * 
    * @param type The child type to check, may not be <code>null</code> or 
    * empty.
    * 
    * @return <code>true</code> by default. Derived classes should override this
    * method if an unincluded child of the specified type should not be 
    * considered "missing" during installation.
    */
   public boolean isRequiredChild(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      //return !type.equals(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
      return true;

   }
   
   /**
    * By default dependencies are overwritten on installation.  Some handlers
    * may only "insert" dependencies, and skip installation if the dependency
    * already exists.  Those handlers should override this method to return
    * <code>false</code>.
    * 
    * @return <code>true</code> always.
    */
   public boolean overwritesOnInstall()
   {
      return true;
   }
   
   /**
    * Gets the source id to use to locate an id mapping.  Default implementation
    * just returns the supplied id.  Derived classes for
    * which {@link #delegatesIdMapping()} returns <code>true</code> may need
    * to override this method to return a modified result. 
    * 
    * @param id The dependency id, may not be <code>null</code> or empty.
    * 
    * @return The source id, never <code>null</code> or empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   protected String getSourceForIdMapping(String id) throws PSDeployException
   {
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      return id;
   }
   
   /**
    * Gets the target id using the supplied mapping.  Default implementation
    * just returns {@link PSIdMapping#getTargetId()}.  Derived classes for
    * which {@link #delegatesIdMapping()} returns <code>true</code> may need
    * to override this method to return a modified result. 
    * 
    * @param mapping The mapping to use, may be not be <code>null</code>.  
    * Default implementation requires the mapping type match the value returned
    * by {@link #getIdMappingType()}.
    * @param id The source id, may not be <code>null</code> or empty.  Default 
    * implementation requires this match the source id of the supplied mapping.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   public String getTargetId(PSIdMapping mapping, String id)
      throws PSDeployException
   {
      if (mapping == null)
         throw new IllegalArgumentException("mapping may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      if (!(m_def.supportsIdMapping() || delegatesIdMapping()))
         throw new UnsupportedOperationException(
            "Type must support id mapping or delegate id mapping");

      if (!mapping.getObjectType().equals(getIdMappingType()))
         throw new IllegalArgumentException("mapping has invalid type");

      if (!mapping.getSourceId().equals(id))
         throw new IllegalArgumentException(
            "mapping source must match supplied id");

      return mapping.getTargetId();
   }
   
   /**
    * Gets the list of external dbms referenced by this dependency.  Derived
    * classes should overide this method if they may return a list.  The default
    * implementation returns <code>null</code>.
    * 
    * @param tok The security token to use, may not be <code>null</code>.
    * @param dep A dependency of the type defined by this handler, may not be
    * <code>null</code>.
    * 
    * @return The list of <code>PSDbmsInfo</code> objects, may be 
    * <code>null</code> if this method is not overriden by the derived handler,
    * may be empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   public List getExternalDbmsInfoList(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("dep wrong type for this handler");
      
         
      return null;
   }

   /**
    * Gets a dependency def from its type, ensuring that it is found.
    *
    * @param type The type to get, may not be <code>null</code> or empty.
    *
    * @return The def, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> is <code>null</code>
    * or empty.
    * @throws RuntimeException if the def cannot be found.
    */
   protected PSDependencyDef getValidDependencyDef(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      PSDependencyDef def = m_map.getDependencyDef(type);
      if (def == null)
         throw new RuntimeException("DependencyDef for type " + type +
            " cannot be located.");

      return def;
   }

   /**
    * Gets a dependency handler from its type, ensuring that it is found.
    * This is made public method so that it can be used between packages.
    *
    * @param type The type to get, may not be <code>null</code> or empty.
    *
    * @return The handler, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>type</code> is <code>null</code>
    * or empty.
    * @throws RuntimeException if the def cannot be found.
    */
   protected PSDependencyHandler getDependencyHandler(String type)
   {
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      PSDependencyDef def = getValidDependencyDef(type);
      return m_map.getDependencyHandler(def);
   }

   /**
    * Creates a dependency object from a def and an entry returned from
    * <code>PSDbmsHelper.getRegistrationEntries()</code>.  Type will be set to
    * <code>PSDependency.TYPE_SHARED</code>
    *
    * @param def The def to use, may not be <code>null</code>.
    * @param entry An entry where the key is the object id, and the value is the
    * object name, both as <code>String</code> objects.  May not be
    * <code>null</code>, and may not have a <code>null</code> or empty key or
    * value.
    *
    * @return The dependency, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected PSDeployableObject createDependency(PSDependencyDef def,
      Map.Entry entry)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      if (entry == null)
         throw new IllegalArgumentException("entry may not be null");

      String key = (String)entry.getKey();
      if (key == null || key.trim().length() == 0)
         throw new IllegalArgumentException(
            "entry's key may not be null or empty");

      String value = (String)entry.getValue();
      if (value == null || value.trim().length() == 0)
         throw new IllegalArgumentException(
            "entry's value may not be null or empty");

      return createDependency(def, key, value);
   }

   /**
    * Creates a deployable element from a def and its id and name.  Type will be
    * set to <code>PSDependency.TYPE_SHARED</code>
    *
    * @param def The def to use, may not be <code>null</code>.
    * @param id The id, may not be <code>null</code> or empty.
    * @param name The name, may not be <code>null</code> or empty.
    *
    * @return The deployable element, never <code>null</code>.
    */
   protected PSDeployableElement createDeployableElement(PSDependencyDef def, 
      String id, String name)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSDeployableElement de = new PSDeployableElement(PSDependency.TYPE_SHARED,
         id, def.getObjectType(), def.getObjectTypeName(), name, 
         def.supportsIdTypes(), def.supportsIdMapping(), 
         def.supportsUserDependencies(), def.supportsParentId());
         
      de.setShouldAutoExpand(def.shouldAutoExpand());
      
      return de;
   }
   
   /**
    * Creates a dependency object from a def and its id and name.  Type will be
    * set to <code>PSDependency.TYPE_SHARED</code>
    *
    * @param def The def to use, may not be <code>null</code>.
    * @param id The id, may not be <code>null</code> or empty.
    * @param name The name, may not be <code>null</code> or empty.
    *
    * @return The dependency, never <code>null</code>.
    */
   protected PSDeployableObject createDependency(PSDependencyDef def,
      String id, String name)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      PSDeployableObject obj = new PSDeployableObject(PSDependency.TYPE_SHARED, 
         id, def.getObjectType(), def.getObjectTypeName(), name,
         def.supportsIdTypes(), def.supportsIdMapping(),
         def.supportsUserDependencies(), def.supportsParentId());
      obj.setShouldAutoExpand(def.shouldAutoExpand());
      
      return obj;
   }

   /**
    * Writes supplied doc out to a temp file.
    *
    * @param doc The document to store in the file.  May not be
    * <code>null</code>.
    *
    * @return The file, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    * @throws PSDeployException If there are any errors.
    */
   protected PSPurgableTempFile createXmlFile(Document doc)
      throws PSDeployException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      FileOutputStream out = null;
      try
      {
         PSPurgableTempFile xmlFile = new PSPurgableTempFile("dpl_", ".xml",
            null);
         out = new FileOutputStream(xmlFile);
         PSXmlDocumentBuilder.write(doc, out);
         return xmlFile;
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (IOException ex){}
      }

   }

   
   /**
    * Writes supplied String which is an xml representation of Element
    * out to a temp file.
    *
    * @param str The string representation of document to store in the file.  
    * May not be <code>null</code>.
    *
    * @return The file, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    * @throws PSDeployException If there are any errors.
    */
   protected static PSPurgableTempFile createXmlFile(String str)
      throws PSDeployException
   {
      if (org.apache.commons.lang.StringUtils.isBlank(str))
         throw new IllegalArgumentException("doc may not be empty or null");

      FileOutputStream out = null;
      try
      {
         PSPurgableTempFile xmlFile = new PSPurgableTempFile("dpl_", ".xml",
            null);
         out = new FileOutputStream(xmlFile);
         
         // add xml header if necessary
         String tmpStr = str;
         if (!tmpStr.toUpperCase().startsWith(XML_HDR_STR.toUpperCase()))
            tmpStr = XML_HDR_STR + tmpStr;
           
         out.write(tmpStr.getBytes(IPSUtilsConstants.RX_JAVA_ENC));
         return xmlFile;
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         if (out != null)
            try {out.close();} catch (IOException ex){}
      }
   }

   /**
    * Creates an Xml document from the supplied dependency file.
    *
    * @param in The inputstream from which the document is to be created. This
    * method takes ownership of the stream and closes it when finished.  May
    * not be <code>null</code>.
    *
    * @return The Document, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>in</code> is <code>null</code>.
    * @throws PSDeployException if there are any errors.
    */
   protected static Document createXmlDocument(InputStream in)
         throws PSDeployException
   {
      if (in == null)
         throw new IllegalArgumentException("in may not be null");

      try
      {
         return PSXmlDocumentBuilder.createXmlDocument(in, false);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      finally
      {
         try {in.close();} catch (IOException e){}
      }
   }

   /**
    * Gets the id mapping for the specified dependency.
    *
    * @param ctx The current context, may not be <code>null</code>.
    * @param dep The dependency, may not be <code>null</code> and must support
    * id mapping or be a deployable element.
    *
    * @return The mapping, may be <code>null</code> if id transforms are not
    * required.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there is a current map in the context, but
    * no mapping for the supplied dependency is found, or if the mapping is
    * found but the target id is not set.
    */
   protected PSIdMapping getIdMapping(PSImportCtx ctx, PSDependency dep)
      throws PSDeployException
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      PSIdMapping result;
      
      if (dep.supportsParentId())
      {
         result = getIdMapping(ctx, dep.getDependencyId(), dep.getObjectType(), 
            dep.getParentId(), dep.getParentType());
      }
      else
         result = getIdMapping(ctx, dep.getDependencyId(), dep.getObjectType());
      
      return result;
   }

   /**
    * Gets the id mapping for the specified dependency's id and type.
    *
    * @param ctx The current context, may not be <code>null</code>.
    * @param id The id of the dependency, may not be <code>null</code> or empty.
    * @param type The type of the dependency, may not be <code>null</code> or 
    * empty.
    *
    * @return The mapping, may be <code>null</code> if id transforms are not
    * required.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there is a current map in the context, but
    * no mapping for the supplied dependency is found, or if the mapping is
    * found but the target id is not set.
    */
   protected PSIdMapping getIdMapping(PSImportCtx ctx, String id, String type)
      throws PSDeployException
   {
      return getIdMapping(ctx, id, type, null, null);
   }
   
   /** 
    * Gets the id mapping for the specified dependency's id and type.
    *
    * @param ctx The current context, may not be <code>null</code>.
    * @param id The id of the dependency, may not be <code>null</code> or empty.
    * @param type The type of the dependency, may not be <code>null</code> or 
    * empty.
    * @param parentId The id of the parent if the specified dependency supports
    * parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified dependency 
    * supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    *
    * @return The mapping, may be <code>null</code> if id transforms are not
    * required.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there is a current map in the context, but
    * no mapping for the supplied dependency is found, or if the mapping is
    * found but the target id is not set.
    */
   protected PSIdMapping getIdMapping(PSImportCtx ctx, String id, String type, 
      String parentId, String parentType)
         throws PSDeployException
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

         
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
        
      PSIdMapping mapping = null;
      PSIdMap map = ctx.getCurrentIdMap();
      if (map != null)
      {
         mapping = getIdMapping(map, id, type, parentId, parentType);
         if (mapping.getTargetId() == null)
         {
            Object[] args = {mapping.getObjectType(), mapping.getSourceId(),
                  map.getSourceServer()};
            throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING,
               args);
         }
      }
      
      return mapping;
   }
   
   /** 
    * Gets the id mapping for the specified dependency's id and type.
    *
    * @param idMap The idMap, may not be <code>null</code>.
    * @param id The id of the dependency, may not be <code>null</code> or empty.
    * @param type The type of the dependency, may not be <code>null</code> or 
    * empty.
    * @param parentId The id of the parent if the specified dependency supports
    * parent id.  May be <code>null</code>, never empty.
    * @param parentType The type of the parent if the specified dependency 
    * supports parent id.  May be <code>null</code> only if 
    * <code>parentId</code> is <code>null</code>, never empty.
    *
    * @return The mapping, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If no mapping for the specified dependency is 
    * found.
    */
   public PSIdMapping getIdMapping(PSIdMap idMap, String id, String type, 
      String parentId, String parentType)
         throws PSDeployException
   {
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

         
      if (parentId != null && parentId.trim().length() == 0)
         throw new IllegalArgumentException("parentId may not be empty");
      if (parentId != null && (parentType == null || 
         parentType.trim().length() == 0))
      {
         throw new IllegalArgumentException(
            "parentType may not be null or empty");
      }
         
      // transform mapping type if necessary
      PSDependencyHandler handler;
      if (type.equals(getType()))
         handler = this;
      else
         handler = getDependencyHandler(type);
      
      type = handler.getIdMappingType();
      id = handler.getSourceForIdMapping(id);
      
      if (parentType != null)
      {
         parentType = handler.getParentIdMappingType();
      }
         
      PSIdMapping mapping = null;
      mapping = idMap.getMapping(id, type, parentId, parentType);
      if (mapping == null)
      {
         Object[] args = {type, id, idMap.getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING,
            args);
      }

      return mapping;
   }
   
   /**
    * Transforms the mapping of a specified association given the id and type.
    *
    * @param ctx The import ctx to use to get id mappings, may not be
    * <code>null</code>.
    * @param value The value to be transformed, may not be <code>null</code>.
    * @param type The type of dependency of the value.  May not be 
    * <code>null</code> or empty.
    * 
    * @return Id mapping for the id and type.  May be <code>null</code> if 
    * mapping was not found, transforms are not required, or dependency does
    * not exist for mapped value.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any other errors.
    */
   protected PSIdMapping getIdMappingOfAssoc(PSSecurityToken tok,
         PSImportCtx ctx, String value, String type) 
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (value == null)
         throw new IllegalArgumentException("prop may not be null");
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      PSIdMapping idMapping = null;
      
      try
      {
         // Try to transform the value 
         // NOTE: a PSDeployException will be thrown if mapping does not exist
         //       It is caught and handled below.
         idMapping = getIdMapping(ctx, value, type);

         // If there is a mapping for the association, 
         // See if the mapped item actually exists on the target system.
         if (idMapping != null)
         {
            PSDependencyHandler depHandler = getDependencyHandler(type);
            PSDependency dependentOb = depHandler.getDependency(tok,
                  idMapping.getTargetId());
            if (dependentOb == null)
            {
               idMapping = null;
            }
         }
      }
      // If the exception is an due to a Missing ID Mapping,
      // that is acceptable, set the idMapping and continue.
      // Any other exception should be rethrown.
      catch (PSDeployException de)
      {
         if (de.getErrorCode() == IPSDeploymentErrors.MISSING_ID_MAPPING)
         {
            idMapping = null;
         }
         else 
         {
            throw new PSDeployException(de.getErrorCode());
         }
      }
      finally
      {
         if (idMapping != null)
            value = idMapping.getTargetId();         
      }
      
      return idMapping;
   }

   
   
   /**
    * Gets the new id from the supplied <code>ctx</code> for a given source id 
    * and type.  
    * 
    * @param ctx The current context, may not be <code>null</code>.  The idMap 
    * in the ctx may not be <code>null</code>.
    * @param id The source id to get a new value for, may not be
    * <code>null</code> or empty.
    * @param type The type of id, may not be <code>null</code> or empty.
    * 
    * @return The new id, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if the id cannot be obtained.
    */
   protected int getNewIdInt(PSImportCtx ctx, String id, String type) 
      throws PSDeployException
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");
      
      PSIdMap idMap = ctx.getCurrentIdMap();
      if (idMap == null)
         throw new IllegalArgumentException("ctx must contain an idMap");
         
      // transform mapping type if necessary
      if (!type.equals(getType()))
      {
         PSDependencyHandler handler = getDependencyHandler(type);
         type = handler.getIdMappingType();
      }
      
      return idMap.getNewIdInt(id, type);
   }  
   
   
   
   /**
    * Gets the transaction log action for the supplied dependency, using its
    * id mapping if available.
    *
    * @param ctx The current context, may not be <code>null</code>.
    * @param dep The dependency, may not be <code>null</code>.
    *
    * @return The action, one of the
    * <code>PSTransactionSummary.ACTION_XXX</code> values.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there is a current map in the context, but
    * no mapping for the supplied dependency is found.
    */
   protected int getRowAction(PSImportCtx ctx, PSDependency dep)
      throws PSDeployException
   {
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      PSIdMapping mapping = null;
      if (dep.supportsIDMapping())
         mapping = getIdMapping(ctx, dep);
      int rowAction = PSTransactionSummary.ACTION_MODIFIED;

      // if no mapping, we can't tell, so call it update - might be a "restore"
      // to the same repository as the source, so no id mapping
      if (mapping != null && mapping.isNewObject())
         rowAction = PSTransactionSummary.ACTION_CREATED;

      return rowAction;
   }

   /**
    * Get a specified child dependency from a given dependency object.  
    * Recursively checks child dependencies looking for the specified child.
    *
    * @param dep The dependency object, which contains child dependency objects,
    * may not be <code>null</code>.
    * @param childId The id of the to be retrieved child dependency, may not be
    * <code>null</code> or empty.
    * @param childObjType The object type of the child dependency, may not be
    * <code>null</code> or empty.
    *
    * @return The retrieved child dependency object, it will never be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if cannot find the specified child dependency
    * or any other error occurs.
    */
   protected PSDependency getChildDependency(PSDependency dep, String childId,
      String childObjType) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (childId == null || childId.trim().length() == 0)
         throw new IllegalArgumentException("childId may not be null or empty");
      if (childObjType == null || childObjType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childObjType may not be null or empty");

      PSDependency childDep = doGetChildDependency(dep, childId, childObjType);
      
      if ( childDep == null )
      {
         Object[] args = {childId, childObjType, dep.getDependencyId(),
            dep.getObjectType()};

         throw new PSDeployException(IPSDeploymentErrors.CHILD_DEP_NOT_FOUND,
            args);
      }
      
      return childDep;
   }
   
   /**
    * Get a specified child dependency from a given dependency object.  
    * Recursively checks child dependencies looking for the specified child.
    *
    * @param dep The dependency object, which contains child dependency objects,
    * may not be <code>null</code>.
    * @param childId The id of the to be retrieved child dependency, may not be
    * <code>null</code> or empty.
    * @param childObjType The object type of the child dependency, may not be
    * <code>null</code> or empty.
    *
    * @return The retrieved child dependency object, it may be <code>null</code>
    * if the specified child could not be found.
    * @throws PSDeployException 
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   protected PSDependency doGetChildDependency(PSDependency dep, String childId,
      String childObjType) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (childId == null || childId.trim().length() == 0)
         throw new IllegalArgumentException("childId may not be null or empty");
      if (childObjType == null || childObjType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childObjType may not be null or empty");

      PSDependency childDep = dep.getChildDependency(childId, childObjType);
      if (childDep == null)
      {
         Iterator children = dep.getDependencies();
         if (children != null)
         {
            while (children.hasNext() && childDep == null)
            {
               PSDependency child = (PSDependency)children.next();
               childDep = doGetChildDependency(child, childId, childObjType);
            }
         }
      }
      
      return childDep;
   }
      
   /**
    * Determines if a specified child dependency from a given dependency object
    * is included in the package.  Recursively checks child dependencies looking
    * for the specified child.
    *
    * @param dep The dependency object, which contains child dependency objects,
    * may not be <code>null</code>.
    * @param childId The id of the to be retrieved child dependency, may not be
    * <code>null</code> or empty.
    * @param childObjType The object type of the child dependency, may not be
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the child dependency exists and is included
    * in the package, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if an error occurs.
    */
   protected boolean isChildDependencyIncluded(PSDependency dep, String childId,
      String childObjType) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (childId == null || childId.trim().length() == 0)
         throw new IllegalArgumentException("childId may not be null or empty");
      if (childObjType == null || childObjType.trim().length() == 0)
         throw new IllegalArgumentException(
            "childObjType may not be null or empty");

      boolean isIncluded = false;
      
      try
      {
         PSDependency childDep = getChildDependency(dep, childId, childObjType);
         isIncluded = childDep.isIncluded();
      }   
      catch (PSDeployException e)
      {
         if (e.getErrorCode() != IPSDeploymentErrors.CHILD_DEP_NOT_FOUND)
         {
            throw e;
         }
      }
      
      return isIncluded;
   }
   
   /**
    * Add a transaction log entry for an installed dependency.
    *
    * @param dep The installed dependency, it may not be <code>null</code>.
    * @param ctx The import context to aid in the installation, may not be
    * <code>null</code>.
    * @param type The GUID type of the object that has been affected, may not be
    * <code>null</code>.
    * @param isNew <code>true</code>, if it does not exist on the target system
    * @throws PSDeployException 
    */
   public void addTransactionLogEntryByGuidType(PSDependency dep,
      PSImportCtx ctx, PSTypeEnum type, boolean isNew) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (type == null)
         throw new IllegalArgumentException("type may not be null");

      int transAction = (isNew == false)
            ? PSTransactionSummary.ACTION_MODIFIED
            : PSTransactionSummary.ACTION_CREATED;
      
      if (dep.supportsIDMapping())
      {
         PSIdMapping mapping = getIdMapping(ctx, dep);
         // make sure mapping is reset after update
         if (mapping != null)
            mapping.setIsNewObject(false);
      }

      addTransactionLogEntry(dep, ctx, type.getDisplayName(), 
            PSTransactionSummary.TYPE_CMS_OBJECT, transAction);
   }

   /**
    * Inserts an entry into the log transaction table from the given parameters.
    *
    * @param dep To be logged dependency object, may not be <code>null</code>
    * and must be of the correct type.
    * @param ctx The import context used to transform the dependency id for
    * deletion if found in id map, assume not <code>null</code>.
    * @param elementName The name of the element, may not be <code>null</code>
    * or empty.
    * @param elementType The element type, may not <code>null</code> or empty.
    * @param action One of the <code>PSTransactionSummary.ACTION_xxx</code>
    * values.
    *
    * @throws IllegalArgumentException if there is any invalid parameters.
    * @throws PSDeployException if any other error occurs.
    */
   public void addTransactionLogEntry(PSDependency dep, PSImportCtx ctx,
      String elementName, String elementType, int action) 
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (elementName == null || elementName.trim().length() == 0 )
         throw new IllegalArgumentException(
            "elementName may not be null or empty");
      if (elementType == null || elementType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "elementType may not be null or empty");
      if (! PSTransactionSummary.isActionValid(action))
         throw new IllegalArgumentException(
            "action must be one of PSTransactionSummary.ACTION_XXX values");

      if (!dep.getObjectType().equals(getType()))
         throw new IllegalArgumentException("invalid dependency type");

      int logId = ctx.getPackageLogid();

      // get the target/mapped id
      PSIdMapping depMapping = null;
      if (dep.supportsIDMapping() || delegatesIdMapping())
         depMapping = getIdMapping(ctx, dep);
      String id = (depMapping == null) ? dep.getDependencyId() : 
         getTargetId(depMapping, dep.getDependencyId());

      // format the DEPENDENCY column
      String depString;
      if ( dep.getDisplayName().equals(dep.getDependencyId()) )
      {
         depString = dep.getObjectTypeName() + " \"" + dep.getDisplayName() + 
            "\"";
      }
      else
      {
         depString = dep.getObjectTypeName() + " \"" + dep.getDisplayName() + 
            "\" (" + id + ")";
      }

      PSLogHandler lh = ctx.getLogHandler();      
      lh.addTransactionLogEntry(logId, depString, elementName, elementType, 
         action, ctx.getNextTxnSequence(logId));
   }
      
   /**
    * Create an XML document from the contents of the supplied file.
    * 
    * @param file The file, may not be <code>null</code>.  
    * 
    * @return The document, never <code>null</code>.
    * 
    * @throws PSDeployException if the file does not exist, or if a valid XML 
    * document cannot be created from its contents.
    */
   protected Document getXmlDocumentFromFile(File file) throws PSDeployException
   {
      if (file == null)
         throw new IllegalArgumentException("file may not be null");
      
      FileInputStream in = null;
      try 
      {
         in = new FileInputStream(file);
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
            try {in.close();} catch (IOException e) {}
      }
            
            
   }
   
   /**
    * Gets all types that support id type mapping.
    * 
    * @return An iterator over zero or more types as <code>String</code> 
    * objects, never <code>null</code>, may be empty.
    */
   private List getIdTypes()
   {
      if (m_idTypes == null)
      {
         List<String> types = new ArrayList<String>();
         Iterator defs = m_map.getDefs();
         while (defs.hasNext())
         {
            PSDependencyDef def = (PSDependencyDef)defs.next();
            if (def.supportsIdTypes())
               types.add(def.getObjectType());
         }
         m_idTypes = types;
      }
      
      return m_idTypes;
   }
   
   /**
    * A util header for xml files.
    */
   private static final String XML_HDR_STR = 
      "<?xml version=\"1.0\" encoding=\"utf-8\"?>";
   
   /**
    * List of types that support id type mappings, <code>null</code> until first
    * call to {@link #getIdTypes()}, never <code>null</code> or modified after
    * that, may be empty.
    */
   private List m_idTypes = null;
   
   /**
    * Constant for app name containing system control library file.
    */
   static final String SYS_CONTROL_APP = "sys_resources";

   /**
    * Constant for app name containing user control library file.
    */
   static final String USER_CONTROL_APP = "rx_resources";
   
   /**
    * The dependency defintion supplied when this handler was constructed, never
    * <code>null</code> or modified after that.
    */
   protected PSDependencyDef m_def;

   /**
    * The dependency map supplied when this handler was constructed, never
    * <code>null</code> or modified after that.
    */
   protected PSDependencyMap m_map;

   /**
    * Acl service
    */
   private IPSAclService m_aclSvc = null;
}
