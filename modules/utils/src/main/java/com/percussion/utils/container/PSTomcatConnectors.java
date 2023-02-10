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

package com.percussion.utils.container;

import com.percussion.install.PSLogger;
import com.percussion.utils.xml.PSInvalidXmlException;
import com.percussion.utils.xml.PSXmlUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PSTomcatConnectors extends PSAbstractXmlConnectors {

    private static final Path SERVER = Paths.get("Server");
    private static final Path SERVER_XML_PATH = SERVER.resolve(Paths.get("conf/server.xml.5.3"));
    private static final Path CM1_LEGACY_LAX_FILE = Paths.get("PercussionServer.lax");
    private static final Path CM1_LEGACY_LAX_FILE_LINUX = Paths.get("PercussionServer.bin.lax");


    private  Path rxRootDir;
    private  Path serverFile;
    private  Path laxFile;
    private  Path propertiesFile;

    private  Path dtsRoot;

    public PSTomcatConnectors(Path rxDir, Path dtsRoot)
    {
        super(rxDir.resolve(dtsRoot.resolve("Server")));
       // dtsRoot = rxDir.resolve(dtsRoot);
        rxRootDir = rxDir;
        this.serverFile = dtsRoot.resolve(SERVER_XML_PATH);

        if(getOperatingSystem().toUpperCase().contains("WINDOWS")){
            this.laxFile = rxDir.resolve(CM1_LEGACY_LAX_FILE);
        }else{
            this.laxFile = rxDir.resolve(CM1_LEGACY_LAX_FILE_LINUX);
        }

        this.propertiesFile = dtsRoot.resolve("Server/conf/perc/perc-catalina.properties");
        PSLogger.logInfo("Perc-Catalina properties file*********" + this.propertiesFile);
    }

    public String getOperatingSystem() {
        String os = System.getProperty("os.name");
        return os;
    }

    @Override
    public String toString() {
        return "PSTomcatConnectors {" +
                "rxRootDir=" + rxRootDir +
                ", serverFile=" + serverFile +
                ", laxFile=" + laxFile + "," +super.toString() +
                '}';
    }


    public void load() {
        Map<String,String> props=null;
        if (Files.exists(propertiesFile)) {
            PSLogger.logInfo("Perc-Catalina Exists *********" + this.propertiesFile);
            props = loadProperties(propertiesFile);
        }
        if(getServerFile().toFile().exists()) {
            try {
                // load the doc
                Document doc = PSXmlUtils.getDocFromFile(getServerFile().toFile());
                fromXml(doc.getDocumentElement(), props);
            } catch (IOException | SAXException | PSInvalidXmlException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void save() {
        try {
            Map<String,String> props=null;
            if (Files.exists(propertiesFile)) {
                props = loadProperties(propertiesFile);
            }
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

    @Override
    protected Path getServerFile() {
        return this.serverFile;
    }
}
