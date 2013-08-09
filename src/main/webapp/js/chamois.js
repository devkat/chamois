require([
  'jquery',
  'underscore',
  'jstree',
  'bootstrap',
  './gui',
  './mercury-editor'
], function(
  $,
  _
) {
  
  $.jstree._themes = '/vendor/jstree/themes/';
  
  $(document).ready(function() {
    
    $('#nodes-tree').jstree({
      themes: {
        theme: 'default',
        dots: false
      },
      json_data: {
        ajax: {
          url: '/api/node'
        }
      },
      plugins: [ "themes", "json_data", "ui" ]
    });
    
  });
  
});