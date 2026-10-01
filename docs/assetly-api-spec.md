# Assetly API 명세 초안

이 문서는 Assetly v1.0 REST API의 초안을 정리한다.

세부 request/response 필드는 구현하면서 조정할 수 있지만, API의 책임과 URL 구조는 이 문서를 기준으로 잡는다.

## 1. 공통 규칙

### Base URL

```text
/api
```

### 인증

로그인 후 발급받은 JWT access token을 사용한다.

```http
Authorization: Bearer {accessToken}
```

### 응답 형식

성공 응답은 기본적으로 JSON을 사용한다.

```json
{
  "data": {}
}
```

목록 응답은 다음 형태를 기본으로 한다.

```json
{
  "data": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5
  }
}
```

에러 응답은 다음 형태를 기본으로 한다.

```json
{
  "error": {
    "code": "ASSET_NOT_FOUND",
    "message": "자산을 찾을 수 없습니다."
  }
}
```

## 2. Auth API

### 회원가입

```http
POST /api/auth/signup
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "name": "홍길동"
}
```

Response:

```json
{
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```

### 로그인

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

Response:

```json
{
  "data": {
    "accessToken": "jwt-token",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "홍길동"
    }
  }
}
```

### 내 정보 조회

```http
GET /api/auth/me
```

Response:

```json
{
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "홍길동"
  }
}
```

## 3. Organization API

### 조직 생성

```http
POST /api/organizations
```

Request:

```json
{
  "name": "대덕 연구소",
  "description": "연구실 장비 관리"
}
```

Response:

```json
{
  "data": {
    "id": 1,
    "name": "대덕 연구소",
    "description": "연구실 장비 관리",
    "myRole": "OWNER"
  }
}
```

### 내 조직 목록

```http
GET /api/organizations
```

Response:

```json
{
  "data": [
    {
      "id": 1,
      "name": "대덕 연구소",
      "myRole": "OWNER"
    }
  ]
}
```

### 조직 상세

```http
GET /api/organizations/{organizationId}
```

### 조직 수정

```http
PATCH /api/organizations/{organizationId}
```

권한:

- OWNER

## 4. Member API

### 멤버 목록

```http
GET /api/organizations/{organizationId}/members
```

권한:

- OWNER
- ADMIN

### 멤버 역할 변경

```http
PATCH /api/organizations/{organizationId}/members/{memberId}/role
```

Request:

```json
{
  "role": "MANAGER"
}
```

권한:

- OWNER
- ADMIN

### 멤버 제거

```http
DELETE /api/organizations/{organizationId}/members/{memberId}
```

권한:

- OWNER
- ADMIN

## 5. Asset API

### 자산 등록

```http
POST /api/organizations/{organizationId}/assets
```

권한:

- OWNER
- ADMIN

Request:

```json
{
  "assetCode": "IT-2026-00132",
  "name": "MacBook Pro 14",
  "description": "개발팀 노트북",
  "categoryId": 1,
  "locationId": 1,
  "assignedUserId": 2,
  "status": "IN_USE",
  "purchaseDate": "2025-03-14",
  "purchasePrice": 2390000
}
```

Response:

```json
{
  "data": {
    "id": 1,
    "publicCode": "q8K2md91",
    "assetCode": "IT-2026-00132",
    "name": "MacBook Pro 14",
    "status": "IN_USE"
  }
}
```

### 자산 목록

```http
GET /api/organizations/{organizationId}/assets
```

Query:

```text
keyword
status
categoryId
locationId
page
size
```

권한:

- 조직 멤버

### 자산 상세

```http
GET /api/organizations/{organizationId}/assets/{assetId}
```

권한:

- 조직 멤버

### QR 공개 코드로 자산 조회

```http
GET /api/assets/public/{publicCode}
```

동작:

- 로그인 필요
- 사용자가 해당 자산의 조직 멤버인지 확인
- 권한이 있으면 자산 상세 반환

