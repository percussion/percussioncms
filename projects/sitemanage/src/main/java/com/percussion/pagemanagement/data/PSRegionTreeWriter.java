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
package com.percussion.pagemanagement.data;

import java.io.IOException;
import java.io.Writer;


public class PSRegionTreeWriter extends PSAbstractRegionNodeTreeVisitor
{
    protected Writer writer;
    
    

    public PSRegionTreeWriter(Writer writer)
    {
        super();
        this.writer = writer;
    }


    public void write(PSRegionNode node) throws PSRegionTreeWriterException {
        PSRegionTreeUtils.visitNodes(node, this);
    }


    @Override
    protected void visitEnd(PSRegionCode regionCode)
    {
        //Ignore
    }


    @Override
    protected void visitEnd(PSRegion region)
    {
        write(region.getEndTag());
    }


    @Override
    protected void visitStart(PSRegionCode regionCode)
    {
        write(regionCode.getTemplateCode());
    }


    @Override
    protected void visitStart(PSRegion region)
    {
        write(region.getStartTag());
    }
    
    protected void write(String s) {
        if (s != null) {
            try
            {
                writer.write(s);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                throw new PSRegionTreeWriterException(e);
            }
        }
    }
    
    public static class PSRegionTreeWriterException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
        public PSRegionTreeWriterException(String message) {
            super(message);
        }
        
        public PSRegionTreeWriterException(String message, Throwable cause) {
            super(message, cause);
        }
        public PSRegionTreeWriterException(Throwable cause) {
            super(cause);
        }
        
    }


    
    

}
