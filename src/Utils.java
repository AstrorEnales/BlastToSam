/*
The MIT License (MIT)

Copyright (c) 2014 Marcel Friedrichs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

public final class Utils {
    public static String trimmedSubstring(final String line, final int start) {
        return trimmedSubstring(line, start, line.length());
    }

    public static String trimmedSubstring(final String line, int start, int end) {
        while (start < end && line.charAt(start) == ' ')
            start++;
        while (end > start && line.charAt(end - 1) == ' ')
            end--;
        return line.substring(start, end);
    }

    public static String trim(final String line) {
        int len = line.length();
        int start = 0;
        int end = len;
        while (start < end && line.charAt(start) == ' ')
            start++;
        while (end > start && line.charAt(end - 1) == ' ')
            end--;
        if (end == len)
            return start == 0 ? line : line.substring(start);
        return line.substring(start, end);
    }

    public static String remove(final String str, final char remove) {
        int firstOccurrence = str.indexOf(remove);
        if (firstOccurrence == -1) {
            return str;
        }
        final char[] chars = str.toCharArray();
        int pos = firstOccurrence;
        for (int i = firstOccurrence + 1; i < chars.length; i++) {
            if (chars[i] != remove) {
                chars[pos++] = chars[i];
            }
        }
        return new String(chars, 0, pos);
    }

    public static int lengthOfStringWithoutChar(final String str, final char remove) {
        int length = 0;
        final char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != remove) {
                length++;
            }
        }
        return length;
    }

    public static boolean startsWith(final String line, final String start) {
        if (line.length() < start.length())
            return false;
        for (int i = 0; i < start.length(); i++)
            if (start.charAt(i) != line.charAt(i))
                return false;
        return true;
    }
}
