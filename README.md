商业版本：https://doc.yfhl.net/

官方网站：https://www.jeedocm.com?plan=ghdoc

安装视频：https://www.ixigua.com/7141227222257533471?utm_source=xiguastudio

# 介绍
一款多角色在线文档管理系统，系统集成了用户管理、角色管理、部门管理、文档管理、新闻管理、问答管理、通告管理、文档全文检索。
支持常用的office文档，视频文件、PDF文档在线预览，下载，笔记，评论。

# 技术栈
SpringBoot / Redis / Shiro / Vue / MySQL

# 产品功能

## 系统完善：完善的权限控制和用户系统
权限控制：基于Shiro和JWT开发的权限控制功能。    
用户系统：用户管理、部门管理、角色管理等。    

## 多角色：多角色支持    
用户端：全文检索文档，新闻消息、知识问答、我的资料、系统公告、个人中心等。    
1、全文检索可以通过检索文档内容查找相关资料。
2、查看管理发布的新闻消息详情。
3、可以回复相关问答，也可以发布自己的问答，管理员审核通过之后可以进行回复。
4、可以上传自己的资料信息，管理员审核通过之后可以被检索。
管理端：文档管理、新闻管理、知识问答管理、系统公告管理、系统设置。    
  


# 环境要求
JDK 1.8+  [点此下载](https://cdn.yfhl.net/java-win/jdk-8u181-windows-x64.exe)        
Mysql5.7+  [点此下载](https://cdn.yfhl.net/java-win/mysql-installer-community-5.7.31.0.msi)   
Redis3.2+  [点此下载](https://cdn.yfhl.net/java-win/Redis-x64-3.2.100.msi)   
LibreOffice  [点此下载](https://yf-commons-files.oss-cn-beijing.aliyuncs.com/java-win/LibreOffice_7.2.4_Win_x64.msi) 


# 快速运行
1、自行安装MySQL数据库（版本最好大于5.7），将`安装资源中`的`yf_doc_ky.sql`导入到安装好的数据库    
2、安装Java环境，要求JDK版本大于1.7   
3、安装LibreOffice 用于文件转换在线预览,注意安装目录不能有中文 
4、请修改外置配置文件：application-local.yml 改成您自己的MySQL配置，修改LibreOffice安装目录  
5、Windows通过start.bat运行，Linux运行start.sh运行    
6、如果无意外，可通过：http://localhost:8101 访问到项目了    
7、管理员账号密码：admin/admin 学员账号：student/student

注意：运行的时候运行包所在目录不能包含中文目录，会导致LibreOffice启动失败。

# 其它支持
官方QQ交流群：537163131      
邮箱：626264481@qq.com   
手机：18710213152    
网站：https://www.jeedocm.com/?plan=githubygq
