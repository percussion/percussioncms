/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.preview;

import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.PSSiteManagerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author DavidBenua
 *
 */
public class ConfigurableSiteLoaderImpl extends CachingSiteLoaderImpl
   implements SiteLoader 
{
	private static Log log = LogFactory.getLog(ConfigurableSiteLoaderImpl.class); 
	
	private List<String> allowedSites; 

	/**
	 * 
	 */
	public ConfigurableSiteLoaderImpl() {
		super(); 
		allowedSites = new ArrayList<String>(); 
	}

	@Override
	public synchronized List<IPSSite> loadAllSites() throws PSSiteManagerException {
		List<IPSSite> allSites = super.loadAllSites();
		List<IPSSite> mySites = new ArrayList<IPSSite>(); 
		
		for(IPSSite site : allSites)
		{
			if(allowedSites.contains(site.getName()))
			{
				log.debug("found allowed site " + site.getName()); 
				mySites.add(site); 
			}
			else
			{
				log.debug("ignoring site " + site.getName()); 
			}
		}
		
		return mySites; 
	}

	public List<String> getAllowedSites() {
		return allowedSites;
	}

	public void setAllowedSites(List<String> allowedSites) {
		this.allowedSites = allowedSites;
	}
	
	

}
