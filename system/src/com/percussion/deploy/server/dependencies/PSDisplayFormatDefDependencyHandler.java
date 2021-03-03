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

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDFMultiProperty;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.percussion.services.error.PSNotFoundException;
import org.w3c.dom.Element;

/**
 * Class to handle packaging and deploying a display format definition.
 */
public class PSDisplayFormatDefDependencyHandler
   extends PSCmsObjectDependencyHandler
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
   public PSDisplayFormatDefDependencyHandler(PSDependencyDef def,
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

      List<PSDependency> childDeps = new ArrayList<>();

      PSDisplayFormat df = loadDisplayFormat(getComponentProcessor(tok),
         dep.getDependencyId());
      if (df == null)
      {
         Object[] args = {dep.getDependencyId(), dep.getObjectTypeName(),
            dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
            args);
      }

      PSDependencyHandler commHandler = getDependencyHandler(
         PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      PSDFMultiProperty commProp = getCommunityProperty(df);
      if (commProp != null)
         childDeps.addAll(getDepsFromMultiValuedProperty(tok, commProp,
            commHandler));

      //Acl deps
      // Acl dep uses ids, but display formats are referenced by names, so get
      // the display format id, set it on the dependency and reset it back to
      // display format name and do this on a clone in case ... of exceptions
      PSDependency d = (PSDependency) dep.clone();
      d.setDependencyId(String.valueOf(df.getDisplayId()));
      addAclDependency(tok, PSTypeEnum.DISPLAY_FORMAT, d, childDeps);
      d.setDependencyId(df.getDisplayName());

      return childDeps.iterator();
    }

   // see base class
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
         throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List<PSDependency> deps = new ArrayList<>();
      Iterator dfs = loadAll(getComponentProcessor(tok));
      while (dfs.hasNext())
      {
         PSDisplayFormat df = (PSDisplayFormat)dfs.next();
         String id = df.getInternalName();
         String name = df.getDisplayName();
         deps.add(createDependency(m_def, id, name));
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
      PSDisplayFormat df = loadDisplayFormat(getComponentProcessor(tok), id);
      if (df != null)
         dep = createDependency(m_def, id, df.getDisplayName());

      return dep;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>Community</li>
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

      // load the component
      PSDisplayFormat df = loadDisplayFormat(getComponentProcessor(tok),
         dep.getDependencyId());
      if (df != null)
         files.add(createDependencyFile(df));

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
         // restore the file
         Iterator files = getDependencyFilesFromArchive(archive, dep);
         Element root = getElementFromFile(archive, dep,
            (PSDependencyFile)files.next());

         // restore the object and clone it
         PSDisplayFormat sourceDispFormat = new PSDisplayFormat(root);
         PSDisplayFormat newDispFormat =
            (PSDisplayFormat)sourceDispFormat.clone();

         // create a list for transaction support
         PSDbComponentCollection dbCompList = new PSDbComponentCollection(
            PSDisplayFormat.class);

         // look for existing
         PSComponentProcessorProxy proc = getComponentProcessor(tok);
         PSDisplayFormat tgtDispFormat = loadDisplayFormat(proc,
            dep.getDependencyId());

         // if it already exists on the target, add it to the list marked for
         // delete so we can "replace" it with the source version
         if (tgtDispFormat != null)
         {
            // first set key value of format we will save using the existing
            // target format's id.
            newDispFormat.setLocator(PSDisplayFormat.createKey(
               new String[]{getIdFromKey(tgtDispFormat,
                  tgtDispFormat.getInternalName())}));

            tgtDispFormat.markForDeletion();
            dbCompList.add(tgtDispFormat);
            
            // keep target version
            newDispFormat.setVersion(tgtDispFormat.getVersion());
         }

         // translate ids in the new version as necessary
         if (ctx.getCurrentIdMap() != null)
         {
            transformIds(ctx, newDispFormat);
         }
         
         // remove 'sys_community' property as this information is in acl
         newDispFormat.removeProperty(PSDisplayFormat.PROP_COMMUNITY);
         
         // add it to the list and save
         dbCompList.add(newDispFormat);
         proc.save(new IPSDbComponent[] {dbCompList});

         // add the transaction to the log
         int action = (tgtDispFormat == null) ?
            PSTransactionSummary.ACTION_CREATED :
            PSTransactionSummary.ACTION_MODIFIED;
         addTransactionLogEntry(dep, ctx, newDispFormat, action);
      }
      catch (PSCmsException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }


   /**
    * Loads the display format specified by the supplied id.
    *
    * @param proc The processor to use, may not be <code>null</code>.
    * @param name The internal name of the format, may not be <code>null</code>
    * or empty.
    *
    * @return The format, or <code>null</code> if no matching format is found.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any errors.
    */
   public PSDisplayFormat loadDisplayFormat(PSComponentProcessorProxy proc, String name)
      throws PSDeployException
   {
      if (proc == null)
         throw new IllegalArgumentException("proc may not be null");
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      // look for existing, match by internal name
      PSDisplayFormat df = null;
      Iterator dfs = loadAll(proc);
      while (dfs.hasNext() && df == null)
      {
         PSDisplayFormat test = (PSDisplayFormat)dfs.next();
         if (test.getInternalName().equalsIgnoreCase(name))
            df = test;
      }

      return df;
   }

   /**
    * Loads the display format specified by the supplied id. A wrapper for
    * doFindDisplayFormatById()
    *
    * @param tok The security token, never <code>null</code>
    * @param depId The display ID of the format
    *
    * @return The format, or <code>null</code> if no matching format is found.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any errors.
    */
//   protected PSDisplayFormat findDisplayFormatById(PSSecurityToken tok, String depId)
//      throws PSDeployException
//   {
//      return doFindDisplayFormatById(getComponentProcessor(tok), depId);
//   }
   
   /**
    * Loads the display format specified by the supplied id.
    *
    * @param proc The processor to use, may not be <code>null</code>.
    * @param depId The display ID of the format
    *
    * @return The format, or <code>null</code> if no matching format is found.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If there are any errors.
    */
//   protected PSDisplayFormat doFindDisplayFormatById(
//         PSComponentProcessorProxy proc, String depId) throws PSDeployException
//   {
//      if (proc == null)
//         throw new IllegalArgumentException("proc may not be null");
//      
//      PSDisplayFormat df = null;
//      
//      //Generate a guid
//      PSGuid guid = new PSGuid(PSTypeEnum.DISPLAY_FORMAT, PSDependencyUtils
//            .getGuidValFromString(depId, m_def.getObjectTypeName()));
//      
//      PSKey[] keys =
//      {PSDisplayFormat.createKey(new String[]
//      {String.valueOf(guid.getUUID())})};  
//      try
//      {
//         Element[] e = proc.load(PSDisplayFormat
//               .getComponentType(PSDisplayFormat.class), keys);
//         if ( e.length > 0 )
//            df =  new PSDisplayFormat(e[0]);
//      }
//      catch (Exception e)
//      {
//         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e
//               .getLocalizedMessage());
//      }
//      return df;
//   }
   
   /**
    * Transforms the child ids in the supplied display format.
    *
    * @param ctx The context to use to get id mappings, assumed not
    * <code>null</code>.
    * @param df The display format to transform, assumed not <code>null</code>.
    *
    * @throws PSDeployException if there are any errors
    */
   private void transformIds(PSImportCtx ctx, PSDisplayFormat df)
      throws PSDeployException
   {
      // transform community ids
      PSDFMultiProperty commProp = getCommunityProperty(df);
      if (commProp != null)
         transformMultiValuedProperty(commProp, ctx,
            PSCommunityDependencyHandler.DEPENDENCY_TYPE);
   }


   /**
    * Loads all display formats.
    *
    * @param proc The processor to use, assumed not <code>null</code>.
    *
    * @return An iterator over zero or more formats, never <code>null</code>.
    *
    * @throws PSDeployException If there are any errors.
    */
   private Iterator loadAll(PSComponentProcessorProxy proc) throws PSDeployException
   {
      try
      {
         Element[] elements = proc.load(PSDisplayFormat.getComponentType(
            PSDisplayFormat.class), null);
         List<PSDisplayFormat> result = new ArrayList<>(
               elements.length);
         for (int i = 0; i < elements.length; i++)
         {
            PSDisplayFormat df = new PSDisplayFormat(elements[i]);
            result.add(df);
         }

         return result.iterator();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }
   }

   /**
    * Gets the property containing defined communities, unless the property
    * specifies "all" communities.
    *
    * @param df The format object to get the property from, assumed not
    * <code>null</code>.
    *
    * @return The property, or <code>null</code> if "all" is defined or the
    * property does not exist.
    */
   private PSDFMultiProperty getCommunityProperty(PSDisplayFormat df)
   {
      PSDFMultiProperty result = null;

      // get community dependencies unless "all" is specified
      if (!df.doesPropertyHaveValue(PSDisplayFormat.PROP_COMMUNITY,
         PSDisplayFormat.PROP_COMMUNITY_ALL))
      {
         // find the community property
         Iterator props = df.getProperties();
         while (props.hasNext() && result == null)
         {
            PSDFMultiProperty prop = (PSDFMultiProperty)props.next();
            if (PSDisplayFormat.PROP_COMMUNITY.equals(prop.getName()))
               result = prop;
         }
      }

      return result;
   }

   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "DisplayFormatDef";

   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<>();

   static
   {
      ms_childTypes.add(PSCommunityDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }



}
