(control/defroute ["category/" :categoryId] :post
  (api/call-api {:id :petstore :path "/category" :method :put})
  (control/redirect "/category"))
