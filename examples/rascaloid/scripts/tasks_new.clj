(control/defroute ["project/" :projectId "/story/" :storyId "/tasks/new"] :get
  (renderer/render {:template "tasks/new.ftl"}))
