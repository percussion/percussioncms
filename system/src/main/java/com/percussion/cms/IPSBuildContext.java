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
package com.percussion.cms;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to share information between a sequence of steps that
 * are executed polymorphically. The context is used while building a result
 * document for a query request. Typically, the build step should request the
 * document from the context and use it to create the nodes it needs while
 * building the DisplayField element for the output document. It should then
 * add the field using one of the other methods in the interface. It is
 * possible that a build step will not add a node at all.
 *
 * @see IPSBuildStep
 */
public interface IPSBuildContext
{
   /**
    * To create elements, a document is needed.
    *
    * @return The document object to which all fields added to this object
    *    will eventually be added. This object should be treated read-only.
    */
   public Document getResultDocument();

   /**
    * If a DisplayField element created by a build step should be hidden, it
    * should be added using this method after the node is created.
    *
    * @param dispNode A DisplayField element that should be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName The name of the control that should render this node.
    *    Must not be empty.
    *
    * @throws IllegalArgumentException if dispNode is <code>null</code> or
    *    controlName is <code>null</code> or empty.
    */
   public void addHiddenField( Element dispNode, String controlName );



   /**
    * If a DisplayField element created by a build step should be visible when
    * rendered, it should be added using this method after the node is created.
    *
    * @param dispNode A DisplayField element that should be visible when
    *    the editor is rendered. Must not be <code>null</code>.
    *
    * @param controlName The name of the control that should render this node.
    *    Must not be empty.
    *
    * @throws IllegalArgumentException if dispNode is <code>null</code> or
    *    controlName is <code>null</code> or empty.
    */
   public void addVisibleField( Element dispNode, String controlName );
}


