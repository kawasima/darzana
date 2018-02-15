(control/defroute ["project/" :projectId "/story/" :storyId "/task/" :taskId "/edit"] :get
  (api/call-api [{:id :rascaloid_api
                  :method :get
                  :path "/task/{taskId}"
                  :var :task}
                 {:id :rascaloid_api
                  :method :get
                  :path "/taskStatus"
                  :var :taskStatus}])
  (control/if-success
   (renderer/render {:template "task/edit.ftl"})
   (renderer/render {:template "error.ftl"})))
