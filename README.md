# JavaFX MES System

基于 JavaFX + Spring Boot 的跨平台 MES 系统，采用 RBAC 权限管理模式。

## ✨ 功能特性

- 🔐 **RBAC 权限管理** - 完整的用户-角色-权限体系，支持按钮级别权限控制
- 🎨 **VS Code 风格界面** - 现代化深色主题，美观易用
- 💾 **SQLite 数据库** - 轻量级无需额外安装数据库
- 🔄 **跨平台支持** - 支持 Windows、Linux、macOS
- 🔒 **密码加密** - SHA-256 加密存储
- 📱 **MVC 架构** - JavaFX 视图层 + Spring Boot 业务层分离
- 🐳 **Docker 支持** - 支持通过 noVNC 在浏览器中访问桌面

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+

### 本地运行

```bash
# 1. 编译打包
mvn clean package -DskipTests

# 2. 运行应用
java --add-modules javafx.controls,javafx.fxml -jar target/javafx-mes-1.0.0.jar
```

### 默认账号
| 用户名 | 密码 | 权限 |
|--------|------|------|
| admin | admin123 | 管理员（全部权限） |
| user | user123 | 普通用户 |

## 🐳 Docker 运行

### 方式一：Docker Compose（推荐）

```bash
# 构建并启动
docker-compose up --build -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

### 方式二：手动构建

```bash
# 构建镜像
docker build -t mes-desktop:latest .

# 运行容器
docker run -d --name mes-desktop \
  -p 5900:5900 \
  -p 6080:6080 \
  -e VNC_PASSWORD=mes123456 \
  mes-desktop:latest
```

### 访问方式

1. **浏览器访问（noVNC）**
   - 地址: http://localhost:6080
   - 密码: `mes123456`

2. **VNC 客户端访问**
   - 地址: localhost:5900
   - 密码: `mes123456`

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/mes/
│   │   ├── MesApplication.java       # Spring Boot 启动类
│   │   ├── JavaFxApplication.java    # JavaFX 启动类
│   │   ├── config/                   # 配置类
│   │   ├── controller/               # JavaFX 控制器
│   │   ├── entity/                   # JPA 实体
│   │   ├── repository/               # 数据访问层
│   │   ├── service/                  # 业务逻辑层
│   │   ├── dto/                      # 数据传输对象
│   │   ├── util/                     # 工具类
│   │   └── view/                     # 视图管理
│   └── resources/
│       ├── fxml/                     # JavaFX 界面
│       ├── css/                      # 样式文件
│       └── application.yml           # 应用配置
```

## 🎯 核心功能

### 1. 登录认证
- 用户名密码登录
- 错误提示
- 支持回车键快捷登录

### 2. 控制台 Dashboard
- 用户总数统计
- 角色总数统计
- 权限总数统计
- 当前用户权限列表

### 3. 用户管理
- ✅ 添加用户（支持设置密码）
- ✅ 编辑用户
- ✅ 删除用户
- ✅ 重置密码
- ✅ 分配角色
- ✅ 启用/禁用用户
- ✅ 按钮级别权限控制

### 4. 角色管理
- ✅ 添加角色
- ✅ 编辑角色
- ✅ 删除角色
- ✅ 分配权限
- ✅ 按钮级别权限控制

### 5. 权限管理
- ✅ 添加权限
- ✅ 编辑权限
- ✅ 删除权限
- ✅ 权限类型（菜单/按钮）

### 6. 计量单位管理
- ✅ 新增计量单位
- ✅ 编辑计量单位
- ✅ 删除计量单位
- ✅ 导出 Excel
- ✅ 按钮级别权限控制

### 7. 修改密码
- 验证原密码
- 设置新密码
- 密码强度校验

## 🔧 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.4 | 后端框架 |
| JavaFX | 21.0.2 | 前端UI框架 |
| SQLite JDBC | 3.45.2.0 | 数据库 |
| Spring Data JPA | 3.2.4 | ORM 框架 |
| AtlantisFX | 2.0.1 | UI 主题库 |
| Lombok | 最新 | 代码简化 |
| Apache POI | 5.2.5 | Excel 导出 |

## 🎨 界面预览

### 登录界面
- VS Code 深色主题
- 无边框窗口
- 窗口拖拽支持

### 主界面
- 左侧侧边栏导航
- 动态权限菜单
- 用户信息展示
- 控制台数据看板
- 全屏模式保留系统任务栏

### 数据表格
- 深色主题表格
- 内联操作按钮
- 响应式布局

## ⚙️ 配置说明

配置文件位于 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:mes.db          # 数据库文件路径
  jpa:
    hibernate:
      ddl-auto: update                # 自动创建/更新表结构
    show-sql: false                   # 是否显示SQL
```

## 📝 开发说明

### 新增权限
1. 在 `DataInitializer.java` 中添加权限
2. 在界面中控制按钮/菜单可见性：
   ```java
   authService.hasPermission("permission:name")
   ```

### 新增界面
1. 创建 FXML 文件
2. 创建对应的 Controller
3. 在 `MainController` 中添加加载方法

## 🐛 已修复的 Bug

1. ✅ 退出登录后无法正确返回登录界面
2. ✅ 重置用户密码后登录失败
3. ✅ 全屏模式看不到桌面状态栏
4. ✅ 权限管理未细化到按钮级别
5. ✅ 添加用户时没有密码输入框

## 🐛 常见问题

### 1. JavaFX 模块问题
运行时添加模块参数:
```
--add-modules javafx.controls,javafx.fxml
```

### 2. Docker GUI 显示问题
- 使用 noVNC 浏览器访问: http://localhost:6080
- 或使用 VNC 客户端连接: localhost:5900

### 3. 数据库文件
数据库文件 `mes.db` 会在第一次运行时自动创建。

## 📄 许可证

MIT License
