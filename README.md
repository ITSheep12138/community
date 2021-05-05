# community

#### 介绍
学习仿牛客网讨论区项目，可以用来学习或者基于此项目再次定制属于自己的小圈子。

#### 软件架构
主要使用spring boot集成开发。此系统为一个单体应用。

前端使用thymeleaf。

后端使用spring boot, mybatis, rabbitmq, aop, caffeine cache

数据库使用到了MySQL，非关系型（NoSql)为redis。

集成邮件系统。

#### 安装教程

1.  初始化MySQL数据库

    建表SQL为init_schema.sql
    
    初始数据为init_data.sql

2.  redis安装

    可以自行选择平台安装，本人使用阿里云，采用docker容器部署。

    - 搜索镜像
     `docker search redis`
    - 拉取redis
     `docker pull redis:6.0`
    - 创建容器，设置端口映射
     `docker run -id --name=c_redis -p 6379:6379 redis:6.0`
    - 最后测试是否安装成功

3.  RabbitMQ安装
    
    - 拉取镜像
     `docker pull rabbitmq`
    - 创建容器，设置端口映射
      `docker run -d -p 5672:5672 -p 15672:15672 38e57f281891`
      
       说明：5672为服务端口，15672为web控制台端口 38e57f281891为镜像id
    - 最后测试是否安装成功
4.  配置属性
    最后就是将对应的配置文件配置完成即可。


#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request

