package test.percussion.pso.demandpreview.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.error.PSException;
import com.percussion.pso.demandpreview.exception.SiteLookUpException;
import com.percussion.pso.demandpreview.service.impl.DefaultPageTemplateBean;
import com.percussion.pso.jexl.IPSOObjectFinder;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

public class DefaultPageTemplateBeanTest {
    
	Log log  = LogFactory.getLog(DefaultPageTemplateBeanTest.class); 
	Mockery context; 
	TestableDefaultPageTemplateBean cut; 
	IPSOObjectFinder objFinder; 
	IPSTemplateService tempSvc; 
	
	@Before
	public void setUp() throws Exception {
		context = new Mockery(); 
		cut = new TestableDefaultPageTemplateBean(); 
		objFinder = context.mock(IPSOObjectFinder.class,"objFinder");
		cut.setObjFinder(objFinder);
		tempSvc = context.mock(IPSTemplateService.class,"tempSvc");
		cut.setTempSvc(tempSvc); 
	}

	@Test
	public void testFindTemplateSingle() {
		final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class,"t1");
		final List<IPSAssemblyTemplate> tlist = new ArrayList<IPSAssemblyTemplate>(){{add(t1);}};
		final IPSGuid t1ID = new PSGuid(PSTypeEnum.TEMPLATE, 1L); 
		final IPSGuid ctypeId = new PSGuid(PSTypeEnum.NODEDEF, 42L); 		
		final PSComponentSummary summ = new PSComponentSummary(){{setContentTypeId(42L);}}; 
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSGuid contentId = context.mock(IPSGuid.class,"contentId");
		
