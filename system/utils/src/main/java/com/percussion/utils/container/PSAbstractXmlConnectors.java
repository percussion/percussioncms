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

package com.percussion.utils.container;

import com.percussion.utils.tomcat.PSTomcatConnector;
import com.percussion.utils.xml.IPSXmlErrors;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PSAbstractXmlConnectors extends PSAbstractConnectors implements XMLEnabled {
    // private XML constants
    private static final String SERVER_NODE_NAME = "Server";
    private static final String SERVICE_NODE_NAME = "Service";
    /**
     * Xml node name of the Connector element.
     */
    public static final String CONNECTOR_NODE_NAME = "Connector";

    /**
     * Name of the JBoss property used in configuration files to specify the
     * address of the local host.
     */
    public static final String ADDRESS_PROP = "jboss.bind.address";

    private static final Pattern ADDRESS_PATTERN = Pattern.compile("-b (\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})");

    protected Map<String,String> properties = new HashMap<>();


    public PSAbstractXmlConnectors(Path connectorFileContet) {
        super(connectorFileContet);
    }

    protected Map<String,String> loadLaxProperties(Path rxRootDir) {
        HashMap<String,String> returnProps = new HashMap<>();

        Path laxFile = rxRootDir.resolve("PercussionServer.lax");

        Properties laxProperties = null;

        String address = "0.0.0.0";

        try {
            laxProperties = loadPropertiesFile(laxFile);

            String argsLine = laxProperties.getProperty("lax.command.line.args");

            if (argsLine != null && argsLine.contains("-b")) {
                Matcher matcher = ADDRESS_PATTERN.matcher(argsLine);
                while (matcher.find()) {
                    address = matcher.group(1);
                    System.out.println("Found jboss.bind.address " + address + " from lax file");
                    break;
                }
            }

        }catch (Exception e ){
            System.out.println("No lax file exists");
        }
        returnProps.put(ADDRESS_PROP, address);
        return returnProps;
    }



    /**
     * Copy all attributes from the source to the target
     *
     * @param source The source element, assumed not <code>null</code>.
     * @param target The target element, assumed not <code>null</code>.
     */
    private static void copyAttributes(Element source, Element target)
    {
        NamedNodeMap attrs = source.getAttributes();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++)
        {
            Attr attr = (Attr) attrs.item(i);
            target.setAttribute(attr.getName(), attr.getValue());
        }
    }

    public Document toXml() throws IOException, SAXException, PSInvalidXmlException {
        return this.toXml(getProperties());
    }


    public Document toXml(Map<String, String> properties) throws IOException, SAXException, PSInvalidXmlException {
        // create a new one
        Document newDoc = PSXmlDocumentBuilder.createXmlDocument();
        // load the doc
        Document curDoc = PSXmlUtils.getDocFromFile(getServerFile().toFile());
        
        // create new server element
        Element newServerEl = newDoc.createElement(SERVER_NODE_NAME);
        PSXmlDocumentBuilder.replaceRoot(newDoc, newServerEl);

        // find the old server element, copy attributes to new one
        PSXmlTreeWalker tree = new PSXmlTreeWalker(curDoc);
        Element root = (Element)tree.getCurrent();
        if (root == null || !root.getNodeName().equals(SERVER_NODE_NAME))
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
                    SERVER_NODE_NAME);
        copyAttributes(root, newServerEl);

        // create a new service element
        Element newServiceEl = PSXmlDocumentBuilder.addEmptyElement(newDoc,
                newServerEl, SERVICE_NODE_NAME);

        // find the old service element, copy attributes to new one
        Element serviceEl = tree.getNextElement(SERVICE_NODE_NAME,
                tree.GET_NEXT_ALLOW_CHILDREN);
        if (serviceEl == null)
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
                    SERVICE_NODE_NAME);
        copyAttributes(serviceEl, newServiceEl);

        // append new connector list
        for (IPSConnector connector : getConnectors())
        {
            PSAbstractConnector newconnector = PSTomcatConnector.getBuilder().setConnectorFileContext(getConnectorFileContext()).setCopyFrom(connector).build();
            newServiceEl.appendChild(newconnector.toXml(properties));
        }

        // walk the old doc, append non-http connectors and other elements
        int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
                PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
        int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
                PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

        Element testEl = tree.getNextElement(firstFlag);
        while (testEl != null)
        {
            // copy all but http connectors
            if (!(testEl.getNodeName().equals(
                    PSTomcatConnector.CONNECTOR_NODE_NAME) ))
            {
                PSXmlDocumentBuilder.copyTree(newDoc, newServiceEl, testEl);
            }

            testEl = tree.getNextElement(nextFlag);
        }

        return newDoc;
    }



    @Override
    public void fromXml(Element el) throws SAXException, IOException, PSInvalidXmlException {
        fromXml(el,getProperties());
    }


    public void fromXml(Element el, Map<String,String> properties) throws IOException, SAXException, PSInvalidXmlException {

        // load the doc
        Document doc = PSXmlUtils.getDocFromFile(getServerFile().toFile());

        PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
        Element root = (Element)tree.getCurrent();
        if (root == null || !root.getNodeName().equals(SERVER_NODE_NAME))
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
                    SERVER_NODE_NAME);

        Element serviceEl = tree.getNextElement(SERVICE_NODE_NAME,
                tree.GET_NEXT_ALLOW_CHILDREN);
        if (serviceEl == null)
            throw new PSInvalidXmlException(IPSXmlErrors.XML_ELEMENT_MISSING,
                    SERVICE_NODE_NAME);

        int firstFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
                PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
        int nextFlag = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
                PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

        List<IPSConnector> connList = new ArrayList<>();
        Element connEl = tree.getNextElement(
                CONNECTOR_NODE_NAME, firstFlag);
        while (connEl != null)
        {
            PSAbstractConnector newConnector = PSAbstractConnector.getBuilder().setConnectorFileContext(getConnectorFileContext()).setSource(connEl).setProps(properties).build();

            connList.add(newConnector);

            connEl = tree.getNextElement(CONNECTOR_NODE_NAME,
                    nextFlag);
        }

        setConnectors(connList);


    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public abstract void load();



    public abstract void save() ;

    protected abstract Path getServerFile();



}
