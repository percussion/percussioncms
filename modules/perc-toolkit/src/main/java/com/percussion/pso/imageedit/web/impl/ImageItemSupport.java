/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.imageedit.web.impl;

import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.imageedit.data.AbstractImageMetaData;
import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageEditorException;
import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.data.OpenImageResult;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManagerLocator;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.services.cache.ImageCacheManagerLocator;
import com.percussion.pso.utils.RxItemUtils;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ImageItemSupport
{
   private static final String _WIDTH = "_width";

   private static final String _HEIGHT = "_height";

   private static final String _SIZE = "_size";

   private static final String _TYPE = "_type";

   private static final String _EXT = "_ext";

   private static final String _FILENAME = "_filename";

   private static final Logger log = LogManager.getLogger(ImageItemSupport.class);
   
   protected IPSContentWs cws = null; 
   protected IPSGuidManager gmgr = null; 
   protected ImageSizeDefinitionManager isdm = null;
   protected ImageCacheManager cache = null; 
   
   protected void initServices()
   {
      if(cws == null)
      {
         isdm = ImageSizeDefinitionManagerLocator.getImageSizeDefinitionManager();
         cache = ImageCacheManagerLocator.getImageCacheManager(); 
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
         cws = PSContentWsLocator.getContentWebservice(); 
      }
   }
   
   /**
    * Opens an item. 
    * @param contentid the content id
    * @param result result holder where item status will be stored. 
    * @return the item. Never <code>null</code>
    * @throws Exception if the item does not exist or any server error occurs. 
    */
   protected PSCoreItem openItem(String contentid,   OpenImageResult result)
      throws Exception
   {
      initServices();
      IPSGuid guid = gmgr.makeGuid(new PSLocator(contentid)); 
      List<IPSGuid> glist = Collections.<IPSGuid>singletonList(guid);
      List<PSItemStatus> slist = cws.prepareForEdit(glist);
      result.setItemStatus(slist.get(0)); 
      List<PSCoreItem> clist = cws.loadItems(glist, true, false, false, false );
      return clist.get(0);
   }
   
   protected PSItemChild getChild(PSCoreItem item)
   {
      initServices();
      String emsg; 
      if(item == null)
      {
         emsg = "item must not be null";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      String childName = isdm.getSizedImageNodeName();
      PSItemChild child = item.getChildByName(childName);
      if(child == null)
      {
         emsg = "child not found " + childName; 
         log.error(emsg);
         throw new ImageEditorException(emsg);
      }
      return child;
   }
   
   protected String getChildName()
   {
	  log.debug("getChildName: getting child name...");
	  log.debug("getChildName: isdm: " + isdm);
      String childName = isdm.getSizedImageNodeName();
      log.debug("getChildName: what is childname: {}", childName);
      return childName;
   }
   
   protected List<PSItemChildEntry> getChildEntries(String contentid)
     throws Exception
   {
      IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(contentid));
      return getChildEntries(itemGuid);
   }
   
   
   protected List<PSItemChildEntry> getChildEntries(IPSGuid itemGuid ) 
      throws Exception
   {
      initServices();
      String childName = getChildName();
      List<PSItemChildEntry> children = cws.loadChildEntries(itemGuid, childName, true );
      return children; 
   }
   
   protected PSItemChildEntry createChildEntry(String contentid)
      throws Exception
   { 
      IPSGuid itemGuid = gmgr.makeGuid(new PSLocator(contentid)); 
      return createChildEntry(itemGuid); 
   }
   
   protected PSItemChildEntry createChildEntry(IPSGuid itemGuid)
      throws Exception
   {
      List<PSItemChildEntry> elist = cws.createChildEntries(itemGuid, getChildName(), 1); 
      PSItemChildEntry entry = elist.get(0);
      return entry;
   }
   /**
    * Finds a child entry for a specific image size code. 
    * @param entries the list of child entries. Must not be <code>null</code>, may be <code>empty</code>
    * @param code the size code. Must not be <code>null</code> or <code>blank</code>
    * @return the child entry. Will be <code>null</code> if no entry exists for the specified
    * size code. 
    * @throws Exception if the child does not exist. 
    * @throws IllegalArgumentException if the size code is blank. 
    */
   protected PSItemChildEntry findChildEntry(List<PSItemChildEntry>entries, String code) throws Exception 
   { 
      log.debug("findChildEntry: finding child entry");
      if(StringUtils.isBlank(code))
      {
         throw new IllegalArgumentException("entry code must be specified"); 
      }
     
      log.debug("findChildEntry: looking for child {}", code);
      String emsg;
      initServices(); 
      String codeField = isdm.getSizedImagePropertyName(); 
      
      for(PSItemChildEntry entry : entries )
      {
         String entrycode = RxItemUtils.getFieldValue(entry, codeField); 
         if(code.equals(entrycode))
         {
            log.debug("findChildEntry: found entry for code {}", code);
            return entry; 
         }
      }
      log.debug("findChildEntry: entry not found for code {}", code);
      return null; 
   }
   
   
   protected List<IPSGuid> buildGuidList(List<PSItemChildEntry> entries)
   {
       List<IPSGuid> glist = new ArrayList<IPSGuid>();
       for(PSItemChildEntry entry : entries)
       {
          glist.add(entry.getGUID());
       }
       return glist;     
   }
   
   protected void readMetaData(IPSItemAccessor item, Object bean, Map<String, String> fieldMap) 
      throws Exception 
   {
      for(Map.Entry<String, String> entry : fieldMap.entrySet())
      {
         String fieldName = entry.getValue();
         String propertyName = entry.getKey(); 
         log.debug("readMetaData: setting field {} property {}", fieldName, propertyName);
         PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, propertyName);
         if(pd != null && pd.getPropertyType().equals(ImageSizeDefinition.class))
         {
            log.debug("readMetaData: found image size description");
            String size_code = RxItemUtils.getFieldValue(item, fieldName);
            ImageSizeDefinition sd = isdm.getImageSize(size_code); 
            if(sd != null)
            {
               BeanUtils.setProperty(bean, propertyName, sd); 
            }
         }         		
         else if(RxItemUtils.isBinaryField(item, fieldName))
         {
            log.debug("readMetaData: found binary field {}", fieldName);
            ImageData img = new ImageData();
            img.setBinary(RxItemUtils.getFieldBinary(item, fieldName));
            
            readBinaryMetaData(item, img, fieldName);
             
            log.debug("readMetaData: img.getSize(): " + img.getSize());  
            
            if (img.getSize() != 0)
            {
            	log.debug("readMetaData: loading the image details into the image");
	            String imageKey = cache.addImage(img); 
	            if(bean instanceof AbstractImageMetaData)
	            {
	               AbstractImageMetaData mdata = (AbstractImageMetaData) bean;
	               mdata.setImageKey(imageKey); 
	               mdata.setMetaData(new ImageMetaData(img)); 
	               
	               log.debug("readMetaData: setup mdata with imagekey: " + mdata.getImageKey());
	               
	               bean = mdata;
	            }
	        }
            else
            {
            	log.debug("readMetaData: the image size is 0, so nothing has been uploaded. Leaving imagekey and metadata default (maybe null)");
            }
            //BeanUtils.setProperty(bean, propertyName, imageKey); 
         }
         else
         {
            log.debug("readMetaData: setting ordinary property to: " + RxItemUtils.getFieldValueRaw(item, fieldName));
            BeanUtils.setProperty(bean, propertyName, RxItemUtils.getFieldValueRaw(item, fieldName));
         }
      }
      
   }

   protected void writeMetaData(IPSItemAccessor item, Object bean, Map<String,String> fieldMap)
      throws Exception
   {
      String emsg; 
      for(Map.Entry<String, String> entry : fieldMap.entrySet())
      {
         String fieldName = entry.getValue();
         String propertyName = entry.getKey(); 
         
         PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(bean, propertyName);
         if(pd != null && pd.getPropertyType().equals(ImageSizeDefinition.class))
         {
        	
            log.debug("writeMetaData: found image size description");
            ImageSizeDefinition sd = (ImageSizeDefinition) PropertyUtils.getProperty(bean, propertyName); 
            if(sd != null)
            {
               log.debug("writeMetaData: dealing with the size_code field - setting to: " + sd.getCode());
               RxItemUtils.setFieldValue(item, fieldName, sd.getCode());  
            }
         }              
         else if(RxItemUtils.isBinaryField(item, fieldName))
         {
            log.debug("writeMetaData: found binary field {}", fieldName);
            String imageKey = BeanUtils.getProperty(bean, propertyName);

            
            
            log.debug("writeMetaData: imageKey: {}", imageKey);
            if(StringUtils.isNotBlank(imageKey))
            {
               ImageMetaData img = cache.getImage(imageKey);
               log.debug("writeMetaData: got image from cache: " + img.getSize());
               
               ImageData imageData = (ImageData)img;
               log.debug("created ImageData from ImageMetaData");
               
               ByteArrayInputStream byis = new ByteArrayInputStream(imageData.getBinary());
               log.debug("Created a byte array input stream of size");
               
               RxItemUtils.setFieldValue(item, fieldName, byis);
               log.debug("Stored the byte array into the field: {}", fieldName);
               
               if(img != null)
               {  
                  writeBinaryMetaData(item, img, fieldName);
               }
               else
               {
                  log.debug("writeMetaData: no image found in cache for field {} key {}", fieldName, imageKey);
               }  
            }
            else
            {
               log.debug("writeMetaData: no image for field {}", fieldName);
            }
             
         }
         else
         {
            RxItemUtils.setFieldValue(item, fieldName, BeanUtils.getProperty(bean, propertyName));
            log.debug("writeMetaData: dealing with other field: {}", fieldName);
            log.debug("writeMetaData: value being used: {}", BeanUtils.getProperty(bean, propertyName));
         }
      }  
   }
   
   
   protected void readBinaryMetaData(IPSItemAccessor item, ImageMetaData image, String fieldName) 
       throws Exception
   {
        log.debug("reading binary metadata");
        image.setFilename(RxItemUtils.getFieldValue(item, fieldName+_FILENAME)); 
        image.setExt(RxItemUtils.getFieldValue(item, fieldName+_EXT)); 
        image.setMimeType(RxItemUtils.getFieldValue(item, fieldName+_TYPE)); 
        image.setSize(RxItemUtils.getFieldNumeric(item, fieldName+_SIZE).longValue());
        image.setHeight(RxItemUtils.getFieldNumeric(item, fieldName+_HEIGHT).intValue()); 
        image.setWidth(RxItemUtils.getFieldNumeric(item, fieldName+_WIDTH).intValue()); 
   }
   
   protected void writeBinaryMetaData(IPSItemAccessor item, ImageMetaData image, String fieldName)
      throws Exception
   {
	  log.debug("writeBinaryMetaData: writing child image meta data... filename: " + image.getFilename());
      RxItemUtils.setFieldValue(item, fieldName+_FILENAME, image.getFilename()); 
      RxItemUtils.setFieldValue(item, fieldName+ _EXT,  image.getExt()); 
      RxItemUtils.setFieldValue(item, fieldName+_TYPE, image.getMimeType());
      RxItemUtils.setFieldValue(item, fieldName+_SIZE, image.getSize()); 
      RxItemUtils.setFieldValue(item, fieldName+_HEIGHT, image.getHeight()); 
      RxItemUtils.setFieldValue(item, fieldName+_WIDTH, image.getWidth()); 
   }
   
   /**
    * @param cws the cws to set
    */
   public void setCws(IPSContentWs cws)
   {
      this.cws = cws;
   }

   /**
    * @param isdm the isdm to set
    */
   public void setIsdm(ImageSizeDefinitionManager isdm)
   {
      this.isdm = isdm;
   }

   public void setGmgr(IPSGuidManager gmgr)
   {
      this.gmgr = gmgr;
   }

   /**
    * @param cache the cache to set
    */
   public void setCache(ImageCacheManager cache)
   {
      this.cache = cache;
   }
}
