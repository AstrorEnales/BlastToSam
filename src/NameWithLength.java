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

class NameWithLength {
    public String Name;
    public int Length = BlastReader.LENGTH_NOT_READ;
    public boolean NoHits;

    public String getCleanName(boolean cutNameModeOn) {
        if (cleanName == null)
            cleanName = cleanName(Name, cutNameModeOn);
        return cleanName;
    }

    private String cleanName;

    public static String cleanName(String name, boolean cutNameModeOn) {
        name = name.replace('\t', ' ');
        if (cutNameModeOn) {
            int indexOfSpace = name.indexOf(' ');
            if (indexOfSpace != -1)
                name = name.substring(0, indexOfSpace);
        }
        return name;
    }
}
