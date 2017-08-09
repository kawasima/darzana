(control/defroute "pet/new" :get
  (api/call-api {:id :petstore :path "/category" :method :get
                 :var "categories"})
  (renderer/render {:template "/petstore/pet/new"}))
