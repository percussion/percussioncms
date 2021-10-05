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

package com.percussion.util;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xmldom.PSXmlDomContext;
import com.percussion.xmldom.PSXmlDomUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Properties;

import static org.apache.commons.lang.Validate.notNull;

/**
 * Utility class to apply tidy to HTML and/or extract content from HTML string.
 * 
 * @author yubingchen
 */
public class PSTidyUtils
{
    /**
     * The logger for this class.
     */
    private static final Logger ms_logger = LogManager.getLogger("PSHtmlUtils");
    
    private static Properties m_tidyProperties = null;
    
    /**
     * Apply tidy to the specified HTML string.
     * 
     * @param source the string in question, not <code>null</code>.
     * @param filename the file name that contains the source. It may be <code>null</code> or empty.
     * 
     * @return the text after apply tidy to the HTML string in question, not <code>null</code>.
     * 
     * @throws PSExtractHTMLException if a tidy error occurs.
     * no error will be logged here, but expecting caller to log the error as needed.
     */
    public static String applyTidy(String source, String filename)
    {
       notNull(source, "source");
       Document doc = getTidiedDocument(source, filename);
       return PSXmlDocumentBuilder.toString(doc);
    }
    
    public static void setTidyProperties(Properties props)
    {
       notNull(props);
       m_tidyProperties = props;
    }
    
    /**
     * Apply tidy to the specified HTML string.
     * 
     * @param source the string in question, not <code>null</code>.
     * @param filename the file name that contains the source. It may be <code>null</code> or empty.
     * 
     * @return the text after apply tidy to the HTML string in question, not <code>null</code>.
     */
    public static Document getTidiedDocument(String source, String filename)
    {
       PSXmlDomContext context = new PSXmlDomContext("Tidy HTML");
       context.setServerPageTags(null); // disable server-page-tags process
       notNull(source, "source");
       try
       {
          if (m_tidyProperties == null)
             context.setTidyProperties("rxW2Ktidy.properties");
          else
             context.setTidyProperties(m_tidyProperties);
          
          // don't add additional name-space into <body> tag
          context.getTidyProperties().remove(PSXmlDomUtils.ADD_NAMESPACE_LIST);
          return PSXmlDomUtils.loadXmlDocument(context, source);
       }
       catch (Exception e)
       {
          String msg = e.getLocalizedMessage();
          if (e instanceof IOException)
          {
             msg = "Failed to apply tidy to source = \"" + source + "\"";
          }
          
          if (e instanceof PSExtensionProcessingException)
          {
             if (StringUtils.isBlank(filename))
                 msg = "Failed to apply tidy.";
             else
                 msg = "Failed to apply tidy to file '" + filename + "'.";
          }
          
          ms_logger.error(msg, e);
          throw new RuntimeException(msg, e);
       }
    }
    
}
