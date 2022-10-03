# ANS Graph compression

This repository focuses on the application of asymmetric numeral systems to
large-scale graph compression.

Asymmetric numeral systems are a family of entropy encoders that obtains
compression quality comparable to arithmetic coding, thus optimal for a
given source of symbols, while having a decoding speed similar to Huffman
coding. They were presented by Jarek Duda and are heavily used in several
different scenarios, such as Linux kernels, Facebook Zstandard, JPEG XL
and many others.

This work presents an application of such encoders on large-scale web and
social graphs. The final proposed methodology is the result of three iterations 
and combines asymmetric numeral systems, instantaneous codes and
patched frame of reference encoding.

The experimental results show how this methodology saves as much as 76 
percent of space with respect to quasi-succinct representations and is 
capable of storing graphs in as low as 3.5 bits per link.

This was my Master's thesis at the University of Milan, the complete work can be read [here](https://github.com/tomfran/ANS-Graph-compression/tree/main/thesis/thesis.pdf).