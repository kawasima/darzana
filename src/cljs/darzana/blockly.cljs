(ns Blockly.Language)

(def if_success
  (js-obj
    "help_url" ""
    "init"
    (fn []
      (this-as me
        (.setColor me 180)
        (-> me (.appendDummyInput ) (.appendTitle "if-success"))
        (-> me (.appendStatementInput "success") (.appendTitle "success"))
        (-> me (.appendStatementInput "error") (.appendTitle "error"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))))))

(def if_contains
  (js-obj
    "help_url" ""
    "init"
    (fn []
      (this-as me
        (.setColor me 180)
        (-> me
          (.appendDummyInput )
          (.appendTitle "if-contains")
          (.appendTitle "key")
          (.appendTitle (new js/Blockly.FieldTextInput "") "key"))
        (-> me (.appendStatementInput("contains")) (.appendTitle "Yes"))
        (-> me (.appendStatementInput("not-contains")) (.appendTitle "No"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))        
        ))))
