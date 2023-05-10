## 项目说明

> 项目地址：https://github.com/lisiwei1/logSystem

此demo项目用AOP统一日志管理，并接入ELK实现日志秒查，可以根据日志任意字段进行查询，其中还可以实现不同节点间的链路追踪

默认日志格式如下，如有需要可以自行修改代码。因为开发者技术水平有限，若有bug或者不合理的地方请见谅！

```json
{
  "classMethod": "api",
  "hostName": "LAPTOP-572VT37S",
  "consumeTime": 57,
  "hostIp": "192.168.0.118",
  "responseTime": "2023-05-06 13:39:35.840",
  "requestParams": "{\"code\":\"22\"}",
  "serverName": "admin-server-01",
  "requestIp": "127.0.0.1",
  "className": "com.logdemo.test.web.TestController",
  "httpMethod": "GET",
  "url": "http://127.0.0.1:20230/test/api",
  "sqls": [],
  "requestTime": "2023-05-06 13:39:35.783",
  "responseParams": "调用成功！",
  "traceToken": "e9ff6423-d14c-470e-be32-3f953f89b2f3",
  "currentOrder": 1,
  "desc": "测试API接口"
}
```





## 效果演示



控制台输出：

![image](https://user-images.githubusercontent.com/44285123/236731292-3a585aaf-7771-41b5-9cb3-ec18fec997d3.png)


ELK界面（下面会讲如何进行ELK配置和简单使用）

![image](https://user-images.githubusercontent.com/44285123/236731300-f7cc672d-9eac-43dd-a53a-7b38982fc690.png)


## 使用说明

直接启动项目，执行请求127.0.0.1:20230/test/getMethod，然后可以到控制台查看日志输出





### POM文件配置

若不需要用到ELK则可以去掉logstash-logback-encoder模块，若不用到WEB也可以去掉spring-boot-starter-web，因为此日志系统不仅仅记录web请求日志，还可以记录定时任务的日志，甚至还可以记录任意方法的执行日志，只要对应方法加上@LogOperation注解即可，但要注意的是当前项目有个bug。比如controller标有@GetMapping的方法A调用Service层的B方法，而且B方法有@LogOperation注解，那么只会记录B方法的日志，A方法的日志直接被B方法日志覆盖，应该是因为日志是通过threadlocal实现的，因此同一个线程下执行的方法都会被最后执行的方法被覆盖。因此要注意下此问题。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.aspectj</groupId>
    <artifactId>aspectjweaver</artifactId>
    <version>1.9.7</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
</dependency>
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>30.0-jre</version>
</dependency>
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
</dependency>
<!--集成logstash-->
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>6.6</version>
</dependency>
```



### springboot的启动类

启动类请加上@EnableAsync以实现异步打印日志

![image](https://user-images.githubusercontent.com/44285123/236731337-5b7c6d83-3b0a-4ce8-b7c2-64637e6fbb0d.png)


### 配置文件

application.yml上加上此配置，用于记录当前服务的名称，每一个服务应该都不一样，方面后面区分和查询

![image](https://user-images.githubusercontent.com/44285123/236731352-942be19c-61ad-44f5-95a4-1ce2683bac32.png)


logback.xml文件若用不上ELK则直接将右边两处的红框的内容删掉即可。其中<destination>127.0.0.1:4560</destination>是指logstah的地址和端口，根据实际情况进行修改。

![image](https://user-images.githubusercontent.com/44285123/236731362-160f8523-f20f-40d9-9aa0-a8218dde77fe.png)


### AOP核心模块

日志系统核心代码就是core包下这块，其中LogAspect类就是设置切点，有需要可以自行修改源码修改实现细节

![image](https://user-images.githubusercontent.com/44285123/236731375-d9ac6f20-fb9a-49ac-afbf-3199ec2a85af.png)


### ELK配置

使用ELK记得将logback.xml的那两处注释取消掉

自行安装ELK（elasticsearch、Logstash和kibana），此处使用的是7.15版本，而且项目和ELK都是本机环境下运行的，如果不是请自行修改对应配置的地址和端口。

![image](https://user-images.githubusercontent.com/44285123/236731401-d07ed208-ae0d-40e2-a878-eb6489b71890.png)


#### ELK文件配置

在logstash的bin目录下新增logstash.conf文件

![image](https://user-images.githubusercontent.com/44285123/236731416-259681b5-bf6b-43d5-8524-2e4150de95d8.png)
往文件里面添加下面内容。其中input-tcp下的host和port要跟logbacj.xml中的destination配置一致。

然后修改output-elasticsearch-index的内容，即"weblog-info-test-%{+YYYY.MM.dd}"，自定义索引名称请改weblog-info-test这部分，此处是设置日志索引名称的，后面创建索引模式需要用到，到时需要用到 weblog-info-test-*

```
input {
  tcp {
    mode => "server"
    host => "0.0.0.0"
    port => 4560
    codec => json_lines
  }
}

filter {
  json {
    # 不加这段会只有message字段，值都到message里面，没有各个字段的索引
    source => "message"
    remove_field => ["message"]
  }
}

output {
  elasticsearch {
    hosts => "127.0.0.1:9200"
    index => "weblog-info-test-%{+YYYY.MM.dd}"
  }
}
```



#### ELK启动

```
1.运行elasticsearch
双击执行bin目录下的elasticsearch.bat
启动后可以打开http://localhost:9200查看是否启动成功

2.运行Logstash
切换到bin目录，在地址栏输入cmd回车后，执行logstash -f logstash.conf

3.运行kibana
执行bin目录下的kibana.bat
然后打开http://localhost:5601/，此界面就是ELK的使用界面
```



#### ELK使用
ELK是可以设置密码的，有需要请自行设置

启动ELK后，用浏览器打开http://localhost:5601/

点击左侧的stack management

![image](https://user-images.githubusercontent.com/44285123/236731440-1a114e80-d849-45e5-9eb2-fb6f7022f16d.png)
然后点击索引模式，再点击右上角的【创建索引模式】

![image](https://user-images.githubusercontent.com/44285123/236731575-e18ef1cb-6c42-4f6a-a2e4-ad25e557cefb.png)
然后创建索引模式

![image](https://user-images.githubusercontent.com/44285123/236731588-f673da1a-2257-4bc9-a11a-8b7aa0b20c56.png)
然后打开Discover选择刚刚创建的索引模式

![image](https://user-images.githubusercontent.com/44285123/236731601-0b0ef38b-2d5a-4889-ad98-e4d63d430792.png)
![image](https://user-images.githubusercontent.com/44285123/236731610-7b274427-c2b9-4424-b35e-e2e68629a8e3.png)


然后就可以在ELK上愉快的查询日志了，可以指定时间，指定日志里面的字段进行查询，而且是秒查。

![image](https://user-images.githubusercontent.com/44285123/236731614-64903c17-12e3-4cbc-8766-cef4344cfed1.png)


## 链路追踪

使用tracetoken进行查询，就可以查到一个请求的完整链路，而且还可以根据currentOrder字段查看调用顺序，1为首次请求，2为第二次，依次递加。

![image](https://user-images.githubusercontent.com/44285123/236731626-f644d0c4-aca0-4695-afa0-490a34fbfd0f.png)


用于跟踪请求在不同节点之间的调用链，直接在ELK上选择tracetoken（需要此功能在进行http调用的地方进行代码配置）

原理：每一个日志都会自动判断请求头有没有带traceToken字段，若没有则生成一个UUID，currentOrder也是如此，没有则设置为1.若此请求还调用其他服务节点，则在调用前header上附上当前tracetoken和currentOrder。



### 链路追踪配置

比如在创建HTTP请求时，先在header附上tracetoken和currentOrder。请参考下面代码：

```java
private HttpHeaders createHttpHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentTraceToken());
    httpHeaders.add(LogVariableKey.TRACE_TOKEN, LogPackageHolder.getCurrentOrder().toString());
    return httpHeaders;
}
```



## SQL语句日志

此项目并未记录SQL日志，如有需要，请调用下面方法将SQL信息添加到日志

```java
LogPackageHolder.addSQL(sqltext); // sqltext指具体sql信息
```



实际效果

![image](https://user-images.githubusercontent.com/44285123/236731896-e1e47499-127a-4085-abc1-78de5ae6a343.png)

## 注意事项（必读）



### 跳过日志记录

#### 注解方式

如果一些请求入参或者出参过大，不想记录，或者指定一些方法都不记录日志，直接添加@LogOperation注解来实现，比如@LogOperation(value = "测试API接口", skipReq= true)这个注解就记录当前注解所在的方法的方法描述为 "测试API接口"，对应日志的desc字段，而skipReq= true表示此方法跳过日志入参，skipLog= true则是不记录日志。

```java
/**
 * 日志注解
 * @Author lsw
 * @Date 2023/5/6 13:04
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    String value() default "";//接口名字

    boolean skipLog() default false;//不记录整体日志

    boolean skipReq() default false;//不记录请求日志

    boolean skipRsp() default false;//不记录响应日志

    boolean skipSql() default false;//跳过sql

}
```



#### 配置文件方式

直接指定一些类跳过日志记录，在此处代码添加即可。

![image](https://user-images.githubusercontent.com/44285123/236731670-0a7e5629-10da-40b2-8a07-318ffcd004ba.png)


#### 注解改代码

只记录某些注解下的日志或者不记录某些注解下的，比如指记录Get方法的日志，或者只记录@Scheduled下定时任务的日志

那直接修改代码，找到core.log包下的LogAspect文件进行修改。

比如不记录标有@GetMapping下的方法，那就删除对应切点（下图红框处）

![image](https://user-images.githubusercontent.com/44285123/236731678-b042a139-77ad-4349-a375-2143a6910f8f.png)


### 获取方法名称/说明

获取到方法说明写入日志的desc字段

![image](https://user-images.githubusercontent.com/44285123/236731690-47f490cc-7f21-4449-b560-258f0aabeeed.png)




#### 直接修改代码逻辑

举例：我已经用了swagger的注解给web接口写上了，我想获取这些注解里面的注释写入日志里面要怎么做？

那请看core.log包下的LogAspect文件下的getAnnotationText方法，这段代码逻辑是根据方法获取指定注解的内容。

@LogOperation的value值优先级最高。

![image](https://user-images.githubusercontent.com/44285123/236731705-b967437c-1249-459d-a7c2-0ac03cab5bea.png)


若要获取swagger注解@ApiOperation("查询在线用户")的信息

![image](https://user-images.githubusercontent.com/44285123/236731978-65e63c30-dca9-47ef-984e-4180b91ca46c.png)
在下图所指位置添加这段代码就行

```java
ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
if (apiOperation != null) {
    logPackage.putVariable(LogVariableKey.DESC, apiOperation.value());
}
```

![image](https://user-images.githubusercontent.com/44285123/236731718-6343e8cd-ddf9-43be-8545-160990766ed9.png)




#### 添加配置类实现（有缺陷）

如下图，比如有个注解@MethodDesc，那新增一个配置类，把指定注解传进去，那么AOP就能获取这个注解的信息

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MethodDesc {

    // 方法说明
    String value() ;

    String note() default "";

    String[] tags() default "";

}
```

```java
@Configuration
public class LogConfig {

    @Bean
    public LogConfiguration logConfiguration(){
        LogConfiguration configuration = new LogConfiguration<>();
        configuration.setAnnotationClass(MethodDesc.class);
        return configuration;
    }

}
```



但是不能获取注解的指定信息，只能获取注解的整体信息。因此最好按照上面方式修改代码

![image](https://user-images.githubusercontent.com/44285123/236731745-7a7834f6-c6b2-45e7-a47c-7ef6ed15376d.png)
![image](https://user-images.githubusercontent.com/44285123/236731748-615514c8-2bce-47a2-88b3-bea0a13af731.png)
