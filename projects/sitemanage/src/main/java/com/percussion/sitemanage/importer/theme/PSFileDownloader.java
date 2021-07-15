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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.importer.theme;

import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.commons.lang.Validate.notNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.percussion.HTTPClient.URI;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.PSRequest;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper.LogCategory;
import com.percussion.sitemanage.importer.utils.PSAsyncFileDownload;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

/**
 * Downloader class that will be used to get files from an url and create a
 * local copy.
 * 
 * @author Santiago M. Murchio
 * 
 */
public class PSFileDownloader implements IPSFileDownloader
{
    private static HashSet<String> multiThreadSync = new HashSet<>();

    private enum Operation {
        DELETE, CHECK
    }

    public static synchronized boolean checkOperation(Operation operation, String item)
    {
        if (operation == Operation.DELETE)
        {
            multiThreadSync.remove(item);
            return true;
        }
        
        boolean result = !multiThreadSync.contains(item);
        if (result)
        {
            multiThreadSync.add(item);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.sitemanage.importer.theme.IPSFileDownloader#downloadFiles
     * (java.util.Map, com.percussion.sitemanage.importer.IPSSiteImportLogger)
     */
    @Override
    public List<PSPair<Boolean, String>> downloadFiles(Map<String, String> urlToPathMap, PSSiteImportCtx context,
            boolean createAsset)
    {
        List<PSPair<Boolean, String>> downloaded = new ArrayList<>();
        if (urlToPathMap.size() > 0)
        {
            notNull(urlToPathMap);

            Set<String> urls = urlToPathMap.keySet();

            final Map<String, Object> requestInfoMap = PSRequestInfo.copyRequestInfoMap();
            PSRequest request = (PSRequest) requestInfoMap.get(PSRequestInfo.KEY_PSREQUEST);
            requestInfoMap.put(PSRequestInfo.KEY_PSREQUEST, request.cloneRequest());

            PSAsyncFileDownload downloader = new PSAsyncFileDownload(requestInfoMap);

            for (String url : urls)
            {

                String filePath = urlToPathMap.get(url);
                if (checkOperation(Operation.CHECK, url))
                {
                    downloader.addDownload(filePath, url, createAsset);
                }
            }
            downloader.download();
            downloaded = downloader.getResults();

            for (String url : urls)
            {
                checkOperation(Operation.DELETE, url);
            }

        }
        return downloaded;
    }

    @Override
    public PSPair<Boolean, String> downloadFile(String url, String destination)
    {
        Map<String, String> downloads = new HashMap<>();
        downloads.put(url, destination);
        List<PSPair<Boolean, String>> downloaded = downloadFiles(downloads, null, false);
        return downloaded.get(0);
    }

}
