/*
 *     Percussion CMS
 *     Copyright (C) 1999-2022 Percussion Software, Inc.
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

package com.percussion.html;

import com.percussion.error.PSExceptionUtils;
import com.percussion.utils.xml.PSXmlUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * Utility class for handling handling HTML documents and
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
            props = getCleanerProperties(configFile);
        }else{
            props = getDefaultCleanerProperties();
        }

        Safelist safe = getSafeListFromProperties(props);
        Document.OutputSettings settings = getOutputSettings(props, encoding);

        cleansed  = Jsoup.clean(fragment, "https://parser", safe,settings);


        return cleansed;
    }

    /**
     * Builds a safe list based on the supplied properties.
     * @param props a set of properties from an html-cleaner properties file
     * @return A safelist based on the configured property values.
     */
    protected static Safelist getSafeListFromProperties(Properties props){
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
        ret = processAllowedAttributes(propVal,ret);

        propVal = props.getProperty(PROP_ENFORCED_ATTRS);
        ret = processEnforcedAttrs(propVal,ret);

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

    protected static Safelist processAllowedAttributes(String propVal, Safelist ret) {

        if(propVal == null || StringUtils.isEmpty(propVal.trim()))
            return ret;

        String[] attrs = propVal.split(",");
        String[] attrsTrimmed = Arrays.stream(attrs).map(String::trim).toArray(String[]::new);

        for(String a : attrsTrimmed){
            String[] args = a.split(";");
            String tag = args[0];
            ret.addAttributes(tag,Arrays.copyOfRange(args,1,args.length));
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
     * @param filename
     * @return
     */
    //TODO: Add caching
    public static Properties getCleanerProperties(String filename){
        Properties props = new Properties();

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