### 자산 수정

```http
PATCH /api/organizations/{organizationId}/assets/{assetId}
```

권한:

- OWNER
- ADMIN
- MANAGER 일부 필드

Request:

```json
{
  "name": "MacBook Pro 14",
  "description": "개발팀 노트북",
  "categoryId": 1,
  "locationId": 2,
  "assignedUserId": 3,
  "status": "REPAIR",
  "purchaseDate": "2025-03-14",
  "purchasePrice": 2390000
}
```

수정 시 변경 이력을 생성한다.

### 자산 삭제

```http
DELETE /api/organizations/{organizationId}/assets/{assetId}
```

권한:

- OWNER
- ADMIN

동작:

- soft delete 권장
- 삭제 이력 생성

## 6. Asset History API

### 자산별 이력 조회

```http
GET /api/organizations/{organizationId}/assets/{assetId}/histories
```

권한:

- 조직 멤버

### 조직 전체 이력 조회

```http
GET /api/organizations/{organizationId}/histories
```

Query:

```text
assetId
actorId
actionType
from
to
page
size
```

권한:

- 조직 멤버

## 7. Category API

### 카테고리 생성

```http
POST /api/organizations/{organizationId}/categories
```

권한:

- OWNER
- ADMIN

Request:

```json
{
  "name": "노트북"
}
```

### 카테고리 목록

```http
GET /api/organizations/{organizationId}/categories
```

권한:

- 조직 멤버

### 카테고리 수정

```http
PATCH /api/organizations/{organizationId}/categories/{categoryId}
```

권한:

- OWNER
- ADMIN

### 카테고리 삭제

```http
DELETE /api/organizations/{organizationId}/categories/{categoryId}
```

권한:

- OWNER
- ADMIN

## 8. Location API

### 위치 생성

```http
POST /api/organizations/{organizationId}/locations
```

권한:

- OWNER
- ADMIN

Request:

```json
{
  "name": "개발팀 사무실",
  "description": "본관 3층"
}
```

### 위치 목록

```http
GET /api/organizations/{organizationId}/locations
```

권한:

- 조직 멤버

### 위치 수정

```http
PATCH /api/organizations/{organizationId}/locations/{locationId}
```

권한:

- OWNER
- ADMIN

### 위치 삭제

```http
DELETE /api/organizations/{organizationId}/locations/{locationId}
```

권한:

- OWNER
- ADMIN

## 9. Dashboard API

### 대시보드 요약

```http
GET /api/organizations/{organizationId}/dashboard
```

권한:

- 조직 멤버

Response:

```json
{
  "data": {
    "totalAssets": 128,
    "availableAssets": 52,
    "inUseAssets": 64,
    "repairAssets": 9,
    "lostAssets": 3,
    "recentAssets": [],
    "recentHistories": []
  }
}
```

## 10. 권한 체크 규칙

모든 조직 하위 API는 다음 검사를 수행한다.

1. JWT가 유효한가?
2. 사용자가 해당 조직의 멤버인가?
3. 사용자의 역할이 요청한 작업을 수행할 수 있는가?
4. 요청한 리소스가 해당 조직에 속하는가?

특히 4번이 중요하다.

예를 들어 `/api/organizations/1/assets/10` 요청이 들어왔을 때, assetId 10이 organizationId 1에 속하는지 반드시 확인해야 한다.

## 11. 주요 에러 코드

```text
AUTH_INVALID_CREDENTIALS
AUTH_UNAUTHORIZED
AUTH_FORBIDDEN
USER_EMAIL_DUPLICATED
ORGANIZATION_NOT_FOUND
ORGANIZATION_MEMBER_NOT_FOUND
ASSET_NOT_FOUND
ASSET_CODE_DUPLICATED
CATEGORY_NOT_FOUND
CATEGORY_NAME_DUPLICATED
LOCATION_NOT_FOUND
LOCATION_NAME_DUPLICATED
VALIDATION_ERROR
```
