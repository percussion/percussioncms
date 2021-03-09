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
 
package com.percussion.deploy.server.dependencies;


import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSApplicationIDTypeMapping;
import com.percussion.deploy.objectstore.PSApplicationIDTypes;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSIdMap;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.IPSIdTypeHandler;
import com.percussion.deploy.server.PSAppTransformer;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSDeploymentHandler;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSNotLockedException;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.config.PSConfigManager;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Class to handle packaging and deploying a Relationship defintion.
 */
public class PSRelationshipDefDependencyHandler 
   extends PSAppObjectDependencyHandler implements IPSIdTypeHandler
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
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
           throws PSDeployException, PSNotFoundException {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (! dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      // use a set to weed out dupes
      Set childDeps = new HashSet();

      PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(false);
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

      List<PSDependency> deps = new ArrayList<>();
      
      PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(false);
      Iterator configs = cfgSet.iterator();
      while (configs.hasNext())
      {
         PSRelationshipConfig cfg = (PSRelationshipConfig)configs.next();
         deps.add(createDependency(m_def, cfg.getName(), cfg.getLabel()));
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
         
      PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(false);
      PSRelationshipConfig cfg = cfgSet.getConfig(id);
      if (cfg != null)
         dep = createDependency(m_def, cfg.getName(), cfg.getLabel());
         
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

      List<PSDependencyFile> files = new ArrayList<>();

      PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(false);
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
   
         // load and lock the current config
         boolean exists = true;
         PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(true);
         PSRelationshipConfig tgtCfg = cfgSet.getConfig(cfgName);
         if (tgtCfg == null)
         {
            // if new, call add to get a new config with sys defaults
            tgtCfg = cfgSet.addConfig(cfgName, PSRelationshipConfig.RS_TYPE_SYSTEM);
            exists = false;
         }
         
         // update the target config with source data
         tgtCfg.copyFrom(srcCfg);
         
         // tranform id type ids
         PSIdMap idMap = ctx.getCurrentIdMap();
         if (idMap != null)
            transformIds(tgtCfg, ctx.getIdTypes(), idMap);
         
         // save the new one, releasing the lock
         PSConfigManager cfgMgr = PSConfigManager.getInstance();
         try 
         {
            Document cfgSetDoc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(cfgSetDoc, cfgSet.toXml(cfgSetDoc));
            StringWriter writer = new StringWriter();
            PSXmlDocumentBuilder.write(cfgSetDoc, writer);
            cfgMgr.saveRxConfiguration(PSConfigurationFactory.RELATIONSHIPS_CFG, 
               writer.toString(), true, PSDeploymentHandler.DEPLOY_SUBSYSTEM);
            cfgSet = null;
                  
            // log txn entry
            int action = exists ? PSTransactionSummary.ACTION_MODIFIED : 
               PSTransactionSummary.ACTION_CREATED;
            addTransactionLogEntry(dep, ctx, cfgName, 
               PSTransactionSummary.TYPE_CMS_OBJECT, action);
         }
         finally 
         {
            // if we didn't save it, try to unlock it
            if (cfgSet != null)
            {
               try 
               {
                  cfgMgr.saveRxConfiguration(
                     PSConfigurationFactory.RELATIONSHIPS_CFG, null, true, 
                     PSDeploymentHandler.DEPLOY_SUBSYSTEM);
               }
               catch (Exception ex) 
               {
                  // well, we tried
               }
            }
         }
         
      }
      catch (PSUnknownNodeTypeException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSServerConfigException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (PSNotLockedException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (IOException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      catch (SAXException e) 
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
   
   //see IPSIdTypeHandler interface
   public PSApplicationIDTypes getIdTypes(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      PSRelationshipConfigSet cfgSet = getRelationshipConfigSet(false);
      PSRelationshipConfig cfg = cfgSet.getConfig(dep.getDependencyId());
      if (cfg == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(), 
            dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
            args);
      }
         
      PSApplicationIDTypes idTypes = new PSApplicationIDTypes(dep);
      
      // exit and effect params, user properties, 
      List mappings = new ArrayList();
      String reqName = dep.getDisplayName();
      
      mappings.clear();
      idTypes.addMappings(reqName, 
         IPSDeployConstants.ID_TYPE_ELEMENT_EXTENSIONS, mappings.iterator());
      
      mappings.clear();
      PSAppTransformer.checkConditionalEffects(mappings, cfg.getEffects(), 
         null);
      idTypes.addMappings(reqName, 
         IPSDeployConstants.ID_TYPE_ELEMENT_EFFECTS, mappings.iterator());
      
      mappings.clear();
      PSAppTransformer.checkProperties(mappings, cfg.getUserDefProperties(), 
         null);
      idTypes.addMappings(reqName, 
         IPSDeployConstants.ID_TYPE_ELEMENT_USER_PROPERTIES, 
         mappings.iterator());
      
      mappings.clear();
      PSAppTransformer.checkProcessChecks(mappings, cfg.getProcessChecks(),
         null);
      idTypes.addMappings(reqName,
         IPSDeployConstants.ID_TYPE_ELEMENT_PROCESS_CHECKS,
         mappings.iterator());
      

      PSCloneOverrideFieldList overrideList = cfg.getCloneOverrideFieldList();
      if (overrideList != null)
      {
         mappings.clear();      
         PSAppTransformer.checkCloneFieldOverrides(mappings, overrideList, 
            null);
         idTypes.addMappings(reqName,
            IPSDeployConstants.ID_TYPE_ELEMENT_CLONE_FIELD_OVERRIDES,
            mappings.iterator());
      }
      
      return idTypes;
   }

   //see IPSIdTypeHandler interface
   public void transformIds(Object object, PSApplicationIDTypes idTypes, 
      PSIdMap idMap) throws PSDeployException
   {
      if (object == null)
         throw new IllegalArgumentException("object may not be null");
      
      if (idTypes == null)
         throw new IllegalArgumentException("idTypes may not be null");
      
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (!(object instanceof PSRelationshipConfig))
      {
         throw new IllegalArgumentException("invalid object type");
      }
      
      PSRelationshipConfig cfg = (PSRelationshipConfig)object;
      
      // walk id types and perform any transforms
      Iterator resources = idTypes.getResourceList(false);
      while (resources.hasNext())
      {
         String resource = (String)resources.next();
         Iterator elements = idTypes.getElementList(resource, false);
         while (elements.hasNext())
         {
            String element = (String)elements.next();
            Iterator mappings = idTypes.getIdTypeMappings(
                  resource, element, false);
            while (mappings.hasNext())
            {
               
               PSApplicationIDTypeMapping mapping = 
                  (PSApplicationIDTypeMapping)mappings.next();
               
               if (mapping.getType().equals(
                  PSApplicationIDTypeMapping.TYPE_NONE))
               {
                  continue;
               }
                 
               if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_EFFECTS))
               {
                  PSAppTransformer.transformConditionalEffects(cfg.getEffects(), 
                     mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_USER_PROPERTIES))
               {
                  PSAppTransformer.transformProperties(
                     cfg.getUserDefProperties(), mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_PROCESS_CHECKS))
               {
                  PSAppTransformer.transformProcessChecks(
                     cfg.getProcessChecks(), mapping, idMap);
               }
               else if (element.equals(
                  IPSDeployConstants.ID_TYPE_ELEMENT_CLONE_FIELD_OVERRIDES))
               {
                  PSAppTransformer.transformCloneFieldOverrides(
                     cfg.getCloneOverrideFieldList(), mapping, idMap);
               }
            }
         }
      }
   }

   /**
    * Load the relationship config set containing all relationship 
    * configurations.
    * 
    * @param edit <code>true</code> to lock for editing, <code>false</code> for 
    * read only.  If <code>true</code>, any existing locks on the config will
    * be overriden.
    * @return the relationshipconfiguration set
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSRelationshipConfigSet getRelationshipConfigSet(boolean edit) 
      throws PSDeployException
   {
      try 
      {
         PSConfigManager cfgMgr = PSConfigManager.getInstance();
         Document cfgDoc = cfgMgr.getRxConfiguration(
            PSConfigurationFactory.RELATIONSHIPS_CFG, edit, true, true, 
            PSDeploymentHandler.DEPLOY_SUBSYSTEM);
         PSRelationshipConfigSet cfgSet = 
            (PSRelationshipConfigSet)PSConfigurationFactory.getConfiguration(
               PSConfigurationFactory.RELATIONSHIPS_CFG, cfgDoc);
               
         return cfgSet;
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "RelationshipDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSExitDefDependencyHandler.DEPENDENCY_TYPE);
   }


}
