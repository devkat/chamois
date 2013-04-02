requirejs.config({
  baseUrl: '',
  paths: {
    
    // vendor
    dojo: '//ajax.googleapis.com/ajax/libs/dojo/1.8.3/dojo',
    bootstrap: '//netdna.bootstrapcdn.com/twitter-bootstrap/2.2.1/js/bootstrap.min',
    underscore: '/static/vendor/underscore-1.4.4',
    jstree: '/static/vendor/jstree-v.pre1.0/jquery.jstree',
    dgrid: '/classpath/dgrid',
    'put-selector': '/classpath/put-selector',
    xstyle: '/classpath/xstyle',
    jqform: 'http://malsup.github.com/jquery.form',
    
    // Chamois
    core: '/static/js/core',
    gui: '/static/js/gui',
  },
  shim: {
    bootstrap: ['jquery'],
    jqform: ['jquery'],
    underscore: { exports: '_' },
    jstree: { exports: 'jstree' }
  }
});
