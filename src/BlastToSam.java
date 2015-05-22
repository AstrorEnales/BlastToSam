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
        System.out.println("[Info] BlastToSam v2015-05-22");
        System.out.println("[Info] Converting '" + argsParser.InputFilepath + "' to '" + argsParser.OutputFilepath + "'");
        System.out.println("[Info] SortingOrder is " + argsParser.SortingOrder);
        System.out.println("[Info] NameMode is " + argsParser.NameMode);
        if (argsParser.RemoveQueriesWithoutHits)
            System.out.println("[Info] Removing queries without hits is on");

        Date startTime = new Date();

        System.out.println("[Info] Reading input file...");
        Date startReadTime = new Date();
        BlastReader blast;
        if (argsParser.InputFilepath.endsWith(".xml")) {
            blast = new BlastXmlReader(argsParser.InputFilepath);
        } else {
            blast = new BlastTextReader(argsParser.InputFilepath);
        }
        long elapsedRead = new Date().getTime() - startReadTime.getTime();

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
        Date startWriteTime = new Date();
        BufferedWriter writer = new BufferedWriter(new FileWriter(argsParser.OutputFilepath));
        // Header
        writer.write("@HD\tVN:1.4\tSO:");
        writer.write(argsParser.SortingOrder);
        writer.newLine();
        // Comment
        writer.write("@CO\tBlast query result");
        if (blast.DatabaseName.length() > 0)
            writer.write(" for database '" + blast.DatabaseName + "'");
        writer.write(". Converted with BlastToSam");
        writer.newLine();
        // Queries
        int queryId = 1;
        int queriesWithHits = 0;
        for (NameWithLength query : blast.queries) {
            if (!argsParser.RemoveQueriesWithoutHits || !query.NoHits) {
                query.Id = queryId++;
                writer.write("@RG\tID:" + query.Id);
                writer.write("\tDS:");
                writer.write(query.getCleanName(cutNameModeOn));
                writer.write("\tPL:*\tSM:*");
                writer.newLine();
                queriesWithHits++;
            }
        }
        // References
        for (NameWithLength ref : blast.references) {
            writer.write("@SQ\tSN:");
            writer.write(ref.getCleanName(cutNameModeOn));
            writer.write("\tLN:" + ref.Length);
            writer.newLine();
        }
        // Alignments
        for (MatchAlignment alignment : blast.alignments) {
            // QNAME  FLAG  RNAME  POS  MAPQ  CIGAR  RNEXT  PNEXT  TLEN  SEQ  QUAL
            writer.write(alignment.Query.getCleanName(cutNameModeOn));
            writer.write("\t" + alignment.getFlag());
            writer.write('\t');
            writer.write(alignment.Reference.getCleanName(cutNameModeOn));
            writer.write("\t" + alignment.SubjectStart);
            writer.write("\t" + alignment.getMapScore());
            writer.write('\t');
            writer.write(alignment.getCigar());
            writer.write("\t*\t0\t0\t");
            writer.write(alignment.getSequence());
            writer.write("\t*\tAS:i:" + alignment.Score);
            writer.write("\tXE:f:" + alignment.EValue);
            writer.write("\tRG:Z:" + alignment.Query.Id);
            writer.write("\tNM:i:" + alignment.getEditDistance());
            writer.newLine();
        }
        writer.close();
        long elapsedWrite = new Date().getTime() - startWriteTime.getTime();
        long elapsedTotal = new Date().getTime() - startTime.getTime();
        System.out.println("[Info] Query count: " + blast.queries.size() + ", with hits: " + queriesWithHits);
        System.out.println("[Info] Reference count: " + blast.references.size());
        System.out.println("[Info] Alignments count: " + blast.alignments.size());
        System.out.println("[Info] Read  Time " + (elapsedRead < 1000 ? (elapsedRead + "ms") : ((elapsedRead / 1000.0) + "sec")));
        System.out.println("[Info] Write Time " + (elapsedWrite < 1000 ? (elapsedWrite + "ms") : ((elapsedWrite / 1000.0) + "sec")));
        System.out.println("[Info] Total Time " + (elapsedTotal < 1000 ? (elapsedTotal + "ms") : ((elapsedTotal / 1000.0) + "sec")));
    }
}
