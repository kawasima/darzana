(control/defroute "" :get
  (api/call-api {:id :rascaloid_api
                 :path "/projects"
                 :method :get
                 :var "projects"})
  (renderer/render {:template "index.ftl"}))
