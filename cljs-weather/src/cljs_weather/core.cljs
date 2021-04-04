(ns cljs-weather.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rd]
   [ajax.core :refer [GET]]))

(enable-console-print!)

(defonce app-state (atom {:title "WhichWeather"
                          :postal-code ""
                          :data-received? false
                          :temperatures {:today {:label "Today"
                                                 :value nil}
                                         :tomorrow {:label "Tomorrow"
                                                    :value nil}}}))

(def api-key "6735e7359a43946795090a880c89c67d")

(defn handle-response [resp]
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 8 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn get-forecast! []
  (let [postal-code (:postal-code @app-state)]
    (GET "http://api.openweathermap.org/data/2.5/forecast"
         {:params {"q" postal-code
                   "appid" api-key
                   "units" "imperial"}
          :handler handle-response})))

(defn title []
  [:h1 {:title @app-state}])

(defn temperature [temp]
  [:div {:class "temperature"}
   [:div {:class "value"}
    (:value temp)]
   [:h2 (:label temp)]])

(defn postal-code []
  [:div {:class "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "text"
            :placeholder "Postal Code"
            :value (:postal-code @app-state)
            :on-change #(swap! app-state assoc :postal-code (-> % .-target .-value))}]
   [:button {:on-click get-forecast!} "Go"]])

(defn app []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]])

(rd/render [app]
           (. js/document (getElementById "app")))
