'use strict';

module.exports = function(grunt) {
    grunt.loadNpmTasks('grunt-contrib-jasmine');
    grunt.loadNpmTasks('grunt-casper');

    // show elapsed time at the end
    require('time-grunt')(grunt);
    // load all grunt tasks
    require('load-grunt-tasks')(grunt);

    // Use a similar RequireJS config as the main app
    var requireJsConfig = require('./public/pages/_bootstrap').requireJsConfig;
    requireJsConfig.baseUrl = './public/';
    requireJsConfig.paths['test'] = '../test';
    
    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        develop: {
            server: {
                file: 'app.js'
            }
        },
        wait: {
            options: {
                delay: 5000
            }
        },
        jasmine: {
            test: {
                options: {
                    specs: 'test/unit/spec.*.js',
                    template: require('grunt-template-jasmine-requirejs'),
                    templateOptions: {
                        requireConfig: requireJsConfig
                    }
                }
            }
        },
        casper: {
            options: {
                test: true,
                'log-level': 'info',
                parallel: false
            },
            all: {
                src: ['test/integration/spec.*.js'],
                dest: function(input) {
                    var fullname = input.toString();
                    var idx = fullname.lastIndexOf('/');
                    var filename = fullname.substring(idx + 1).replace(/\.js$/,'.xml');
                    return 'reports/integration/' + filename;
                },
                options: { }
            }
        }
    });
    
    grunt.registerTask('default', ['unit-tests', 'integration-tests']);
    grunt.registerTask('unit-tests', ['jasmine']);
    grunt.registerTask('integration-tests', ['develop', 'wait', 'casper']);
};
