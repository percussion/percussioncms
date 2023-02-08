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
package com.percussion.UTComponents;

import javax.swing.*;
import java.awt.event.ActionListener;


/**
 * The abstract class for all the editor components which should work with
 * {@link UTCellEditor } which can be used as table cell editor.
 */
public abstract class UTEditorComponent extends JPanel
   implements IEditorComponent
{

   //see interface for description
   public abstract JComponent getEditorComponent();

   //see interface for description
   public abstract void addActionListener(ActionListener l);
}
