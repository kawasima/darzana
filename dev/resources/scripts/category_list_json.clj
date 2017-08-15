(control/defroute "category/json" :get
  (api/call-api {:id :petstore :path "/category" :method :get
                 :var "categories"})
  (renderer/render {:format :json :var "categories"}))
