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
