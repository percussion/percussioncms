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

package com.percussion.security.xml;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;

/**
 * Utility class for securing XML parses.
 */
public class PSSecureXMLUtils {

    // Set to true
    private static final String SECURE_PROCESSING_FEATURE = XMLConstants.FEATURE_SECURE_PROCESSING;

    // Set to true based on param
    private static final String DISALLOW_DOCTYPES_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";

    // Set to false
    private static final String SAX_GENERAL_EXTERNAL_ENTITIES_FEATURE="http://xml.org/sax/features/external-general-entities";

    // Set to true
    private static final String X1_GENERAL_EXTERNAL_ENTITIES_FEATURE = "http://xerces.apache.org/xerces-j/features.html#external-general-entities";

   //Set to true
    private static final String X2_GENERAL_EXTERNAL_ENTITIES_FEATURE="http://xerces.apache.org/xerces2-j/features.html#external-general-entities";

    //false
    private static final String X1_EXTERNAL_PARAMETER_ENTITIES_FEATURE="http://xerces.apache.org/xerces-j/features.html#external-parameter-entities";

    private static final String X2_EXTERNAL_PARAMETER_ENTITIES_FEATURE="http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities";

    private static final String SAX_EXTERNAL_PARAMETER_ENTITIES_FEATURE="http://xml.org/sax/features/external-parameter-entities";

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private static final boolean XINCLUDE_AWARE=false;
    private static final boolean EXPAND_ENTITY_REFERENCES=false;

    private static final Logger log = LogManager.getLogger(PSSecureXMLUtils.class);

    /**
     * Will return a Document DocumentBuilderFactory initialized with security features enabled.
     * The default settings follow OWASP guidelines for protecting against
     * XML eXternal Entity injection (XXE) vulnerabilities.:
     *
     * https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html
     *
     * As an XML application server / middleware that relies heavily on DTD's. the disable all DTD
     * feature is optional.
     *
     * @param dbf An initialized DocumentBuilderFactory
     * @param disableAllDocTypes When true all doc types are disabled, this is most secure but is not compatible with any XML that has a doctype declaration declared
     * @return The DocumentBuilderFactory with the secure features enabled.
     */
    public static DocumentBuilderFactory enableSecureFeatures(DocumentBuilderFactory dbf, boolean disableAllDocTypes){

        dbf.setXIncludeAware(XINCLUDE_AWARE);
        dbf.setExpandEntityReferences(EXPAND_ENTITY_REFERENCES);

        //Set each feature logging any errors as warnings for unsupported features.
        try{
            dbf.setFeature(SECURE_PROCESSING_FEATURE,true);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SECURE_PROCESSING_FEATURE);
        }

        try{
            dbf.setFeature(DISALLOW_DOCTYPES_FEATURE,disableAllDocTypes);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    DISALLOW_DOCTYPES_FEATURE);
        }

        try{
            dbf.setFeature(SAX_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SAX_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(X1_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X1_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(X2_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X2_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(X1_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X1_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(X2_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X2_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(SAX_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SAX_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            dbf.setFeature(LOAD_EXTERNAL_DTD,false);
        } catch (ParserConfigurationException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    LOAD_EXTERNAL_DTD);
        }


        return dbf;

    }

    /**
     * Secures XMLInputFactory instances.  External entities are disabled and DTD's are turned on
     * or off based on the caller.
     *
     * @param xif A valid XMLInputFactory
     * @param supportDTD When true, DTDs are supported, when false they aren't, false is the more secure option
     * @return
     */
    public static XMLInputFactory enableSecureFeatures(XMLInputFactory xif,  boolean supportDTD){
        // This disables DTDs entirely for that factory
        xif.setProperty(XMLInputFactory.SUPPORT_DTD, supportDTD);

        // disable external entities
        xif.setProperty("javax.xml.stream.isSupportingExternalEntities", false);

        return xif;
    }

    public static SAXParserFactory enableSecurityFeatures(SAXParserFactory spf, boolean disableAllDocTypes){

        //Set each feature logging any errors as warnings for unsupported features.
        try{
            spf.setFeature(SECURE_PROCESSING_FEATURE,true);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SECURE_PROCESSING_FEATURE);
        }

        try{
            spf.setFeature(DISALLOW_DOCTYPES_FEATURE,disableAllDocTypes);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    DISALLOW_DOCTYPES_FEATURE);
        }

        try{
            spf.setFeature(SAX_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SAX_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(X1_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X1_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(X2_GENERAL_EXTERNAL_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X2_GENERAL_EXTERNAL_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(X1_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X1_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(X2_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    X2_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(SAX_EXTERNAL_PARAMETER_ENTITIES_FEATURE,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    SAX_EXTERNAL_PARAMETER_ENTITIES_FEATURE);
        }

        try{
            spf.setFeature(LOAD_EXTERNAL_DTD,false);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("enableSecureFeatures exception thrown, XML Feature: {} is not supported by this XML Parser.",
                    LOAD_EXTERNAL_DTD);
        }

        return spf;
    }

}
