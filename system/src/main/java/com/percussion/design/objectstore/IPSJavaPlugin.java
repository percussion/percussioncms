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

package com.percussion.design.objectstore;


/**
 * Interface to implement the Java plugin object whose XML representation
 * follows the following DTD.
 * &lt;!ELEMENT PSXJavaPlugin EMPTY&gt;
 * &lt;!ATTLIST PSXJavaPlugin
 * oskey CDATA #REQUIRED
 * browserkey CDATA #REQUIRED
 * versiontouse CDATA #REQUIRED
 * versioningtype (Dynamic | Static) #REQUIRED
 * downloadlocation CDATA #IMPLIED
 * &gt;
 */
public interface IPSJavaPlugin extends IPSComponent
{
   /**
    * @return OS key for the plugin. Never <code>null</code> or empty.
    */
   public String getOsKey();

   /**
    * @return Browser key for the plugin. Never <code>null</code> or empty.
    */
   public String getBrowserKey();

   /**
    * @return pluginversion to use. Never <code>null</code> or empty.
    */
   public String getVersionToUse();

   /**
    * @return plugin versining type. Never <code>null</code> or empty.
    */
   public String getVersioningType();

   /**
    * @return plugin download location. Never <code>null</code> or empty.
    */
   public String getDownloadLocation();

   /**
    * @return <code>true</code> if plugin uses static versioning,
    * <code>false</code> for dynamic versioning.
    */
   public boolean isStaticVersioning();

   /**
    * See the {@link 
    * com.percussion.cms.objectstore.IPSCmsComponent#equals(Object) interface} 
    * for complete details.
    */
   public boolean equals( Object obj );

   /**
    * Generates code of the object.
    */
   public int hashCode();

   /**
    * Returns the family classid based on the major version.  The rest of the 
    * version string is ignored.  
    * See http://java.sun.com/javase/6/webnotes/family-clsid.html for more info.
    * Currently only the 1.5 and 1.6 families are supported (
    * {@link #CLASSID_STATIC_15} and {@link #CLASSID_STATIC_16}).
    *
    * @return the static classid based on the version
    */
   public String getStaticClsid();

   static public final String VERSIONING_TYPE_STATIC = "static";
   static public final String VERSION_STATIC = "jpi-version";

   static public final String VERSIONING_TYPE_DYNAMIC = "dynamic";
   static public final String CLASSID_DYNAMIC =
      "clsid:8AD9C840-044E-11D1-B3E9-00805F499D93";
   static public final String VERSION_DYNAMIC = "version";

   /**
    * Static family classid for version 1.5
    */
   public static final String CLASSID_STATIC_15 = 
      "clsid:CAFEEFAC-0015-0000-FFFF-ABCDEFFEDCBA";
   
   /**
    * Static family classid for version 1.6
    */
   public static final String CLASSID_STATIC_16 = 
      "clsid:CAFEEFAC-0016-0000-FFFF-ABCDEFFEDCBA";
   
   
   /** DTD constant. */
   static public final String XML_NODE_NAME = "PSXJavaPlugin";
   /** DTD constant. */
   static public final String ATTR_OSKEY = "oskey";
   /** DTD constant. */
   static public final String ATTR_BROWSERKEY = "browserkey";
   /** DTD constant. */
   static public final String ATTR_VERSIONTOUSE = "versiontouse";
   /** DTD constant. */
   static public final String ATTR_VERSIONINGTYPE = "versioningtype";
   /** DTD constant. */
   static public final String ATTR_DOWNLOADLOCATION = "downloadlocation";
}
