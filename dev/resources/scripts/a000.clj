(control/defroute "" :get
  (log/scopes)
  (renderer/render {:template "/petstore/index"}))
