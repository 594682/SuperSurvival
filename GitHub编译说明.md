# 使用 GitHub Actions 自动编译「超级生存模组」

**版本：** Minecraft Java 1.20.1 + Forge 47.3.0 + Java 17

## 在 GitHub 网页上操作（无需本地配置编译环境）
1. 把下载的 ZIP **解压**。打开文件夹 `SuperSurvival-Forge-1.20.1-GitHub修复版`。
2. 登录 GitHub，点击 **New repository**，仓库名可以是 `SuperSurvival`，建议先选择 **Private**。
3. 在仓库中点击 **Add file → Upload files**，将解压后文件夹**里面的内容**上传，
   务必包括隐藏目录 `.github/workflows/build.yml`，以及 `src`、`scripts`、`build.gradle`、
   `settings.gradle` 和 `gradle.properties`。不要只上传 ZIP。
   如果网页上传时漏了 `.github`，可以用 **Add file → Create new file**
   手动创建路径 `.github/workflows/build.yml`，粘贴下载包内对应文件内容。
4. 点击 **Commit changes**。
5. 打开仓库上方 **Actions** → 左侧 **Build Super Survival (Forge 1.20.1)**
   → **Run workflow** → 运行。
   首次 Push 到 `main` 时，工作流也会自动触发。
6. 如果显示绿色 ✔，打开那次运行页面最下方 **Artifacts**
   → 下载 `SuperSurvival-Forge-1.20.1-JAR`。
   解压 Artifacts ZIP，把里面的 `supersurvival-2.1.0.jar` 放入 `mods` 文件夹。
7. 如果显示红色 ✖，点开 **Compile Forge mod and capture log** 查看错误，
   并下载 `SuperSurvival-build-error-log`，将报错内容发回来，可以继续逐条修复。

## 本次已修改
- 将错误的 1.21+ `SavedData.Factory` 调用替换为 Forge 1.20.1 可用的三参数 `computeIfAbsent`。
- 修复“多个空桶变一个岩浆桶”的消耗逻辑。
- 传送门防止主手/副手重复处理。
- 出生点简易聚落中的床由错误的单方块改成双方块床，并在建造成功后记录完成。
- 村民物品交易数量按 1.20.1 的物品最大堆叠上限处理（无法完整保留原来要求的 67 个）。
- 保留原头像 `src/main/resources/logo.png`、死亡不掉落及 LLL 权限快捷键。
- 增加 GitHub Actions 编译和下载失败日志流程。

## 已知限制
- 这里没有真正执行过 ForgeGradle 编译；GitHub 首次构建仍可能发现其他问题。
- 出生村庄为自建简易房屋和村民，不是完整的原版村庄结构。
- Shift 在原版用于下车，与矿车下降按键存在冲突。
- 1.20.1 没有重锤或风爆附魔，重锤暂用下界合金锄代替。
- 原版末影龙骑乘/飞行不保证正常，仍需专门的游戏内测试。
- “死亡不掉落”不会解除极限模式死亡后无法正常复活的限制。
- 模组会更改世界规则和出生点附近方块；**请备份存档**。
- LLL 只允许单人世界所有者启用管理员命令；不会自动切换游戏模式，
  在多人服务器不会开放给普通玩家。

## 官网
https://www.bilibili.com/video/BV1GJ411x7h7
