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

package com.percussion.delivery.metadata.any23;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Vector;

import org.apache.tika.io.IOUtils;
import org.apache.any23.source.ByteArrayDocumentSource;
import org.apache.any23.util.ReaderInputStream;

/**
 * This is a DocumentSource implementation to use Reader object as the
 * source by Any23.
 * 
 * @author miltonpividori
 *
 */
public class PSReaderDocumentSource extends ByteArrayDocumentSource implements IPSDocumentSource
{

    /**
     * All InputStream object returned by the openInputStream() method.
     */
    private List<InputStream> openInputStream = new Vector<>();

    public PSReaderDocumentSource(Reader reader, String mimeType) throws IOException
    {
        super(new ReaderInputStream(reader), "file:///", mimeType + "; charset=utf-8");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deri.any23.source.FileDocumentSource#openInputStream()
     */
    @Override
    public InputStream openInputStream() throws IOException
    {
        InputStream inputStream = super.openInputStream();

        openInputStream.add(inputStream);

        return inputStream;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.metadata.extractor.any23.IPSDocumentSource#close()
     */
    public void close()
    {
        for (InputStream in : openInputStream)
            IOUtils.closeQuietly(in);

        openInputStream.clear();
    }

}
