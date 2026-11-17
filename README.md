<div align="center">

# MINEZ CoreProtect

A fork of [CoreProtect](https://github.com/PlayPro/CoreProtect) maintained for the MINEZ server.

<a href="README.md">English</a> ｜
<a href="README_zh-CN.md">简体中文</a>

<br>

<div>
<a href="https://github.com/PlayPro/CoreProtect"><img src="https://img.shields.io/badge/upstream-25.0-2196f3" alt="upstream"></a>
<a href="#versioning"><img src="https://img.shields.io/badge/version-25.0.3-4caf50" alt="version"></a>
<a href="#supported-versions"><img src="https://img.shields.io/badge/Paper-26.3-4caf50" alt="Paper"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-Artistic%202.0-9e9e9e" alt="license"></a>
</div>

<br>

<a href="https://docs.coreprotect.net/">Documentation</a> ｜
<a href="https://github.com/MINEZ/CoreProtect/issues">Issue Tracker</a> ｜
<a href="https://github.com/MINEZ/CoreProtect/releases">Releases</a> ｜
<a href="#changes-from-upstream">What's Changed</a>

</div>

> [!IMPORTANT]
> **This is an unofficial fork and is not affiliated with, endorsed by, or supported by PlayPro or the CoreProtect team.**
>
> Never take anything about this fork to the upstream repository, the official Discord, or Patreon. Use [this repository's issue tracker](https://github.com/MINEZ/CoreProtect/issues) instead.
>
> To tell the two apart, reproduce the problem on an unmodified upstream build first:
>
> - **It still happens without this fork** — an upstream problem. Report it upstream as you normally would, with no mention of this fork.
> - **It does not happen without this fork** — ours. It belongs here, and only here.

## About

This repository tracks the upstream `master` branch and adds a small set of changes needed by the MINEZ server. Everything not listed below is identical to upstream.

CoreProtect itself is a data logging and anti-griefing tool: it records block, container, entity and player activity, and lets staff inspect, look up, roll back and restore it. Its commands, permissions, configuration and API are documented at [docs.coreprotect.net](https://docs.coreprotect.net/), and that documentation applies here unchanged apart from the additions below.

### Supported Versions

Built and run against Paper 26.3, the only server this fork is deployed on. Upstream's wider compatibility (Spigot, Folia and older versions) is left in place but not tested here.

Upstream is followed by merge:

```bash
git fetch upstream
git merge upstream/master
```

## Changes from Upstream

| Name | Type | Change |
| --- | --- | --- |
| `m:` / `meta:` / `metadata:` | Lookup parameter | Added |
| `#namespace:tag` in `i:` / `e:` | Lookup parameter | Changed |

**Metadata filter.** `m:` narrows a lookup down to items whose saved metadata contains the given text, which is the way to find items that carry a particular namespaced ID — a custom enchantment, a data component or a persistent data key — since none of the existing parameters look inside an item at all. It works because Bukkit writes those keys into the serialized metadata as plain UTF-8, so a match is a substring match on the stored bytes. Several values can be given separated by commas; a row matches if any of them is found. Each value needs at least three characters, to keep it from matching nearly everything.

```
/co lookup r:#global t:30d i:enchanted_book m:minez:eternal_covenant
```

The `meta:` and `metadata:` spellings are the same parameter, and it takes part in tab completion. It applies to lookups only: `#summary`, rollback, restore and purge do not accept it, and neither do actions that store no item metadata (`a:chat`, `a:command`, `a:session`, `a:username` and `a:sign`), which are refused rather than silently ignoring the filter.

**Vanilla and data pack tags.** Upstream only understands its own block groups after a `#`, such as `#sign` or `#container`. `i:` and `e:` now also take any block, item or entity tag the server knows, from vanilla or from a data pack, written as `#namespace:tag` — for example `i:#minecraft:enchantable/durability`, `i:#minez:smithing_templates` or `e:#minecraft:skeletons`. The namespace can be left out when it is `minecraft`, or when the name exists under one namespace only; if it exists under several, write the namespace out. Tags are collected the first time they are needed and dropped whenever data packs are loaded or reloaded (`/minecraft:reload`, `/datapack enable` and `/datapack disable`), so a changed data pack is picked up without a restart. They are offered in tab completion too.

Upstream's own groups are checked first, so where a name exists in both — `#sign` is upstream's sign group, not a tag — the upstream meaning wins. To reach the vanilla tag in such a case, write it out in full, such as `#minecraft:all_signs`.

## Building

Requires JDK 25 and Maven.

```bash
mvn -B verify
```

The plugin lands in `target/` as `CoreProtect-<version>.jar`; the `original-` jar next to it is the unshaded build and is not the one to deploy.

Every push to `master` is also built by [GitHub Actions](https://github.com/MINEZ/CoreProtect/actions), which uploads the plugin jar as an artifact. Releases carry the same jar.

## Versioning

The fork version is the upstream version with a revision number added: `25.0.3` is the third build of this fork based on upstream `25.0`. The number restarts at `.1` when upstream's version changes.

Upstream publishes some builds to its Patreon supporters before the public repository catches up, and the version on Patreon can be ahead of the one in the public source. This fork only follows the public repository, and its version always names the public version it is built from.

The fork is built from the `development` branch setting in `pom.xml`, which is needed to build upstream's latest code. On such builds CoreProtect skips its database patch scripts and prints “Development branch detected, skipping patch scripts.” at startup. That message is expected, but it means database migrations that upstream ships as patch scripts do not run here; fixes made in code take effect as usual.

The download links on the upstream project and website point at official builds and do **not** include these changes.

## Upstream

CoreProtect is developed by [PlayPro](https://github.com/PlayPro). For using CoreProtect itself, the [documentation](https://docs.coreprotect.net/) and the [Discord](https://discord.gg/b4DZ4jy) remain the right places to go — for CoreProtect as upstream ships it, not for anything in this fork.

The changes here are built for one server's needs and are maintained separately. Please keep discussion of them out of upstream's channels.

## License

CoreProtect is licensed under the [Artistic License 2.0](LICENSE). Modifications in this fork are provided under the same terms.
