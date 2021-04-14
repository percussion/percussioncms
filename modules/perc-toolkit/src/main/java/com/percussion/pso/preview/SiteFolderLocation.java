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

import java.util.HashMap;
import java.util.Map;

import com.percussion.pso.utils.PSOMutableUrl;
import com.percussion.server.PSRequestParsingException;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;

/**
 *  Location bean. 
 *
 * @author DavidBenua
 *
 */
public class SiteFolderLocation
{
   private String folderPath;
   private int folderid; 
   private IPSSite site;
   
   /**
    * Default Constructor
    */
   public SiteFolderLocation()
   {
     
   }
   
   /**
    * Gets the parameter map for this location. These parameters are 
    * unique to that location. 
    * @return the map. 
    */
   public Map<String,Object> getParameterMap()
   {
      Map<String, Object> pmap = new HashMap<String, Object>(); 
      pmap.put(IPSHtmlParameters.SYS_SITEID, String.valueOf(getSiteid())); 
      pmap.put(IPSHtmlParameters.SYS_FOLDERID, String.valueOf(getFolderid())); 
      return pmap;       
   }
   
   /**
    * Fixes a URL by applying the parameters in the map
    * @param baseUrl
    * @return the url with the parameters added. 
    * @throws PSRequestParsingException
    */
   public String fixUrl(String baseUrl) throws PSRequestParsingException
   {
      PSOMutableUrl url = new PSOMutableUrl(baseUrl); 
      url.setParamList(this.getParameterMap());    
      return url.toString(); 
   }

   /**
    * @return the siteName
    */
   public String getSiteName()
   {
      return site.getName(); 
   }


   /**
    * @return the folderPath
    */
   public String getFolderPath()
   {
      return folderPath;
   }

   /**
    * @param folderPath the folderPath to set
    */
   public void setFolderPath(String folderPath)
   {
      this.folderPath = folderPath;
   }

   /**
    * @return the siteid
    */
   public long getSiteid()
   {
      return site.getSiteId(); 
   }

   /**
    * @return the folderid
    */
   public int getFolderid()
   {
      return folderid;
   }

   /**
    * @param folderid the folderid to set
    */
   public void setFolderid(int folderid)
   {
      this.folderid = folderid;
   }

   /**
    * @return the site
    */
   public IPSSite getSite()
   {
      return site;
   }

   /**
    * @param site the site to set
    */
   public void setSite(IPSSite site)
   {
      this.site = site;
   }

  
}
