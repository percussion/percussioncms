/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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

package com.percussion.i18n;

import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.server.PSConsole;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Singleton class to load the TMX resources and expose methods for serverwide
 * usage. The resource bundle is an XML document conforming to TMX DTD. Whole
 * document is parsed into a hash map of hash maps. The key for the first
 * hashmap is the language and the values are the hashmap of key-value pairs
 * for that language. Map is built when server is initialized and can be
 * reloaded on demand. The most used method {@link #getString(String, String)} 
 * is written to guarantee to return a meaningful value.
 */
 /*
  * Note: right now loading the document into cache is fairly quick process.
  * However, when several languages are deployed and the resource bundle grows
  * to a size to slowdown loading resources signinfcantly, we may have to
  * consider reading resources in a separate thread.
  */
public class PSTmxResourceBundle
   implements IPSTmxDtdConstants
{
   private static class TmxResourceBundleHolder
   {
      public static final PSTmxResourceBundle BUNDLE = new PSTmxResourceBundle();
   }
   
   /**
    * Constructor is made private to enforce the singleton pattern.
    * Creates and initializes the resource cache.
    */
   private  PSTmxResourceBundle()
   {
      try
      {
         loadResources();
      }
      catch(Throwable t)
      {
         Logger l = Logger.getLogger("TmxResourceBundle");
         l.error("Unexpected error loading resources " 
            + t.getLocalizedMessage());
         
         //if debug is on dump the stack to server console
         if(ms_Debug)
         {
            t.printStackTrace();
         }
      }
   }

   /**
    * Get the singleton instance of this class.
    *
    * @return The singleton instance of this class, never <code>null</code>.
    */
    public static PSTmxResourceBundle getInstance()
   {
      return TmxResourceBundleHolder.BUNDLE;
   }
   /**
    * Get the value for the given key using the default language. Simply
    * delegates to <code>getString(String, String)</code>
    * @param    key    lookup key string
    * @return lookup translation value for the key in default language
    */
   public String getString(String key)
   {
      return getString(key, ms_DefaultLanguage);
   }

   /**
    * Method to allow cleanup. Should be the last one to call.
    */
   public void terminate()
   {
      //Store the missing keys to a properties file, may be for debug purpose.
       File missingFile = new File(m_rxRootDir, FILE_MISSING_RESOURCES);
      try(FileOutputStream fos = new FileOutputStream(missingFile))
      {
         ms_MissingResources.store(fos,
            "Created by " + SUBSYSTEM);
      }
      catch(Throwable t)
      {
         Logger l = Logger.getLogger("TmxResourceBundle");
         l.error("Unexpected error during termination " 
            + t.getLocalizedMessage());
         //if debug is on dump the stack to server console
         if(ms_Debug)
         {
            t.printStackTrace();
         }
      }
   }

   /**
    * Get the value for the given key and language string. If the key is not
    * found in the specified language bundle or in the default language bundle,
    * key is added to the list of missing keys. This key will finally be dumped
    * to a property file.
    * @param key lookup key string, if <code>null</code> or <code>empty</code>,
    * return value will be empty string.
    * @param language language string, if <code>null</code> or <code>empty</code>,
    * default language is assumed.
    * @return the lookup value for the given key and langauge. Never <code>null</code>
    * may be <code>empty</code>.
    */
   public String getString(String key, String language)
   {
      PSTmxUnit unit = getUnit(key, language);

      if (unit == null)
         return (key == null) ? "" : trimKey(key);
      else
      {
         if(! unit.isValid())
         {
            if(!ms_MissingResources.containsKey(key))
            {
               ms_MissingResources.put(key, key);
            }
            
            if(!ms_Debug)
            {
               // if no value check the default language if we didn't already               
               if (!ms_DefaultLanguage.equals(language))
               {
                  unit = getUnit(key, ms_DefaultLanguage);
                  if (unit != null && unit.isValid())
                     return unit.toString();
               }
               
               return PSI18nUtils.getLastSubKey(key);
            }
         }         
         return unit.toString();
      }
   }
   
   /**
    * When returning the key as the value of a lookup, it is necessary to 
    * strip the initial substring up to and including the '@' symbol.
    * @param key the original key, assumed non-<code>null</code>
    * @return the trimmed key, never <code>null</code>
    */
   private String trimKey(String key)
   {
      int atsign = key.indexOf('@');
      if (atsign >= 0 && (key.length() - atsign) > 1)
      {
         return key.substring(atsign + 1);
      }
      else
      {
         // Key is broken
         return key;
      }
   }

   /**
    * Just like {@link #getString(String, String)}, except it gets the
    * mnemonic of the specified translation unit (key, language).
    * 
    * @return The mnemonic of the specified unit, <code>0</code> if cannot
    *    find the mnemonic for the specified translation unit.
    */
   public int getMnemonic(String key, String language)
   {
      PSTmxUnit unit = getUnit(key, language);
      return unit == null ? 0 : unit.getMnemonic();
   }
   
   /**
    * Just like {@link #getString(String, String)}, except it gets the
    * tooltip of the specified translation unit (key, language).
    * 
    * @return The tooltip of the specified unit, <code>null</code> if cannot
    *    find the tooltip for the specified translation unit.
    */
   public String getTooltip(String key, String language)
   {
      PSTmxUnit unit = getUnit(key, language);
      return unit == null ? null : unit.getTooltip();
   }   
   
   /**
    * Just like {@link #getString(String, String)}, except it gets the Unit 
    * for the given key and language string. Unit objects with a null or
    * empty value are invalid and are added to a missing resource list.
    * 
    * @return the lookup value for the given key and langauge. It may be 
    *    <code>null</code> if cannot find the specified unit.
    */
   private PSTmxUnit getUnit(String key, String language)
   {
      if(key == null || key.length() < 1)
         return null;
      if(language == null || language.length() < 1)
         language = ms_DefaultLanguage;

      Map map = (HashMap)ms_ResourceBundles.get(language);
      if(map == null)
         map = (HashMap)ms_ResourceBundles.get(ms_DefaultLanguage);

      PSTmxUnit obj = null;
      if(map == null && ms_Debug)
      {
         PSConsole.printMsg(SUBSYSTEM,
          "TMX Resource Bundle does not contain any deployed language resources");
      }
      else if(map != null)
      {
         obj = (PSTmxUnit) map.get(key);
      }

      return obj;
   }

   /**
    * Gets a list of all of the keys for the provided language. If the
    * provided language cannot be found, the default language will be returned
    * if the default language cannot be found then <code>null</code> will be
    * returned.
    *
    * @param language string, if <code>null</code> or <code>empty</code>,
    * default language is assumed.
    * @return all of the keys as <code>Strings</code>.  May be
    * <code>null</code> if language is not supported.
    */
   public Iterator getKeys(String language)
   {
      if(language == null || language.length() < 1)
         language = ms_DefaultLanguage;

      Map map = (HashMap)ms_ResourceBundles.get(language);

      if(map == null)
         map = (HashMap)ms_ResourceBundles.get(ms_DefaultLanguage);

      Iterator keys = Collections.emptyIterator();

      if(map == null)
      {
         PSConsole.printMsg(SUBSYSTEM,
         "TMX Resource Bundle does not contain any deployed language resources  Check ResourceBundle.tmx file");
      }
      else if(map != null)
      {
         // get the keys set and iterator here
         keys = map.keySet().iterator();
      }

      return keys;
   }

   /** Sets the debug mode.
    * @param debug <code>true</code> to set the debug mode on <code>false</code>
    * to set the debug mode off.
    * @see #ms_Debug
    */
   public synchronized void setDebugMode(boolean debug)
   {
      ms_Debug = debug;
   }

   /**
    * Get debug mode flag.
    * @return <code>true</code> if debug mode on,
    * <code>false</code> otherwise.
    * {@link #ms_Debug}
    */
   public synchronized boolean getDebugMode()
   {
      return ms_Debug;
   }
   
   /**
    * Get the set of configured language strings
    * 
    * @return The set, never <code>null</code>, not empty once 
    * {@link #loadResources()} has been called.  Modifications to the set do
    * not affect this object.
    */
   @SuppressWarnings("unchecked")
   public Set<String> getLanguages()
   {
      return new HashSet<>(ms_ResourceBundles.keySet());
   }

   /**
    * Flag when set to <code>true</code> executes the bundle in debug mode in
    * that
    * <ul>
    * <li>When the key id "psx.ce.label.Content Title" and the resource bundle
    * does not contain an entry for this key the return value will be
    * "psx.ce.label.Content Title". This when debug mode is off will be
    * "Content Title"</li>
    * <li>Additional error log is enabled</li>
    * </ul>
    * The default is <code>false</code>.
    */
   private static boolean ms_Debug = false;


   public static final String RX_RESOURCES_I18NPATH="rx_resources" + File.separator + "I18n";
   public static final String SYS_RESOURCES_I18NPATH="sys_resources" + File.separator + "I18n";

   /**
    * String constant specifying the location path of the TMX resource bundle.
    */
    public static final String MASTER_RESOURCE_FILEPATH =
      "rxconfig" + File.separator + "I18n" + File.separator + "ResourceBundle.tmx";
    /**
     * Map storing all language resources. Never <code>null</code>.
     * <code>Empty</code> until loadResources() is called successfully.
     */
    static HashMap ms_ResourceBundles = new HashMap();

    /**
     * Default language for the system
     */
    static public String ms_DefaultLanguage = PSI18nUtils.DEFAULT_LANG;

    /**
     * Name of the module to print on server console for displaying messages.
     */
    static private final String SUBSYSTEM = "I18n";

    /**
     * Properties object to cache the missing resources. Never <code>null</code>.
     */
    static private Properties ms_MissingResources = new Properties();

    /**
     * Name of the file to store missing resources.
     */
    private static  final String FILE_MISSING_RESOURCES =
      "rxconfig" + File.separator + "I18n" + File.separator +
      "MissingResources.properties";
    
    /**
     * The rhythmyx root directory, never <code>null</code> or empty after
     * construction.
     */
    private String m_rxRootDir;


    private void processResourceFiles(Path p) throws IOException, SAXException {
        if(Files.exists(p)){
            Iterator<Path> files
                    = p.iterator();
            while(files.hasNext()){
                Path f = files.next();
                if(f.toAbsolutePath().endsWith(".tmx")){
                    addResourcesToCache(getTmxResourceDoc(f));
                }
            }
        }
    }
    /**
     * This method loads/reloads the i18n resource to cache.
     * @throws IOException
     * @throws SAXException
     */
   public synchronized boolean loadResources()
      throws IOException, SAXException
    {
      flushCache();

      PSConsole.printMsg(SUBSYSTEM, "Loading I18n Resources to Cache...");

      IPSRhythmyxInfo rxInfo = PSRhythmyxInfoLocator.getRhythmyxInfo();
      m_rxRootDir = (String) rxInfo
            .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY);

      //Master file
      addResourcesToCache(getMasterResourceDoc(m_rxRootDir));

      //sys_resources/i18n files
      processResourceFiles(Paths.get(m_rxRootDir,SYS_RESOURCES_I18NPATH));

      //rx_resources/i18n files
      processResourceFiles(Paths.get(m_rxRootDir,RX_RESOURCES_I18NPATH));


      // flush entire cache
      if (PSCacheManager.isAvailable())
         PSCacheManager.getInstance().flush();
      
      PSConsole.printMsg(SUBSYSTEM, "Done Loading.");
      return true;
    }

    private Document getTmxResourceDoc(Path f) throws IOException, SAXException {

        try(FileInputStream fis = new FileInputStream(f.toFile())) {
            //must use UTF-8 encoding to read the file
            return PSXmlDocumentBuilder.createXmlDocument(
                    new InputStreamReader(fis,
                            StandardCharsets.UTF_8), false);
        }
    }

    private void addResourcesToCache(Document doc) {
        NodeList nl = doc.getElementsByTagName(ELEM_HEADER);
        if(nl == null || nl.getLength() < 1)
        {
            PSConsole.printMsg(
                    SUBSYSTEM, "Invalid TMX Document. Header element missing");
        }
        Element header = (Element)nl.item(0);
        nl = header.getElementsByTagName(ELEM_PROP);
        if(nl == null || nl.getLength() < 1)
        {
            PSConsole.printMsg(SUBSYSTEM,
                    "Invalid TMX Document. No supported language is specified in the header");
        }
        Element elem = null;
        String value = null;
        Node node = null;
        int nlLength = nl.getLength();
        for(int i=0; i<nlLength; i++)
        {
            elem = (Element)nl.item(i);
            if(!elem.getAttribute(ATTR_TYPE).equals(ATTR_VAL_SUPPORTEDLANGUAGE))
                continue;
            node = elem.getFirstChild();
            value = "";
            if(node instanceof Text)
                value = ((Text)node).getData();
            if(value.length() < 1)
                continue;
            ms_ResourceBundles.put(value, new HashMap());
        }
        if(ms_ResourceBundles.size() < 1)
        {
            PSConsole.printMsg(SUBSYSTEM,
                    "Invalid TMX Document. No supported language is specified in the header.");
            return;
        }
        nl = doc.getElementsByTagName(ELEM_TU);
        NodeList nlTuv = null;
        NodeList nlSeg = null;
        NodeList nlProps = null;
        Element elemTuv = null;
        Element elemProp = null;
        String key = null;
        String lang = null;
        String mnemonic = null;
        String tooltip = null;
        String propType = null;
        Map map = null;
        nlLength = nl.getLength();
        for(int i=0; nl!=null && i<nlLength; i++) {
            elem = (Element) nl.item(i);
            key = elem.getAttribute(ATTR_TUID);
            if (key == null || key.length() < 1)
                continue;
            nlTuv = elem.getElementsByTagName(ELEM_TUV);
            for (int j = 0; nlTuv != null && j < nlTuv.getLength(); j++) {
                elemTuv = (Element) nlTuv.item(j);
                lang = elemTuv.getAttribute(ATTR_XML_LANG);
                map = (HashMap) ms_ResourceBundles.get(lang);
                if (map == null) {
                    continue;
                }
                // get the mnemonic and tooltip from the properties
                mnemonic = null;
                tooltip = null;
                nlProps = elemTuv.getElementsByTagName(ELEM_PROP);
                int propLength = nlProps.getLength();
                if (nlProps != null && propLength > 0) {
                    for (int k = 0; k < propLength; k++) {
                        elemProp = (Element) nlProps.item(k);
                        propType = elemProp.getAttribute(ATTR_TYPE);
                        if (propType != null
                                && propType.equalsIgnoreCase(ATTR_VAL_MNEMONIC)) {
                            mnemonic = PSXMLDomUtil.getElementData(elemProp);
                        } else if (propType != null
                                && propType.equalsIgnoreCase(ATTR_VAL_TOOLTIP)) {
                            tooltip = PSXMLDomUtil.getElementData(elemProp);
                        }
                    }
                }

                // get the value from "seg" element
                nlSeg = elemTuv.getElementsByTagName(ELEM_SEG);
                value = "";
                if (nlSeg != null && nlSeg.getLength() > 0) {
                    node = nlSeg.item(0).getFirstChild();
                    if (node instanceof Text)
                        value = ((Text) node).getData();
                }
                PSTmxUnit entry = new PSTmxUnit(value, mnemonic, tooltip);
                map.put(key, entry);
            }
        }
    }
    /**
     * Method to empty the resource cache.
     */
   private void flushCache()
   {
      if(ms_ResourceBundles.isEmpty())
         return;

      PSConsole.printMsg(SUBSYSTEM, "Flushing I18n Resource Cache...");
      ms_ResourceBundles.clear();
      System.gc();
      PSConsole.printMsg(SUBSYSTEM, "Done Flushing.");
   }

   /**
   * Utility method to load and return the master TMX resource bundle as DOM
   * document.
   *
   * @param rxroot Rhythmyx root directory as string. Must not be <code>null</code>,
   * may be <code>empty</code>.
   * @return DOM document of the TMX resource file.
   * @throws FileNotFoundException
   * @throws UnsupportedEncodingException
   * @throws SAXException
   * @throws IOException
   */
   static public Document getMasterResourceDoc(String rxroot)
      throws FileNotFoundException, UnsupportedEncodingException, SAXException,
             IOException
   {
      synchronized(m_masterResourceMonitor)
      {
         File tmxFile = getMasterResourceFile(rxroot);
         if (tmxFile.exists())
         {
             try(FileInputStream fis = new FileInputStream(tmxFile)) {
                 //must use UTF-8 encoding to read the file
                 return PSXmlDocumentBuilder.createXmlDocument(
                         new InputStreamReader(fis,
                                 StandardCharsets.UTF_8), false);
             }
         }
         else
         {
            return new PSTmxDocument().getDOMDocument();
         }
      }
   }

   /**
    * Get a file reference to the {@link #MASTER_RESOURCE_FILEPATH} file.
    * 
    * @param rxRootDir The rx root directory, may not be <code>null</code>. 
    * 
    * @return The file, never <code>null</code>.
    */
   public static File getMasterResourceFile(String rxRootDir)
   {
      File tmxFile = new File(rxRootDir, MASTER_RESOURCE_FILEPATH);
      
      return tmxFile;
   }

   /**
    * Saves the master resource document, optionally reloading resources to the
    * cache.
    *
    * @param doc The resource doc to save, may not be <code>null</code>, must
    * conform to the
    * @param reload <code>true</code> to reload the resources into the cache,
    * <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    * @throws IOException if any IO errors occur.
    * @throws SAXException if there is a problem reloading the resources.
    */
   public void saveMasterResourceBundle(IPSTmxDocument doc, boolean reload)
      throws IOException, SAXException
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      synchronized(m_masterResourceMonitor)
      {
         File tmxFile = getMasterResourceFile(m_rxRootDir);
         
         doc.save(tmxFile, true);
      }

      // reload the resources into the cache if requested
      if (reload)
         loadResources();
   }

   /**
    * Monitor object used to synchronize access to the master resource doc.
    * Never <code>null</code> or modified.
    */
   private static Object m_masterResourceMonitor = new Object();

   /**
    *  Main method for testing purpose.
    * @param args
    */
   static public void main(String[] args)
   {
      try
      {
/*
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileReader(ms_ResourceFile), false);
         NodeList nl = doc.getElementsByTagName("tu");
         Element elem = (Element) nl.item(0);
         Element body = (Element) elem.getParentNode();
         for(int i=1; i<5000; i++)
         {
            elem = (Element)body.appendChild(elem.cloneNode(true));
            elem.setAttribute("tuid", Integer.toString(i));
         }
         PSXmlDocumentBuilder.write(doc, new FileWriter(ms_ResourceFile));
*/
         PSTmxResourceBundle bundle = PSTmxResourceBundle.getInstance();

         long x = new Date().getTime();
         for(int i=0; i<5000; i++)
         {
            bundle.getString(Integer.toString(i), "fr-ca");
         }
         System.out.println(new Date().getTime() - x);

         x = new Date().getTime();
         bundle.loadResources();
         System.out.println(new Date().getTime() - x);

         x = new Date().getTime();
         for(int i=0; i<5000; i++)
         {
            bundle.getString(Integer.toString(i), "fr-ca");
         }
         System.out.println(new Date().getTime() - x);
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
