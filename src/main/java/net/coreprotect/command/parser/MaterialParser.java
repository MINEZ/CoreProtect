package net.coreprotect.command.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import net.coreprotect.language.Phrase;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.model.action.LookupActions;
import net.coreprotect.utility.BlockTypeUtils;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.MaterialUtils;

/**
 * Parser for material and entity related command arguments
 */
public class MaterialParser {

    private static final String[] INCLUDE_PREFIXES = {
            "include:",
            "blocks:",
            "block:",
            "items:",
            "item:",
            "i:",
            "b:"
    };

    private static final String[] EXCLUDE_PREFIXES = {
            "exclude:",
            "e:"
    };

    public static String stripIncludePrefix(String normalizedArgument) {
        return ParserUtils.stripPrefix(normalizedArgument, INCLUDE_PREFIXES);
    }

    public static String stripExcludePrefix(String normalizedArgument) {
        return ParserUtils.stripPrefix(normalizedArgument, EXCLUDE_PREFIXES);
    }

    /**
     * Parse restricted materials and entities from command arguments
     * 
     * @param player
     *            The command sender
     * @param inputArguments
     *            The command arguments
     * @param argAction
     *            The list of actions to include
     * @return A list of restricted materials and entities
     */
    public static List<Object> parseRestricted(CommandSender player, String[] inputArguments, List<Integer> argAction) {
        List<Object> restricted = new ArrayList<>();
        int next = 0;

        for (int index = 1; index < inputArguments.length; index++) {
            String argument = ParserUtils.normalize(inputArguments[index]);

            if (ParserUtils.equalsAny(argument, INCLUDE_PREFIXES)) {
                next = 4;
                continue;
            }

            String stripped = stripIncludePrefix(argument);
            if (next == 4 || stripped != null) {
                String spec = (stripped != null) ? stripped : argument;
                if (spec.contains(",")) {
                    for (String part : spec.split(",")) {
                        String candidate = part.trim();
                        if (!candidate.isEmpty() && !addRestrictedTarget(player, candidate, argAction, restricted)) {
                            return null;
                        }
                    }
                    next = spec.endsWith(",") ? 4 : 0;
                }
                else {
                    if (!addRestrictedTarget(player, spec, argAction, restricted)) {
                        return null;
                    }
                    next = 0;
                }
            }
            else {
                next = 0;
            }
        }
        return restricted;
    }

    /**
     * Parse excluded materials and entities from command arguments
     * 
     * @param player
     *            The command sender
     * @param inputArguments
     *            The command arguments
     * @param argAction
     *            The list of actions to include
     * @return A map of excluded materials and entities
     */
    public static Map<Object, Boolean> parseExcluded(CommandSender player, String[] inputArguments, List<Integer> argAction) {
        Map<Object, Boolean> excluded = new HashMap<>();
        int next = 0;

        for (int index = 1; index < inputArguments.length; index++) {
            String argument = ParserUtils.normalize(inputArguments[index]);

            if (ParserUtils.equalsAny(argument, EXCLUDE_PREFIXES)) {
                next = 5;
                continue;
            }

            String stripped = stripExcludePrefix(argument);
            if (next == 5 || stripped != null) {
                String spec = (stripped != null) ? stripped : argument;
                if (spec.contains(",")) {
                    for (String part : spec.split(",")) {
                        String candidate = part.trim();
                        if (!candidate.isEmpty()) {
                            addExcludedTarget(candidate, argAction, excluded);
                        }
                    }
                    next = spec.endsWith(",") ? 5 : 0;
                }
                else {
                    addExcludedTarget(spec, argAction, excluded);
                    next = 0;
                }
            }
            else {
                next = 0;
            }
        }
        return excluded;
    }

