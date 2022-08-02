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

package com.percussion.tools.redirect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Shared utility functions
 */
public class PSRedirectUtils {

    public static final Logger log = LogManager.getLogger(PSRedirectTool.class);

    /**
     * Writes a list of lines to a redirect file.
     * @param outdir The target directory
     * @param fileName The target filename
     * @param lines A list of strings to write
     */
    public static int writeRedirectFile(String outdir,
                                         String fileName, List<String> lines){
        int count = 0;
        Path p = Paths.get(outdir,fileName);
        try {
           if(Files.deleteIfExists(p)){
               log.info("Deleted existing file: {}" ,
                       p);
           }

           Path out =  Files.createFile(p);
           try(FileWriter fw = new FileWriter(out.toFile())){
                for(String s : lines){
                    if(s!=null) {
                        fw.write(s);
                        count++;
                    }
                }
                fw.flush();
           }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug(e);
        }
        return count;
    }

    public static String getRedirectOutputFile(String outDir, String file){
        return Paths.get(outDir,file).toAbsolutePath().toString();
    }
}
