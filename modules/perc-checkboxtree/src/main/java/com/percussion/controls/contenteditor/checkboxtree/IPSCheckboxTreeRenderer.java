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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.controls.contenteditor.checkboxtree;

import javax.swing.tree.TreeCellRenderer;

/**
 * Renderer interface that allows users to set and use extra parameters. These 
 * extra parameters can be used by subclasses to implement custom behavior.
 */
public interface IPSCheckboxTreeRenderer extends TreeCellRenderer, 
   IPSExtraParameters
{
}
