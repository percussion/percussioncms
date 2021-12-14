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
