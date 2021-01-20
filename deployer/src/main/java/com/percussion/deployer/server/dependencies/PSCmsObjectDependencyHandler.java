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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSCmsComponent;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSMultiValuedProperty;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.deployer.error.IPSDeploymentErrors;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base class to provide utility methods for all handlers deploying cms objects.
 */
public abstract class PSCmsObjectDependencyHandler
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
    * @throws PSDeployException if any other error occurs.
    */
   public PSCmsObjectDependencyHandler(PSDependencyDef def,
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
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
    * Gets the id from the supplied key.  Returns the value of the first part of
    * the key.
    *
    * @param comp The component from which the key is retrieved, may not be
    * <code>null</code>.
    * @param name The name of the component, used for error handling.  May not
    * be <code>null</code> or empty.
    *
    * @return The key, never <code>null</code> or emtpy.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If the id cannot be determined.
    */
   protected String getIdFromKey(IPSDbComponent comp, String name)
      throws PSDeployException
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      String id = null;
      PSKey key = comp.getLocator();

      if (key.isAssigned() && key.getPartCount() > 0)
         id = key.getPart(key.getDefinition()[0]);

      if (id == null || id.trim().length() == 0)
      {
         Object[] args = {name, comp.getComponentType()};
         throw new PSDeployException(IPSDeploymentErrors.EXTRACT_ID_FROM_KEY,
            args);
      }

      return id;
   }

   /**
    * Reserves a new id for the supplied dependency and sets it in the supplied
    * <code>idMap</code>.
    *
    * @param dep The depdendency for which the id is to be reserved, may not
    * be <code>null</code>.
    * @param idMap The idMap on which the the new id is to be set, may not be
    * <code>null</code>.
    * @param comp The component represented by the <code>dep</code> object, may
    * not be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * <code>null</code>.
    * @throws PSDeployException if there are any other errors.
    */
   protected void reserveNewId(PSDependency dep, PSIdMap idMap,
      IPSDbComponent comp) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");

      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      PSIdMapping mapping;
      if (dep.supportsParentId())
      {
         mapping = idMap.getMapping(dep.getDependencyId(),
            dep.getObjectType(), dep.getParentId(), dep.getParentType());
      }
      else
      {
         mapping = idMap.getMapping(dep.getDependencyId(),
            dep.getObjectType());
      }
      if (mapping == null)
      {
         Object[] args = {dep.getObjectType(), dep.getDependencyId(),
            idMap.getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING,
            args);
      }

      if (mapping.isNewObject() && (mapping.getTargetId() == null))
      {
         // gen the new id
         try
         {
            // use internal server request
            PSComponentProcessorProxy proc = getComponentProcessor();
            proc.assignKey(new IPSDbComponent[] {comp}, null);
         }
         catch (PSCmsException e)
         {
            Object[] args = {dep.getDisplayName(), dep.getObjectTypeName(),
               e.getLocalizedMessage()};
            throw new PSDeployException(IPSDeploymentErrors.EXTRACT_ID_FROM_KEY,
               args);
         }

         String id = getIdFromKey(comp, dep.getDisplayName());

         // if supports parent id, set with the parent's new id
         if (dep.supportsParentId())
         {
            PSIdMapping parentMapping = idMap.getMapping(
               dep.getParentId(), dep.getParentType());
            if (parentMapping == null)
            {
               Object[] args = {dep.getParentType(), dep.getParentId(),
                  idMap.getSourceServer()};
               throw new PSDeployException(
                  IPSDeploymentErrors.MISSING_ID_MAPPING, args);
            }

            mapping.setTarget(id, dep.getDisplayName(),
               parentMapping.getTargetId(),
               parentMapping.getTargetName());
         }
         else
            mapping.setTarget(id, dep.getDisplayName());
      }

   }

   /**
    * Creates and returns a dependency file for the supplied component.
    *
    * @param comp The component, may not be <code>null</code>.
    *
    * @return The dependency file, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>comp</code> is
    * <code>null</code>.
    * @throws PSDeployException if any other error occurs.
    */
   protected PSDependencyFile createDependencyFile(IPSCmsComponent comp)
      throws PSDeployException
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      Document doc;

      doc = PSXmlDocumentBuilder.createXmlDocument();
      PSXmlDocumentBuilder.replaceRoot(doc, comp.toXml(doc));
      File file = createXmlFile(doc);

      return new PSDependencyFile(PSDependencyFile.TYPE_COMPONENT_XML, file);
   }

   /**
    * Get a list of dependency files for a specified dependency from an archive.
    *
    * @param archive The archive handler to retrieve the dependency files from,
    * may not be <code>null</code>.
    * @param dep The dependency object, may not be <code>null</code>.
    *
    * @return An iterator one or more <code>PSDependencyFile</code> objects,
    * never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there is no dependency file in the
    * archive for the specified dependency object, or any other error occurs.
    */
   protected Iterator getDependencyFilesFromArchive(PSArchiveHandler archive,
      PSDependency dep) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      Iterator files = archive.getFiles(dep);

      if (!files.hasNext())
      {
         Object[] args =
         {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_COMPONENT_XML],
            dep.getObjectType(), dep.getDependencyId(), dep.getDisplayName()
         };
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }

      return files;
   }

   /**
    * Restore the specified file as an XML document and return its root element.
    *
    * @param archive The archive handler for the archive file, may not be
    * <code>null</code>.
    * @param dep The dependency for which the file is to be retrieved, may not
    * be <code>null</code>.
    * @param file The to be retrieved dependency file, may not be
    * <code>null</code> and its type must be
    * {@link PSDependencyFile#TYPE_COMPONENT_XML}
    *
    * @return The element, never <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   protected Element getElementFromFile(PSArchiveHandler archive,
      PSDependency dep, PSDependencyFile file) throws PSDeployException
   {
      if (archive == null)
         throw new IllegalArgumentException("archive may not be null");
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (file == null)
         throw new IllegalArgumentException("file may not be null");

      Document doc = null;
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
      if (root == null)
      {
         Object args[] = {
            PSDependencyFile.TYPE_ENUM[PSDependencyFile.TYPE_COMPONENT_XML],
            dep.getObjectTypeName(), dep.getDependencyId(),
            dep.getDisplayName(), "Null document root"
            };
         throw new PSDeployException(
            IPSDeploymentErrors.INVALID_DEPENDENCY_FILE, args);
      }

      return root;
   }

   /**
    * Get a list of dependencies from the values of the supplied multi valued
    * property.  All values are assumed to specify the id of a dependency whose
    * type is handled by the supplied <code>handler</code>.
    *
    * @param tok The security token to use, may not be <code>null</code>.
    * @param prop The property, may not be <code>null</code>.
    * @param handler The handler to use to create the dependencies, may not be
    * <code>null</code>.
    *
    * @return The list of dependencies, never <code>null</code>, may be empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any other errors.
    */
   protected List<PSDependency> getDepsFromMultiValuedProperty(PSSecurityToken tok,
      PSMultiValuedProperty prop, PSDependencyHandler handler)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null");
      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");

      List<PSDependency> deps = new ArrayList<PSDependency>();
      Iterator vals = prop.iterator();
      while (vals.hasNext())
      {
         String val = (String)vals.next();
         PSDependency dep = handler.getDependency(tok, val);
         if (dep != null)
            deps.add(dep);
      }

      return deps;
   }

   /**
    * Transforms all values in the supplied multi-valued property.
    *
    * @param prop The property whose values are to be transformed, may not be
    * <code>null</code>.
    * @param ctx The import ctx to use to get id mappings, may not be
    * <code>null</code>.
    * @param type The type of dependency each value in the propery represents,
    * used to get the correct id mapppings.  May not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any other errors.
    */
   protected void transformMultiValuedProperty(PSMultiValuedProperty prop,
      PSImportCtx ctx, String type) throws PSDeployException
   {
      if (prop == null)
         throw new IllegalArgumentException("prop may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (type == null || type.trim().length() == 0)
         throw new IllegalArgumentException("type may not be null or empty");

      boolean isMapped = true;

      // get each value and transform it.
      List newIds = new ArrayList();
      Iterator vals = prop.iterator();
      while (vals.hasNext() && isMapped)
      {
         String val = (String)vals.next();
         PSIdMapping idMapping = getIdMapping(ctx, val, type);
         if (idMapping != null)
            newIds.add(idMapping.getTargetId());
         else
            isMapped = false;
      }

      if (isMapped)
      {
         prop.clear();
         prop.add((String[])newIds.toArray(new String[newIds.size()]));
      }
   }

   /**
    * Adds a transacition log summary for the supplied cms component.
    *
    * @param dep The dependency being installed, may not be <code>null</code>.
    * @param ctx The import ctx to use to log the entry, may not be
    * <code>null</code>.
    * @param comp The component for which the transaction is being logged, may
    * not be <code>null</code>.
    * @param action The action taken, one of the
    * <code>PSTransactionSummary.ACTION_XXX</code> values.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any errors.
    */
   protected void addTransactionLogEntry(PSDependency dep, PSImportCtx ctx,
      IPSDbComponent comp, int action) throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      if (ctx == null)
         throw new IllegalArgumentException("ctx may not be null");
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      addTransactionLogEntry(dep, ctx, comp.getComponentType(),
            PSTransactionSummary.TYPE_CMS_OBJECT, action);
   }

   /**
    * Gets the component processor proxy shared by all dependency handlers.
    * Sets the context using a request generated by a call to
    * {@link PSRequest#getContextForRequest()}.  See
    * {@link #getComponentProcessor(PSSecurityToken)} for more information.
    *
    * @return The processor, never <code>null</code>.
    * @throws PSDeployException if the processor cannot be created.
    */
   protected PSComponentProcessorProxy getComponentProcessor() 
      throws PSDeployException
   {
      return getComponentProcessor(PSRequest.getContextForRequest());
   }


   /**
    * Convenience method that calls 
    * {@link #getRelationshipProcessor(PSSecurityToken, Map) 
    * getRelationshipProcessor(tok, null)}
    */
   protected PSRelationshipProcessor getRelationshipProcessor(
      PSSecurityToken tok)
      throws PSDeployException
   {
      return getRelationshipProcessor(tok, null);
   }

   /**
    * Gets the relationship processor proxy shared by all dependency handlers.
    * Sets the context using a request generated from the supplied security
    * token. A single instance of the processor proxy is created the first time
    * this method is called, and is cached thereafter (assuming there is at 
    * least one instance of this class held to prevent garbage collection).  The
    * returned processor is not thread safe - it is assumed that only one thread
    * will use this instance at a time.
    *
    * @param tok The security token to use to set the context on the processor,
    * may not be <code>null</code>.
    * @param params params to set on the request created from the supplied 
    * <code>tok</code>, may be <code>null</code>.
    *
    * @return The processor, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>tok</code> is <code>null</code>.
    * @throws PSDeployException if the processor cannot be created.
    */
   protected PSRelationshipProcessor getRelationshipProcessor(
      PSSecurityToken tok, Map params)
      throws PSDeployException
   {
      PSRequest req = new PSRequest(tok);
      if (params != null)
         req.setParameters(new HashMap(params));
      
      return getRelationshipProcessor(req);
   }
   
   /**
    * Gets the cms component processor proxy shared by all dependency handlers.
    * Sets the context using a request generated from the supplied security
    * token.  A single instance of the processor proxy is created the first time
    * this method is called, and is cached thereafter (assuming there is at least
    * one instance of this class held to prevent garbage collection).  The
    * returned processor is not thread safe - it is assumed that only one thread
    * will use this instance at a time.
    *
    * @param tok The security token to use to set the context on the processor,
    * may not be <code>null</code>.
    *
    * @return The processor, never <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>tok</code> is <code>null</code>.
    * @throws PSDeployException if the processor cannot be created.
    */
   protected PSComponentProcessorProxy getComponentProcessor(PSSecurityToken tok)
      throws PSDeployException
   {
      return getComponentProcessor(new PSRequest(tok));
   }

   /**
    * Gets the relationship processor shared by all dependency handlers.
    * Sets the context using the supplied request.
    * See {@link #getRelationshipProcessor(PSSecurityToken)} for more 
    * information.
    *
    * @param req The request to use to set the context, assumed not
    * <code>null</code>.
    *
    * @return The processor, never <code>null</code>.
    *
    * @throws PSDeployException if the processor cannot be created.
    */
   private PSRelationshipProcessor getRelationshipProcessor(
      PSRequest req) throws PSDeployException
   {
      if (m_relationshipProcessor == null)
      {
            m_relationshipProcessor = PSRelationshipProcessor.getInstance();      
         }

      return m_relationshipProcessor;
   }

   /**
    * Gets the local component processor shared by all dependency handlers.
    * Sets the context using the supplied request.
    * See {@link #getComponentProcessor(PSSecurityToken)} for more information.
    *
    * @param req The request to use to set the context, assumed not
    * <code>null</code>.
    *
    * @return The processor, never <code>null</code>.
    *
    * @throws PSDeployException if the processor cannot be created.
    */
   private PSComponentProcessorProxy getComponentProcessor(PSRequest req)
      throws PSDeployException
   {
      if (m_componentProcessor == null)
      {
         try
         {
            m_componentProcessor = new PSComponentProcessorProxy(
               PSComponentProcessorProxy.PROCTYPE_SERVERLOCAL, req);
         }
         catch (PSCmsException e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }
      }
      else
      {
         m_componentProcessor.setProcessorContext(req);
      }

      return m_componentProcessor;
   }

   /**
    * The relationship proxy processor used by all derived handlers.
    * <code>null</code> until the first call to {@link #getRelationshipProcessor(PSRequest)},
    * never <code>null</code> after that.  Each call to that method will set the
    * context.
    */
   private PSRelationshipProcessor m_relationshipProcessor = null;

   /**
    * The component proxy processor used by all derived handlers.
    * <code>null</code> until the first call to {@link #getComponentProcessor(PSRequest)},
    * never <code>null</code> after that.  Each call to that method will set the
    * context.
    */
   private PSComponentProcessorProxy m_componentProcessor = null;
}
