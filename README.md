# apk2gradle
Apk to gradle project
将apk转成gradle可开发项目

## 依赖工具
[apktool](https://ibotpeaches.github.io/Apktool/)

[dex2jar](https://github.com/pxb1988/dex2jar)

[Android-SmaliPlugin](https://github.com/Mosect/Android-SmaliPlugin)

## 注意
1. 不支持加固（加壳）apk
2. 资源res可以添加和修改，但是不能删除
3. java类可以添加和修改，不能删除；修改的最小单位是方法和字段

## 构建工具：
在项目目录下，执行（需要设置JAVA_HOME环境变量为JDK1.8）：
```
gradlew outputProject
```
相关工具将会输出在**build/apk2gradle-xxx**目录下

## 更新记录

### V2.1.0
1. 更改资源存储方式，名称含有特殊字符的资源才会放到res.aar，其他资源放在项目src/main/res之下
2. 更新了Android-SmaliPlugin版本为1.2.0-b1，支持Copy注解

### V2.0.2
1. 更新Android-SmaliPlugin为1.1.2
2. 其他细节优化


### V2.0.0-b1

1. 导出的项目改用[Android-SmaliPlugin](https://github.com/Mosect/Android-SmaliPlugin)，支持java+smali混合开发
2. 优化资源处理方式

## 已知问题

### V2.1.0
1. 新版本androidx对v31只了支持，引用了非原生rom资源，values-v31/colors.xml文件可能包含非AndroidSdk资源，需要手动修复出错位置

###V2.0.0-b1
1. 直接运行debug有可能无法编译smali代码，属于Android-SmaliPlugin问题，请在[Android-SmaliPlugin](https://github.com/Mosect/Android-SmaliPlugin)项目提issue


## 使用工具：
进入工具目录，执行：
```
java -jar apk2gradle.jar export <apk_file_path> [output_dir]
```
之后输出相关gradle项目，可以使用Androi Studio 4.+打开

## 二次开发
AndroidStudio打开apk2gradle导出的项目，在app/src/main/java目录，创建相关代码即可。
**相同类下的方法、字段会替换原本dex的方法和字段**

## 支持
个人博客：http://mosect.com
