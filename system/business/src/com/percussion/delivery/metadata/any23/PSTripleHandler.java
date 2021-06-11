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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.metadata.any23;

import com.percussion.delivery.metadata.PSMetadataExtractorService;
import com.percussion.delivery.metadata.extractor.data.PSMetadataProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.any23.extractor.ExtractionContext;
import org.apache.any23.writer.TripleHandler;
import org.apache.any23.writer.TripleHandlerException;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Value;


/**
 * Custom TripleHandler to collect metadata properties when a file is processed.
 * 
 * @author miltonpividori
 * 
 */
public class PSTripleHandler implements TripleHandler
{

    private static final Logger log = LogManager.getLogger(PSTripleHandler.class);

    /**
     * Accidental RDFa that will not be included as a metadata property.
     */
    private final String[] accidentalRDFaList =
        new String[] {
            "vocab#stylesheet",
            "#head",
            "#tr"
        };

    private static final String VOCAB_URL = "http://www.w3.org/1999/xhtml/vocab#";

    /**
     * Has every namespace of the page being processed. The key is the URL, the
     * value is the declared namespace. Fox example: {
     * 'http://purl.org/dc/terms/': 'dcterms' }. It used to replace the URL by
     * the declared name when filling PSMetadataProperty.name field.
     */
    private Map<String, String> namespacesByUrl = new HashMap<>();

    /**
     * All the PSMetadataProperty objects that were created from the metadata
     * properties extracted from the page being processed.
     */
    private Set<PSMetadataProperty> properties = new HashSet<>();

    /**
     * The linktext of the PSMetadataEntry.
     */
    private String pageLinktext;

    /**
     * The type of the PSMetadataEntry.
     */
    private String pageType;

    /**
     * Regular expression to separate the URL from the property name in a
     * Triple's predicate.
     */
    private Pattern patternForNamespaceURLExtraction = Pattern.compile("(.+[/#])([^/]+)");

    public PSTripleHandler()
    {
        // Replace XHTML vocab URL by an empty string.
        namespacesByUrl.put(VOCAB_URL, StringUtils.EMPTY);
        namespacesByUrl.put("http://percussion.com/perc/elements/1.0/", "perc");
        namespacesByUrl.put("http://purl.org/dc/terms/", "dcterms");
        namespacesByUrl.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        namespacesByUrl.put("http://ogp.me/ns#", "og");
        namespacesByUrl.put("http://ogp.me/ns/fb#", "fb");
        namespacesByUrl.put("http://vocab.sindice.net/any23#", "any23");
    }

    public Set<PSMetadataProperty> getProperties()
    {
        return properties;
    }

    public String getPageLinktext()
    {
        return pageLinktext;
    }

    public String getPageType()
    {
        if(StringUtils.isNotEmpty(pageType)) {
            return pageType;
        }else{
            HashMap map = new HashMap();
                for(PSMetadataProperty p : getProperties()) {
                    if(p.getName().equals("dcterms:type")) {
                        pageType = p.getValue();
                        return pageType;
                    }
                }
            }

        return "";
    }

    /**
     * Any23 extractor executes this method with every namespace of the page.
     */
    @SuppressWarnings("unused")
    public void receiveNamespace(String declaredNamespace, String namespaceUrl, ExtractionContext arg2)
            throws TripleHandlerException
    {
        if (!namespaceUrl.equals(VOCAB_URL))
            namespacesByUrl.put(namespaceUrl, declaredNamespace);
    }

