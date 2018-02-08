(control/defroute ["project/" :projectId "/iteration/" :iterationId] :get
  (mapper/read-value {:var "stories"}
                     {:var :stories})
  (api/call-api [{:id :rascaloid_api
                  :path "/iteration/{iterationId}"
                  :method :get
                  :var "iteration"}
                 {:id :rascaloid_api
                  :method :get
                  :path "/iteration/{iterationId}/kanban"
                  :var "stories"}
                 {:id :rascaloid_api
                  :path "/project/{projectId}"
                  :method :get
                  :var "project"}])
  (renderer/render {:template "iteration/show.ftl"}))
