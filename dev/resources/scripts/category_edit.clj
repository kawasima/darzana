(control/defroute ["category/edit/" :categoryId]  :get
  (control/if-success
   (-> (api/call-api {:id :petstore :path "/category/{categoryId}" :method :get :var "category"})
       (renderer/render {:template "/petstore/category/edit"}))
   (renderer/render {:template "/petstore/500.html"})))
