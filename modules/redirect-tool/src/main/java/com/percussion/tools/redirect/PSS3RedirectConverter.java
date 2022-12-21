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

public class PSS3RedirectConverter implements IPSRedirectConverter{
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
                                String delimiter) {
        return 0;
    }

    @Override
    public String convertVanityRedirect(PSPercussionRedirectEntry e) {
        return null;
    }

    @Override
    public String convertRegexRedirect(PSPercussionRedirectEntry e) {
        return null;
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String getAbsolutePrefix() {
        return null;
    }

    @Override
    public void setAbsolutePrefix(String prefix) {

    }
}
