# apk2gradle
Apk to gradle project
将apk转成gradle可开发项目

## 依赖工具
[apktool](https://ibotpeaches.github.io/Apktool/)

[dex2jar](https://github.com/pxb1988/dex2jar)

[apktool修改版本](https://github.com/mosect/Apktool/)

## 注意
1. 不支持加固（加壳）apk
2. 资源res可以添加和修改，但是不能删除
3. java类可以添加和修改，不能删除；修改的最小单位是方法和字段

## 构建工具：
在项目目录下，执行：
```
gradlew outputProject
```
相关工具将会输出在**build/apk2gradle-xxx**目录下

不想构建工具？直接点击下载：

[V1.1.0](http://mosect.com/assets/apk2gradle/apk2gradle-1.1.0.zip)

## 更新记录
**V1.1.0**
```
1. 改用aar方式导入原apk资源文件，相关文件：res.aar
2. 修复smali合并错误问题
3. 弃用unknown.jar方式，改用resources目录存放
5. 更新apktool工具，此工具为修改后版本，增加参数 -r-txt 来输出aar的R.txt
```

## 使用工具：
进入工具目录，执行：
```
java -jar apk2gradle.jar export <apk_file_path> [output_dir]
```
之后输出相关gradle项目，可以使用AndroiStudio打开

## 二次开发
AndroidStudio打开apk2gradle导出的项目，在app/src/main/java目录，创建相关代码即可。
**相同类下的方法、字段会替换原本dex的方法和字段**

## 支持
个人博客：http://mosect.com
