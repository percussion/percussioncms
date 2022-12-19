/*
 * Copyright 1999-2022 Percussion Software, Inc.
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

package com.percussion.i18n;

import com.percussion.i18n.tmxdom.IPSTmxDocument;
import com.percussion.i18n.tmxdom.IPSTmxDtdConstants;
import com.percussion.i18n.tmxdom.PSTmxDocument;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.utils.io.PathUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

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
@SuppressFBWarnings({"PATH_TRAVERSAL_IN", "PATH_TRAVERSAL_IN"})
public class PSTmxResourceBundle
   implements IPSTmxDtdConstants
{

    private static final Logger log = LogManager.getLogger(PSTmxResourceBundle.class);

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
      catch(Exception t)
      {
         Logger l = LogManager.getLogger("TmxResourceBundle");
         l.error("Unexpected error loading resources {}", t.getLocalizedMessage());
         
         //if debug is on dump the stack to server console
         if(ms_Debug)
         {
             log.error(t.getMessage());
             log.debug(t.getMessage(), t);
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
      catch(Exception t)
      {
         Logger l = LogManager.getLogger("TmxResourceBundle");
         l.error("Unexpected error during termination {} ", t.getMessage());
         l.debug(t.getMessage(),t);
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
         log.info(
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
         log.info(
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
    private static final String SUBSYSTEM = "I18n";

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


    private void processResourceFiles(Path p) throws IOException, SAXException, ParserConfigurationException {
        if(Files.exists(p)){
            try(Stream<Path> stream =Files.list(p)){
                Iterator<Path> files = stream.iterator();
                    while (files.hasNext()) {
                        Path f = files.next();
                        if (f.toAbsolutePath().toString().endsWith(".tmx")) {
                            addResourcesToCache(getTmxResourceDoc(f));
                        }
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
           throws IOException, SAXException, ParserConfigurationException {
      flushCache();

      log.info("Loading I18n Resources to Cache...");

      m_rxRootDir = PathUtils.getRxDir().getAbsolutePath();

      //Master file
      addResourcesToCache(getMasterResourceDoc(m_rxRootDir));

      //sys_resources/i18n files
      processResourceFiles(Paths.get(m_rxRootDir,SYS_RESOURCES_I18NPATH));

      //rx_resources/i18n files
       processResourceFiles(Paths.get(m_rxRootDir,RX_RESOURCES_I18NPATH));

      
      log.info("Done Loading.");
      return true;
    }

    private Document getTmxResourceDoc(Path f) throws IOException, SAXException, ParserConfigurationException {

        try(FileInputStream fis = new FileInputStream(f.toFile())) {
            //must use UTF-8 encoding to read the file
             DocumentBuilderFactory factory = PSXmlDocumentBuilder.getDocumentBuilderFactory(false);
             factory.setIgnoringComments(true);
             factory.setIgnoringElementContentWhitespace(true);
              factory.setValidating(false);
              DocumentBuilder db = factory.newDocumentBuilder();
              return db.parse(fis);
        }
    }

    private void addResourcesToCache(Document doc) {
        NodeList nl = doc.getElementsByTagName(IPSTmxDtdConstants.ELEM_HEADER);
        if(nl == null || nl.getLength() < 1)
        {
            log.error( "Invalid TMX Document. Header element missing");
            return;
        }

        Element header=null;
        if(nl.item(0) instanceof  Element)
            header = (Element)nl.item(0);

        if(header!=null)
            nl = header.getElementsByTagName(IPSTmxDtdConstants.ELEM_PROP);

        if(nl == null || nl.getLength() < 1)
        {
            log.error(
                    "Invalid TMX Document. No supported language is specified in the header");
            return;
        }

        Node elem = null;
        String value = null;
        Node node = null;
        int nlLength = nl.getLength();
        for(int i=0; i<nlLength; i++)
        {
            elem = nl.item(i);
            if(elem instanceof Element && !elem.getAttributes().getNamedItem(IPSTmxDtdConstants.ATTR_TYPE).getNodeValue().equals(IPSTmxDtdConstants.ATTR_VAL_SUPPORTEDLANGUAGE))
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
            log.error(
                    "Invalid TMX Document. No supported language is specified in the header.");
            return;
        }
        nl = doc.getElementsByTagName(IPSTmxDtdConstants.ELEM_TU);
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
            key = ((Element)elem).getAttribute(IPSTmxDtdConstants.ATTR_TUID);
            if (key == null || key.length() < 1)
                continue;
            if(elem instanceof Element){

            nlTuv = ((Element)elem).getElementsByTagName(IPSTmxDtdConstants.ELEM_TUV);
            for (int j = 0; nlTuv != null && j < nlTuv.getLength(); j++) {
                if(nlTuv.item(j) instanceof Element) {
                    elemTuv = (Element) nlTuv.item(j);
                }

                lang = elemTuv.getAttribute(IPSTmxDtdConstants.ATTR_XML_LANG);
                map = (HashMap) ms_ResourceBundles.get(lang);
                if (map == null) {
                    continue;
                }
                // get the mnemonic and tooltip from the properties
                mnemonic = null;
                tooltip = null;
                nlProps = elemTuv.getElementsByTagName(IPSTmxDtdConstants.ELEM_PROP);
                int propLength = nlProps.getLength();
                if (nlProps != null && propLength > 0) {
                    for (int k = 0; k < propLength; k++) {
                        elemProp = (Element) nlProps.item(k);
                        propType = elemProp.getAttribute(IPSTmxDtdConstants.ATTR_TYPE);
                        if (propType != null
                                && propType.equalsIgnoreCase(IPSTmxDtdConstants.ATTR_VAL_MNEMONIC)) {
                            mnemonic = PSXMLDomUtil.getElementData(elemProp);
                        } else if (propType != null
                                && propType.equalsIgnoreCase(IPSTmxDtdConstants.ATTR_VAL_TOOLTIP)) {
                            tooltip = PSXMLDomUtil.getElementData(elemProp);
                        }
                    }
                }

                // get the value from "seg" element
                nlSeg = elemTuv.getElementsByTagName(IPSTmxDtdConstants.ELEM_SEG);
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
    }
    /**
     * Method to empty the resource cache.
     */
   private void flushCache()
   {
      if(ms_ResourceBundles.isEmpty())
         return;

      log.info("Flushing I18n Resource Cache...");
      ms_ResourceBundles.clear();

      log.info(SUBSYSTEM, "Done Flushing.");
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
           throws IOException, SAXException, ParserConfigurationException {
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

}
