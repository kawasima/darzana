(control/defroute "category/create" :post
  (mapper/read-value {:var "category"} {:type io.swagger.model.Category :var :category})
  (control/if-success
   (-> (api/call-api {:id :petstore :path "/category" :method :post})
       (control/redirect "/category"))
   (renderer/render {:template "/petstore/category/new"})))
