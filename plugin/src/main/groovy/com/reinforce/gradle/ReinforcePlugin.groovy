package com.reinforce.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.platform.base.Variant

class ReinforcePlugin implements Plugin<Project>{

    /**
     * 断点调试命令： ./gradlew testPlugin -Dorg.gradle.daemon=false -Dorg.gradle.debug=true 参考：https://www.jianshu.com/p/952f5aa52bb5
     * System.getenv() 获取系统环境变量
     * System.properties.toString() 获取属性配置
     * project.properties 获取gradle的配置
     * project.extensions.getByName("android")可以获取module下build.gradle android{}里面的配置 [ext, defaultArtifacts, reporting, sourceSets, java, buildOutputs, android, reinforce]
     */

    @Override
    void apply(Project project) {
        //获取Android SDK路径
        def props = new java.util.Properties()
        props.load( new java.io.FileInputStream(project.rootDir.toString() + "/local.properties"))
        def ANDROID_HOME = props.getProperty("sdk.dir")

        //获取buildToolsVersion
        def buildToolsVersion = project.extensions.getByName("android").getAt("buildToolsVersion")

        // apksigner路径
        def apksignerPath = ANDROID_HOME + File.separator + "build-tools" + File.separator + buildToolsVersion + File.separator + "apksigner"

        println("apksignerPath: " + apksignerPath)

        //zipalign路径
        def zipalignPath = ANDROID_HOME + File.separator + "build-tools" + File.separator + buildToolsVersion + File.separator + "zipalign"

        println("zipalignPath: " + zipalignPath)

        project.extensions.create("reinforce",ArgumentsBean)
        project.reinforce.extensions.create("qihu",QihuArguments)
        project.reinforce.extensions.create("legu",LeguArguments)
        project.afterEvaluate {
            project.android.applicationVariants.all { variant ->
                def variantName = variant.name.capitalize()
                println("variantName: " + variantName)
                def assemble = variant.assembleProvider.get()
                println("assemble: " + assemble)
                project.task("assemble${variantName}Reinforce").dependsOn(assemble).doFirst {
                    println("doFirst")
                    println("keystorePath: " + project.reinforce.keystorePath)
                    println("keystorePassword: " + project.reinforce.keystorePassword)
                    println("alias: " + project.reinforce.alias)
                    println("aliasPassword: " + project.reinforce.aliasPassword)
                    println("apkDir: " + project.reinforce.apkDir)
                    println("reinforcedApkDir: " + project.reinforce.reinforcedApkDir)

                    println("legu sourcePath: " + project.reinforce.legu.sourcePath)
                    println("legu secretId: " + project.reinforce.legu.secretId)
                    println("legu secretKey: " + project.reinforce.legu.secretKey)

                    println("qihu sourcePath: " + project.reinforce.qihu.sourcePath)
                    println("qihu account: " + project.reinforce.qihu.account)
                    println("qihu password: " + project.reinforce.qihu.password)
                }.doLast {

                    String apkDirPath = project.reinforce.apkDir
                    String reinforcedApkDirPath = project.reinforce.reinforcedApkDir
                    println("apkDirPath: " + apkDirPath)
                    File apkDir = new File(apkDirPath)
                    if(!apkDir.exists() || !apkDir.isDirectory()){
                        project.logger.error("apkDir不存在或者apkDir不是一个目录")
                        throw new ProjectConfigurationException("apkDir不存在或者apkDir不是一个目录")
                    }
                    File reinforcedApkDir = new File(reinforcedApkDirPath)
                    if(!reinforcedApkDir.exists()){
                        reinforcedApkDir.mkdirs()
                    }

                    for(File file : apkDir.listFiles()) {
                        if (file.exists() && file.getName().endsWith(".apk")) {
                            //360加固
                            qihuReinforce(project,file)
                            //乐固加固
                            leguReinforce(project,apksignerPath,zipalignPath,file)

                        }
                    }

                }
            }
        }
    }
    void qihuReinforce(Project project, File file){
        String loginCommand = "java -jar " +
                project.reinforce.qihu.sourcePath +
                " -login " +
                project.reinforce.qihu.account +
                " " +
                project.reinforce.qihu.password
        println("360加固登录命令： " + loginCommand)
        CommandResult commandResult = executeCommand(loginCommand)
        if((commandResult.errorStr != null && commandResult.errorStr.trim().length() > 0) || !commandResult.outputStr.contains("login success")) {
            project.logger.error("360加固登录失败")
            throw new RuntimeException("360加固登录失败")
        }

        String importSignCommand = "java -jar " +
                project.reinforce.qihu.sourcePath +
                " -importsign " +
                project.reinforce.keystorePath +
                " " +
                project.reinforce.keystorePassword +
                " " +
                project.reinforce.alias +
                " " +
                project.reinforce.aliasPassword
        println("360加固导入签名命令： " + importSignCommand)
        CommandResult importSignCommandResult = executeCommand(importSignCommand)
        if((importSignCommandResult.errorStr != null && importSignCommandResult.errorStr.trim().length() > 0) || !importSignCommandResult.outputStr.contains("signer saving succeed")) {
            project.logger.error("360导入签名失败")
            throw new RuntimeException("360导入签名失败")
        }

        String jiaguCommand = "java -jar " +
                project.reinforce.qihu.sourcePath +
                " -jiagu " +
                file.getAbsolutePath() +
                " " +
                project.reinforce.reinforcedApkDir +
                " -autosign"
        println("360加固命令： " + jiaguCommand)
        CommandResult jiaguCommandResult = executeCommand(jiaguCommand)
        if((jiaguCommandResult.errorStr != null && jiaguCommandResult.errorStr.trim().length() > 0) || !jiaguCommandResult.outputStr.contains("任务完成_已签名")) {
            project.logger.error("360加固失败")
            throw new RuntimeException("360加固失败")
        }
        println("360加固完成")
    }
    void leguReinforce(Project project, def apksignerPath, def zipalignPath, File file){
        //加固
        String fileName = file.getName()
        println("fileName: " + fileName)
        String reinforceCommand = 'java -Dfile.encoding=utf-8 -jar ' + project.reinforce.legu.sourcePath + ' -sid ' + project.reinforce.legu.secretId + ' -skey ' + project.reinforce.legu.secretKey + ' -uploadPath ' + file.getAbsolutePath() + ' -downloadPath ' + project.reinforce.reinforcedApkDir
        println("reinforceCommand: " + reinforceCommand)
        CommandResult reinforceCommandResult = executeCommand(reinforceCommand)
        if((reinforceCommandResult.errorStr != null && reinforceCommandResult.errorStr.trim().length() > 0) || reinforceCommandResult.outputStr.contains("错误码")){
            project.logger.error("乐固加固失败")
            throw new RuntimeException("乐固加固失败")
        }
        String reinforceApkPath = project.reinforce.reinforcedApkDir + File.separator + fileName.replace(".apk","_legu.apk")
        println("reinforceApkPath: " + reinforceApkPath)

        //对齐(zipalign可以在V1签名后执行,但zipalign不能在V2签名后执行,只能在V2签名之前执行)
        String zipalignedApkPath = reinforceApkPath.replace(".apk","_zipaligned.apk")
        println("zipalignedApkPath: " + zipalignedApkPath)

        String zipalignedCommand = zipalignPath + " -v 4 " + reinforceApkPath + " " + zipalignedApkPath
        CommandResult zipalignedResult = executeCommand(zipalignedCommand)
        if(zipalignedResult.errorStr != null && zipalignedResult.errorStr.trim().length() > 0){
            project.logger.error("对齐失败")
            throw new RuntimeException("对齐失败")
        }
        //重签名
        String signedApkPath = zipalignedApkPath.replace(".apk","_signed.apk")
        println("signedApkPath: " + signedApkPath)
        String signCommand = apksignerPath + " sign --ks " + project.reinforce.keystorePath + " --ks-key-alias " + project.reinforce.alias + " --ks-pass pass:" + project.reinforce.keystorePassword + " --key-pass pass:" + project.reinforce.aliasPassword + " --out " + signedApkPath + " " + zipalignedApkPath
        println("signCommand: " + signCommand)
        CommandResult signedResult = executeCommand(signCommand)
        if(signedResult.errorStr != null && signedResult.errorStr.trim().length() > 0){
            project.logger.error("重签名失败")
            throw new RuntimeException("重签名失败")
        }
        println("乐固加固完成")
        boolean isDeleteReinforceApk = false
        boolean isDeleteZipalignedApkFileApk = false
        File reinforceApkFile = new File(reinforceApkPath)
        if(reinforceApkFile.exists()){
            isDeleteReinforceApk = reinforceApkFile.delete()
            println("删除加固包：" + isDeleteReinforceApk)
        }

        File zipalignedApkFile = new File(zipalignedApkPath)
        if(zipalignedApkFile.exists()){
            isDeleteZipalignedApkFileApk = zipalignedApkFile.delete()
            println("删除对齐包：" + isDeleteZipalignedApkFileApk)
        }
        if(isDeleteReinforceApk && isDeleteZipalignedApkFileApk){
            File resultApk = new File(signedApkPath)
            boolean isRenameSuccess = resultApk.renameTo(reinforceApkFile)
            println("重命名包：" + isRenameSuccess)
        }

    }

    CommandResult executeCommand(String cmd){
        def serr = new StringBuilder()
        def proc
        try{
            proc = cmd.execute()
            proc.consumeProcessErrorStream(serr)
            String result = proc.text
            println("executeCommand: " + cmd + ";\nresult: " + result)
            String error = serr.toString()
            println("executeCommand: " + cmd + ";\noccurred an error: " + error)
            proc.closeStreams()
            return new CommandResult(result,error)
        }catch(Exception e){
            e.printStackTrace()
            return new CommandResult("",e.getMessage())
        }
    }
}