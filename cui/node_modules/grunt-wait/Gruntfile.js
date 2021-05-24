/*
 * grunt-wait
 * https://github.com/Bartvds/grunt-wait
 *
 * Copyright (c) 2013-2018 Bart van der Schoor
 * Licensed under the MIT license.
 */

'use strict';

module.exports = function (grunt) {

  grunt.initConfig({
    jshint: {
      all: [
        'Gruntfile.js',
        'tasks/*.js'
      ],
      options: {
        jshintrc: '.jshintrc'
      }
    },
    wait: {
      default: {
        options: {
        }
      },
      basic: {
        options: {
          delay: 100,
          before: function () {
            console.log('before() ');
          },
          after: function () {
            console.log('after() should be around 100ms');
          }
        }
      },
      stopBefore: {
        options: {
          delay: 100,
          before: function () {
            console.log('before() should skip after');
            return false;
          },
          after: function () {
            console.log('after() shouldnt show up');
          }
        }
      },
      finishBefore: {
        options: {
          delay: 1000,
          before: function () {
            console.log('before()');
            return true;
          },
          after: function () {
            console.log('after() should finish instantly');
          }
        }
      },
      newDelay: {
        options: {
          delay: 2000,
          before: function () {
            console.log('before()');
            return 10;
          },
          after: function () {
            console.log('after() should be around 10ms');
          }
        }
      },
      waitAnother: {
        options: {
          delay: 500,
          before: function () {
            console.log('before()');
          },
          after: function (options) {
            options._calls = (options._calls || 0) + 1;
            if (options._calls === 1) {
              return 500;
            }

            console.log('after() should be around 1000ms');
          }
        }
      },
      repeat: {
        options: {
          delay: 20,
          before: function (options) {
            console.log('before()');
            options._repeat = 3;
          },
          after: function (options) {
            console.log('after() should repeat 3 times around 20ms');
            options._repeat -= 1;
            if (options._repeat > 0) {
              return true;
            }
          }
        }
      },
      random: {
        options: {
          delay: 10,
          after : function() {
            console.log('gamble');
            return Math.random() < 0.05 ? false : true;
          }
        }
      }/*,
      errorBefore: {
        options: {
          delay: 50,
          before: function () {
            console.log('before()');
            return 'simple error';
          },
          after: function () {
            console.log('after()');
            //return 'simple error';
          }
        }
      },
      errorAfter: {
        options: {
          delay: 50,
          before: function () {
            console.log('before()');
            //return 'simple error';
          },
          after: function () {
            console.log('after()');
            return 'simple error';
          }
        }
      }*/
    }
  });

  grunt.loadTasks('tasks');
  grunt.loadNpmTasks('grunt-contrib-jshint');

  grunt.registerTask('test', ['jshint', 'wait']);

  grunt.registerTask('default', ['test']);

};
