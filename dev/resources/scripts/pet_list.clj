(control/defroute "pet" :get
  (api/call-api {:id :petstore :path "/pet" :method :get})
  (renderer/render {:template "/petstore/pet/list"}))
