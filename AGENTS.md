# mall-ai 项目规则

## 技术栈（严格遵守，不得引入新依赖，除非用户明确同意）
- JDK 21 / Spring Boot 3.3.5 / Maven
- MyBatis-Plus 3.5.9（boot3 starter + mybatis-plus-jsqlparser 伴生包，分页插件需要）/ MySQL 8 / Redis 7（07 章引入）
- 认证 Sa-Token 1.39（08 章引入）/ 文档 Knife4j 4.5（09 章引入）/ Lombok + Hutool 5.8

## 项目结构
- 分层：Controller(参数校验/组装) → Service(业务) → Mapper(数据)，禁止跨层调用
- 位置：module/<域>/entity|mapper|service|controller|dto；通用底座在 common 包

## 编码规范
- 入参一律 DTO + jakarta.validation，出参一律 VO，禁止 Controller 直接出入实体
- 所有接口返回 Result<T>；业务异常统一 throw BusinessException(ErrorCode)
- 逻辑删除字段 deleted，乐观锁字段 version，create_time/update_time 自动填充
- 事务加在 ServiceImpl 方法上，事务内禁止远程调用与耗时 IO
- 注释规范：类与方法一律中文 Javadoc（职责/参数/返回值/关键取舍），非显而易见的逻辑加行内注释说明意图

## 常用命令
- 编译 mvn compile / 测试 mvn test / 启动 mvn spring-boot:run
- 本地 MySQL 127.0.0.1:3306/mall（root/root），Redis 127.0.0.1:6379

## 验收要求
- 每次改动后 mvn compile；涉及业务逻辑补测试并 mvn test
- 不许吞异常/注释代码来消除报错；完成与否以真实命令输出为准

