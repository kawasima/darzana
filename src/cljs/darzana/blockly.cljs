(ns darzana.block
  (:use [darzana.i18n :only [t]])
  (:require
    [Blockly :as Blockly])
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
        (-> me (.appendDummyInput) (.appendTitle (t :labels/if-success)))
        (-> me (.appendStatementInput "success") (.appendTitle (t :labels/success)))
        (-> me (.appendStatementInput "error") (.appendTitle (t :labels/error)))
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
        (-> me (.appendDummyInput) (.appendTitle (t :labels/call-api)))
        (-> me (.appendValueInput "API") (.setCheck "Array"))
        (-> me (.setPreviousStatement true))
        (-> me (.setNextStatement true))
        (.. me (setInputsInline true))))))

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
        (aset me "itemCount_" 1)))
    
    "mutationToDom"
    (fn [workspace]
      (this-as me
        (let [container (.createElement js/document "mutation")]
          (.setAttribute container "items" (aget me "itemCount_"))
          container)))
    
    "domToMutation"
    (fn [container]
      (this-as me
        (for [x (range 0 (dec (aget me "itemCount_")))]
          (.removeInput me (str "API" x)))
        (aset me "itemCount"
          (js/parseInt (.getAttribute container "items") 10))
        (for [x (range 0 (dec (aget me "itemCount_")))]
          (let [input (.appendValueInput me (str "API" x))]
            (if (= x 0)
              (.appendTitle input
                (aget js/Blockly "LANG_LISTS_CREATE_WITH_INPUT_WITH")))))
        (if (= (aget me "itemCount_") 0)
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
                              (aget "connection"))
                  x 0]
            (when (< x (.-itemCount_ me))
              (let [itemBlock (new js/Blockly.Block workspace "lists_create_with_item")]
                (.initSvg itemBlock)
                (.connect connection (aget itemBlock "previousConnection"))
                (recur (aget itemBlock "nextConnection") (inc x)))))
          containerBlock)))

    "compose"
    (fn [containerBlock]
      (this-as me
        (if (= (aget me "itemCount_") 0)
          (.removeInput me "EMPTY")
          (for [x (range (dec (aget me "itemCount_")) 0)]
            (.removeInput me (str "API" x))))
        (aset me "itemCount_" 0)
        (loop [itemBlock (.getInputTargetBlock containerBlock "STACK")]
          (when itemBlock
            (let [input (.appendValueInput me (str "API" (aget me "itemCount_")))]
              (if (= (aget me "itemCount_") 0)
                (.appendTitle input (aget js/Blockly "LANG_LIST_CREATE_WITH_INPUT_WITH")))
              (if (aget itemBlock "valueConnection_")
                (-> input (aget "connection") (.connect (aget itemBlock "valueConnection_"))))
              (aset me "itemCount_" (inc (aget me "itemCount_")))
              (recur (and (aget itemBlock "nextConnection")
                       (-> itemBlock (aget "nextConnection") (.targetBlock)))))))
        (if (= (aget me "itemCount_") 0)
          (-> me
            (.appendDummyInput "EMPTY")
            (.appendTitle (aget js/Blockly "LANG_LISTS_CREATE_EMPTY_TITLE"))))))
    
    "saveConnections"
    (fn [containerBlock]
      (this-as me
        (loop [ itemBlock (.getInputTargetBlock containerBlock "STACK")
                x 0]
          (when itemBlock
            (aset itemBlock "valueConnection_"
              (when-let [input (.getInput me (str "API" x))]
                (-> input (aget "connection") (aget "targetConnection"))))
            (recur (and (aget itemBlock "nextConnection")
                     (-> itemBlock (aget "nextConnection") (.taregtBlock)))
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
        (aset me "contextMenu" false)))))

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
        (aset me "contextMenu" false)))))

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
        (-> me (.appendDummyInput) (.appendTitle (t :labels/redirect)))
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
        (-> me (.appendDummyInput) (.appendTitle (t :labels/render)))
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
        (-> me (.appendDummyInput) (.appendTitle (t :labels/marga)))
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
  (clj->js
    { :helpUrl ""
      :init
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
            (.appendTitle (new js/Blockly.FieldTextInput "10") "probability-a"))
          (-> me
            (.setMutator
              (Blockly.Mutator. ['ab-testing-alternative'])))
          (-> me
            (.appendStatementInput "test-b")
            (.appendTitle "B")
            (.appendTitle (new js/Blockly.FieldTextInput "90") "probability-b"))
          (.setPreviousStatement me true)
          (.setNextStatement me true)
          (set! (.-altCount_ me) 0)
          ))
      :mutationToDom
      (fn []
        (this-as me
          (let [container (.createElement js/document "mutation")]
            (. container setAttribute "alternative" (.-altCount_ me))
            container)))

      :domToMutation
      (fn [xmlElement]
        (this-as me
          (set! (.-altCount_ me) (js/parseInt (. xmlElement getAttribute "alternative" 10)))
          (doseq [x (range 1 (.-altCount_ me))]
            (-> me
              (.appendStatementInput (str "ALT" x))))
          ))

      :decompose
      (fn [workspace]
        (this-as me
          (let [containerBlock (Blockly.Block. workspace "ab_testing_alternative_container")]
            (. containerBlock initSvg)
            (loop [ x 1
                    connection (.. containerBlock (getInput "STACK") -connection)]
              (when (<= x (.-altCount_ me))
                (let [altBlock (Blockly.Block. workspace "ab_testing_alternative")]
                  (. altBlock initSvg)
                  (. connection connect (. altBlock -previousConnection))
                  (recur (inc x) (. altBlock -nextConnection)))))
            containerBlock)))

      :compose
      (fn [containerBlock]
        (this-as me
          (loop [clauseBlock (. containerBlock getInputTargetBlock "STACK")]
            (when-not (nil? clauseBlock)
              (set! (.-altCount_ me) (inc (.-altCount_ me)))
              (let [altInput (. me appendStatementInput "ALT")]
                (when (. clauseBlock -statementConnection_)
                  (.. altInput -connection (connect (.-statementConnection_ clauseBlock)))))
              (recur (.. clauseBlock -nextConnection targetBlock)))
            )))
      :saveConnections
      (fn [containerBlock])
      }))

(defblock ab_testing_alternative_container
  (clj->js
    { :init
      (fn []
        (this-as me
          (.  me setColour 210)
          (.. me appendDummyInput (appendTitle ""))
          (.  me appendStatementInput "STACK")
          (set! (.-contextMenu me) false)
          ))}))

(defblock ab_testing_alternative
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (.. me (setColour 210))
          (-> me
            (.appendDummyInput)
            (.appendTitle (t :labels/alternative)))))}
    ))

(goog/exportProperty js/Blockly "Language" js/Blockly.Language)
