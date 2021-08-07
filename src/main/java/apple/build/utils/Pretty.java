package apple.build.utils;

public class Pretty {
    public static String commas(String number) {
        char[] chars = number.toCharArray();
        StringBuilder s = new StringBuilder();
        for (int index = chars.length - 1, i = 0; index >= 0; index--) {
            s.append(chars[index]);
            if (index != 0 && i++ == 2) {
                i = 0;
                s.append(',');
            }
        }
        return s.reverse().toString();
    }

    public static String uppercaseFirst(String s) {
        char[] chars = s.toCharArray();
        if (chars.length == 0) return s;
        chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            chars[i] = Character.toLowerCase(chars[i]);
        }
        return new String(chars);
    }
}
