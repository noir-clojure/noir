(ns noir.util.s3
  "Basic S3 utils"
  (:refer-clojure :exclude [list])
  (:import 
    (org.jets3t.service.security AWSCredentials)
    (org.jets3t.service.acl AccessControlList)
    (org.jets3t.service.impl.rest.httpclient RestS3Service)
    (org.jets3t.service.model S3Object)))

(declare *s3*)

(defn service 
  "Create an S3 service object"
  [{secret :secret-key access :access-key}]
  (new RestS3Service (new AWSCredentials access secret)))

(defmacro with-s3 
  "Given a server-spec which contains {:secret-key :access-key} execute the given body
  within the context of an S3 connection"
  [server-spec & body]
  `(binding [*s3* (service ~server-spec)]
     ~@body))

(defn put! 
  "Put the given file on S3 where bucket is the string name of the S3 bucket to use."
  [bucket file]
  (let [obj (new S3Object file)]
    (. obj setAcl (. AccessControlList REST_CANNED_PUBLIC_READ))
    (. *s3* putObject bucket obj)))

(defn list 
  "List all files in the bucket with the given prefix"
  [bucket prefix]
  (let [s3b (. *s3* getBucket bucket)]
    (. *s3* listObjects s3b prefix "")))

