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
package com.percussion.widgetbuilder.utils;

import java.io.File;
import java.io.Reader;

/**
 * Transforms a file from a widget package as it is read from a stream.  
 * 
 * @author JaySeletz
 *
 */
public interface IPSWidgetFileTransformer
{

    /**
     * Transform the contents of the file based on the package spec.
     * 
     * @param file The file path of the file 
     * @param reader The content of the file, caller retains ownership of the underlying stream, and this method must not close it.
     * @param packageSpec The current package being generated
     * 
     * @return A reader to the modified content.
     */
    Reader transformFile(File file, Reader reader, PSWidgetPackageSpec packageSpec) throws PSWidgetPackageBuilderException;

    /**
     * Determine if this transformer should handle the supplied file
     * 
     * @param file The path of the file in the package, not <code>null</code>.
     * 
     * @return <code>true</code> if this transformer should handle the file, <code>false</code> if not.
     */
    boolean handleFile(File file);

    /**
     * Transform the file path of the file as necessary
     * 
     * @param file The path of the file in the package, not <code>null</code>.
     * @param packageSpec The current package being generated
     * @return The transformed file path, not <code>null</code>.
     */
    File transformPath(File file, PSWidgetPackageSpec packageSpec)  throws PSWidgetPackageBuilderException;

}
