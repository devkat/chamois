define([
  'jquery',
  'underscore',
  'bootstrap'
], function(
  $,
  _
) {
  
  $(document).ready(function() {
    $('span.has-error').closest('.form-group').addClass('has-error');
    
    _.each($('.popover-trigger'), function(node) {
      $(node).popover({
        html: true,
        content: $($(node).attr('data-content-selector')).html()
      });
    });
  });
  
});