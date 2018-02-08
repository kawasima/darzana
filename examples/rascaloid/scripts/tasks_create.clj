(control/defroute ["project/" :projectId "/story/" :storyId "/tasks/create"] :post
  (mapper/read-value {:var "stories"}
                     {:var :stories})
  (control/if-success
   (-> (api/call-api {:id :rascaloid_api
                      :path "/story/{storyId}/tasks"
                      :method :post
                      :var "task"})
       (control/if-success
        (control/redirect "/project/{projectId}/story/{storyId}")
        (renderer/render {:template "error.ftl"})))
   (renderer/render {:template "stories/new.ftl"})))