    /**
     * Any23 extractor executes this method with every triple of the page. Here
     * the PSMetadataProperty objects are created with triples information.
     */
    @SuppressWarnings("unused")
    public void receiveTriple(Resource arg0, IRI propertyURL, Value propertyValue, IRI arg3, ExtractionContext arg4)
            throws TripleHandlerException
    {
        // Don't process accidental RDFa, as styles, etc.
        if (accidentalRDFa(propertyURL.toString()))
            return;

        // Process the propertyUrl
        PropertyURLProcessingResult propertyURLProcessingResult = processPropertyUrl(propertyURL.toString());

        // Check the property name. If it's 'alternative', then it maps to
        // PSMetadataEntry.linktext, and
        // 'type' to PSMetadataEntry.type. If none of them, then a
        // PSMetadataProperty is created.
        if (propertyURLProcessingResult.getPropertyNameWithDeclaredNamespace().equals(
                PSMetadataExtractorService.ALTERNATIVE_PROPERTY_NAME))
            pageLinktext = propertyValue.stringValue();
        else if (propertyURLProcessingResult.getPropertyNameWithDeclaredNamespace().equals(
                PSMetadataExtractorService.TYPE_PROPERTY_NAME))
            pageType = propertyValue.stringValue();
        else
        {
            PSMetadataProperty property = getMetadataProperty(propertyURLProcessingResult, propertyValue);

            if (property != null)
                properties.add(property);
        }
    }

    /**
     * Given a PropertyURLProcessingResult object, and the value of the metadata
     * property, it creates the PSMetadataProperty with its value.
     * 
     * @param propertyURLProcessingResult Result of processing the property URL.
     *            Should never be <code>null</code>.
     * @param propertyValue Property value. Should never be <code>null</code>.
     * @return PSMetadataProperty object with the metadata property information
     *         given. Can be <code>null</code> if there are errors in parsing
     *         the property value (for instance, a malformed date).
     */
    private PSMetadataProperty getMetadataProperty(PropertyURLProcessingResult propertyURLProcessingResult,
            Value propertyValue)
    {
        String propertyName = propertyURLProcessingResult.getPropertyNameWithDeclaredNamespace();

        String realPropertyValue = propertyValue.stringValue();
        
        PSMetadataProperty prop = new PSMetadataProperty(propertyName, realPropertyValue);

        return prop;
    }
    
    /**
     * Replace HTML tags that are typically self closing tags (like
     * <code>BR</code>), that were converted to a pair of tags by Any23. Write
     * those tags as self closing tags again (E.G. <BR/>
     * ) to avoid issues with browsers.
     * 
     * @param xhtmlPropertyValue XHTML that was generated by Any23 library.
     * @return xhtml property with all pairs of BR tags converted to self
     *         closing tags.
     */
    private String replaceSelfClosingTags(String xhtmlPropertyValue)
    {
        // This pattern matches <BR> tag pairs and content between them. (Case
        // insensitive).
        Pattern brTagsPattern = Pattern.compile("<\\s*BR[^>]*>(.*?)<\\s*/\\s*BR>", Pattern.CASE_INSENSITIVE);
        Matcher matcher = brTagsPattern.matcher(xhtmlPropertyValue);
        xhtmlPropertyValue = matcher.replaceAll("<BR/>");
        return xhtmlPropertyValue;
    }

    /**
     * Check if a property URL is accidental RDFa.
     * 
     * @param propertyUrl Property URL to check. Should never be
     *            <code>null</code>.
     * @return 'true' if the property URL is accidetal RDFa. 'false' otherwise.
     */
    private boolean accidentalRDFa(String propertyUrl)
    {
        for (String accidentalRDFa : accidentalRDFaList)
            if (propertyUrl.endsWith(accidentalRDFa))
                return true;
        
        // we take out the property url if it has more than 100
        // characters because it isn't a valid property name
        if(propertyUrl.length() > 100){
            return true;
        }

        return false;
    }

    public String getNamespace(String completePropertyUrl){
        if(!org.springframework.util.StringUtils.isEmpty(completePropertyUrl)) {
            if(completePropertyUrl.indexOf("#") > 0) {
                return completePropertyUrl.substring(0, completePropertyUrl.indexOf("#")+1);
            }else if(completePropertyUrl.lastIndexOf("/") >0){
                return completePropertyUrl.substring(0, completePropertyUrl.lastIndexOf("/")+1);
            } else{
                return "";
            }
        }
        return completePropertyUrl;
    }

