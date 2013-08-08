"use strict";

var require = {
  baseUrl: '/js',
  waitSeconds: 60,
  paths: {
    // vendor
    bootstrap: '../vendor/bootstrap/dist/js/bootstrap.min',
    jstree: '../vendor/jstree/jquery.jstree',
    'jquery.validation': '../vendor/jquery.validation/jquery.validate',
    jquery: '../vendor/jquery/jquery.min',
    jsrender: '../vendor/jsrender/jsrender',
    moment: '../vendor/moment/min/moment.min',
    sprintf: '../vendor/sprintf/src/sprintf.min',
    underscore: '../vendor/underscore/underscore-min'
  },
  shim: {
    bootstrap: [ 'jquery' ],
    fuelux: [ 'bootstrap' ],
    'jquery.validation': [ 'jquery' ],
    jsrender: { exports: 'jsrender' },
    sprintf: { exports: 'sprintf' },
    underscore: { exports: '_' }
  }
};

