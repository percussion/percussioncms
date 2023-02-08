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
    * and write any files or directories under <CODE>codeRoot</CODE>
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
    void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException;

   /**
    * The indicator flag which reflects that the implementing extension has
    * not been initialized yet.
    */
    int NOT_INITIALIZED = -1;

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
   int ERROR_INVALID_PARAMETER = 7403;


   //***** Predefined extension names *****
   
   /**
    * Name of the binary assembler.
    */
   String BINARY_ASSEMBLER =
         "Java/global/percussion/assembly/binaryAssembler";

   /**
    * Name of the text page assembler.
    */
   String DATABASE_ASSEMBLER =
         "Java/global/percussion/assembly/databaseAssembler";

   /**
    * Name of the debugging assembler used during development.
    */
   String DEBUG_ASSEMBLER =
         "Java/global/percussion/assembly/debugAssembler";

   /**
    * Name of the dispatch assembler
    */
   String DISPATCH_ASSEMBLER =
         "Java/global/percussion/assembly/dispatchAssembler";

   /**
    * Name of the legacy assembler. 
    */
   String LEGACY_ASSEMBLER =
         "Java/global/percussion/assembly/legacyAssembler";

   /**
    * Name of the text page assembler.
    */
   String VELOCITY_ASSEMBLER =
         "Java/global/percussion/assembly/velocityAssembler";
   
   Set<String> KNOWN_ASSEMBLERS = Collections.unmodifiableSet(
         new HashSet<>(Arrays.asList(BINARY_ASSEMBLER,
                 DATABASE_ASSEMBLER,
                 DEBUG_ASSEMBLER,
                 DISPATCH_ASSEMBLER,
                 LEGACY_ASSEMBLER,
                 VELOCITY_ASSEMBLER)));
}
