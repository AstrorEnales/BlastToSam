BlastToSam
==========

Tool for converting BLASTN query results to the SAM format. The supported output format of BLASTN is currently limited to the default (-outfmt 0).

This tool is compliant to SAM file format specification v1.4 (http://samtools.github.io/hts-specs/SAMv1.pdf) and validated with picard 1.124 ValidateSamFile.

Jar Download
==========
The newest version can always be downloaded from [here.](http://astror.pavo.uberspace.de/ci/blasttosam/BlastToSam.jar)
 
[![Build Status](https://travis-ci.org/AstrorEnales/BlastToSam.svg)](https://travis-ci.org/AstrorEnales/BlastToSam)

Why use this tool?
==========
Until now there was only a small perl script (blast2sam.pl) in the samtools package to convert blast query outputs to sam format and it didn't even work properly. After some searching i found another perl script (http://bioinformatics.ovsa.fr/41/blast2sam) which was a complete rewrite of the one in the samtools and it works really well.

BlastToSam goes one step further and implements all sorting modes specified in the sam specification and can be set via command line parameter -s.

Command Line Example
==========
```
java -jar BlastToSam.jar -i query.blastn -s queryname -o result.sam
```

Example to produce a sam file compatible with the igv viewer:

```
java -jar BlastToSam.jar -i query.blastn -s coordinate -n cut -o result.sam
```

Parameter
==========
Parameter | Description
--------------- | ----------------
-h, --help | This help page
-i [VALUE] | Specifies the blast input filepath (Required)
-o [VALUE] | Specifies the sam output filepath (Required)
-s [VALUE] | Specifies the sorting order (unknown, unsorted, queryname, coordinate) (Optional)
-n [VALUE] | Specifies the name mode (cut, complete) (Optional)
-r         | Removes query entries without hits (Optional)

The name mode 'cut' parameter specifies that all reference and query sequence names are cut at the first occuring space character. This is for example needed to use the resulting sam file in the igv viewer.

Issues, Bugs, Feedback
==========
If you found a general bug or one with a specific version of blastn please create an issue on this github page. If you want to give general feedback mail me or open a discussion issue.

Testing
==========
BlastToSam was tested with the following versions of BLASTN:

- BLASTN 2.2.18+
- BLASTN 2.2.19+
- BLASTN 2.2.21+
- BLASTN 2.2.22+
- BLASTN 2.2.23+
- BLASTN 2.2.24+
- BLASTN 2.2.25+
- BLASTN 2.2.26+
- BLASTN 2.2.27+
- BLASTN 2.2.28+
- BLASTN 2.2.29+
- BLASTN 2.2.30+

Changesets
==========
2015-05-12

- Improved performance
- Added console information about the current state
- Added -r parameter

2015-05-11

- Building the complementary sequence now catches unknown chars and handles N's properly

2014-11-24

- Fixed a bug in the sorting orders
- Added name mode parameter (see parameter overview)