		log.info("Testing findTemplate"); 
		try {
			context.checking(new Expectations(){{
			  one(objFinder).getComponentSummary(contentId);
			  	will(returnValue(summ)); 
			  allowing(t1).getGUID();
			  	will(returnValue(t1ID)); 
			  one(site).getAssociatedTemplates();
			  	will(returnValue(Collections.singleton(t1))); 
			  one(tempSvc).findTemplatesByContentType(ctypeId);
			  	will(returnValue(tlist)); 
			  one(t1).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Default)); 
			  one(t1).getOutputFormat();
			    will(returnValue(IPSAssemblyTemplate.OutputFormat.Page)); 
			  allowing(t1).getName();
			  	will(returnValue("TestTemplate1")); 
			}});
			
			IPSAssemblyTemplate result = cut.findTemplate(site, contentId);
			assertNotNull(result); 
			assertEquals(t1, result); 
			context.assertIsSatisfied();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e); 
			fail("Exception caught"); 
		} 
	}
    
	@Test
	public void testFindTemplateMulti() {
		final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class,"t1");
		final IPSAssemblyTemplate t2 = context.mock(IPSAssemblyTemplate.class,"t2");
		final IPSAssemblyTemplate t3 = context.mock(IPSAssemblyTemplate.class,"t3");
		final List<IPSAssemblyTemplate> tlist = new ArrayList<IPSAssemblyTemplate>(){{add(t3); add(t2); add(t1);}};
		final IPSGuid t1ID = new PSGuid(PSTypeEnum.TEMPLATE, 1L); 
		final IPSGuid t2ID = new PSGuid(PSTypeEnum.TEMPLATE, 2L);
		final IPSGuid t3ID = new PSGuid(PSTypeEnum.TEMPLATE, 3L);
		final IPSGuid ctypeId = new PSGuid(PSTypeEnum.NODEDEF, 42L); 		
		final PSComponentSummary summ = new PSComponentSummary(){{setContentTypeId(42L);}}; 
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSGuid contentId = context.mock(IPSGuid.class,"contentId");
		final Set<IPSAssemblyTemplate> siteTemplates = new HashSet<IPSAssemblyTemplate>(){{add(t3); add(t1);}};  
		
		log.info("Testing findTemplate Multiple"); 
		try {
			context.checking(new Expectations(){{
			  one(objFinder).getComponentSummary(contentId);
			  	will(returnValue(summ)); 
			  allowing(t1).getGUID();
			  	will(returnValue(t1ID)); 
			  one(site).getAssociatedTemplates();
			  	will(returnValue(siteTemplates)); 
			  one(tempSvc).findTemplatesByContentType(ctypeId);
			  	will(returnValue(tlist)); 
			  one(t1).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Default)); 
			  one(t1).getOutputFormat();
			    will(returnValue(IPSAssemblyTemplate.OutputFormat.Page)); 
			  allowing(t1).getName();
			  	will(returnValue("TestTemplate1"));
			  one(t2).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Default)); 
			  one(t2).getOutputFormat();
			    will(returnValue(IPSAssemblyTemplate.OutputFormat.Page)); 
			  allowing(t2).getName();
			  	will(returnValue("TestTemplate2")); 
			  allowing(t2).getGUID();
				will(returnValue(t2ID)); 

     		  one(t3).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Never)); 
			  allowing(t3).getOutputFormat();
				will(returnValue(IPSAssemblyTemplate.OutputFormat.Binary)); 
			  allowing(t3).getName();
				will(returnValue("TestTemplate3")); 
			  allowing(t3).getGUID();
				will(returnValue(t3ID)); 

			  	
			}});
			
			IPSAssemblyTemplate result = cut.findTemplate(site, contentId);
			assertNotNull(result); 
			assertEquals(t1, result); 
			context.assertIsSatisfied();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e); 
			fail("Exception caught"); 
		} 
	}
    
	@Test
	public void testFindTemplateNotFound() {
		final IPSAssemblyTemplate t1 = context.mock(IPSAssemblyTemplate.class,"t1");
		final IPSAssemblyTemplate t2 = context.mock(IPSAssemblyTemplate.class,"t2");
		final IPSAssemblyTemplate t3 = context.mock(IPSAssemblyTemplate.class,"t3");
		final List<IPSAssemblyTemplate> tlist = new ArrayList<IPSAssemblyTemplate>(){{add(t3); add(t2); add(t1);}};
		final IPSGuid t1ID = new PSGuid(PSTypeEnum.TEMPLATE, 1L); 
		final IPSGuid t2ID = new PSGuid(PSTypeEnum.TEMPLATE, 2L);
		final IPSGuid t3ID = new PSGuid(PSTypeEnum.TEMPLATE, 3L);
		final IPSGuid ctypeId = new PSGuid(PSTypeEnum.NODEDEF, 42L); 		
		final PSComponentSummary summ = new PSComponentSummary(){{setContentTypeId(42L);}}; 
		final IPSSite site = context.mock(IPSSite.class,"site");
		final IPSGuid contentId = context.mock(IPSGuid.class,"contentId");
		final Set<IPSAssemblyTemplate> siteTemplates = new HashSet<IPSAssemblyTemplate>(){{add(t3); add(t1);}};  
		
		log.info("Testing findTemplate Not Found"); 
		try {
			context.checking(new Expectations(){{
			  one(objFinder).getComponentSummary(contentId);
			  	will(returnValue(summ)); 
			  allowing(t1).getGUID();
			  	will(returnValue(t1ID)); 
			  one(site).getAssociatedTemplates();
			  	will(returnValue(siteTemplates)); 
			  one(tempSvc).findTemplatesByContentType(ctypeId);
			  	will(returnValue(tlist)); 
			  one(t1).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Default)); 
			  one(t1).getOutputFormat();
			    will(returnValue(IPSAssemblyTemplate.OutputFormat.Snippet)); 
			  allowing(t1).getName();
			  	will(returnValue("TestTemplate1"));
			  one(t2).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Default)); 
			  one(t2).getOutputFormat();
			    will(returnValue(IPSAssemblyTemplate.OutputFormat.Page)); 
			  allowing(t2).getName();
			  	will(returnValue("TestTemplate2")); 
			  allowing(t2).getGUID();
				will(returnValue(t2ID)); 

     		  one(t3).getPublishWhen(); 
			  	will(returnValue(IPSAssemblyTemplate.PublishWhen.Never)); 
			  allowing(t3).getOutputFormat();
				will(returnValue(IPSAssemblyTemplate.OutputFormat.Binary)); 
			  allowing(t3).getName();
				will(returnValue("TestTemplate3")); 
			  allowing(t3).getGUID();
				will(returnValue(t3ID)); 

			  allowing(site).getName();
			  	will(returnValue("Mock Site")); 
			}});
			
			IPSAssemblyTemplate result = cut.findTemplate(site, contentId);
			
		} catch (SiteLookUpException sle){	
			log.info("expected exception caught"); 
			context.assertIsSatisfied();
			
		} catch (Exception e) {
			log.error(e.getMessage(), e); 
			fail("Exception caught"); 
		} 
	}
	private class TestableDefaultPageTemplateBean extends DefaultPageTemplateBean
	{

		@Override
		public void setObjFinder(IPSOObjectFinder objFinder) {
			super.setObjFinder(objFinder);
		}

		@Override
		public void setTempSvc(IPSTemplateService tempSvc) {
			super.setTempSvc(tempSvc);
		}
		

	}
}
