if (phantom.args.length != 1) {
  console.log('Expected a target URL parameter.');
  phantom.exit(1);
}

var page = require('webpage').create();
var url = phantom.args[0];
var page_opened = false;

page.onConsoleMessage = function(message) {
  console.log("App console: " + message);
};

console.log("Loading URL: " + url);

page.open(url, function(status) {
  if (page_opened) {
    return;
  }
  page_opened = true;

  if (status != "success") {
    console.log('Failed to open ' + url);
    phantom.exit(1);
  }
  console.log("Loaded successfully.");
});

