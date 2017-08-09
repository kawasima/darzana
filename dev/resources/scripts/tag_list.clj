(control/defroute "tag" :get
  (api/call-api {:id :petstore :path "/tag" :method :get
                 :var "tags"})
  (renderer/render {:template "/petstore/tag/list"}))
