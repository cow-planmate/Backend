package com.example.planmate.common.externalAPI;

import com.example.planmate.common.valueObject.PlaceVO;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@Component
@RequiredArgsConstructor
public class GooglePlaceDetails {
    @Value("${api.google.key}")
    private String googleApiKey;
    @Value("${spring.img.url}")
    private String imgUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public List<? extends PlaceVO> searchGooglePlaceDetails(List<? extends PlaceVO> placeVOs){

        String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?placeid=";
        String googleApiKeyUrl = "&fields=photos&key=" + googleApiKey;
        for(PlaceVO placeVO : placeVOs){
            String placeId = placeVO.getPlaceId();
            File file = new File(imgUrl + placeId + ".webp");
            if(file.exists()){
               continue;
            }
            String searchUrl = detailsUrl + placeId + googleApiKeyUrl;
            ResponseEntity<Map> detailsResponse = restTemplate.getForEntity(searchUrl, Map.class);

            // 응답에서 사진 참조값 추출
            Map<String, Object> result = (Map<String, Object>) detailsResponse.getBody().get("result");
            Map<String, Object> photo = ((List<Map<String, Object>>) result.get("photos")).get(0);
            String photoReference = (String) photo.get("photo_reference");

            // 2. 사진 참조값으로 이미지 파일 다운로드
            String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + photoReference + "&key=" + googleApiKey;
            ResponseEntity<byte[]> photoBytesResponse = restTemplate.getForEntity(photoUrl, byte[].class);

            byte[] imageBytes = photoBytesResponse.getBody();
            try {
                ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
                byte[] webpBytes = image.bytes(new WebpWriter(6, 80, 4, false));

                if (webpBytes != null) {
                    File outputFile = new File(imgUrl + placeId + ".webp");
                    File parentDir = outputFile.getParentFile();
                    if (!parentDir.exists()) {
                        parentDir.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    fos.write(webpBytes);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return placeVOs;
    }
}
