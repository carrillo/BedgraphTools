BedgraphTools
=============

Tools to modify and characterize bedgraph files.

This Toolbox was created to extract gene architecture features from transcriptome files in the BED file format (http://genome.ucsc.edu/FAQ/FAQformat).

All scripts are designed to be saved as jars and run from terminal using java -jar SCRIPT ARGUMENTS. Use -Xms and -Xmx arguments to dedicate more memory to java (java -Xms4000m -Xmx12000m -jar SCRIPT ARGUMENTS). 
To get script specific information, please call the scripts without any arguments. 

1. Contained scripts
- FilterBEDEntryOnLength
- FilterBEDOnIDs
- FilterExonsOnPosition (Filters the exon list produced by GetExons by their relative position in the transcript. Works strand specific).
- FilterIntronsOnPosition (see FilterExonOnPosition). 
- GetExons
- GetIntrons
- GetBidirectionalTranscriptPairs (Returns transcript pairs facing away from each other and being within a defined distance).
- GetTSSorPolyASite (Extracts start or end of transcript or any other BED entry. Works strand specific.)
- FilterExonsOnPosition (works on BED file returned by GetExons).
