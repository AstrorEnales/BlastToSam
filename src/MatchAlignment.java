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

public final class MatchAlignment {
    public int Score;
    public String EValue;
    public NameWithLength Reference;
    public NameWithLength Query;
    public boolean QueryReverse = false;
    public boolean SubjectReverse = false;
    public String QuerySequence = "";
    public String SubjectSequence = "";
    public int QueryStart = BlastReader.LENGTH_NOT_READ;
    public int SubjectStart = BlastReader.LENGTH_NOT_READ;
    public int AlignmentLength;
    public int AlignmentPositive;
    public int Flag;

    public final int getEditDistance() {
        return AlignmentLength - AlignmentPositive;
    }

    public final void addAlignmentFragment(String queryLine, String subjectLine) {
        addQueryLineFragment(queryLine);
        addSubjectLineFragment(subjectLine);
    }

    private void addQueryLineFragment(String queryLine) {
        String querySeq = Utils.trimmedSubstring(queryLine, QUERY_IDENTIFIER.length());
        int indexOfSpaceAfterQueryStartNumber = querySeq.indexOf(' ');
        int queryStart = Integer.parseInt(querySeq.substring(0, indexOfSpaceAfterQueryStartNumber));
        querySeq = Utils.trimmedSubstring(querySeq, indexOfSpaceAfterQueryStartNumber + 1);
        int queryEnd = Integer.parseInt(querySeq.substring(querySeq.lastIndexOf(' ') + 1));
        if (queryEnd < queryStart) {
            QueryReverse = true;
            QueryStart = queryEnd;
        } else if (QueryStart == BlastReader.LENGTH_NOT_READ) {
            QueryStart = queryStart;
        }
        QuerySequence += querySeq.substring(0, querySeq.indexOf(' '));
    }

    private static final String QUERY_IDENTIFIER = "Query";

    private void addSubjectLineFragment(String subjectLine) {
        subjectLine = Utils.trimmedSubstring(subjectLine, SUBJECT_IDENTIFIER.length());
        int indexOfSpaceAfterSubjectStartNumber = subjectLine.indexOf(' ');
        int subjectStart = Integer.parseInt(subjectLine.substring(0, indexOfSpaceAfterSubjectStartNumber));
        subjectLine = Utils.trimmedSubstring(subjectLine, indexOfSpaceAfterSubjectStartNumber + 1);
        int subjectEnd = Integer.parseInt(subjectLine.substring(subjectLine.lastIndexOf(' ') + 1));
        if (subjectEnd < subjectStart) {
            SubjectReverse = true;
            Flag |= 0x0010;
            SubjectStart = subjectEnd;
        } else if (SubjectStart == BlastReader.LENGTH_NOT_READ) {
            SubjectStart = subjectStart;
        }
        SubjectSequence += subjectLine.substring(0, subjectLine.indexOf(' '));
    }

    private static final String SUBJECT_IDENTIFIER = "Sbjct";

    public final String getSequence() {
        if (resultSequence != null)
            return resultSequence;
        int pos = QuerySequence.indexOf('-');
        if (pos == -1)
            pos = QuerySequence.length();
        final char[] chars = QuerySequence.toCharArray();
        for (int i = pos; i < chars.length; i++) {
            if (chars[i] != '-') {
                chars[pos++] = chars[i];
            }
        }
        final char[] resultChars = SubjectReverse ? new char[pos] : chars;
        if (SubjectReverse) {
            for (int i = 0; i < pos; i++) {
                char c = chars[i];
                int targetIndex = pos - i - 1;
                if (c == 'a' || c == 'A')
                    resultChars[targetIndex] = 'T';
                else if (c == 't' || c == 'T')
                    resultChars[targetIndex] = 'A';
                else if (c == 'g' || c == 'G')
                    resultChars[targetIndex] = 'C';
                else if (c == 'c' || c == 'C')
                    resultChars[targetIndex] = 'G';
                else if (c == 'r' || c == 'R')
                    resultChars[targetIndex] = 'Y';
                else if (c == 'y' || c == 'Y')
                    resultChars[targetIndex] = 'R';
                else if (c == 'm' || c == 'M')
                    resultChars[targetIndex] = 'K';
                else if (c == 'k' || c == 'K')
                    resultChars[targetIndex] = 'M';
                else
                    resultChars[targetIndex] = c;
            }
        }
        resultSequence = new String(resultChars, 0, pos);
        return resultSequence;
    }

    private String resultSequence;

    public final String getCigar() {
        if (cigarText != null)
            return cigarText;
        StringBuilder cigar = new StringBuilder();
        String type = "=";
        String previous = "";
        int count = 0;
        char[] queryChars = QuerySequence.toCharArray();
        char[] subjectChars = SubjectSequence.toCharArray();
        int i = SubjectReverse ? queryChars.length - 1 : 0;
        for (; SubjectReverse ? i >= 0 : i < queryChars.length; i += SubjectReverse ? -1 : 1) {
            if (queryChars[i] == '-')
                type = "D";
            else if (subjectChars[i] == '-')
                type = "I";
            else if (queryChars[i] == subjectChars[i])
                type = "=";
            else
                type = "X";
            if (previous.length() == 0)
                previous = type;
            if (type.equals(previous)) {
                count++;
            } else if (count >= 1) {
                cigar.append(count);
                cigar.append(previous);
                previous = type;
                count = 1;
            }
        }
        cigar.append(count);
        cigar.append(type);

        // Head clipping
        if (QueryStart > 1) {
            if (SubjectReverse) {
                cigar.append(QueryStart - 1);
                cigar.append("H");
            } else
                cigar.insert(0, (QueryStart - 1) + "H");
        }

        // Tail clipping
        int seqLength = Utils.lengthOfStringWithoutChar(queryChars, '-') + QueryStart - 1;
        if (seqLength < Query.Length) {
            if (SubjectReverse)
                cigar.insert(0, (Query.Length - seqLength) + "H");
            else {
                cigar.append(Query.Length - seqLength);
                cigar.append("H");
            }
        }
        cigarText = cigar.toString();
        return cigarText;
    }

    private String cigarText;

    public final int getMapScore() {
        return 0;
        // TODO: Use E-Value as mapping score (Phred scaled)
        //long eval = Double.valueOf(EValue).longValue();
        //return (int)(eval >= 0 ? 0 : -10 * (Math.log(eval) / Math.log(10)));
    }

    public final String getFlag() {
        return "" + Flag;
    }
}
