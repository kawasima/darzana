(control/defroute ["project/" :projectId "/story/" :storyId "/task/" :taskId "/update"] :post
  (api/call-api {:id :rascaloid_api
                 :method :put
                 :path "/task/{taskId}"})
  (control/if-success
   (control/redirect "/project/{projectId}/story/{storyId}")
   (renderer/render {:template "error.ftl"})))
