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

package com.percussion.sitemanage.importer.utils;

import com.percussion.HTTPClient.URI;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper.LogCategory;
import com.percussion.sitemanage.importer.theme.PSAssetCreator;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;
import static org.springframework.util.CollectionUtils.isEmpty;

public class PSFileDownLoadJobRunner implements Runnable
{
    private PSAssetCreator assetCreator = new PSAssetCreator();

    private PSFileDownloadJob job;

    private List<PSPair<Boolean, String>> results = new ArrayList<>();

    private boolean hasCompleted = false;

    private Map<String, Object> requestMap;

    public PSFileDownLoadJobRunner(PSFileDownloadJob job, Map<String, Object> requestMap)
    {
        this.job = job;
        this.requestMap = requestMap;
    }

    
    public boolean equals(Object obj)
    {

        if (obj instanceof PSFileDownLoadJobRunner)
        {
            PSFileDownLoadJobRunner compareJob = (PSFileDownLoadJobRunner) obj;
            return compareJob.getJob().getFile().equals(this.getJob().getFile())
                    && compareJob.getJob().getUrl().equals(this.getJob().getUrl())
                    && compareJob.getJob().getCreateAsset().equals(this.getJob().getCreateAsset());
        }
        return false;
    }



    public void setRequestInfo(Map<String, Object> requestInfoMap)
    {
        if (PSRequestInfo.isInited())
        {
            PSRequestInfo.resetRequestInfo();
        }
        PSRequestInfo.initRequestInfo(requestInfoMap);
    }

    @Override
    public void run()
    {
        setRequestInfo(this.requestMap);
        if (job.createAsset)
        {
            results = downloadCreateAsset(job.url, job.file);
        }
        else
        {
           
            results = downloadFile(job.url, job.file);
        }
        hasCompleted = true;
    }

    public List<PSPair<Boolean, String>> getResults()
    {
        return results;
    }

    public void setResults(List<PSPair<Boolean, String>> results)
    {
        this.results = results;
    }

    public boolean hasCompleted()
    {
        return hasCompleted;
    }

    public PSFileDownloadJob getJob()
    {
        return job;
    }

    public void setJob(PSFileDownloadJob job)
    {
        this.job = job;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.sitemanage.importer.theme.IPSFileDownloader#downloadFile
     * (String, String)
     */
    public List<PSPair<Boolean, String>> downloadFile(String url, String destinationPath)
    {
        List<PSPair<Boolean, String>> localResults = new ArrayList<>();
        try
        {
            URI uri = new URI(url);
            URL fileUrl = uri.toURL();

            if(url.equals(destinationPath)){
                File destFile = new File(destinationPath);
                PSPair<Boolean, String> result = new PSPair<>(false, getErrorMessage(url, destFile.getName()));
                localResults.add(result);
                return localResults;
            }
            File file = new File(destinationPath);

            if (doesFileExist(file))
                localResults.add(new PSPair<>(true, getWarningMessage(url, destinationPath)));

            if(copyToFile(fileUrl, file)){
                localResults.add(new PSPair<>(true, getSucessMessage(url, destinationPath)));
            }else{
                localResults.add(new PSPair<>(true, getMissingMessage(url, destinationPath)));
            }


        }
        catch (Exception e)
        {
            File destFile = new File(destinationPath);
            PSPair<Boolean, String> result = new PSPair<>(false, getErrorMessage(url, destFile.getName()));
            localResults.add(result);
        }
        return localResults;
        
    }

    private boolean copyToFile(URL fileUrl, File file) throws IOException
    {
        boolean returnStatus = false;
        int timeout = PSSiteImporter.getImportTimeout();

            HttpsURLConnection connection = null;
            Exception savedException = null;
            InputStream stream = null;
            try{
                connection = (HttpsURLConnection) fileUrl.openConnection();
                connection.addRequestProperty("User-Agent", "Mozilla");
                connection.connect();
                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK &&
                        connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM &&
                        connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                    returnStatus=false;
                }else {
                    connection.setConnectTimeout(timeout);
                    connection.setReadTimeout(timeout);

                    stream = connection.getInputStream();

                    copyInputStreamToFile(stream, file);
                    returnStatus = true;
                }
            } catch (Exception e) {
                savedException = e;
                throw e;
            } finally {
                if(stream != null) {
                    if (savedException != null) {
                        try {
                            stream.close();
                        } catch (Exception e2) {
                            savedException.addSuppressed(e2);
                        }
                    } else {
                        stream.close();
                    }
                }
                if(connection !=null){
                    connection.disconnect();
                }
            }
        return returnStatus;
    }

