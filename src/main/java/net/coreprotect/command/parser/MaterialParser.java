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
        String[] argumentArray = inputArguments.clone();
        List<Object> restricted = new ArrayList<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT);
                argument = argument.replaceAll("\\\\", "");
                argument = argument.replaceAll("'", "");

                if (argument.equals("i:") || argument.equals("include:") || argument.equals("item:") || argument.equals("items:") || argument.equals("b:") || argument.equals("block:") || argument.equals("blocks:")) {
                    next = 4;
                }
                else if (next == 4 || argument.startsWith("i:") || argument.startsWith("include:") || argument.startsWith("item:") || argument.startsWith("items:") || argument.startsWith("b:") || argument.startsWith("block:") || argument.startsWith("blocks:")) {
                    argument = argument.replaceAll("include:", "");
                    argument = argument.replaceAll("i:", "");
                    argument = argument.replaceAll("items:", "");
                    argument = argument.replaceAll("item:", "");
                    argument = argument.replaceAll("blocks:", "");
                    argument = argument.replaceAll("block:", "");
                    argument = argument.replaceAll("b:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            if (!checkTags(i3, restricted)) {
                                Material i3_material = MaterialUtils.getType(i3);
                                if (i3_material != null && (i3_material.isBlock() || argAction.contains(LookupActions.CONTAINER))) {
                                    restricted.add(i3_material);
                                }
                                else if (!argAction.contains(LookupActions.CONTAINER) && BlockTypeUtils.hasBlockType(i3)) {
                                    restricted.add(BlockTypeUtils.normalizeKey(i3));
                                }
                                else {
                                    EntityType i3_entity = EntityUtils.getEntityType(i3);
                                    if (i3_entity != null) {
                                        restricted.add(i3_entity);
                                    }
                                    else if (i3_material != null) {
                                        restricted.add(i3_material);
                                    }
                                    else {
                                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, i3));
                                        // Functions.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co help include"));
                                        return null;
                                    }
                                }
                            }
                        }
                        if (argument.endsWith(",")) {
                            next = 4;
                        }
                        else {
                            next = 0;
                        }
                    }
                    else {
                        if (!checkTags(argument, restricted)) {
                            Material material = MaterialUtils.getType(argument);
                            if (material != null && (material.isBlock() || argAction.contains(LookupActions.CONTAINER))) {
                                restricted.add(material);
                            }
                            else if (!argAction.contains(LookupActions.CONTAINER) && BlockTypeUtils.hasBlockType(argument)) {
                                restricted.add(BlockTypeUtils.normalizeKey(argument));
                            }
                            else {
                                EntityType entityType = EntityUtils.getEntityType(argument);
                                if (entityType != null) {
                                    restricted.add(entityType);
                                }
                                else if (material != null) {
                                    restricted.add(material);
                                }
                                else {
                                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.INVALID_INCLUDE, argument));
                                    // Functions.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.MISSING_PARAMETERS, "/co help include"));
                                    return null;
                                }
                            }
                        }
                        next = 0;
                    }
                }
                else {
                    next = 0;
                }
            }
            count++;
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
        String[] argumentArray = inputArguments.clone();
        Map<Object, Boolean> excluded = new HashMap<>();
        int count = 0;
        int next = 0;
        for (String argument : argumentArray) {
            if (count > 0) {
                argument = argument.trim().toLowerCase(Locale.ROOT);
                argument = argument.replaceAll("\\\\", "");
                argument = argument.replaceAll("'", "");

                if (argument.equals("e:") || argument.equals("exclude:")) {
                    next = 5;
                }
                else if (next == 5 || argument.startsWith("e:") || argument.startsWith("exclude:")) {
                    argument = argument.replaceAll("exclude:", "");
                    argument = argument.replaceAll("e:", "");
                    if (argument.contains(",")) {
                        String[] i2 = argument.split(",");
                        for (String i3 : i2) {
                            if (!checkTags(i3, excluded)) {
                                Material i3_material = MaterialUtils.getType(i3);
                                if (i3_material != null && (i3_material.isBlock() || argAction.contains(LookupActions.CONTAINER))) {
                                    excluded.put(i3_material, false);
                                }
                                else if (!argAction.contains(LookupActions.CONTAINER) && BlockTypeUtils.hasBlockType(i3)) {
                                    excluded.put(BlockTypeUtils.normalizeKey(i3), false);
                                }
                                else {
                                    EntityType i3_entity = EntityUtils.getEntityType(i3);
                                    if (i3_entity != null) {
                                        excluded.put(i3_entity, false);
                                    }
                                    else if (i3_material != null) {
                                        excluded.put(i3_material, false);
                                    }
                                }
                            }
                        }
                        if (argument.endsWith(",")) {
                            next = 5;
                        }
                        else {
                            next = 0;
                        }
                    }
                    else {
                        if (!checkTags(argument, excluded)) {
                            Material iMaterial = MaterialUtils.getType(argument);
                            if (iMaterial != null && (iMaterial.isBlock() || argAction.contains(LookupActions.CONTAINER))) {
                                excluded.put(iMaterial, false);
                            }
                            else if (!argAction.contains(LookupActions.CONTAINER) && BlockTypeUtils.hasBlockType(argument)) {
                                excluded.put(BlockTypeUtils.normalizeKey(argument), false);
                            }
                            else {
                                EntityType iEntity = EntityUtils.getEntityType(argument);
                                if (iEntity != null) {
                                    excluded.put(iEntity, false);
                                }
                                else if (iMaterial != null) {
                                    excluded.put(iMaterial, false);
                                }
                            }
                        }
                        next = 0;
                    }
                }
                else {
                    next = 0;
                }
            }
            count++;
        }
        return excluded;
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

    /**
     * Check if an argument matches a block tag
     * 
     * @param argument
     *            The argument to check
     * @return true if the argument matches a block tag
     */
    public static boolean checkTags(String argument) {
        return getTags().containsKey(argument) || resolveDynamicTag(argument) != null;
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
        for (Entry<String, Set<Material>> entry : getTags().entrySet()) {
            String tag = entry.getKey();
            Set<Material> materials = entry.getValue();

            if (argument.equals(tag)) {
                for (Material block : materials) {
                    list.put(block, false);
                }

                return true;
            }
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
        for (Entry<String, Set<Material>> entry : getTags().entrySet()) {
            String tag = entry.getKey();
            Set<Material> materials = entry.getValue();

            if (argument.equals(tag)) {
                list.addAll(materials);
                return true;
            }
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
        boolean isBlock = false;
        if (checkTags(argument)) {
            isBlock = true;
        }
        else {
            Material material = MaterialUtils.getType(argument);
            if (material != null) {
                isBlock = true;
            }
            else if (BlockTypeUtils.hasBlockType(argument)) {
                isBlock = true;
            }
            else {
                EntityType entityType = EntityUtils.getEntityType(argument);
                if (entityType != null) {
                    isBlock = true;
                }
            }
        }
        return isBlock;
    }
}
