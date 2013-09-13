var app;

Blockly.Language.if_success = {
  helpUrl: '',
  init: function() {
      
    this.setColour(180);
    this.appendDummyInput()
        .appendTitle("if-success");
      
    this.appendStatementInput('success')
        .appendTitle('success');
    this.appendStatementInput("error")
        .appendTitle("error");
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
  }
};
Blockly.Language.call_api = {
  helpUrl: '',
  init: function() {
      
    this.setColour(80);
    this.appendDummyInput()
        .appendTitle("call-api");
    this.appendValueInput('API')
        .setCheck('Array');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setInputsInline(true);
    this.setTooltip('');
  }
};

Blockly.Language.api_list = {
  helpUrl: '',
  init: function() {
    this.setColour(90);
    this.appendValueInput('API0')
        .appendTitle("API paralell call");
    this.setOutput(true, 'Array');
    this.setMutator(new Blockly.Mutator(['lists_create_with_item']));
    this.setTooltip('');
    this.itemCount_ = 1;
  },
  mutationToDom: function(workspace) {
    var container = document.createElement('mutation');
    container.setAttribute('items', this.itemCount_);
    return container;
  },
  domToMutation: function(container) {
    for (var x = 0; x < this.itemCount_; x++) {
      this.removeInput('API' + x);
    }
    this.itemCount_ = window.parseInt(container.getAttribute('items'), 10);
    for (var x = 0; x < this.itemCount_; x++) {
      var input = this.appendValueInput('API' + x);
      if (x == 0) {
        input.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
      }
    }
    if (this.itemCount_ == 0) {
      this.appendDummyInput('EMPTY')
          .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE);
    }
  },
  decompose: function(workspace) {
    var containerBlock = new Blockly.Block(workspace,
                                           'lists_create_with_container');
    containerBlock.initSvg();
    var connection = containerBlock.getInput('STACK').connection;
    for (var x = 0; x < this.itemCount_; x++) {
      var itemBlock = new Blockly.Block(workspace, 'lists_create_with_item');
      itemBlock.initSvg();
      connection.connect(itemBlock.previousConnection);
      connection = itemBlock.nextConnection;
    }
    return containerBlock;
  },
  compose: function(containerBlock) {
    // Disconnect all input blocks and remove all inputs.
    if (this.itemCount_ == 0) {
      this.removeInput('EMPTY');
    } else {
        
      for (var x = this.itemCount_ - 1; x >= 0; x--) {
          
        this.removeInput('API' + x);
      }
    }
    this.itemCount_ = 0;
    // Rebuild the block's inputs.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    while (itemBlock) {
        
      var input = this.appendValueInput('API' + this.itemCount_);
      if (this.itemCount_ == 0) {
          
        input.appendTitle(Blockly.LANG_LISTS_CREATE_WITH_INPUT_WITH);
      }
      // Reconnect any child blocks.
      if (itemBlock.valueConnection_) {
          
        input.connection.connect(itemBlock.valueConnection_);
      }
      this.itemCount_++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
    if (this.itemCount_ == 0) {
        
      this.appendDummyInput('EMPTY')
          .appendTitle(Blockly.LANG_LISTS_CREATE_EMPTY_TITLE);
    }
  },
  saveConnections: function(containerBlock) {
    // Store a pointer to any connected child blocks.
    var itemBlock = containerBlock.getInputTargetBlock('STACK');
    var x = 0;
    while (itemBlock) {
      var input = this.getInput('API' + x);
      itemBlock.valueConnection_ = input && input.connection.targetConnection;
      x++;
      itemBlock = itemBlock.nextConnection &&
          itemBlock.nextConnection.targetBlock();
    }
  }
};
Blockly.Language.lists_create_with_container = {
  // Container.
  init: function() {
    this.setColour(90);
    this.appendDummyInput()
        .appendTitle(Blockly.LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD);
    this.appendStatementInput('STACK');
    this.setTooltip('');
    this.contextMenu = false;
  }
};

Blockly.Language.lists_create_with_item = {
  // Add items.
  init: function() {
    this.setColour(90);
    this.appendDummyInput()
        .appendTitle('API');
    this.setPreviousStatement(true);
    this.setNextStatement(true);
    this.setTooltip('');
    this.contextMenu = false;
  }
};
Blockly.Language.api = {
  helpUrl: '',
  init: function() {
      
    this.setColour(0);
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldDropdown([['groups', 'groups'], ['group', 'group']]),"api");
    this.setInputsInline(true);
    this.setOutput(true, 'Array');      
    this.setTooltip('');
  }
};

Blockly.Language.redirect = {
    
  helpUrl: '',
  init: function() {
      
    this.setColour(320);
    this.appendDummyInput()
        .appendTitle("redirect");
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldTextInput(''), 'url');
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(false);
    this.setTooltip('');
  }
};

var templateDropdown;
Blockly.Language.render = {
  helpUrl: '',
  init: function() {
    this.setColour(340);
    this.appendDummyInput()
        .appendTitle("render");
    this.appendDummyInput()
        .appendTitle(templateDropdown(), 'template');
    this.setInputsInline(true);
    this.setPreviousStatement(true);
    this.setNextStatement(false);
    this.setTooltip('');
  }
};

