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

import java.util.ArrayList;
import java.util.List;

import static com.percussion.tools.redirect.PSRedirectUtils.getRedirectOutputFile;


public class PSBaseRedirectConverter implements IPSRedirectConverter {

    protected static final Logger log = LogManager.getLogger(PSRedirectTool.class);

    /**
     * Runs a redirect conversion.
     *
     * @param redirects A non null list of PSRedirectEntries
     * @param outDir    The folder to generate the redirect file to
     * @return A count of the redirects processed
     */
    @Override
    public int convertRedirects(PSPercussionRedirectEntryList redirects, String outDir,
                                String fileStartChar,
                                String fileEndChar,
                                String delimiter
                                ) {
        int count = 0;
        List<String> lines = new ArrayList<>();

        for(PSPercussionRedirectEntry e : redirects){
            if (e.getCategory().equalsIgnoreCase("REGEX")) {
                lines.add(convertRegexRedirect(e));
            }else{
                lines.add(convertVanityRedirect(e));
            }
            count++;
        }

        int fileCount = PSRedirectUtils.writeRedirectFile(outDir,getFilename(),lines,
                 fileStartChar,
                 fileEndChar,
                delimiter);
        if(fileCount!=count){
            log.warn("{} lines were written to {}, but {} lines were detected.",
                    fileCount,getFilename(),count);
        }
        if(fileCount > 0) {
            log.info("{} redirects were exported to {}",
                    fileCount, getRedirectOutputFile(outDir, getFilename()));
        }
        return count;
    }

    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry e) {
        return e.toString();
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry e) {
        return e.toString();
    }

    @Override
    public String getFilename() {
        return null;
    }

    private String prefix;

    @Override
    public String getAbsolutePrefix() {
        return prefix;
    }

    @Override
    public void setAbsolutePrefix(String prefix) {
        this.prefix = prefix;
    }
}
