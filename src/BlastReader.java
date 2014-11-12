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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public final class BlastReader {
    public BlastReader(String filepath) throws IOException {
        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filepath));

        Query currentQuery = null;
        ReferenceMatch currentReference = null;
        MatchAlignment currentAlignment = null;
        int flag = 0x0100;
        while ((line = reader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (line.startsWith(DATABASE_IDENTIFIER)) {
                DatabaseName = line.substring(DATABASE_IDENTIFIER.length()).trim().replace("\t", " ");
            } else if (line.startsWith(QUERY_IDENTIFIER)) {
                currentQuery = new Query();
                Queries.add(currentQuery);
                currentQuery.QueryName = line.substring(QUERY_IDENTIFIER.length()).trim();
                flag ^= 0x0100;
            } else if (currentQuery != null) {
                if (currentQuery.Length == LENGTH_NOT_READ) {
                    if (line.startsWith(LENGTH_IDENTIFIER))
                        currentQuery.Length = Integer.parseInt(line.substring(LENGTH_IDENTIFIER.length()).trim());
                    else
                        currentQuery.QueryName += line.trim();
                } else {
                    if (line.startsWith(REFERENCE_IDENTIFIER)) {
                        currentReference = new ReferenceMatch();
                        currentReference.Parent = currentQuery;
                        currentQuery.Matches.add(currentReference);
                        currentReference.ReferenceName = line.substring(REFERENCE_IDENTIFIER.length()).trim();
                    } else if (currentReference != null) {
                        if (currentReference.Length == LENGTH_NOT_READ) {
                            if (line.startsWith(LENGTH_IDENTIFIER))
                                currentReference.Length = Integer.parseInt(line.substring(LENGTH_IDENTIFIER.length()).trim());
                            else
                                currentReference.ReferenceName += line.trim();
                        } else {
                            if (trimmedLine.startsWith(SCORE_IDENTIFIER)) {
                                currentAlignment = new MatchAlignment();
                                currentAlignment.Parent = currentReference;
                                currentAlignment.Flag = flag;
                                flag = 0x0100;
                                currentReference.Alignments.add(currentAlignment);
                                String scoreText = trimmedLine.substring(SCORE_IDENTIFIER.length()).split("bits")[0].trim();
                                currentAlignment.Score = (int) (Math.ceil(Double.parseDouble(scoreText)));
                                currentAlignment.EValue = line.substring(line.indexOf(EXPECT_IDENTIFIER) + EXPECT_IDENTIFIER.length()).split(",")[0].trim();
                            } else if (currentAlignment != null && trimmedLine.startsWith(IDENTITIES_IDENTIFIER)) {
                                String scoreText = trimmedLine.substring(IDENTITIES_IDENTIFIER.length()).split(Pattern.quote("("))[0].trim();
                                String[] parts = scoreText.split("/");
                                currentAlignment.EditDistance = Integer.parseInt(parts[1]) - Integer.parseInt(parts[0]);
                            } else if (currentAlignment != null && line.startsWith("Query ")) {
                                String alignLine = reader.readLine();
                                String subjectLine = reader.readLine();
                                currentAlignment.addAlignmentFragment(line, alignLine, subjectLine);
                            }
                        }
                    }
                }
            }
        }
        reader.close();
    }

    public ArrayList<Query> Queries = new ArrayList<Query>();
    public String DatabaseName = "";

    private static final String DATABASE_IDENTIFIER = "Database:";
    private static final String QUERY_IDENTIFIER = "Query=";
    private static final String LENGTH_IDENTIFIER = "Length=";
    private static final String REFERENCE_IDENTIFIER = ">";
    private static final String SCORE_IDENTIFIER = "Score =";
    private static final String EXPECT_IDENTIFIER = "Expect =";
    private static final String IDENTITIES_IDENTIFIER = "Identities =";
    static final int LENGTH_NOT_READ = -999;
}
