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
package com.percussion.i18n.tmxdom;

/**
 * This interface defines method common to all leaf nodes. leaf nodes are node
 * That have text values and no other children.
 */
public interface IPSTmxLeafNode
   extends IPSTmxNode
{
   /**
    * Method to return value of the TMX leaf node.
    * @return value of the node, not <code>null</code> may be <code>empty</code>.
    */
   public String getValue();

   /**
    * Method to set the value for the TMX leaf node.
    * @param value if <code>null</code> specified empty value is assumed.
    */
   public void setValue(String value);
}
