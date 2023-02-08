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

package com.percussion.design.objectstore;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;


/**
 * The IPSComponent interface defines required methods for all component
 * level object store objects. Component objects are contained within
 * document objects, such as PSApplication. Components should be derived
 * from the PSComponent or PSCollectionComponent class rather than
 * implement this interface directly.
 *
 *   @see         PSComponent
 * @see         PSCollectionComponent
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public interface IPSComponent extends Cloneable {

   /**
    * Get the id assigned to this component.
    *
    * @return               the id assigned to this component
    */
   public abstract int getId();

   /**
    * Get the id assigned to this component.
    *
    * @param      id         the to assign the component
    */
   public abstract void setId(int id);

   /**
    * This method is called to create an XML element node with the
    * appropriate format for the given object. An element node may contain a
    * hierarchical structure, including child objects. The element node can
    * also be a child of another element node.
    *
    * @return    the newly created XML element node
    */
   public abstract Element toXml(Document doc);

   /**
    * This method is called to populate an object from its XML representation.
    * An element node may contain a hierarchical structure, including child 
    * objects. The element node can also be a child of another element node.
    * <p>
    * Each component should add itself to <code>parentComponents</code> before
    * constructing its child components, and should restore the original
    * <code>parentComponents</code> before returning.
    *
    * @param sourceNode   the XML element node to populate from, not <code>null
    * </code>.
    * @param parentDoc may be <code>null</code>.
    * @param parentComponents a collection of all the components created in
    * the process of creating this component.  May be <code>null</code>. 
    * 
    * @throws PSUnknownNodeTypeException if the XML element node does not 
    * represent a type supported by the class.
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       List<IPSComponent> parentComponents)
      throws PSUnknownNodeTypeException;

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    * 
    * @param   cxt The validation context.
    * 
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException;
   
   /**
    * Creates a new instance of this object, deep copying all member variables.
    * If an implementing class has mutable member variables, it must override 
    * this method and clone() each of those variables.  This method will create
    * a shallow copy if it is not overridden.
    * <p>
    * NOTE: Currently, this method has been overridden only in a subset of the
    * classes that require it.  Only those classes recursively aggregated by
    * <code>PSDisplayMapper</code> are correct.
    * 
    * @return a deep-copy clone of this instance, never <code>null</code>.
    */
   @Deprecated
   public Object clone();
}