    private static boolean addRestrictedTarget(CommandSender player, String token, List<Integer> argAction, List<Object> restricted) {
        if (checkTags(token, restricted)) {
            return true;
        }
        Object target = resolveTarget(token, argAction);
        if (target != null) {
            restricted.add(target);
            return true;
        }
        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, token));
        return false;
    }

    private static void addExcludedTarget(String token, List<Integer> argAction, Map<Object, Boolean> excluded) {
        if (checkTags(token, excluded)) {
            return;
        }
        Object target = resolveTarget(token, argAction);
        if (target != null) {
            excluded.put(target, false);
        }
    }

    private static Object resolveTarget(String token, List<Integer> argAction) {
        boolean containerAction = argAction != null && argAction.contains(LookupActions.CONTAINER);
        Material material = MaterialUtils.getType(token);
        if (material != null && (material.isBlock() || containerAction)) {
            return material;
        }
        if (!containerAction && BlockTypeUtils.hasBlockType(token)) {
            return BlockTypeUtils.normalizeKey(token);
        }
        EntityType entityType = EntityUtils.getEntityType(token);
        if (entityType != null) {
            return entityType;
        }
        return material;
    }

    /**
     * Get a map of block tags and their associated materials
     * 
     * @return A map of block tags and their associated materials
     */
    /**
     * 原版与数据包标签的缓存。键为 "#命名空间:路径"，值为标签展开后的 Material 或 EntityType。
     *
     * 之所以不写死材质清单，是因为硬编码的 BlockGroup 会随版本更新而过时——例如 DOORS
     * 至今仍只有 8 种门，缺了 mangrove、cherry、bamboo 等。改为运行时读取注册表后，
     * 服务端支持什么标签就有什么标签，无需跟随 Minecraft 版本维护。
     */
    private static volatile Map<String, Set<Object>> dynamicTagCache;

    /**
     * 使标签缓存失效，下次访问时重建。数据包加载或重载后应调用。
     */
    public static void invalidateDynamicTags() {
        dynamicTagCache = null;
    }

    /**
     * 获取全部动态标签，按需构建缓存。
     */
    private static Map<String, Set<Object>> getDynamicTags() {
        Map<String, Set<Object>> cache = dynamicTagCache;
        if (cache != null) {
            return cache;
        }

        Map<String, Set<Object>> built = new HashMap<>();
        collectTags(built, Tag.REGISTRY_BLOCKS, Material.class);
        collectTags(built, Tag.REGISTRY_ITEMS, Material.class);
        collectTags(built, Tag.REGISTRY_ENTITY_TYPES, EntityType.class);

        cache = Collections.unmodifiableMap(built);
        dynamicTagCache = cache;
        return cache;
    }

    private static <T extends Keyed> void collectTags(Map<String, Set<Object>> target, String registry, Class<T> clazz) {
        try {
            for (Tag<T> tag : Bukkit.getTags(registry, clazz)) {
                mergeTag(target, tag);
            }
        }
        catch (Throwable e) {
            /* 服务端未提供该注册表时跳过，其余注册表照常可用 */
        }
    }

    private static <T extends Keyed> void mergeTag(Map<String, Set<Object>> target, Tag<T> tag) {
        if (tag == null) {
            return;
        }

        NamespacedKey key = tag.getKey();
        Set<T> values = tag.getValues();
        if (key == null || values == null || values.isEmpty()) {
            return;
        }

        /* blocks 与 items 下的同名标签取并集 */
        target.computeIfAbsent("#" + key.getNamespace() + ":" + key.getKey(), unused -> new HashSet<>()).addAll(values);
    }

    /**
     * 供 Tab 补全使用的全部动态标签键。
     */
    public static Set<String> getDynamicTagKeys() {
        return getDynamicTags().keySet();
    }

    /**
     * 将 #标签 解析为对应的 Material / EntityType 集合，无法解析时返回 null。
     *
     * 省略命名空间时优先匹配 minecraft，其次要求在其他命名空间下唯一；
     * 命中多个则返回 null，要求使用者写全命名空间以消除歧义。
     */
    public static Set<Object> resolveDynamicTag(String argument) {
        if (argument == null || argument.length() < 2 || argument.charAt(0) != '#') {
            return null;
        }

        Map<String, Set<Object>> tags = getDynamicTags();
        String name = argument.substring(1).toLowerCase(Locale.ROOT);

        if (name.indexOf(':') >= 0) {
            Set<Object> values = tags.get("#" + name);
            return values != null ? values : lookupTagDirectly(name);
        }

        Set<Object> vanilla = tags.get("#" + NamespacedKey.MINECRAFT + ":" + name);
        if (vanilla != null) {
            return vanilla;
        }

        Set<Object> matched = null;
        String suffix = ":" + name;
        for (Entry<String, Set<Object>> entry : tags.entrySet()) {
            if (entry.getKey().endsWith(suffix)) {
                if (matched != null) {
                    return null;
                }

                matched = entry.getValue();
            }
        }

        return matched;
    }

    /**
     * 按完整键直接向服务端查询标签。
     *
     * 用于兜底：即便某些服务端实现在枚举时不返回数据包标签，
     * 只要使用者写全了命名空间，这里仍然能解析出来。
     */
    private static Set<Object> lookupTagDirectly(String name) {
        NamespacedKey key = NamespacedKey.fromString(name);
        if (key == null) {
            return null;
        }

        Set<Object> values = new HashSet<>();
        appendTagValues(values, Tag.REGISTRY_BLOCKS, key, Material.class);
        appendTagValues(values, Tag.REGISTRY_ITEMS, key, Material.class);
        appendTagValues(values, Tag.REGISTRY_ENTITY_TYPES, key, EntityType.class);
        return values.isEmpty() ? null : values;
    }

    private static <T extends Keyed> void appendTagValues(Set<Object> target, String registry, NamespacedKey key, Class<T> clazz) {
        try {
            Tag<T> tag = Bukkit.getTag(registry, key, clazz);
            if (tag != null && tag.getValues() != null) {
                target.addAll(tag.getValues());
            }
        }
        catch (Throwable e) {
            /* 该注册表下无此标签 */
        }
    }

    public static Map<String, Set<Material>> getTags() {
        Map<String, Set<Material>> tagMap = new HashMap<>();
        tagMap.put("#button", BlockGroup.BUTTONS);
        tagMap.put("#sign", BlockGroup.SIGNS);
        tagMap.put("#container", BlockGroup.CONTAINERS);
        tagMap.put("#door", BlockGroup.DOORS);
        tagMap.put("#natural", BlockGroup.NATURAL_BLOCKS);
        tagMap.put("#pressure_plate", BlockGroup.PRESSURE_PLATES);
        tagMap.put("#shulker_box", BlockGroup.SHULKER_BOXES);
        if (!BlockGroup.BUNDLES.isEmpty()) {
            tagMap.put("#bundle", BlockGroup.BUNDLES);
        }
        return tagMap;
    }

    private static Set<Material> resolveTagMaterials(String argument) {
        if (argument == null || !argument.startsWith("#")) {
            return null;
        }
        switch (argument) {
            case "#button":
                return BlockGroup.BUTTONS;
            case "#sign":
                return BlockGroup.SIGNS;
            case "#container":
                return BlockGroup.CONTAINERS;
            case "#door":
                return BlockGroup.DOORS;
            case "#natural":
                return BlockGroup.NATURAL_BLOCKS;
            case "#pressure_plate":
                return BlockGroup.PRESSURE_PLATES;
            case "#shulker_box":
                return BlockGroup.SHULKER_BOXES;
            case "#bundle":
                return BlockGroup.BUNDLES.isEmpty() ? null : BlockGroup.BUNDLES;
            default:
                return null;
        }
    }

    /**
     * Check if an argument matches a block tag
     * 
     * @param argument
     *            The argument to check
     * @return true if the argument matches a block tag
     */
    public static boolean checkTags(String argument) {
        return resolveTagMaterials(argument) != null || resolveDynamicTag(argument) != null;
    }

    /**
     * Check if an argument matches a block tag and add the associated materials to the list
     * 
     * @param argument
     *            The argument to check
     * @param list
     *            The list to add the associated materials to
     * @return true if the argument matches a block tag
     */
    public static boolean checkTags(String argument, Map<Object, Boolean> list) {
        // 先匹配 CoreProtect 自带的方块组（如 #sign），没有时再匹配原版与数据包标签。
        Set<Material> materials = resolveTagMaterials(argument);
        if (materials != null) {
            for (Material block : materials) {
                list.put(block, false);
            }
            return true;
        }

        Set<Object> tagValues = resolveDynamicTag(argument);
        if (tagValues != null) {
            for (Object tagValue : tagValues) {
                list.put(tagValue, false);
            }

            return true;
        }

        return false;
    }

    /**
     * Check if an argument matches a block tag and add the associated materials to the list
     * 
     * @param argument
     *            The argument to check
     * @param list
     *            The list to add the associated materials to
     * @return true if the argument matches a block tag
     */
    public static boolean checkTags(String argument, List<Object> list) {
        // 先匹配 CoreProtect 自带的方块组（如 #sign），没有时再匹配原版与数据包标签。
        Set<Material> materials = resolveTagMaterials(argument);
        if (materials != null) {
            list.addAll(materials);
            return true;
        }

        Set<Object> tagValues = resolveDynamicTag(argument);
        if (tagValues != null) {
            list.addAll(tagValues);
            return true;
        }

        return false;
    }

    /**
     * Check if a string represents a block or entity
     * 
     * @param argument
     *            The string to check
     * @return true if the string represents a block or entity
     */
    public static boolean isBlockOrEntity(String argument) {
        return checkTags(argument)
                || MaterialUtils.getType(argument) != null
                || BlockTypeUtils.hasBlockType(argument)
                || EntityUtils.getEntityType(argument) != null;
    }
}
