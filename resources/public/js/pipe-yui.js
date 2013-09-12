YUI().use(['aui-sortable-list', 'aui-button', 'aui-modal', 'aui-diagram-builder', 'aui-io', 'aui-ace-editor',
           'app-base', 'handlebars'],function(Y) {
    var accumulator = (function () {
      var n = 1;
      return function() {
        return n++;
      };
    })();
    
    function CommandParser(cfg) {
      CommandParser.superclass.constructor.apply(this, arguments);
    }

    CommandParser.register = function(commandClass) {
      if (!this.commands) this.commands = {};
      this.commands[commandClass.NAME] = commandClass;
    };
        CommandParser.ATTRS = {
          x: { value: 10 },
          y: { value: 10},
          fields: { value: [] },
          connectors: { value: [] }
        };
    Y.extend(CommandParser, Y.Base, {
      parse: function(sexp, prevCommand, connectorName) {
        while(sexp.length > 0) {
          var commandExp = sexp.shift();
          var commandName = commandExp.shift();
          var commandClass = CommandParser.commands[commandName];
          var command = new commandClass();
          command.setAttrs({
            xy: [this.get('x'), this.get('y')],
            parser: this
          });
          this.set('x', this.get('x') + 90);
          this.set('y', this.get('y') + 90);

          command.parse(commandExp);

          this.get('fields').push(command);
          if (prevCommand != null) {
            console.log(command.get('name'));
            this.get('connectors').push({
              connector: {
                name: connectorName ? connectorName : 'connector-' + accumulator()
              },
              source: prevCommand.get('name'),
              target: command.get('name')
            });
          }
          prevCommand = command;
        }
      },
      _stringify: function(node, nodeMap) {
        var commandClass = CommandParser.commands[node.type];
        var command = new commandClass();
        command.setAttrs(node);
        
      },
      stringify: function(obj) {
        var nodeMap = {};
        var targets = {};
        Y.Array.each(obj.nodes, function(node) {
          nodeMap[node.name] = node;
          Y.Array.each(node.transitions, function(transition) {
            targets[transition.target] = true;
          });
        });
        var targetKeys = Y.Object.keys(targets);
        var entryPoints = Y.Array.filter(Y.Object.keys(nodeMap), function(name) {
          return Y.Array.indexOf(targetKeys, name) < 0;
        });
        if (entryPoints.length != 1)
          throw "wrong flow!";
        nodeMap[entryPoints[0]];
        return entryPoint;
      }
    });

    function Command(cfg) {
      Command.superclass.constructor.apply(this, arguments);
    }
    Command.ATTRS = {
      name: {},
      type: { readOnly: true },
      xy: {},
      parser: { readOnly: true }
    };

    Y.extend(Command, Y.Base, {
    });

    
    function CallApiCommand(cfg) {
      CallApiCommand.superclass.constructor.apply(this, arguments);
    }
    CallApiCommand.ATTRS = {
      type: { value: 'call-api' },
      name: { value: 'call-api-' + accumulator() },
      apis: { value: [] }
    };
    CallApiCommand.SERIALIZABLE_ATTRS = ["apis"];
    CallApiCommand.NAME = 'call-api';
    Y.extend(CallApiCommand, Command, {
      parse: function(args) {
        this.set('apis', args.join(','));
      }
    });
    CommandParser.register(CallApiCommand);

    function IfSuccessCommand(cfg) {
      IfSuccessCommand.superclass.constructor.apply(this, arguments);
    }
    IfSuccessCommand.ATTRS = {
      type: { value: 'if-success' },
      name: { value: 'if-success-' + accumulator() }
    };
    IfSuccessCommand.NAME = 'if-success';
    Y.extend(IfSuccessCommand, Command, {
      parse: function(args) {
        this.get('parser').parse([args[0]], this, "success");
        this.get('parser').parse([args[1]], this, "error");
      }
    });
    CommandParser.register(IfSuccessCommand);

    
    function RenderCommand(cfg) {
      RenderCommand.superclass.constructor.apply(this, arguments);
    }
    RenderCommand.ATTRS = {
      type: { value: 'render' },
      name: { value: 'render-' + accumulator() },
      template: { }
    };
    RenderCommand.SERIALIZABLE_ATTRS = ["template"];
    RenderCommand.NAME = 'render';
    Y.extend(RenderCommand, Command, {
      parse: function(args) {
        this.set('template', args[0]);
      }
    });
    CommandParser.register(RenderCommand);

    function RedirectCommand(cfg) {
      RedirectCommand.superclass.constructor.apply(this, arguments);
    }
    RedirectCommand.ATTRS = {
      type: { value: 'redirect' },
      name: { value: 'redirect-' + accumulator() },
      url: { }
    };
    RedirectCommand.SERIALIZABLE_ATTRS = ["url"];
    RedirectCommand.NAME = 'redirect';
    Y.extend(RedirectCommand, Command, {
      parse: function(args) {
        this.set('url', args[0]);
      }
    });
    CommandParser.register(RedirectCommand);    

    Y.Object.each(CommandParser.commands, function(commandClass, name) {
      var camelName = Y.Array.map(name.split(/\-/), function(s){ return s[0].toUpperCase() + s.substring(1); }).join("");
      var attrs = {
        type: { value: name }
      };
      Y.Array.each(Y.Object.keys(commandClass.ATTRS), function(key) {
        if (Y.Array.indexOf(['name', 'type', 'parser'], key) < 0) {
          attrs[key] = { validator: Y.Lang.isString };
        }
      });


      Y["DiagramNode" + camelName] = Y.Component.create({
        NAME: 'diagram-node',
        ATTRS: attrs,
        EXTENDS: Y.DiagramNodeTask,
            prototype: {
              SERIALIZABLE_ATTRS: Y.DiagramNodeTask.prototype.SERIALIZABLE_ATTRS.concat(commandClass.SERIALIZABLE_ATTRS || []),
              getPropertyModel: function () {
                var instance = this;
                
                var model = Y.DiagramNodeTask.superclass.getPropertyModel.apply(instance, arguments);
                
                Y.Array.each(Y.Object.keys(commandClass.ATTRS), function(key) {
                  if (key == 'name' || key == 'type')
                    return;

                  model.push({
                    attributeName: key,
                    name: key
                  });
                });
                
                return model;
              },
              _renderLabel: function() {
                var instance = this;
                instance.labelNode = Y.Node.create(instance.get('description'));
                instance.get('contentBox').placeAfter(instance.labelNode);
              }
            }
      });
      Y.DiagramBuilder.types[name] = Y["DiagramNode" + camelName];
    });

    var availableFields = [{
      iconClass: 'diagram-node-call-api-icon',
      label: 'call-api',
      type: 'call-api'
    }, {
      iconClass: 'diagram-node-render-icon',
      label: 'render',
      type: 'render'
    }, {
      iconClass: 'diagram-node-if-success-icon',
      label: 'if-success',
          type: 'if-success'
    }, {
      iconClass: 'diagram-node-redirect-icon',
      label: 'redirect',
      type: 'redirect'
    }];

  Y.RouteListView = Y.Base.create('routeList', Y.View, [], {
    template: Y.Handlebars.compile(Y.one('#route-list-template').getHTML()),
    events: {
      '.btn-add-route': {
        click: 'add' 
      }
    },
    render: function() {
      var content = this.template({});
      this.get('container').setHTML(content);
      var placeholder = Y.Node.create('<li class="placeholder"></li>');

      this.routes = new Y.SortableList({
        dropCondition: function(event) {
          return true;
        },
        dropOn: 'list-route',
        nodes: '#list-route li',
        placeholder: placeholder
      });
    },
    add: function() {
      var self = this;
      var template = Y.Handlebars.compile(Y.one('#dialog-route-template').getHTML());
      var modal = new Y.Modal({
        bodyContent: template(),
        modal:true,
        draggable: true
      }).render();
      modal.addToolbar([
        { label: 'OK',
          on: {
            click: function() {
              var method = Y.one("[name=method]", modal).val();
              var path   = Y.one("[name=path]",   modal).val();
              self.addRoute(method, path);
              modal.hide();
            }
          }
        }
      ]);
    },
    addRoute: function(method, path) {
      var tmpl = Y.Handlebars.compile(Y.one('#route-list-item-template').getHTML());
      var newRoute = Y.Node.create(tmpl({method: method, path: path}));
      Y.one("#list-route").append(newRoute);
      this.routes.add(newRoute);
    }
  });

  Y.RouteEditView = Y.Base.create('routeEdit', Y.View, [], {
    template: Y.Handlebars.compile(Y.one('#route-edit-template').getHTML()),
    events: {
      '.btn-save': {
          click: 'save'
      }
    },
    render: function() {
      var content = this.template({});
      var container = this.get('container').setHTML(content);
      this.diagram = new Y.DiagramBuilder({
        availableFields: availableFields,
        boundingBox: container.one('#diagram-builder-bb'),
        contentBox:  container.one('#diagram-builder-sn')
      });
      return this;
    },
    renderDiagram: function() {
      this.commandParser = new CommandParser();
      var routeJson = ["POST", "/groups", ["call-api", ["groups-post"]], ["if-success", ["redirect", "/groups"], ["render", "edit.hbs"]]];

      this.commandParser.parse(routeJson.splice(2));

      this.diagram.render();
      Y.Array.each(this.commandParser.get('fields'), function(f) {
        this.diagram.addField(f.getAttrs());
      }, this);
      this.diagram.connectAll(this.commandParser.get('connectors'));
    },
    save: function() {
      console.log(this.commandParser.stringify(this.diagram.toJSON()));
    }
  });
             
  Y.TemplateEditView = Y.Base.create('templateEdit', Y.View, [], {
    render: function() {
      
    }
  });
             
  var app = new Y.App({
    container: "#darzana",
    viewContainer: "#darzana",
    transitions: true,
    views: {
      routeList: { type: Y.RouteListView, preserve: true },
      routeEdit: { type: Y.RouteEditView },
      templateEdit: {type: Y.TemplateEditView}
    }});
  app.route('/router', function () {
    app.showView('routeList');
  });
  app.route('/router/edit', function () {
    app.showView('routeEdit', {}, {
      callback: function(view) {
        view.renderDiagram();
      }
    });
  });
  app.route('/template/edit', function () {
    app.showView('templateEdit');
  });

  /*
  app.route('/router/:name', function (req) {
    var name = req.params.name;
    this.showView('home', {name: name});
  });
  */
  app.render();
  app.navigate('/router');
});

