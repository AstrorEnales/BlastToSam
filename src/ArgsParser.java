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

public final class ArgsParser {
    public ArgsParser(String[] args) throws ParseException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {
                if (i + 1 >= args.length)
                    throw new ParseException("ERR: Unable to read input filepath. No value was given for the parameter '-i'!");
                InputFilepath = args[i + 1];
            } else if (args[i].equals("-o")) {
                if (i + 1 >= args.length)
                    throw new ParseException("ERR: Unable to read output filepath. No value was given for the parameter '-o'!");
                OutputFilepath = args[i + 1];
            } else if (args[i].equals("-s")) {
                if (i + 1 >= args.length)
                    throw new ParseException("ERR: Unable to read sorting order. No value was given for the parameter '-s'!");
                SortingOrder = args[i + 1];
                if (!isSortingOrderValid()) {
                    throw new ParseException("ERR: Invalid sorting order '" + SortingOrder + "'! Valid values are: " +
                            SORT_ORDER_UNKNOWN + ", " + SORT_ORDER_UNSORTED + ", " + SORT_ORDER_QUERYNAME + " and " + SORT_ORDER_COORDINATE + ".");
                }
            } else if (args[i].equals("--help") || args[i].equals("-h")) {
                System.out.println("BlastToSam Help");
                System.out.println("\tExample: java -jar BlastToSam.jar -i query.blastn -s " + SORT_ORDER_QUERYNAME + " -o result.sam");
                System.out.println("\tParameter:");
                System.out.println("\t-h, --help\tThis help page");
                System.out.println("\t-i [VALUE]\tSpecifies the blast input filepath (Required)");
                System.out.println("\t-o [VALUE]\tSpecifies the sam output filepath (Required)");
                System.out.println("\t-s [VALUE]\tSpecifies the sorting order [" + SORT_ORDER_UNKNOWN + ", " +
                        SORT_ORDER_UNSORTED + ", " + SORT_ORDER_QUERYNAME + ", " + SORT_ORDER_COORDINATE + "] (Optional)");
                HelpPageShown = true;
                return;
            }
        }
        if (InputFilepath.length() == 0)
            throw new ParseException("ERR: No valid input filepath is given!");
        if (OutputFilepath.length() == 0)
            throw new ParseException("ERR: No valid output filepath is given!");
    }

    public String InputFilepath = "";
    public String OutputFilepath = "";
    public String SortingOrder = SORT_ORDER_UNKNOWN;
    public boolean HelpPageShown;

    public static final String SORT_ORDER_UNKNOWN = "unknown";
    public static final String SORT_ORDER_UNSORTED = "unsorted";
    public static final String SORT_ORDER_QUERYNAME = "queryname";
    public static final String SORT_ORDER_COORDINATE = "coordinate";

    private boolean isSortingOrderValid() {
        return SortingOrder.equals(SORT_ORDER_COORDINATE) || SortingOrder.equals(SORT_ORDER_UNKNOWN) ||
                SortingOrder.equals(SORT_ORDER_QUERYNAME) || SortingOrder.equals(SORT_ORDER_UNSORTED);
    }

    class ParseException extends Exception {
        public ParseException(String message) {
            super(message);
        }
    }
}
