YUI().use(
  ['aui-diagram-builder', 'aui-modal', 'aui-io', 'aui-ace-editor'],
  function(Y) {

    Y.DiagramNodeAPI = Y.Component.create({
      NAME: 'diagram-node',
      ATTRS: {
        height: { value: 70 },
        type: { value: 'API' },
        width: { value: 70 }
      },
      EXTENDS: Y.DiagramNodeState,
      prototype: {
        hotPoints: Y.DiagramNode.SQUARE_POINTS,
        renderShapeBoundary: function() {
          var instance = this;
          var boundary = instance.boundary = instance.get('graphic').addShape(
            instance.get('shapeBoundary')
          );
          
          boundary.translate(8, 8);
          
          return boundary;
        },
        _valueShapeBoundary: function() {
          var instance = this;
          
          return {
            height: 55,
            type: 'rect',
            stroke: {
              weight: 7,
              color: 'transparent',
              opacity: 0
            },
            width: 55
          };
        },
        getPropertyModel: function() {
          var instance = this;
          var strings = instance.getStrings();
          return [
            {
              attributeName: "api",
              editor: new Y.TextCellEditor({
                validator: {
                  rules: {
                    value: {
                      required: true
                    }
                  }
                }
              }),
              name: "API"
            },
            {
              attributeName: 'type',
              editor: false,
              name: strings['type']
            }
          ];
        }
      }
    });
    

    Y.DiagramBuilder.types['api'] = Y.DiagramNodeAPI;

    var availableFields = [
      {
        iconClass: 'aui-diagram-node-start-icon',
        label: 'Start',
        type: 'start'
      },
      {
        iconClass: 'aui-diagram-node-task-icon',
        label: 'API call',
        type: 'api'
      },
      {
        iconClass: 'aui-diagram-node-task-icon',
        label: 'Render a template',
        type: 'render'
      },
      {
        iconClass: 'aui-diagram-node-join-icon',
        label: 'Join',
        type: 'join'
      },
      {
        iconClass: 'aui-diagram-node-fork-icon',
        label: 'Fork',
        type: 'fork'
      },
      {
        iconClass: 'aui-diagram-node-end-icon',
        label: 'End',
        type: 'end'
      }
    ];

    var diagramBuilder = new Y.DiagramBuilder (
      {
        availableFields: availableFields,
        boundingBox: '#diagram-builder-bb',
        fields: [],
        render: true,
        srcNode: '#diagram-builder-sn'
      }
    );

    diagramBuilder.connectAll(
      []
    );

    new Y.Button({
      srcNode: '#btn-save'
    }).render().on('click', function() {
      console.log(diagramBuilder.toJSON());
    });

    var modal = new Y.Modal(
      {
        centered: true,
        modal: true,
        visible: false,
        width: 800,
        zIndex: 100,
        headerContent: "edit template",
        bodyContent: "<div id=\"editor-template\"></div>",
        render: '#modal-edit-template',
        toolbars: {
          footer: [
            {
              label: 'Cancel',
              on: {
                click: function() {
                  modal.hide();
                }
              }
            },
            {
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
            }
          ]
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
  }
);