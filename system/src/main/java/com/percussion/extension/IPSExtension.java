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
package com.percussion.extension;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * IPSExtension is a lightweight interface to be implemented by extensions
 * of any kind. Implementing classes may support direct invocation,
 * inspection, etc. in their own way.
 * <p>
 * Generally, an extension handler will return an IPSExtension object
 * from the prepare() method, and the caller will cast this to some
 * known subtype, if necessary.
 */
public interface IPSExtension
{
   /**
    * Initializes this extension.
    * <p>
    * Note that the extension will have permission to read
    * and write any files or directiors under <CODE>codeRoot</CODE>
    * (recursively). The extension will not have permissions for
    * any other files or directories.
    *
    * @param def The extension def, which contains configuration
    * info and initialization params.
    *
    * @param codeRoot The root directory where this extension
    * should install and look for any files relating to itself. The
    * subdirectory structure under codeRoot is left up to the
    * extension implementation. Must not be <CODE>null</CODE>.
    *
    * @throws PSExtensionException If the codeRoot does not exist,
    * or is not accessible. Also thrown for any other initialization
    * errors that will prohibit this extension from doing its job
    * correctly, such as invalid or missing properties.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException;

   /**
    * The indicator flag which reflects that the implementing extension has
    * not been initialized yet.
    */
   public static final int NOT_INITIALIZED = -1;

   /**
    * A required parameter to the specified exit was invalid.
    * <p>
    * The arguments passed in for this message are:
    * <TABLE BORDER="1">
    * <TR><TH>Arg</TH><TH>Description</TH></TR>
    * <TR><TD>0</TD><TD>the exit name</TD></TR>
    * <TR><TD>1</TD><TD>the message related to the failure</TD></TR>
    * </TABLE>
    */
   public static final int ERROR_INVALID_PARAMETER = 7403;


   //***** Predefined extension names *****
   
   /**
    * Name of the binary assembler.
    */
   public static final String BINARY_ASSEMBLER =
         "Java/global/percussion/assembly/binaryAssembler";

   /**
    * Name of the text page assembler.
    */
   public static final String DATABASE_ASSEMBLER =
         "Java/global/percussion/assembly/databaseAssembler";

   /**
    * Name of the debugging assembler used during development.
    */
   public static final String DEBUG_ASSEMBLER =
         "Java/global/percussion/assembly/debugAssembler";

   /**
    * Name of the dispatch assembler
    */
   public static final String DISPATCH_ASSEMBLER =
         "Java/global/percussion/assembly/dispatchAssembler";

   /**
    * Name of the legacy assembler. 
    */
   public static final String LEGACY_ASSEMBLER =
         "Java/global/percussion/assembly/legacyAssembler";

   /**
    * Name of the text page assembler.
    */
   public static final String VELOCITY_ASSEMBLER =
         "Java/global/percussion/assembly/velocityAssembler";
   
   public static final Set<String> KNOWN_ASSEMBLERS = Collections.unmodifiableSet(
         new HashSet<String>(Arrays.asList(new String[] {
               BINARY_ASSEMBLER,
               DATABASE_ASSEMBLER,
               DEBUG_ASSEMBLER,
               DISPATCH_ASSEMBLER,
               LEGACY_ASSEMBLER,
               VELOCITY_ASSEMBLER}))); 
}
