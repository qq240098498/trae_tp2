# 快递代收点管理系统

小区/校园快递代收点管理系统，包含包裹入库、取件码生成、客户取件、包裹查询、滞留提醒、统计分析、代寄件登记等功能。

## 技术栈

- 后端：Spring Boot 3.x + JPA + H2
- 前端：Vue 3 + Vite + Element Plus
- 部署：Docker + Docker Compose

## 快速启动

### Docker方式（推荐）

```bash
# 构建并启动
docker-compose up -d --build

# 查看日志
docker-compose logs -f

# 停止
docker-compose down
```

启动后访问：
- 前端界面：http://localhost
- 后端API：http://localhost:8080/api
- H2控制台：http://localhost:8080/h2-console

### 本地开发

#### 后端
```bash
cd backend
mvn spring-boot:run
```

#### 前端
```bash
cd frontend
npm install
npm run dev
```

## 功能模块

1. **首页仪表盘** - 今日入库、待取件、滞留件、待寄件统计
2. **包裹入库** - 录入快递信息，自动生成取件码
3. **客户取件** - 取件码/手机号查询，确认取件
4. **包裹查询** - 多条件筛选查询
5. **滞留管理** - 超过48小时未取件提醒
6. **数据统计** - 每日入库量统计图表
7. **代寄件登记** - 寄件信息录入
8. **代寄件管理** - 寄件列表，标记已寄出
