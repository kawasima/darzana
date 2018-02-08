(control/defroute ["project/" :projectId "/story/" :storyId] :get
  (api/call-api [{:id :rascaloid_api
                  :path "/story/{storyId}"
                  :method :get
                  :var "story"}
                 {:id :rascaloid_api
                  :method :get
                  :path "/story/{storyId}/tasks"
                  :var "tasks"}
                 {:id :rascaloid_api
                  :path "/project/{projectId}"
                  :method :get
                  :var "project"}])
  (renderer/render {:template "story/show.ftl"}))
