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

import org.apache.xalan.processor.TransformerFactoryImpl;
import org.w3c.dom.Node;
import org.xml.sax.XMLFilter;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import java.net.URI;

public class PSTransformerFactoryImpl extends TransformerFactoryImpl {


    private void forceResolver(){
      //noop
    }

    /**
     * Constructor TransformerFactoryImpl
     */
    public PSTransformerFactoryImpl() {
        super();
        forceResolver();
    }

    @Override
    public Templates processFromNode(Node node) throws TransformerConfigurationException {
        forceResolver();
        return super.processFromNode(node);
    }

    /**
     * Get InputSource specification(s) that are associated with the
     * given document specified in the source param,
     * via the xml-stylesheet processing instruction
     * (see http://www.w3.org/TR/xml-stylesheet/), and that matches
     * the given criteria.  Note that it is possible to return several stylesheets
     * that match the criteria, in which case they are applied as if they were
     * a list of imports or cascades.
     *
     * <p>Note that DOM2 has it's own mechanism for discovering stylesheets.
     * Therefore, there isn't a DOM version of this method.</p>
     *
     * @param source  The XML source that is to be searched.
     * @param media   The media attribute to be matched.  May be null, in which
     *                case the prefered templates will be used (i.e. alternate = no).
     * @param title   The value of the title attribute to match.  May be null.
     * @param charset The value of the charset attribute to match.  May be null.
     * @return A Source object capable of being used to create a Templates object.
     * @throws TransformerConfigurationException
     */
    @Override
    public Source getAssociatedStylesheet(Source source, String media, String title, String charset) throws TransformerConfigurationException {
        forceResolver();
        return super.getAssociatedStylesheet(source, media, title, charset);
    }

    /**
     * Create a new Transformer object that performs a copy
     * of the source to the result.
     *
     * @return A Transformer object that may be used to perform a transformation
     * in a single thread, never null.
     * @throws TransformerConfigurationException May throw this during
     *                                           the parse when it is constructing the
     *                                           Templates object and fails.
     */
    @Override
    public TemplatesHandler newTemplatesHandler() throws TransformerConfigurationException {
        forceResolver();
        return super.newTemplatesHandler();
    }

    /**
     * <p>Set a feature for this <code>TransformerFactory</code> and <code>Transformer</code>s
     * or <code>Template</code>s created by this factory.</p>
     *
     * <p>
     * Feature names are fully qualified {@link URI}s.
     * Implementations may define their own features.
     * An {@link TransformerConfigurationException} is thrown if this <code>TransformerFactory</code> or the
     * <code>Transformer</code>s or <code>Template</code>s it creates cannot support the feature.
     * It is possible for an <code>TransformerFactory</code> to expose a feature value but be unable to change its state.
     * </p>
     *
     * <p>See {@link TransformerFactory} for full documentation of specific features.</p>
     *
     * @param name  Feature name.
     * @param value Is feature state <code>true</code> or <code>false</code>.
     * @throws TransformerConfigurationException if this <code>TransformerFactory</code>
     *                                           or the <code>Transformer</code>s or <code>Template</code>s it creates cannot support this feature.
     * @throws NullPointerException              If the <code>name</code> parameter is null.
     */
    @Override
    public void setFeature(String name, boolean value) throws TransformerConfigurationException {
        super.setFeature(name, value);
    }

    /**
     * Look up the value of a feature.
     * <p>The feature name is any fully-qualified URI.  It is
     * possible for an TransformerFactory to recognize a feature name but
     * to be unable to return its value; this is especially true
     * in the case of an adapter for a SAX1 Parser, which has
     * no way of knowing whether the underlying parser is
     * validating, for example.</p>
     *
     * @param name The feature name, which is a fully-qualified URI.
     * @return The current state of the feature (true or false).
     */
    @Override
    public boolean getFeature(String name) {
        return super.getFeature(name);
    }

    /**
     * Allows the user to set specific attributes on the underlying
     * implementation.
     *
     * @param name  The name of the attribute.
     * @param value The value of the attribute; Boolean or String="true"|"false"
     * @throws IllegalArgumentException thrown if the underlying
     *                                  implementation doesn't recognize the attribute.
     */
    @Override
    public void setAttribute(String name, Object value) throws IllegalArgumentException {
        super.setAttribute(name, value);
    }

    /**
     * Allows the user to retrieve specific attributes on the underlying
     * implementation.
     *
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     * @throws IllegalArgumentException thrown if the underlying
     *                                  implementation doesn't recognize the attribute.
     */
    @Override
    public Object getAttribute(String name) throws IllegalArgumentException {
        return super.getAttribute(name);
    }

