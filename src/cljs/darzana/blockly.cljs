(ns darzana.block
  (:use-macros [darzana.core :only [defblock]]))

(def apiDropdown)
(def templateDropdown)

(defblock if_success
  (js-obj
    "help_url" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 180)
        (-> me (.appendDummyInput) (.appendTitle "if-success"))
        (-> me (.appendStatementInput "success") (.appendTitle "success"))
        (-> me (.appendStatementInput "error") (.appendTitle "error"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))))))

(defblock if_contains
  (js-obj
    "help_url" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 180)
        (-> me
          (.appendDummyInput)
          (.appendTitle "if-contains")
          (.appendTitle "key")
          (.appendTitle (new js/Blockly.FieldTextInput "") "key"))
        (-> me (.appendStatementInput "contains") (.appendTitle "Yes"))
        (-> me (.appendStatementInput "not-contains") (.appendTitle "No"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))))))

(defblock call_api
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 180)
        (-> me (.appendDummyInput) (.appendTitle "call-api"))
        (-> me (.appendValueInput "API") (.setCheck "Array"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))))))

(defblock api_list
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 90)
        (-> me (.appendValueInput "API0") (.appendTitle "API paralell call"))
        (-> me (.setOutput true "Array"))
        (-> me (.setMutator (new js/Blockly.Mutator (array "lists_create_with_item"))))
        (set! (.-itemCount_ me) 1)))
    
    "mutationToDom"
    (fn [workspace]
      (this-as me
        (let [container (.createElement js/document "mutation")]
          (.setAttribute container "items" (.-itemCount_ me))
          container)))
    
    "domToMutation"
    (fn [container]
      (this-as me
        (for [x (range 0 (dec (.-itemCount_ me)))]
          (.removeInput me (str "API" x)))
        (set! (.-itemCount_ me)
          (js/parseInt (.getAttribute container "items") 10))
        (for [x (range 0 (dec (.-itemCount_ me)))]
          (let [input (.appendValueInput me (str "API" x))]
            (if (= x 0)
              (.appendTitle input
                (aget js/Blockly "LANG_LISTS_CREATE_WITH_INPUT_WITH")))))
        (if (= (.-itemCount_ me) 0)
          (-> me
            (.appendDummyInput "EMPTY")
            (.appendTitle (aget js/Blockly "LANG_LISTS_CREATE_EMPTY_TITLE"))))))

    "decompose"
    (fn [workspace]
      (this-as me
        (let [containerBlock
               (new js/Blockly.Block workspace "lists_create_with_container")]
          (.initSvg containerBlock)
          (loop [ connection (-> containerBlock
                              (.getInput "STACK")
                              (.-connection))
                  x 0]
            (when (< x (.-itemCount_ me))
              (let [itemBlock (new js/Blockly.Block workspace "lists_create_with_item")]
                (.initSvg itemBlock)
                (.connect connection (.-previousConnection itemBlock))
                (recur (.-nextConnection itemBlock) (inc x)))))
          containerBlock)))

    "compose"
    (fn [containerBlock]
      (this-as me
        (if (= (.-itemCount_ me) 0)
          (.removeInput me "EMPTY")
          (for [x (range (dec (.itemCount_ me)) 0)]
            (.removeInput me (str "API" x))))
        (set! (.-itemCount_ me) 0)
        (loop [itemBlock (.getInputTargetBlock containerBlock "STACK")]
          (when itemBlock
            (let [input (.appendValueInput me (str "API" (.-itemCount_ me)))]
              (if (= (.-itemCount_ me) 0)
                (.appendTitle input (aget js/Blockly "LANG_LIST_CREATE_WITH_INPUT_WITH")))
              (if (.-valueConnection_ itemBlock)
                (-> input (.-connection) (.connect (.valueConnection_ itemBlock))))
              (set! (.-itemCount_ me) (inc (.itemCount_ me)))
              (recur (and (.-nextConnection itemBlock)
                       (-> itemBlock (.-nextConnection) (.targetBlock)))))))
        (if (= (.-itemCount_ me) 0)
          (-> me
            (.appendDummyInput "EMPTY")
            (.appendTitle (aget js/Blockly "LANG_LISTS_CREATE_EMPTY_TITLE"))))))
    
    "saveConnections"
    (fn [containerBlock]
      (this-as me
        (loop [ itemBlock (.getInputTargetBlock containerBlock "STACK")
                x 0]
          (when itemBlock
            (set! (.-valueConnection_ itemBlock)
              (when-let [input (.getInput me (str "API" x))]
                (.. input -connection -targetConnection)))
            (recur (and (.-nextConnection itemBlock)
                     (.. itemBlock -nextConnection targetBlock))
              (inc x))))))))

