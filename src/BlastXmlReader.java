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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BlastXmlReader extends BlastReader {
    public BlastXmlReader(String filename) throws IOException {
        File inputFile = new File(filename);
        XMLInputFactory factory = XMLInputFactory.newInstance();
        NameWithLength currentQuery = null;
        NameWithLength currentReference = null;
        MatchAlignment currentAlignment = null;
        int flag = 0x0100;
        try {
            XMLEventReader reader = factory.createXMLEventReader(new FileReader(inputFile));
            while (reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.getEventType() == XMLStreamReader.START_ELEMENT) {
                    StartElement startElement = event.asStartElement();
                    String localName = startElement.getName().getLocalPart();
                    if (localName.equals(ITERATION_IDENTIFIER)) {
                        currentQuery = new NameWithLength();
                        flag ^= 0x0100;
                    } else if (localName.equals(QUERY_NAME_IDENTIFIER)) {
                        currentQuery.Name = getValue(reader);
                    } else if (localName.equals(QUERY_LENGTH_IDENTIFIER)) {
                        currentQuery.Length = Integer.parseInt(getValue(reader));
                        currentQuery = addToListOrGetEntry(queries, currentQuery);
                    } else if (localName.equals(ITERATION_MESSAGE_IDENTIFIER)) {
                        currentQuery.NoHits = getValue(reader).equals("No hits found");
                    } else if (localName.equals(HIT_IDENTIFIER)) {
                        currentReference = new NameWithLength();
                    } else if (localName.equals(HIT_NAME_IDENTIFIER)) {
                        currentReference.Name = getValue(reader);
                    } else if (localName.equals(HIT_LENGTH_IDENTIFIER)) {
                        currentReference.Length = Integer.parseInt(getValue(reader));
                        currentReference = addToListOrGetEntry(references, currentReference);
                    } else if (localName.equals(HSP_IDENTIFIER)) {
                        currentAlignment = new MatchAlignment();
                        currentAlignment.Query = currentQuery;
                        currentAlignment.Reference = currentReference;
                        currentAlignment.Flag = flag;
                        flag = 0x0100;
                        alignments.add(currentAlignment);
                    } else if (localName.equals(HSP_QUERY_SEQUENCE_IDENTIFIER)) {
                        currentAlignment.QuerySequence = getValue(reader);
                    } else if (localName.equals(HSP_HIT_SEQUENCE_IDENTIFIER)) {
                        currentAlignment.SubjectSequence = getValue(reader);
                    } else if (localName.equals(HSP_BITS_SCORE_IDENTIFIER)) {
                        currentAlignment.Score = (int) (Math.ceil(Double.parseDouble(getValue(reader))));
                    } else if (localName.equals(HSP_EVALUE_IDENTIFIER)) {
                        currentAlignment.EValue = getValue(reader);
                    } else if (localName.equals(HSP_QUERY_FROM_IDENTIFIER)) {
                        currentAlignment.QueryStart = Integer.parseInt(getValue(reader));
                    } else if (localName.equals(HSP_QUERY_TO_IDENTIFIER)) {
                        int queryEnd = Integer.parseInt(getValue(reader));
                        if (queryEnd < currentAlignment.QueryStart) {
                            currentAlignment.QueryReverse = true;
                            currentAlignment.QueryStart = queryEnd;
                        }
                    } else if (localName.equals(HSP_HIT_FROM_IDENTIFIER)) {
                        currentAlignment.SubjectStart = Integer.parseInt(getValue(reader));
                    } else if (localName.equals(HSP_HIT_TO_IDENTIFIER)) {
                        int subjectEnd = Integer.parseInt(getValue(reader));
                        if (subjectEnd < currentAlignment.SubjectStart) {
                            currentAlignment.SubjectReverse = true;
                            currentAlignment.Flag |= 0x0010;
                            currentAlignment.SubjectStart = subjectEnd;
                        }
                    } else if (localName.equals(HSP_ALIGNMENT_LENGTH_IDENTIFIER)) {
                        currentAlignment.AlignmentLength = Integer.parseInt(getValue(reader));
                    } else if (localName.equals(HSP_ALIGNMENT_POSITIVE_IDENTIFIER)) {
                        currentAlignment.AlignmentPositive = Integer.parseInt(getValue(reader));
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException e) {
            System.out.println("Error while opening file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getValue(XMLEventReader reader) throws XMLStreamException {
        return reader.nextEvent().asCharacters().getData();
    }

    private static final String ITERATION_IDENTIFIER = "Iteration";
    private static final String QUERY_NAME_IDENTIFIER = "Iteration_query-def";
    private static final String QUERY_LENGTH_IDENTIFIER = "Iteration_query-len";
    private static final String ITERATION_MESSAGE_IDENTIFIER = "Iteration_message";
    private static final String HIT_IDENTIFIER = "Hit";
    private static final String HIT_NAME_IDENTIFIER = "Hit_def";
    private static final String HIT_LENGTH_IDENTIFIER = "Hit_len";
    private static final String HSP_IDENTIFIER = "Hsp";
    private static final String HSP_BITS_SCORE_IDENTIFIER = "Hsp_bit-score";
    private static final String HSP_EVALUE_IDENTIFIER = "Hsp_evalue";
    private static final String HSP_QUERY_FROM_IDENTIFIER = "Hsp_query-from";
    private static final String HSP_QUERY_TO_IDENTIFIER = "Hsp_query-to";
    private static final String HSP_HIT_FROM_IDENTIFIER = "Hsp_hit-from";
    private static final String HSP_HIT_TO_IDENTIFIER = "Hsp_hit-to";
    private static final String HSP_QUERY_SEQUENCE_IDENTIFIER = "Hsp_qseq";
    private static final String HSP_HIT_SEQUENCE_IDENTIFIER = "Hsp_hseq";
    private static final String HSP_ALIGNMENT_LENGTH_IDENTIFIER = "Hsp_align-len";
    private static final String HSP_ALIGNMENT_POSITIVE_IDENTIFIER = "Hsp_positive";
}
