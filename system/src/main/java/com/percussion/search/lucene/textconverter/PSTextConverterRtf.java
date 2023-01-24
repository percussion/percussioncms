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
package com.percussion.search.lucene.textconverter;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Extracts the text from input stream corresponding to RTF file using
 * RTFEditorKit.
 * 
 */
public class PSTextConverterRtf implements IPSLuceneTextConverter
{

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.search.lucene.textconverter.IPSLuceneTextConverter#getConvertedText(java.io.InputStream,
    * java.lang.String)
    */
   public String getConvertedText(InputStream is, String mimetype)
      throws PSExtensionProcessingException
   {
      String resultText = "";
      DefaultStyledDocument styledDoc = new DefaultStyledDocument();
      try
      {
         new RTFEditorKit().read(is, styledDoc, 0);
         resultText = styledDoc.getText(0, styledDoc.getLength());
      }
      catch (IOException e)
      {
         throw new PSExtensionProcessingException(m_className, e);
      }
      catch (BadLocationException e)
      {
         throw new PSExtensionProcessingException(m_className, e);
      }

      return resultText;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    * java.io.File)
    */
   public void init(IPSExtensionDef def, File codeRoot)
      throws PSExtensionException
   {

   }

   /**
    * A memeber variable to hold the name of this class.
    */
   private String m_className = getClass().getName();
}
