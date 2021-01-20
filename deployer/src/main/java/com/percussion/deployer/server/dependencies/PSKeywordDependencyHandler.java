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
import com.percussion.deployer.objectstore.PSDependencyFile;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.deployer.server.IPSServiceDependencyHandler;
import com.percussion.deployer.server.PSArchiveHandler;
import com.percussion.deployer.server.PSDbmsHelper;
import com.percussion.deployer.server.PSDependencyDef;
import com.percussion.deployer.server.PSDependencyMap;
import com.percussion.deployer.server.PSImportCtx;
import com.percussion.deployer.services.IPSDeployService;
import com.percussion.deployer.services.PSDeployServiceException;
import com.percussion.deployer.services.PSDeployServiceLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.IPSContentService;
import com.percussion.services.content.PSContentServiceLocator;
import com.percussion.services.content.data.PSKeyword;
import com.percussion.tablefactory.PSJdbcRowData;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.util.PSBijectionMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



/**
 * Class to handle packaging and deploying a keyword definition.
 */
public class PSKeywordDependencyHandler extends PSDataObjectDependencyHandler
   implements IPSServiceDependencyHandler
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
   public PSKeywordDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap)
   {
      super(def, dependencyMap);
   }
      
   // see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildDependencies(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
         
      //Acl deps
      Set<PSDependency> childDeps = new HashSet<PSDependency>();
      addAclDependency(tok, PSTypeEnum.KEYWORD_DEF, dep, childDeps);

      // no children
      return childDeps.iterator();
   }
   
   // see base class
   @Override
   public Iterator<PSDependency> getDependencies(PSSecurityToken tok)
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      // get all keyword lookup categories
      List<PSDependency> deps = new ArrayList<PSDependency>();
      
      List<PSKeyword> keywords = ms_contentSvc.findKeywordsByLabel(null, null);
      for (PSKeyword keyword : keywords)
      {
         String value = keyword.getValue();
         PSDependency dep = createDependency(m_def, value, keyword.getName()); 
         if (keyword.getId() != Long.valueOf(value))
            dep.setDependencyType(PSDependency.TYPE_SYSTEM);
         
         deps.add(dep);
      }
      
      return deps.iterator();
   }
      
   // see base class
   @Override
   public PSDependency getDependency(PSSecurityToken tok, String id) 
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
         
      PSDependency dep = null;
      
      PSKeyword keyword = findKeywordByDependencyID(id);
      if (keyword != null)
      {
         String value = keyword.getValue();
         dep = createDependency(m_def, value, keyword.getName()); 
         
         if (keyword.getId() != Long.valueOf(value))
            dep.setDependencyType(PSDependency.TYPE_SYSTEM);
      }
            
      return dep;      
   }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
   public Iterator getChildTypes()
   {
      return ms_childTypes.iterator();
   }

   // see base class
   @Override
   public String getType()
   {
      return DEPENDENCY_TYPE;
   }

   // see base class
   @Override
   @SuppressWarnings("unchecked")
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
      
      String keyId = dep.getDependencyId();
      PSKeyword keyword = findKeywordByDependencyID(keyId);
      if (keyword == null)
      {
         Object[] args = {keyId, dep.getObjectTypeName(), dep.getDisplayName()};
         throw new PSDeployException(IPSDeploymentErrors.DEP_OBJECT_NOT_FOUND,
               args);
      }
      
      files.add(getDepFileFromKeyword(keyword));
      
      return files.iterator();
   }

   // see base class
   @Override
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
      
      IPSDeployService depSvc = PSDeployServiceLocator.getDeployService();
      try
      {
         depSvc.installDependencyFiles(tok, archive, dep, ctx, this);
      }
      catch (PSDeployServiceException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "error occurred while installing keyword: " +
               e.getLocalizedMessage());
      }
   }

   /**
    * See {@link IPSServiceDependencyHandler#doInstallDependencyFiles(
    * PSSecurityToken, PSArchiveHandler, PSDependency, PSImportCtx)} for
    * details.
    */
   @SuppressWarnings("unchecked")
   public void doInstallDependencyFiles(PSSecurityToken tok,
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

      // retrieve the data from the archive
      Iterator files = getDependecyDataFiles(archive, dep);
      PSDependencyFile file = (PSDependencyFile) files.next();
      
      PSKeyword keyword = null;
      PSIdMapping keyMapping = getIdMapping(ctx, dep); 
      if (keyMapping != null)
         keyword = findKeywordByDependencyID(keyMapping.getTargetId());
      else
         keyword = findKeywordByDependencyID(dep.getDependencyId());
     
      boolean isNew = (keyword == null) ? true : false;
      try
      {
         // load packaged keyword
         String packagedContent = PSDependencyUtils.getFileContentAsString(
               archive, file);
         PSKeyword tempKey = new PSKeyword();
         tempKey.fromXML(packagedContent);
         
         if (!isNew)
         {
            keyword = ms_contentSvc.loadKeyword(keyword.getGUID(), null);
         }
         else
         {
            keyword = ms_contentSvc.createKeyword(tempKey.getLabel(),
                  tempKey.getDescription());
         }
                  
         keyword.copy(tempKey);
         
         // transform the id and value
         // (if mapping is null, then you are installing to same server
         //  so no need to do mapping)
         if (keyMapping != null)
         {
            String tgtId = keyMapping.getTargetId();
            keyword.setId(-1);
            keyword.setId(Long.valueOf(tgtId));
            
            // must clear choices before setting value
            keyword.setChoices(null);
            
            keyword.setValue(tgtId);
         }
         
         keyword.setChoices(tempKey.getChoices());
                
         Integer version = keyword.getVersion();
         if (version != null)
         {
            keyword.setVersion(null);
            
            // bump the current version as hibernate is not incrementing the
            // version on save if the keyword object has not been modified
            keyword.setVersion(version + 1);
         }
         else
         {
            keyword.setVersion(0);
         }
         
         ms_contentSvc.saveKeyword(keyword);

         // add transaction log
         addTransactionLogEntryByGuidType(dep, ctx, PSTypeEnum.KEYWORD_DEF,
               isNew);
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, e,
               "error occurred while installing keyword: " + 
               e.getLocalizedMessage());
      }
   }
   
   // see base class
   @Override
   public void reserveNewId(PSDependency dep, PSIdMap idMap)
      throws PSDeployException
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
      
      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");
      
      if (idMap == null)
         throw new IllegalArgumentException("idMap may not be null");
      
      PSIdMapping mapping = idMap.getMapping(dep.getDependencyId(), 
         dep.getObjectType());
      if (mapping == null)
      {
         Object[] args = {dep.getObjectType(), dep.getDependencyId(), 
            idMap.getSourceServer()};
         throw new PSDeployException(IPSDeploymentErrors.MISSING_ID_MAPPING, 
            args);
      }
      
      if (mapping.isNewObject() && mapping.getTargetId() == null)
      {
         mapping.setTarget(String.valueOf(
            PSDbmsHelper.getInstance().getNextId(LOOKUP_TABLE)), 
            dep.getDisplayName());
      }
      
   }
   
   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id) 
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
         
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");
      
      return getDependency(tok, id) != null;
   }
   
   /**
    * Gets the table data for the entire keyword table.
    *  
    * @return The data, may be <code>null</code> if no rows are found.
    * 
    * @throws PSDeployException if there are any errors.
    */
   PSJdbcTableData getKeywordTableData() throws PSDeployException
   {
      PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();

      PSJdbcTableSchema schema = dbmsHelper.catalogTable(LOOKUP_TABLE, false);
      return dbmsHelper.catalogTableData(schema, null, null);
   }
   
   /**
    * Creates a map of keyword row ids to the corresponding type and value.  
    * Returns a <code>PSBijectionMap</code> so that either the key or value may 
    * be used to retrieve each other.
    * 
    * @param data The table data to use, may not be <code>null</code> and must
    * be data from the <code>RXLOOKUP</code> table.
    * 
    * @return The map, where the key is the row id as a <code>String</code>,
    * and the value is a <code>String</code> containing the lookup type and
    * values concatenated together with a ":" delimiter.  Never 
    * <code>null</code>, may be empty.
    * 
    * @throws PSDeployException if there are any errors.
    */
   @SuppressWarnings("unchecked")
   PSBijectionMap getRowIdMap(PSJdbcTableData data) throws PSDeployException
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");
         
      if (!LOOKUP_TABLE.equalsIgnoreCase(data.getName()))
         throw new IllegalArgumentException("data must be from RXLOOKUP table");
      
      PSBijectionMap map = new PSBijectionMap(data.getRowCount());
      Iterator rows = data.getRows();
      while (rows.hasNext())
      {
         PSJdbcRowData row  = (PSJdbcRowData)rows.next();
         String rowId = getRequiredColumnValue(data.getName(), LOOKUP_ID, row);
         String type = getRequiredColumnValue(data.getName(), LOOKUP_TYPE, row);
         String val = getRequiredColumnValue(data.getName(), LOOKUP_VALUE, row);
         
         map.put(rowId, PSPairDependencyId.getPairDependencyId(type, val));
      }
      
      return map;
   }
   
   /**
    * Get a keyword dependency id from the row id of a keyword entry in the 
    * supplied row id map.  Since only keyword group ids are used as dependency
    * id's, if the supplied row is a group, then its value is returned.  If its
    * not a group, then its type, which indicates the group to which it belongs,
    * is returned.
    * 
    * @param rowIdMap The result of a call to 
    * {@link #getRowIdMap(PSJdbcTableData)}, may not be <code>null</code>}.
    * @param rowId The id of the row in the map to use to get the corresponding
    * keyword dependency id.  May not be <code>null</code> or empty.
    * 
    * @return The dependency id, or <code>null</code> if the specified row id
    * is not found in the map.  Never empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if there are any other errors.
    */
   String getKeywordDepId(PSBijectionMap rowIdMap, String rowId) 
      throws PSDeployException
   {
      if (rowIdMap == null)
         throw new IllegalArgumentException("rowIdMap may not be null");
      
      if (rowId == null || rowId.trim().length() == 0)
         throw new IllegalArgumentException("rowId may not be null or empty");
      
      String depId = null;
      
      String typeValPair = (String)rowIdMap.getValue(rowId);
      
      if (typeValPair != null)
      {
         PSPairDependencyId pairId = new PSPairDependencyId(
            typeValPair);
         String type = pairId.getParentId();
         String val = pairId.getChildId();
         
         // If the type indicates a group, then the value is the id
         // to use to get the dependency.  If not, then the type 
         // is the id to use.
         if (type.equals(LOOKUP_GROUP_KEY))
            depId = val;
         else
            depId = type;
      }
      
      return depId;
   }
   
   /**
    * Transforms any keyword ids in the supplied type-value string.  Since only 
    * keyword group ids are used as dependency id's, if the supplied string
    * specifies a group, then its value is transformed.  If it is not a group, 
    * then its type, which indicates the group to which it belongs, is 
    * transformed.
    * 
    * @param ctx The import context to use, may not be <code>null</code>.
    * @param srcTypeVal The type-value string to transform, in the form 
    * "<type>:<value>".  This is the format of the value side of the map 
    * returned by a call to {@link #getRowIdMap(PSJdbcTableData)}.  May not be
    * <code>null</code> or empty.
    * 
    * @return The transformed string, never <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException If the <code>srcTypeVal</code> string is 
    * malformed, or any other error occurs.
    */
   String transformTypeVal(PSImportCtx ctx, String srcTypeVal) 
      throws PSDeployException
   {
      PSPairDependencyId pairId = new PSPairDependencyId(srcTypeVal);
      String type = pairId.getParentId();
      String val = pairId.getChildId();
      PSIdMapping mapping;
      
      // If the type indicates a group, then the value is the id
      // to use to get the dependency.  If not, then the type 
      // is the id to use.
      if (type.equals(LOOKUP_GROUP_KEY))
      {
         mapping = getIdMapping(ctx, val, DEPENDENCY_TYPE);
         if (mapping != null)
            val = mapping.getTargetId();
      }
      else
      {
         mapping = getIdMapping(ctx, type, DEPENDENCY_TYPE);
         if (mapping != null)
            type = mapping.getTargetId();
      }
      
      return PSPairDependencyId.getPairDependencyId(type, val);
   }
   
   /**
    * Utility method to find the Keyword by a given dependency id(as a string).
    * 
    * @param depId the id which represents the value of the keyword, assumed not
    * <code>null</code>.
    * @return <code>null</code> if Keyword is not found.
    */
   private PSKeyword findKeywordByDependencyID(String depId)
   {
      if (Integer.parseInt(depId) <= 0)
         return null;

      PSKeyword keyword = null;

      List<PSKeyword> keywords = ms_contentSvc.findKeywordsByLabel(null, null);
      for (PSKeyword key : keywords)
      {
         if (String.valueOf(key.getValue()).equals(depId))
         {
            keyword = key;
            break;
         }
      }
   
      return keyword;
   }
   
   /**
    * Creates a dependency file from a given dependency data object.
    * @param keyword the keyword, never <code>null</code>.
    * @return The dependency file object, it will never be <code>null</code>.
    *
    * @throws IllegalArgumentException if any param is invalid.
    * @throws PSDeployException if any other error occurs.
    */
   private PSDependencyFile getDepFileFromKeyword(PSKeyword keyword)
      throws PSDeployException
   {
      if (keyword == null)
         throw new IllegalArgumentException("depData may not be null");
      String str = "";
      try
      {
         str = keyword.toXML();
      }
      catch (Exception e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "Unable to generate a dependency file for Keyword:"
                     + keyword.getName());
      }
      
      return new PSDependencyFile(PSDependencyFile.TYPE_SERVICEGENERATED_XML,
            createXmlFile(str));
   }
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "Keyword";
   
   /**
    * Constant for LOOKUPTYPE column value that defines a lookup type.
    */
   private static final String LOOKUP_GROUP_KEY = "1";
   
   // private constants for table and columns
   private static final String LOOKUP_TABLE = "RXLOOKUP";
   private static final String LOOKUP_VALUE = "LOOKUPVALUE";
   private static final String LOOKUP_TYPE = "LOOKUPTYPE";
   private static final String LOOKUP_ID = "LOOKUPID";
   
   /**
    * Get the content service
    */
   private static IPSContentService ms_contentSvc = 
       PSContentServiceLocator.getContentService();
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List<String> ms_childTypes = new ArrayList<String>();
   static
   {
      ms_childTypes.add(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
   }
   
   
}
