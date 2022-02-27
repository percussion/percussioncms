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

import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PSJBossConnectors extends PSAbstractXmlConnectors {


    private static final Path JBOSS_APPSERVER_PATH = Paths.get("AppServer");
    private static final Path JBOSS_SERVER_XML_PATH = JBOSS_APPSERVER_PATH.resolve(Paths.get("server","rx","deploy","jboss-web.deployer","server.xml"));
    private static final Path CM1_LEGACY_LAX_FILE = Paths.get("PercussionServer.lax");


    private final Path rxRootDir;



    private final Path serverFile;
    private final Path laxFile;


    public PSJBossConnectors(File rxDir)
    {
        super(JBOSS_APPSERVER_PATH.resolve(Paths.get("server","rx")));
        rxRootDir = rxDir.toPath();
        this.serverFile = rxRootDir.resolve(JBOSS_SERVER_XML_PATH);
        this.laxFile = rxRootDir.resolve(CM1_LEGACY_LAX_FILE);
    }

    public PSJBossConnectors(File rxDir,PSAbstractConnectors connectorInfo) {
        this(rxDir);
        setConnectors(connectorInfo.getConnectors());
    }



    public void load() {
        Map<String,String> laxProps = loadLaxProperties(rxRootDir);
        try {
            // load the doc
            Document doc = PSXmlUtils.getDocFromFile(getServerFile().toFile());
            fromXml(doc.getDocumentElement(),laxProps);
        } catch (IOException | SAXException | PSInvalidXmlException e) {
            throw new RuntimeException(e);
        }

    }


    public void save() {
        try {
            Map<String,String> laxProps = loadLaxProperties(rxRootDir);
            List<IPSConnector> toSaveConnectors = new ArrayList<>(this.getConnectors());

            this.getConnectors().clear();
            load();
            mergeConnectors(toSaveConnectors);
            Document doc = toXml();
            PSXmlUtils.saveDocToFile(serverFile.toFile(), doc);

        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }

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

    @Override
    public Path getServerFile() {
        return serverFile;
    }

    @Override
    public String toString() {
        return "PSJBossConnectors{" +
                "rxRootDir=" + rxRootDir +
                ", serverFile=" + serverFile +
                ", laxFile=" + laxFile + "," +super.toString() +
                '}';
    }


}
