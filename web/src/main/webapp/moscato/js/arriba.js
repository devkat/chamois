require([
  'jquery',
  'underscore',
  './gui/tree',
  'bootstrap',
  //'fuelux',
  './gui',
  './mercury-editor'
], function(
  $,
  _,
  tree
) {
  
  $(document).ready(function() {
    
    var navtree = $('<ul>').attr('id', 'navtree');
    /*
    $('#nav-trigger').popover({
      html: true,
      placement: 'bottom',
      content: navtree
    })
    .on('show.bs.popover', function(evt) {
      tree(navtree, { apiUrl: '/api/node', hrefUrl: '/document' });
    });
    */
    $('#nav-dropdown').on('show.bs.dropdown', function(evt) {
      tree($('.dropdown-menu', evt.target), { apiUrl: '/rest/resource', hrefUrl: '' });
    });
  });
  
  
  /*
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
   */
  
});