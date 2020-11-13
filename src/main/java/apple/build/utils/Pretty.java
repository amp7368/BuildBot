package apple.build.utils;

public class Pretty {
    public static String commas(String number) {
        System.out.println(number);
        char[] chars = number.toCharArray();
        StringBuilder s = new StringBuilder();
        for (int index = chars.length - 1, i = 0; index >= 0; index--) {
            s.append(chars[index]);
            if (index!=0 && i++ == 2) {
                i = 0;
                s.append(',');
            }
        }
        return s.reverse().toString();
    }
}
