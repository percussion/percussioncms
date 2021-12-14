/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
