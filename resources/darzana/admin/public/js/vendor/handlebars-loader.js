;(function() {
  var TemplateLoader = function() {
    this.cache = {};
    this.prefix = "/";
    this.suffix = ".hbs";
    this.initialize.apply(this, arguments);
  };

  function basename(filename) {
    return filename.substring(filename.lastIndexOf("/") + 1);
  }
 
  _.extend(TemplateLoader.prototype, {}, {
    initialize: function() {
    },
    config: function(options) {
      if (options['prefix']) this.prefix = options['prefix'];
      if (options['suffix']) this.suffix = options['suffix'];
    },
    load: function(templateList, options) {
      var self = this;
      var unloadedTemplates = templateList.concat();
      $.each(templateList, function() {
        var templateName = this.toString();
        $.ajax({
          url: self.prefix + templateName + self.suffix,
          dataType: "text",
          success: function(templateBody) {
            self.cache[templateName] = Handlebars.compile(templateBody);
            if (basename(templateName).lastIndexOf("_", 0) == 0) {
              Handlebars.registerPartial(templateName, self.cache[templateName]);
            }
            
            unloadedTemplates = _.without(unloadedTemplates, templateName);
            if (unloadedTemplates.length == 0 && self.complete)
              self.complete.call(self);
          }
        });
      });
      if (options['complete']) {
        this.complete = options['complete'];
      }
    },
    get: function(templateName) {
      return this.cache[templateName];
    },
    merge: function(templateName, context) {
      return this.cache[templateName](context);
    }
  });
 
  Handlebars.TemplateLoader = new TemplateLoader();
})();