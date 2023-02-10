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
/**
 * 
 */
package com.percussion.pagemanagement.data;

public abstract class PSAbstractRegionNodeTreeVisitor implements IPSRegionNodeTreeVisitor {

    private IPSRegionNodeVisitor startRegionNodeVisitor = new IPSRegionNodeVisitor() {

        public void visit(PSRegionCode regionCode)
        {
            visitStart(regionCode);
        }

        public void visit(PSRegion region)
        {
            visitStart(region);
        }
    };
    
    private IPSRegionNodeVisitor endRegionNodeVisitor = new IPSRegionNodeVisitor() {

        public void visit(PSRegionCode regionCode)
        {
            visitEnd(regionCode);
        }

        public void visit(PSRegion region)
        {
            visitEnd(region);
        }
    };
    
    
    
    public IPSRegionNodeVisitor getStartRegionNodeVisitor()
    {
        return startRegionNodeVisitor;
    }

    public IPSRegionNodeVisitor getEndRegionNodeVisitor()
    {
        return endRegionNodeVisitor;
    }

    protected abstract void visitEnd(PSRegionCode regionCode);

    protected abstract void visitEnd(PSRegion region);

    protected abstract void visitStart(PSRegionCode regionCode);

    protected abstract void visitStart(PSRegion region);
    

}
