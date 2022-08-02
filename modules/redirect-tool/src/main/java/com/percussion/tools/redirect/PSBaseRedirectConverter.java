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
    public int convertRedirects(PSPercussionRedirectEntryList redirects, String outDir) {
        int count = 0;
        List<String> lines = new ArrayList<>();

        for(PSPercussionRedirectEntry e : redirects){
            if (e.getType().equalsIgnoreCase("REGEX")) {
                lines.add(convertRegexRedirect(e));
            }else{
                lines.add(convertVanityRedirect(e));
            }
            count++;
        }

        int fileCount = PSRedirectUtils.writeRedirectFile(outDir,getFilename(),lines);
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
}
