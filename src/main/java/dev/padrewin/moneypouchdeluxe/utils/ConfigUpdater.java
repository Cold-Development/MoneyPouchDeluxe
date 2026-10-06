package dev.padrewin.moneypouchdeluxe.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adds the settings a new version brings to an owner's existing file: every key of the default file
 * (the one in the jar) that the owner's file doesn't have is inserted together with its comments,
 * after the key it follows in the default file and with the owner's indentation.
 * <p>
 * The file is edited line by line, so the owner's values, comments, spacing and order stay exactly
 * as they were; nothing is ever removed or changed.
 */
public final class ConfigUpdater {

    private ConfigUpdater() {
    }

    /**
     * @param file     the owner's file
     * @param defaults the default version of the file
     * @return the paths of the settings that were added (empty if the file was already up to date)
     */
    public static List<String> update(File file, InputStream defaults) throws IOException {
        List<String> defaultLines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(defaults, StandardCharsets.UTF_8))) {
            defaultLines = reader.lines().collect(Collectors.toCollection(ArrayList::new));
        }

        List<String> lines = ConfigFiles.read(file);
        List<String> added = new ArrayList<>();
        merge(defaultLines, parse(defaultLines, 0, defaultLines.size(), 0, ""), lines, null, added);

        if (!added.isEmpty()) {
            ConfigFiles.write(file, lines);
        }
        return added;
    }

    /**
     * A key of the default file, with the lines it covers.
     */
    private static final class Entry {
        String path;
        int commentStart;
        int keyLine;
        int end;
        int indent;
        List<Entry> children = new ArrayList<>();
    }

    private static List<Entry> parse(List<String> lines, int from, int to, int indent, String parentPath) {
        List<Entry> entries = new ArrayList<>();
        int i = from;
        while (i < to) {
            String line = lines.get(i);
            String key = ConfigFiles.keyOf(line);
            if (key == null || ConfigFiles.indentOf(line) != indent) {
                i++;
                continue;
            }

            Entry entry = new Entry();
            entry.path = parentPath.isEmpty() ? key : parentPath + "." + key;
            entry.keyLine = i;
            entry.indent = indent;
            entry.end = Math.min(ConfigFiles.blockEnd(lines, i, indent), to);
            entry.commentStart = Math.max(from, ConfigFiles.commentStart(lines, i, indent));

            int childIndent = ConfigFiles.childIndent(lines, i + 1, entry.end);
            if (childIndent > indent) {
                entry.children = parse(lines, i + 1, entry.end, childIndent, entry.path);
            }

            entries.add(entry);
            i = entry.end;
        }
        return entries;
    }

    private static void merge(List<String> defaults, List<Entry> entries, List<String> lines, Entry parent, List<String> added) {
        Entry previous = null;
        for (Entry entry : entries) {
            ConfigFiles.Block existing = ConfigFiles.findBlock(lines, entry.path);
            if (existing == null) {
                if (insert(defaults, entry, previous, parent, lines)) {
                    added.add(entry.path);
                }
            } else if (!entry.children.isEmpty() && isSection(lines, existing)) {
                merge(defaults, entry.children, lines, entry, added);
            }
            previous = entry;
        }
    }

    /**
     * Inserts the entry (comments, key and everything under it) after its previous sibling, or as the
     * first key of its section when it has none.
     *
     * @return false if it couldn't be placed (its section is a plain value in the owner's file)
     */
    private static boolean insert(List<String> defaults, Entry entry, Entry previous, Entry parent, List<String> lines) {
        int targetIndent = 0;
        ConfigFiles.Block parentBlock = null;
        if (parent != null) {
            parentBlock = ConfigFiles.findBlock(lines, parent.path);
            if (parentBlock == null || !isSection(lines, parentBlock)) {
                return false;
            }
            int childIndent = ConfigFiles.childIndent(lines, parentBlock.keyLine + 1, parentBlock.end);
            targetIndent = childIndent >= 0 ? childIndent : parentBlock.indent + (entry.indent - parent.indent);
        }

        ConfigFiles.Block previousBlock = previous != null ? ConfigFiles.findBlock(lines, previous.path) : null;
        int index;
        if (previousBlock != null) {
            index = previousBlock.end;
        } else if (parentBlock != null) {
            index = parentBlock.keyLine + 1;
        } else {
            index = firstTopLevelKey(lines);
        }

        List<String> block = new ArrayList<>();
        boolean blankBefore = entry.commentStart > 0 && defaults.get(entry.commentStart - 1).isBlank();
        boolean blankAfter = entry.end < defaults.size() && defaults.get(entry.end).isBlank();
        if (previousBlock != null && blankBefore) {
            block.add("");
        }
        for (int i = entry.commentStart; i < entry.end; i++) {
            block.add(reindent(defaults.get(i), targetIndent - entry.indent));
        }
        if (previousBlock == null && blankAfter) {
            block.add("");
        }
        lines.addAll(index, block);
        return true;
    }

    /**
     * A key with nothing after the colon (its value is the indented block below it).
     */
    private static boolean isSection(List<String> lines, ConfigFiles.Block block) {
        String line = lines.get(block.keyLine).trim();
        String afterColon = line.substring(line.indexOf(':') + 1).trim();
        return afterColon.isEmpty() || afterColon.startsWith("#");
    }

    /**
     * @return where the first top-level key starts (its comments included), or the end of the file
     */
    private static int firstTopLevelKey(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (ConfigFiles.indentOf(lines.get(i)) == 0 && ConfigFiles.keyOf(lines.get(i)) != null) {
                return ConfigFiles.commentStart(lines, i, 0);
            }
        }
        return lines.size();
    }

    private static String reindent(String line, int delta) {
        if (line.isBlank()) {
            return "";
        }
        if (delta > 0) {
            return " ".repeat(delta) + line;
        }
        int remove = Math.min(-delta, ConfigFiles.indentOf(line));
        return line.substring(remove);
    }

}
