<div align="center">

# MINEZ CoreProtect

面向 MINEZ 服务器维护的 [CoreProtect](https://github.com/PlayPro/CoreProtect) Fork。

<a href="README.md">English</a> ｜
<a href="README_zh-CN.md">简体中文</a>

<br>

<div>
<a href="https://github.com/PlayPro/CoreProtect"><img src="https://img.shields.io/badge/upstream-25.0-2196f3" alt="upstream"></a>
<a href="#版本号"><img src="https://img.shields.io/badge/version-25.0.3-4caf50" alt="version"></a>
<a href="#受支持的版本"><img src="https://img.shields.io/badge/Paper-26.3-4caf50" alt="Paper"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-Artistic%202.0-9e9e9e" alt="license"></a>
</div>

<br>

<a href="https://docs.coreprotect.net/">查阅文档</a> ｜
<a href="https://github.com/MINEZ/CoreProtect/issues">问题反馈</a> ｜
<a href="https://github.com/MINEZ/CoreProtect/releases">版本发布</a> ｜
<a href="#相对上游的改动">改动一览</a>

</div>

> [!IMPORTANT]
> **本项目是非官方 Fork，与 PlayPro 及 CoreProtect 团队没有关系，既未经其认可，也不由其提供支持。**
>
> 与本 Fork 有关的一切，请提交至[本仓库的 issue](https://github.com/MINEZ/CoreProtect/issues)，不要带到上游仓库、官方 Discord 或 Patreon。
>
> 区分方法是先在未经修改的上游构建上复现：
>
> - **脱离本 Fork 仍会出现** —— 属于上游的问题，照常向上游反馈，不必也不要提及本 Fork。
> - **脱离本 Fork 便不再出现** —— 属于我们的问题，只应提交到这里。

## 关于

本仓库跟随上游 `master` 分支，仅承载 MINEZ 服务器所需的少量改动。除下文列出的内容外，其余部分与上游完全一致。

CoreProtect 本身是数据记录与反破坏工具：记录方块、容器、实体与玩家的活动，供管理人员检查、查询、回滚与恢复。命令、权限、配置与 API 见 [docs.coreprotect.net](https://docs.coreprotect.net/)，除下文新增的内容外，那里的文档同样适用于本 Fork。

### 受支持的版本

以 Paper 26.3 构建并运行，这也是本 Fork 唯一部署的服务端。上游对 Spigot、Folia 与旧版本的兼容照原样保留，但在这里未经测试。

以合并方式跟随上游：

```bash
git fetch upstream
git merge upstream/master
```

## 相对上游的改动

| 名称 | 类型 | 改动 |
| --- | --- | --- |
| `m:` / `meta:` / `metadata:` | 查询参数 | 新增 |
| `i:` / `e:` 中的 `#命名空间:标签` | 查询参数 | 修改 |

**元数据筛选。** `m:` 把查询结果限定为保存的元数据中含有给定文本的物品。要找出带有某个命名空间 ID 的物品（自定义魔咒、数据组件或持久化数据的键）只能用它，现有的参数都不看物品内部。它之所以可行，是因为 Bukkit 序列化元数据时会把这些键以 UTF-8 原文写入，所以匹配就是在保存的字节中查找子串。可以用逗号分隔多个值，含有其中任何一个即算匹配。每个值至少 3 个字符，免得几乎什么都能匹配上。

```
/co lookup r:#global t:30d i:enchanted_book m:minez:eternal_covenant
```

`meta:` 与 `metadata:` 是同一个参数的别名，也参与 Tab 补全。只用于查询：`#summary`、回滚、恢复与清除都不接受它；不保存物品元数据的动作（`a:chat`、`a:command`、`a:session`、`a:username` 与 `a:sign`）同样不接受，会直接拒绝，而不是悄悄忽略筛选条件。

**原版与数据包标签。** 上游在 `#` 之后只认得自己的方块组，例如 `#sign`、`#container`。现在 `i:` 与 `e:` 也接受服务器上的任意方块、物品或实体标签，无论来自原版还是数据包，写作 `#命名空间:标签`，例如 `i:#minecraft:enchantable/durability`、`i:#minez:smithing_templates` 或 `e:#minecraft:skeletons`。命名空间为 `minecraft` 时可以省略，名字只存在于一个命名空间下时也可以省略；存在于多个命名空间下时须写出命名空间。标签在第一次用到时收集，数据包加载或重载时（`/minecraft:reload`、`/datapack enable` 与 `/datapack disable`）丢弃，因此数据包改了不必重启。标签同样参与 Tab 补全。

上游的方块组优先匹配，所以两边同名时（`#sign` 是上游的告示牌方块组，不是标签）以上游的含义为准。这时要用原版的标签，请写出完整的名字，例如 `#minecraft:all_signs`。

## 构建

需要 JDK 25 与 Maven。

```bash
mvn -B verify
```

插件输出到 `target/`，文件名为 `CoreProtect-<版本号>.jar`；旁边带 `original-` 前缀的是未打包依赖的版本，不要部署它。

每次推送到 `master` 也会由 [GitHub Actions](https://github.com/MINEZ/CoreProtect/actions) 构建，插件 jar 上传为 Artifact。Release 附带的是同一个 jar。

## 版本号

本 Fork 的版本号为上游版本号加一位修订号：`25.0.3` 即基于上游 `25.0` 的第 3 个构建。上游的版本号变化后，修订号从 `.1` 重新开始。

上游会先把部分构建发布给 Patreon 赞助者，之后公开仓库才跟上，所以 Patreon 上的版本号可能领先于公开源码中的版本号。本 Fork 只跟随公开仓库，版本号始终标明所基于的公开版本。

本 Fork 以 `pom.xml` 中的 `development` 分支设置构建，这是编译上游最新代码所必需的。这样构建出来的 CoreProtect 会跳过数据库补丁脚本，启动时显示“Development branch detected, skipping patch scripts.”。这条提示属正常现象，但也意味着上游以补丁脚本发布的数据库迁移在这里不会执行；写在代码中的修复照常生效。

上游项目与官网上的下载链接指向官方构建，**不**包含这些改动。

## 上游

CoreProtect 由 [PlayPro](https://github.com/PlayPro) 开发。关于 CoreProtect 本身的用法，[文档](https://docs.coreprotect.net/)与 [Discord](https://discord.gg/b4DZ4jy) 仍是合适的去处——仅限上游发布的 CoreProtect，而非本 Fork 中的任何内容。

这里的改动是为一台服务器的需要而做，单独维护。请不要在上游的渠道中讨论它们。

## 许可证

CoreProtect 以 [Artistic License 2.0](LICENSE) 开源。本 Fork 中的修改以相同条款提供。
