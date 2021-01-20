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

import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDependencyFile;
import com.percussion.deploy.objectstore.PSDeployComponentUtils;
import com.percussion.deploy.objectstore.PSIdMapping;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.deploy.server.PSArchiveHandler;
import com.percussion.deploy.server.PSDependencyDef;
import com.percussion.deploy.server.PSDependencyMap;
import com.percussion.deploy.server.PSImportCtx;
import com.percussion.i18n.PSLocale;
import com.percussion.i18n.PSLocaleException;
import com.percussion.i18n.PSLocaleManager;
import com.percussion.i18n.PSTmxResourceBundle;
import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.util.PSBijectionMap;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class to handle packaging and deploying a Locale definintion.
 */
@SuppressWarnings(value={"unchecked"})
public class PSLocaleDefDependencyHandler extends PSDependencyHandler
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
    * @throws PSDeployException if there are any errors.
    */
   public PSLocaleDefDependencyHandler(PSDependencyDef def, 
      PSDependencyMap dependencyMap) throws PSDeployException
   {
      super(def, dependencyMap);
      try 
      {
         m_localeMgr = PSLocaleManager.getInstance();
         initTmxTypes();
      }
      catch (PSLocaleException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
   }

   /**
    * Utility method to find the Template by a given guid(as a STRINGGGGGG)
    * @param lStr the language string
    * @return PSLocale may return <code>null</code> if not found
    * @throws PSDeployException
    */
   private PSLocale findLocaleByLanguageString(String lStr)
         throws PSDeployException
   {
      if (lStr == null || lStr.trim().length() == 0)
         throw new IllegalArgumentException(
               "dependency ID may not be null or empty");

      PSLocale loc = null;
      try
      {
         loc = m_localeMgr.getLocale(lStr);
      }
      catch (PSLocaleException e)
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               "could not locate the specified locale, " + lStr + "\n" 
                     + e.getLocalizedMessage());
      }
      return loc;
   }

   
   
   // see base class
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

      Set childDeps = new HashSet();

      PSDependencyHandler fileHandler = getDependencyHandler(
         PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
         
      Iterator files =  m_localeMgr.getResourceFiles(dep.getDependencyId());
      while (files.hasNext())
      {
         File file = (File)files.next();
         PSDependency child = fileHandler.getDependency(tok, 
            PSDeployComponentUtils.stripPathPrefix(file.getPath()));
         if (child != null)
         {
            child.setDependencyType(PSDependency.TYPE_LOCAL);
            childDeps.add(child);
         }
      }
      
      // add dependencies for all tmx keys containing ids so they will be
      // mapped on install
      Document tmxDoc = null;
      try 
      {
         tmxDoc = PSTmxResourceBundle.getMasterResourceDoc(m_rxRoot);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      childDeps.addAll(getTmxDependencies(tok, tmxDoc));
      
      // add translation settings if any
      PSDependency d = getTranslationSettingsDependencies(tok, dep.getDependencyId());
      if ( d != null )
      {
         d.setDependencyType(PSDependency.TYPE_LOCAL);
         childDeps.add(d);  
      }

      //Acl deps
      PSLocale l = findLocaleByLanguageString(dep.getDependencyId());
      PSDependencyHandler h =  
         getDependencyHandler(PSAclDefDependencyHandler.DEPENDENCY_TYPE);
      PSGuid guid = new PSGuid(PSTypeEnum.LOCALE, l.getLocaleId());
      PSDependency aclDep = h.getDependency(tok, String.valueOf(guid.longValue()));
      if ( aclDep != null )
         childDeps.add(aclDep);
      
      return childDeps.iterator();
    }

   // see base class
   @Override
   public Iterator getDependencies(PSSecurityToken tok) throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      List deps = new ArrayList();
      
      try 
      {
         Iterator<PSLocale> locales = m_localeMgr.getLocales();
         while (locales.hasNext())
         {
            PSLocale locale = locales.next();
            deps.add(createDependency(m_def, String.valueOf(locale.getLanguageString()), 
                  locale.getDisplayName()));

         }
      }
      catch (PSLocaleException e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      return deps.iterator();
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
      PSLocale locale = findLocaleByLanguageString(id);
      if (locale != null)
      {
         dep = createDependency(m_def, id, locale.getDisplayName());
      }
      return dep;
   }

   // see base class
   @Override
   public boolean doesDependencyExist(PSSecurityToken tok, String id)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");
      
      if (id == null || id.trim().length() == 0)
         throw new IllegalArgumentException("id may not be null or empty");

      return findLocaleByLanguageString(id) != null;
   }

   /**
    * Provides the list of child dependency types this class can discover.
    * The child types supported by this handler are:
    * <ol>
    * <li>SuppportFile</li>
    * </ol>
    *
    * @return An iterator over zero or more types as <code>String</code>
    * objects, never <code>null</code>, does not contain <code>null</code> or
    * empty entries.
    */
   @Override
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

   /**
    * See base class method for details.  
    * 
    * @return <code>true</code> to ensure that keywords are installed before
    * the locale so that id transformations will find the target keywords 
    * already installed.
    */
   @Override
   public boolean shouldDeferInstallation()
   {
      return true;
   }

   // see base class
   @Override
   public Iterator getDependencyFiles(PSSecurityToken tok, PSDependency dep)
      throws PSDeployException
   {
      if (tok == null)
         throw new IllegalArgumentException("tok may not be null");

      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");

      if (!dep.getObjectType().equals(DEPENDENCY_TYPE))
         throw new IllegalArgumentException("dep wrong type");

      List files = new ArrayList();
      
      try 
      {
         PSLocale locale = findLocaleByLanguageString(dep.getDependencyId());
         if (locale != null)
         {
            // save the locale to a file
            Document doc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(doc, locale.toXml(doc));
            File localeFile = createXmlFile(doc);
            files.add(new PSDependencyFile(
               PSDependencyFile.TYPE_SUPPORT_FILE, localeFile));
            
            // save a tmx file with this languages entries
            IPSTmxDocument langTmxDoc;
            IPSTmxDocument masterTmxDoc = new PSTmxDocument(
               PSTmxResourceBundle.getMasterResourceDoc(m_rxRoot));
            langTmxDoc = masterTmxDoc.extract(
               dep.getDependencyId());
            
            File tmxFile = createXmlFile(langTmxDoc.getDOMDocument());
            files.add(new PSDependencyFile(
               PSDependencyFile.TYPE_SUPPORT_FILE, tmxFile));
               
            // save the entire rxlookup table so we can transform keyword tmx
            // entries on the target
            PSKeywordDependencyHandler keyHandler = 
               (PSKeywordDependencyHandler)getDependencyHandler(
                  PSKeywordDependencyHandler.DEPENDENCY_TYPE);
            PSJdbcTableData keyData = keyHandler.getKeywordTableData();
            Document keyDoc = PSXmlDocumentBuilder.createXmlDocument();
            PSXmlDocumentBuilder.replaceRoot(keyDoc, keyData.toXml(keyDoc));
            files.add(new PSDependencyFile(PSDependencyFile.TYPE_DBMS_DATA, 
               createXmlFile(keyDoc)));
         }   
      }
      catch (Exception e) 
      {
         if (e instanceof PSDeployException)
            throw (PSDeployException)e;
            
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
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

      Iterator files = archive.getFiles(dep);
      PSDependencyFile localeFile = null;
      PSDependencyFile tmxFile = null;
      PSDependencyFile keyDataFile = null;
      int type = PSDependencyFile.TYPE_SUPPORT_FILE;
      while (files.hasNext())
      {
         PSDependencyFile file;
         // get the locale def if we don't have it
         file = (PSDependencyFile)files.next();
         if (localeFile == null && file.getType() == type)
            localeFile = file;
         else if (tmxFile == null && file.getType() == type)
         {
            tmxFile = file;
            // set type for next file
            type = PSDependencyFile.TYPE_DBMS_DATA;
         }
         else if (keyDataFile == null && file.getType() == type)
            keyDataFile = file;
      }
      
      // throw error if one was not found 
      if (tmxFile == null)
      {
         Object[] args = 
         {
            PSDependencyFile.TYPE_ENUM[type], dep.getObjectType(), 
            dep.getDependencyId(), dep.getDisplayName()
         };
         
         throw new PSDeployException(
            IPSDeploymentErrors.MISSING_DEPENDENCY_FILE, args);
      }
      
      
      // restore the objects
      PSLocale tgtLocale;
      IPSTmxDocument srcTmx;
      try 
      {
         // load the target locale if exists, else set it to null. This 
         // behavior should be built into locale mgr, not here
         try 
         {
            tgtLocale = findLocaleByLanguageString(dep.getDependencyId());
         }
         catch ( PSDeployException dex)
         {
            tgtLocale = null;
         }
         
         // restore source locale from archive
         Document localeDoc = PSXmlDocumentBuilder.createXmlDocument(
            archive.getFileData(localeFile), false);
         
         // backup the current info for target locale
         int tgtLocaleID = 0;
         Integer tgtVersion = new Integer(0);
         IPSCmsObjectMgr mgr = PSCmsObjectMgrLocator.getObjectManager();
         boolean isNew = true;
         if ( tgtLocale == null )
         {
            tgtLocale = mgr.createLocale("en-us", "en-us");
            tgtLocaleID = 0;
            tgtVersion = new Integer(0);
         }
         else
         {
            isNew = false;
            tgtLocaleID = tgtLocale.getLocaleId();
            tgtVersion = tgtLocale.getVersion();
         }
         
         // Deserialize  and restore id, version for hibernate updates
         tgtLocale.fromXml(localeDoc.getDocumentElement());
         
      
         // restore tmx doc from archive
         Document tmxDomDoc = PSXmlDocumentBuilder.createXmlDocument(
            archive.getFileData(tmxFile), false);
         
         // transform the ids in the tmx doc
         if (ctx.getCurrentIdMap() != null)
         {
            Document keyDataDoc = PSXmlDocumentBuilder.createXmlDocument(
               archive.getFileData(keyDataFile), false);
            PSJdbcTableData keyData = new PSJdbcTableData(
               keyDataDoc.getDocumentElement());
            transformTmxIds(ctx, dep, tmxDomDoc, keyData);
         }  
         srcTmx = new PSTmxDocument(tmxDomDoc);
         
         // merge the tmx bundle into the master
         IPSTmxDocument masterTmxDoc = new PSTmxDocument(
            PSTmxResourceBundle.getMasterResourceDoc(m_rxRoot));
         
         // set the merge config doc to do what we want
         masterTmxDoc.setMergeConfigDoc(getMergeConfig());
         masterTmxDoc.merge(srcTmx);
         
         // update locale id and save it
         if ( isNew == false )
         {
            // use the existing information on the locale, if new the locale
            // manager has already allocated the correct id and version
            tgtLocale.setLocaleId(tgtLocaleID);
            tgtLocale.setVersion(tgtVersion);
         }
         m_localeMgr.saveLocale(tgtLocale);

         // save the bundle
         PSTmxResourceBundle.getInstance().saveMasterResourceBundle(
            masterTmxDoc, true);
      }
      catch (Exception e) 
      {
         throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR, 
            e.getLocalizedMessage());
      }
      
      // check to see if overwriting   
      int transAction = tgtLocale == null
         ? PSTransactionSummary.ACTION_CREATED
         : PSTransactionSummary.ACTION_MODIFIED;
      
      addTransactionLogEntry(dep, ctx, dep.getDisplayName(), 
         PSTransactionSummary.TYPE_FILE, transAction);
   }

   /**
    * gets the translation settings dependency as an **aggregate**
    * @param tok the security token never <code>null</code>
    * @param id the locale id never <code>null</code> or empty
    * @return the actual dependency, may or may not be <code>null</code>. If 
    * there are no translation settings defined, then <code>null</code> is 
    * returned
    * @throws PSDeployException
    */
   private PSDependency getTranslationSettingsDependencies(PSSecurityToken tok,
         String id) throws PSDeployException 
   {
      if ( StringUtils.isBlank(id) )
         throw new IllegalArgumentException("locale id may not be null or empty");
      PSLocale l = findLocaleByLanguageString(id);

      PSDependencyHandler handler = getDependencyHandler(
            PSTranslationSettingsDefDependencyHandler.DEPENDENCY_TYPE);
      return handler.getDependency(tok, l.getLanguageString());
   }
   /**
    * Get all dependencies specified by id's within the translation unit keys
    * in the supplied tmx document.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param doc The tmx doc, assumed not <code>null</code> and to be a valid
    * tmx document.
    * 
    * @return A set of dependency objects, never <code>null</code>, may be
    * empty.
    * 
    * @throws PSDeployException If the are any errors.
    */
   private Set getTmxDependencies(PSSecurityToken tok, Document doc) 
      throws PSDeployException
   {
      Set deps = new HashSet();
      clearKeyData();
      
      NodeList elements = doc.getElementsByTagName(IPSTmxDtdConstants.ELEM_TU);
      if (elements != null)
      {
         for (int i = 0; i < elements.getLength(); i++) 
         {
            Element unit = (Element)elements.item(i);
            String key = unit.getAttribute(IPSTmxDtdConstants.ATTR_TUID);
            PSDependency dep = getTmxDep(tok, key);
            if (dep != null)
               deps.add(dep);
         }
      }
      
      return deps;
   }
   
   /**
    * Get a dependency represented by the supplied tmx tu id.
    * 
    * @param tok The security token to use, assumed not <code>null</code>.
    * @param tuid The translation unit id to parse for dependent ids, assumed 
    * not <code>null</code> or empty.
    * 
    * @return A dependency, or <code>null</code> if the tu id does not represent
    * one.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSDependency getTmxDep(PSSecurityToken tok, String tuid) 
      throws PSDeployException
   {
      PSDependency dep = null;
      
      // walk all possible tmx dependency types and process any match
      Iterator types = m_tmxTypes.iterator();
      while (types.hasNext())
      {
         PSTmxType tmxType = (PSTmxType)types.next();
         if (tuid.startsWith(tmxType.getPrefix()))
         {
            // try to parse the tuid for an id.  If we don't get one, keep 
            // checking other types
            String depId = null;
            String[] ids = parseTuIds(tuid, tmxType, null);
            if (ids == null)
               continue;

            // If type supports parent, then parent is the first id and type.
            // In this case, we actually want to return the parent as the 
            // dependency, not the child.
            PSDependencyHandler handler = getDependencyHandler(
               tmxType.getTypes()[0]);
            
            // keywords require special handling
            if (handler.getType().equals(
               PSKeywordDependencyHandler.DEPENDENCY_TYPE))
            {
               PSKeywordDependencyHandler keyHandler = 
                  (PSKeywordDependencyHandler)handler;
               PSBijectionMap idMap = keyHandler.getRowIdMap(getKeyData());
               
               // if we don't find a match, assume this is not our key.
               depId = keyHandler.getKeywordDepId(idMap, ids[0]);
            }
            else
               depId = ids[0];
            
            if (depId == null)
               continue;
               
            dep = handler.getDependency(tok, depId);
            break;
         }
      }
   
      return dep;
   }
   
   /**
    * Transforms ids in the translation units of the supplied tmx doc.
    * 
    * @param ctx The import context, assumed not <code>null</code>.
    * @param dep The dependency being installed, assumed not <code>null</code>.
    * @param tmxDoc The doc to translate, assumed not <code>null</code> and to
    * be a valid tmx document.
    * @param srcKeyData The table data of the RXLOOKUP table from the source
    * server restored from the repository, assumed not <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private void transformTmxIds(PSImportCtx ctx, PSDependency dep, 
      Document tmxDoc, PSJdbcTableData srcKeyData) throws PSDeployException
   {
      clearKeyData();
      
      NodeList elements = tmxDoc.getElementsByTagName(
         IPSTmxDtdConstants.ELEM_TU);
      if (elements != null)
      {
         for (int i = 0; i < elements.getLength(); i++) 
         {
            Element unit = (Element)elements.item(i);
            String key = unit.getAttribute(IPSTmxDtdConstants.ATTR_TUID);
            Iterator types = m_tmxTypes.iterator();
            while (types.hasNext())
            {
               PSTmxType tmxType = (PSTmxType)types.next();
               if (key.startsWith(tmxType.getPrefix()))
               {
                  String newKey = key;
                  
                  // try to parse the tuid for an id.  If we don't get one, keep 
                  // checking other types
                  StringBuffer suffix = new StringBuffer();
                  String[] ids = parseTuIds(key, tmxType, suffix);
                  if (ids == null)
                     continue;
                  
                  if (ids.length > 1)
                  {
                     // make sure we found this as a dependency - avoids 
                     // attempts to transform system dependencies that are not
                     // mapped (and don't need to be).  If supports parents, we'll
                     // check for the parent
                     if (doGetChildDependency(dep, ids[0], tmxType.getTypes()[0]) == 
                        null)
                     {
                        continue;
                     }
                     
                     PSIdMapping mapping = getIdMapping(ctx, ids[1], 
                        tmxType.getTypes()[1], ids[0], 
                        tmxType.getTypes()[0]);
                     if (mapping != null)
                     {
                        newKey = tmxType.getPrefix() + 
                           mapping.getTargetParentId() + tmxType.getDelims()[0] 
                           + mapping.getTargetId() + tmxType.getDelims()[1] + 
                           suffix.toString();
                     }
                  }
                  else
                  {
                     // see if we have a keyword id - requires special handling
                     if (tmxType.getTypes()[0].equals(
                        PSKeywordDependencyHandler.DEPENDENCY_TYPE))
                     {
                        PSKeywordDependencyHandler keyHandler = 
                           (PSKeywordDependencyHandler)getDependencyHandler(
                              PSKeywordDependencyHandler.DEPENDENCY_TYPE);
                              
                        PSBijectionMap srcIdMap = keyHandler.getRowIdMap(
                           srcKeyData);

                        // make sure we found this as a dependency - avoids 
                        // attempts to transform system dependencies that are 
                        // not mapped (and don't need to be).  
                        String keyId = keyHandler.getKeywordDepId(srcIdMap, 
                           ids[0]);
                        if (keyId == null || doGetChildDependency(dep, keyId, 
                           tmxType.getTypes()[0]) == null)
                        {
                           continue;
                        }

                        PSBijectionMap tgtIdMap = keyHandler.getRowIdMap(
                           getKeyData());
                           
                        // get source type and value
                        String srcTypeVal = (String)srcIdMap.getValue(ids[0]);
                        if (srcTypeVal != null)
                        {
                           // translate source type and value
                           String tgtTypeVal = keyHandler.transformTypeVal(ctx, 
                              srcTypeVal);
                           
                           // get new row id from tgt map
                           String tgtId = (String)tgtIdMap.getKey(tgtTypeVal);
                           if (tgtId != null)
                           {
                              newKey = tmxType.getPrefix() + tgtId +
                                 tmxType.getDelims()[0] + suffix.toString();
                           }
                        }
                     }
                     else
                     {
                        // make sure we found this as a dependency - avoids 
                        // attempts to transform system dependencies that are 
                        // not mapped (and don't need to be).  
                        if (doGetChildDependency(dep, ids[0], tmxType.getTypes()[0]) == 
                           null)
                        {
                           continue;
                        }
                     
                        PSIdMapping mapping = getIdMapping(ctx, ids[0], 
                           tmxType.getTypes()[0]);
                        if (mapping != null)
                        {
                           newKey = tmxType.getPrefix() + mapping.getTargetId()
                              + tmxType.getDelims()[0] + suffix.toString();
                        }
                     }
                  }
                  
                  unit.setAttribute(IPSTmxDtdConstants.ATTR_TUID, newKey);
                  break;
               }
            }
         }
      }
      
   }
   
   /**
    * Parses the supplied translation unit id for id's based on the supplied
    * tmx type.
    * 
    * @param tuid The string to parse, assumed not <code>null</code> or empty.
    * @param tmxType The type to check for, assumed not <code>null</code>.
    * @param suffix A buffer in which to return the suffix portion of the key
    * after it is parsed. This is everything after the delimiter following the
    * last id discovered.  May be <code>null</code> if this is not needed, 
    * assumed to be empty if supplied.
    * 
    * @return An array of ids, will contain one entry unless the type supports
    * parent ids, in which case the array will contain two entries, the parent
    * id followed by the child id, may be <code>null</code> if no id could be
    * parsed from the key.
    */
   private String[] parseTuIds(String tuid, PSTmxType tmxType, 
      StringBuffer suffix)
   {
      String[] tuIds = null;
      
      String[] delims = tmxType.getDelims();
      if (delims.length > 1)
      {
         // this means this type supports parent type
         String idStr = tuid.substring(tmxType.getPrefix().length());
         int endParIdPos = idStr.indexOf(delims[0]);
         if (endParIdPos != -1)
         {
            String parId = idStr.substring(0, endParIdPos);
            idStr = idStr.substring(endParIdPos + 1);
            if (idStr.trim().length() > 0)
            {
               int endChildIdPos = idStr.indexOf(delims[1]);
               if (endChildIdPos != -1)
               {
                  String childId = idStr.substring(0, endChildIdPos);
                  if (childId.trim().length() > 0)
                  {
                     tuIds = new String[] {parId, childId};
                     if (suffix != null && idStr.length() > endChildIdPos + 1)
                        suffix.append(idStr.substring(endChildIdPos + 1));
                  }
               }
            }
         }
      }
      else
      {
         String idStr = tuid.substring(tmxType.getPrefix().length());
         int endPos = idStr.indexOf(delims[0]);
         if (endPos != -1)
         {
            String id = idStr.substring(0, endPos);
            tuIds = new String[] {id};
            if (suffix != null && idStr.length() > endPos + 1)
               suffix.append(idStr.substring(endPos + 1));
         }
      }
      
      return tuIds;
   }
   
   /**
    * Clears the cached RXLOOKUP table data, which is cached by a call to 
    * {@link #getKeyData()}.
    */
   private void clearKeyData()
   {
      m_keyData = null;
   }
   
   /**
    * Gets the RXLOOKUP table data, will be cached until a call to 
    * {@link #clearKeyData()}.
    * 
    * @return The data, never <code>null</code>.
    * 
    * @throws PSDeployException if there are any errors.
    */
   private PSJdbcTableData getKeyData() throws PSDeployException
   {
      if (m_keyData == null)
      {
         PSKeywordDependencyHandler keyHandler = 
            (PSKeywordDependencyHandler)getDependencyHandler(
               PSKeywordDependencyHandler.DEPENDENCY_TYPE);
         m_keyData = keyHandler.getKeywordTableData();
      }
      
      return m_keyData;
   }
   
   /**
    * Initializes the list of tmx types used to parse the tmx document for ids.
    */
   private void initTmxTypes()
   {
      m_tmxTypes.clear();
      m_tmxTypes.add(new PSTmxType(TMX_ID_CE, new String[] {DOT}, 
         new String[] {PSCEDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_CONTENT_TYPE, new String[] {AT}, 
         new String[] {PSCEDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_KEYWORD, new String[] {AT}, 
         new String[] {PSKeywordDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_SITE_DESC, new String[] {AT}, 
         new String[] {PSSiteDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_SITE_NAME, new String[] {AT}, 
         new String[] {PSSiteDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_TRANSITION, new String[] {DOT, AT}, 
         new String[] {PSWorkflowDependencyHandler.DEPENDENCY_TYPE, 
         PSTransitionDefDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_VARIANT, new String[] {AT}, 
         new String[] {PSVariantDefDependencyHandler.DEPENDENCY_TYPE}));
         
      m_tmxTypes.add(new PSTmxType(TMX_ID_WORKFLOW, new String[] {AT}, 
         new String[] {PSWorkflowDependencyHandler.DEPENDENCY_TYPE}));
   }
   
   /**
    * This method is used to get the merge config doc used to define the rules 
    * when merging the source tmx doc into the target master tmx doc.
    *
    * @return The doc, never <code>null</code>.
    * 
    * @throws RuntimeException if the file cannot be located.
    * @throws PSDeployException if the doc cannot be loaded.
    */
   private static Document getMergeConfig() throws PSDeployException
   {
      if (ms_mergeConfig == null)
      {
         InputStream in = 
            PSLocaleDefDependencyHandler.class.getResourceAsStream(
               MERGE_CONFIG_FILE);

         if (in == null)
         {
            // this is in the package dir, so it should never be null, but this
            // will be better than a NullPointerException
            throw new RuntimeException("Cannot locate " + MERGE_CONFIG_FILE);
         }
         
         try
         {
            ms_mergeConfig = PSXmlDocumentBuilder.createXmlDocument(in, false);
         }
         catch (Exception e)
         {
            throw new PSDeployException(IPSDeploymentErrors.UNEXPECTED_ERROR,
               e.getLocalizedMessage());
         }
      }      

      return ms_mergeConfig;
   }
   
   
   /**
    * Constant for this handler's supported type
    */
   final static String DEPENDENCY_TYPE = "LocaleDef";

   /**
    * Singleton instance of the locale manager, initialized during construction,
    * never <code>null</code> or modified after that.
    */
   private PSLocaleManager m_localeMgr;
   
   /**
    * Cached table data for the RXLOOKUP table, used to discover keyword 
    * dependencies and to transform them on install.  Initially 
    * <code>null</code>, set to <code>null</code> by calls to 
    * {@link #clearKeyData()}, and set to a non-<code>null</code> value by
    * a call to {@link #getKeyData()}.
    */
   private PSJdbcTableData m_keyData = null;

   /**
    * Path of the rx root, assumed to be the current directory.  Never 
    * <code>null</code> or empty.
    */
   private static final String m_rxRoot = PSServer.getRxDir().getAbsolutePath();

   /**
    * The name of the merge config doc.
    */   
   private static final String MERGE_CONFIG_FILE = 
      "PSLocaleDefDependencyHandlerResources.xml";
   
   /**
    * The merge config doc used to define the rules when merging the source
    * tmx doc into the target master tmx doc.  <code>null</code> until first
    * call to {@link #getMergeConfig()}, never <code>null</code> or modified
    * after that.
    */
   private static Document ms_mergeConfig = null;
   
   /**
    * List of child types supported by this handler, it will never be
    * <code>null</code> or empty.
    */
   private static List ms_childTypes = new ArrayList();

   /**
    * The prefix for the translation unit id reperesenting a workflow.
    */
   private static final String TMX_ID_WORKFLOW = "psx.workflow.workflow.";
   
   /**
    * The prefix for the translation unit id reperesenting a keyword.
    */
   private static final String TMX_ID_KEYWORD = "psx.keyword.";
   
   /**
    * The prefix for the translation unit id reperesenting a site name.
    */
   private static final String TMX_ID_SITE_NAME = "psx.site.name.";
   
   /**
    * The prefix for the translation unit id reperesenting a site description.
    */
   private static final String TMX_ID_SITE_DESC = "psx.site.description.";
   
   /**
    * The prefix for the translation unit id reperesenting a variant.
    */
   private static final String TMX_ID_VARIANT = "psx.variant.";
   
   /**
    * The prefix for the translation unit id reperesenting a content type.
    */
   private static final String TMX_ID_CONTENT_TYPE = "psx.contenttype.";
   
   /**
    * The prefix for the translation unit id reperesenting a transiton in a 
    * workflow.
    */
   private static final String TMX_ID_TRANSITION = "psx.workflow.transition.";
   
   /**
    * The prefix for the translation unit id reperesenting a content editor 
    * field.
    */
   private static final String TMX_ID_CE = "psx.ce.local.";
   
   /**
    * Constant for the tuid delimeter "@"
    */
   private static final String AT = "@";
   
   /**
    * Constant for the tuid delimeter "."
    */
   private static final String DOT = ".";
   
   /**
    * List of tmx types, never <code>null</code>, empty until ctor calls
    * {@link #initTmxTypes()}, never modified after that.
    */
   private List m_tmxTypes = new ArrayList();
   
   static
   {
      ms_childTypes.add(PSSupportFileDependencyHandler.DEPENDENCY_TYPE);
      ms_childTypes.add(PSTranslationSettingsDefDependencyHandler.DEPENDENCY_TYPE);
   }

   /**
    * Class to encapsulate data about a tmx key and its delimeters and 
    * corresponding dependency types.
    */   
   private class PSTmxType
   {
      /**
       * Construct a type.
       * 
       * @param prefix The prefix of this type's translation unit id, assumed 
       * not <code>null</code> or empty.
       * @param delims An array of delimiters for this key, used when parsing it 
       * to extract ids, specifies the first token following the id.  Normally 
       * there is one, but for types that support parent ids, there are two, the
       * parent's and child's delimiters respectively.  Assumed not 
       * <code>null</code> and to have one or two values.
       * @param types An array of dependency types, corresponding to the type of
       * the ids tokenized by the <code>delims</code> values.  Assumed not 
       * <code>null</code> and to have the same number of values as the
       * <code>delims</code> array.
       */
      public PSTmxType(String prefix, String[] delims, String[] types)
      {
         m_prefix = prefix;
         m_delims = delims;
         m_types = types;
      }
      
      /**
       * Get the prefix string supplied in the ctor.
       * 
       * @return The prefix, never <code>null</code> or empty.
       */
      public String getPrefix()
      {
         return m_prefix;
      }
      
      /**
       * Get the delimiters array supplied in the ctor.
       * 
       * @return The delims, never <code>null</code> or empty.
       */
      public String[] getDelims()
      {
         return m_delims;
      }
      
      /**
       * Get the types array supplied in the ctor.
       * 
       * @return The types, never <code>null</code> or empty.
       */
      private String[] getTypes()
      {
         return m_types;
      }
      
      /**
       * The prefix supplied to the ctor, never <code>null</code> or empty or
       * modified after that.
       */
      private String m_prefix;
      
      /**
       * The delims supplied to the ctor, never <code>null</code> or empty or
       * modified after that.
       */
      private String[] m_delims;
      
      /**
       * The types supplied to the ctor, never <code>null</code> or empty or
       * modified after that.
       */
      private String[] m_types;
   }
   
}