Blockly.Language.marga = {
    
  helpUrl: '',
  init: function() {
      
    this.setColour(160);
    this.appendDummyInput().appendTitle("marga");
    this.appendDummyInput()
        .appendTitle(new Blockly.FieldDropdown([['GET', 'GET'], ['POST', 'POST']]),"method")
        .appendTitle("path")
        .appendTitle(new Blockly.FieldTextInput(''), 'path');
    this.appendStatementInput('component');
    this.setTooltip('');
  }
};

Blockly.Language.switch_ab = {
  helpUrl: '',
  init: function() {
      
    this.setColour(160);
    this.appendDummyInput().appendTitle("A/B testing");
    this.appendStatementInput('test-a')
        .appendTitle("Test A")
        .appendTitle(new Blockly.FieldTextInput('10'));
    this.appendStatementInput('test-b')
        .appendTitle("Test B")
        .appendTitle(new Blockly.FieldTextInput('90'));
    this.setPreviousStatement(true);
    this.setNextStatement(true);

    this.setTooltip('');
  }
};

CodeMirror.defineMode("mustache", function(config, parserConfig) {
  var mustacheOverlay = {
    token: function(stream, state) {
      var ch;
      if (stream.match("{{")) {
        while ((ch = stream.next()) != null)
          if (ch == "}" && stream.next() == "}") break;
        stream.eat("}");
        return "mustache";
      }
      while (stream.next() != null && !stream.match("{{", false)) {}
      return null;
    }
  };
  return CodeMirror.overlayMode(CodeMirror.getMode(config, parserConfig.backdrop || "text/html"), mustacheOverlay);
});

var Route = Backbone.Model.extend({
  urlRoot: function() {
    return 'router/' + this.get('router');
  }
});

var RouteList = Backbone.Collection.extend({
  model: Route,
  url: function() {
    return 'router/' + this.router;
  }
});

var Template = Backbone.Model.extend({
  urlRoot: function() {
    return 'template';
  }
});

var TemplateList = Backbone.Collection.extend({
  model: Template,
  url: function() {
    return 'template'; 
  }
});

var MenuView = Backbone.View.extend({
  el: $('<div id="page-menu"/>'),
  initialize: function() {
      this.render();
  },
  render: function() {
    var template = Handlebars.TemplateLoader.get('menu');
    this.$el.html(template({}));
  }
});

var TemplateListView = Backbone.View.extend({
  el: $('<div id="page-template-list"/>'),
  events: {
    "submit #form-template-new": "createTemplate",
    "click .btn-add": "newTemplate",
    "click a.btn-delete": "deleteTemplate"
  },
  initialize: function() {
    this.collection = new TemplateList();
    this.collection.on('reset',  this.render, this);
    this.collection.on('add',    this.render, this);
    this.collection.on('remove', this.render, this);
    this.collection.fetch({reset: true});
  },
  render: function() {
    var template = Handlebars.TemplateLoader.get('template/list');
    this.$el.html(template({templates: this.collection.toJSON()}));
  },
  newTemplate: function(event) {
    var template = $(Handlebars.TemplateLoader.get('template/new').call(this, {}));
    this.$('.list-templates').append(template);
    $(window).scrollTop(template.offset().top);
  },
  createTemplate: function(event) {
    var template = new Template({path: this.$("#form-template-new [name=path]").val()});
    try {
      this.collection.add(template);
      template.save();
      $("#form-template-new").parent().remove();
    } catch (e) {
      console.error(e);
    }
    return false;
  },
  deleteTemplate: function(event) {
    var id = $(event.currentTarget).data("template-id");
    this.collection.at(id).destroy();
  }
});

var TemplateEditView = Backbone.View.extend({
  el: $('<div id="page-template-edit"/>'),
  events: {
    "click .btn-save": "save",
    "click .btn-back": "back"
  },
  initialize: function() {
    this.model = new Template({
      id: this.options['path'],
      path: this.options['path']});
    this.model.on('change', this.render, this);
    this.model.fetch();
  },
  render: function() {
    var template = Handlebars.TemplateLoader.get('template/edit');
    this.$el.html(template(this.model.toJSON()));
    this.codeMirror = CodeMirror.fromTextArea(
      this.$("textarea[name=hbs]")[0],
      {
        mode: 'mustache',
        lineNumbers: true
      }
    );
  },
  save: function(e) {
    var self = this;
    this.model.save("hbs", this.codeMirror.getValue(), {
      success: function(model) {
        self.$(".label-comm-status").removeClass("label-info").addClass("label-success").text("Saved!");

        setTimeout(function() {
          self.$(".label-comm-status").removeClass("label-success").text("");
        }, 1500);
      }
    });
    this.$(".label-comm-status").addClass("label-info").text("Saving...");
  },
  back: function(e) {
    app.navigate("template", {trigger: true});
  }
});

