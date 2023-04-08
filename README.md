# TessMemoryLeakSample

A sample java project to reproduce memory leak when using Tess4j for parallel OCR processing on PDF files in Docker environment.

To reproduce, build the docker image and run using the following commands:

    docker build -t sample .
    docker run -t sample

