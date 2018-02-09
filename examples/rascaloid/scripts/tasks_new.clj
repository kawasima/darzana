(control/defroute ["project/" :projectId "/story/" :storyId "/tasks/new"] :get
  (api/call-api {:id :rascaloid_api
                 :method :get
                 :path "/taskStatus"
                 :var :taskStatus})
  (control/if-success
   (renderer/render {:template "tasks/new.ftl"})
   (renderer/render {:template "error.ftl"})))
