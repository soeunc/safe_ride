package com.example.safe_ride.weather.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@Slf4j
@Controller
@RequestMapping("/safe-ride")
public class WeatherC {

    @Value("${weather.api.decode-key}")
    private String apiKey;

    @Value("${kakao.rest.key}")
    private String kakaoRestApiKey;

    public static int TO_GRID = 0;
    public static int TO_GPS = 1;

    public static class LatXLngY {
        public double lat;
        public double lng;

        public double x;
        public double y;

    }

    private LatXLngY convertGRID_GPS(int mode, double lat_X, double lng_Y )
    {
        double RE = 6371.00877; // 지구 반경(km)
        double GRID = 5.0; // 격자 간격(km)
        double SLAT1 = 30.0; // 투영 위도1(degree)
        double SLAT2 = 60.0; // 투영 위도2(degree)
        double OLON = 126.0; // 기준점 경도(degree)
        double OLAT = 38.0; // 기준점 위도(degree)
        double XO = 43; // 기준점 X좌표(GRID)
        double YO = 136; // 기1준점 Y좌표(GRID)

        //
        // LCC DFS 좌표변환 ( code : "TO_GRID"(위경도->좌표, lat_X:위도,  lng_Y:경도), "TO_GPS"(좌표->위경도,  lat_X:x, lng_Y:y) )
        //


        double DEGRAD = Math.PI / 180.0;
        double RADDEG = 180.0 / Math.PI;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD;
        double slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD;
        double olat = OLAT * DEGRAD;

        double sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn);
        double sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5);
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn;
        double ro = Math.tan(Math.PI * 0.25 + olat * 0.5);
        ro = re * sf / Math.pow(ro, sn);
        LatXLngY rs = new LatXLngY();

        if (mode == TO_GRID) {
            rs.lat = lat_X;
            rs.lng = lng_Y;
            double ra = Math.tan(Math.PI * 0.25 + (lat_X) * DEGRAD * 0.5);
            ra = re * sf / Math.pow(ra, sn);
            double theta = lng_Y * DEGRAD - olon;
            if (theta > Math.PI) theta -= 2.0 * Math.PI;
            if (theta < -Math.PI) theta += 2.0 * Math.PI;
            theta *= sn;
            rs.x = Math.floor(ra * Math.sin(theta) + XO + 0.5);
            rs.y = Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        }
        else {
            rs.x = lat_X;
            rs.y = lng_Y;
            double xn = lat_X - XO;
            double yn = ro - lng_Y + YO;
            double ra = Math.sqrt(xn * xn + yn * yn);
            if (sn < 0.0) {
                ra = -ra;
            }
            double alat = Math.pow((re * sf / ra), (1.0 / sn));
            alat = 2.0 * Math.atan(alat) - Math.PI * 0.5;

            double theta = 0.0;
            if (Math.abs(xn) <= 0.0) {
                theta = 0.0;
            }
            else {
                if (Math.abs(yn) <= 0.0) {
                    theta = Math.PI * 0.5;
                    if (xn < 0.0) {
                        theta = -theta;
                    }
                }
                else theta = Math.atan2(xn, yn);
            }
            double alon = theta / sn + olon;
            rs.lat = Math.round(alat * RADDEG);
            rs.lng = Math.round(alon * RADDEG);
        }
        return rs;
    }

    @GetMapping("/weather")
    public String viewWeather(Model model) {
        return "weather/weather";
    }

    @GetMapping("/weather/fcst")
    public ResponseEntity<String> getWeatherData(@RequestParam String base_date,
                                                 @RequestParam String base_time,
                                                 @RequestParam Double lat,
                                                 @RequestParam Double lon) {
        // base_date = "20240427" base_time = "0500" lat=37.2009716824183 lon=127.09583577621638
        final String BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0";
        final LatXLngY pos = convertGRID_GPS(TO_GRID, lat, lon);

        String url = String.format("%s/getVilageFcst?serviceKey=%s&pageNo=1&numOfRows=20&dataType=JSON&base_date=%s&base_time=%s&nx=%d&ny=%d",
                BASE_URL, apiKey, base_date, base_time, Math.round(pos.x), Math.round(pos.y));


        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForEntity(url, String.class);
        } catch (HttpClientErrorException e) {
            // 클라이언트 에러 발생 시
            return ResponseEntity.badRequest().build();
        } catch (HttpServerErrorException e) {
            // 서버 에러 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // 기타 예외 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/weather/trans-coord")
    public ResponseEntity<String> getTransCoordData(@RequestParam Double lat,
                                                    @RequestParam Double lon) {
        // uri 주소 생성
        URI uri = UriComponentsBuilder
                .fromUriString("https://dapi.kakao.com")
                .path("/v2/local/geo/transcoord.json")
                .queryParam("x", lon)  // query parameter가 필요한 경우 이와 같이 사용
                .queryParam("y", lat)
                .queryParam("input_coord", "WGS84")
                .queryParam("output_coord", "TM")
                .encode()
                .build()
                .toUri();

//        System.out.println(uri.toString());

        // HttpHeaders 객체 생성 및 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

        // HttpEntity 객체 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplete = new RestTemplate();
            return restTemplete.exchange(
                    uri,
                    HttpMethod.GET,
                    request,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            // 클라이언트 에러 발생 시
            return ResponseEntity.badRequest().build();
        } catch (HttpServerErrorException e) {
            // 서버 에러 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // 기타 예외 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/weather/getNearbyMsrstnList")
    public ResponseEntity<String> getNearbyMsrstnList(@RequestParam Double tmX,
                                                      @RequestParam Double tmY) {
        // uri 주소 생성
        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr")
                .path("/B552584/MsrstnInfoInqireSvc/getNearbyMsrstnList")
                .queryParam("serviceKey", apiKey)
                .queryParam("returnType", "json")
                .queryParam("tmX", tmX)
                .queryParam("tmY", tmY)
                .encode()
                .build()
                .toUri();

        // HttpHeaders 객체 생성 및 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Language", "ko-KR");

        // HttpEntity 객체 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplete = new RestTemplate();
            return restTemplete.exchange(
                    uri,
                    HttpMethod.GET,
                    request,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            // 클라이언트 에러 발생 시
            return ResponseEntity.badRequest().build();
        } catch (HttpServerErrorException e) {
            // 서버 에러 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // 기타 예외 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/weather/airInfo")
    public ResponseEntity<String> getAirInfo(
            @RequestParam String stationName,
            @RequestParam String dataTerm) {
        // uri 주소 생성
        URI uri = UriComponentsBuilder
                .fromUriString("https://apis.data.go.kr")
                .path("/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty")
                .queryParam("serviceKey", apiKey)
                .queryParam("returnType", "json")
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .queryParam("stationName", stationName)
                .queryParam("dataTerm", dataTerm)
                .queryParam("ver", 1.1)
                .encode()
                .build()
                .toUri();

        // HttpHeaders 객체 생성 및 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Language", "ko-KR");

        // HttpEntity 객체 생성
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            RestTemplate restTemplete = new RestTemplate();
            return restTemplete.exchange(
                    uri,
                    HttpMethod.GET,
                    request,
                    String.class
            );
        } catch (HttpClientErrorException e) {
            // 클라이언트 에러 발생 시
            return ResponseEntity.badRequest().build();
        } catch (HttpServerErrorException e) {
            // 서버 에러 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            // 기타 예외 발생 시
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