    public String getPlainPropertyName(String completePropertyUrl){
        if(!org.springframework.util.StringUtils.isEmpty(completePropertyUrl)) {
            if(completePropertyUrl.indexOf("#") > 0) {
                return completePropertyUrl.substring(completePropertyUrl.indexOf("#") + 1);
            }else if(completePropertyUrl.lastIndexOf("/") >0){
                return completePropertyUrl.substring(completePropertyUrl.lastIndexOf("/") + 1);
            }
        }
        return completePropertyUrl;
    }

    /**
     * Given a complete property URL, it makes some processing on it, replacing
     * the URL by the declared namespace, or deleting it if it's the case of the
     * default namespace (vocab attribute).
     * 
     * @param completePropertyUrl The complete property url extracted. Should
     *            never be <code>null</code>.
     * @return A PropertyURLProcessingResult with information about processing.
     *         Never <code>null</code>.
     */
    private PropertyURLProcessingResult processPropertyUrl(String completePropertyUrl)
    {
        try {

            String namespaceUrl = getNamespace(completePropertyUrl);
            String plainPropertyName =  getPlainPropertyName(completePropertyUrl);
            String propertyName = completePropertyUrl;

            // Replace the URL by the declared namespace, or delete it if it's
            // the case of the default one.
            if (namespacesByUrl.containsKey(namespaceUrl)) {
                String replacement = namespacesByUrl.get(namespaceUrl);

                if (!StringUtils.isEmpty(replacement))
                    propertyName = completePropertyUrl.replace(namespaceUrl, replacement + ":");
                else
                    propertyName = completePropertyUrl.replace(namespaceUrl, replacement);
            }

            return new PropertyURLProcessingResult(plainPropertyName, propertyName);
        }catch(IllegalStateException e){
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }
        return new PropertyURLProcessingResult(completePropertyUrl, completePropertyUrl);
    }

    @SuppressWarnings("unused")
    public void close() throws TripleHandlerException
    {
        // no-op
    }

    @SuppressWarnings("unused")
    public void closeContext(ExtractionContext arg0) throws TripleHandlerException
    {
        // no-op
    }

    @SuppressWarnings("unused")
    public void endDocument(IRI arg0) throws TripleHandlerException
    {
        // no-op
    }

    @SuppressWarnings("unused")
    public void openContext(ExtractionContext arg0) throws TripleHandlerException
    {
        // no-op
    }

    @SuppressWarnings("unused")
    public void setContentLength(long arg0)
    {
        // no-op
    }

    @SuppressWarnings("unused")
    public void startDocument(IRI arg0) throws TripleHandlerException
    {
        // no-op
    }

    /**
     * Represent the result of processing a property URL. It contains the plain
     * property name (for example, "title") and the property name with declared
     * namespace (for example, "dcterms:title").
     * 
     * @author miltonpividori
     * 
     */
    class PropertyURLProcessingResult
    {
        /**
         * The plain property name. For example, "title".
         */
        private String plainPropertyName;

        /**
         * The property name along with the declared namespace. For example,
         * "dcterms:title".
         */
        private String propertyNameWithDeclaredNamespace;
        
        private static final String PROPERTY_ABSTRACT = "abstract";

        public PropertyURLProcessingResult(String plainPropertyName, String propertyNameWithDeclaredNamespace)
        {
            this.plainPropertyName = plainPropertyName;
            this.propertyNameWithDeclaredNamespace = propertyNameWithDeclaredNamespace;
        }

        /**
         * @return the plainPropertyName
         */
        public String getPlainPropertyName()
        {
            return plainPropertyName;
        }

        /**
         * @return the propertyNameWithDeclaredNamespace
         */
        public String getPropertyNameWithDeclaredNamespace()
        {
            return propertyNameWithDeclaredNamespace;
        }
    }
}