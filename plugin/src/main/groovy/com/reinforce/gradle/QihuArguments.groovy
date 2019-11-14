package com.reinforce.gradle

class QihuArguments {
    def sourcePath = ""
    def account = ""
    def password = ""

    void sourcePath(String path){
        sourcePath = path
    }

    void account(String account){
        this.account = account;
    }

    void password(String password){
        this.password = password;
    }
}