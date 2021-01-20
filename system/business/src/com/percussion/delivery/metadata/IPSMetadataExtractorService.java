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

package com.percussion.delivery.metadata;

import com.percussion.delivery.metadata.extractor.data.PSMetadataEntry;

import java.io.Reader;
import java.util.Map;

/**
 * This interface defines the behavior of an extractor of metadata information
 * from pages. It provides two overloads of the 'process' method, one to process
 * files in the file system, and another one to read from a Reader object.
 * 
 * @author miltonpividori
 * 
 */
public interface IPSMetadataExtractorService
{
  
    /**
     * Process a page content represented by the Reader parameter. Pagepath is given
     * as a parameter, and site and folder are generated from it.
     * 
     * @param sourceToScan A Reader object representing the page content to be
     * processed to extract metadata information from.
     * @param pagepath The pagepath of the page. Site and folder are generated
     * from it.
     * @return A PSMetadataEntry with the extracted metadata from the Reader
     * object. Never <code>null</code>.
     */
    public abstract PSMetadataEntry process(Reader sourceToScan, String mimeType, String pagepath, Map<String,Object> additional);
}