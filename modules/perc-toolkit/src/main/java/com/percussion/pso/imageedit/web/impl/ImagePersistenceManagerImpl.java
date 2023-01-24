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
package com.percussion.pso.imageedit.web.impl;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.OpenImageResult;
import com.percussion.pso.imageedit.data.SimpleImageMetaData;
import com.percussion.pso.imageedit.data.SizedImageMetaData;
import com.percussion.pso.imageedit.web.ImagePersistenceManager;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImagePersistenceManagerImpl extends ImageItemSupport 
  implements ImagePersistenceManager   
{
   private static final Logger log = LogManager.getLogger(ImagePersistenceManagerImpl.class);
   
   private boolean extralogging = true; 
   private String imageContentType; 
   
   private Map<String, String> masterFieldMap;
   private Map<String, String> childFieldMap; 
   
   public ImagePersistenceManagerImpl()
   {
      
   }
   
   
   /**
    * @see ImagePersistenceManager#CreateImage(MasterImageMetaData, String, boolean)
    */
   public String CreateImage(MasterImageMetaData master, String folderid,
         boolean checkin) throws Exception
   {
      initServices();
      log.debug("CreateImage: creating image..."); 
      List<PSCoreItem> ilist = cws.createItems(imageContentType, 1);
      log.debug("CreateImage: a content item has been created");
      PSCoreItem item = ilist.get(0);
      log.debug("CreateImage: writing the metadata for master");
      writeMetaData(item, master, masterFieldMap);
      log.debug("CreateImage: master metadata written, going to save");
      List<IPSGuid> glist = cws.saveItems(ilist, false, false);
      log.debug("CreateImage: master saved");
      IPSGuid itemGuid = glist.get(0);
      String childName = getChildName(); 
      List<PSItemChildEntry> allEntries = new ArrayList<PSItemChildEntry>();
      for(SimpleImageMetaData sized : master.getSizedImages().values())
      {
         PSItemChildEntry entry = createChildEntry(itemGuid);
         writeMetaData(entry, sized, this.getChildFieldMap());
         allEntries.add(entry); 
      }
      if(allEntries.size() > 0)
      {       
        cws.saveChildEntries(itemGuid, childName, allEntries ); 
      }
      if(StringUtils.isNotBlank(folderid))
      {
         IPSGuid folderGuid = gmgr.makeGuid(new PSLocator(folderid, "0"));
         cws.addFolderChildren(folderGuid, glist); 
      }
      if(checkin)
      {
         cws.checkinItems(glist, null); 
      }
      PSLocator loc = gmgr.makeLocator(itemGuid);
      return loc.getPart(PSLocator.KEY_ID);
   }
   
   /**
    * 
    * @see ImagePersistenceManager#validateSystemTitleUnique(String, String)
    */
   public boolean validateSystemTitleUnique(String sysTitle, String folderId )
       throws Exception
   {
       initServices();
       Validate.notEmpty(sysTitle);
       log.debug("validateSystemTitleUnique: Title is {}", sysTitle);
       if(StringUtils.isEmpty(folderId))
       {  // no folder id, no problem...
          return true; 
       }
       IPSGuid folder = gmgr.makeGuid(new PSLocator(folderId, "0"));
       List<PSItemSummary> items = cws.findFolderChildren(folder, false);
       log.debug("validateSystemTitleUnique: there are " + items.size() + " folder children "); 
       for(PSItemSummary item : items)
       {
          if(sysTitle.equalsIgnoreCase(item.getName()))
          { // we found a matching title
               log.debug("validateSystemTitleUnique: item name matches: " + item.getContentTypeId() + 
                  " GUID " + item.getGUID());
               return false; 
          }
       }
       return true; 
   }
   /**
    * @see ImagePersistenceManager#OpenImage(String)
    */
   public OpenImageResult OpenImage(String contentid) throws Exception
   {
	  log.debug("OpenImage: Starting to open image");
      
	  OpenImageResult result = new OpenImageResult();
      PSCoreItem item = openItem(contentid,  result); 
      log.debug("OpenImage: successfully opened the PSCoreItem");
      
      MasterImageMetaData master = new MasterImageMetaData(); 
      readMetaData(item, master, this.getMasterFieldMap()); 
      log.debug("OpenImage: read the data into the master image: imageKey: " + master.getImageKey());
     
      for(PSItemChildEntry entry : getChildEntries(contentid ))
      {
         SizedImageMetaData sized = new SizedImageMetaData(); 
         readMetaData(entry, sized, this.getChildFieldMap());
         master.addSizedImage(sized); 
         log.debug("OpenImage: read the data into the sized image [" + sized.getSizeDefinition().getCode() + "] and added to the master");
      }
      result.setMasterImage(master); 
      
      return result; 
   }
   /**
    * @see ImagePersistenceManager#UpdateImage(MasterImageMetaData, String, PSItemStatus)
    */
   public void UpdateImage(MasterImageMetaData image, String contentid,
         PSItemStatus itemStatus) throws Exception
   {
      initServices();
      try
      {
         OpenImageResult result = new OpenImageResult();
         PSCoreItem item = openItem(contentid,  result);
         writeMetaData(item, image, masterFieldMap);
         
         List<PSCoreItem> ilist = Collections.<PSCoreItem>singletonList(item);
         List<IPSGuid> glist = cws.saveItems(ilist, false, false);
         IPSGuid itemGuid = glist.get(0);
         
         List<PSItemChildEntry> entries = getChildEntries(itemGuid);
         List<IPSGuid> childrenToRemove = buildGuidList(entries); //start with a list of all entries
         List<PSItemChildEntry> childrenToSave = new ArrayList<PSItemChildEntry>();
         List<PSItemChildEntry> childrenToInsert = new ArrayList<PSItemChildEntry>(); 
         
         log.debug("UpdateImage: dealing with sized images - there are " + image.getSizedImages().size() + " image(s)");
         for(SizedImageMetaData sized : image.getSizedImages().values())
         {
            String sizeCode = sized.getSizeDefinition().getCode(); 
            log.debug("UpdateImage: in the for loop - trying to find child entry for sized: {}", sizeCode);
            PSItemChildEntry entry =  findChildEntry(entries, sizeCode); 
            if(entry == null)
            { 
               log.debug("creating new entry for {}", sizeCode);
               entry = createChildEntry(itemGuid );
               childrenToInsert.add(entry); 
            }
            else               
            {
               childrenToSave.add(entry);
               //since we found it, we don't have to remove it.
               IPSGuid entryGuid = entry.getGUID();
               log.debug("UpdateImage: found an entry for " + sizeCode + " guid " + entryGuid);
               childrenToRemove.remove(entryGuid); 
            }
            writeMetaData(entry, sized, childFieldMap);
            
            log.debug("UpdateImage: entry is: " + entry);
         }
         String childName = getChildName();
         log.debug("UpdateImage: Dealing with child: {}", childName);
         
         if(childrenToRemove.size() > 0 )
         {
            log.debug("removing " + childrenToRemove.size() + " unused child rows ");          
            cws.deleteChildEntries(itemGuid, childName, childrenToRemove );
         }
         if(childrenToInsert.size() > 0)
         {
            log.debug("inserting " + childrenToInsert.size() + " child entries"); 
            if(log.isDebugEnabled() && extralogging)
            {
               for(PSItemChildEntry entry : childrenToInsert)
               {
                  log.debug("child is " + entry);
                  Iterator<PSItemField> it = entry.getAllFields();
                  while(it.hasNext())
                  {
                     PSItemField fld = it.next(); 
                     log.debug("field is " + fld); 
                  }
               }
            }
            cws.saveChildEntries(itemGuid, childName, childrenToInsert );
         }
         if(childrenToSave.size() > 0 )
         {
            log.debug("saving " + childrenToSave.size() + " child entries"); 
            if(log.isDebugEnabled() && extralogging)
            {
               for(PSItemChildEntry entry : childrenToSave)
               {
                  log.debug("child is " + entry);
                  Iterator<PSItemField> it = entry.getAllFields();
                  while(it.hasNext())
                  {
                     PSItemField fld = it.next(); 
                     log.debug("field is " + fld); 
                  }
               }
            }
            cws.saveChildEntries(itemGuid, childName, childrenToSave );
         }
         
         
         if(itemStatus != null)
         {
            cws.releaseFromEdit(Collections.<PSItemStatus>singletonList(itemStatus), false); 
         }
         else
         {
            cws.checkinItems(Collections.<IPSGuid>singletonList(itemGuid), null);
         }
      } catch (PSErrorsException ee)
      {
          log.error("Server Errors exception " + ee, ee); 
          Map<IPSGuid, Object> errors = ee.getErrors();
          for(Map.Entry<IPSGuid, Object> entry : errors.entrySet())
          {
             log.error("Error for GUID " + entry.getKey() + 
                   " : " + entry.getValue()); 
          }
          throw ee; 
      } catch (Exception ex)
      {
          log.error("Unexpected Exception while updating item Error: {}", ex.getMessage());
          log.debug(ex.getMessage(),ex);
          throw ex; 
      }
   }

  
 
   /**
    * @return the imageContentType
    */
   public String getImageContentType()
   {
      return imageContentType;
   }

   /**
    * @param imageContentType the imageContentType to set
    */
   public void setImageContentType(String imageContentType)
   {
      this.imageContentType = imageContentType;
   }

   public Map<String, String> getMasterFieldMap()
   {
      return masterFieldMap;
   }


   public void setMasterFieldMap(Map<String, String> masterFieldMap)
   {
      this.masterFieldMap = masterFieldMap;
   }


   public Map<String, String> getChildFieldMap()
   {
      return childFieldMap;
   }


   public void setChildFieldMap(Map<String, String> childFieldMap)
   {
      this.childFieldMap = childFieldMap;
   }


 
}
