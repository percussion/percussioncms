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
package com.percussion.delivery.multitenant;

import com.percussion.delivery.multitenant.IPSTenantAuthorization.Status;
import com.percussion.error.PSExceptionUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


/**
 * Intercepts requests and extracts the tenant id. Authorizes the tenant id. Sets the tenant id
 * on the tenant context.
 * 
 * Caches authorizations to insure request performance.
 * 
 * @author erikserating
 *
 */
@Deprecated
public class PSTenantSecurityFilter implements Filter
{

	  /**
     * Log for this class.
     */
    private static final Logger log = LogManager.getLogger(PSTenantSecurityFilter.class);
    

    /**
     * Tenant authorization provider.
     */
    private IPSTenantAuthorization tenantAuth=null;
    
    private IPSTenantCache cache=null;
    
    
    /**
     * The header and query string parameter name for the tenant id.
     */
    public static final String TENANTID_PARAM_NAME = "perc-tid";
    public static final String DEFAULT_COMPANY = "Percussion Software";

    /**
     * Create a new security filter.
     * @param tenantAuth cannot be <code>null</code>.
     */
    public PSTenantSecurityFilter(IPSTenantAuthorization tenantAuth, IPSTenantCache tenantCache)
    {
        if(tenantAuth == null)
            log.warn("tenantAuth cannot be null. skipping authorization filter.");
        else{
        	this.tenantAuth = tenantAuth;

        	log.debug("Tenant Authorization Initialized.");
        }
       
        if(tenantCache == null)
            log.warn("tenantCache cannot be null. skipping authorization filter.");
        else{
        	this.cache = tenantCache;
        	log.debug("Tenant Authorization Caching Initialized.");
        }
   
        
    }
    
    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy()
    {
        log.debug("Tenant Security Filter Destroyed.");
    }

