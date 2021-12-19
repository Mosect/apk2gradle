# apk2gradle
Apk to gradle project
将apk转成gradle可开发项目

## 依赖工具
[apktool](https://ibotpeaches.github.io/Apktool/)

[dex2jar](https://github.com/pxb1988/dex2jar)

## 注意
1. 不支持加固（加壳）apk，以后也不提供相关脱壳工具
2. 资源res可以添加和修改，但是不能删除
3. java类可以添加和修改，不能删除；修改的最小单位是方法和字段
4. 输出的

## 构建工具：
在项目目录下，执行：
```
gradlew outputProject
```
相关工具将会输出在**build/apk2gradle-xxx**目录下

## 使用工具：
进入工具目录，执行：
```
java -jar apk2gradle.jar export <apk_file_path> [output_dir]
```
之后输出相关gradle项目，可以使用AndroiStudio打开

## 支持
个人博客：http://mosect.com
