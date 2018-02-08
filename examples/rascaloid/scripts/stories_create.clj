(control/defroute ["project/" :projectId "/stories/create"] :post
  (mapper/read-value {:var "stories"}
                     {:var :stories})
  (control/if-success
   (-> (api/call-api {:id :rascaloid_api
                      :path "/project/{projectId}/stories"
                      :method :post
                      :var "story"})
       (control/if-contains
        "iterationId"
        (->
         (mapper/read-value {:scope :page :var ["story" "id"]}
                            {:var "storyId"})
         (api/call-api {:id :rascaloid_api
                        :path "/iteration/{iterationId}/addStory/{storyId}"
                        :method :put})))
       (control/redirect "/project/{projectId}/iteration/{iterationId}"))
   (renderer/render {:template "stories/new.ftl"})))
