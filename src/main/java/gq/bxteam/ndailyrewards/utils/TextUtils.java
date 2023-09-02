package gq.bxteam.ndailyrewards.utils;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<(#[A-Fa-f0-9]{6})>(.*?)</(#[A-Fa-f0-9]{6})>");
    private static final Pattern LEGACY_GRADIENT_PATTERN = Pattern.compile("<(&[A-Za-z0-9])>(.*?)</(&[A-Za-z0-9])>");
    private static final Pattern RGB_PATTERN = Pattern.compile("<(#......)>");

    @SuppressWarnings("deprecation")
    public static String applyColor(String text) {
        if (text == null) {
            return "Not found";
        }

        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
        Matcher legacyGradientMatcher = LEGACY_GRADIENT_PATTERN.matcher(text);
        Matcher rgbMatcher = RGB_PATTERN.matcher(text);

        while (gradientMatcher.find()) {
            Color startColor = Color.decode(gradientMatcher.group(1));
            String between = gradientMatcher.group(2);
            Color endColor = Color.decode(gradientMatcher.group(3));
            BeforeType[] types = BeforeType.detect(between);
            between = BeforeType.replaceColors(between);
            text = text.replace(gradientMatcher.group(0), rgbGradient(between, startColor, endColor, types));
        }

        while (legacyGradientMatcher.find()) {
            char first = legacyGradientMatcher.group(1).charAt(1);
            String between = legacyGradientMatcher.group(2);
            char second = legacyGradientMatcher.group(3).charAt(1);
            ChatColor firstColor = ChatColor.getByChar(first);
            ChatColor secondColor = ChatColor.getByChar(second);
            BeforeType[] types = BeforeType.detect(between);
            between = BeforeType.replaceColors(between);
            if (firstColor == null) {
                firstColor = ChatColor.WHITE;
            }
            if (secondColor == null) {
                secondColor = ChatColor.WHITE;
            }
            text = text.replace(legacyGradientMatcher.group(0), rgbGradient(between, firstColor.getColor(), secondColor.getColor(), types));
        }

        while (rgbMatcher.find()) {
            ChatColor color = ChatColor.of(Color.decode(rgbMatcher.group(1)));
            text = text.replace(rgbMatcher.group(0), color.toString());
        }

        return ChatColor.translateAlternateColorCodes('&', text);
    }

    @SuppressWarnings("deprecation")
    private static String rgbGradient(String str, Color from, Color to, BeforeType[] types) {
        final double[] red = linear(from.getRed(), to.getRed(), str.length());
        final double[] green = linear(from.getGreen(), to.getGreen(), str.length());
        final double[] blue = linear(from.getBlue(), to.getBlue(), str.length());
        StringBuilder before = new StringBuilder();
        for (BeforeType type : types) {
            before.append(ChatColor.getByChar(type.getCode()));
        }
        final StringBuilder builder = new StringBuilder();
        if (str.length() == 1) {
            return ChatColor.of(to) + before.toString() + str;
        }
        for (int i = 0; i < str.length(); i++) {
            builder.append(ChatColor.of(new Color((int) Math.round(red[i]), (int) Math.round(green[i]), (int) Math.round(blue[i])))).append(before).append(str.charAt(i));
        }
        return builder.toString();
    }

    private static double [] linear(double from, double to, int max) {
        final double[] res = new double[max];
        for (int i = 0; i < max; i++) {
            res[i] = from + i * ((to - from) / (max - 1));
        }
        return res;
    }

    public enum BeforeType {
        MIXED('k'),
        BOLD('l'),
        CROSSED('m'),
        UNDERLINED('n'),
        CURSIVE('o');

        private final char code;

        BeforeType(char code) {
            this.code = code;
        }

        public char getCode() {
            return code;
        }

        public static BeforeType[] detect(@NotNull String text) {
            List<BeforeType> values = new ArrayList<>();
            if (text.contains("&k")) {
                values.add(MIXED);
            }
            if (text.contains("&l")) {
                values.add(BOLD);
            }
            if (text.contains("&m")) {
                values.add(CROSSED);
            }
            if (text.contains("&n")) {
                values.add(UNDERLINED);
            }
            if (text.contains("&o")) {
                values.add(CURSIVE);
            }
            return values.toArray(new BeforeType[0]);
        }

        public static String replaceColors(String text) {
            return text.replaceAll("&[kmno]", "");
        }
    }
}
