package com.github.archx.m3u8d.download.body;

import com.github.archx.m3u8d.download.listener.ProgressListener;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

import java.io.IOException;

/**
 * ProgressResponseBody
 *
 * @author archx
 * @since 2020/9/5 8:55
 */
public class ProgressResponseBody extends ResponseBody {

    private ResponseBody body;
    private ProgressListener progressListener;
    private long oldPoint = 0;

    public ProgressResponseBody(Response originalResponse, long startPos, ProgressListener progressListener) {
        this.body = originalResponse.body();
        this.progressListener = progressListener;
        this.oldPoint = startPos;
    }

    @Override
    public MediaType contentType() {
        return body.contentType();
    }

    @Override
    public long contentLength() {
        return body.contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(new ForwardingSource(body.source()) {
            private long bytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long read = super.read(sink, byteCount);
                bytesRead += read == -1 ? 0 : read;
                long size = contentLength() + oldPoint;
                if (progressListener != null) {
                    double v = new Long(bytesRead + oldPoint).doubleValue() / size;
                    progressListener.processing("", "", (int) (v * 100));
                }
                return bytesRead;
            }
        });
    }

}
