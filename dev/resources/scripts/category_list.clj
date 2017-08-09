(control/defroute "category" :get
  (api/call-api {:id :petstore :path "/category" :method :get
                 :var "categories"})
  (renderer/render {:template "/petstore/category/list"}))
