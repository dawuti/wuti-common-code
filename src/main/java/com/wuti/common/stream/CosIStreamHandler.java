package com.wuti.common.stream;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.qcloud.cos.model.CompleteMultipartUploadResult;
import com.qcloud.cos.model.InitiateMultipartUploadResult;
import com.qcloud.cos.model.PartETag;
import com.qcloud.cos.model.UploadPartResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

public class CosIStreamHandler implements IStreamHandler {

    private final Logger logger = LoggerFactory.getLogger(CosIStreamHandler.class);

    private String key;
    private ITencentCOSService tencentCOSService;
    private InitiateMultipartUploadResult initiateMultipartUploadResult;
    private List<PartETag> partETags = Lists.newArrayList();
    private String uploadId = null;
    private String fileUrl;

    public CosIStreamHandler(String key, ITencentCOSService tencentCOSService) {
        this.key = key;
        this.tencentCOSService = tencentCOSService;
    }

    @Override
    public void handler(OutputStream outputStream, int count) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(((ByteArrayOutputStream)outputStream).toByteArray());

        int available = 0;
        try {
            available = inputStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (count == 0) {
                logger.debug("initiateMultipartUpload cos开始,key={}", key);
                initiateMultipartUploadResult = tencentCOSService.initiateMultipartUpload(key);
                uploadId = initiateMultipartUploadResult.getUploadId();
                logger.debug("initiateMultipartUpload cos结束,uploadId={},key={}", uploadId, key);
            }

            if (StringUtils.isBlank(uploadId)) {
                return;
            }
            int partNumber = count + 1;
            logger.debug("上传cos开始,key={},partNumber={},inputStream.available()={},uploadId={}", key, partNumber, available, uploadId);
            UploadPartResult uploadPartResult = tencentCOSService.uploadPart(key, uploadId, partNumber, available, inputStream);
            logger.debug("上传cos开始,key={},partNumber={},inputStream.available()={},uploadId={},UploadPartResult={}", key, partNumber, available, uploadId, JSON.toJSONString(uploadPartResult));

            partETags.add(new PartETag(uploadPartResult.getPartNumber(), uploadPartResult.getETag()));
        } catch (Exception e) {
            logger.info("分片调用cos失败,key={}", key, e);
            tencentCOSService.abortMultipartUpload(key, uploadId);
            throw e;
        } finally {

            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void submitCos() {
        logger.debug("completeMultipartUpload cos开始,key={}", key);
        CompleteMultipartUploadResult completeMultipartUploadResult = tencentCOSService.completeMultipartUpload(key, uploadId, partETags);
        fileUrl = tencentCOSService.getFileUrl(completeMultipartUploadResult);
        logger.debug("completeMultipartUpload cos结束,key={},result ={},fileUrl={}", key, JSON.toJSONString(completeMultipartUploadResult), fileUrl);

    }

}
