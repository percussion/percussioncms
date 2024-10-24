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

package com.percussion.activity.service.impl;

import com.percussion.activity.data.PSActivityNode;
import com.percussion.activity.data.PSContentActivity;
import com.percussion.activity.data.PSContentActivityList;
import com.percussion.activity.data.PSContentActivityRequest;
import com.percussion.activity.data.PSContentTraffic;
import com.percussion.activity.data.PSContentTrafficRequest;
import com.percussion.activity.data.PSEffectiveness;
import com.percussion.activity.data.PSEffectivenessComparator;
import com.percussion.activity.data.PSEffectivenessList;
import com.percussion.activity.data.PSEffectivenessRequest;
import com.percussion.activity.data.PSTrafficDetails;
import com.percussion.activity.data.PSTrafficDetailsList;
import com.percussion.activity.data.PSTrafficDetailsRequest;
import com.percussion.activity.service.IPSActivityService;
import com.percussion.activity.service.IPSContentActivityService;
import com.percussion.activity.service.IPSEffectivenessService;
import com.percussion.activity.service.IPSTrafficService;
import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;

/**
 * See interface for details.
 * @author BJoginipally
 *
 */
@Path("/activity")
@Component("contentActivityService")
@Lazy
public class PSContentActivityService implements IPSContentActivityService 
{
    IPSActivityService activityService;
    IPSEffectivenessService effectivenessService;
    IPSTrafficService trafficService;
    IPSAnalyticsProviderService analyticsProviderService;
    IPSSystemProperties systemProperties;
    
    /**
     * Used for sorting effectiveness results.
     */
    PSEffectivenessComparator eComp = new PSEffectivenessComparator();
          
    @Autowired
    public PSContentActivityService(IPSActivityService activityService, IPSEffectivenessService effectivenessService,
            IPSTrafficService trafficService, IPSAnalyticsProviderService analyticsProviderService, IPSSystemProperties systemProperties)
    {
        this.activityService = activityService;
        this.effectivenessService = effectivenessService;
        this.trafficService = trafficService;
        this.analyticsProviderService = analyticsProviderService;
        this.systemProperties = systemProperties;
    }

    @POST
    @Path("/contentactivity")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public List<PSContentActivity> getContentActivity(PSContentActivityRequest request)
    {
        try {
            //validate the request
            contentActivityReqvalidator.validate(request);

            return new PSContentActivityList(getContentActivity(request.getPath(), request.getDurationType(), request.getDuration(), true));
        } catch (PSValidationException | IPSActivityService.PSActivityServiceException | IPSPathService.PSPathServiceException e) {
            throw new WebApplicationException(e);
        }
    }
    
