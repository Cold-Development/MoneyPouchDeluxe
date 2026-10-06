package dev.padrewin.moneypouchdeluxe.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Edits YAML files line by line instead of loading and re-saving them, so everything the owner wrote
 * (comments, including the ones at the end of a line, spacing, quotes) stays exactly as it was.
 * Understands the block style YAML of the plugin's own files (keys, nested sections, lists).
 */
public final class ConfigFiles {

    private ConfigFiles() {
    }

    /**
     * Removes a section (the key and every line indented under it) along with the comment block
     * right above it, and puts a short comment in its place. The path can be nested, e.g.
     * {@code pouches.tier}; everything else in the file stays as it was.
     *
     * @return true if the section was found and removed
     */
    public static boolean removeSection(File file, String path, String replacementComment) throws IOException {
        List<String> lines = read(file);
        Block block = findBlock(lines, path);
        if (block == null) {
            return false;
        }

        List<String> result = new ArrayList<>(lines.subList(0, block.commentStart));
        result.add(" ".repeat(block.indent) + "# " + replacementComment);
        result.addAll(lines.subList(block.end, lines.size()));
        write(file, result);
        return true;
    }

    /**
     * Where a key is in a file: its comment block, its own line and everything under it.
     */
    static final class Block {
        final int commentStart; // first line of the comments directly above the key (= keyLine if none)
        final int keyLine;
        final int end;          // index after the last line of the block
        final int indent;

        Block(int commentStart, int keyLine, int end, int indent) {
            this.commentStart = commentStart;
            this.keyLine = keyLine;
            this.end = end;
            this.indent = indent;
        }
    }

    /**
     * @param path a key path such as {@code pouches.title.format}
     * @return where the key is, or null if the file doesn't have it
     */
    static Block findBlock(List<String> lines, String path) {
        String[] keys = path.split("\\.");

        int from = 0;
        int to = lines.size();
        int indent = 0;
        for (int k = 0; k < keys.length; k++) {
            int keyLine = -1;
            for (int i = from; i < to; i++) {
                String line = lines.get(i);
                if (indentOf(line) == indent && isTopLevelKey(line.substring(indent), keys[k])) {
                    keyLine = i;
                    break;
                }
            }
            if (keyLine < 0) {
                return null;
            }
            int end = blockEnd(lines, keyLine, indent);
            if (k == keys.length - 1) {
                return new Block(commentStart(lines, keyLine, indent), keyLine, end, indent);
            }
            from = keyLine + 1;
            to = end;
            indent = childIndent(lines, from, to);
            if (indent < 0) {
                return null;
            }
        }
        return null;
    }

    /**
     * @return the first line of the comments directly above the key, at the same indentation
     */
    static int commentStart(List<String> lines, int keyLine, int indent) {
        int start = keyLine;
        while (start > 0 && lines.get(start - 1).trim().startsWith("#") && indentOf(lines.get(start - 1)) == indent) {
            start--;
        }
        return start;
    }

    /**
     * @return the index after the last line of the block starting at {@code start}: the block ends at
     * the next line (key or comment) indented as much as the key or less, except list items ({@code - x})
     * which may sit at the key's own indentation. Blank lines right before that line are left outside,
     * so the spacing between sections is kept.
     */
    static int blockEnd(List<String> lines, int start, int indent) {
        int end = start + 1;
        while (end < lines.size()) {
            String line = lines.get(end);
            boolean listItem = indentOf(line) == indent && line.trim().startsWith("- ");
            if (!line.isBlank() && indentOf(line) <= indent && !listItem) {
                break;
            }
            end++;
        }
        while (end > start + 1 && lines.get(end - 1).isBlank()) {
            end--;
        }
        return end;
    }

    /**
     * @return the indentation of the first key in the range, or -1 if there's none
     */
    static int childIndent(List<String> lines, int from, int to) {
        for (int i = from; i < to; i++) {
            String line = lines.get(i);
            if (!line.isBlank() && !line.trim().startsWith("#") && !line.trim().startsWith("- ")) {
                return indentOf(line);
            }
        }
        return -1;
    }

    /**
     * @return the key on this line ({@code key: value} or {@code key:}), or null if it isn't a key line
     */
    static String keyOf(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("- ")) {
            return null;
        }
        int colon = trimmed.indexOf(':');
        if (colon <= 0 || (colon + 1 < trimmed.length() && trimmed.charAt(colon + 1) != ' ')) {
            return null;
        }
        String key = trimmed.substring(0, colon).trim();
        if (key.length() >= 2 && (key.startsWith("\"") && key.endsWith("\"") || key.startsWith("'") && key.endsWith("'"))) {
            key = key.substring(1, key.length() - 1);
        }
        return key;
    }

    static int indentOf(String line) {
        int indent = 0;
        while (indent < line.length() && line.charAt(indent) == ' ') {
            indent++;
        }
        return indent;
    }

    public static void backup(File file, String suffix) throws IOException {
        Files.copy(file.toPath(), new File(file.getParentFile(), file.getName() + suffix).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
    }

    private static boolean isTopLevelKey(String line, String key) {
        if (!line.startsWith(key)) {
            return false;
        }
        String rest = line.substring(key.length()).stripTrailing();
        return rest.startsWith(":");
    }

    static List<String> read(File file) throws IOException {
        return new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
    }

    /**
     * Keeps the file's own line endings (CRLF or LF), so only the changed lines differ.
     */
    static void write(File file, List<String> lines) throws IOException {
        String original = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String separator = original.contains("\r\n") ? "\r\n" : "\n";
        Files.write(file.toPath(), (String.join(separator, lines) + separator).getBytes(StandardCharsets.UTF_8));
    }

}
