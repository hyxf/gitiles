## 基于gradle的gitles项目

> gitiles 2.0-5

### 源码改造[2步]：

1. 将原gitiles内，java,resource目录复制到 src/main 目录内

2. 若运行出现组件依赖问题，分析 原gitiles内 WORKSPACE 与 build.gradle，进行版本升级

### 运行[gradle]

~~~bash
gradle clean run
~~~

Mac 服务配置【支持开机启动】

> com.gutils.gitiles.plist

### 运行[单独 jar]

Download [the latest JAR](https://github.com/hyxf/gitiles/releases/latest)

运行

~~~bash
~ java -jar gitiles-1.0.0.jar
usage: gitiles -d <arg> [-i <arg>] [-p <arg>] [-t <arg>] [-u <arg>]

gitiles

 -d,--dir <arg>     git mirror directory
 -i,--ip <arg>      ip
 -p,--port <arg>    port
 -t,--title <arg>   Web title
 -u,--url <arg>     git url

make it easy!
~~~

构建jar 

~~~bash
gradle clean jar
~~~

------------------------------

### 支持 git http server

> 类：DevServer.java
>
> 方法：appHandler

~~~java
    //----- 支持 git http server
    ServletHolder gitHolder = handler.addServlet(GitServlet.class,"/git/*");
    Map<String, String> params = new HashMap<String,String>();
    params.put("base-path",cfg.getString("gitiles",null,"basePath"));
    params.put("export-all",cfg.getString("gitiles",null,"exportAll"));
    gitHolder.setInitParameters(params);
    //-----
~~~