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
package com.percussion.share.dao;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

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
    protected static Logger log = LogManager.getLogger(PSXmlFileDataRepository.class);

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
            //Remove the BOM if it is present so it doesn't break serialization.
            Path p = Paths.get(fileEntry.getFileName());
            if(isContainBOM(p)){
                removeBom(p);
            }

            String text = new BufferedReader(
                    new InputStreamReader(data))
                    .lines()
                    .collect(Collectors.joining("\n"));

            object = PSSerializerUtils.unmarshal(text.trim(), type);
            if(object == null){
                log.debug("Unable to process XML {}",data);
            }
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
    public static class PSXmlFileDataRepositoryException extends Exception
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

    private static void removeBom(Path path) throws IOException {

        if (isContainBOM(path)) {

            byte[] bytes = Files.readAllBytes(path);

            ByteBuffer bb = ByteBuffer.wrap(bytes);

            log.debug("Found BOM!");

            byte[] bom = new byte[3];
            // get the first 3 bytes
            bb.get(bom, 0, bom.length);

            // remaining
            byte[] contentAfterFirst3Bytes = new byte[bytes.length - 3];
            bb.get(contentAfterFirst3Bytes, 0, contentAfterFirst3Bytes.length);

            log.debug("Remove the first 3 bytes, and overwrite the file!");

            // override the same path
            Files.write(path, contentAfterFirst3Bytes);

        } else {
            log.debug("This file doesn't contains UTF-8 BOM!");
        }

    }

    private static boolean isContainBOM(Path path) throws IOException {

        if (Files.notExists(path)) {
            throw new IllegalArgumentException("Path: " + path + " does not exists!");
        }

        boolean result = false;

        byte[] bom = new byte[3];
        try (InputStream is = new FileInputStream(path.toFile())) {

            // read 3 bytes of a file.
            is.read(bom);

            // BOM encoded as ef bb bf
            String content = new String(Hex.encodeHex(bom));
            if ("efbbbf".equalsIgnoreCase(content)) {
                result = true;
            }

        }

        return result;
    }

}
