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
package com.percussion.pagemanagement.dao.impl;

import com.percussion.pagemanagement.data.PSMetadataDocType;
import com.percussion.pagemanagement.data.PSMetadataDocTypeOptions;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Provides utilities to convert the doc type value stored in DB, and to convert
 * the value supplied from the client before saving it in DB. Also provides a
 * default doc type to set when adding a new template.
 * 
 * @author leonardohildt
 * 
 */
public class PSMetadataDocTypeUtils
{

    // Constant for xhtml doc type
    private static final String XHTML = "xhtml";

    // Constant for html5 doc type
    private static final String HTML5 = "html5";

    // Constant for custom doc type
    private static final String CUSTOM = "custom";

    // Constant for xhtml doc type value
    private static final String PERC_XHTML = "PERC_XHTML";

    // Constant for html5 doc type value
    private static final String PERC_HTML5 = "PERC_HTML5";

    // Constant for custom doc type
    private static final String PERC_XHTML_DOC_TYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n"
            + "<html xml:lang=\"en\"\n"
            + "xmlns=\"http://www.w3.org/1999/xhtml\"\n"
            + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n"
            + "xmlns:dcterms=\"http://purl.org/dc/terms/\"\n"
            + "xmlns:perc=\"http://percussion.com/\"\n"
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">";

    // Constant for html5 doc type FIXME: i18n on HTML Lang
    private static final String PERC_HTML5_DOC_TYPE = "<!DOCTYPE html>\n" + "<html lang=\"en\" xmlns=\"http://www.w3.org/1999/xhtml\" prefix=\"dcterms: http://purl.org/dc/terms/ rdf: http://www.w3.org/1999/02/22-rdf-syntax-ns# perc: http://percussion.com/perc/elements/1.0/ og: http://ogp.me/ns# fb: http://ogp.me/ns/fb#\" >" ;


    /**
     * Convert the doc type value stored in the DB and generate a
     * PSMetadataDocType object, that will be returned to client. So when
     * PERC_XHTML -> xhmtl, PERC_HTML5 -> html5. Any other value is treated as
     * custom value, and in that case, it sets the selected value to custom.
     * 
     * @param docTypeValue String value that it is stored in DB for a given
     *            template, assumed not <code>null</code>.
     * 
     */
    public static PSMetadataDocType convertDocTypeValueToObject(String docTypeValue)
    {
        String docTypeName = XHTML;
        boolean customValue = false;
        PSMetadataDocType metaDocType = new PSMetadataDocType();
        List<PSMetadataDocTypeOptions> optionsList = new ArrayList<>();
        optionsList.add(new PSMetadataDocTypeOptions(XHTML, PERC_XHTML_DOC_TYPE));
        optionsList.add(new PSMetadataDocTypeOptions(HTML5, PERC_HTML5_DOC_TYPE));

        if (StringUtils.isBlank(docTypeValue))
        {
            metaDocType.setSelected(docTypeName);
            optionsList.add(new PSMetadataDocTypeOptions(CUSTOM, ""));
        }
        else
        {
            if (PERC_XHTML.equalsIgnoreCase(docTypeValue))
            {
                metaDocType.setSelected(XHTML);
            }

            if (PERC_HTML5.equalsIgnoreCase(docTypeValue))
            {
                metaDocType.setSelected(HTML5);
            }

            if (!PERC_HTML5.equalsIgnoreCase(docTypeValue) && !PERC_XHTML.equalsIgnoreCase(docTypeValue))
            {
                metaDocType.setSelected(CUSTOM);
                optionsList.add(new PSMetadataDocTypeOptions(CUSTOM, docTypeValue));
                customValue = true;
            }

            if (!customValue)
            {
                optionsList.add(new PSMetadataDocTypeOptions(CUSTOM, ""));
            }
        }
        metaDocType.setOptions(optionsList);

        return metaDocType;
    }

    /**
     * Provide the doc type to store in the DB. It is used when a new template
     * is added in the system. For newly templates, the default doc type is
     * HTML5.
     * 
     */
    public static String getDocType(PSMetadataDocType docType)
    {
        if (docType == null)
        {
            return PERC_XHTML;
        }
        
        String docTypeValue = docType.getSelected();
        if (XHTML.equalsIgnoreCase(docTypeValue) || PERC_XHTML.equalsIgnoreCase(docTypeValue))
        {
            return PERC_XHTML;
        }

        if (HTML5.equalsIgnoreCase(docTypeValue) || PERC_HTML5.equalsIgnoreCase(docTypeValue))
        {
            return PERC_HTML5;
        }
        
        String docTypeConverted = PERC_HTML5;
        for (PSMetadataDocTypeOptions option : docType.getOptions())
            {
                if (option.getOption().equalsIgnoreCase(CUSTOM))
                {
                    docTypeConverted = option.getValue();
                    break;
                }
            }
        
        return docTypeConverted;
    }
    
    /**
     * Provide the default doc type to set when a new template is created. For
     * newly templates, the default doc type is HTML5.
     * 
     */
    public static PSMetadataDocType getDefaultDocType()
    {
        PSMetadataDocType defaultDocType = new PSMetadataDocType();
        defaultDocType.setSelected(HTML5);
        return defaultDocType;
    }
    
}
