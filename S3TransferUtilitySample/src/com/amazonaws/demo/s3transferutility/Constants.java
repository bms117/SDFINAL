/*
 * Copyright 2015-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.demo.s3transferutility;

import java.io.File;

public class Constants {

    /*
     * You should replace these values with your own. See the README for details
     * on what to fill in.
     */
    public static final String COGNITO_POOL_ID = "us-east-2:a33436d1-9803-4519-9da6-bc4da7e92726";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-east-2";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "this-is-a-test-write-dt04";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-east-2";

    // FILENAMES
    public static final String FileNameNode = "newNode.txt";
    public static final String FileNameBeacon = "tomorrow.txt";

    //  PHONE IDENTIFIER (CHANGE THIS WHEN DOWNLOADING APP ONTO PHONES)
    public static final String identity = "0";
}
