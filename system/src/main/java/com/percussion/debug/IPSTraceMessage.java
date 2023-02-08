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

package com.percussion.debug;

import com.percussion.design.objectstore.PSTraceInfo;

/**
 * This interface is implemented by classes that are used to generate trace 
 * messages of different types.  Each type of trace information will be 
 * represented by a class that
 * implements this interface.
 */
public interface IPSTraceMessage
{

   /**
    * Retreives the necessary information from the object it was instantiated
    * with, and then formats the output of its message and sends it to the
    * supplied output stream.  Will not supply a messge body if Timestamp only
    * option is true.
    * @param traceInfo The trace options set by the application.  May not be
    * <code>null</code>.
    * @param source the source of the information to be used in generating the
    * trace message.   May not be <code>null</code>.
    * @param target The writer to which the formatted message written.
    * May not be <code>null</code>.
    * @throws IOException if there is a problem writing to the writer.
    *
    */
   public void printTrace(PSTraceInfo traceInfo, Object source,
      PSTraceWriter target)
   throws java.io.IOException;

   /**
    * Used to identify the type of information this object is used to trace.
    *
    * @return The type flag of the object implementing this interface.
    */
   public int getTypeFlag();
}
