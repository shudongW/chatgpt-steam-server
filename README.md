# 简介
Open AI ChatGPT流式输出。Open AI Stream server. ChatGPT Stream server.

**此项目只是一个简单示例项目，实现流式输出，仅做参考。大家最好还是自己基于SDK动手实现**
---
### 目前本项目支持两种流式输出，基于[ChatGPT-Java SDK](https://github.com/shudongW/chatgpt-java-main) 。

流式输出实现方式 | 小程序 | 安卓 | ios | H5
---|---|---|---|---
SSE参考：[OpenAIEventSourceListener](https://github.com/shudongW/chatgpt-steam-server/blob/main/src/main/java/com/tech/chatgpt/listener/OpenAIEventSourceListener.java) | 不支持| 支持| 支持 | 支持
WebSocket参考：[SocketIOListener](https://github.com/shudongW/chatgpt-steam-server/blob/main/src/main/java/com/tech/chatgpt/listener/SocketIOListener.java) | 支持| 支持| 支持| 支持
---
**最新版SDK参考：https://github.com/shudongW/chatgpt-java-main

### 有bug欢迎朋友们指出，互相学习，所有咨询全部免费。

公众号 | 微信 | 知识星球
---|---|---
<img width="210" height="300" alt="二维码" src="https://raw.githubusercontent.com/shudongW/myself_img/main/gzh.jpg"> | <img width="210" height="300" alt="二维码" src="https://raw.githubusercontent.com/shudongW/myself_img/main/me.png"> | <img width="310" height="210" alt="二维码" src="https://raw.githubusercontent.com/shudongW/myself_img/main/xt.jpg">
---

# SSE
主要是基于[SSE](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events#event_stream_format) 实现的（可以百度下这个技术）。也是最近在了解到SSE。OpenAI官网在接受Completions接口的时候，有提到过这个技术。
Completion对象本身有一个stream属性，当stream为true时候Api的Response返回就会变成Http长链接。
具体可以看下文档：https://platform.openai.com/docs/api-reference/completions/create
<img width="1080" alt="1" src="https://raw.githubusercontent.com/shudongW/myself_img/main/gpt_client-1.gif">



## 依赖
最新版参考：https://github.com/shudongW/chatgpt-java-main
目前是1.0.16版本
```
<dependency>
    <groupId>com.unfbx</groupId>
    <artifactId>chatgpt-java</artifactId>
    <version>1.0.16</version>
</dependency>
```
# 项目部署

## 拉取源代码
```
git clone https://github.com/shudongW/chatgpt-steam-server
```
## Socket修改配置
com/tech/chatgpt/config/SocketIOConfig.java
```
host='你的服务器地址' // localhost
port='socket port'
```
修改application.properties文件
默认5300端口，可以自己修改
国内使用apiHostProxy代理地址可直接访问Openai
过滤采用的是百度的敏感词过滤
```
spring.web.resources.static-locations= classpath:/static/dist/
server.port=5300
gpt.channel=azure
chatgpt.apiKey=sk-9JzK5K3ubQUo3kVSDFWER4R4WRFEWR345WREWR32
chatgpt.apiHost=https://api.openai.com/
chatgpt.apiHostProxy=https://api.openai-proxy.com/
azure.apiKey=14c7DFSEFRWER234324324324EWRFWER
azure.apiHost=https://azure-oxxxt.openai.azure.com/
azure.apiPath.AI35=openai/deployments/{model_name}/chat/completions?api-version=2023-07-01-preview
azure.apiPath.Davinci003=openai/deployments/{model_name}/completions?api-version=2022-12-01
filter.baidu.appId=10000000
filter.baidu.apiKey=123456
filter.baidu.secretKey=safasfasfasfsafasf
```
## 运行
运行ChatgptSteamServerApplication
```
ChatgptSteamServerApplication
```
运行成功后打开浏览器：

```
http://localhost:5300/
```
能打开此页面表示运行成功
<img width="1080" alt="1" src="https://raw.githubusercontent.com/shudongW/myself_img/main/gpt_client-1.gif">


代码其实很简单，小伙伴们可以下载代码来看下。

