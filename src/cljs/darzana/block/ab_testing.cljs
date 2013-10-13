(ns darzana.block.ab-testing
  (:use [darzana.i18n :only [t]])
  (:require
    [Blockly :as Blockly])
  (:use-macros [darzana.core :only [defblock]]))

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
            (.appendTitle (new Blockly.FieldTextInput "") "test-id"))
          (-> me
            (.setMutator
              (Blockly.Mutator. (array "ab_testing_alternative"))))
          (-> me
            (.appendStatementInput "ALT0")
            (.appendTitle (new Blockly.FieldTextInput "A") "ALT_NAME0"))
          (-> me
            (.appendStatementInput "ALT1")
            (.appendTitle (new Blockly.FieldTextInput "B") "ALT_NAME1"))
          (. me setPreviousStatement true)
          (. me setNextStatement true)
          (set! (. me -altCount_) 2)
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
          (set! (. me -altCount_) (js/parseInt (. xmlElement getAttribute "alternative") 10))
          (loop [x 2]
            (when (< x (. me -altCount_))
              (.. me
                (appendStatementInput (str "ALT" x))
                (appendTitle (new Blockly.FieldTextInput "") (str "ALT_NAME" x)))
              (recur (inc x))))))

      :decompose
      (fn [workspace]
        (this-as me
          (let [containerBlock (new Blockly.Block workspace "ab_testing_alternative_container")]
            (. containerBlock initSvg)
            (loop [ x 0
                    connection (.. containerBlock (getInput "STACK") -connection)]
              (when (< x (. me -altCount_))
                (let [altBlock (new Blockly.Block workspace "ab_testing_alternative")]
                  (. altBlock initSvg)
                  (. connection connect (. altBlock -previousConnection))
                  (recur (inc x) (. altBlock -nextConnection)))))
            containerBlock)))

      :compose
      (fn [containerBlock]
        (this-as me
          (loop [x (dec (. me -altCount_))]
            (when (>= x 0)
              (. me removeInput (str "ALT" x))
              (recur (dec x))))
          (set! (. me -altCount_) 0)
          (loop [clauseBlock (. containerBlock getInputTargetBlock "STACK")]
            (when-not (nil? clauseBlock)
              (let [altInput (.. me (appendStatementInput (str "ALT" (. me -altCount_))))]
                (when-let [statementConnection (. clauseBlock -statementConnection_)]
                  (.. altInput -connection (connect statementConnection)))
                (. altInput appendTitle
                  (new js/Blockly.FieldTextInput (or (. clauseBlock -altName_) ""))
                  (str "ALT_NAME" (. me -altCount_))))
              (set! (. me -altCount_) (inc (. me -altCount_)))
              (recur (.. clauseBlock -nextConnection targetBlock))))))

      :saveConnections
      (fn [containerBlock]
        (this-as me
          (loop [ clauseBlock (. containerBlock getInputTargetBlock "STACK")
                  x 0]
            (when clauseBlock
              (case (. clauseBlock -type)
                "ab_testing_alternative"
                (let [ inputAlt (. me getInput (str "ALT" x))]
                  (set! (. clauseBlock -statementConnection_)
                    (and inputAlt (.. inputAlt -connection -targetConnection)))
                  (set! (. clauseBlock -altName_)
                    (some-> inputAlt (.-titleRow) (first) (.-text_)))))
              (recur (and (. clauseBlock -nextConnection)
                       (.. clauseBlock -nextConnection targetBlock)) (inc x))))))}))

(defblock ab_testing_alternative_container
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (.  me setColour 210)
          (.. me appendDummyInput (appendTitle ""))
          (.  me appendStatementInput "STACK")
          (set! (. me -contextMenu) false)
          ))}))

(defblock ab_testing_alternative
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 210)
          (.. me appendDummyInput
            (appendTitle (t :labels/alternative)))
          (.setPreviousStatement me true)
          (.setNextStatement me true)
          (set! (. me -contextMenu) false)))
      }))

(defblock ab_testing_convert
  (clj->js
    { :helpUrl ""
      :init
      (fn []
        (this-as me
          (. me setColour 220)
          (.. me appendDummyInput 
            (appendTitle (t :labels/convert))
            (appendTitle (new Blockly.FieldTextInput "") "test-id"))
          (. me setPreviousStatement true)
          (. me setNextStatement true)))
      }))
