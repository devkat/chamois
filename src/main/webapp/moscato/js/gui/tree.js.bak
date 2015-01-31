require([
  "dojo/ready",
  "dojo/query",
  "dgrid/OnDemandGrid",
  "dgrid/tree",
  "dijit/form/Button",
  "dojo/_base/array",
  "dijit/TooltipDialog",
  "dijit/popup",
  "dijit/Menu",
  "dijit/MenuItem",
  "dijit/Dialog",
  "dijit/form/Form",
  "dijit/form/TextBox",
  "dojo/dom",
  "dojo/on",
  "dojo/dom-form",
  "dojo/store/Observable", "dojo/store/Cache", "dojo/store/Memory",
  "dojo/date/locale",
  "dojo/date/stamp"
  ], function(ready, query, OnDemandGrid, Tree, Button, array, TooltipDialog, popup,
      Menu, MenuItem, Dialog, Form, TextBox, dom, on, domForm, Observable, Cache, Memory, dateLocale, dateStamp) {
  ready(function() {
    
    var selectedId = undefined;

    var createForm = new Form({ id: "create-form"});
    var newDocumentDialog = new Dialog({
      content: createForm.domNode
    });
    
    dojo.create('label', { "for": "name", innerHTML: "Name:" }, createForm.domNode);
    dojo.place(new TextBox({ name: 'name' }).domNode, createForm.domNode, "last");
    dojo.place(new TextBox({ type: 'hidden', name: 'parent' }).domNode, createForm.domNode, "last");
    dojo.place(new TextBox({ type: 'hidden', name: 'nodeType' }).domNode, createForm.domNode, "last");
    var buttonsDiv = dojo.create('div', {className: 'buttons'}, createForm.domNode);
    new Button({
      label: 'Create',
      onClick: function() {
        var item = domForm.toObject(createForm.id);
        array.forEach(['parent', 'nodeType'], function(attr) {
          if (/^[0-9]+$/.test(item[attr])) {
            item[attr] = parseInt(item[attr]);
          }
        });
        documentStore.add(item);
        newDocumentDialog.hide();
      }
    }, dojo.create("div", {}, buttonsDiv));
    new Button({
      label: 'Cancel',
      onClick: function() {
        newDocumentDialog.hide();
      }
    }, dojo.create("div", {}, buttonsDiv));
    
    function showNodeDialog(label, nodeType) {
      newDocumentDialog.set("title", label);
      createForm.attr('value', {
        'parent': selectedId,
        'nodeType': nodeType
      });
      newDocumentDialog.show();
    }
    
    
    var menu = new Menu({});
    var newDocumentItem = new MenuItem({
      label: "New Document",
      showLabel: true,
      iconClass: "dijitEditorIcon dijitEditorIconNewPage",
      onClick: function() { showNodeDialog("New Document", 2); }
    });
    menu.addChild(newDocumentItem);
    var newFolderItem = new MenuItem({
      label: "New Folder",
      showLabel: true,
      iconClass: "dijitIcon dijitFolderClosed",
      onClick: function() { showNodeDialog("New Folder", 1); }
    });
    menu.addChild(newFolderItem);
    menu.addChild(new MenuItem({
      label: "Delete",
      showLabel: true,
      iconClass: "dijitEditorIcon dijitEditorIconDelete",
      onClick: function() { documentStore.remove(selectedId); }
    }));
    
    var tooltipDialog = new TooltipDialog({
      id: 'tooltipDialog',
      content: menu.domNode,//"<p>I have a mouse leave event handler that will close the dialog.",
      onMouseLeave: function() {
        selectedId = undefined;
        popup.close(tooltipDialog);
      }
    });
    
    var renderDate = function(object, data, td, options) {
      if (data) {
        var date = dateStamp.fromISOString(data); // + " UTC", { datePattern: "yyyy-MM-dd", timePattern: "HH:mm:ss vz" });
        td.innerHTML = dateLocale.format(date);
      }
    };

    var grid = new OnDemandGrid({
      columns: {
        col1: Tree({
          label:'Name',
          field:'name',
          renderCell: function(object, data, td, options) {
            var container = dojo.create("span", { className: 'nodeContainer' }, td);
            var link = (object.nodeType === 2) ?
              dojo.create("a", { href: "/document/view/" + object.document }, container) :
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
        }),
        col2: {
          field: "created",
          label: "Created",
          renderCell: renderDate
        },
        col3: {
          field: "modified",
          label: "Modified",
          renderCell: renderDate
        }
      },
      //store: Observable(Cache(documentStore, Memory()))
      store: Observable(documentStore)
    }, "documents");
    
    /*
    grid.on(".dgrid-row:mouseover", function(evt) {
      evt.preventDefault(); // prevent default browser context menu
      var item = grid.row(evt).data;
      popup.open({
        popup: tooltipDialog,
        around: dom.byId("node_" + item.id),
        orient: [ "after-centered" ]
      });

      //menu.targetNode = evt.target;
      //menu.show();
    }); 
       */
    
    grid.startup();
    
  });
});
