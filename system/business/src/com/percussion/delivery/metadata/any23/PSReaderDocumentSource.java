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
