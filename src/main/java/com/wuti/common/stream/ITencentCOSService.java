package com.wuti.common.stream;

import com.qcloud.cos.model.*;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ITencentCOSService {

    /**
     * 初始化分片上传
     *
     * @return
     * @Param
     */
    public InitiateMultipartUploadResult initiateMultipartUpload(String key);

    /**
     * 分片上传 第n片
     *
     * @param uploadId
     * @param partNumber
     * @param partSize
     * @param inputStream
     * @return
     */
    public UploadPartResult uploadPart(String key, String uploadId, int partNumber, long partSize, InputStream inputStream);

    /**
     * 完成分片上传
     *
     * @param key
     * @param uploadId
     * @param partETags
     * @return
     */
    public CompleteMultipartUploadResult completeMultipartUpload(String key, String uploadId, List<PartETag> partETags);

    public String getFileUrl(CompleteMultipartUploadResult result);

    /**
     * 中断分片上传
     *
     * @param key
     * @param uploadId
     */
    public void abortMultipartUpload(String key, String uploadId);


}
