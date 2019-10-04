#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export SPARK_DIST_CLASSPATH=$(/home/mendezr/development/hadoop-2.9.2/bin/hadoop classpath)
export HADOOP_HOME=$HOME/development/hadoop-2.9.2
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HADOOP_HOME/lib/native

echo $JAVA_HOME
echo $SPARK_DIST_CLASSPATH
echo $HADOOP_HOME

gcloud dataproc jobs submit spark --cluster superclusterhive --region europe-west1 --class org.fortysevendeg.sparksftp.ReadingSFTPConnectorApp --properties 'spark.executor.extraJavaOptions=-verbose:class,spark.executorEnv.SFTP_USER=webdev01,spark.executorEnv.SFTP_PASS=sftptest1,spark.executorEnv.SFTP_HOST=35.205.82.199,spark.executorEnv.SFTP_PATH=/home/webdev01/sample.psv' --jars gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/HikariCP-java7-2.4.12.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/activation-1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aliyun-java-sdk-core-3.4.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aliyun-java-sdk-ecs-4.2.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aliyun-java-sdk-ram-3.0.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aliyun-java-sdk-sts-3.0.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aliyun-sdk-oss-3.0.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/annotations-api.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aopalliance-1.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/apacheds-i18n-2.0.0-M15.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/apacheds-kerberos-codec-2.0.0-M15.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/api-asn1-api-1.0.0-M20.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/api-i18n-1.0.0-M20.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/api-util-1.0.0-M20.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/asm-3.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/avro-1.7.7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/aws-java-sdk-bundle-1.11.199.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/azure-data-lake-store-sdk-2.2.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/azure-keyvault-core-0.8.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/azure-storage-5.4.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/bootstrap.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/catalina-ant.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/catalina-ha.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/catalina-tribes.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/catalina.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-beanutils-1.7.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-beanutils-core-1.8.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-cli-1.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-codec-1.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-collections-3.2.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-compress-1.4.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-configuration-1.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-csv-1.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-daemon-1.0.13.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-daemon.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-digester-1.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-httpclient-3.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-io-2.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-lang-2.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-lang3-3.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-logging-1.1.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-math3-3.1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/commons-net-3.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/curator-client-2.7.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/curator-framework-2.7.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/curator-recipes-2.7.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/ecj-4.3.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/ehcache-3.3.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/el-api.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/fst-2.50.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/geronimo-jcache_1.0_spec-1.0-alpha-1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/gson-2.2.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/guava-11.0.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/guice-3.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/guice-servlet-3.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-aliyun-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-annotations-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-ant-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archive-logs-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archive-logs-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archive-logs-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archives-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archives-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-archives-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-auth-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-aws-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-azure-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-azure-datalake-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-common-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-common-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-common-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-common-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-datajoin-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-datajoin-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-datajoin-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-distcp-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-distcp-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-distcp-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-extras-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-extras-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-extras-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-gridmix-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-gridmix-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-gridmix-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-client-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-client-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-client-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-client-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-native-client-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-native-client-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-native-client-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-native-client-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-nfs-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-rbf-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-rbf-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-rbf-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-hdfs-rbf-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-kms-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-app-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-app-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-app-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-common-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-common-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-common-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-core-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-core-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-core-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-plugins-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-plugins-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-hs-plugins-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-jobclient-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-jobclient-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-jobclient-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-jobclient-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-shuffle-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-shuffle-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-client-shuffle-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-examples-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-examples-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-mapreduce-examples-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-nfs-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-openstack-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-resourceestimator-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-resourceestimator-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-resourceestimator-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-rumen-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-rumen-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-rumen-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-sls-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-sls-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-sls-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-streaming-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-streaming-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-streaming-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-api-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-api-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-api-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-distributedshell-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-distributedshell-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-distributedshell-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-unmanaged-am-launcher-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-unmanaged-am-launcher-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-applications-unmanaged-am-launcher-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-client-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-client-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-client-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-common-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-common-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-common-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-registry-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-applicationhistoryservice-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-applicationhistoryservice-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-applicationhistoryservice-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-common-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-common-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-common-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-nodemanager-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-nodemanager-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-nodemanager-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-resourcemanager-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-resourcemanager-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-resourcemanager-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-router-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-sharedcachemanager-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-tests-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-tests-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-tests-2.9.2-tests.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-tests-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-timeline-pluginstorage-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-timelineservice-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-timelineservice-hbase-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-timelineservice-hbase-tests-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-web-proxy-2.9.2-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-web-proxy-2.9.2-test-sources.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hadoop-yarn-server-web-proxy-2.9.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hamcrest-core-1.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hbase-annotations-1.2.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hbase-client-1.2.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hbase-common-1.2.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hbase-protocol-1.2.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/hsqldb-2.3.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/htrace-core-3.1.0-incubating.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/htrace-core4-4.1.0-incubating.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/httpclient-4.5.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/httpcore-4.4.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-annotations-2.6.7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-annotations-2.7.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-core-2.6.7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-core-2.7.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-core-asl-1.9.13.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-databind-2.6.7.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-databind-2.7.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-dataformat-yaml-2.6.7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-jaxrs-1.9.13.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-mapper-asl-1.9.13.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-module-jaxb-annotations-2.6.7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-module-paranamer-2.7.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-module-scala_2.11-2.6.7.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jackson-xc-1.9.13.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jasper-el.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jasper.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/java-util-1.9.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/java-xmlbuilder-0.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/javax.inject-1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jaxb-api-2.2.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jaxb-impl-2.2.3-1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jcip-annotations-1.0-1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jcodings-1.0.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jdom-1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jersey-client-1.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jersey-core-1.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jersey-guice-1.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jersey-json-1.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jersey-server-1.9.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jets3t-0.9.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jettison-1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jetty-6.1.26.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jetty-sslengine-6.1.26.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jetty-util-6.1.26.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jline-0.9.94.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/joni-2.1.2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/json-20170516.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/json-io-2.5.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/json-simple-1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/json-smart-1.3.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/json4s-jackson_2.11-3.5.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jsp-api-2.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jsp-api.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jsr305-3.0.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jsr311-api-1.1.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jul-to-slf4j-1.7.25.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/junit-4.11.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/leveldbjni-all-1.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/log4j-1.2.17.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/metrics-core-2.2.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/metrics-core-3.0.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/mockito-all-1.8.5.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/mssql-jdbc-6.2.1.jre7.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/netty-3.6.2.Final.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/netty-3.9.9.Final.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/netty-all-4.0.23.Final.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/netty-all-4.1.17.Final.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/nimbus-jose-jwt-4.41.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/ojalgo-43.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/okhttp-2.7.5.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/okio-1.6.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/paranamer-2.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/paranamer-2.8.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/parquet-jackson-1.10.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/protobuf-java-2.5.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/servlet-api-2.5.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/servlet-api.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/slf4j-api-1.7.25.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/slf4j-log4j12-1.7.25.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/snappy-java-1.0.5.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/sparksftpTest-assembly-0.0.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/stax-api-1.0-2.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/stax2-api-3.1.4.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-coyote.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-dbcp.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-i18n-es.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-i18n-fr.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-i18n-ja.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/tomcat-juli.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/woodstox-core-5.0.3.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/xercesImpl-2.9.1.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/xml-apis-1.3.04.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/xmlenc-0.52.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/xz-1.0.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/zookeeper-3.4.6.jar,gs://dataproc-419763b0-894e-4402-8a6d-ed84cf766202-europe-west1/jsch-0.1.53.jar
