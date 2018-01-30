(control/defroute ["project/" :projectId] :get
  (api/call-api [{:id :rascaloid_api
                  :path "/project/{projectId}"
                  :method :get
                  :var "project"}
                 {:id :rascaloid_api
                  :path "/project/{projectId}/iterations"
                  :method :get
                  :var "iterations"}])
  (renderer/render {:template "project/show.ftl"}))
