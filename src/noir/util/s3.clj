(ns noir.utils.s3
  (:refer-clojure :exclude [list])
  (:import 
    (org.jets3t.service.security AWSCredentials)
    (org.jets3t.service.acl AccessControlList)
    (org.jets3t.service.impl.rest.httpclient RestS3Service)
    (org.jets3t.service.model S3Object)))

(declare *s3*)

(defmacro with-s3 [server-spec & body]
  (binding [*s3* (service server-spec)]
    ~@body))

(defn service [{secret :secret-key access :access-key}]
  (new RestS3Service (new AWSCredentials access secret)))

(defn put! [bucket file]
  (let [obj (new S3Object file)]
    (. obj setAcl (. AccessControlList REST_CANNED_PUBLIC_READ))
    (. *s3* putObject bucket obj)))

(defn list [bucket prefix]
  (let [s3b (. *s3* getBucket bucket)]
    (. *s3* listObjects s3b prefix "")))

