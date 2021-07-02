/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.jexl SiteFolderFinderImplTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.preview;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.jexl.PSOObjectFinder;
import com.percussion.pso.preview.SiteFolderFinderImpl;
import com.percussion.pso.preview.SiteFolderLocation;
import com.percussion.pso.preview.SiteLoader;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.security.IPSSecurityWs;

/**
 * 
 *
 * @author DavidBenua
 *
 */
public class SiteFolderFinderImplTest
{
   private static final Logger log = LogManager.getLogger(SiteFolderFinderImplTest.class);
   
   Mockery context;
   SiteFolderFinderImpl cut;
   
   PSLocator loc; 
   IPSGuidManager gmgr ;
   IPSContentWs cws;
   IPSSiteManager siteMgr;
   IPSSecurityWs secws; 
   SiteLoader siteLoader;
   PSOObjectFinder finder; 
   
   /**
    * @throws Exception
    */
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      cut = new SiteFolderFinderImpl(); 
      gmgr = context.mock(IPSGuidManager.class);
      SiteFolderFinderImpl.setGmgr(gmgr);
      cws = context.mock(IPSContentWs.class);
      SiteFolderFinderImpl.setCws(cws);
      siteMgr = context.mock(IPSSiteManager.class); 
      
      siteLoader = context.mock(SiteLoader.class);
      cut.setSiteLoader(siteLoader); 
       
      secws = context.mock(IPSSecurityWs.class);
      SiteFolderFinderImpl.setSecws(secws);
    
