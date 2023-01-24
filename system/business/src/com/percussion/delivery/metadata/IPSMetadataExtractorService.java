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
