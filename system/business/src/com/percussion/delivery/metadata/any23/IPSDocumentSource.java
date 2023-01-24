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

import org.apache.any23.source.DocumentSource;

/**
 * DocumentSource (from Any23) implementation that defines a
 * 'close' method, to quietly closes every InputStream opened.
 * 
 * @author miltonpividori
 *
 */
public interface IPSDocumentSource extends DocumentSource
{
    /**
     * Closes every InputStream object returned by openInputStream() method.
     */
    public void close();
}
