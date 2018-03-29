(control/defroute ["post/" :id]  :get
  (api/call-api {:id :jsonplaceholder
                 :path "/posts/{id}"
                 :method :get
                 :var "post"})
  (log/scopes)
  (renderer/render {:template "/posts/show.ftlh"}))
