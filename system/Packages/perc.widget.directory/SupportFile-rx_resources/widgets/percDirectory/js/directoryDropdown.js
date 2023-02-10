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
(function($) {

    $.fn.dropDownControl = function(opts) {
        
        var orgXmlUrl = opts.orgUrl;
        var deptParentXmlUrl = opts.deptParentUrl;
        var deptXmlUrl = opts.deptUrl;
        var paramName = opts.paramName;
        var readonly = opts.readonly === "yes";
        var departmentVal = opts.departmentVal;
        var sortorder = opts.sortorder;
        var departmentXml;
        // The following 2 vars need to be hardcoded to match the current organization search
        var orgContainerSelect = $("#organizationSearch");
        var orgContainerSelectEdit = $('#perc-content-edit-organizationSearch');
        var deptContainerSelect = $("#" + paramName);
        var isOrgSelectDirty = false;
        var isDeptSelectDirty = false;
        var selectedDeptId = '-1';
        
        function getDirectoryData() {
            let deptXmlUrlTemp = deptXmlUrl;

            if (!readonly && orgContainerSelect.val() !== '-1') {
                deptXmlUrlTemp += "?sys_contentid=" + orgContainerSelect.val();
            }
            else if (readonly) {
                if (orgContainerSelectEdit.val() !== '-1') {
                    deptXmlUrlTemp += "?sys_contentid=" + orgContainerSelectEdit.val();
                }
            }

            getXml(deptXmlUrlTemp, function(xml) {
                departmentXml = xml;
                updateSelectors(deptContainerSelect, departmentXml.documentElement);
            });
        }
        
        /**
         * Returns an xml document from the specified URL.  Generic method
         * that can be used to retrieve XML from any XML application that
         * is defined.
         * @param {*} url the url of the xml application
         * @param {*} callback - callback contains xml doc to be used
         */
        function getXml(url, callback) {
            $.ajax({
                type: "GET",
                url: url,
                dataType: "xml",
                success: callback
            });
        }
        
        /**
         * Updates the <select> elements with results of a
         * DB lookup.  Another generic method that can update
         * a selector based on the container element.
         * @param {*} container the html container to modify. Must be the jQuery select element
         * @param {*} xml the xml doc
         * @see getXml
         */
        function updateSelectors(container, xml) {
            
            var orgNodes = xml.children;

            if (readonly) {
                for (var i = 0; i < orgNodes.length; i++) {
                    if (orgNodes[i].nodeType == 1) {
                        const orgName = orgNodes[i].children[0].textContent;
                        const orgId = orgNodes[i].children[1].textContent;
                        if (orgId === departmentVal) {
                            container.append(orgName);
                            return;
                        }
                    }
                }
                container.append('<option value="-1">Search All Departments</option>');
                return;
            }
            
            // If modifying more than 1 selector, this statement
            // must be modified to determine which text to use for the selector.
            container.append('<option value="-1">Search All Departments</option></br>');
            
            for (let i = 0; i < orgNodes.length; i++) {
                if (orgNodes[i].nodeType === 1) {
                    const orgName = orgNodes[i].children[0].textContent;
                    const orgId = orgNodes[i].children[1].textContent;
                    container.append('<option value=' + orgId + '>' + orgName + '</option>');
                }
            }

            // check first to see if the organization drop down is dirty.
            // if it is, do not set the value to previously selected value.
            if (departmentVal !== '-1' && !isOrgSelectDirty && !isDeptSelectDirty) {
                deptContainerSelect.val(departmentVal);
            }

            if (selectedDeptId !== '-1') {
                deptContainerSelect.val(selectedDeptId);
                selectedDeptId = '-1';
            }
            else if (selectedDeptId === '-1' && orgContainerSelect.val() === '-1') {
                deptContainerSelect.val('-1');
            }
        } // end method updateSelectors()
        
        /**
         * Watches the organization drop down for changes.
         * Updates the department drop down with only departments
         * from the respective organization that has been selected.
         */
        orgContainerSelect.on("change",function() {
            isOrgSelectDirty = true;
            const selectedValue = orgContainerSelect.val();

            if(selectedValue !== null) {
                deptContainerSelect.find('option').remove();
                callUpdateSelectors(deptContainerSelect, deptXmlUrl, selectedValue);
            }
        });

        /**
         * Watches the department drop down for changes.
         * Updates the organization drop down with the respective
         * parent organization.
         */
        deptContainerSelect.on("change",function() {
            isDeptSelectDirty = true;
            const selectedValue = deptContainerSelect.val();
            if (selectedValue !== null) {
                updateOrgSelector(deptParentXmlUrl, selectedValue);
            }
        });

        function updateOrgSelector(deptParentXmlUrl, value) {
            deptContainerSelect.find('option').remove();
            selectedDeptId = value;

            let xmlUrlTemp = deptParentXmlUrl;
            if (value !== '-1') {
                xmlUrlTemp += "?sys_contentid=" + value.toString();
            }
            
            getXml(xmlUrlTemp, function(xml) {
                let xmlTemp = xml.documentElement;
                xmlTemp = xmlTemp.children;
                if (selectedDeptId !== '-1') {
                    const value = xmlTemp[0].children[1].textContent;
                    orgContainerSelect.val(value);
                }

                getDirectoryData();
            });
        }

        /**
         * This function updates the <select> element with
         * values from the resulting XML document.  The query made
         * calls for all departments with the organization id as
         * the parent.
         * @param {*} container the jQuery container select element
         * @param {*} url the URL for the backend XML app
         * @param {*} id the parent organization id
         */
        function callUpdateSelectors(container, url, id) {
            let xmlUrlTemp = url;
            if (id !== '-1') {
                xmlUrlTemp += "?sys_contentid=" + id.toString();
            }
            getXml(xmlUrlTemp, function(xml) {
                updateSelectors(container, xml.documentElement);
            });
        }

        getDirectoryData();
    };
})(jQuery);
