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
