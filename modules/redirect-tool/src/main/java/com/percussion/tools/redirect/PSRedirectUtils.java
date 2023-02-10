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
                                         String fileName,
                                        List<String> lines,
                                        String fileStartChar,
                                        String fileEndChar,
                                        String delimiter){
        int count = 0;
        Path p = Paths.get(outdir,fileName);
        try {
           if(Files.deleteIfExists(p)){
               log.info("Deleted existing file: {}" ,
                       p);
           }

           Path out =  Files.createFile(p);
           try(FileWriter fw = new FileWriter(out.toFile())){
               //Write file start characters
               if(fileStartChar!=null){
                   fw.write(fileStartChar);
               }
               //loop through the lines
               int idx = 0;
                for(String s : lines){
                    idx++; //increment every time
                    if(s!=null) {
                        count++; //only increment for valid lines
                        if(delimiter != null &&  idx < lines.size()){
                            //append delimiter unless this is the last line
                            s = s.concat(delimiter);
                        }
                        fw.write(s);
                    }
                }
                //Write the end chars
               if(fileEndChar != null){
                   fw.write(fileEndChar);
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
