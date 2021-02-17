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

package com.percussion.activity.service.impl;

import com.percussion.activity.data.*;
import com.percussion.activity.service.IPSActivityService;
import com.percussion.activity.service.IPSContentActivityService;
import com.percussion.activity.service.IPSEffectivenessService;
import com.percussion.activity.service.IPSEffectivenessService.PSEffectivenessServiceException;
import com.percussion.activity.service.IPSTrafficService;
import com.percussion.analytics.data.PSAnalyticsProviderConfig;
import com.percussion.analytics.error.PSAnalyticsProviderException;
import com.percussion.analytics.error.PSAnalyticsProviderException.CAUSETYPE;
import com.percussion.analytics.service.IPSAnalyticsProviderService;
import com.percussion.share.service.IPSSystemProperties;
import com.percussion.share.service.exception.PSBeanValidationException;
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
import javax.ws.rs.core.MediaType;
import java.util.*;

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
    	//validate the request
    	contentActivityReqvalidator.validate(request);

    	//Test Data, remove after implementation
        //fillTestData(request.getPath(), caList);
    
    	return new PSContentActivityList(getContentActivity(request.getPath(), request.getDurationType(), request.getDuration(), true));
    }
    
    @POST
    @Path("/effectiveness")
    @SuppressWarnings("unchecked")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public List<PSEffectiveness> getEffectiveness(PSEffectivenessRequest request)
    {
        //validate the request
        contentActivityReqvalidator.validate(request);

        try
        {
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
        catch (PSAnalyticsProviderException e)
        {
            throw new PSEffectivenessServiceException(analyticsProviderService.getErrorMessageHandler().getMessage(e));
        }
    }
    
    @Override
    @POST
    @Path("/contenttraffic")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSContentTraffic getContentTraffic(PSContentTrafficRequest request) 
    {
        return trafficService.getContentTraffic(request);
    }
    
    @Override
    @POST
    @Path("/trafficdetails")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTrafficDetails> getTrafficDetails(PSTrafficDetailsRequest request)
    {
        return new PSTrafficDetailsList(trafficService.getTrafficDetails(request));
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
            boolean includeSite)
    {
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
            String path = req.getPath();
            String durationType = req.getDurationType();
            String duration = req.getDuration();
            rejectIfBlank("contentactivity", "path", path);
            rejectIfBlank("contentactivity", "durationType", durationType);
            rejectIfBlank("contentactivity", "duration", duration);
            try
            {
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
