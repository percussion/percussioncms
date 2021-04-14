/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.imageedit.web.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;


import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.IPSItemAccessor;
import com.percussion.cms.objectstore.PSBinaryValue;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChild;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSItemFieldMeta;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.pso.imageedit.data.ImageData;
import com.percussion.pso.imageedit.data.ImageMetaData;
import com.percussion.pso.imageedit.data.ImageSizeDefinition;
import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.SizedImageMetaData;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.web.impl.ImageItemSupport;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.webservices.content.IPSContentWs;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class ImageItemSupportTest
{
   private static Log log = LogFactory.getLog(ImageItemSupportTest.class);
   
   Mockery context;
   
   TestableImageItemSupport cut ; 
   
   IPSContentWs cws = null; 
   IPSGuidManager gmgr = null; 
   ImageSizeDefinitionManager isdm = null;
   ImageCacheManager cache = null; 
   /**
    * @throws Exception
    */
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new TestableImageItemSupport(); 
      
      cws = context.mock(IPSContentWs.class);
      gmgr = context.mock(IPSGuidManager.class); 
      isdm = context.mock(ImageSizeDefinitionManager.class);
      cache = context.mock(ImageCacheManager.class); 
      
      cut.setCws(cws);
      cut.setGmgr(gmgr);
      cut.setIsdm(isdm); 
      cut.setCache(cache);
      
   }
   /**
    * Test method for {@link ImageItemSupport#getChild(PSCoreItem)}.
    */
   @Test
   public final void testGetChild()
   {
      final PSItemChild child = context.mock(PSItemChild.class);
      final PSCoreItem item = context.mock(PSCoreItem.class);
      
      context.checking(new Expectations(){{
         one(isdm).getSizedImageNodeName();
         will(returnValue("foo"));
         one(item).getChildByName("foo");
         will(returnValue(child)); 
      }});
      
      PSItemChild c2 = cut.getChild(item);
      assertNotNull(c2);
      assertEquals(child,c2); 
      context.assertIsSatisfied();
   }

   @Test
   public final void testFindChildEntry()
   {
      
      final PSItemChildEntry entrya = context.mock(PSItemChildEntry.class,"entrya");
      final PSItemChildEntry entryb = context.mock(PSItemChildEntry.class,  "entryb");
      final List<PSItemChildEntry> entries = new ArrayList<PSItemChildEntry>(){{
         add(entrya);
         add(entryb);
      }};
      
      final PSItemField flda = context.mock(PSItemField.class,"flda");
      final PSItemField fldb = context.mock(PSItemField.class,"fldb");
      
      final IPSFieldValue vala = context.mock(IPSFieldValue.class,"vala");
      final IPSFieldValue valb = context.mock(IPSFieldValue.class,"valb");
      
      try
      {
         context.checking(new Expectations(){{
            one(isdm).getSizedImagePropertyName();
            will(returnValue("size")); 
            atLeast(1).of(entrya).getFieldByName("size");
            will(returnValue(flda));
            atLeast(1).of(entryb).getFieldByName("size");
            will(returnValue(fldb));
            atLeast(1).of(flda).getValue();
            will(returnValue(vala));
            atLeast(1).of(fldb).getValue();
            will(returnValue(valb));
            allowing(vala).getValueAsString();
            will(returnValue("a")); 
            allowing(valb).getValueAsString();
            will(returnValue("b")); 
         }});
         
         
         PSItemChildEntry result = cut.findChildEntry(entries, "b"); 
         assertNotNull(result);
         assertEquals(entryb, result); 
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
           log.error("Unexpected Exception " + ex,ex);
           fail("Exception caught"); 
      }
      
      
   }
   /**
    * Test method for {@link ImageItemSupport#readMetaData(IPSItemAccessor, Object, Map)}.
    */
   @Test
   public final void testReadMetaData()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      MasterImageMetaData master = new MasterImageMetaData();
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("alt", "fld");
      
      final PSItemField fld = context.mock(PSItemField.class);
      final IPSFieldValue val = context.mock(IPSFieldValue.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      
      try
      {  
         context.checking(new Expectations(){{
            atLeast(1).of(item).getFieldByName("fld");
            will(returnValue(fld));
            atLeast(1).of(fld).getItemFieldMeta();
            will(returnValue(meta));
            one(meta).getBackendDataType();
            will(returnValue(PSItemFieldMeta.DATATYPE_TEXT)); 
            atLeast(1).of(fld).getValue();
            will(returnValue(val));
            atLeast(1).of(val).getValue();
            will(returnValue("Alt Text")); 
         }});
         
         cut.readMetaData(item, master, fldmap );
         
         assertEquals("Alt Text",master.getAlt());
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }
   
   /**
    * Test method for {@link ImageItemSupport#readMetaData(IPSItemAccessor, Object, Map)}.
    */
   @Test
   public final void testReadMetaDataBinary()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      MasterImageMetaData master = new MasterImageMetaData();
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("imageKey", "fld");
      
      final PSItemField fld = context.mock(PSItemField.class);
      final IPSFieldValue val = context.mock(PSBinaryValue.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      final String bval = "The quick brown fox jumps over the lazy dog";
     
      try
      {
         setBinaryReadExpectations(item); 
         
         context.checking(new Expectations(){{
            atLeast(1).of(item).getFieldByName("fld");
            will(returnValue(fld));
            atLeast(1).of(fld).getItemFieldMeta();
            will(returnValue(meta));
            one(meta).getBackendDataType();
            will(returnValue(PSItemFieldMeta.DATATYPE_BINARY)); 
            atLeast(1).of(fld).getValue();
            will(returnValue(val));
            one(cache).addImage(with(any(ImageData.class)));
            will(returnValue("4345364345"));
            
            one(val).getValue();
            will(returnValue(bval.getBytes()));
         }});
         
         cut.readMetaData(item, master, fldmap );
         
         assertNotNull(master.getImageKey());
         log.debug("Master image key" + master.getImageKey());
         assertEquals("4345364345",master.getImageKey());

         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }

   @Test
   public final void testReadMetaDataImageSize()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      SizedImageMetaData sized = new SizedImageMetaData();
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("sizeDefinition", "fld");
      final ImageSizeDefinition sdef = new ImageSizeDefinition();
      sdef.setCode("sizeA");
      sdef.setLabel("Size A"); 
      
      final PSItemField fld = context.mock(PSItemField.class);
      final IPSFieldValue val = context.mock(IPSFieldValue.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      
      try
      {  
         context.checking(new Expectations(){{
            atLeast(1).of(item).getFieldByName("fld");
            will(returnValue(fld));
            atLeast(1).of(fld).getValue();
            will(returnValue(val));
            one(val).getValueAsString();
            will(returnValue("sizeA"));
            one(isdm).getImageSize("sizeA");
            will(returnValue(sdef));
         }});
         
         cut.readMetaData(item, sized, fldmap );
         
         assertNotNull(sized.getSizeDefinition());
         log.debug("Size definition is " + sized.getSizeDefinition()); 
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }
   /**
    * Test method for {@link ImageItemSupport#writeMetaData(IPSItemAccessor, Object, Map)}.
    */
   @Test
   public final void testWriteMetadata()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      MasterImageMetaData master = new MasterImageMetaData();
      master.setAlt("Alt Text"); 
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("alt", "fld");
      
      final PSItemField fld = context.mock(PSItemField.class);
      final IPSFieldValue val = context.mock(IPSFieldValue.class);
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class);
      
      try
      {  
         context.checking(new Expectations(){{
            atLeast(1).of(item).getFieldByName("fld");
            will(returnValue(fld));
            atLeast(1).of(fld).getItemFieldMeta();
            will(returnValue(meta));
            one(fld).clearValues();
            one(fld).addValue(with(any(PSTextValue.class)));
            one(meta).getBackendDataType();
            will(returnValue(PSItemFieldMeta.DATATYPE_TEXT));
            
         }});
         
         cut.writeMetaData(item, master, fldmap );
         
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }
   
   /**
    * Test method for {@link ImageItemSupport#writeMetaData(IPSItemAccessor, Object, Map)}.
    */
   @Test
   public final void testWriteMetadataBinary()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class,"item");
      MasterImageMetaData master = new MasterImageMetaData();
      master.setImageKey("133245");
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("imageKey", "fld");
      
      final PSItemField fld = context.mock(PSItemField.class,"fld");
      final IPSFieldValue val = context.mock(IPSFieldValue.class,"val");
      final PSItemFieldMeta meta = context.mock(PSItemFieldMeta.class,"meta");
      
      final ImageData image = new ImageData();
      image.setBinary("Some String".getBytes());
      image.setFilename("file.name");
      image.setExt("jpg");
      image.setMimeType("text/plain");
      image.setSize(457L);
      image.setHeight(42);
      image.setWidth(37); 
      try
      {  
         setBinaryWriteExpectations(item);
         
         context.checking(new Expectations(){{
            atLeast(1).of(item).getFieldByName("fld");
            will(returnValue(fld));
            atLeast(1).of(fld).clearValues(); 
            atLeast(1).of(fld).addValue(with(any(IPSFieldValue.class))); 
            atLeast(1).of(fld).getItemFieldMeta();
            will(returnValue(meta));
            one(meta).getBackendDataType();
            will(returnValue(PSItemFieldMeta.DATATYPE_BINARY));
            one(cache).getImage("133245");
            will(returnValue(image)); 
         }});
         
         cut.writeMetaData(item, master, fldmap );
         
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }

   /**
    * Test method for {@link ImageItemSupport#writeMetaData(IPSItemAccessor, Object, Map)}.
    */
   @Test
   public final void testWriteMetadataSizeDefinition()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      SizedImageMetaData sized = new SizedImageMetaData();
      final ImageSizeDefinition sdef = new ImageSizeDefinition(){{
         setCode("sizeA");
         setLabel("Size A"); 
         setHeight(42);
         setWidth(37); 
      }};
      sized.setSizeDefinition(sdef);
      Map<String, String> fldmap = new HashMap<String, String>();
      fldmap.put("sizeDefinition", "fld");
      
      final PSItemField fld = context.mock(PSItemField.class);
      final IPSFieldValue val = context.mock(IPSFieldValue.class);
      
      try
      {  
         context.checking(new Expectations(){{
            atLeast(0).of(item).getFieldByName("fld");
            will(returnValue(fld));
            one(fld).clearValues();
            one(fld).addValue(with(any(PSTextValue.class)));
            
         }});
         
         cut.writeMetaData(item, sized, fldmap );
         
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("exception caught"); 
      }
    
   }

   @Test
   public final void testReadBinaryMetaData()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class,"item");
      ImageMetaData image = new ImageMetaData();
      String fieldName = "fld"; 

      try
      {
         
         setBinaryReadExpectations(item);
         
         cut.readBinaryMetaData(item, image, fieldName);
         assertEquals(37, image.getWidth());
         assertEquals(42, image.getHeight());
         assertEquals(487L, image.getSize());
         assertEquals("file.name", image.getFilename());
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception");
      }
   }
   
   /**
    * Test method for {@link ImageItemSupport#writeBinaryMetaData(IPSItemAccessor, ImageMetaData, String)}.
    */
   @Test
   public final void testWriteBinaryMetaData()
   {
      final IPSItemAccessor item = context.mock(IPSItemAccessor.class); 
      ImageMetaData image = new ImageMetaData();
      String fieldName = "fld"; 


      try
      {
         
         setBinaryWriteExpectations(item);
         image.setFilename("file.name");
         image.setExt("jpg");
         image.setMimeType("text/html"); 
         image.setSize(487L);
         image.setHeight(42);
         image.setWidth(37); 
         
         cut.writeBinaryMetaData(item, image, fieldName);
         
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception");
      }
   }
   
   private void setBinaryWriteExpectations(final IPSItemAccessor item)
   {
      final PSItemField fld_filename = context.mock(PSItemField.class,"fld_filename");
      final PSItemField fld_ext = context.mock(PSItemField.class,"fld_ext");
      final PSItemField fld_type = context.mock(PSItemField.class,"fld_type");
      final PSItemField fld_size = context.mock(PSItemField.class,"fld_size");
      final PSItemField fld_height = context.mock(PSItemField.class,"fld_height");
      final PSItemField fld_width = context.mock(PSItemField.class, "fld_width");
      

      try
      {
         context.checking(new Expectations(){{
            one(item).getFieldByName("fld_filename");
            will(returnValue(fld_filename));
            one(item).getFieldByName("fld_ext");
            will(returnValue(fld_ext));
            one(item).getFieldByName("fld_type");
            will(returnValue(fld_type));
            one(item).getFieldByName("fld_size");
            will(returnValue(fld_size));
            one(item).getFieldByName("fld_height");
            will(returnValue(fld_height));
            one(item).getFieldByName("fld_width");
            will(returnValue(fld_width));
 
            one(fld_filename).clearValues();
            one(fld_ext).clearValues();
            one(fld_type).clearValues();
            one(fld_size).clearValues();
            one(fld_width).clearValues();
            one(fld_height).clearValues();
            
            one(fld_filename).addValue(with(any(IPSFieldValue.class))); 
            one(fld_ext).addValue(with(any(IPSFieldValue.class))); 
            one(fld_type).addValue(with(any(IPSFieldValue.class))); 
            one(fld_size).addValue(with(any(IPSFieldValue.class))); 
            one(fld_height).addValue(with(any(IPSFieldValue.class))); 
            one(fld_width).addValue(with(any(IPSFieldValue.class))); 
                        
         }});
      } catch (Exception ex)
      {
         log.error("unexpected exception " + ex , ex); 
         fail("Exception");
      }
   }
   
   private void setBinaryReadExpectations(final IPSItemAccessor item)
   {
   
      final PSItemField fld_filename = context.mock(PSItemField.class,"fld_filename");
      final PSItemField fld_ext = context.mock(PSItemField.class,"fld_ext");
      final PSItemField fld_type = context.mock(PSItemField.class,"fld_type");
      final PSItemField fld_size = context.mock(PSItemField.class,"fld_size");
      final PSItemField fld_height = context.mock(PSItemField.class, "fld_height");
      final PSItemField fld_width = context.mock(PSItemField.class, "fld_width");
      
      final IPSFieldValue val_filename = context.mock(IPSFieldValue.class,"val_filename");
      final IPSFieldValue val_ext = context.mock(IPSFieldValue.class,"val_ext");
      final IPSFieldValue val_type = context.mock(IPSFieldValue.class,"val_type");
      final IPSFieldValue val_size = context.mock(IPSFieldValue.class,"val_size");
      final IPSFieldValue val_height = context.mock(IPSFieldValue.class,"val_height");
      final IPSFieldValue val_width = context.mock(IPSFieldValue.class,"val_width");
      
      try
      {
         context.checking(new Expectations(){{
            one(item).getFieldByName("fld_filename");
            will(returnValue(fld_filename));
            one(item).getFieldByName("fld_ext");
            will(returnValue(fld_ext));
            one(item).getFieldByName("fld_type");
            will(returnValue(fld_type));
            one(item).getFieldByName("fld_size");
            will(returnValue(fld_size));
            one(item).getFieldByName("fld_height");
            will(returnValue(fld_height));
            one(item).getFieldByName("fld_width");
            will(returnValue(fld_width));
 
            one(fld_filename).getValue();
            will(returnValue(val_filename));
            one(fld_ext).getValue();
            will(returnValue(val_ext)); 
            one(fld_type).getValue();
            will(returnValue(val_type)); 
            one(fld_size).getValue();
            will(returnValue(val_size)); 
            one(fld_height).getValue();
            will(returnValue(val_height)); 
            one(fld_width).getValue();
            will(returnValue(val_width)); 

            one(val_filename).getValueAsString();
            will(returnValue("file.name"));            
            one(val_ext).getValueAsString();
            will(returnValue("jpg"));
            one(val_type).getValueAsString();
            will(returnValue("text/html"));
            one(val_size).getValueAsString();
            will(returnValue("487"));
            one(val_height).getValueAsString();
            will(returnValue("42"));
            one(val_width).getValueAsString();
            will(returnValue("37"));
                        
         }});
         
         
        } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception");
      }
      
   }
   
   
   private class TestableImageItemSupport extends ImageItemSupport
   {

      

      /**
       * @see ImageItemSupport#findChildEntry(List, String)
       */
      @Override
      public PSItemChildEntry findChildEntry(List<PSItemChildEntry> entries,
            String code) throws Exception
      {
         return super.findChildEntry(entries, code);
      }

      /**
       * @see ImageItemSupport#getChild(PSCoreItem)
       */
      @Override
      public PSItemChild getChild(PSCoreItem item)
      {

         return super.getChild(item);
      }

      /**
       * @see ImageItemSupport#readBinaryMetaData(IPSItemAccessor, ImageMetaData, String)
       */
      @Override
      public void readBinaryMetaData(IPSItemAccessor item,
            ImageMetaData image, String fieldName) throws Exception
      {

         super.readBinaryMetaData(item, image, fieldName);
      }

      /**
       * @see ImageItemSupport#readMetaData(IPSItemAccessor, Object, Map)
       */
      @Override
      public void readMetaData(IPSItemAccessor item, Object bean,
            Map<String, String> fieldMap) throws Exception
      {

         super.readMetaData(item, bean, fieldMap);
      }

      /**
       * @see ImageItemSupport#writeBinaryMetaData(IPSItemAccessor, ImageMetaData, String)
       */
      @Override
      public void writeBinaryMetaData(IPSItemAccessor item,
            ImageMetaData image, String fieldName) throws Exception
      {
        
         super.writeBinaryMetaData(item, image, fieldName);
      }

      /**
       * @see ImageItemSupport#writeMetaData(IPSItemAccessor, Object, Map)
       */
      @Override
      public void writeMetaData(IPSItemAccessor item, Object bean,
            Map<String, String> fieldMap) throws Exception
      {
         // 
         super.writeMetaData(item, bean, fieldMap);
      }
      
   }
}
