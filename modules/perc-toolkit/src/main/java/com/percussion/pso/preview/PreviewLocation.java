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

/**
 * Represents a site and folder location where a preview might take place.  
 *
 * The Natural Order of multiple locations is alphabetic by site name and 
 * then folder location. 
 *  
 * @author DavidBenua
 *
 */
public class PreviewLocation implements Comparable<PreviewLocation>
{
   String siteName;
   String path; 
   String url; 
   
   public PreviewLocation()
   {
      
   }

   /**
    * @see Comparable#compareTo(Object)
    */
   public int compareTo(PreviewLocation other)
   {
      if(this == other) return 0; 
      int s = this.siteName.compareTo(other.getSiteName());
      if(s != 0)
      {
         return s; 
      }
      s = this.path.compareTo(other.getPath()); 
      return s;
   }

   /**
    * @see Object#equals(Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return super.equals(obj);
   }

   /**
    * @return the siteName
    */
   public String getSiteName()
   {
      return siteName;
   }

   /**
    * @param siteName the siteName to set
    */
   public void setSiteName(String siteName)
   {
      this.siteName = siteName;
   }

   /**
    * @return the path
    */
   public String getPath()
   {
      return path;
   }

   /**
    * @param path the path to set
    */
   public void setPath(String path)
   {
      this.path = path;
   }

   /**
    * @return the url
    */
   public String getUrl()
   {
      return url;
   }

   /**
    * @param url the url to set
    */
   public void setUrl(String url)
   {
      this.url = url;
   }
}
