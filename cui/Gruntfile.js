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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

'use strict';

module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-contrib-jasmine');

    // show elapsed time at the end
    //require('time-grunt')(grunt);
    // load all grunt tasks
    //require('load-grunt-tasks')(grunt);
    // Use a similar RequireJS config as the main app
    var requireJsConfig = require('./pages/_bootstrap').requireJsConfig;
    requireJsConfig.baseUrl = './';
    requireJsConfig.paths['test'] = './test';
    
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        jasmine: {
            test: {
                src: [
                ],
                options: {
                    specs: './test/test.*.js',
                    template: require('grunt-template-jasmine-requirejs'),
                    templateOptions: {
                        requireConfig: requireJsConfig
                    }
                }
            }
        }
    });
    grunt.registerTask('default', ['jasmine']);
};
