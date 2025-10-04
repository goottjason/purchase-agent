package com.jason.purchase_agent.util.downloader;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageDownloader {
    public static List<String> downloadImagesToLocal(String code, List<String> imageLinks) {
        List<String> filePaths = new ArrayList<>();
        String saveDir = "C:\\imgupload";
        new File(saveDir).mkdirs();
        int maxRetry = 3;
        for (int n = 0; n < imageLinks.size(); n++) {
            String filePath = Paths.get(saveDir, code + "-" + (n + 1) + ".jpg").toString();
            boolean success = false;
            int retryCount = 0;
            while (!success && retryCount < maxRetry) {
                try (InputStream in = new URL(imageLinks.get(n)).openStream()) {
                    // 썸네일로 1000x1000 리사이즈+최적화
                    Thumbnails.of(in)
                            .size(1000, 1000)
                            .outputFormat("jpg")
                            .outputQuality(0.89f)
                            .toFile(filePath);
                    success = true;
                } catch (Exception e) {
                    retryCount++;
                    if (retryCount >= maxRetry) {
                        // 최종 실패: log 기록, 상태반영
                        log.error("[이미지 다운로드 실패] code={}, idx={}, url={}, msg={}",
                                code, (n + 1), imageLinks.get(n), e.getMessage());
                        filePath = null; // 또는 placeholder 세팅
                    } else {
                        try { Thread.sleep(350); } catch (InterruptedException ignore) {}
                    }
                }
            }
            if (filePath != null) filePaths.add(filePath); // 성공한 것만 세팅
        }
        return filePaths;
    }

    /**
     * 폴더에서 지정된 상품코드가 포함된 이미지 파일을 확장자 기준으로 10장까지 찾아 반환한다.
     *
     * @param code 찾을 상품 코드 (예: "250922IHB001")
     * @return 이미지 파일 리스트
     */
    public static List<File> findProductImageFiles(String code) {
        File dir = new File("C:/imgupload"); // 폴더 경로 수정 가능
        String[] extensions = { ".jpg", ".jpeg", ".png", ".gif", ".bmp" };

        List<File> result = new ArrayList<>();
        if (!dir.exists() || !dir.isDirectory()) {
            return result;
        }

        File[] files = dir.listFiles();
        if (files == null) return result;

        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            if (!file.isFile()) continue;
            // 확장자와 코드 둘 다 포함되어야함
            boolean hasCode = fileName.contains(code.toLowerCase());
            boolean hasValidExt = false;
            for (String ext : extensions) {
                if (fileName.endsWith(ext)) {
                    hasValidExt = true;
                    break;
                }
            }
            if (hasCode && hasValidExt) {
                result.add(file);
                // 최대 10장 제한
                if (result.size() >= 10) break;
            }
        }
        return result;
    }
}
