;(function($) {
  var Debug = {
    formatJSON:function(json) {
      var out = '';
      if (json instanceof Object) {
        out += '<table>';
        for (key in json) {
          out += '<tr><th>' + key + '</th>'
            + '<td>' + this.formatJSON(json[key]) + '</td></tr>';
        }
        out += '</table>';
      } else if (json instanceof Array) {
        out += '<table>';
        for (var i=0; i < json.length; i++) {
          out += '<tr><th>' + i + '</th>'
            + '<td>' + this.formatJSON(json[i]) + '</td></tr>';
        }
        out += '</table>';
      } else {
        out += json;
      }
      return out;
    },
    collapsible: function(content, caption) {
      content.wrap('<div class="accordion"></div>').before($('<div class="accordion-switch">▶' + caption +'</div>')).hide();
    }
  };
  $(document).on('click', '.accordion .accordion-switch', function(e) {
    $(this).siblings().toggle();
  });
  window.Debug = Debug;
})(jQuery);
