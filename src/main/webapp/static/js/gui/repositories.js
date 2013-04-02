require([
  'require',
  'jquery',
  'bootstrap',
  'underscore',
  'dgrid/OnDemandGrid',
  'dgrid/tree',
  "dojo/date/locale",
  'dojo/date/stamp',
  "dojo/store/Observable",
  'core/repositoryStore'
], function(
  require,
  $,
  bootstrap,
  _,
  OnDemandGrid,
  Tree,
  dateLocale,
  dateStamp,
  Observable,
  repositoryStore
) {
  var repoStore = repositoryStore();
  
  function renderDate(object, data, td, options) {
    if (data) {
      var date = dateStamp.fromISOString(data); // + " UTC", { datePattern: "yyyy-MM-dd", timePattern: "HH:mm:ss vz" });
      td.innerHTML = date;//dateLocale.format(date);
    }
  };

  $(function() {
    
    var grid = new OnDemandGrid({
      columns: {
        col1: {
          label: 'Name',
          field: 'name',
          renderCell: function(object, data, td, options) {
            $(td).append($('<a>').attr("href", "/repository/" + object.idField).html(data));
            return;
            
            
            var container = dojo.create("span", { className: 'nodeContainer' }, td);
            var link = (object.nodeType === 2) ?
              dojo.create("a", { href: "/repository/view/" + object.id }, container) :
              dojo.create("span", {}, container);
            link.id = "node_" + object.id;
            
            var iconClass = "";
            if (object.nodeType === 1) {
              on(link, 'click', function(evt) {
                grid.expand(grid.row(evt));
              });
              /*
              var expandoIcon = query(".dgrid-expando-icon", td)[0];
              var expanded = dojo.hasClass(expandoIcon, 'ui-icon-triangle-1-e');
              var suffix = expanded ? "Open" : "Closed";
              */
              var suffix = "Closed";
              iconClass = "bedocsIcon bedocsIconFolder" + suffix;
            }
            else {
              iconClass = "bedocsIcon bedocsIconDocument";
            }
            dojo.create("img", {
              className: iconClass,
              src: "/classpath/dojo/resources/blank.gif"
            }, link);
            link.appendChild(dojo.doc.createTextNode(data));
            
            /*
            var actionsTrigger = dojo.create("span", { innerHTML: 'Actions', className: 'nodeActions' }, container);
            */
            
            on(link, "mouseover", function(evt) {
              evt.preventDefault();
              selectedId = object.id;
              array.forEach([newDocumentItem, newFolderItem], function(item) {
                dojo.style(item.domNode, 'display', object.nodeType === 1 ? '' : 'none');
              });
              popup.open({
                popup: tooltipDialog,
                around: link,
                orient: [ "after-centered" ]
              });
            });

            return link;
          }
        },
        col2: {
          field: "created",
          label: "Created",
          renderCell: renderDate
        }
      },
      //store: Observable(Cache(documentStore, Memory()))
      store: Observable(repoStore)
    }, "repositories");
    
    grid.startup();
    
    (function(dialog) {
      $('#dialog-cancel').click(function() {
        dialog.modal('hide');
      });
      
      $('#dialog-submit').click(function(evt) {
        var data = _.reduce($('form', dialog).serializeArray(), function(item, pair) {
          item[pair.name] = pair.value;
          return item;
        }, {});
        repoStore.put(data);
        dialog.modal('hide');
      });
    })($('#dialog-new-repository'));
    
  });
  
});
