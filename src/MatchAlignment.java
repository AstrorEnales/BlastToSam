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

import java.util.ArrayList;
import java.util.Arrays;

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
        String querySeq = queryLine.substring("Query".length()).trim();
        String queryStartText = querySeq.split(" ")[0];
        int queryStart = Integer.parseInt(queryStartText);
        querySeq = querySeq.substring(querySeq.indexOf(queryStartText) + queryStartText.length()).trim();
        String[] queryParts = querySeq.split(" ");
        int queryEnd = Integer.parseInt(queryParts[queryParts.length - 1]);
        if (queryEnd < queryStart) {
            QueryReverse = true;
            QueryStart = queryEnd;
        } else if (QueryStart == BlastReader.LENGTH_NOT_READ) {
            QueryStart = queryStart;
        }
        querySeq = queryParts[0];
        QuerySequence += querySeq;

        subjectLine = subjectLine.substring("Sbjct".length()).trim();
        String subjectStartText = subjectLine.split(" ")[0];
        int subjectStart = Integer.parseInt(subjectStartText);
        subjectLine = subjectLine.substring(subjectLine.indexOf(subjectStartText) + subjectStartText.length()).trim();
        String[] subjectParts = subjectLine.split(" ");
        int subjectEnd = Integer.parseInt(subjectParts[subjectParts.length - 1]);
        if (subjectEnd < subjectStart) {
            SubjectReverse = true;
            Flag |= 0x0010;
            SubjectStart = subjectEnd;
        } else if (SubjectStart == BlastReader.LENGTH_NOT_READ) {
            SubjectStart = subjectStart;
        }
        SubjectSequence += subjectParts[0];
    }

    public String getSequence() {
        String sequence = QuerySequence.replace("-", "");
        if (SubjectReverse) {
            StringBuilder result = new StringBuilder(sequence).reverse();
            for (int i = 0; i < sequence.length(); i++) {
                int charIndex = FromChars.indexOf("" + result.charAt(i));
                if (charIndex == -1) {
                    System.out.println("Unknown char while building complementary sequence: '" +
                            result.charAt(i) + "', will be left unchanged!");
                } else {
                    result.setCharAt(i, ToChars.get(charIndex).charAt(0));
                }
            }
            sequence = result.toString();
        }
        return sequence;
    }

    private static ArrayList<String> FromChars = new ArrayList<String>(Arrays.asList(
            new String[]{"a", "t", "g", "c", "r", "y", "m", "k", "s", "w", "n", "A", "T", "G", "C", "R", "Y", "M", "K", "S", "W", "N"}));
    private static ArrayList<String> ToChars = new ArrayList<String>(Arrays.asList(
            new String[]{"t", "a", "c", "g", "y", "r", "k", "m", "s", "w", "n", "T", "A", "C", "G", "Y", "R", "K", "M", "S", "W", "N"}));

    public String getCigar(int queryLength) {
        ArrayList<String> cigarParts = new ArrayList<String>();
        if (QueryStart > 1)
            cigarParts.add((QueryStart - 1) + "H");

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
                cigarParts.add(count + previous);
                previous = type;
                count = 1;
            }
        }
        cigarParts.add(count + type);

        int seqLength = QuerySequence.replace("-", "").length() + (QueryStart - 1);
        if (seqLength < queryLength)
            cigarParts.add((queryLength - seqLength) + "H");

        StringBuilder cigar = new StringBuilder();
        if (SubjectReverse)
            for (int i = cigarParts.size() - 1; i >= 0; i--)
                cigar.append(cigarParts.get(i));
        else
            for (String cigarPart : cigarParts)
                cigar.append(cigarPart);
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
