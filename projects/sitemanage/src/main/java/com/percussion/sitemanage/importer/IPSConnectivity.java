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
package com.percussion.sitemanage.importer;

import java.io.IOException;

import org.jsoup.nodes.Document;

/**
 * Inner interface to wrap connectivity JSoup connectivity
 */
public interface IPSConnectivity
{
    /**
     * Wraps Jsoup get, fulfills IOException around binary parsing
     * 
     * @return a Jsoup document
     * @throws IOException when ignoreContent is set to false, and binary is
     *             encountered
     */
    Document get() throws IOException;

    /**
     * Fulfills Jsoup getResponseStatusCode contract
     * 
     * @return a valid HTTP response code
     */
    int getResponseStatusCode();

    /**
     * Fulfills Jsoup getResponseUrl contract
     * 
     * @return returns a String based representation for the URL
     */
    String getResponseUrl();
}
