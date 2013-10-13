(ns darzana.block
  (:use [darzana.i18n :only [t]])
  (:require
    [Blockly :as Blockly])
  (:use-macros [darzana.core :only [defblock]]))

(declare ^:dynamic apiDropdown)
(declare ^:dynamic templateDropdown)

(defblock key_assign
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 130)
          (. me setInputsInline true)
          (.. me (appendValueInput "FROM") (appendTitle "assign"))
          (.. me (appendValueInput "TO")   (appendTitle "=>"))
          (. me setOutput true)))
      }))

(defblock key_keyword
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 130)
          (. me setInputsInline true)
          (.. me appendDummyInput
            (appendTitle ":")
            (appendTitle (new Blockly.FieldTextInput "") "KEYWORD"))
          (. me setOutput true "KEY")))
      }))

(defblock key_literal
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 130)
          (. me setInputsInline true)
          (.. me appendDummyInput
            (appendTitle "'")
            (appendTitle (new Blockly.FieldTextInput "") "STRING")
            (appendTitle "'"))
          (. me setOutput true "KEY")))
      }))

(defblock key_composite
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 130)
          (. me setInputsInline true)
          (.. me (appendValueInput "KEY0") (appendTitle "keys") (setCheck "KEY"))
          (.. me (appendValueInput "KEY1") (setCheck "KEY"))
          (. me setOutput true "KEY")
          ))
      }))


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
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (.setColour me 90)
          (-> me (.appendValueInput "API0") (.appendTitle "API paralell call"))
          (-> me (.setOutput true "Array"))
          (-> me (.setMutator (new Blockly.Mutator (array "lists_create_with_item"))))
          (set! (.-itemCount_ me) 1)))
      
      :mutationToDom
      (fn [workspace]
        (this-as me
          (let [container (.createElement js/document "mutation")]
            (. container setAttribute "items" (.-itemCount_ me))
            container)))

      :domToMutation
      (fn [container]
        (this-as me
          (doseq [x (range 0 (dec (. me -itemCount_)))]
            (. me removeInput (str "API" x)))
          (set! (.-itemCount_ me)
            (js/parseInt (. container getAttribute "items") 10))
          (doseq [x (range 0 (dec (. me -itemCount_)))]
            (let [input (. me appendValueInput (str "API" x))]
              (if (= x 0)
                (.appendTitle input
                  (Blockly/LANG_LISTS_CREATE_WITH_INPUT_WITH)))))
          (if (= (.-itemCount_ me) 0)
            (-> me
              (.appendDummyInput "EMPTY")
              (.appendTitle (Blockly/LANG_LISTS_CREATE_EMPTY_TITLE))))))

      :decompose
      (fn [workspace]
        (this-as me
          (let [containerBlock
                 (new Blockly.Block workspace "lists_create_with_container")]
            (. containerBlock initSvg)
            (loop [ connection (.. containerBlock
                                 (getInput "STACK")
                                 -connection)
                    x 0]
              (when (< x (. me -itemCount_))
                (let [itemBlock (new Blockly.Block workspace "lists_create_with_item")]
                  (. itemBlock initSvg)
                  (. connection connect (. itemBlock -previousConnection))
                  (recur (. itemBlock -nextConnection) (inc x)))))
            containerBlock)))

      :compose
      (fn [containerBlock]
        (this-as me
          (if (= (. me -itemCount_) 0)
            (. me removeInput "EMPTY")
            (loop [x (dec (. me -itemCount_))]
              (when (>= x 0)
                (. me removeInput (str "API" x))
                (recur (dec x)))))
          (set! (.-itemCount_ me) 0)
          (loop [itemBlock (. containerBlock getInputTargetBlock "STACK")]
            (when itemBlock
              (let [input (. me appendValueInput (str "API" (.-itemCount_ me)))]
                (if (= (.-itemCount_ me) 0)
                  (. input appendTitle Blockly/LANG_LIST_CREATE_WITH_INPUT_WITH))
                (if (.-valueConnection_ itemBlock)
                  (-> input .-connection (.connect (.-valueConnection_ itemBlock))))
                (set! (.-itemCount_ me) (inc (.-itemCount_ me)))
                (recur (and (.-nextConnection itemBlock)
                         (.. itemBlock -nextConnection targetBlock))))))
          (if (= (.-itemCount_ me) 0)
            (-> me
              (.appendDummyInput "EMPTY")
              (.appendTitle Blockly/LANG_LISTS_CREATE_EMPTY_TITLE)))))

      :saveConnections
      (fn [containerBlock]
        (this-as me
          (loop [ itemBlock (. containerBlock getInputTargetBlock "STACK")
                  x 0]
            (when itemBlock
              (let [input (. me getInput (str "API" x))]
                (set! (.-valueConnection_ itemBlock)
                  (if input (.. input -connection -targetConnection) input)))
              (recur (and (.-nextConnection itemBlock)
                       (.. itemBlock -nextConnection targetBlock))
                (inc x))))))
      }))
    

    

(defblock lists_create_with_container
  (clj->js
    { :init
      (fn []
        (this-as me
          (.setColour me 90)
          (-> me
            (.appendDummyInput)
            (.appendTitle Blockly/LANG_LISTS_CREATE_WITH_CONTAINER_TITLE_ADD))
          (.appendStatementInput me "STACK")
          (set! (. me -contextMenu) false))) }))

(defblock lists_create_with_item
  (js-obj
    "init"
    (fn []
      (this-as me
        (.setColour me 90)
        (-> me
          (.appendDummyInput)
          (.appendTitle "API"))
        (. me setPreviousStatement true)
        (. me setNextStatement true)
        (set! (. me -contextMenu) false)))))

(defblock api
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (. me setColour 0)
        (-> me
          (.appendDummyInput)
          (.appendTitle (apiDropdown) "api"))
        (. me setInputsInline true)
        (. me setOutput true "Array")))))

(defblock redirect
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (. me setColour 320)
        (-> me (.appendDummyInput) (.appendTitle (t :labels/redirect)))
        (-> me
          (.appendDummyInput)
          (.appendTitle (new Blockly.FieldTextInput "") "url"))
        (. me setInputsInline true)
        (. me setPreviousStatement true)
        (. me setNextStatement false)))))

(defblock render
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (. me setColour 340)
        (-> me (.appendDummyInput) (.appendTitle (t :labels/render)))
        (-> me
          (.appendDummyInput)
          (.appendTitle (templateDropdown) "template"))
        (. me setInputsInline true)
        (. me setPreviousStatement true)
        (. me setNextStatement false)))))

(defblock store_session
  (js-obj
    "helpUrl" ""
    "init"
    (fn []
      (this-as me
        (. me setColour 180)
        (-> me (.appendValueInput "KEY0") (.appendTitle "store-session"))
        (. me setPreviousStatement true)
        (. me setNextStatement true)))))

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

(goog/exportProperty js/Blockly "Language" js/Blockly.Language)