    /** 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException
    {
    	
        String tenantid = extractTenantId(req);
        
        PSTenantInfo t = null;
        
        if((tenantAuth !=null) && (cache != null))
        {
        	if(tenantid==null)
        		tenantid="not-specified";
        	

        	log.debug("Applying filter to tenant {}",tenantid);
        	
        	//Skip processing for NetSuite services
        	boolean netsuite = false;
        	if(((HttpServletRequest)req).getPathInfo()!=null)
        	{
        		if(((HttpServletRequest)req).getPathInfo().contains("/netsuite/") || tenantid.equals("1"))
        			netsuite=true;
        	}else if(tenantid.equals("1"))
        		netsuite = true;
        	
        	if(!netsuite)
        		{
        			t = (PSTenantInfo) cache.get(tenantid,req);
    		    
		        	if(t==null){
		        		
		        		if(log.isDebugEnabled())
		        			log.debug("Authorizing tenant {}", tenantid);
		        		
		        		PSLicenseStatus ret = tenantAuth.authorize(tenantid, 1, req);
			            if(ret.getStatusCode()  == Status.SUCCESS || ret.getStatusCode() == Status.UNEXPECTED_ERROR)
			            {
			            	if(log.isDebugEnabled())
			            		log.debug("Authorized tenant {}" , tenantid);
			            	
			            	if(ret.getStatusCode() == Status.SUCCESS){
				            	//Setup the tenant info for the cache so we get a hit next time
				            	t = new PSTenantInfo();
				            	t.setAPIUsageStart(new Date());
				            	t.setLastAuthorizationCheckDate(new Date());
				            	t.setTenantId(tenantid);
				            	t.clearAPIUsage();
				            	t.addAPIUsage(1);
				            	t.setLicenseStatus(ret); 	
				            	
				            	if(log.isDebugEnabled())
				            		log.debug("Caching tenant authorization for tenant {}" , tenantid);
				            	cache.put(t);
			            	}
			            
			            	if(log.isDebugEnabled())
			            		log.debug("Setting tenant context to {}" , tenantid);
					        
			            	//Add the tenant id to the requests thread.
			                PSThreadLocalTenantContext.setTenantId(tenantid);
			            }
			            else
			            {
			            	if(log.isDebugEnabled())
			            		log.debug("Authorization failed for tenant {} Status is {}",  tenantid, ret.getLicenseStatus());
					        
			                if(resp instanceof HttpServletResponse)
			                {
			                    ((HttpServletResponse)resp).sendError(HttpServletResponse.SC_FORBIDDEN, ret.getLicenseStatus());
			                    return;
			                }
			                
							resp.getWriter().println("403 Forbidden: " + ret.getLicenseStatus());
							resp.flushBuffer();
							return;
			            }
		        	}else{
		            	//Log the request thru the metrics service. 
		            	logUsage(t,req);
		        		PSThreadLocalTenantContext.setTenantId(tenantid);
		        		
		        		if(log.isDebugEnabled())
		        			log.debug("Setting Tenant Context to {}" , tenantid);
		        	}
        	
        		}else{
        				log.debug("Skipping authorization for Percussion Tenant or NetSuite Service for tenant {}", tenantid);
        			//This is the netsuite service - ignore auth on this one.
	        		PSThreadLocalTenantContext.setTenantId(tenantid);

	        			log.debug("Tenant Context set to {}",  tenantid);
        		}
        }
        else
        {
            PSThreadLocalTenantContext.clearTenantId();
        }
        
        chain.doFilter(req, resp);

    }

    /** 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config)
    {
       log.info("Tenant Security Filter initialized..");

    }
    
    /**
     * Helper method to extract the tenant id from the request. The tenant id
     * can be found on the header or in the query string parameter. The header
     * takes precedence.
     * @param req assumed not <code>null</code>.
     * @return the tenant id if found or <code>null</code> if not found.
     */
    private String extractTenantId(ServletRequest req)
    {
        String tenantid = null;
        //Try to find it on the header first
        if(req instanceof HttpServletRequest)
        {
            tenantid = ((HttpServletRequest)req).getHeader(TENANTID_PARAM_NAME);
        }
        if(tenantid == null) // Fallback to query string
            tenantid = req.getParameter(TENANTID_PARAM_NAME);
        
        //Handle an empty tenant id
        if(tenantid == null || tenantid.trim().isEmpty())
        	tenantid=null;

        return tenantid;
    }
    
    public static boolean isNumeric(String str)  
    {
		double d;
		try
      	{
        	d = Double.parseDouble(str);
      	}
      	catch(NumberFormatException nfe)
      	{
        	return false;
      	}
      	log.debug("It's a number {}", d);
      return true;  
    }  
    
    /***
     * Records usage with the Metrics service. 
     */
    private void logUsage(IPSTenantInfo t, ServletRequest req){
    	
    	try{
	    	Validate.notNull(t);
	    	Validate.notNull(req);
	    	String method = "";
	    	
	    	if(req instanceof HttpServletRequest){
	    			
	    		HttpServletRequest r = (HttpServletRequest) req;
	    		if(r.getPathInfo()!= null)
	    			method = r.getPathInfo().replace("/",".");
	    		
	    		if(isNumeric(method.substring(method.lastIndexOf(".")))){
	    			method = method.substring(0,method.lastIndexOf("."));
	    		}
	    		
	    		//Default to Percussion if the company name is missing
	    		if(t.getLicenseStatus().getCompany()==null || t.getLicenseStatus().getCompany().isEmpty())
	    			t.getLicenseStatus().setCompany(DEFAULT_COMPANY);
	    	
	    	}
    	}catch(Exception e){
    		log.debug("Error logging metrics: {}", PSExceptionUtils.getMessageForLog(e));
    	}
    }

	/**
	 * Returns the authorization cache used by the filter to cache authorization of 
	 * tenants per request. 
	 * 
	 * @return the cache
	 */
	public IPSTenantCache getCache() {
		return cache;
	}

	/**
	 * Sets the cache used by the filter to cache request authorizations per tenant. 
	 *  
	 * @param cache the cache to set
	 */
	public void setCache(IPSTenantCache cache) {
		this.cache = cache;
	}

}
