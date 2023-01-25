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

package com.percussion.workflow;


/**
 * An interface that defines methods for states context. The table joins are hidden
 * from the user.
 *
 * @author Rammohan Vangapalli
 * @version 1.0
 * @since 2.0
 *
 */
public interface IPSStatesContext
{
   /**
    * Get the primary key for the state object. 
    * @return the primary key, never <code>null</code> for a persisted object,
    * but will be <code>null</code> for a newly constructed object.
    */
   public IPSStatesContextPK getStatePK();
   
   /**
    * Gets the StateID
    * @return  StateID 
    */
  public int getStateID();

   /**
    * Gets the State Name
    * @return  State Name 
    */
  public String getStateName();

   /**
    * Gets the State Description
    * @return  State Description 
    */
  public String getStateDescription();

   /**
    * Lets to know if this state has valid flag true. Valid=true means this state
    * has the value "Y". Note that being in a publishable state may include
    * more than "Y".
    * @return  true if publishable
    */
  public boolean getIsValid();
  
   /**
    * Determine if this state has a valid flag indicating content should be
    * unpublished.
    * 
    * @return <code>true</code> if it is set to unpublish, <code>false</code>
    * otherwise.
    */
  public boolean getIsUnpublish();

  /**
   * Get the value of the valid flag
   * @return the valid flag value, never <code>null</code> or empty
   */
  public String getContentValidValue();

}
