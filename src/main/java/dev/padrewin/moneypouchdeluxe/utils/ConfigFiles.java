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
 * Only handles top-level keys, which is all a migration needs.
 */
public final class ConfigFiles {

    private ConfigFiles() {
    }

    /**
     * @return true if the file has this key at the top level ({@code key:} at the start of a line)
     */
    public static boolean hasTopLevelKey(List<String> lines, String key) {
        for (String line : lines) {
            if (isTopLevelKey(line, key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds lines (a key with its comments) before the first top-level key of the file, i.e. right
     * after the header comments, if the key isn't in the file yet.
     *
     * @return true if the file was changed
     */
    public static boolean addTopLevelKeyIfMissing(File file, String key, List<String> linesToAdd) throws IOException {
        List<String> lines = read(file);
        if (hasTopLevelKey(lines, key)) {
            return false;
        }

        int index = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.isBlank() && !line.startsWith("#")) {
                index = i;
                break;
            }
        }

        List<String> insert = new ArrayList<>(linesToAdd);
        insert.add("");
        lines.addAll(index, insert);
        write(file, lines);
        return true;
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
        String[] keys = path.split("\\.");

        int from = 0;
        int to = lines.size();
        int indent = 0;
        int start = -1;
        int end = -1;
        for (int k = 0; k < keys.length; k++) {
            start = -1;
            for (int i = from; i < to; i++) {
                String line = lines.get(i);
                if (indentOf(line) == indent && isTopLevelKey(line.substring(indent), keys[k])) {
                    start = i;
                    break;
                }
            }
            if (start < 0) {
                return false;
            }
            end = blockEnd(lines, start, indent);
            if (k < keys.length - 1) {
                from = start + 1;
                to = end;
                indent = childIndent(lines, from, to);
                if (indent < 0) {
                    return false;
                }
            }
        }

        // The comments directly above the key, at the same indentation, describe it
        int commentStart = start;
        while (commentStart > 0 && lines.get(commentStart - 1).trim().startsWith("#")
                && indentOf(lines.get(commentStart - 1)) == indent) {
            commentStart--;
        }

        List<String> result = new ArrayList<>(lines.subList(0, commentStart));
        result.add(" ".repeat(indent) + "# " + replacementComment);
        result.addAll(lines.subList(end, lines.size()));
        write(file, result);
        return true;
    }

    /**
     * @return the index after the last line of the block starting at {@code start}: the block ends at
     * the next line (key or comment) indented as much as the key or less. Blank lines right before
     * that line are left outside, so the spacing between sections is kept.
     */
    private static int blockEnd(List<String> lines, int start, int indent) {
        int end = start + 1;
        while (end < lines.size() && (lines.get(end).isBlank() || indentOf(lines.get(end)) > indent)) {
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
    private static int childIndent(List<String> lines, int from, int to) {
        for (int i = from; i < to; i++) {
            String line = lines.get(i);
            if (!line.isBlank() && !line.trim().startsWith("#")) {
                return indentOf(line);
            }
        }
        return -1;
    }

    private static int indentOf(String line) {
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

    private static List<String> read(File file) throws IOException {
        return new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
    }

    /**
     * Keeps the file's own line endings (CRLF or LF), so only the changed lines differ.
     */
    private static void write(File file, List<String> lines) throws IOException {
        String original = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        String separator = original.contains("\r\n") ? "\r\n" : "\n";
        Files.write(file.toPath(), (String.join(separator, lines) + separator).getBytes(StandardCharsets.UTF_8));
    }

}
