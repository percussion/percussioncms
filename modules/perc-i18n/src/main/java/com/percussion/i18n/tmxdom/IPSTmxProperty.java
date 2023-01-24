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
 * This interface defines common methods for the TMX node 'prop'. Refer to the
 * TMX 1.4 DTD for more details of this node at:
 * <p>
 * <a href="http://www.lisa.org/tmx/">Localisation Industry Standards Association</a>
 * </p>
 * @see IPSTmxDtdConstants
 */
public interface IPSTmxProperty
   extends IPSTmxNote
{
   /**
    * Method to get the type attribute of this node.
    * @return value of the 'type' attribute, never <code>null</code>
    * or <code>empty</code>
    */
   public String getType();

   /**
    * Sets type attribute for this node
    * @param    type if <code>null</code>, assumed <code>empty</code>.
    */
   public void setType(String type);
}
