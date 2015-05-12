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

public class MatchAlignment {
    public int Score;
    public String EValue;
    public int EditDistance;
    public NameWithLength Reference;
    public NameWithLength Query;
    public boolean QueryReverse = false;
    public boolean SubjectReverse = false;
    public String QuerySequence = "";
    public String SubjectSequence = "";
    public int QueryStart = BlastReader.LENGTH_NOT_READ;
    public int SubjectStart = BlastReader.LENGTH_NOT_READ;
    public int Flag;

    public void addAlignmentFragment(String queryLine, String subjectLine) {
        addQueryLineFragment(queryLine);
        addSubjectLineFragment(subjectLine);
    }

    private void addQueryLineFragment(String queryLine) {
        String querySeq = queryLine.substring(QUERY_IDENTIFIER.length()).trim();
        int indexOfSpaceAfterQueryStartNumber = querySeq.indexOf(' ');
        int queryStart = Integer.parseInt(querySeq.substring(0, indexOfSpaceAfterQueryStartNumber));
        querySeq = querySeq.substring(indexOfSpaceAfterQueryStartNumber + 1).trim();
        int queryEnd = Integer.parseInt(querySeq.substring(querySeq.lastIndexOf(' ') + 1));
        if (queryEnd < queryStart) {
            QueryReverse = true;
            QueryStart = queryEnd;
        } else if (QueryStart == BlastReader.LENGTH_NOT_READ) {
            QueryStart = queryStart;
        }
        querySeq = querySeq.substring(0, querySeq.indexOf(' '));
        QuerySequence += querySeq;
    }

    private static final String QUERY_IDENTIFIER = "Query";

    private void addSubjectLineFragment(String subjectLine) {
        subjectLine = subjectLine.substring(SUBJECT_IDENTIFIER.length()).trim();
        int indexOfSpaceAfterSubjectStartNumber = subjectLine.indexOf(' ');
        int subjectStart = Integer.parseInt(subjectLine.substring(0, indexOfSpaceAfterSubjectStartNumber));
        subjectLine = subjectLine.substring(indexOfSpaceAfterSubjectStartNumber + 1).trim();
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

    public String getSequence() {
        String sequence = QuerySequence.replace("-", "");
        if (SubjectReverse) {
            StringBuilder result = new StringBuilder(sequence).reverse();
            for (int i = 0; i < sequence.length(); i++) {
                String key = "" + result.charAt(i);
                if (ComplementaryMap.containsKey(key))
                    result.setCharAt(i, ComplementaryMap.get(key).charAt(0));
                else
                    System.out.println("Unknown char while building complementary sequence: '" +
                            result.charAt(i) + "', will be left unchanged!");
            }
            sequence = result.toString();
        }
        return sequence;
    }

    private static HashMap<String, String> ComplementaryMap;

    public MatchAlignment() {
        if (ComplementaryMap == null) {
            ComplementaryMap = new HashMap<String, String>();
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
        }
    }

    public String getCigar(int queryLength) {
        StringBuilder cigar = new StringBuilder(QueryStart > 1 ? ((QueryStart - 1) + "H") : "");
        String type = "=";
        String previous = "=";
        int count = 0;
        for (int i = 0; i < QuerySequence.length(); i++) {
            if (QuerySequence.charAt(i) == '-')
                type = "D";
            else if (SubjectSequence.charAt(i) == '-')
                type = "I";
            else if (QuerySequence.charAt(i) == SubjectSequence.charAt(i))
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
        int seqLength = QuerySequence.replace("-", "").length() + (QueryStart - 1);
        if (seqLength < queryLength) {
            if (SubjectReverse)
                cigar.insert(0, (queryLength - seqLength) + "H");
            else {
                cigar.append((queryLength - seqLength));
                cigar.append("H");
            }
        }
        return cigar.toString();
    }

    public int getMapScore() {
        return 0;
        // TODO: Use E-Value as mapping score (Phred scaled)
        //long eval = Double.valueOf(EValue).longValue();
        //return (int)(eval >= 0 ? 0 : -10 * (Math.log(eval) / Math.log(10)));
    }

    public String getFlag() {
        return "" + Flag;
    }
}