/*
    new Y.Button({
      srcNode: '#btn-save'
    }).render().on('click', function() {
      var nodes = diagram.toJSON();
      var json = commandParser.stringify(nodes);
      console.log(JSON.stringify(json));
    });

    var modal = new Y.Modal({
      centered: true,
      modal: true,
      visible: false,
      width: 800,
      zIndex: 100,
      headerContent: "edit template",
          bodyContent: "<div id=\"editor-template\"></div>",
      render: '#modal-edit-template',
      toolbars: {
        footer: [{
          label: 'Cancel',
          on: {
            click: function() {
              modal.hide();
            }
          }
        }, {
          label: 'Save',
          on: {
            click: function() {
              Y.io.request("/template/large_area", {
                method: 'post',
                data: {source: aceEditor.getEditor().getValue()},
                on: {
                  success: function() {
                    modal.hide();
                  }
                }
              });
            }
          }
        }]
      }
    });
    
    var aceEditor = new Y.AceEditor(
      {
        mode: 'html',
        width: 700,
        boundingBox: '#editor-template'
      });

    Y.one('#btn-edit-template').on(
      'click',
      function() {
        Y.io.request("/template/large_area", {
          method: 'get',
          on: {
            success: function() {
              aceEditor.getEditor().setValue(this.get('responseData'));
              aceEditor.render();
              modal.show();
            }
          }
        });
      });

    Y.io.request("/router/hotpepper", {
      method: 'get',
      dataType: 'json',
      on: {
        success: function() {
          var data = this.get('responseData');
          console.log(data.length);
        }
      }
    });
  });
*/