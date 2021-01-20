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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