      finder = context.mock(PSOObjectFinder.class);
      SiteFolderFinderImpl.setFinder(finder);
   }
   
   /**
    * Test method for {@link SiteFolderFinderImpl#findSiteFolderLocations(String, String, String)}.
    */
   @Test
   public final void testFindSiteFolderLocationsWithFolderId()
   {
      log.debug("testing site folder previews");
      final PSFolder myFolder = context.mock(PSFolder.class, "myFolder");
      
      final IPSGuid folderGuid = context.mock(IPSGuid.class, "folderGuid"); 
      final IPSSite mySite = context.mock(IPSSite.class, "mySite");
      
      final PSLocator folderLoc = new PSLocator(2);
      final Node myNode = context.mock(Node.class);
      final Property myProperty = context.mock(Property.class);
      
      
      final PSComponentSummary summary = context.mock(PSComponentSummary.class);
      
        
      
      cut.setTestCommunityVisibility(false);
      
      
      try
      {
         context.checking(new Expectations(){{
            atLeast(1).of(gmgr).makeGuid(with(any(PSLocator.class))); 
            will(returnValue(folderGuid)); 
            
            one(cws).findFolderPaths(with(any(IPSGuid.class)));
            will(returnValue(new String[]{"//Sites/foo/bar/baz"})); 
            one(finder).getComponentSummaryById("2"); 
            will(returnValue(summary));
            one(summary).getName();
            will(returnValue("foo"));
            one(siteLoader).findAllSites();
            will(returnValue(Arrays.asList(new IPSSite[]{mySite})));
            atLeast(1).of(mySite).getFolderRoot();
            will(returnValue("//Sites/foo"));
            atLeast(1).of(mySite).getName();
            will(returnValue("foo")); 
         }}); 
         
         List<SiteFolderLocation> locs = cut.findSiteFolderLocations("1", "2", null);
         
         assertNotNull(locs);
         assertEquals(1, locs.size()); 
         
         SiteFolderLocation rloc = locs.get(0); 
         assertEquals("//Sites/foo/bar/baz/foo", rloc.getFolderPath()); 
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
      assertTrue(true); 
      
   }
   
   @Test
   public final void testFindSiteFolderLocationsNoFolderId()
   {
      log.debug("testing site previews no folder id");
      final PSFolder myFolder = context.mock(PSFolder.class, "myFolder");
    
      final PSLocator myFolderLoc = new PSLocator(2,0); 
      
      final IPSGuid folderGuid = context.mock(IPSGuid.class, "folderGuid"); 
      final IPSSite mySite = context.mock(IPSSite.class, "mySite");
     
      cut.setTestCommunityVisibility(false);
      
      try
      {
         context.checking(new Expectations(){{
            one(gmgr).makeGuid(with(any(PSLocator.class))); 
            will(returnValue(folderGuid));
            one(cws).findFolderPaths(with(any(IPSGuid.class))); 
            will(returnValue(new String[]{"//Sites/foo/bar/baz"}));
            one(siteLoader).findAllSites();
            will(returnValue(Arrays.asList(new IPSSite[]{mySite})));
            atLeast(1).of(mySite).getFolderRoot();
            will(returnValue("//Sites/foo"));
            atLeast(1).of(mySite).getName();
            will(returnValue("foo")); 
            one(gmgr).makeLocator(folderGuid);
            will(returnValue(myFolderLoc));
            one(cws).findPathIds("//Sites/foo/bar/baz"); 
            will(returnValue(Arrays.asList(new IPSGuid[]{folderGuid})));
         }}); 
         
         List<SiteFolderLocation> locs = cut.findSiteFolderLocations("1", null, null);
         
         assertNotNull(locs);
         assertEquals(1, locs.size()); 
         
         SiteFolderLocation rloc = locs.get(0); 
         assertEquals("//Sites/foo/bar/baz", rloc.getFolderPath());
         
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
      assertTrue(true); 
      
   }
   
   /**
    * Test method for {@link SiteFolderFinderImpl#findSiteFolderLocations(String, String, String)}.
    */
   @SuppressWarnings("unchecked")
   @Test
   public final void testFindSiteFolderLocationsCommunityFiltering()
   {
      log.debug("testing site folder with community filtering");
      final PSFolder myFolder = context.mock(PSFolder.class, "myFolder");
      
      final IPSGuid folderGuid = context.mock(IPSGuid.class, "folderGuid"); 
      final IPSSite mySite = context.mock(IPSSite.class, "mySite");
      
      final PSLocator folderLoc = new PSLocator(2);
      final Node myNode = context.mock(Node.class);
      final Property myProperty = context.mock(Property.class);
      
     
      final PSComponentSummary summary = context.mock(PSComponentSummary.class);
      
      
      cut.setTestCommunityVisibility(true);
      
      
      try
      {
         context.checking(new Expectations(){{
            atLeast(1).of(gmgr).makeGuid(with(any(PSLocator.class))); 
            will(returnValue(folderGuid)); 
            
            one(cws).findFolderPaths(with(any(IPSGuid.class)));
            will(returnValue(new String[]{"//Sites/foo/bar/baz"})); 
            atLeast(1).of(finder).getComponentSummaryById("2"); 
            will(returnValue(summary));
            one(summary).getName();
            will(returnValue("foo"));
            one(siteLoader).findAllSites();
            will(returnValue(Arrays.asList(new IPSSite[]{mySite})));
            atLeast(1).of(mySite).getFolderRoot();
            will(returnValue("//Sites/foo"));
            atLeast(1).of(mySite).getName();
            will(returnValue("foo")); 
            atLeast(1).of(mySite).getGUID();
            will(returnValue(new PSLegacyGuid(300,1))); 
            one(secws).filterByRuntimeVisibility(with(any(List.class)));
            will(returnValue(Collections.singletonList(new PSLegacyGuid(300,1)))); 
         }}); 
         
         List<SiteFolderLocation> locs = cut.findSiteFolderLocations("1", "2", null);
         
         assertNotNull(locs);
         assertEquals(1, locs.size()); 
         
         SiteFolderLocation rloc = locs.get(0); 
         assertEquals("//Sites/foo/bar/baz/foo", rloc.getFolderPath()); 
         context.assertIsSatisfied();
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
      assertTrue(true); 
      
   }
}
