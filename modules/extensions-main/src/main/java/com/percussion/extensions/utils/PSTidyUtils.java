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

package com.percussion.extensions.utils;

import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.share.service.exception.PSExtractHTMLException;
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
