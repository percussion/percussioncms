/*
 * grunt-wait
 * https://github.com/Bartvds/grunt-wait
 *
 * Copyright (c) 2013-2018 Bart van der Schoor
 * Licensed under the MIT license.
 */

'use strict';

module.exports = function (grunt) {

  grunt.registerMultiTask('wait', 'Stop and wait.', function () {
    const options = this.options({
      delay: 0,
      before: null,
      after: null
    });

    let timerId = 0;
    const setTicker = function(delay, call){
      clearTimeout(timerId);
      timerId = setTimeout(call, delay);
    };

    const startTime = Date.now();
    const done = this.async();

    const callback = function () {
      clearTimeout(timerId);
      if(options.after) {
        const value = options.after.call(null, options);

        if (value === true) {
          grunt.log.writeln('Wait another %dms', options.delay);
          setTicker(options.delay, callback);
          return;
        }

        if (typeof value === 'number') {
          grunt.log.writeln('Wait another %dms', value);
          setTicker(value, callback);
          return;
        }

        if (typeof value === 'string' || typeof value === 'object') {
          grunt.log.warn(value);
          done(1);
          return;
        }
      }

      grunt.log.ok('Done waiting after %dms', (Date.now() - startTime));
      done();
    };

    if (options.before) {
      const value = options.before.call(null, options);

      if (value === true) {
        callback();
        return;
      }

      if (value === false) {
        grunt.log.writeln('Done waiting before starting');
        done();
        return;
      }

      if (typeof value === 'number') {
        grunt.log.writeln('Start waiting %dms', value);
        setTicker(value, callback);
        return;
      }

      if (typeof value === 'string') {
        grunt.log.fail(value);
        return;
      }
    }

    grunt.log.writeln('Start waiting %dms', options.delay);
    setTicker(options.delay, callback);
  });
};