(defblock lists_create_with_container
  (js-obj
    "init"
    (fn []
      (this-as me
        (.setColour me 90)
        (-> me
          (.appendDummyInput)
          (.appendTitle (aget js/Blockly "LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD")))
        (.appendStatementInput me "STACK")
        (set! (.-contextMenu me) false)))))

(defblock lists_create_with_item
  (js-obj
    "init"
    (fn []
      (this-as me
        (.setColour me 90)
        (-> me
          (.appendDummyInput)
          (.appendTitle "API"))
        (.setPreviousStatement me true)
        (.setNextStatement me true)
        (set! (.-contextMenu me) false)))))

(defblock api
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 0)
        (-> me
          (.appendDummyInput)
          (.appendTitle (apiDropdown) "api"))
        (.setInputsInline me true)
        (.setOutput me true "Array")))))

(defblock redirect
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 320)
        (-> me (.appendDummyInput) (.appendTitle "redirect"))
        (-> me
          (.appendDummyInput)
          (.appendTitle (new js/Blockly.FieldTextInput "") "url"))
        (.setInputsInline me true)
        (.setPreviousStatement me true)
        (.setNextStatement me false)))))

(defblock render
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 340)
        (-> me (.appendDummyInput) (.appendTitle "render"))
        (-> me
          (.appendDummyInput)
          (.appendTitle (templateDropdown) "template"))
        (.setInputsInline me true)
        (.setPreviousStatement me true)
        (.setNextStatement me false)))))

(defblock store_session
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 180)
        (-> me (.appendDummyInput) (.appendTitle "store-session"))
        (-> me
          (.appendDummyInput)
          (.appendTitle "Session key")
          (.appendTitle (new js/Blockly.FieldTextInput "") "session-key"))
        (-> me
          (.appendDummyInput)
          (.appendTitle "Context key")
          (.appendTitle (new js/Blockly.FieldTextInput "") "context-key"))
        (.setPreviousStatement me true)
        (.setNextStatement me true)))))

(defblock marga
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 160)
        (-> me (.appendDummyInput) (.appendTitle "marga"))
        (-> me
          (.appendDummyInput)
          (.appendTitle
            (new js/Blockly.FieldDropdown
              (array (array "GET" "GET") (array "POST" "POST")))
            "method")
          (.appendTitle "path")
          (.appendTitle (new js/Blockly.FieldTextInput "") "path"))
        (.appendStatementInput me "component")))))

(defblock ab_testing_participate
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (.setColour me 160)
        (-> me (.appendDummyInput) (.appendTitle "A/B testing"))
        (-> me
          (.appendDummyInput)
          (.appendTitle "Test ID")
          (.appendTitle (new js/Blockly.FieldTextInput "") "test-id"))
        (-> me
          (.appendStatementInput "test-a")
          (.appendTitle "A")
          (.appendTitle (new js/Blockly.FieldTextInput "10") "probability-a"))
        (-> me
          (.appendStatementInput "test-b")
          (.appendTitle "B")
          (.appendTitle (new js/Blockly.FieldTextInput "90") "probability-b"))
        (.setPreviousStatement me true)
        (.setNextStatement me true)
        ))))

(goog/exportProperty js/Blockly "Language" js/Blockly.Language)
