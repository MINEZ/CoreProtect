package net.coreprotect.command.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 解析 m:/meta:/metadata: 参数。
 *
 * 该参数用于按物品元数据内容筛选查询结果，典型用途是筛出带有特定命名空间 ID 的
 * 附魔、标签或自定义数据的物品，例如 m:minez:eternal_covenant。
 *
 * 匹配语义为“序列化后的元数据字节中包含该 token”。之所以可行，是因为 Bukkit 在
 * 序列化 ItemMeta 时会把附魔键、PDC 键等以字面 UTF-8 字符串写入字节流。
 */
public final class MetadataFilterParser {
    public static final int MINIMUM_FILTER_CODE_POINTS = 3;

    private MetadataFilterParser() {
        throw new IllegalStateException("Parser class");
    }

    public static ParseResult parse(String[] inputArguments) {
        if (inputArguments == null || inputArguments.length == 0) {
            return new ParseResult(new String[0], Collections.emptyList(), false);
        }

        List<String> arguments = new ArrayList<>(inputArguments.length);
        Set<String> filters = new LinkedHashSet<>();
        boolean specified = false;

        for (int index = 0; index < inputArguments.length; index++) {
            String argument = Objects.toString(inputArguments[index], "").trim();
            if (!isMetadataParameter(argument)) {
                arguments.add(argument);
                continue;
            }

            specified = true;
            StringBuilder collected = new StringBuilder(argument);
            while (index + 1 < inputArguments.length && !MessageFilterParser.isLookupTerminator(inputArguments[index + 1])) {
                String next = Objects.toString(inputArguments[++index], "").trim();
                if (!next.isEmpty()) {
                    collected.append(' ').append(next);
                }
            }

            String merged = collected.toString();
            arguments.add(merged);
            addFilters(filters, value(merged));
        }

        return new ParseResult(arguments.toArray(new String[0]), new ArrayList<>(filters), specified);
    }

    public static boolean isMetadataParameter(String raw) {
        String normalized = Objects.toString(raw, "").trim().toLowerCase(java.util.Locale.ROOT).replace("\\", "").replace("'", "");
        return normalized.startsWith("m:") || normalized.startsWith("meta:") || normalized.startsWith("metadata:");
    }

    private static String value(String argument) {
        int separator = argument.indexOf(':');
        return separator < 0 ? "" : argument.substring(separator + 1);
    }

    private static void addFilters(Set<String> filters, String value) {
        for (String part : value.split(",", -1)) {
            String filter = part.trim();
            if (!filter.isEmpty()) {
                filters.add(filter);
            }
        }
    }

    public static final class ParseResult {
        private final String[] arguments;
        private final List<String> filters;
        private final boolean specified;

        private ParseResult(String[] arguments, List<String> filters, boolean specified) {
            this.arguments = arguments;
            this.filters = Collections.unmodifiableList(filters);
            this.specified = specified;
        }

        public String[] getArguments() {
            return arguments.clone();
        }

        public List<String> getFilters() {
            return filters;
        }

        public boolean isSpecified() {
            return specified;
        }

        public boolean hasInvalidLength() {
            if (filters.isEmpty()) {
                return true;
            }
            for (String filter : filters) {
                if (filter.codePointCount(0, filter.length()) < MINIMUM_FILTER_CODE_POINTS) {
                    return true;
                }
            }
            return false;
        }
    }
}
