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
package com.percussion.servlets.taglib;

/**
 * This component emits tha appropriate markup for a cascaded menu using the
 * CSS menu implementation.
 * 
 * @author dougrand
 *
 */
public class PSCascadeMenuTag extends PSJSFBaseTag
{
   @Override
   public String getComponentType()
   {
      return "com.percussion.jsf.CascadeMenu";
   }
}
