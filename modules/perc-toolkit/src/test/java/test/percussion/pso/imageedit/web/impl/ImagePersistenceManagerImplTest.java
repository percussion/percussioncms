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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemChildEntry;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.imageedit.data.MasterImageMetaData;
import com.percussion.pso.imageedit.data.OpenImageResult;
import com.percussion.pso.imageedit.services.ImageSizeDefinitionManager;
import com.percussion.pso.imageedit.services.cache.ImageCacheManager;
import com.percussion.pso.imageedit.web.impl.ImagePersistenceManagerImpl;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;

public class ImagePersistenceManagerImplTest
{
   private static Log log = LogFactory.getLog(ImagePersistenceManagerImplTest.class);
   
   Mockery context;
   ImagePersistenceManagerImpl cut;
   
   IPSGuidManager gmgr;
   IPSContentWs cws;
   ImageCacheManager cache;
   ImageSizeDefinitionManager isdm;
   
   Map<String, String> parentMap;
   Map<String, String> childMap; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new ImagePersistenceManagerImpl(); 
      parentMap = new HashMap<String, String>();
      childMap = new HashMap<String, String>();
      cut.setChildFieldMap(childMap); 
      cut.setMasterFieldMap(parentMap);
      
      gmgr = context.mock(IPSGuidManager.class);
      cut.setGmgr(gmgr);
      
      cws = context.mock(IPSContentWs.class);
      cut.setCws(cws);
      
      cache = context.mock(ImageCacheManager.class);
      cut.setCache(cache);
      
      isdm = context.mock(ImageSizeDefinitionManager.class); 
      cut.setIsdm(isdm);
      
      cut.setImageContentType("contentType"); 
      
   }
   @Test
   @SuppressWarnings("unchecked")
   public final void testCreateImage()
   {
      log.debug("testing create image");
      
      MasterImageMetaData master = new MasterImageMetaData(); 
      
      try
      {
         final PSCoreItem item = context.mock(PSCoreItem.class);
         final List<PSCoreItem> ilist = Collections.<PSCoreItem>singletonList(item);
         final IPSGuid itemGuid = context.mock(IPSGuid.class);
         final List<IPSGuid> glist = Collections.<IPSGuid>singletonList(itemGuid);
         final PSLocator itemLoc = new PSLocator(42);
         final IPSGuid folderGuid = context.mock(IPSGuid.class,"folderGuid");
         
         context.checking(new Expectations(){{
            one(cws).createItems("contentType", 1);
            will(returnValue(ilist));
            one(cws).saveItems(ilist, false, false );
            will(returnValue(glist));
            atLeast(1).of(isdm).getSizedImageNodeName();
            will(returnValue("childNode"));
            one(cws).addFolderChildren(with(any(IPSGuid.class)), with(any(List.class)));
            one(gmgr).makeLocator(itemGuid);
            will(returnValue(itemLoc));
            one(gmgr).makeGuid(with(any(PSLocator.class)));
            will(returnValue(folderGuid));
         }});
         
         String id = cut.CreateImage(master, "47", false );
         assertTrue(StringUtils.isNotBlank(id));
         assertEquals("42", id);
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception");
      } 
   }
   
   @Test
   public final void testValidateSystemTitleUnique()
   {
      final IPSGuid folderGuid = context.mock(IPSGuid.class);
      final PSItemSummary sum1 = new PSItemSummary(){{
         setName("item1");         
      }};
      final List<PSItemSummary> summs = new ArrayList<PSItemSummary>(){{
         add(sum1);
      }};
      
      try
      {
         context.checking(new Expectations(){{
            one(gmgr).makeGuid(with(any(PSLocator.class)));
            will(returnValue(folderGuid)); 
            one(cws).findFolderChildren(folderGuid, false); 
            will(returnValue(summs)); 
         }});
         
         boolean result = cut.validateSystemTitleUnique("item1", "123" );
         assertFalse(result); 
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception caught"); 
      }
   }
   
   @Test
   @SuppressWarnings("unchecked")
   public final void testOpenImage()
   {
      final PSCoreItem item = context.mock(PSCoreItem.class);
      final List<PSCoreItem> ilist = Collections.<PSCoreItem>singletonList(item);
      final IPSGuid itemGuid = context.mock(IPSGuid.class);
      final List<IPSGuid> glist = Collections.<IPSGuid>singletonList(itemGuid);
      final PSLocator itemLoc = new PSLocator(42);
      final List<PSItemChildEntry> childList = new ArrayList<PSItemChildEntry>();
      
      final PSItemStatus itemStatus = new PSItemStatus(42);
      final List<PSItemStatus> isList = Collections.<PSItemStatus>singletonList(itemStatus);
      try
      {
         context.checking(new Expectations(){{
            atLeast(1).of(gmgr).makeGuid(with(any(PSLocator.class)));
            will(returnValue(itemGuid));
            one(cws).prepareForEdit(with(any(List.class)));
            will(returnValue(isList));   
            one(cws).loadItems(glist, true, false, false, false );
            will(returnValue(ilist));
            one(cws).loadChildEntries(itemGuid, "childNode", true );
            will(returnValue(childList));
            one(isdm).getSizedImageNodeName();
            will(returnValue("childNode"));
         }});
         OpenImageResult oir = cut.OpenImage("42");
         assertNotNull(oir);
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception"); 
      } 
   
   }
   @Test
   @SuppressWarnings("unchecked")
   public final void testUpdateImage()
   {
      MasterImageMetaData master = new MasterImageMetaData(); 
      try
      {
         final PSCoreItem item = context.mock(PSCoreItem.class);
         final List<PSCoreItem> ilist = Collections.<PSCoreItem>singletonList(item);
         final IPSGuid itemGuid = context.mock(IPSGuid.class);
         final List<IPSGuid> glist = Collections.<IPSGuid>singletonList(itemGuid);
         final PSLocator itemLoc = new PSLocator(42);
         final List<PSItemChildEntry> childList = new ArrayList<PSItemChildEntry>();
         
         final PSItemStatus itemStatus = new PSItemStatus(42);
         final List<PSItemStatus> isList = Collections.<PSItemStatus>singletonList(itemStatus);
         
         context.checking(new Expectations(){{
            atLeast(1).of(gmgr).makeGuid(with(any(PSLocator.class)));
            will(returnValue(itemGuid));
            one(cws).prepareForEdit(with(any(List.class)));
            will(returnValue(isList));   
            one(cws).loadItems(glist, true, false, false, false);
            will(returnValue(ilist));
            one(cws).saveItems(ilist, false, false);
            will(returnValue(glist));
            one(cws).loadChildEntries(itemGuid, "childNode", true);
            will(returnValue(childList));
            one(cws).checkinItems(glist, null);
            one(isdm).getSizedImageNodeName();
            will(returnValue("childNode"));
            
            one(isdm).getSizedImageNodeName();
            will(returnValue("childNode"));
         }});
         cut.UpdateImage(master, "42", null );
         
         context.assertIsSatisfied();
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("exception"); 
      } 
   }
}
