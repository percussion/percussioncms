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

package com.percussion.html;

import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.xml.PSXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for handling HTML documents and
 * document fragments.  Intended for use with HTML content.
 * @see PSXmlUtils for XML content.
 */
public class PSHtmlUtils {

    private static final Logger log = LogManager.getLogger();

    /**
     * The name of the default html cleaner properties file
     * that is used to configure html cleaning defaults.
     */
    public static final String HTML_CLEANER_FILENAME="html-cleaner.properties";

    private static Document.OutputSettings getOutputSettings(Properties props, Charset encoding){
        Document.OutputSettings settings = new Document.OutputSettings();

        settings.charset(encoding);

        settings.prettyPrint(!props.getProperty(PROP_PRETTY_PRINT, "false").equalsIgnoreCase("false"));

        if(props.getProperty(PROP_OUTPUT_MODE,"xml").equalsIgnoreCase("xml"))
            settings.syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        else
            settings.syntax(Document.OutputSettings.Syntax.html);

        return settings;
    }

    /**
     * Given an html fragment return a jsoup cleaned and parsed document based on the
     * settings provided.
     *
     * @param fragment A string containing an html body fragment
     * @param encoding Encoding to use for the content
     * @param cleanse When true content will be cleansed using the supplied properties
     * @param configFile The path to the configuration file
     * @return A dom document
     */
    public static Document createHTMLDocument(String fragment,
                                              Charset encoding,
                                              boolean cleanse,
                                              String configFile) throws PSHtmlParsingException{
        Document ret = null;
        W3CDom w3dom = new W3CDom();


        Properties props;
        if(configFile != null) {
            props = getCleanerProperties(configFile);
        }else{
            props = getDefaultCleanerProperties();
        }

        Document.OutputSettings settings = getOutputSettings(props, encoding);

        if(cleanse){
            fragment = cleanseHTMLContent(fragment,encoding,configFile);
        }

        return Jsoup.parseBodyFragment(fragment).outputSettings(settings);

    }

    /**
     * Creates an HTML document from the supplied filename.
     *
     * @param file A valid file resource
     * @param encoding the target encoding of the file
     * @param cleanse when true the document will be cleansed using the supplied html-cleaner.properties in configFile parameter, or the system default.
     * @param configFile The absolute path to the html-cleaner.properties file to use for cleaning rules, if null, the system default will be used.
     * @return
     */
    public static Document createHTMLDocument(File file,
                                              Charset encoding,
                                              boolean cleanse,
                                              String configFile) throws PSHtmlParsingException {
        Document ret = null;

        if(file==null)
            throw new IllegalArgumentException("File must not be null");

        Properties props = new Properties();

        if(configFile != null){
            props = getCleanerProperties(configFile);
        }else{
            props = getDefaultCleanerProperties();
        }



        try{
            ret = Jsoup.parse(file,encoding.name()).outputSettings(getOutputSettings(props,encoding));

            String cleansed = "";
            if(cleanse){
                cleansed = cleanseHTMLContent(ret.html(),encoding,configFile);
            }
            //Apply the cleansed html
            ret.html(cleansed);
        } catch (IOException e) {
            throw new PSHtmlParsingException(e);
        }

        return ret;
    }

    /**
     * Given an html fragment will attempt to parse and cleanse the fragment
     * to contain valid HTML.
     * @param fragment a user supplied html fragment
     * @param encoding the encoding to use
     * @param configFile The property file to use for cleaner configuration
     * @return The cleansed html fragment
     */
    public static String cleanseHTMLContent(String fragment, Charset encoding, String configFile){

        String cleansed = "";
        if(fragment == null || StringUtils.isEmpty(fragment.trim())){
            return cleansed;
        }

        if(encoding == null){
            encoding = StandardCharsets.UTF_8;
        }

        Properties props;
        if(configFile != null) {
            //Catch any extensions or customizations that may be using the old system default and autowire to the new default.
            if(configFile.equals("rxW2Ktidy.properties")){
                configFile = "html-cleaner.properties";
            }
            props = getCleanerProperties(configFile);
        }else{
            props = getDefaultCleanerProperties();
        }

        Safelist safe = getSafeListFromProperties(props, fragment);
        Document.OutputSettings settings = getOutputSettings(props, encoding);

        cleansed  = Jsoup.clean(fragment, "https://parser", safe,settings);


        return cleansed;
    }

