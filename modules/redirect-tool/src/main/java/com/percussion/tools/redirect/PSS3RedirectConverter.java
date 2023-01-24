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
