package com.reinforce.gradle

import org.gradle.api.Action

class ArgumentsBean{
    def keystorePath = ""
    def keystorePassword = ""
    def alias = ""
    def aliasPassword = ""
    def apkDir = ""
    def reinforcedApkDir = ""

    QihuArguments qihu = new QihuArguments()
    LeguArguments legu = new LeguArguments()

    //创建内部Extension，名称为方法名 qihu
    void qihu(Action<QihuArguments> action){
        action.execute(this.qihu)
    }
    //创建内部Extension，名称为方法名 qihu
    void qihu(Closure c) {
        org.gradle.util.ConfigureUtil.configure(c, this.qihu)
    }


    //创建内部Extension，名称为方法名 qihu
    void legu(Action<LeguArguments> action){
        action.execute(this.qihu)
    }
    //创建内部Extension，名称为方法名 qihu
    void legu(Closure c) {
        org.gradle.util.ConfigureUtil.configure(c, this.legu)
    }

    void keystorePath(keystorePath) {
        this.keystorePath = keystorePath
    }

    void keystorePassword(keystorePassword) {
        this.keystorePassword = keystorePassword
    }

    void alias(alias) {
        this.alias = alias
    }

    void aliasPassword(aliasPassword) {
        this.aliasPassword = aliasPassword
    }

    void apkDir(apkDir) {
        this.apkDir = apkDir
    }

    void reinforcedApkDir(reinforcedApkDir) {
        this.reinforcedApkDir = reinforcedApkDir
    }
}