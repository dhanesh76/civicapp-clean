package com.visioners.civic.aws;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {
    
    @Value("${aws.s3.bucket-name}")
    private String bucketName;
    
    @Value("${aws.s3.region}")
    private String region;
    
    @Value("${aws.access-key-id}")
    private String accessKeyId;
    
    @Value("${aws.secret-access-key}")
    private String secretAccessKey;
    
    private S3Client s3Client;
    
    @PostConstruct
    public void initializeS3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
    
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        
        // Generate unique file name with organized folder structure
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = buildFilePath(userId.toString(), "image", fileExtension);
        
        try {
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();
            
            // Upload file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            // Return the public URL
            return generatePublicUrl(fileName);
            
        } catch (S3Exception e) {
            throw new com.visioners.civic.exception.ExternalServiceException("Failed to upload file to S3: " + e.getMessage(), e);
        }
    }
    
    public String uploadAudio(MultipartFile audioFile, Long userId) throws IOException {
        if (audioFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file cannot be empty");
        }

        if(userId == null) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }

        // Generate unique file name with organized folder structure
        String originalFilename = audioFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String fileName = buildFilePath(userId.toString(), "audio", fileExtension);
        
        try {
            // Create PutObjectRequest
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(audioFile.getContentType())
                    .build();
            
            // Upload audio file to S3
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(audioFile.getBytes()));
            
            // Return the public URL
            return generatePublicUrl(fileName);
            
        } catch (S3Exception e) {
            throw new com.visioners.civic.exception.ExternalServiceException("Failed to upload audio file to S3: " + e.getMessage(), e);
        }
    }
    
    private String buildFilePath(String userId, String fileType, String fileExtension) {
        String fileName = UUID.randomUUID().toString() + fileExtension;
        return String.format("%s/%s/%s", userId, fileType, fileName);
    }
    
    private String generatePublicUrl(String fileName) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    public void deleteFile(String fileUrl) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(fileUrl);
            if (key != null && !key.isEmpty()) {
                s3Client.deleteObject(builder -> builder.bucket(bucketName).key(key));
            }
        } catch (S3Exception e) {
            // Log error but don't throw exception to avoid breaking the flow
            System.err.println("Failed to delete file from S3: " + e.getMessage());
        }
    }
    
    private String extractKeyFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        
        // Extract key from S3 URL
        String baseUrl = String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length());
        }
        
        return null;
    }
}