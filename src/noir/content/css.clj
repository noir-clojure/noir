(ns 
  #^{:skip-wiki true}
  noir.content.css
  (:use cssgen))

(def fldi (mixin :float :left
                 :display :inline))

(def emphasis (mixin :color :#6bffbd))
(def de-emph (mixin :color :#91979d))
(def dark-background (mixin :background "url(/img/noir-bg.png)"
                            :color :#d1d9e1))
(def emph-colors (mixin :background :#3f634d
                        :border [:2px :solid :#3c8455]))


(def light-text (mixin :color :#d1d9e1))
(def box (mixin :border-radius :8px
                :padding :10px))
(def light-box (mixin box
                      :background :#474949
                      :border [:2px :solid :#616363]))
(def emph-box (mixin box
                     :background :#3f634d
                     :border [:2px :solid :#3c8455]))


(defn noir-css []
  (css
    (rule "body" 
          dark-background
          :padding [:60px :80px]
          :font-family "'Helvetica Neue',Helvetica,Verdana")
    (rule "#wrapper"
          :margin [0 :auto]
          :width :1000px)
    (rule "#content"
          fldi
          :width "100%"
          :padding-bottom :100px)
    (rule "a"
          :text-decoration :underline
          de-emph
          (rule "&:hover" 
                emphasis))
    (rule "h1"
          :margin-bottom :0px
          light-text)
    (rule "h2"
          :margin-top :10px
          :margin-left :60px
          :font-size :18px
          :font-weight :normal)
    (rule "code"
          fldi
          light-box
          :font-family "Monaco, Consolas, 'Courier New'")
    (rule ".announce"
          fldi
          :width :970px
          :text-align :center
          :font-size :20px
          :margin-top :20px
          :margin-bottom :110px
          emph-box
          :padding :15px)
    (rule "#header"
          fldi
          :width "100%"
          :margin-bottom :50px
          (rule "h1"
                fldi)
          (rule "ul"
                :float :right
                :display :inline
                :list-style :none
                :margin-top :30px
                (rule "li"
                      fldi
                      (rule "a"
                            :text-decoration :none
                            fldi
                            light-box
                            :padding :8px
                            :margin-left :10px
                            (rule "&:hover"
                                  emph-colors)))))
    (rule "ul.content"
          fldi
          (rule "li"
                fldi
                :margin-bottom :55px
                :width "100%"
                (rule ".left"
                      fldi
                      :width "55%"
                      :text-align :left
                      (rule "p" 
                            :padding 0
                            :margin 0
                            :font-size :18px
                            ))
                (rule ".right"
                      fldi
                      :margin-right "5%"
                      :width "40%"
                      (rule "code"
                            :width "100%")
                      (rule "p"
                            :max-width :440px))))

    (rule "#not-found"
          :text-align :center
          :width :600px
          :margin [:0px :auto]
          :margin-top :200px
          (rule "h1"
                emphasis
                :font-size :32px
                :margin-bottom :20px
                ))

    (rule "#exception" 
          :max-width :900px 
          (rule "h1"
                :font-size :24px)
          (rule "ul"
                :margin 0
                :padding 0
                :margin-top :20px
                :list-style :none)
          (rule "table"
                :width "100%"
                :margin-top :20px
                :border-collapse :collapse)
          (rule "tr"
                light-box
                :margin-bottom :10px
                :width "100%")
          (rule "td"
                :padding :10px)

          (rule ".dt"
                :text-align :right)
          (rule ".dd"
                de-emph
                :margin 0
                :padding-left "5%")
          (rule "h1 span"
                :font-size :18px
                :font-weight :normal
                de-emph)
          (rule ".mine"
                emph-box))
    ))