    /**
     * Create an XMLFilter that uses the given source as the
     * transformation instructions.
     *
     * @param src The source of the transformation instructions.
     * @return An XMLFilter object, or null if this feature is not supported.
     * @throws TransformerConfigurationException
     */
    @Override
    public XMLFilter newXMLFilter(Source src) throws TransformerConfigurationException {
        return super.newXMLFilter(src);
    }

    /**
     * Create an XMLFilter that uses the given source as the
     * transformation instructions.
     *
     * @param templates non-null reference to Templates object.
     * @return An XMLFilter object, or null if this feature is not supported.
     * @throws TransformerConfigurationException
     */
    @Override
    public XMLFilter newXMLFilter(Templates templates) throws TransformerConfigurationException {
        return super.newXMLFilter(templates);
    }

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the transformation
     * instructions specified by the argument.
     *
     * @param src The source of the transformation instructions.
     * @return TransformerHandler ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    @Override
    public TransformerHandler newTransformerHandler(Source src) throws TransformerConfigurationException {
        return super.newTransformerHandler(src);
    }

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result, based on the Templates argument.
     *
     * @param templates The source of the transformation instructions.
     * @return TransformerHandler ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    @Override
    public TransformerHandler newTransformerHandler(Templates templates) throws TransformerConfigurationException {
        return super.newTransformerHandler(templates);
    }

    /**
     * Get a TransformerHandler object that can process SAX
     * ContentHandler events into a Result.
     *
     * @return TransformerHandler ready to transform SAX events.
     * @throws TransformerConfigurationException
     */
    @Override
    public TransformerHandler newTransformerHandler() throws TransformerConfigurationException {
        return super.newTransformerHandler();
    }

    /**
     * Process the source into a Transformer object.  Care must
     * be given to know that this object can not be used concurrently
     * in multiple threads.
     *
     * @param source An object that holds a URL, input stream, etc.
     * @return A Transformer object capable of
     * being used for transformation purposes in a single thread.
     * @throws TransformerConfigurationException May throw this during the parse when it
     *                                           is constructing the Templates object and fails.
     */
    @Override
    public Transformer newTransformer(Source source) throws TransformerConfigurationException {
        return super.newTransformer(source);
    }

    /**
     * Create a new Transformer object that performs a copy
     * of the source to the result.
     *
     * @return A Transformer object capable of
     * being used for transformation purposes in a single thread.
     * @throws TransformerConfigurationException May throw this during
     *                                           the parse when it is constructing the
     *                                           Templates object and it fails.
     */
    @Override
    public Transformer newTransformer() throws TransformerConfigurationException {

         return super.newTransformer();

    }

    /**
     * Process the source into a Templates object, which is likely
     * a compiled representation of the source. This Templates object
     * may then be used concurrently across multiple threads.  Creating
     * a Templates object allows the TransformerFactory to do detailed
     * performance optimization of transformation instructions, without
     * penalizing runtime transformation.
     *
     * @param source An object that holds a URL, input stream, etc.
     * @return A Templates object capable of being used for transformation purposes.
     * @throws TransformerConfigurationException May throw this during the parse when it
     *                                           is constructing the Templates object and fails.
     */
    @Override
    public Templates newTemplates(Source source) throws TransformerConfigurationException {
        forceResolver();
        return super.newTemplates(source);
    }

    /**
     * Set an object that will be used to resolve URIs used in
     * xsl:import, etc.  This will be used as the default for the
     * transformation.
     *
     * @param resolver An object that implements the URIResolver interface,
     *                 or null.
     */
    @Override
    public void setURIResolver(URIResolver resolver) {
        super.setURIResolver(resolver);
    }

    /**
     * Get the object that will be used to resolve URIs used in
     * xsl:import, etc.  This will be used as the default for the
     * transformation.
     *
     * @return The URIResolver that was set with setURIResolver.
     */
    @Override
    public URIResolver getURIResolver() {
        return super.getURIResolver();
    }

    /**
     * Get the error listener in effect for the TransformerFactory.
     *
     * @return A non-null reference to an error listener.
     */
    @Override
    public ErrorListener getErrorListener() {
        return super.getErrorListener();
    }

    /**
     * Set an error listener for the TransformerFactory.
     *
     * @param listener Must be a non-null reference to an ErrorListener.
     * @throws IllegalArgumentException if the listener argument is null.
     */
    @Override
    public void setErrorListener(ErrorListener listener) throws IllegalArgumentException {
        super.setErrorListener(listener);
    }

    /**
     * Return the state of the secure processing feature.
     *
     * @return state of the secure processing feature.
     */
    @Override
    public boolean isSecureProcessing() {
        return super.isSecureProcessing();
    }
}
