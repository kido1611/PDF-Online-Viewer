# PDF Online viewer
View online PDF file. This project is created with Kotlin support.


## How it work
----
The PDF file will be downloaded first, before display it. The downloaded file will be saved on default files folder (/data/data/{package name}/files). If the file was exist, PDF file won't be downloaded.


## Dependency
---
This project is running with some dependency:
1. AndroidPdfViewer [https://github.com/barteksc/AndroidPdfViewer](https://github.com/barteksc/AndroidPdfViewer), used to show PDF
2. KTOR [https://ktor.io/clients/http-client/multiplatform.html](https://ktor.io/clients/http-client/multiplatform.html), used to download PDF file
