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
   Simple-minded 'collapsible' implementation for prototyping.
   Jason Priestley
**/

(function($) {
var Collapsible = {
    _init : function() {
	var toggle = $("<span class='perc_collapsible_toggle'/>");
	var self=this;
	var toggle_on = function () { 
	    return $("<span/>")
		.append( $("<span class=\"ui-icon ui-icon-plus ui-state-disabled ui-state-default\">+</span>") )
		.append( $("<span class=\"ui-icon ui-icon-minus ui-state-default\">-</span>")
			 .on("mouseenter", function() { $(this).addClass("ui-state-hover"); })
			 .on("mouseleave",function() { $(this).removeClass("ui-state-hover");} )
			 .on("click", function() {
				     self.collapse(); 
				     toggle.empty().append( toggle_off() ); } ) );
	};

	var toggle_off = function () { 
	    return $("<span/>")
		.append( $("<span class=\"ui-icon ui-icon-plus ui-state-default\">+</span>")
			 .on("mouseenter", function() { $(this).addClass("ui-state-hover"); })
			 .on("mouseleave", function() { $(this).removeClass("ui-state-hover");} )
			 .on("click", function() {
				     self.uncollapse(); 
				     toggle.empty().append( toggle_on() ); } ) )
		.append( $("<span class=\"ui-icon ui-icon-minus ui-state-default ui-state-disabled\">-</span>") );
	};

	this.element.wrap( $("<div/>") );
	this.element
	    .addClass("perc_collapsible_header ui-widget ui-widget-header")
	    .prepend(toggle.append(toggle_on())).addClass("ui-helper-clearfix");
	this.element.parent()
	    .append( $("<div/>").addClass("perc_collapsible_body ui-widget ui-widget-content")
		     .append(this._getData('bodyContent')) );
	return this.element;
    },

    header : function() {
	return this.element;
    },
    body : function() {
	return this.element.next();
    },

    collapse : function () {
	this.body().hide();
    },

    uncollapse : function () {
	this.body().show();
    }
};

$.widget( "ui.perc_collapsible", Collapsible );

$.ui.perc_collapsible.getters = "header body";

})(jQuery);


