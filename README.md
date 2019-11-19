### reinforce
![](https://img.shields.io/badge/license-Apache--2.0-brightgreen.svg?style=flat)
![](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)
![](https://img.shields.io/badge/build-passing-brightgreen.svg?style=flat)
![](https://img.shields.io/badge/release-1.0.3-brightgreen.svg?style=flat)

  这是一个gradle插件，用于打360加固和腾讯乐固加固包，只需要使用一行gradle命令，对于自动化打包非常方便


###  使用方法：

1. 在你的项目根目录的build.gradle里引入以下依赖

   ```
   repositories {
        maven { url 'https://jitpack.io' }  //添加的远程仓库配置
    }
   
   dependencies {     
       classpath 'com.github.paozhuanyinyu:reinforce:1.0.3'  //依赖的插件版本
   }
   ```



2. 在你的项目app/build.gradle里引入以下依赖

   `apply plugin: 'reinforce'`

3. 在你的项目app/build.gradle里添加加固配置参数

   ```
   reinforce{
       keystorePath = 签名文件路径
       keystorePassword = 签名文件密码
       alias = 签名文件别名
       aliasPassword = 别名密码
       apkDir = apk所在的目录，建议使用默认目录(app/build/outputs/apk)
       reinforcedApkDir = 加固包存放目录，建议放到build目录下面，clean后删除，不影响下次打加固包
       enableQihu = 是否开启360加固
       qihu {
           sourcePath = jiagu.jar包路径
           account = 360加固账户名
           password = 360加固账户密码
       }
   
       enableLegu = 是否开启乐固加固
       legu {
           sourcePath = ms-shield.jar包路径
           secretId = 乐固加固secretId
           secretKey = 乐固加固secretKey
       }
   }
   ```

​      这里需要注意360加固和乐固加固的sourcePath参数，两者都支持命令行加固，需要分别下载jar包到电脑上。

### 360加固

1. 到[360加固下载页面](https://jiagu.360.cn/#/global/download) 下载360加固软件。

2. 解压下载的zip文件，在解压目录/jiagu/下面找到jiagu.jar（我电脑是mac，所以目录是360jiagubao_mac/jiagu/jiagu.jar），将jiagu.jar包的全路径配置到上面的qihu下面的sourcePath上。

### 乐固加固

1. 下载乐固ms-shield.jar，链接：https://leguimg.qcloud.com/ms-client/java-tool/1.0.3/ms-shield.jar

2. 将ms-shield.jar包的全路径配置到上面legu下面的sourcePath上



其他参数的配置可以参考项目app/build.gradle



### 然后执行打包命令

`./gradlew clean assemble{variantName,如若没有variant则不写}ReleaseReinforce`


### License

reinforce is released under a Apache-2.0 License. See [LICENSE](LICENSE) file for details.
