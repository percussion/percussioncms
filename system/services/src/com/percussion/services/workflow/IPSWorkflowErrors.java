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
package com.percussion.services.workflow;

/**
 * An enumeration of possible error conditions for the workflow service. Each
 * message code enumerated here must correspond to a message in the bundle for
 * this package.
 * 
 * @author dougrand
 */
public interface IPSWorkflowErrors
{
   /**
    * Failed to load workflow.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The workflow id</TD>
    * </TR>
    * </TABLE>
    */
   public static final int ERROR_LOADING_WORKFLOW = 2;

   /**
    * Failed to load workflow state.
    * <p>
    * The arguments passed in for this message are: <TABLE BORDER="1">
    * <TR>
    * <TH>Arg</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>0</TD>
    * <TD>The workflow state id</TD>
    * </TR>
    * <TR>
    * <TD>1</TD>
    * <TD>The workflow id</TD>
    * </TR>
    * </TABLE>
    */
   public static final int ERROR_LOADING_WORKFLOW_STATE = 3;
}
