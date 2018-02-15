(control/defroute ["project/" :projectId "/story/" :storyId "/task/" :taskId] :get
  (api/call-api [{:id :rascaloid_api
                  :method :get
                  :path "/task/{taskId}"
                  :var :task}
                 {:id :rascaloid_api
                  :method :get
                  :path "/story/{storyId}"
                  :var :story}
                 {:id :rascaloid_api
                  :method :get
                  :path "/project/{projectId}"
                  :var :project}])
  (control/if-success
   (renderer/render {:template "task/show.ftl"})
   (renderer/render {:template "error.ftl"})))
