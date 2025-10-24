# TokenReference : jwt4j-lite
**jwt4j-lite** — Java 및 Spring Boot 환경에서 사용할 수 있는 **경량화된 JWT(Json Web Token) 라이브러리** 


---
# The Goal
이 프로젝트는 **io.jsonwebtoken:jjwt-api** 라이브러리를 대체할 수 있는 **JwtProvider** 역할을 수행하며,  
JWT의 발급과 검증 로직을 커스터마이징할 수 있도록 설계되었습니다.

Main Goals:

- `io.jsonwebtoken:jjwt-*` 라이브러리를 대체 가능한 경량 JWT 엔진 제공  
- 최소한의 설정으로 커스텀 클레임 발급 및 검증 기능 지원  
- 소스와 설정 파일의 규격을 통일하여 코드 품질 및 가독성 향상  
- Spring Boot와 자연스럽게 통합되어 빠른 개발 및 유지보수 가능

---



# Release
- [v1.0.0-rc](./RELEASENOTE.md#v100-2025-09-12-) ✅
- [v0.1.2-beta](./RELEASENOTE.md#v100-2024-04-24-) ✅
- [v0.1.1-beta](./RELEASENOTE.md#v100-2024-01-30-) ✅

# Process
  ![](doc/README_20250121_page-0001.jpg)
  ![](doc/README_20250121_page-0002.jpg)
  ![](doc/README_20250121_page-0003.jpg)

# Guides
1. HTTP Method
   1. API Method List
   2. API Response Format
   3. API Detailed description
      1. 토큰 발행 / Token Issue
      2. 토큰 검증 / Token Verify
      3. 토큰 정보 추출 / Token claim extraction

[//]: # (1. JAR 라이브러리 호출 방식 )

[//]: # (   1. JAR 라이브러리 호출 가이드)

[//]: # (   2. Service Method List )

[//]: # (   3. Service Response Format )

[//]: # (   4. Service 상세설명)

[//]: # (      1. 토큰 발행)

[//]: # (      2. 토큰 검증)

[//]: # (      3. 토큰 정보 추출)


## HTTP Method 
### 1. API Method List 

| Method Name | HTTP Method | Description |
|-------------|-------------|----------|
| api/v1/createToken        | POST        | 토큰 발행    |
| api/v1/verifyToken        | POST        | 토큰 검증    |
| api/v1/extractClaim        | POST        | 토큰 정보 추출 |


### 2. API Response Format
| Key        | Value | Description                      |
|------------|-------|----------------------------------|
| claim      | Map<String, String>  | 토큰 정보 포함되는 데이터<br/> (검증 시 제외) |
| jwt        | String  | 토큰                               |
| resultCode | String  | 결과 코드                            |
| resultMsg  | String  | 결과 메시지                           |
```json
{
   "claim": {
      "ci": "12345678",
      "role": "user",
      "username": "test_user",
      "password": "[VK+SJA26vJFCuDL/kPYAQ073GZTmTOpAbi5izZ10AFx3NlDDW6brVoBOeFlfA5hxHeOQw+Pz+M/XN.."
   },
   "jwt": "e29JzDyi.mwVQotk9DFqLwPpLw8TutiwiX6x4XQUrYtngFyoC7VPVs1txq54NGuzWbHt12rGbA6nnetYSnAinJRpVRzjyXj3GogWjwb2FMeYPshrZFhpaVvJFy2g39FcNusGHoHH5uBcmdEvme6g2crSuNKXbtsaREbakFtGu4oCk7CuVvz1XoAoc43Lc1hAbdU2VReEF7wxsKYQQLk.Ou+L/qyvpu8ssLpZ+qtDOYRQvHEcT/Qvq86KPapmXugS3SvZPnTnZdjzAB+Kcfd+bZX+OjXMBprUQHId25oD5OVK9XVq+3p839qpiJrbdYx6jWG7R5FhlQzQsH2CZezizUEkUlpc5Q38CNN3eJEZAOkO0TXhyMSyUkKyrMVDdVcLdJEzEXTVhwIICfG/+JCziI7/ijqBfSlGE4yB+14tfV2Ks2LdjfXf65zphz1Wm43oP2jzPFvreKta1twUKvhzKLAiYsxMD+kuL14zOJvYQJlnGozZG4rJT8qZUEVMglbCuoeqmXzmAUSGOcg6uaIN2/uPFT4oOgkmAkC5bvKw2g==",
   "resultCode": "200",
   "resultMsg": "Success"
}
```
### 3. API Detailed description
#### a. 토큰 발행 / Token Issue
- 사용자의 정보를 통해 토큰을 발행한다.
- **메소드명 : api/v1/createToken**

| Request Type | Value  | Description |
|--------------|--------|-------------|
| POST         | JSONObject | JSONObject      |

#### 1-2) Request Parameters
* 토큰에 담기는 정보는 필요에 따라 달라진다(아래의 필드도 사용하지 않아도 무관)

| Key      | Value     | Description              |
|----------|-----------|--------------------------|
| ci       | String    | 구분 코드 값                  |
| role     | String    | 사용자 권한                   |
| username | String    | 사용자 아이디                  |
| password | String    | 사용자 패스워드<br/>(평문/암호문 무관) |
| ...      | Primitive Types | 사용자 정보                   |


```json
{
  "ci": "12345678",
  "role" : "user",
  "username" : "test_user",
  "password" : "[VK+SJA26vJFCuDL/kPYAQ073GZTmTOpAbi5izZ10AFx3NlDDW6brVoBOeFlfA5hxHeOQw+Pz+M/XN.."
}
```

#### b. 토큰 검증 / Token Verify
- 사용자의 토큰을 검증한다.
- **메소드명 : api/v1/verifyToken**

| Request Type | Value  | Description |
|--------------|--------|-------------|
| POST         | JSONObject | JSONObject      |

#### 1-2) Request Parameters

| Key | Value     | Description |
|-----|-----------|-------------|
| jwt | String    | 토큰 값        |


```json
{
    "jwt": "e29JzDyi.mwVQotk9DFqLwPpLw8TutiwiX6x4XQUrYtngFyoC7VPVs1txq54NGuzWbHt12rGbA6nnetYSnAinJRpVRzjyXj3GogWjwb2FMeYPshrZFhpaVvJFy2g39FcNusGHoHH5uBcmdEvme6g2crSuNKXbtsaREbakFtGu4oCk7CuVvz1XoAoc43Lc1hAbdU2VReEF7wxsKYQQLk.Ou+L/qyvpu8ssLpZ+qtDOYRQvHEcT/Qvq86KPapmXugS3SvZPnTnZdjzAB+Kcfd+bZX+OjXMBprUQHId25oD5OVK9XVq+3p839qpiJrbdYx6jWG7R5FhlQzQsH2CZezizUEkUlpc5Q38CNN3eJEZAOkO0TXhyMSyUkKyrMVDdVcLdJEzEXTVhwIICfG/+JCziI7/ijqBfSlGE4yB+14tfV2Ks2LdjfXf65zphz1Wm43oP2jzPFvreKta1twUKvhzKLAiYsxMD+kuL14zOJvYQJlnGozZG4rJT8qZUEVMglbCuoeqmXzmAUSGOcg6uaIN2/uPFT4oOgkmAkC5bvKw2g=="
}
```

#### c. 토큰 정보 추출 / Token claim extraction
- 사용자의 토큰을 통해 내부 정보를 추출한다.
- 해당 토큰은 만료 여부와 관계없이 서명 검증만 통과하면 내부 정보(Claim)을 추출할 수 있다.
- **메소드명 : api/v1/extractClaim**

| Request Type | Value  | Description |
|--------------|--------|-------------|
| POST         | JSONObject | JSONObject      |

#### 1-2) Request Parameters

| Key | Value     | Description |
|-----|-----------|-------------|
| jwt | String    | 토큰 값        |


```json
{
    "jwt": "e29JzDyi.mwVQotk9DFqLwPpLw8TutiwiX6x4XQUrYtngFyoC7VPVs1txq54NGuzWbHt12rGbA6nnetYSnAinJRpVRzjyXj3GogWjwb2FMeYPshrZFhpaVvJFy2g39FcNusGHoHH5uBcmdEvme6g2crSuNKXbtsaREbakFtGu4oCk7CuVvz1XoAoc43Lc1hAbdU2VReEF7wxsKYQQLk.Ou+L/qyvpu8ssLpZ+qtDOYRQvHEcT/Qvq86KPapmXugS3SvZPnTnZdjzAB+Kcfd+bZX+OjXMBprUQHId25oD5OVK9XVq+3p839qpiJrbdYx6jWG7R5FhlQzQsH2CZezizUEkUlpc5Q38CNN3eJEZAOkO0TXhyMSyUkKyrMVDdVcLdJEzEXTVhwIICfG/+JCziI7/ijqBfSlGE4yB+14tfV2Ks2LdjfXf65zphz1Wm43oP2jzPFvreKta1twUKvhzKLAiYsxMD+kuL14zOJvYQJlnGozZG4rJT8qZUEVMglbCuoeqmXzmAUSGOcg6uaIN2/uPFT4oOgkmAkC5bvKw2g=="
}
```

