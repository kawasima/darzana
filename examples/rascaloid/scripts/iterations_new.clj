(control/defroute ["project/" :projectId "/iterations/new"] :get
  (renderer/render {:template "iterations/new.ftl"}))
