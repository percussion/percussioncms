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

package com.percussion.process;

import org.w3c.dom.Element;

import java.util.Map;

/**
 * The main interface for handling processes. Processes are named definitions
 * within an xml file that contain sufficient information to execute on one or
 * more OS's. 
 * <p>A process can be obtained by name from the {@link PSProcessManager}. The
 * only method of interest to the user of this framework is the {@link 
 * #start(Map) start} method. All other methods are used by the framework. 
 * <p>Generally, if a custom implementation were needed, implementers would
 * start w/ the {@link PSSimpleProcess} class and not implement this interface
 * directly.
 */
public interface IPSProcess
{
   /**
    * Constructs the state from the specified element.
    *
    * @param el the element defining the working directory, executable,
    * parameters and environment, never <code>null</code>. Must conform to
    * the defintion in sys_processes.dtd for the element named PSXProcess.
    *
    * @throws PSProcessException if any error occurs constructing the state
    * from the specified element.
    */
   public void fromXml(Element el) throws PSProcessException;

   /**
    * Returns the name of the process.
    *
    * @return the name of the process, never <code>null</code> or empty
    */
   public String getName();

   /**
    * Returns the type of the process.
    *
    * @return the type of the process, never <code>null</code> or empty
    */
   public String getType();

   /**
    * Returns the process definition for the current OS.
    *
    * @return the process definition for the current Operating System,
    * may be <code>null</code> if no process definition exists for the
    * current OS
    */
   public PSProcessDef getProcessDef();

   /**
    * Starts the process.
    *
    * @param ctx A {@link Map map} containing data for executing the
    * process, may not be <code>null</code>. Each entry contains a <code>
    * String</code> key and a <code>String</code> value. 
    *
    * @return the process action object which can be used to manage the
    * process, never <code>null</code>.
    *
    * @throws PSProcessException if any error occurs starting the process
    */
   public PSProcessAction start(Map ctx)
      throws PSProcessException;
   
   /**
    * Tag name of the root node for the element which can be specified
    * in the <code>fromXml</code> method.
    */
   public static final String NODE_NAME = "PSXProcess";

   /**
    * Attribute for specifying the function name.
    */
   public static final String ATTR_NAME = "name";

   /**
    * Attribute for specifying the function type.
    */
   public static final String ATTR_TYPE = "type";
}


