package icu.twtool.leetcode.editor;

import com.intellij.openapi.fileTypes.FileType;
import icons.LeetCodeIcons;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class LeetCodeFileType implements FileType {

    public static final LeetCodeFileType INSTANCE = new LeetCodeFileType();

    @Override
    public @NotNull String getName() {
        return "LeetCode File";
    }

    @Override
    public @NotNull String getDescription() {
        return "LeetCode File";
    }

    @Override
    public @NotNull String getDefaultExtension() {
        return "lc";
    }

    @Override
    public Icon getIcon() {
        return LeetCodeIcons.LeetCode;
    }

    @Override
    public boolean isBinary() {
        return true;
    }
}
