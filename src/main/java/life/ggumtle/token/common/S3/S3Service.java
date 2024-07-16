package life.ggumtle.token.common.S3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3AsyncClient s3AsyncClient;
    private final String bucketName;
    private final String region;

    public S3Service(S3AsyncClient s3AsyncClient,
                     @Value("${cloud.aws.s3.bucket}") String bucketName,
                     @Value("${cloud.aws.region.static}") String region) {
        this.s3AsyncClient = s3AsyncClient;
        this.bucketName = bucketName;
        this.region = region;
    }

    public Mono<String> uploadFile(FilePart filePart) {
        String keyName = Paths.get(System.currentTimeMillis() + "_" + filePart.filename()).toString();
        logger.info("Uploading file: {}", keyName);

        return filePart.content()
                .reduce(new byte[0], (bytes, dataBuffer) -> {
                    byte[] combined = new byte[bytes.length + dataBuffer.readableByteCount()];
                    System.arraycopy(bytes, 0, combined, 0, bytes.length);
                    dataBuffer.read(combined, bytes.length, dataBuffer.readableByteCount());
                    return combined;
                })
                .flatMap(bytes -> {
                    PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(keyName)
                            .build();

                    AsyncRequestBody requestBody = AsyncRequestBody.fromBytes(bytes);
                    CompletableFuture<PutObjectResponse> future = s3AsyncClient.putObject(putObjectRequest, requestBody);

                    return Mono.fromFuture(future)
                            .map(response -> {
                                logger.info("File uploaded to S3 with ETag: {}", response.eTag());
                                return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, keyName);
                            })
                            .subscribeOn(Schedulers.boundedElastic());
                });
    }
}
