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

import java.util.HashMap;

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
        for (int i = pos + 1; i < chars.length; i++) {
            if (chars[i] != '-') {
                chars[pos++] = chars[i];
            }
        }
        if (SubjectReverse) {
            for (int i = 0; i < pos; i++) {
                char c = chars[i];
                if (c >= 0 && c < complement.length)
                    chars[pos - i - 1] = complement[c];
                else {
                    chars[pos - i - 1] = c;
                    System.out.println("Unknown char while building complementary sequence: '" +
                            c + "', will be left unchanged!");
                }
            }
        }
        resultSequence = new String(chars, 0, pos);
        return resultSequence;
    }

    private static char[] complement;
    private String resultSequence;

    public MatchAlignment() {
        if (complement == null) {
            HashMap<String, String> ComplementaryMap = new HashMap<String, String>();
            ComplementaryMap.put("a", "t");
            ComplementaryMap.put("t", "a");
            ComplementaryMap.put("g", "c");
            ComplementaryMap.put("c", "g");
            ComplementaryMap.put("r", "y");
            ComplementaryMap.put("y", "r");
            ComplementaryMap.put("m", "k");
            ComplementaryMap.put("k", "m");
            ComplementaryMap.put("s", "s");
            ComplementaryMap.put("w", "w");
            ComplementaryMap.put("n", "n");
            ComplementaryMap.put("A", "T");
            ComplementaryMap.put("T", "A");
            ComplementaryMap.put("G", "C");
            ComplementaryMap.put("C", "G");
            ComplementaryMap.put("R", "Y");
            ComplementaryMap.put("Y", "R");
            ComplementaryMap.put("M", "K");
            ComplementaryMap.put("K", "M");
            ComplementaryMap.put("S", "S");
            ComplementaryMap.put("W", "W");
            ComplementaryMap.put("N", "N");
            complement = new char['x'];
            for (int i = 0; i < complement.length; i++) {
                char c = (char) (i);
                complement[i] = ComplementaryMap.containsKey("" + c) ? ComplementaryMap.get("" + c).charAt(0) : c;
            }
        }
    }

    public final String getCigar() {
        if (cigarText != null)
            return cigarText;
        StringBuilder cigar = new StringBuilder(QueryStart > 1 ? ((QueryStart - 1) + "H") : "");
        if (QuerySequence.equals(SubjectSequence)) {
            if (SubjectReverse)
                cigar.insert(0, SubjectSequence.length() + "=");
            else {
                cigar.append(SubjectSequence.length());
                cigar.append("=");
            }
        } else {
            String type = "=";
            String previous = "=";
            int count = 0;
            char[] queryChars = QuerySequence.toCharArray();
            char[] subjectChars = SubjectSequence.toCharArray();
            for (int i = 0; i < queryChars.length; i++) {
                if (queryChars[i] == '-')
                    type = "D";
                else if (subjectChars[i] == '-')
                    type = "I";
                else if (queryChars[i] == subjectChars[i])
                    type = "=";
                else
                    type = "X";
                if (type.equals(previous)) {
                    count++;
                } else if (count >= 1) {
                    if (SubjectReverse)
                        cigar.insert(0, count + previous);
                    else {
                        cigar.append(count);
                        cigar.append(previous);
                    }
                    previous = type;
                    count = 1;
                }
            }
            if (SubjectReverse)
                cigar.insert(0, count + type);
            else {
                cigar.append(count);
                cigar.append(type);
            }
        }
        int seqLength = Utils.lengthOfStringWithoutChar(QuerySequence, '-') + QueryStart - 1;
        if (seqLength < Query.Length) {
            if (SubjectReverse)
                cigar.insert(0, (Query.Length - seqLength) + "H");
            else {
                cigar.append((Query.Length - seqLength));
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
