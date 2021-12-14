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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

public class PSDocumentBuilderFactoryImpl extends org.apache.xerces.jaxp.DocumentBuilderFactoryImpl{

    public PSDocumentBuilderFactoryImpl() {
        super();
    }

    /**
     * Creates a new instance of a {@link DocumentBuilder}
     * using the currently configured parameters.
     */
    @Override
    public DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        return super.newDocumentBuilder();
    }

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.
     *
     * @param name  name of attribute
     * @param value null means to remove attribute
     */
    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        super.setAttribute(name, value);
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     *
     * @param name
     */
    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        return super.getAttribute(name);
    }

    @Override
    public Schema getSchema() {
        return super.getSchema();
    }

    @Override
    public void setSchema(Schema grammar) {
        super.setSchema(grammar);
    }

    @Override
    public boolean isXIncludeAware() {
        return super.isXIncludeAware();
    }

    @Override
    public void setXIncludeAware(boolean state) {
        super.setXIncludeAware(state);
    }

    @Override
    public boolean getFeature(String name) throws ParserConfigurationException {
        return super.getFeature(name);
    }

    @Override
    public void setFeature(String name, boolean value) throws ParserConfigurationException {
        super.setFeature(name, value);
    }

    /**
     * Specifies that the parser produced by this code will
     * provide support for XML namespaces. By default the value of this is set
     * to <code>false</code>
     *
     * @param awareness true if the parser produced will provide support
     *                  for XML namespaces; false otherwise.
     */
    @Override
    public void setNamespaceAware(boolean awareness) {
        super.setNamespaceAware(awareness);
    }

    /**
     * Specifies that the parser produced by this code will
     * validate documents as they are parsed. By default the value of this
     * is set to <code>false</code>.
     *
     * <p>
     * Note that "the validation" here means
     * <a href="http://www.w3.org/TR/REC-xml#proc-types">a validating
     * parser</a> as defined in the XML recommendation.
     * In other words, it essentially just controls the DTD validation.
     * (except the legacy two properties defined in JAXP 1.2.)
     * </p>
     *
     * <p>
     * To use modern schema languages such as W3C XML Schema or
     * RELAX NG instead of DTD, you can configure your parser to be
     * a non-validating parser by leaving the {@link #setValidating(boolean)}
     * method <code>false</code>, then use the {@link #setSchema(Schema)}
     * method to associate a schema to a parser.
     * </p>
     *
     * @param validating true if the parser produced will validate documents
     *                   as they are parsed; false otherwise.
     */
    @Override
    public void setValidating(boolean validating) {
        super.setValidating(validating);
    }

    /**
     * Specifies that the parsers created by this  factory must eliminate
     * whitespace in element content (sometimes known loosely as
     * 'ignorable whitespace') when parsing XML documents (see XML Rec
     * 2.10). Note that only whitespace which is directly contained within
     * element content that has an element only content model (see XML
     * Rec 3.2.1) will be eliminated. Due to reliance on the content model
     * this setting requires the parser to be in validating mode. By default
     * the value of this is set to <code>false</code>.
     *
     * @param whitespace true if the parser created must eliminate whitespace
     *                   in the element content when parsing XML documents;
     *                   false otherwise.
     */
    @Override
    public void setIgnoringElementContentWhitespace(boolean whitespace) {
        super.setIgnoringElementContentWhitespace(whitespace);
    }

    /**
     * Specifies that the parser produced by this code will
     * expand entity reference nodes. By default the value of this is set to
     * <code>true</code>
     *
     * @param expandEntityRef true if the parser produced will expand entity
     *                        reference nodes; false otherwise.
     */
    @Override
    public void setExpandEntityReferences(boolean expandEntityRef) {
        super.setExpandEntityReferences(expandEntityRef);
    }

    /**
     * <p>Specifies that the parser produced by this code will
     * ignore comments. By default the value of this is set to <code>false
     * </code>.</p>
     *
     * @param ignoreComments <code>boolean</code> value to ignore comments during processing
     */
    @Override
    public void setIgnoringComments(boolean ignoreComments) {
        super.setIgnoringComments(ignoreComments);
    }

    /**
     * Specifies that the parser produced by this code will
     * convert CDATA nodes to Text nodes and append it to the
     * adjacent (if any) text node. By default the value of this is set to
     * <code>false</code>
     *
     * @param coalescing true if the parser produced will convert CDATA nodes
     *                   to Text nodes and append it to the adjacent (if any)
     *                   text node; false otherwise.
     */
    @Override
    public void setCoalescing(boolean coalescing) {
        super.setCoalescing(coalescing);
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which are namespace aware.
     *
     * @return true if the factory is configured to produce parsers which
     * are namespace aware; false otherwise.
     */
    @Override
    public boolean isNamespaceAware() {
        return super.isNamespaceAware();
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which validate the XML content during parse.
     *
     * @return true if the factory is configured to produce parsers
     * which validate the XML content during parse; false otherwise.
     */
    @Override
    public boolean isValidating() {
        return super.isValidating();
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which ignore ignorable whitespace in element content.
     *
     * @return true if the factory is configured to produce parsers
     * which ignore ignorable whitespace in element content;
     * false otherwise.
     */
    @Override
    public boolean isIgnoringElementContentWhitespace() {
        return super.isIgnoringElementContentWhitespace();
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which expand entity reference nodes.
     *
     * @return true if the factory is configured to produce parsers
     * which expand entity reference nodes; false otherwise.
     */
    @Override
    public boolean isExpandEntityReferences() {
        return super.isExpandEntityReferences();
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which ignores comments.
     *
     * @return true if the factory is configured to produce parsers
     * which ignores comments; false otherwise.
     */
    @Override
    public boolean isIgnoringComments() {
        return super.isIgnoringComments();
    }

    /**
     * Indicates whether or not the factory is configured to produce
     * parsers which converts CDATA nodes to Text nodes and appends it to
     * the adjacent (if any) Text node.
     *
     * @return true if the factory is configured to produce parsers
     * which converts CDATA nodes to Text nodes and appends it to
     * the adjacent (if any) Text node; false otherwise.
     */
    @Override
    public boolean isCoalescing() {
        return super.isCoalescing();
    }
}
