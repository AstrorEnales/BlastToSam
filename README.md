BlastToSam
==========

Tool for converting BLASTN query results to the SAM format.

This tool is compliant to SAM file format specification v1.4 (http://samtools.github.io/hts-specs/SAMv1.pdf) and validated with picard 1.124 ValidateSamFile.

It was tested with the following versions of BLASTN:

- BLASTN 2.2.25+

Why use this tool?
==========
Until now there was only a small perl script (blast2sam.pl) in the samtools package to convert blast query outputs to sam format and it didn't even work properly. After some searching i found another perl script (http://bioinformatics.ovsa.fr/41/blast2sam) which was a complete rewrite of the one in the samtools and it works really well.

BlastToSam goes one step further and implements all sorting modes specified in the sam specification and can be set via command line parameter -s.

Command Line Example
==========
```
java -jar BlastToSam.jar -i query.blastn -s queryname -o result.sam
```

Parameter
==========
Parameter | Description
--------------- | ----------------
-h, --help   | This help page
-i [VALUE] | Specifies the blast input filepath (Required)
-o [VALUE] | Specifies the sam output filepath (Required)
-s [VALUE] | Specifies the sorting order (unknown, unsorted, queryname, coordinate) (Optional)

Issues, Bugs, Feedback
==========
If you found a general bug or one with a specific version of blastn please create an issue on this github page. If you want to give general feedback mail me or open a discussion issue.
