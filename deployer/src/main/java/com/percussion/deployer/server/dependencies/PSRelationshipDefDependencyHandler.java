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


import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.webservices.IPSWebserviceErrors;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Class to handle packaging and deploying a Relationship defintion.
 */
public class PSRelationshipDefDependencyHandler 
   extends PSAppObjectDependencyHandler
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
    * @throws PSDeployException if any other error occurs.
    */
   public PSRelationshipDefDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
   }
   
   // see base class
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      // use a set to weed out dupes
      Set childDeps = new HashSet();

      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig cfg = cfgSet.getConfig(dep.getDependencyId());
      if (cfg != null)
      {
         // this will add extensions, and files, and apps from the params and
         // conditions.
         addApplicationDependencies(tok, childDeps, cfg.toXml(
            PSXmlDocumentBuilder.createXmlDocument()));
         
         // this will add all dependencies specified by id types
         childDeps.addAll(getIdTypeDependencies(tok, dep));
      }
            
      return childDeps.iterator();
    }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      Iterator configs = cfgSet.iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig cfg = (PSRelationshipConfig)configs.next();
         String name = cfg.getName();
         deps.add(createDependency(m_def, name, name));
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
         
      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig cfg = cfgSet.getConfig(id);
      if (cfg != null)
      {
         String name = cfg.getName();
         dep = createDependency(m_def, name, name);
      }
         
      return dep;
   }
   
   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Extension</li>
    * <li>Any Id Type</li>
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
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List<PSDependencyFile> files = new ArrayList<PSDependencyFile>();

      PSRelationshipConfigSet cfgSet = 
         PSRelationshipCommandHandler.getConfigurationSet();
      PSRelationshipConfig cfg = cfgSet.getConfig(dep.getDependencyId());
      if (cfg != null)
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         PSXmlDocumentBuilder.replaceRoot(doc, cfg.toXml(doc));
         File file = createXmlFile(doc);
         files.add(new PSDependencyFile(PSDependencyFile.TYPE_COMPONENT_XML, 
            file));
      }
      
      return files.iterator();
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

      try 
      {
         // restore the object
         String cfgName = dep.getDependencyId();
         Iterator files = archive.getFiles(dep);
   
         if (!files.hasNext())
         {
            Object[] args =
            {
               PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_COMPONENT_XML],
               dep.getObjectType(), cfgName, dep.getDisplayName()
            };
            throw new PSDeployException(
               IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
         }
         
         Document doc = null;
         PSDependencyFile file = (PSDependencyFile)files.next();
         if (file.getType() == PSDependencyFile.TYPE_COMPONENT_XML)
         {
            doc = createXmlDocument(archive.getFileData(file));
         }
         else
         {
            Object[] args =
            {
               PSDependencyFile.TYPE_ENUM[file.getType()],
               PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_COMPONENT_XML]
            };
            throw new PSDeployException(
               IPSDeploymentErrors.WRONG_DEPENDENCY_FILE_TYPE, args);
         }
         
         Element root = doc.getDocumentElement();
         PSRelationshipConfig srcCfg = new PSRelationshipConfig(root, null, 
            null);
   
         // load the current config
         boolean exists = true;
         PSRelationshipConfigSet cfgSet = 
            PSRelationshipCommandHandler.getConfigurationSet();
         PSRelationshipConfig tgtCfg = cfgSet.getConfig(cfgName);
         if (tgtCfg == null)
         {
            // if new, call add to get a new config with sys defaults
            tgtCfg = cfgSet.addConfig(cfgName, PSRelationshipConfig.RS_TYPE_SYSTEM);
            exists = false;
         }
         
         // update the target config with source data
         tgtCfg.copyFrom(srcCfg);
         
         // save the new one
         PSWebserviceUtils.saveRelationshipConfigSet(cfgSet,
               IPSWebserviceErrors.SAVE_FAILED);

         // log txn entry
         int action = exists ? PSTransactionSummary.ACTION_MODIFIED : 
            PSTransactionSummary.ACTION_CREATED;
         addTransactionLogEntry(dep, ctx, cfgName, 
               PSTransactionSummary.TYPE_CMS_OBJECT, action);
      }
      catch (PSUnknownNodeTypeException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSErrorException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
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
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "RelationshipDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();

   static
   {
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }


}
