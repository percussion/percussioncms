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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
