**Version：facelib 2.1.0.1**

***
## 目录
* [一.FaceSDK集成](#一.FaceSDK集成)
	* [1.1添加三方依赖库](#1.1添加三方依赖库)
	* [1.2需要如下权限](#1.2需要如下权限)
* [二.接口说明及示例](#二.接口说明及示例)
	* [2.1人脸数据库的增删改查](#2.1人脸数据库的增删改查)
		* [2.1.1人脸数据库的创建](#2.1.1人脸数据库的创建)
		* [2.1.2人脸数据库的添加](#2.1.2人脸数据库的添加)
		* [2.1.3人脸数据库的更新](#2.1.3人脸数据库的更新)
		* [2.1.4人脸数据库的查询](#2.1.4人脸数据库的查询)
		* [2.1.5人脸数据库的保存到本地](#2.1.5人脸数据库的保存到本地)
		* [2.1.6人脸数据库的清除](#2.1.6人脸数据库的清除)
	* [2.2人脸检测参数配置](#2.2人脸检测参数配置)
		* [2.2.1人脸检测配置参数](#2.2.1人脸检测配置参数)
		* [2.2.2人脸数据类](#2.2.2人脸数据类)
		* [2.3相机/video人脸检测](#2.3相机/video人脸检测)
		* [2.3.1人脸sdk创建](#2.3.1人脸sdk创建)
		* [2.3.2设置相机预览数据](#2.3.2设置相机预览数据)
		* [2.3.3检测数据获取](#2.3.3检测数据获取)
		* [2.3.4人脸检测的销毁](#2.3.4人脸检测的销毁)
	* [2.4单张图片的人脸检测](#2.4单张图片的人脸检测)
		* [2.4.1人脸检测创建](#2.4.1人脸检测创建)
		* [2.4.2人脸检测初始化](#2.4.2人脸检测初始化)
		* [2.4.3人脸检测接口](#2.4.3人脸检测接口)
## 一.FaceSDK集成

### 1.1添加三方依赖库
在project的build.gradle中添加
```java
allprojects {
    repositories {

        google()
        jcenter()
        maven {url "http://mvnrepo.rokid-inc.com/nexus/content/repositories/snapshots/"}
        maven {url "http://mvnrepo.rokid-inc.com/nexus/content/repositories/releases/"}
    }
}
```

在app的build.gradle中添加依赖
```java
dependencies {
    implementation 'com.rokid:facelib:2.1.0.1-SNAPSHOT'
}
```


### 1.2需要如下权限
网络权限：
```java
<uses-permission android:name="android.permission.INTERNET"/>
```

读取外部存储权限：
```java
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE/>
```
相机权限：
```java
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
```
---
## 二.接口说明及示例
1、人脸数据库增删改查人脸信息
2、相机预览数据人脸检测，人脸跟踪，人脸识别。能够获取人脸角度以及人脸人脸质量等信息
3、单帧图片人脸检测，支持bitmap、NV21格式数据人脸检测
### 2.1人脸数据库的增删改查
#### 2.1.1人脸数据库的创建
```java
dbCreator = new FaceDbHelper(getApplicationContext());
dbCreator.setModel(FaceDbHelper.MODEL_DB);
//设置数据库的名称
dbCreator.configDb("user.db");
```
#### 2.1.2人脸数据库的添加
```java
UserInfo info = new UserInfo("name", "card no");
                        Bitmap bm = BitmapFactory.decodeFile("sdcard/test.jpg");
String uuid = dbCreator.add(bm, info).uuid;
```
#### 2.1.2人脸数据库的删除
```java
dbCreator.remove(uuid);
```
#### 2.1.3人脸数据库的更新
```java
Bitmap bm = BitmapFactory.decodeFile("sdcard/test.jpg");
dbCreator.update(bm, newInfo);
```
#### 2.1.4人脸数据库的查询
```java
dbCreator.queryBySize(0,10);
```
#### 2.1.5人脸数据库的保存到本地
数据库文件会储存在/sdcard/facesdk/ 目录下
人脸数据库和算法数据库分别对应文件"user.db"和"SearchEngine.bin"
使用人脸识别功能时需将这两个文件拷贝至需要的设备中
```java
dbCreator.save();
```
#### 2.1.6人脸数据库的清除
```java
dbCreator.clearDb();
```
---
### 2.2人脸检测参数配置
#### 2.2.1人脸检测配置参数
```java
DFaceConf conf = new DFaceConf();
conf.setSize(width, height); // 设置数据宽高
conf.setRoi(rect); // 设置检测roi区域
conf.setDataType(type) // 设置数据格式 DataFormat 
SFaceConf conf = new SFaceConf();
conf.setRecog(true, dbId); // 人脸识别开关开，dbId数据库引擎id
DataFormat.DATA_BGR // bgr图片数据
DataFormat.DATA_BITMAP // bitmap数据
DataFormat.DATA_YUV420 // camera nv21数据
```
#### 2.2.2人脸数据类
```java
DataFormat.DATA_BGR // bgr图片数据
DataFormat.DATA_BITMAP // bitmap数据
DataFormat.DATA_YUV420 // camera nv21数据 
FaceModel {
   int width;
   int height;
   List<FaceDO> faces; //人脸检测数据model，包含FaceDO list
}

FaceDO {
    public RectF faceRectF; // 人脸rect
    public long trackId;  // trackId 人脸trackId，tracking中id不变
    public byte[] UUID;   // 人脸UUID
    public float quality; //人脸质量
    public UserInfo userInfo; //人脸信息
    public float[] pose; //人脸角度
    public float userInfoScore; //人脸识别的分数
    public float sharpness; //人脸清晰度
}
```
---
### 2.3相机/video人脸检测
#### 2.3.1人脸sdk创建
```java
videoFace = VideoRokidFace.create(context,videoDFaceConf);
videoFace.sconfig(sFaceConf);
```
  * **返回：**

      `IVideoRokidFace` - 相机预览数据检测接口
#### 2.3.2设置相机预览数据
```java
videoFace.setData(new VideoInput(bytes));
```
#### 2.3.3检测数据获取
```java
 videoFace.getBytes();
```
#### 2.3.4人脸检测+人脸跟踪
```java
    videoFace.startTrack(new RokidFaceCallback() {
        @Override
        public void onFaceCallback(FaceModel model) {
        
        }
    });
```
#### 2.3.4人脸检测的销毁
```java
 videoFace.destroy();
```
---
### 2.4单张图片的人脸检测
#### 2.4.1人脸检测创建
```java
  IImageRokidFace imageFace = ImageRokidFace.create(context);
```
 * **返回：**

      `IImageRokidFace ` - 单帧图片检测接口
#### 2.4.2人脸检测初始化
```java
// 如果图片检测需要人脸识别，则sconfig
imageFace.sconfig(new SFaceConf().setRecog(true, dbId)); 
```
#### 2.4.3人脸检测接口
```java
imageFace.setImageFaceCallback(new BitmapInput(bitmap),
	new ImageRokidFace.ImageFaceCallBack() {
	    @Override
	    public void onFaceModel(FaceModel model) {

	    }});
```
* **入参：**

    `input` - `BitmapInput`类型，包含Bitmap
* **返回：**

    `model` - `FaceModel` 包含`FaceDo`类型
#### 2.4.4 人脸检测销毁
```java
 imageFace.destroy();
```
