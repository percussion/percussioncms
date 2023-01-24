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

import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.utils.types.PSPair;

import java.util.List;
import java.util.Map;

/**
 * @author Ignacio Erro
 * 
 */
public interface IPSFileDownloader
{

    /**
     * Downloads the given files into local files, specified in the parameter.
     * Returns a list of pairs so the caller can find out if the download was
     * successfull or not, together with the message to use in either case.
     * 
     * @param urlToPathMap {@link Map}<{@link String}, {@link String}> where the
     *            key is the url of the file, and the value is the absolute
     *            local path where the file should be saved to. Must not be
     *            <code>null</code>.
     * @param context {@link PSSiteImportCtx} object, must not be
     *            <code>null</code>.
     * @param createAsset <code>true</code> if an asset needs to be created. The
     *            resource is downloaded to a temp file, then it is deleted.
     *            <code>false</code> in other case.
     * @return {@link List}<{@link PSPair}<{@link Boolean}, {@link String}>>
     *         never <code>null</code>, but may be empty. For each value, he
     *         first element is <code>true</code> if the download was
     *         successfull, and <code>false</code> otherwise. The second element
     *         is the successfull message, or an error message, depending if the
     *         download could be completed or not.
     */
    public List<PSPair<Boolean, String>> downloadFiles(Map<String, String> urlToPathMap, PSSiteImportCtx context,
            boolean createAsset);

    /**
     * Downloads a given file from the given url. It writes that file in the
     * destination path if it does not exist. Otherwise an information message is
     * logged to indicate this finding.
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
    public PSPair<Boolean, String> downloadFile(String url, String destination);
}
