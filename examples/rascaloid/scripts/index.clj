(control/defroute "" :get
  (api/call-api {:id :rascaloid_api
                 :path "/projects"
                 :method :get
                 :var "projects"})
  (control/if-success
   (renderer/render {:template "index.ftl"})
   (renderer/render {:template "error.ftl"})))
