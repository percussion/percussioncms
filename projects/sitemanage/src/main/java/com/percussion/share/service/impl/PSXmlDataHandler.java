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
package com.percussion.share.service.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.share.service.impl.jaxb.Data;
import com.percussion.share.service.impl.jaxb.Property;
import com.percussion.share.service.impl.jaxb.Request;
import com.percussion.share.service.impl.jaxb.Response;
import com.percussion.share.service.impl.jaxb.Settings;
import com.percussion.share.service.impl.jaxb.Property.Pvalues;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This handler uses jaxb to load data from a specified xml file.
 * 
 * @author peterfrontiero
 */
public class PSXmlDataHandler
{
    /**
     * Gets the response data associated with the request which matches the specified properties.
     * 
     * @param properties request properties, must not be <code>null</code>.
     * 
     * @return Response containing result data or <code>null</code> if a matching request could not be found or an
     * error occurs.
     */
    public Response getData(Map<String, Object> properties)
    {
        notNull(properties);
    
        InputStream is = null;
        try
        {
            JAXBContext jc = JAXBContext.newInstance("com.percussion.share.service.impl.jaxb");
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            is = new FileInputStream(new File(file));
            Data data = (Data) unmarshaller.unmarshal(is);
            List<Request> requests = data.getRequest();
            for (Request request : requests)
            {
                Map<String, Object> reqProps = new HashMap<>();
                Settings settings = request.getSettings();
                List<Property> props = settings.getProperty();
                for (Property prop : props)
                {
                    Object val;
                    Pvalues pvalues = prop.getPvalues();
                    if (pvalues != null)
                    {
                        val = pvalues.getPvalue();                        
                    }
                    else
                    {
                        val = prop.getValue();
                    }
                    
                    reqProps.put(prop.getName(), val);
                }
                
                if (reqProps.equals(properties))
                {
                    return request.getResponse();
                }
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred getting response data : ", e);
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException e)
                {
                }
            }
        }
                    
        return null;
    }

    /**
     * @return the file
     */
    public String getFile()
    {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file)
    {
        this.file = file;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSXmlDataHandler.class);
 
    /**
     * The path to the xml data file.  Initialized in constructor, never <code>null</code> after that.
     */
    private String file;

}
