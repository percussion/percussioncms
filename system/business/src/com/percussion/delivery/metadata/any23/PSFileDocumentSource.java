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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.tika.io.IOUtils;
import org.apache.any23.source.FileDocumentSource;

/**
 * This is a DocumentSource implementation to use files as a source by
 * Any23.
 * <p>
 * This classes fixes some issues present in FileDocumentSource (from Any23). It
 * closes every InputStream created when the close method is invoked.
 * 
 * @author miltonpividori
 * 
 */
class PSFileDocumentSource extends FileDocumentSource implements IPSDocumentSource
{
    /**
     * All InputStream object returned by the openInputStream() method.
     */
    private List<InputStream> openInputStream;

    public PSFileDocumentSource(File file)
    {
        super(file);

        openInputStream = new ArrayList<InputStream>();
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