/*[ IPSCreateModelItem.java ]******************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.guitools;

/**
 * @author DougRand
 *
 * This interface allows a model to state that it can create new instances
 * of an appropriate type for itself.
 */
public interface IPSCreateModelItem
{
   /**
    * Create a new instance of the appropriate object for the given model.
    * If there is a problem, throws an exception.
    * 
    * @return an instance of the correct class for the model, see the
    * specific model code for details.
    * 
    * @throws InstantiationException if an instance cannot be created.
    */
   Object createInstance() throws InstantiationException;
}
