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

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSChoiceBuilder;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployComponentUtils;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.error.PSException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.util.PSIteratorUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * Class to handle packaging and deploying authtypes.  
 */
public class PSAuthTypeDependencyHandler extends PSDependencyHandler
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
   public PSAuthTypeDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
   
   // see base class
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      List deps = new ArrayList();
      
      // get entry
      Properties props = getAuthTypesProps();
      
      // get app as local dependency
      String resource = props.getProperty(getPropKey(dep.getDependencyId()));
      PSDependency appDep = getAppDep(tok, resource);
      if (appDep != null)
      {
         if (appDep.canBeIncludedExcluded())
            appDep.setDependencyType(PSDependency.TYPE_LOCAL);
         deps.add(appDep);
      }
      
      // get keywords as local dependency
      PSDependency keywordDep = getDependencyHandler(
         PSKeywordDependencyHandler.DEPENDENCY_TYPE).getDependency(tok, 
            AUTH_TYPE_LOOKUP_ID);
      if (keywordDep != null)
      {
         if (keywordDep.canBeIncludedExcluded())
            keywordDep.setDependencyType(PSDependency.TYPE_LOCAL);
         deps.add(keywordDep);
      }
      
      // get cfg file as local dependency
      PSDependency propFileDep = getDependencyHandler(
         PSConfigFileDependencyHandler.DEPENDENCY_TYPE).getDependency(tok, 
            IPSConstants.AUTHTYPE_PROP_FILE);
      
      if (propFileDep != null)
      {
         if (propFileDep.canBeIncludedExcluded())
            propFileDep.setDependencyType(PSDependency.TYPE_LOCAL);
         deps.add(propFileDep);
      }  
      
      return deps.iterator();
   }

   // see base class
   public Iterator getDependencies(PSSecurityToken tok)
      throws PSDeployException
   {
      Set deps = new HashSet();
      Properties props = getAuthTypesProps();
      Map nameMap = getAuthtypeNames();
      
      // walk all entries and parse authtypes from keys
      Iterator entries = props.entrySet().iterator();
      while (entries.hasNext())
      {
         Map.Entry entry = (Entry) entries.next();
         String key = (String) entry.getKey();
         String authType = getAuthType(key);
         if (authType != null)
         {
            String name = (String) nameMap.get(authType);
            PSDependency dep = createDependency(m_def, authType, 
               (name != null ? name : authType));
            
            // set type to whatever the app type is
            String value = (String) entry.getValue();
            
            PSDependency appDep = getAppDep(tok, value);
            if (appDep != null)
               dep.setDependencyType(appDep.getDependencyType());
            
            deps.add(dep);
         }
      }
      
      return deps.iterator();
   }

   // see base class
   public PSDependency getDependency(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      PSDependency dep = null;
      
      // load authtype props, see if we find our key
      Properties props = getAuthTypesProps();
      String propKey = getPropKey(id);
      if (props.containsKey(propKey))
      {
         String name = (String) getAuthtypeNames().get(id);
         dep = createDependency(m_def, id, (name != null ? name : id));
         PSDependency appDep = getAppDep(tok, props.getProperty(propKey));
         if (appDep != null)
            dep.setDependencyType(appDep.getDependencyType());         
      }
      
      return dep;
   }
   
   /**
    * Get a map of all authtype names.
    * 
    * @return The map, with the authtype as the key and the display name as the
    * value, both as <code>String</code> objects, never <code>null</code>, may
    * be empty.
    * 
    * @throws PSDeployException If the names cannot be retrieved. 
    */
   private Map getAuthtypeNames() throws PSDeployException
   {
      try
      {
         Map result = new HashMap();
         Iterator entries = PSChoiceBuilder.getGlobalLookupEntries(
            AUTH_TYPE_LOOKUP_ID, PSRequest.getContextForRequest());
         while (entries.hasNext())
         {
            PSEntry entry = (PSEntry) entries.next();
            String name = entry.getLabel().getText();
            if (name.trim().length() > 0)
               result.put(entry.getValue(), name);
         }
         
         return result;
      }
      catch (PSException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }
   
   /**
    * Get the app dependency represented by the supplied resource string.
    *  
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param resource The resource, assumed not <code>null</code> or empty and
    * in the form app/resource.
    * 
    * @return The dependency, or <code>null</code> if the supplied 
    * <code>resource</code> does not locate an application.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSDependency getAppDep(PSSecurityToken tok, String resource) 
      throws PSDeployException
   {
      PSDependency appDep = null;
      
      String appName = PSDeployComponentUtils.getAppName(resource);
      if (appName != null)
      {
         PSDependencyHandler appHandler = getDependencyHandler(
            PSApplicationDependencyHandler.DEPENDENCY_TYPE);         
         appDep = appHandler.getDependency(tok, appName);
      }
      
      return appDep;
   }
   
   /**
    * Get the key used to find the authtype entry in the properties file from
    * the authtype id.
    * 
    * @param id The id, assumed not <code>null</code> or empty.
    * 
    * @return The key, never <code>null</code> or empty. 
    */
   private String getPropKey(String id)
   {
      return IPSConstants.AUTHTYPE_PREFIX + id;
   }
   
   /**
    * Get the authtype from the supplied key.
    * 
    * @param key The key from the properties file, assumed not <code>null</code> 
    * or empty.
    * 
    * @return The portion of the key that defines the authtype, or 
    * <code>null</code> if the key does not specify one, never empty.
    */
   private String getAuthType(String key)
   {
      String authType = null;
      if (key.startsWith(IPSConstants.AUTHTYPE_PREFIX))
         authType = key.substring(IPSConstants.AUTHTYPE_PREFIX.length());
      if (authType != null && authType.trim().length() == 0)
         authType = null;
         
      return authType;
   }
   
   /**
    * Load the authtypes properties file
    * 
    * @return The properties, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors
    */
   private Properties getAuthTypesProps() throws PSDeployException
   {
      FileInputStream in = null;
      try
      {
         Properties props = new Properties();
         in = new FileInputStream(PSServer.getRxDir().getAbsolutePath() + "/"
               + IPSConstants.AUTHTYPE_PROP_FILE);
         props.load(in);
         return props;
      }
      catch (IOException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      finally
      {
         if (in != null)
         {
            try {in.close();} catch (IOException e) {}
         }
      }
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Application</li>
    * <li>Keyword</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
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
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      return (getDependency(tok, id) != null);
   }

   // see base class
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
   
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
   
      // noop - will always have required data as local dependencies
      return PSIteratorUtils.emptyIterator();
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
         
      //noop - will always have required data as local dependencies
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "AuthType";
   
   /**
    * Constant for the id of the authtype keyword group
    */
   private static final String AUTH_TYPE_LOOKUP_ID = "10";
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   static
   {
      ms_childTypes.add(PSKeywordDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSApplicationDependencyHandler.DEPENDENCY_TYPE);
   }

}

