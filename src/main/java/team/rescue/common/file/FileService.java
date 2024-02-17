package team.rescue.common.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.rescue.error.exception.ServiceException;
import team.rescue.error.type.ServiceError;
import team.rescue.util.ImageUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

	private final AmazonS3 s3Client;

	@Value("${cloud.aws.s3.bucket}")
	private String bucketName;
	private static final String S3_BUCKET_DIR_PREFIX = "origin/";

	public String uploadImageToS3(MultipartFile image) {

		log.info("[S3 이미지 업로드] imageName={}", image.getOriginalFilename());

		// 파일 존재 여부 확인
		validateFile(image);

		if (image.getOriginalFilename() == null) {
			throw new ServiceException(ServiceError.FILE_EXTENSION_INVALID);
		}
		String fileName =
				S3_BUCKET_DIR_PREFIX + ImageUtil.generateImageName(image.getOriginalFilename());

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentType(image.getContentType());
		objectMetadata.setContentLength(image.getSize());

		try {
			PutObjectRequest putObjectRequest = new PutObjectRequest(
					bucketName, fileName, image.getInputStream(), objectMetadata
			);
			s3Client.putObject(putObjectRequest);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return s3Client.getUrl(bucketName, fileName).toString();
	}

	public void deleteImages(String  imagePath) {
		log.info("[S3 이미지 삭제] imagePath={}", imagePath);

		// 파일 이름 추출
		String fileName = S3_BUCKET_DIR_PREFIX + extractFileName(imagePath);

		try {
			DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, fileName);

			s3Client.deleteObject(deleteObjectRequest);
			log.info("S3 이미지 '{}' 삭제 성공", fileName);
		} catch (Exception e) {
			log.error("S3 이미지 '{}' 삭제 실패: {}", fileName, e.getMessage());
			throw new ServiceException(ServiceError.FILE_DELETION_FAILED);
		}
	}

	private String extractFileName(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			throw new ServiceException(ServiceError.FILE_PATH_INVALID);
		}
		return filePath.substring(filePath.lastIndexOf("/") + 1);
	}

	private void validateFile(MultipartFile file) {
		if (file.isEmpty()) {
			throw new ServiceException(ServiceError.FILE_NOT_EXIST);
		}
		if (file.getOriginalFilename() == null) {
			throw new ServiceException(ServiceError.FILE_EXTENSION_INVALID);
		}
	}
}