    /**
     * Builds a safe list based on the supplied properties.
     * @param props a set of properties from a html-cleaner.properties file
     * @param fragment The fragment that will be cleansed.  Needed to handle wildcard attrs like aria- or data-
     * @return A safelist based on the configured property values.
     */
    protected static Safelist getSafeListFromProperties(Properties props, String fragment){
        Safelist ret = new Safelist();

        String propVal = props.getProperty(PROP_BASE_SAFELIST,"relaxed");

        switch(propVal){
            case "none":
                ret = new Safelist(Safelist.none());
                break;
            case "simpleText":
                ret = new Safelist(Safelist.simpleText());
                break;
            case "basic":
                ret = new Safelist(Safelist.basic());
                break;
            case "basicWithImages":
                ret = new Safelist(Safelist.basicWithImages());
                break;
            default:
                ret = new Safelist(Safelist.relaxed());
        }

        //Now process the rules.
        propVal = props.getProperty(PROP_REL_LINKS,"true");
        ret.preserveRelativeLinks(Boolean.parseBoolean(propVal));

        propVal = props.getProperty(PROP_ADD_PROTOCOLS,"");
        ret = processAddProtocols(propVal, ret);

        propVal = props.getProperty(PROP_ALLOWED_TAGS);
        ret = processAllowedTags(propVal, ret);

        propVal = props.getProperty(PROP_ALLOWED_ATTRS);
        ret = processAllowedAttributes(fragment, propVal,ret);

        propVal = props.getProperty(PROP_ENFORCED_ATTRS);
        ret = processEnforcedAttrs( propVal,ret);

        propVal = props.getProperty(PROP_REMOVE_TAGS);
        ret = processRemovedTags(propVal, ret);

        propVal = props.getProperty(PROP_REMOVE_ATTRS);
        ret = processRemovedAttrs(propVal,ret);

        propVal = props.getProperty(PROP_REMOVE_PROTOCOLS);
        ret = processRemovedProtocols(propVal, ret);
        return ret;


    }

    private static Safelist processRemovedProtocols(String propVal, Safelist ret) {

        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] protocols = propVal.split(",");
        String[] protocolsTrimmed = Arrays.stream(protocols).map(String::trim).toArray(String[]::new);
        for(String p : protocolsTrimmed){
            String[] args = p.split(";");
            ret.removeProtocols(args[0].trim(),args[1].trim(),Arrays.copyOfRange(args,2,args.length));
        }

