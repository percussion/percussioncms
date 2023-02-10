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
