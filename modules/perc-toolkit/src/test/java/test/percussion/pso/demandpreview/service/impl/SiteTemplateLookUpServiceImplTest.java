package test.percussion.pso.demandpreview.service.impl;

import com.percussion.pso.demandpreview.exception.SiteLookUpException;
import com.percussion.pso.demandpreview.service.SiteEditionConfig;
import com.percussion.pso.demandpreview.service.SiteEditionHolder;
import com.percussion.pso.demandpreview.service.SiteEditionLookUpService;
import com.percussion.pso.demandpreview.service.impl.SiteEditionLookUpServiceImpl;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.utils.guid.IPSGuid;
import junit.framework.Assert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class SiteTemplateLookUpServiceImplTest
{
	private static final Logger log = LogManager.getLogger(SiteTemplateLookUpServiceImplTest.class);
	
	Mockery context;
	TestLookUpService lookUp;
	
	IPSSiteManager siteManager;
	IPSPublisherService publisherService;
	IPSAssemblyService asm; 
	SiteEditionLookUpService siteEditionLookUpService;
	
	IPSGuidManager guidManager;

	
	@Before
	public void setUp() throws Exception
	{
		context = new Mockery();
		lookUp = new TestLookUpService();
		siteManager = context.mock(IPSSiteManager.class,"siteManager");
		publisherService = context.mock(IPSPublisherService.class,"publisherService");
		siteEditionLookUpService = context.mock(SiteEditionLookUpService.class,"siteEditionLookupService");
		guidManager = context.mock(IPSGuidManager.class,"guidManager");
		asm = context.mock(IPSAssemblyService.class,"asm");
		lookUp.setSiteManager(siteManager);
		lookUp.setPubisherService(publisherService);
		lookUp.setGuidManager(guidManager);
		lookUp.setAsm(asm); 
	}

	@Test
	@Ignore
	//TODO: Fix testLookUpSiteEdition
	public void testLookUpSiteEdition() throws SiteLookUpException
	{
		Map<String, SiteEditionConfig> siteLookUpMap = new HashMap<>();
		SiteEditionConfig sConfig = new SiteEditionConfig();
		sConfig.setSiteName("psoSite");
		sConfig.setEditionName("psoEdition"); 
		sConfig.setAssemblyContext(1); 
		sConfig.setContextURLRootVar("http://test/");
		siteLookUpMap.put("psoSite", sConfig);
		lookUp.setSiteLookUpMap(siteLookUpMap);
		
		final String siteId = "234";
		final SiteEditionHolder stHolder = new SiteEditionHolder();
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSSite pSite = context.mock(IPSSite.class,"pSite");
		final IPSEdition edition = context.mock(IPSEdition.class,"edition");
		final IPSGuid siteGuid = context.mock(IPSGuid.class,"siteGuid");
		final IPSAssemblyTemplate template = context.mock(IPSAssemblyTemplate.class,"template");
		stHolder.setSite(pSite);
		stHolder.setEdition(edition);
		try
		{
		   context.checking(new Expectations(){{
			one(guidManager).makeGuid(Integer.parseInt(siteId), PSTypeEnum.SITE);
			will(returnValue(siteGuid));
			one(siteManager).loadSite(siteGuid);
			will(returnValue(site));
			atLeast(1).of(site).getName();
			will(returnValue("psoSite"));
			one(publisherService).findEditionByName("psoEdition");
			will(returnValue(edition));		
			one(siteManager).loadSite("psoSite"); 
			will(returnValue(pSite));
		   }});	   
		}
		catch (SiteLookUpException | PSNotFoundException ex)
		{
			log.error("Error looking up site info", ex);
			fail("Exception"); 
		}

		SiteEditionHolder holder = lookUp.LookUpSiteEdition(siteId);
		assertNotNull(holder);
		assertSame(holder.getClass(), stHolder.getClass());
	    context.assertIsSatisfied();
	}
	
	@Test
	public void testLookupWithWrongSite() throws SiteLookUpException
	{
		Map<String, SiteEditionConfig> siteLookUpMap = new HashMap<String, SiteEditionConfig>();
		SiteEditionConfig sConfig = new SiteEditionConfig();
		sConfig.setSiteName("psoSite");
		sConfig.setEditionName("psoTemplate");
		siteLookUpMap.put("siteName", sConfig);
		lookUp.setSiteLookUpMap(siteLookUpMap);
		
		final String siteId = "234";
		final SiteEditionHolder stHolder = new SiteEditionHolder();
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSSite pSite = context.mock(IPSSite.class,"pSite");
		final IPSEdition edition = context.mock(IPSEdition.class,"edition");
		final IPSGuid siteGuid = context.mock(IPSGuid.class,"siteGuid");
		stHolder.setSite(pSite);
		stHolder.setEdition(edition);
		try
		{
		   context.checking(new Expectations(){{
		      one(guidManager).makeGuid(142, PSTypeEnum.SITE);
	            will(returnValue(siteGuid));
	            one(siteManager).loadSite(siteGuid);
	            will(throwException(new PSNotFoundException("Site Not Found")));
	     	   }});	   
		   lookUp.LookUpSiteEdition("142");
           Assert.fail("Test with wrong site name failed");
		}
		catch (SiteLookUpException | PSNotFoundException ex)
		{
			log.info("Got expected exception {}" , ex.getMessage());

		}
		context.assertIsSatisfied();

	}
	
	@Test
	public void testLookWithNoSiteName() throws SiteLookUpException
	{
		Map<String, SiteEditionConfig> siteLookUpMap = new HashMap<String, SiteEditionConfig>();
		SiteEditionConfig sConfig = new SiteEditionConfig();
		sConfig.setSiteName("");
		sConfig.setEditionName("psoTemplate");
		siteLookUpMap.put("site", sConfig);
		lookUp.setSiteLookUpMap(siteLookUpMap);
		
		final String siteId = "234";
		final SiteEditionHolder stHolder = new SiteEditionHolder();
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSSite pSite = context.mock(IPSSite.class,"pSite");
		final IPSEdition edition = context.mock(IPSEdition.class,"edition");
		final IPSGuid siteGuid = context.mock(IPSGuid.class,"siteGuid");
		stHolder.setSite(pSite);
		stHolder.setEdition(edition);
		try
		{
		   context.checking(new Expectations(){{
		      one(guidManager).makeGuid(Integer.parseInt(siteId), PSTypeEnum.SITE);
		      will(returnValue(siteGuid));		
		      one(siteManager).loadSite(siteGuid);
		      will(returnValue(site));
		      atLeast(1).of(site).getName();
		      will(returnValue("siteName"));
		   }});	   

		   lookUp.LookUpSiteEdition(siteId);
		   fail("Test with empty site name failed");
		}
		catch (PSNotFoundException| SiteLookUpException ex)
		{
			log.error("Error looking up site info", ex);
		}
		context.assertIsSatisfied();

	}
	
	@Test
	public void testLookWithNoSiteEdition() throws SiteLookUpException
	{
		Map<String, SiteEditionConfig> siteLookUpMap = new HashMap<String, SiteEditionConfig>();
		SiteEditionConfig sConfig = new SiteEditionConfig();
		sConfig.setSiteName("psoSite");
		sConfig.setEditionName("");
		siteLookUpMap.put("site", sConfig);
		lookUp.setSiteLookUpMap(siteLookUpMap);
		
		final String siteId = "234";
		final SiteEditionHolder stHolder = new SiteEditionHolder();
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSSite pSite = context.mock(IPSSite.class,"pSite");
		final IPSEdition edition = context.mock(IPSEdition.class,"edition");
		final IPSGuid siteGuid = context.mock(IPSGuid.class,"siteGuid");
		stHolder.setSite(pSite);
		stHolder.setEdition(edition);
		try
		{
		   context.checking(new Expectations(){{
			one(guidManager).makeGuid(Integer.parseInt(siteId), PSTypeEnum.SITE);
			will(returnValue(siteGuid));		
			one(siteManager).loadSite(siteGuid);
            will(returnValue(site));
			atLeast(1).of(site).getName();
	        will(returnValue("siteName"));	        
		   }});
		   
		   lookUp.LookUpSiteEdition(siteId);
           fail("Test with empty edition Name failed");
		}
		catch (PSNotFoundException | SiteLookUpException ex)
		{
			log.error("Error looking up site info", ex);
		}

		context.assertIsSatisfied();

	}	
	
	private class TestLookUpService extends SiteEditionLookUpServiceImpl
	{
		@Override
		public void setSiteManager(IPSSiteManager siteManager)
		{
			super.setSiteManager(siteManager);
		}
		@Override
		public void setPubisherService(IPSPublisherService publisherService)
		{
			super.setPubisherService(publisherService);
		}
		@Override
		public void setGuidManager(IPSGuidManager guidManager)
		{
			super.setGuidManager(guidManager);
		}
      /**
       * @see SiteEditionLookUpServiceImpl#setAsm(IPSAssemblyService)
       */
      @Override
      public void setAsm(IPSAssemblyService asm)
      {
          super.setAsm(asm);
      }
		
	}

}