        return ret;
    }

    private static Safelist processRemovedAttrs(String propVal, Safelist ret) {
       if(propVal == null || StringUtils.isEmpty(propVal.trim()))
           return ret;

       String[] attrs = propVal.split(",");
       String[] attrsTrimmed = Arrays.stream(attrs).map(String::trim).toArray(String[]::new);
       for(String a : attrs){
           String[] args = a.split(";");
           ret.removeAttributes(args[0].trim(),Arrays.copyOfRange(args,1,args.length));
       }

        return ret;
    }

    protected static Safelist processRemovedTags(String propVal, Safelist ret) {
        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] tags = propVal.split(",");
        String[] tagsTrimmed = Arrays.stream(tags).map(String::trim).toArray(String[]::new);
        ret.removeTags(tagsTrimmed);

        return ret;
    }

    private static Safelist processEnforcedAttrs(String propVal, Safelist ret) {
        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] attrs = propVal.split(",");
        String[] attrsTrimmed =  Arrays.stream(attrs).map(String::trim).toArray(String[]::new);

        for(String a : attrsTrimmed){
            String[] args = a.split(";");
            if(args.length != 3){
                log.warn("Skipping enforcedAttribute entry as it is not well formed. Entry: {}", a);
            }else {
                ret.addEnforcedAttribute(args[0], args[1], args[2]);
            }
        }

        return ret;
    }

    protected static Safelist processAllowedAttributes(String fragment, String propVal, Safelist ret) {

        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] attrs = propVal.split(",");
        String[] attrsTrimmed = Arrays.stream(attrs).map(String::trim).toArray(String[]::new);
        Document doc = Jsoup.parse(fragment);

        for(String a : attrsTrimmed){
            ArrayList<String> args = new ArrayList<>(Arrays.asList(a.split(";")));
            String tag = args.get(0);
            ArrayList<String> extraAttrs = new ArrayList<>();
            for(String attrName : args){
                if(attrName.endsWith("-*")){
                    String subKey = attrName.substring(0,attrName.lastIndexOf("-*"));
                    //We need to handle wildcard tags
                    Elements elems = doc.getElementsByAttributeStarting(subKey);
                    for(Element e : elems){
                        for(Attribute ea : e.attributes()){
                            if(ea.getKey().startsWith(subKey)){
                                if(!extraAttrs.contains(ea.getKey())){
                                    extraAttrs.add(ea.getKey());
                                }
                            }
                        }
                    }
                }
            }
            args.remove(0);
            args.addAll(extraAttrs);
            ret.addAttributes(tag, args.toArray(new String[0]));
        }


        return ret;
    }


    protected static Safelist processAllowedTags(String propVal, Safelist ret) {

        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return  ret;
        String[] tags = propVal.split(",");
        String[] trimmedTags =  Arrays.stream(tags).map(String::trim).toArray(String[]::new);
        ret.addTags(trimmedTags);

        return ret;
    }

    /**
     *
     * @param propVal
     * @param ret
     * @return
     */
    protected static Safelist processAddProtocols(String propVal, Safelist ret) {

        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] protocols  = propVal.split(",");
        String[] trimmedP =  Arrays.stream(protocols).map(String::trim).toArray(String[]::new);
        for(String p : trimmedP){
            String[] args = p.split(";");
            ret.addProtocols(args[0].trim(),args[1].trim(), Arrays.copyOfRange(args,2,args.length));
        }
        return ret;
    }

    /**
     * Gets the html cleaner properties from the specified filename.
     * @param filename The path and filename of the html-cleaner.properties file. Will assume installroot if filename matches the system default filename.
     * @return The configured properties for the cleaner to use.
     */
    //TODO: Add caching
    public static Properties getCleanerProperties(String filename){
        Properties props = new Properties();

        //Load the properties file from the system root if the default is passed in.
        if("html-cleaner.properties".equals(filename)){
            String basePath = System.getProperty("rxdeploydir") + File.separatorChar;
            filename = basePath + filename;
        }

        try(FileInputStream is = new FileInputStream(new File(filename))){
            props.load(is);
        } catch (IOException e) {
            log.warn("Loading system default html-cleaner.properties as there was an error loading html cleaner properties from: {}. Error: {}",
                    filename,PSExceptionUtils.getMessageForLog(e));
            //Use the default properties instead.
            props = getDefaultCleanerProperties();
        }

        return props;
    }

    /**
     * Method to return a set of default cleaner properties in case the
     * property file can not be found or loaded.
     *
     * @return A default configuration for the HTML cleaner
     */
    //TODO: Add Caching
    public static Properties getDefaultCleanerProperties(){
        Properties props = new Properties();

        try(InputStream is = PSHtmlUtils.class.getResourceAsStream("/com/percussion/html/html-cleaner.properties")){
            props.load(is);
        } catch (IOException e) {
           log.error("Error loading default html cleaner properties.  HTML cleansing may be unreliable. Error: {}",
                   PSExceptionUtils.getMessageForLog(e));
        }

        return props;
    }

    /**
     * Get a W3C Dom Document from a JSoup document
     * @param doc a valid jsoup document
     * @return a valid w3c document
     */
    public static org.w3c.dom.Document getW3cDomDocument(Document doc){
        if(doc == null)
            throw new IllegalArgumentException("Document must not be null");

        W3CDom w3c = new W3CDom();
        return w3c.fromJsoup(doc);
    }

    /**
     * Gets the contents of the body element in the supplied html string.
     * @param doc A valid document
     * @return the contents of the body of the document.
     */
    public static String getBodyContents(Document doc){
        if(doc == null)
            throw new IllegalArgumentException("Document must not be null");

        return doc.body().html();
    }
    public static final String PROP_OUTPUT_MODE = "output-mode";
    public static final String PROP_BASE_SAFELIST = "base-safelist";
    public static final String PROP_REL_LINKS = "preserveRelativeLinks";
    public static final String PROP_ENCODING="encoding";
    public static final String PROP_ALLOWED_TAGS="allowedTags";
    public static final String PROP_REMOVE_TAGS="removeTags";
    public static final String PROP_ALLOWED_ATTRS="allowedAttributes";
    public static final String PROP_REMOVE_ATTRS="removeAttributes";
    public static final String PROP_ENFORCED_ATTRS="enforcedAttributes";
    public static final String PROP_ADD_PROTOCOLS="addProtocols";
    public static final String PROP_REMOVE_PROTOCOLS="removeProtocols";
    public static final String PROP_PRETTY_PRINT = "prettyPrint";


}
