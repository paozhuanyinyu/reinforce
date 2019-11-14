package com.reinforce.gradle

class LeguArguments {
    def sourcePath = ""
    def secretId = ""
    def secretKey = ""

    void sourcePath(String path){
        sourcePath = path
    }

    void secretId(String secretId){
        this.secretId = secretId
    }
    void secretKey(String secretKey){
        this.secretKey = secretKey
    }
}