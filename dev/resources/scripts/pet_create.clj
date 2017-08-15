(control/defroute "pet/create" :post
  (api/call-api {:id :petstore :path "/pet" :method :post})
  (control/redirect "/pet"))
