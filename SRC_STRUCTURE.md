# 源代码包结构说明

## 项目整体结构
```
src/
├── client/                 # 客户端主包
│   ├── ui/               # UI界面相关类
│   │   ├── UIComponentFactory.java    # UI组件工厂类
│   │   ├── LoginRegisterUI.java       # 登录注册界面管理类
│   │   └── ChatMainUI.java           # 聊天主界面管理类
│   ├── network/          # 网络通信相关类
│   │   └── NetworkManager.java        # 网络管理器
│   ├── handler/          # 消息处理相关类
│   │   └── MessageHandler.java       # 消息处理器
│   └── ChatClient.java              # 客户端主类
├── server/               # 服务器端相关类
│   └── ChatServer.java              # 服务器主类
└── common/               # 公共类
    ├── Message.java                 # 消息实体类
    └── Group.java                   # 群组实体类
```

## 各包详细说明

### client.ui 包
**职责**: 负责所有用户界面的创建、管理和交互

- **UIComponentFactory.java**
  - 提供静态方法创建统一风格的UI组件
  - 包括按钮、文本框、密码框、圆角面板等
  - 实现了界面样式的统一管理

- **LoginRegisterUI.java**
  - 管理登录和注册界面的组件
  - 处理登录注册相关的用户交互
  - 与主控制器进行数据交换

- **ChatMainUI.java**
  - 管理聊天主界面的所有组件
  - 处理聊天界面的用户交互
  - 维护用户列表和群组列表的显示

### client.network 包
**职责**: 负责与服务器的网络通信

- **NetworkManager.java**
  - 管理与服务器的Socket连接
  - 处理消息的发送和接收
  - 提供连接状态管理和异常处理

### client.handler 包
**职责**: 负责处理从服务器接收的消息

- **MessageHandler.java**
  - 根据消息类型进行分类处理
  - 更新UI界面的相应部分
  - 处理各种业务逻辑响应

### client 包
**职责**: 客户端主控制器，协调各模块工作

- **ChatClient.java**
  - 程序入口点
  - 协调UI、网络、消息处理模块
  - 提供对外的公共接口

### server 包
**职责**: 服务器端实现

- **ChatServer.java**
  - 服务器主类
  - 处理客户端连接
  - 管理用户会话和消息转发

### common 包
**职责**: 提供客户端和服务器共享的数据结构

- **Message.java**
  - 消息实体类，定义消息格式和类型
  - 实现Serializable接口支持网络传输

- **Group.java**
  - 群组实体类，定义群组结构
  - 实现Serializable接口支持网络传输