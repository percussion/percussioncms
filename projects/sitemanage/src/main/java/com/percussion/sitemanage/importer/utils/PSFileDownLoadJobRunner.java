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

package com.percussion.sitemanage.importer.utils;

import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.percussion.HTTPClient.Log;
import com.percussion.HTTPClient.URI;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.share.service.exception.PSExtractHTMLException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.PSSiteImporter;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper.LogCategory;
import com.percussion.sitemanage.importer.theme.PSAssetCreator;
import com.percussion.sitemanage.importer.theme.PSFileDownloader;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.Validate;

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
            if (compareJob.getJob().getFile() == this.getJob().getFile()
                    && compareJob.getJob().getUrl() == this.getJob().getUrl()
                    && compareJob.getJob().getCreateAsset() == this.getJob().getCreateAsset())
                return true;
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

            File file = new File(destinationPath);

            if (doesFileExist(file))
                localResults.add(new PSPair<>(true, getWarningMessage(url, destinationPath)));

            copyToFile(fileUrl, file);

            localResults.add(new PSPair<>(true, getSucessMessage(url, destinationPath)));
        }
        catch (Exception e)
        {
            Exception debug = e;
            File destFile = new File(destinationPath);
            PSPair<Boolean, String> result = new PSPair<>(false, getErrorMessage(url, destFile.getName()));
            localResults.add(result);
        }
        return localResults;
        
    }

    private void copyToFile(URL fileUrl, File file) throws IOException
    {

        int timeout = PSSiteImporter.getImportTimeout();
        if (timeout > 0)
        {
            copyURLToFile(fileUrl, file, timeout, timeout);
        }
        else
        {
            copyURLToFile(fileUrl, file);
        }
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
        PSPurgableTempFile tempImage = null;
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
                tempImage = new PSPurgableTempFile("tempImage", fileExtension, null);
                copyToFile(fileUrl, tempImage);
                destinationPath = URLDecoder.decode(destinationPath);
                InputStream fileInput = new FileInputStream(tempImage);
                createAsset(fileInput, destinationPath, this.assetCreator);

                localResults.add(new PSPair<>(true, getSucessMessage(url, destinationPath)));
            }
            return localResults;
        }
        catch (Exception e)
        {
            File destFile = new File(destinationPath);
            localResults.add(new PSPair<>(false, getErrorMessage(url, destFile.getName())));
            return localResults;
        }
        finally
        {
            // delete the temp file
            if (tempImage != null)
                tempImage.delete();
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
     * @param url {@link File} file to be downloaded, assumed not be
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
