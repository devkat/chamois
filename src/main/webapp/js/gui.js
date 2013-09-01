define([
  'jquery',
  'underscore',
  'bootstrap'
], function(
  $,
  _
) {
  
  $(document).ready(function() {
    /*
    $('span.has-error').closest('.form-group').addClass('has-error');
    
    $('label.label').removeClass('label');
    $('span > input[type=radio]').parent().wrap($('<div>').addClass('radio')).wrap($('<label>'));
    */
    
    $('.drawer-trigger').click(function(e) {
      $($(e.target).attr('data-target')).toggleClass('in');
    });
    
    
    _.each($('.popover-trigger'), function(node) {
      $(node).popover({
        html: true,
        content: $($(node).attr('data-content-selector')).html()
      });
    });
  });
  
});