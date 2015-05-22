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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class BlastTextReader extends BlastReader {
    public BlastTextReader(String filename) throws IOException {
        File inputFile = new File(filename);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        String line;
        currentQuery = null;
        currentReference = null;
        currentAlignment = null;
        flag = 0x0100;
        boolean foundDatabaseName = false;
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0)
                continue;
            String trimmedLine = Utils.trim(line);
            if (trimmedLine.length() == 0)
                continue;
            if (!foundDatabaseName && Utils.startsWith(line, DATABASE_IDENTIFIER)) {
                DatabaseName = Utils.trimmedSubstring(line, DATABASE_IDENTIFIER.length());
                foundDatabaseName = true;
            } else if (Utils.startsWith(line, QUERY_IDENTIFIER)) {
                startNewQuery(line);
            } else if (currentQuery != null) {
                if (currentQuery.Length == LENGTH_NOT_READ) {
                    readQueryInformation(trimmedLine);
                } else {
                    if (Utils.startsWith(line, NO_HITS_IDENTIFIER)) {
                        currentQuery.NoHits = true;
                    } else if (Utils.startsWith(line, REFERENCE_IDENTIFIER)) {
                        startNewReference(line);
                    } else if (currentReference != null) {
                        if (currentReference.Length == LENGTH_NOT_READ) {
                            readReferenceInformation(trimmedLine);
                        } else {
                            readAlignmentInformation(reader, line, trimmedLine);
                        }
                    }
                }
            }
        }
        reader.close();
    }

    private NameWithLength currentQuery = null;
    private NameWithLength currentReference = null;
    private MatchAlignment currentAlignment = null;
    private int flag = 0x0100;

    private void startNewQuery(String line) {
        currentQuery = new NameWithLength();
        queries.add(currentQuery);
        currentQuery.Name = Utils.trimmedSubstring(line, QUERY_IDENTIFIER_LEN);
        flag ^= 0x0100;
    }

    private void readQueryInformation(String line) {
        if (Utils.startsWith(line, LENGTH_IDENTIFIER)) {
            currentQuery.Length = Integer.parseInt(Utils.trimmedSubstring(line, LENGTH_IDENTIFIER_LEN));
        } else {
            currentQuery.Name += line;
        }
    }

    private void startNewReference(String line) {
        currentReference = new NameWithLength();
        currentReference.Name = Utils.trimmedSubstring(line, 1);
    }

    private void readReferenceInformation(String line) {
        if (Utils.startsWith(line, LENGTH_IDENTIFIER)) {
            currentReference.Length = Integer.parseInt(Utils.trimmedSubstring(line, LENGTH_IDENTIFIER_LEN));
            currentReference = addToListOrGetEntry(references, currentReference);
        } else {
            currentReference.Name += line;
        }
    }

    private void readAlignmentInformation(BufferedReader reader, String line, String trimmedLine) throws IOException {
        if (Utils.startsWith(trimmedLine, SCORE_IDENTIFIER)) {
            currentAlignment = new MatchAlignment();
            currentAlignment.Query = currentQuery;
            currentAlignment.Reference = currentReference;
            currentAlignment.Flag = flag;
            flag = 0x0100;
            alignments.add(currentAlignment);
            String scoreText = trimmedLine.substring(SCORE_IDENTIFIER_LEN);
            scoreText = Utils.trimmedSubstring(scoreText, 0, scoreText.indexOf(BITS_IDENTIFIER));
            currentAlignment.Score = (int) (Math.ceil(Double.parseDouble(scoreText)));
            currentAlignment.EValue = Utils.trimmedSubstring(line, line.indexOf(EXPECT_IDENTIFIER) + EXPECT_IDENTIFIER_LEN);
        } else if (currentAlignment != null && Utils.startsWith(trimmedLine, IDENTITIES_IDENTIFIER)) {
            String scoreText = trimmedLine.substring(IDENTITIES_IDENTIFIER_LEN);
            scoreText = Utils.trimmedSubstring(scoreText, 0, scoreText.indexOf('('));
            int indexOfSlash = scoreText.indexOf('/');
            currentAlignment.AlignmentLength = Integer.parseInt(scoreText.substring(indexOfSlash + 1));
            currentAlignment.AlignmentPositive = Integer.parseInt(scoreText.substring(0, indexOfSlash));
        } else if (currentAlignment != null && Utils.startsWith(line, QUERY_ALIGNMENT_IDENTIFIER)) {
            reader.readLine(); // Align line
            String subjectLine = reader.readLine();
            currentAlignment.addAlignmentFragment(line, subjectLine);
        }
    }

    private static final String DATABASE_IDENTIFIER = "Database:";
    private static final String QUERY_IDENTIFIER = "Query=";
    private static final int QUERY_IDENTIFIER_LEN = QUERY_IDENTIFIER.length();
    private static final String QUERY_ALIGNMENT_IDENTIFIER = "Query ";
    private static final String BITS_IDENTIFIER = "bits";
    private static final String LENGTH_IDENTIFIER = "Length=";
    private static final int LENGTH_IDENTIFIER_LEN = LENGTH_IDENTIFIER.length();
    private static final String NO_HITS_IDENTIFIER = "***** No hits found *****";
    private static final String REFERENCE_IDENTIFIER = ">";
    private static final String SCORE_IDENTIFIER = "Score =";
    private static final int SCORE_IDENTIFIER_LEN = SCORE_IDENTIFIER.length();
    private static final String EXPECT_IDENTIFIER = "Expect =";
    private static final int EXPECT_IDENTIFIER_LEN = EXPECT_IDENTIFIER.length();
    private static final String IDENTITIES_IDENTIFIER = "Identities =";
    private static final int IDENTITIES_IDENTIFIER_LEN = IDENTITIES_IDENTIFIER.length();
}
