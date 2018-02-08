(control/defroute ["project/" :projectId "/stories/new"] :get
  (renderer/render {:template "stories/new.ftl"}))
