(control/defroute "projects/create" :post
  (mapper/read-value {:var "project"}
                     {:var :project})
  (control/if-success
   (-> (api/call-api {:id :rascaloid_api :path "/projects" :method :post})
       (control/redirect "/"))
   (renderer/render {:template "projects/new.ftl"})))
