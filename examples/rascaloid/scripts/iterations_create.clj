(control/defroute ["project/" :projectId "/iterations/create"] :post
  (mapper/read-value {:var "iteration"}
                     {:var :iteration})
  (control/if-success
   (-> (api/call-api {:id :rascaloid_api :path "/project/{projectId}/iterations" :method :post})
       (control/redirect "/project/{projectId}"))
   (renderer/render {:template "iterations/new.ftl"})))
