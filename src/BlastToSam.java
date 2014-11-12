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

// Version 2014-11-11

import java.io.*;
import java.util.*;

public class BlastToSam {
    public static void main(String[] args) throws IOException {
        ArgsParser argsParser;
        try {
            argsParser = new ArgsParser(args);
        } catch (ArgsParser.ParseException ex) {
            System.out.println("ERR: " + ex.getMessage());
            return;
        }
        if (argsParser.HelpPageShown)
            return;

        BlastReader blast = new BlastReader(argsParser.InputFilepath);

        ArrayList<String> readGroupNameTable = new ArrayList<String>();
        HashMap<String, Integer> referenceNameTable = new HashMap<String, Integer>();
        for (Query query : blast.Queries) {
            if (!readGroupNameTable.contains(query.QueryName))
                readGroupNameTable.add(query.QueryName);
            for (ReferenceMatch match : query.Matches)
                if (!referenceNameTable.containsKey(match.ReferenceName))
                    referenceNameTable.put(match.ReferenceName, match.Length);
        }

        ArrayList<MatchAlignment> alignments = new ArrayList<MatchAlignment>();
        for (Query query : blast.Queries)
            for (ReferenceMatch match : query.Matches)
                for (MatchAlignment alignment : match.Alignments)
                    alignments.add(alignment);

        ArrayList<String> referenceNameKeys = new ArrayList<String>(referenceNameTable.keySet());
        if (argsParser.SortingOrder.equals(ArgsParser.SORT_ORDER_COORDINATE)) {
            Collections.sort(referenceNameKeys);
            Collections.sort(alignments, new Comparator<MatchAlignment>() {
                @Override
                public int compare(MatchAlignment query1, MatchAlignment query2) {
                    return query1.Parent.Parent.QueryName.compareTo(query2.Parent.Parent.QueryName);
                }
            });
        } else if (argsParser.SortingOrder.equals(ArgsParser.SORT_ORDER_QUERYNAME)) {
            Collections.sort(alignments, new Comparator<MatchAlignment>() {
                @Override
                public int compare(MatchAlignment query1, MatchAlignment query2) {
                    int nameCompare = query1.Parent.ReferenceName.compareTo(query2.Parent.ReferenceName);
                    return nameCompare != 0 ? nameCompare :
                            (query1.SubjectStart > query2.SubjectStart ? 1 :
                                    (query1.SubjectStart < query2.SubjectStart ? -1 : 0));
                }
            });
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(argsParser.OutputFilepath));
        writeTabDelimLine(writer, new String[]{"@HD", "VN:1.4", "SO:" + argsParser.SortingOrder});
        String comment = "Blast query result" + (blast.DatabaseName.length() > 0 ?
                " for database '" + blast.DatabaseName + "'" : "") + ". Converted with BlastToSam";
        writeTabDelimLine(writer, new String[]{"@CO", comment});
        for (int i = 0; i < readGroupNameTable.size(); i++) {
            writeTabDelimLine(writer, new String[]{
                    "@RG", "ID:" + (i + 1), "DS:" + cleanQueryName(readGroupNameTable.get(i)), "PL:*", "SM:*"
            });
        }
        for (String key : referenceNameKeys) {
            writeTabDelimLine(writer, new String[]{
                    "@SQ", "SN:" + cleanQueryName(key), "LN:" + referenceNameTable.get(key)
            });
        }

        for (MatchAlignment alignment : alignments) {
            ReferenceMatch match = alignment.Parent;
            Query query = match.Parent;
            String shortQueryName = cleanQueryName(query.QueryName);
            String shortReferenceName = cleanQueryName(match.ReferenceName);
            String[] contents = new String[]{
                    shortQueryName,               // QNAME
                    alignment.getFlag(),          // FLAG
                    shortReferenceName,           // RNAME
                    "" + alignment.SubjectStart,  // POS
                    "" + alignment.getMapScore(), // MAPQ     Use E-Value as mapping score (Phred scaled)
                    alignment.getCigar(),         // CIGAR
                    "*",                          // RNEXT
                    "0",                          // PNEXT
                    "0",                          // TLEN
                    alignment.getSequence(),      // SEQ
                    "*",                          // QUAL
                    "AS:i:" + alignment.Score,
                    "XE:f:" + alignment.EValue,
                    "RG:Z:" + (readGroupNameTable.indexOf(query.QueryName) + 1),
                    "NM:i:" + alignment.EditDistance
            };
            writeTabDelimLine(writer, contents);
        }
        writer.close();
    }

    private static String cleanQueryName(String name) {
        return name.replace("\t", " ");
    }

    private static void writeTabDelimLine(BufferedWriter writer, String[] contents) throws IOException {
        if (contents.length == 0) {
            writeLine(writer, "");
            return;
        }
        String result = contents[0];
        for (int i = 1; i < contents.length; i++)
            result += "\t" + contents[i];
        writeLine(writer, result);
    }

    private static void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }

    static String trimStart(String text) {
        return text.replaceAll("^\\s+", "");
    }
}


