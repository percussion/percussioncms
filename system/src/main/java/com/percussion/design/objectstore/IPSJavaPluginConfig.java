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

import com.percussion.cms.objectstore.IPSCmsComponent;

import java.util.Collection;

/**
 * Interface to implement the configuration of possible Java plugins based on
 * the client's OS and browser versions. The object represents a set of plugins
 * for each combination of OS and Browser versions.
 * The DTD for the XML block for this object is:
 * &lt;!ELEMENT PSXJavaPlugin EMPTY&gt;
 * &lt;!ATTLIST PSXJavaPlugin
 * oskey CDATA #REQUIRED
 * browserkey CDATA #REQUIRED
 * versiontouse CDATA #REQUIRED
 * versioningtype (Dynamic | Static) #REQUIRED
 * downloadlocation CDATA #IMPLIED
 * &gt;
 * &lt;!ELEMENT PSXJavaPluginConfig (PSXJavaPlugin+)&gt;
 *@see IPSJavaPlugin
 */
public interface IPSJavaPluginConfig extends IPSComponent
{
   /**
    * Returns plugin object for the given http user agent string which is a CGI
    * variable posted by the browser to the server.
    * @param httpUserAgent HTTP User Agent string. This is typically a string
    * representing the cient's browser and OS.
    * @return <code>IPSPlugin</code> object for the given combination of the OS
    * and Browser in the HTTP User Agent. May be <code>null</code> if one does
    * not exist for that combination.
    */
   public IPSJavaPlugin getPlugin(String httpUserAgent);
   /**
    * Returns plugin object for the given OS and Browser combinations.
    * @param osKey key string representing the operating system. This typically
    * follows the syntax of the CGI variable for OS from the server.
    * @param browserKey key string representing the browser. This typically
    * follows the syntax of the CGI variable for Browser from the server.
    * @return <code>IPSPlugin</code> object for the given combination of the OS
    * and Browser. May be <code>null</code> if one does not exist for that
    * combination.
    */
   public IPSJavaPlugin getPlugin(String osKey, String browserKey);

   /**
    * Returns all plugins provisioned in the config.xml.
    * @return collection of IPSJavaPlugin objects, never <code>null</code>,
    * may be <code>empty</code>.
    */
   public Collection getAllPlugins();

   /**
    * Get the default plugin object.
    * @return <code>IPSPlugin</code> object for the default plugin.
    * Never <code>null</code>
    */
   public IPSJavaPlugin getDefaultPlugin();

   /**
    * See the {@link IPSCmsComponent#equals(Object) interface} for complete
    * details.
    */
   public boolean equals( Object obj );

   /**
    * Generates code of the object. Overrides {@link Object#hashCode()}.
    */
   public int hashCode();

   /**
    * KEY_SEPARATOR.
    */
   static public final char KEY_SEPARATOR = ':';

   /**
    * Default plugin attributes
    */
   static public final String DEFAULT_OSKEY = "Any";
   static public final String DEFAULT_BROWSERKEY = "Any";

   /**
    * List of supported plugin versions
    */
   static public final String[] VERSION_LIST =
      {
         "1.5.0_11" // default
      };
   static public final String DEFAULT_VERSIONTOUSE = VERSION_LIST[0];

   /**
    * List of supported plugin locations
    */
   static public final String[] PLUGIN_LOCATION_LIST =
      {
         "http://java.sun.com/update/1.5.0/jinstall-1_5_0_11-windows-i586.cab#Version=1,5,0,11" // default
      };
   static public final String DEFAULT_DOWNLOADLOCATION = PLUGIN_LOCATION_LIST[0];

   /**
    * List of supported version types
    */
   static public final String[] VERSIONING_LIST =
      {
         "Dynamic",
         "Static"
      };
   /**
    * List of supported Operating Systems
    */
   static public final String[] OS_LIST =
      {
         DEFAULT_OSKEY,
         "Windows XP",
         "Windows 2000",
         "Windows NT",
         "Mac OS X"
      };

   /**
    * List of supported Operating Systems representation in useragent
    */
   static public final String[] OS_LIST_USERAGENT =
      {
         DEFAULT_OSKEY,
         "Windows NT 5.1",
         "Windows NT 5.0",
         "Windows NT",
         "PPC Mac OS X"
      };

   /**
    * List of supported Browsers
    */
   static public final String[] BROWSER_LIST =
      {
         DEFAULT_BROWSERKEY,
         "IE 5.0",
         "IE 5.5",
         "IE 6.0",
         "NS 4.79",
         "NS 7.0",
         "Firefox 1.0",
         "Safari"
      };

   /**
    * List of supported Browsers representation in useragent
    */
   static public final String[] BROWSER_LIST_USERAGENT =
      {
         DEFAULT_BROWSERKEY,
         "MSIE 5.0",
         "MSIE 5.5",
         "MSIE 6.0",
         "Mozilla/4.79",
         "Netscape",
         "Firefox/1.0",
         "Safari"
      };

   /**
    * List of applications which use plugin
    */
   static public final String[] PLUGIN_APPLICATION_LIST =
      {
         "sys_cmpHelp",
         "sys_cx",
         "sys_cxDependencyTree",
         "sys_cxItemAssembly"
      };

   //DTD constatnts
   static public final String XML_NODE_NAME = "PSXJavaPluginConfig";
}
