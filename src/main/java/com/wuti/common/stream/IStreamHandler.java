package com.wuti.common.stream;

import java.io.OutputStream;

public interface IStreamHandler {
    void handler(OutputStream outputStream, int count) throws Exception;
    void submitCos();

}
