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

package com.percussion.share.extension;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.server.IPSStartupProcess;
import com.percussion.server.IPSStartupProcessManager;
import com.percussion.server.PSServer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import com.percussion.share.dao.IPSGenericDao;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

@PSSiteManageBean(value = "simpleStartupProcessor")
public class PSSimpleStartupProcessor implements IPSStartupProcess
{
    private static final Logger log = LogManager.getLogger(PSSimpleStartupProcessor.class);

    @Autowired
    private IPSMetadataService metadataService;

    @Override
    public void doStartupWork(Properties startupProps) throws Exception
    {
        createDefaultGlobalVariablesJs();
    }

    private void createDefaultGlobalVariablesJs() throws IPSGenericDao.LoadException {
        //We always want to create the default file if it doesn't exist.



        Path path = Paths.get(PSServer.getRxDir().getAbsolutePath() +
                "/web_resources/cm/common/js/PercGlobalVariablesData.js");
        if( ! Files.exists(path,
            new LinkOption[]{ LinkOption.NOFOLLOW_LINKS})) {

            PSMetadata data = metadataService.find("percglobalvariables");
            if (data != null) {
                String msg = "/**** This is a system generated content, any modifications will be overwritten by the next save of global variables. *****/\n";
                String msg1 = "var PercGlobalVariablesData = ";
                try {
                    FileUtils.writeStringToFile(new File(PSServer.getRxDir().getAbsolutePath()
                                    + "/web_resources/cm/common/js/PercGlobalVariablesData.js"),
                            msg + msg1 + data.getData() + ";", StandardCharsets.UTF_8);
                } catch (IOException e) {
                    log.warn("Error creating default global variables file", e);
                }
            }else{
                String msg = "/**** This is a system generated content, any modifications " +
                        "will be overwritten by the next save of global variables. *****/\n" +
                        "var PercGlobalVariablesData = {};";

                log.info("Creating default PercGlobalVariablesData.js");
                try {
                    Files.write(path, msg.getBytes());
                } catch (IOException e) {
                    log.warn("Error creating default global variables file", e);
                }
            }
        }
    }

    @Override
    @Autowired
    public void setStartupProcessManager(IPSStartupProcessManager mgr)
    {
        mgr.addStartupProcess(this);
    }

    public void setMetadataService(IPSMetadataService metadataService)
    {
        this.metadataService = metadataService;
    }
}
