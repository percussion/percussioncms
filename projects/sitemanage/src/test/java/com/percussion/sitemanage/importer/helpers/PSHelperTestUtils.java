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

package com.percussion.sitemanage.importer.helpers;

import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.PSSiteImporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class PSHelperTestUtils {

    public static String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:12.0) Gecko/20100101 Firefox/12.0";

    /**
     * Creates a tempconfig from a temp resource file in the same package as the
     * caller's class path returns PSPageContent for said file
     * 
     * @param pageName
     *            the name of the resource file
     * @param caller
     *            the caller Class
     * @param context
     *            a context for the PSPageContent
     * @return a PSPageContent for the resource file
     * @throws Exception
     *             most likely if Jsoup parsing bombs out
     */
    public PSPageContent createTempPageBasedOnResource(String pageName,
            Class caller, PSSiteImportCtx context) throws Exception {
        InputStream in = getClass().getResourceAsStream(pageName);
        File tempConfigFile = File.createTempFile(
                pageName.substring(0, pageName.lastIndexOf(".")),
                pageName.substring(pageName.lastIndexOf(".")));
        OutputStream out = new FileOutputStream(tempConfigFile);
        IOUtils.copy(in, out);
        Document doc = Jsoup.parse(tempConfigFile, "UTF-8");
        PSPageContent pageContent = PSSiteImporter.createPageContent(doc,
                context.getLogger());
        return pageContent;
    }
}
