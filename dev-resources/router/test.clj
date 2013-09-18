(defmarga GET "/groups" (call-api [app/groups]) (render "groups/list"))
(defmarga
 GET
 "/groups/edit/:id"
 (call-api [app/group groups app/users])
 (render "groups/edit"))
(defmarga
 POST
 "/groups/save/:id"
 (call-api [app/group-put])
 (if-success (redirect "/groups") (render "/groups/edit")))
(defmarga GET "/groups/new" (render "groups/new"))
(defmarga
 POST
 "/groups/create"
 (call-api [app/group-post])
 (store-session :group ["id" "name"])
 (if-success (redirect "/groups") (render "/groups/new")))
(defmarga
 POST
 "/groups/delete/:id"
 (call-api [app/group-delete])
 (if-success (redirect "/groups") (render "/groups/show")))
(defmarga
 GET
 "/groups/:id"
 (call-api [app/group])
 (render "groups/show"))
(defmarga GET "/juju")
