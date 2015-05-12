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

import java.io.*;
import java.util.*;

public class BlastToSam {
    public static void main(String[] args) throws IOException {
        ArgsParser argsParser;
        try {
            argsParser = new ArgsParser(args);
        } catch (ArgsParser.ParseException ex) {
            System.out.println("[Error] " + ex.getMessage());
            return;
        }
        if (argsParser.HelpPageShown)
            return;
        System.out.println("[Info] BlastToSam v2015-05-12");
        System.out.println("[Info] Converting '" + argsParser.InputFilepath + "' to '" + argsParser.OutputFilepath + "'");
        System.out.println("[Info] SortingOrder is " + argsParser.SortingOrder);
        System.out.println("[Info] NameMode is " + argsParser.NameMode);
        if (argsParser.RemoveQueriesWithoutHits)
            System.out.println("[Info] Removing queries without hits is on");

        Date startTime = new Date();

        System.out.println("[Info] Reading input file...");
        BlastReader blast = new BlastReader(argsParser.InputFilepath);

        ArrayList<String> readGroupNameTable = new ArrayList<String>();
        for (NameWithLength query : blast.queries) {
            if (!readGroupNameTable.contains(query.Name) && (!argsParser.RemoveQueriesWithoutHits || !query.NoHits))
                readGroupNameTable.add(query.Name);
        }

        System.out.println("[Info] Query count: " + blast.queries.size());
        System.out.println("[Info] Reference count: " + blast.references.size());
        System.out.println("[Info] Alignments count: " + blast.alignments.size());

        if (argsParser.SortingOrder.equals(ArgsParser.SORT_ORDER_COORDINATE)) {
            System.out.println("[Info] Sorting in coordinate mode...");
            Collections.sort(blast.references, new Comparator<NameWithLength>() {
                @Override
                public int compare(NameWithLength a, NameWithLength b) {
                    return a.Name.compareTo(b.Name);
                }
            });
            Collections.sort(blast.alignments, new Comparator<MatchAlignment>() {
                @Override
                public int compare(MatchAlignment query1, MatchAlignment query2) {
                    int nameCompare = query1.Reference.Name.compareTo(query2.Reference.Name);
                    return nameCompare != 0 ? nameCompare :
                            (query1.SubjectStart > query2.SubjectStart ? 1 :
                                    (query1.SubjectStart < query2.SubjectStart ? -1 : 0));
                }
            });
        } else if (argsParser.SortingOrder.equals(ArgsParser.SORT_ORDER_QUERYNAME)) {
            System.out.println("[Info] Sorting in queryname mode...");
            Collections.sort(blast.alignments, new Comparator<MatchAlignment>() {
                @Override
                public int compare(MatchAlignment query1, MatchAlignment query2) {
                    return query1.Query.Name.compareTo(query2.Query.Name);
                }
            });
        }

        boolean cutNameModeOn = argsParser.NameMode.equals(ArgsParser.NAME_MODE_CUT);
        System.out.println("[Info] Writing output...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(argsParser.OutputFilepath));
        writeTabDelimLine(writer, new String[]{"@HD", "VN:1.4", "SO:" + argsParser.SortingOrder});
        String comment = "Blast query result" + (blast.DatabaseName.length() > 0 ?
                " for database '" + blast.DatabaseName + "'" : "") + ". Converted with BlastToSam";
        writeTabDelimLine(writer, new String[]{"@CO", comment});
        for (int i = 0; i < readGroupNameTable.size(); i++) {
            writeTabDelimLine(writer, new String[]{
                    "@RG", "ID:" + (i + 1), "DS:" + cleanQueryName(readGroupNameTable.get(i), cutNameModeOn), "PL:*", "SM:*"
            });
        }
        for (NameWithLength ref : blast.references) {
            writeTabDelimLine(writer, new String[]{
                    "@SQ", "SN:" + cleanQueryName(ref.Name, cutNameModeOn), "LN:" + ref.Length
            });
        }

        for (MatchAlignment alignment : blast.alignments) {
            String shortQueryName = cleanQueryName(alignment.Query.Name, cutNameModeOn);
            String shortReferenceName = cleanQueryName(alignment.Reference.Name, cutNameModeOn);
            String[] contents = new String[]{
                    shortQueryName,                               // QNAME
                    alignment.getFlag(),                          // FLAG
                    shortReferenceName,                           // RNAME
                    "" + alignment.SubjectStart,                  // POS
                    "" + alignment.getMapScore(),                 // MAPQ
                    alignment.getCigar(alignment.Query.Length),   // CIGAR
                    "*",                                          // RNEXT
                    "0",                                          // PNEXT
                    "0",                                          // TLEN
                    alignment.getSequence(),                      // SEQ
                    "*",                                          // QUAL
                    "AS:i:" + alignment.Score,
                    "XE:f:" + alignment.EValue,
                    "RG:Z:" + (readGroupNameTable.indexOf(alignment.Query.Name) + 1),
                    "NM:i:" + alignment.EditDistance
            };
            writeTabDelimLine(writer, contents);
        }
        writer.close();

        Date endTime = new Date();
        long elapsed = endTime.getTime() - startTime.getTime();
        System.out.println("[Info] Took " + (elapsed < 1000 ? (elapsed + "ms") : ((elapsed / 1000.0) + "sec")));
    }

    private static String cleanQueryName(String name, boolean cutNameModeOn) {
        name = name.replace('\t', ' ');
        if (cutNameModeOn) {
            int indexOfSpace = name.indexOf(' ');
            if (indexOfSpace != -1)
                name = name.substring(0, indexOfSpace);
        }
        return name;
    }

    private static void writeTabDelimLine(BufferedWriter writer, String[] contents) throws IOException {
        if (contents.length > 0) {
            writer.write(contents[0]);
            for (int i = 1; i < contents.length; i++) {
                writer.write('\t');
                writer.write(contents[i]);
            }
        }
        writer.newLine();
    }
}
