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
