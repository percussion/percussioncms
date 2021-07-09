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
