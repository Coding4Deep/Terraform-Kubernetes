   rs.initiate()
   rs.initiate({
     _id: "rs0",
     members: [
       { _id: 0, host: "mongo-set-0.mongo-svc.spring.svc.cluster.local:27017" }
     ]
   })

   rs.status()
   show dbs
   use devopsdb
   db.stats()