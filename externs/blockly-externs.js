var Blockly = {};

Blockly.inject = function(dom, options){};
Blockly.Xml = {
  workspaceToDom: function(){},
  domToWorkspace: function(){},
  textToDom: function(){},
  domToText: function(){}
};

Blockly.FieldDropdown = function(){};
Blockly.FieldTextInput = function(){};
Blockly.Mutator = function(){};
Blockly.Block = function(){};
Blockly.Block.prototype.initSvg = function(){};
Blockly.Block.prototype.removeInput = function(){};
Blockly.Block.prototype.getInputTargetBlock = function(){};

Blockly.Language = {};
Blockly.Language.prototype = {
  appendDummyInput: function(){},
  appendTitle: function(){},
  appendValueInput: function(){},
  appendStatementInput: function(){},
  setColour: function(){},
  setOutput: function(){},
  setCheck: function() {},
  setMutator: function() {},
  setInputsInline: function() {},
  setPreviousStatement: function() {},
  setNextStatement:function(){}
};