    @POST
    @Path("/effectiveness")
    @SuppressWarnings("unchecked")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request)
    {
        try
        {
            //validate the request
            contentActivityReqvalidator.validate(request);


            //check if analytics is configured
            PSAnalyticsProviderConfig config = analyticsProviderService.loadConfig(false);
            if (config == null)
            {
                throw new PSAnalyticsProviderException("Analytics has not been setup yet.",
                        CAUSETYPE.ANALYTICS_NOT_CONFIG);
            }
            
            List<PSEffectiveness> eList = new ArrayList<>();
            String durationType = request.getDurationType();
            String duration = request.getDuration();
            String path = request.getPath();
            List<PSContentActivity> caList = getContentActivity(path, durationType, duration, false);
            if (caList.isEmpty())
            {
                // there are no sites/sections
                String[] pathSplit = path.split("/");
                if (pathSplit.length > 2)
                {
                    if (StringUtils.isNotEmpty(pathSplit[2]))
                    {
                        // see if analytics is properly configured for the requested site
                        request.setPath('/' + pathSplit[1] + '/' + pathSplit[2]);
                        String nodeName = pathSplit[pathSplit.length-1];
                        PSContentActivity ca = new PSContentActivity(pathSplit[2], path, nodeName, 0, 0, 0, 0, 0);
                        caList.add(ca);
                        eList = effectivenessService.getEffectiveness(request, caList);
                    }
                }
            }
            else
            {
                eList = effectivenessService.getEffectiveness(request, caList);
                Collections.sort(eList, eComp);
            }
            
            return new PSEffectivenessList(eList);
        }
        catch (PSAnalyticsProviderException | PSValidationException | IPSActivityService.PSActivityServiceException | IPSPathService.PSPathServiceException | IPSGenericDao.LoadException e)
        {
            throw new WebApplicationException(e);
        }
    }
    
    @Override
    @POST
    @Path("/contenttraffic")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSContentTraffic getContentTraffic(PSContentTrafficRequest request) throws PSValidationException {
        try {
            return trafficService.getContentTraffic(request);
        } catch (PSTrafficServiceException e) {
            throw new WebApplicationException(e);
        }
    }
    
    @Override
    @POST
    @Path("/trafficdetails")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTrafficDetails> getTrafficDetails(PSTrafficDetailsRequest request)
    {
        try {
            return new PSTrafficDetailsList(trafficService.getTrafficDetails(request));
        } catch (IPSPathService.PSPathServiceException | PSTrafficServiceException | PSDataServiceException e) {
            throw new WebApplicationException(e);
        }
    }
    
    /**
     * Get the date before the current date for the given duration and the given type of duration.
     * If the duration type is days, then it gives a date that many days prior to the current date.
     * @param dtype assumed not <code>null</code>
     * @param duration 
     * @return the date java.util.Date object never <code>null</code>
     */
    private Date getDurationDate(PSDurationTypeEnum dtype, int duration)
    {
    	Calendar cal = Calendar.getInstance();
    	switch(dtype)
    	{
    		case days:
    			cal.add(Calendar.DATE, -duration);
    			break;
    		case weeks:
    			cal.add(Calendar.DATE, -(duration*7));
    			break;
    		case months:
    			cal.add(Calendar.MONTH, -duration); 
    			break;
    		case years:
    			cal.add(Calendar.YEAR, -duration);
    			break;
    		default:
    			throw new IllegalArgumentException("Invalid duration type.");
    	}
    	return cal.getTime();
    }
    
    private List<PSContentActivity> getContentActivity(String path, String durationType, String duration,
            boolean includeSite) throws IPSActivityService.PSActivityServiceException, IPSPathService.PSPathServiceException {
        List<PSContentActivity> caList = new ArrayList<>();
        
        int timeoutSeconds = NumberUtils.toInt(systemProperties.getProperty(IPSSystemProperties.CONTENT_ACTIVITY_TIME_OUT), DEFAULT_TIMEOUT);
        if (timeoutSeconds <= 0)
            timeoutSeconds = DEFAULT_TIMEOUT;
        
        long timeout = timeoutSeconds * 1000;
        
        StopWatch sw = new StopWatch();
        sw.start();
        
        PSDurationTypeEnum dtype = PSDurationTypeEnum.valueOf(durationType);
        Date durationDate = getDurationDate(dtype,Integer.parseInt(duration));
        
        List<PSActivityNode> nodes = activityService.createActivityNodesByPaths(path, includeSite);
        for (PSActivityNode node : nodes) 
        {
            long remaining = timeout - sw.getTime();
            caList.add(activityService.createActivity(node, durationDate, remaining));
        }
        
        return caList;
    }
    
    private PSAbstractBeanValidator<PSContentActivityRequest> contentActivityReqvalidator = 
    	new PSContentActivityRequestValidator();
    
    /**
     * Content Activity request validator, it checks whether the supplied path is not blank 
     * @author bjoginipally
     *
     */
    public class PSContentActivityRequestValidator extends PSAbstractBeanValidator<PSContentActivityRequest>
    {
        @Override
        protected void doValidation(
        PSContentActivityRequest req,
        PSBeanValidationException e)
        {
            String duration="0";

            try {

                String path = req.getPath();
                String durationType = req.getDurationType();
                duration = req.getDuration();
                rejectIfBlank("contentactivity", "path", path);
                rejectIfBlank("contentactivity", "durationType", durationType);
                rejectIfBlank("contentactivity", "duration", duration);

            	PSDurationTypeEnum dtype = PSDurationTypeEnum.valueOf(req.getDurationType());
            	if(dtype == null)
                	e.rejectValue("Duration Type", "durationtype", "Invalid duration type, valid values are days, weeks, " +
        			"months and years.");            	

            }
            catch (Exception ex) 
            {
            	e.rejectValue("Duration Type", "durationtype", "Invalid duration type, valid values are days, weeks, " +
            			"months and years.");            	
			}
            try
            {
            	Integer.parseInt(duration);
            }
            catch(NumberFormatException nfe)
            {
            	e.rejectValue("Duration", "Duration", "The duration must be an integer");            	
            }
        }
    }
    
    /**
     * Temporary method that fills the test data.
     * @param path
     * @param caList
     */
    private void fillTestData(String path, List<PSContentActivity> caList)
    {
        if(path.equals("/Sites/"))
        {
            caList.add(new PSContentActivity("Site1","Site1",357,20,45,20,10));
            caList.add(new PSContentActivity("Site2","Site2",126,16,45,20,10));
            caList.add(new PSContentActivity("Site2","Site3",238,28,45,20,10));
            caList.add(new PSContentActivity("Site4","Site4",18,129,2,3,1));
            caList.add(new PSContentActivity("Resources",580,36,28,45,8));
            caList.add(new PSContentActivity("Non-Resources",256,16,8,12,4));
        }
        else
        {
            caList.add(new PSContentActivity("Site1",357,20,45,20,10));
            caList.add(new PSContentActivity("Section 1",82,16,45,20,10));
            caList.add(new PSContentActivity("Section 2",115,28,45,20,10));
            caList.add(new PSContentActivity("Section 3",157,129,2,3,1));
            caList.add(new PSContentActivity("Resources",580,36,28,45,8));
            caList.add(new PSContentActivity("Non-Resources",256,16,8,12,4));
        }
    }

}
