package test.percussion.pso.demandpreview.servlet;

import static org.junit.Assert.*;

import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.pso.demandpreview.service.DemandPublisherService;
import com.percussion.pso.demandpreview.service.ItemTemplateService;
import com.percussion.pso.demandpreview.service.LinkBuilderService;
import com.percussion.pso.demandpreview.service.SiteEditionHolder;
import com.percussion.pso.demandpreview.service.SiteEditionLookUpService;
import com.percussion.pso.demandpreview.servlet.DemandPreviewController;
import com.percussion.pso.utils.IPSOItemSummaryFinder;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

public class DemandPreviewControllerTest {

	Log log = LogFactory.getLog(DemandPreviewControllerTest.class); 
	Mockery context;
	IPSGuidManager gmgr; 
	TestableDemandPreviewController cut; 
	DemandPublisherService demandSvc; 
	LinkBuilderService linkBuilder; 
	ItemTemplateService itemTemplate; 
	SiteEditionLookUpService siteLookup; 
	IPSOItemSummaryFinder isFinder; 
	
	@Before
	public void setUp() throws Exception {
	   context = new Mockery(); 
	   cut = new TestableDemandPreviewController();
	   gmgr = context.mock(IPSGuidManager.class,"gmgr");
	   cut.setGmgr(gmgr); 
	   demandSvc = context.mock(DemandPublisherService.class,"demandSvc");
	   cut.setDemandPublisherService(demandSvc);
	   linkBuilder = context.mock(LinkBuilderService.class,"linkBuilder");
	   cut.setLinkBuilderService(linkBuilder); 
	   itemTemplate = context.mock(ItemTemplateService.class,"itemTemplate");
	   cut.setItemTemplateService(itemTemplate);
	   siteLookup = context.mock(SiteEditionLookUpService.class,"siteLookup");
	   cut.setSiteEditionLookUpService(siteLookup); 
	   isFinder = context.mock(IPSOItemSummaryFinder.class,"isFinder");
	   cut.setIsFinder(isFinder); 
	}

	@Test
	@Ignore
	//TODO: Fix testDoPublishForPreview
	public void testDoPublishForPreview() {
		final String contentId = "1";
		final String folderId = "2"; 
		final String siteId = "3"; 
		final int pubContextId = 301;
		final PSLocator loc = new PSLocator(1,1); 
		final IPSGuid contentGUID = context.mock(IPSGuid.class,"contentGUID");
		final IPSGuid folderGUID = context.mock(IPSGuid.class,"folderGUID");
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSEdition edition = context.mock(IPSEdition.class,"edition");
		final IPSPublishingContext pubContext = context.mock(IPSPublishingContext.class,"pubContext");
		
		final SiteEditionHolder siteEditionHolder = new SiteEditionHolder(){{
		
			setSite(site);
			setContext(pubContext); 
			setEdition(edition);
		}}; 
		
		final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class,"template");
		final Sequence gseq = context.sequence("guidMgr"); 
		
		try {
			context.checking(new Expectations(){{
			   one(isFinder).getCurrentOrEditLocator("1");
			   will(returnValue(loc)); 
			   one(gmgr).makeGuid(with(any(PSLocator.class))); 
			   inSequence(gseq);
			   will(returnValue(contentGUID)); 
			   one(gmgr).makeGuid(with(any(PSLocator.class)));
			   inSequence(gseq);
			   will(returnValue(folderGUID)); 
			   one(siteLookup).LookUpSiteEdition(siteId);
			   will(returnValue(siteEditionHolder)); 
			   one(itemTemplate).findTemplate(site, contentGUID); 
			   will(returnValue(template));
			   one(demandSvc).publishAndWait(edition, contentGUID, folderGUID);
			   one(linkBuilder).buildLinkUrl(site, template, contentGUID, folderGUID, pubContext,null);
			   will(returnValue("xyz"));
			}}); 
			
			String result = cut.doPublishForPreview(contentId, folderId, siteId);
			assertNotNull(result);
			assertEquals("xyz", result); 
			
			context.assertIsSatisfied(); 
			
		} catch (Exception e) {
			log.error("Exception " + e.getMessage()); 
			fail("Exception Caught"); 
			
		} 
		
	}
	
	private class TestableDemandPreviewController extends DemandPreviewController
	{
		

		@Override
		public void setGmgr(IPSGuidManager gmgr) {
			super.setGmgr(gmgr);
		}

		@Override
		public String doPublishForPreview(String contentId, String folderId,
				String siteId) throws PSAssemblyException, TimeoutException, PSException {
			return super.doPublishForPreview(contentId, folderId, siteId);
		}

		@Override
		public void setIsFinder(IPSOItemSummaryFinder isFinder) {
			super.setIsFinder(isFinder);
		}
		
		
	}
}
