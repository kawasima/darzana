(control/defroute "" :get
  (api/call-api {:id :jsonplaceholder
                 :path "/posts"
                 :method :get
                 :var "posts"})
  (log/scopes)
  (renderer/render {:template "posts/index.ftlh"}))