    /**
     * Downloads a given file from the given url to a temp file. Then an Asset
     * is created for the given resource if it was not already downloaded.
     * Otherwise an information message is logged to indicate this situation.
     * 
     * @param url {@link String} with the url where the file is hosted. Assumed
     *            not be <code>null</code>.
     * @param destinationPath {@link String} with the destination path for the
     *            downloaded file. No process is done to the path. Assumed not
     *            be <code>null</code>.
     * @return {@link PSPair}<{@link Boolean}, {@link String}> never
     *         <code>null</code>. The first element is <code>true</code> if the
     *         download was successfull, and <code>false</code> otherwise. The
     *         second element is the successfull message, or an error message,
     *         depending if the download could be completed or not.
     */
    public List<PSPair<Boolean, String>> downloadCreateAsset(String url, String destinationPath)
    {
        List<PSPair<Boolean, String>> localResults = new ArrayList<>();

        try
        {
            URI uri = new URI(url);
            URL fileUrl = uri.toURL();

            boolean assetExist = PSPathUtils.doesItemExist(destinationPath);

            if (assetExist)
            {
                localResults.add(new PSPair<>(true, getWarningMessage(url, destinationPath)));
            }
            else
            {
                String fileExtension = "." + FilenameUtils.getExtension(destinationPath);
                try(PSPurgableTempFile tempImage = new PSPurgableTempFile("tempImage", fileExtension, null)) {
                    if (copyToFile(fileUrl, tempImage)) {
                        destinationPath = URLDecoder.decode(destinationPath);
                        try (InputStream fileInput = new FileInputStream(tempImage)) {
                            createAsset(fileInput, destinationPath, this.assetCreator);
                        }
                        localResults.add(new PSPair<>(true, getSucessMessage(url, destinationPath)));
                    } else {
                        localResults.add(new PSPair<>(true, getMissingMessage(url, destinationPath)));
                    }
                }
            }
            return localResults;
        }
        catch (Exception e)
        {
            File destFile = new File(destinationPath);
            localResults.add(new PSPair<>(false, getErrorMessage(url, destFile.getName())));
            return localResults;
        }
    }
    
    public static synchronized boolean createAsset(InputStream fileInput, String destinationPath, PSAssetCreator assetCreator)
    {
        try
        {
            if (!PSPathUtils.doesItemExist(destinationPath))
            {
                assetCreator.createAssetIfNeeded(fileInput, destinationPath);
            }
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Builds the error message to use when a download could not be finished.
     * 
     * @param url {@link String} with the url, Assumed not be <code>null</code>.
     * @param destinationPath {@link String}, assumed not be <code>null</code>.
     * @param ex the {@link Exception} to grab the message from. Assumed not
     *            <code>null</code>.
     * @return a {@link String} with the message, never <code>null</code> or
     *         empty.
     */
    private static String getErrorMessage(String url, String destinationPath, Exception ex)
    {
        return "Failed to download '" + url + "' to '" + destinationPath + "'. The underlying error is "
                + ex.getMessage();
    }

    private static String getErrorMessage(String url, String fileName)
    {
        return fileName + ": Failed to download '" + url + "'";
    }

    /**
     * Builds the success message to use when a download was finished
     * successfully.
     * 
     * @param url {@link String} with the url, Assumed not be <code>null</code>.
     * @param destinationPath {@link String}, assumed not be <code>null</code>.
     * @return a {@link String} with the message, never <code>null</code> or
     *         empty.
     */
    private static String getSucessMessage(String url, String destinationPath)
    {
        return "Successfully downloaded '" + url + "' to '" + destinationPath + "'";
    }

    /**
     * Builds the informational message to use when a file was already
     * downloaded.
     * 
     * @param url {@link String} with the url, Assumed not be <code>null</code>.
     * @param destinationPath {@link String}, assumed not be <code>null</code>.
     * @return a {@link String} with the message, never <code>null</code> or
     *         empty.
     */
    private static String getWarningMessage(String url, String destinationPath)
    {
        return "Skip download '" + url + "' to '" + destinationPath + "', as such file already exists.";
    }

    /**
     * Builds the informational message to use when a file was already
     * downloaded.
     *
     * @param url {@link String} with the url, Assumed not be <code>null</code>.
     * @param destinationPath {@link String}, assumed not be <code>null</code>.
     * @return a {@link String} with the message, never <code>null</code> or
     *         empty.
     */
    private static String getMissingMessage(String url, String destinationPath)
    {
        return "Skip download '" + url + "' to '" + destinationPath + "', as such file is not downloadable from the server.";
    }

    /**
     * Logs the results into the logger. Uses the object returned from the
     * download methods, which already has the messages and the status.
     * 
     * @param results {@link List}<{@link PSPair}<{@link Boolean},
     *            {@link String}>> holding the results from the download
     *            process. Not <code>null</code>. If it is empty, nothing will
     *            be logged.
     * @param logger {@link IPSSiteImportLogger}, Not <code>null</code>.
     * 
     * @return <code>false</code> if the results contain any errors, otherwise
     *         <code>true</code>.
     */
    public static boolean logResults(List<PSPair<Boolean, String>> results, IPSSiteImportLogger logger)
    {
        Validate.notNull(results);
        Validate.notNull(logger);

        boolean success = true;
        if (isEmpty(results) || logger == null)
        {
            return success;
        }

        String category = LogCategory.DownloadFile.getName();
        for (PSPair<Boolean, String> result : results)
        {
            PSLogEntryType entryType;
            if (result.getFirst())
            {
                entryType = PSLogEntryType.STATUS;
            }
            else
            {
                entryType = PSLogEntryType.ERROR;
                success = false;
            }

            logger.appendLogMessage(entryType, category, result.getSecond());
        }

        return success;
    }

    /**
     * Checks if a given file was already downloaded.
     * 
     * @param file {@link File} file to be downloaded, assumed not be
     *            <code>null</code>.
     * @return a {@link Boolean} flag that indicates if the file was already
     *         downloaded, true if file exists. Never <code>null</code>. empty.
     */
    private boolean doesFileExist(File file)
    {
        if (file.exists())
            return true;

        return false;
    }
}