var RouteView = Backbone.View.extend({
  el: $('<div id="page-route"/>'),
  events: {
    "change select[name=router]": "fetchRouter"
  },
  initialize: function() {
    var self = this;
    $.ajax({
      url: 'router',
      success: function(data) {
        var options = _.map(data, function(routerFile) {
          return $("<option/>").text(routerFile.replace(/.clj$/,''));
        });
        
        self.$("select[name=router]").append(options);
        if (self.options['router']) {
          self.$("select[name=router]").val(self.options['router']).trigger("change");
        }
      }
    });
    this.render();
  },
  render: function() {
    var template = Handlebars.TemplateLoader.get('route/index');
    this.$el.html(template({}));
  },
  fetchRouter: function(event) {
    var router = $(event.target).val();
    if (!_.isEmpty(router)) {
      app.navigate('#route/' + router);
      new RouteListView({router: router});
    }
  }
});
var RouteListView = Backbone.View.extend({
  el: "#list-route",
  events: {
    "submit #form-route-new": "createRoute",
    "click .btn-add": "newRoute",
    "click a.btn-delete": "deleteRoute"
  },
  initialize: function() {
    this.collection = new RouteList();
    this.collection.router = this.options['router'];
    this.collection.on('reset', this.render, this);
    this.collection.on('add', this.render, this);
    this.collection.on('remove', this.render, this);
    this.collection.fetch({reset: true});
  },
  render: function() {
    var template = Handlebars.TemplateLoader.get("route/list");
    this.$el.html(
      template({routes: this.collection.toJSON(), router: this.options['router']})
    );
  },
  newRoute: function(e) {
    var template = Handlebars.TemplateLoader.get('route/new');
    var route = $(template({}));
    this.$('.list-routes').append(route);
    $(window).scrollTop(route.offset().top);
  },
  createRoute: function(e) {
    var route = new Route({ method: this.$("#form-route-new [name=route-method]").val(),
                            path:   this.$("#form-route-new [name=route-path]").val(),
                            router: this.collection.router });
    try {
      this.collection.add(route);
      route.save();
      $("#form-route-new").parent().remove();
    } catch (e) {
      console.error(e);
    }
    return false;
  },
  deleteRoute: function(event) {
    var id = $(event.currentTarget).data("route-id");
    this.collection.at(id).destroy();
  }
});

var RouteEditView = Backbone.View.extend({
  el: $('<div id="page-route-edit"/>'),
  events: {
    "click .btn-save": "save",
    "click .btn-back": "back"
  },
  initialize: function() {
    this.model = new Route({
      id: this.options['id'],
      router: this.options['router']});
    this.model.on('change', this.render, this);
    this.availableTemplates = new TemplateList();
    this.availableTemplates.on('reset', this.fetchRouter, this);
    this.availableTemplates.fetch({reset: true});
  },
  render: function() {
    var self = this;
    var template = Handlebars.TemplateLoader.get("route/edit");
    this.$el.html(
      template({})
    );
    templateDropdown = function() {
      return new Blockly.FieldDropdown(_.map(
        self.availableTemplates.toJSON(), function(hbs) {
          return [hbs.path, hbs.path];
        }));
      };
    Blockly.inject(document.getElementById('marga-blockly'),
                   {path: './', toolbox: document.getElementById('marga-toolbox')});
    var xml = Blockly.Xml.textToDom(this.model.get('xml'));
    Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
  },
  fetchRouter: function() {
    this.model.fetch();
  },
  save: function(e) {
    var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
    this.model.set({xml: Blockly.Xml.domToText(xml)});
    this.model.save();
  },
  back: function(e) {
    app.navigate("route/" + this.model.get('router'), {trigger: true});
  }
});

var DarzanaApp = Backbone.Router.extend({
  routes: {
    "": "menu",
    "route": "routeIndex",
    "route/:router": "routeIndex",
    "route/:router/:id/edit": "routeEdit",
    "template": "templateList",
    "template/*path/edit": "templateEdit"
  },
  initialize: function() {
    this.currentView = null;
  },
  menu: function() {
    this.switchView(new MenuView());
  },
  routeIndex: function(router) {
    this.switchView(new RouteView({router: router}));
  },
  routeEdit: function(router, id) {
    this.switchView(new RouteEditView({id: id, router: router}));
  },
  templateList: function() {
    this.switchView(new TemplateListView());
  },
  templateEdit: function(path) {
    this.switchView(new TemplateEditView({path: path}));
  },
  switchView: function(newView) {
    if (this.currentView)
      this.currentView.remove();

    this.currentView = newView;
    if (!this.currentView.$el.parent().is('*')) {
      $("#content").append(this.currentView.$el);
    }
  }
});

Handlebars.TemplateLoader.config({prefix: "./hbs/"});
Handlebars.TemplateLoader.load(["menu",
                                "route/index", "route/list", "route/edit", "route/new",
                                "template/list", "template/edit", "template/new"], {
  complete: function() {
    app = new DarzanaApp();
    Backbone.history.start({pushState: false});
  }
});
