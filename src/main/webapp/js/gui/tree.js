define([
  'jquery',
  'underscore',
  'jsrender'
], function(
  $,
  _
) {
  
  var nodeTemplate = $('#tree-node-template');
  
  return function(parent, settings) {
    
    var
      apiUrl = settings.apiUrl,
      hrefUrl = settings.hrefUrl,
      currentPath = window.location.pathname.substring(hrefUrl.length);
    
    function loadChildren(container, path) {
      
      function renderNode(node) {
        var
          nodePath = path + '/' + node.slug,
          elem = $(nodeTemplate.render(_.extend({
            id: _.uniqueId(),
            href: hrefUrl + nodePath,
            current: currentPath === nodePath
          }, node)).trim()),
          triggerElem = $('> .collapse-trigger', elem),
          childrenElem = $('> ul', elem);
        
        triggerElem.on('click', function(evt) {
          childrenElem.collapse('toggle');
          evt.stopPropagation();
        });

        childrenElem.on('show.bs.collapse', function(evt) {
          evt.stopPropagation();
          triggerElem.removeClass('icon-folder-close').addClass('icon-folder-open');
          if ($('> *', childrenElem).length === 0) {
            loadChildren(childrenElem, nodePath);
          }
        });
        childrenElem.on('hide.bs.collapse', function(evt) {
          evt.stopPropagation();
          triggerElem.removeClass('icon-folder-open').addClass('icon-folder-close');
        });
        if (currentPath.match(new RegExp('^' + nodePath))) {
          childrenElem.collapse({toggle: true});
        }
        container.append(elem);
      }

      var loader = $('<span class="icon icon-rotate icon-refresh"></span>');
      container.append(loader);
      $.ajax({
        url: apiUrl + path,
        dataType: 'json',
        success: function(data) {
          loader.remove();
          _.each(data, renderNode);
        },
        error: function(error) {
          debugger;
        }
      });
    }
    
    parent.addClass('tree');
    loadChildren(parent, '', settings);
  };

});