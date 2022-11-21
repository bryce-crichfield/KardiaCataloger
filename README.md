### Trading Card Scanner

This project was develop as a rapid proof-of-concept to develop a cataloging system for managing trading card inventoy.  This application uses real-time hardware accelerated image processing, optical character recognition (OCR), and a graphical user interface to scan and recognize the title of trading cards.  Currently configured for Yugioh trading cards, the application's core processing pipeline can be used for any input media.  

Through the use of a custom pre-processing chain written in OpenCL, the input video feed is fed into Google's Open Source OCR engine, Tesseract, to achieve a 95% recognition accuracy (based on prelimary tests).  The graphical user interface provides a real-time, buffered video feed of both the raw webcam input and the filtered output.



#### Example Output:
![alt text](https://github.com/bryce-crichfield/card_scanner/blob/master/example_out.png)
