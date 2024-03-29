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

package com.percussion.taxonomy.web;

import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.service.TaxonomyService;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

//TODO: Update this with annotations
public class AbstractControllerWithSecurityChecks {
    protected class TaxonParams {
        private int taxID;
        //Default to english
        private int langID = 1;
        private Integer nodeID = null;
        private Integer parentID = null;
        private boolean forJEXL = false;

        public int getTaxID() {
            return taxID;
        }

        public int getLangID() {
            return langID;
        }

        public Integer getNodeID() {
            return nodeID.intValue();
        }

        public Integer getParentID() {
            if(parentID == null){
                return null;
            }
            return parentID.intValue();
        }

        public Integer getNodeIDCanBeNull() {
            return nodeID;
        }
        
        public boolean getForJEXL(){
            return forJEXL;
        }

        public Integer getParentIDCanBeNull() {
            return parentID;
        }

        public TaxonParams(HttpServletRequest request, int taxID, int langID, TaxonomyService taxonomyService) throws Exception {
            if (request.getParameter("taxID") != null && request.getParameter("langID") != null) {
                TaxonParams tp = new TaxonParams(request,taxonomyService);
                this.taxID = tp.getTaxID();
                this.langID = tp.getLangID();
                this.forJEXL = tp.forJEXL;
            } else {
                this.taxID = taxID;
                this.langID = langID;
                if (request.getParameter("forJEXL") != null) {
                    this.forJEXL = Boolean.parseBoolean(request.getParameter("forJEXL"));
                }
            }
        }

        public boolean hasNodeID() {
            return (this.nodeID != null);
        }

        public boolean hasParentID() {
            return (this.parentID != null);
        }

		public TaxonParams(HttpServletRequest request, TaxonomyService taxonomyService) throws Exception {
			//there are two cases here --- Shawn
 			//1. if there is no parameter, getParemeter returns null
			//2. if there parameter is on Request but has no value returns empty String eg nodeID=&
			if (request.getParameter("taxID") != null && request.getParameter("taxID").length() > 0) {
				try {
				   this.taxID = Integer.parseInt(request.getParameter("taxID"));
				} catch (NumberFormatException e) {
				   int taxId = taxonomyService.getTaxonomyIdByName(TaxonomySecurityHelper.sanitizeInputForXSS(
						   StringUtils.stripToEmpty(request.getParameter("taxID"))));
				   if (taxId>0) {
				      this.taxID=taxId;
				   }
				}
				if (this.taxID <= 0) {
					throw new Exception("Invalid taxID param");
				}
			}else { this.taxID = 1;} //Default case if everything fails
		   
			if (request.getParameter("langID") != null && request.getParameter("langID").length() > 0) {
				this.langID = Integer.parseInt(request.getParameter("langID"));
				if (this.langID <= 0) {
					throw new Exception("Invalid langID param");
				}
			}else { this.langID = Language.DEFAUL_LANG;} //Default case if everything fails

			if (request.getParameter("nodeID") != null && request.getParameter("nodeID").length() > 0) {
				this.nodeID = new Integer(Integer.parseInt(request.getParameter("nodeID")));
				if (this.nodeID <= 0) {
					throw new Exception("Invalid nodeID param");
				}
			}
			if (request.getParameter("parentID") != null && request.getParameter("parentID").length() > 0) {
				this.parentID = new Integer(Integer.parseInt(request.getParameter("parentID")));
				if (this.parentID < 0) {
					throw new Exception("Invalid parentID param");
				}
			}
		}
    }

    
    protected String getUserName(HttpServletRequest request){
    	return StringUtils.defaultString(request.getRemoteUser(),"unknown");
    }

    protected void verifyNodeIsEditable(Node node) throws Exception {
        // TODO throw new class
        if (!canEditNode(node)) {
            throw new Exception("Taxonomy Permission Exception: cannot edit node");
        }
    }

    protected boolean canEditNode(Node node) throws Exception {
        boolean ret = false;
        
        if (TaxonomySecurityHelper.amITaxonomyAdmin()) {
            ret = true;
        } else {
            List<String> myRoles = TaxonomySecurityHelper.getMyRoles();
            Collection<Node_editor> editors = node.getNodeEditors();
            
            if (myRoles != null && editors != null) {
               
               for (Node_editor editor : editors) {
                  
                   if (myRoles.contains(editor.getRole())) {
                       ret = true;
                       break;
                   }
               }
            }
        }
        return ret;
    }
}
