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
package com.percussion.share.dao;

import com.percussion.share.dao.PSSerializerUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Loads XML files using JAXB.
 * 
 * @author adamgent
 *
 * @param <T> Container type of items.
 * @param <ITEM> the type of object for each file.
 * @see #fileToObject(com.percussion.share.dao.PSFileDataRepository.PSFileEntry)
 * 
 */
public abstract class PSXmlFileDataRepository<T, ITEM> extends PSFileDataRepository<T>
{

    private Class<ITEM> type;
    
    /**
     * The class is passed into determine what object to build from the XML.
     * <p>
     * @param type a class that supports JAXB serialization.
     */
    public PSXmlFileDataRepository(Class<ITEM> type)
    {
        super();
        this.type = type;
    }


    /**
     * Converts a file into an Object using JAXB. Implementations should call this method
     * in {@link #update(java.util.Set)} for each file.
     * @param fileEntry
     * @return never <code>null</code>.
     * @throws IOException Cannot read the xml file.
     * @throws PSXmlFileDataRepositoryException Failure to load the XML file because its invalid.
     */
    protected ITEM fileToObject(PSFileDataRepository.PSFileEntry fileEntry) throws IOException, PSXmlFileDataRepositoryException {
        InputStream data = fileEntry.getInputStream();
        ITEM object;
        try
        {
            object = PSSerializerUtils.unmarshalWithValidation(data, type);
        }
        catch (Exception e)
        {
            throw new PSXmlFileDataRepositoryException("Failed to parse file: " + fileEntry.getFileName()
                    + ".  The file is invalid.", e);
        }
        return object;
    }
    
    /**
     * A failure in reading the XML.
     * @author adamgent
     *
     */
    public static class PSXmlFileDataRepositoryException extends RuntimeException
    {

        private static final long serialVersionUID = 1L;

        public PSXmlFileDataRepositoryException(String message)
        {
            super(message);
        }

        public PSXmlFileDataRepositoryException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSXmlFileDataRepositoryException(Throwable cause)
        {
            super(cause);
        }

    }

}
