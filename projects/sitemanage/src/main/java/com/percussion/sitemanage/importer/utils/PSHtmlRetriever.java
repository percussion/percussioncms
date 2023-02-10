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

import com.percussion.sitemanage.importer.IPSConnectivity;

import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

/**
 * @author JaySeletz
 *
 */
public class PSHtmlRetriever
{
   
    /**
     * String for JSoup unhandled content type exception text 
     */
    private static final String UNHANDLED_CONTENT_TYPE = "Unhandled content type";
    
    private IPSConnectivity conn;
    
    public PSHtmlRetriever(IPSConnectivity conn)
    {
        Validate.notNull(conn);
        
        this.conn = conn;
    }
    
    public Document getHtmlDocument() throws IOException
    {
        Document doc = null;
        try
        {
            doc = conn.get();
        }
        catch (IOException e)
        {
            // if not an html doc, allow to return null
            if (!isUnhandledContentTypeException(e))
                throw e;
        }
        
        return doc;
    }
    
    /**
     * Determine if the supplied exception indicates we requested a resource from JSoup as a page.  See {@link Connection#ignoreContentType(boolean)}
     * for details (we leave this set to <code>true</code>).
     * 
     * @param e The exception to test, not <code>null</code>.
     * 
     * @return <code>true</code> if it's a JSoup unhandled content type exception, <code>false</code> if not.  
     */
    private boolean isUnhandledContentTypeException(IOException e)
    {
        return (e.getMessage() != null && e.getMessage().contains(UNHANDLED_CONTENT_TYPE));
    }
}
