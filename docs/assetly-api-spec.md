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

권한:

- 조직 멤버

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

### 조직 수정

```http
PATCH /api/organizations/{organizationId}
```

권한:

- OWNER

Request:

```json
{
  "name": "대덕 연구소",
  "description": "연구실 장비와 비품 관리"
}
```

동작:

- 비멤버 요청은 조직 존재 여부를 노출하지 않고 `404 ORGANIZATION_NOT_FOUND`를 반환한다.
- 조직 멤버이지만 Owner가 아니면 `403 AUTH_FORBIDDEN`을 반환한다.

## 4. Member API

### 멤버 목록

```http
GET /api/organizations/{organizationId}/members
```

권한:

- OWNER
- ADMIN

Response:

```json
{
  "data": [
    {
      "id": 1,
      "userId": 1,
      "email": "owner@example.com",
      "name": "홍길동",
      "role": "OWNER",
      "joinedAt": "2026-10-02T09:00:00"
    }
  ]
}
```

### 멤버 추가

```http
POST /api/organizations/{organizationId}/members
```

Request:

```json
{
  "email": "member@example.com",
  "role": "MEMBER"
}
```

권한 및 동작:

- 가입된 사용자를 이메일로 조회해 조직에 추가한다.
- OWNER는 모든 역할을 지정할 수 있다.
- ADMIN은 MANAGER 또는 MEMBER 역할만 지정할 수 있다.
- 미가입 이메일은 `404 USER_NOT_FOUND`, 이미 소속된 사용자는 `409 ORGANIZATION_MEMBER_DUPLICATED`를 반환한다.

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

동작:

- OWNER는 모든 멤버의 역할을 변경할 수 있다.
- ADMIN은 MANAGER와 MEMBER만 서로 변경할 수 있다.
- 조직에는 OWNER가 최소 한 명 남아 있어야 한다.

### 멤버 제거

```http
DELETE /api/organizations/{organizationId}/members/{memberId}
```

권한:

- OWNER
- ADMIN

동작:

- ADMIN은 MANAGER와 MEMBER만 제거할 수 있다.
- 마지막 OWNER는 제거할 수 없다.
- URL의 조직에 속하지 않은 memberId는 `404 ORGANIZATION_MEMBER_NOT_FOUND`를 반환한다.

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

Response:

```json
{
  "data": {
    "items": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

### 자산 상세

```http
GET /api/organizations/{organizationId}/assets/{assetId}
```

권한:

- 조직 멤버

상세 응답에는 `publicCode`, 카테고리, 위치, 담당자, 구매 정보와 생성·수정 시간이 포함된다.

### 담당자 후보 목록

```http
GET /api/organizations/{organizationId}/assignees
```

권한:

- 조직 멤버

동작:

- 자산 담당자 선택에 필요한 `userId`, 이름, 이메일만 반환한다.
- 멤버 역할 관리 권한과는 분리한다.

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
  "assetCode": "IT-2026-00132",
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

변경 이력 단계가 구현되면 수정 내역 저장을 이 API에 연결한다.

- OWNER와 ADMIN은 전체 필드를 수정할 수 있다.
- MANAGER는 상태, 위치, 담당자만 수정할 수 있다.
- MEMBER의 수정 요청은 `403 AUTH_FORBIDDEN`을 반환한다.

### 자산 삭제

```http
DELETE /api/organizations/{organizationId}/assets/{assetId}
```

권한:

- OWNER
- ADMIN

동작:

- `deleted_at`을 기록해 비활성화하고 일반 목록과 상세 조회에서 제외한다.
- 삭제 이력 생성은 변경 이력 단계에서 연결한다.
- 조직 밖의 자산 ID는 `404 ASSET_NOT_FOUND`를 반환한다.

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

Response:

```json
{
  "data": [
    {
      "id": 1,
      "name": "노트북",
      "createdAt": "2026-10-02T01:00:00Z",
      "updatedAt": "2026-10-02T01:00:00Z"
    }
  ]
}
```

### 카테고리 수정

```http
PATCH /api/organizations/{organizationId}/categories/{categoryId}
```

권한:

- OWNER
- ADMIN

Request:

```json
{
  "name": "휴대용 컴퓨터"
}
```

### 카테고리 삭제

```http
DELETE /api/organizations/{organizationId}/categories/{categoryId}
```

권한:

- OWNER
- ADMIN

동작:

- 같은 조직의 이름 중복은 `409 CATEGORY_NAME_DUPLICATED`를 반환한다.
- 다른 조직의 categoryId는 `404 CATEGORY_NOT_FOUND`를 반환한다.
- 자산 연결 후에는 사용 중인 카테고리 삭제를 차단한다.

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

Response:

```json
{
  "data": [
    {
      "id": 1,
      "name": "개발팀 사무실",
      "description": "본관 3층",
      "createdAt": "2026-10-02T01:00:00Z",
      "updatedAt": "2026-10-02T01:00:00Z"
    }
  ]
}
```

### 위치 수정

```http
PATCH /api/organizations/{organizationId}/locations/{locationId}
```

권한:

- OWNER
- ADMIN

Request:

```json
{
  "name": "개발팀 좌석",
  "description": "별관 2층"
}
```

### 위치 삭제

```http
DELETE /api/organizations/{organizationId}/locations/{locationId}
```

권한:

- OWNER
- ADMIN

동작:

- 같은 조직의 이름 중복은 `409 LOCATION_NAME_DUPLICATED`를 반환한다.
- 다른 조직의 locationId는 `404 LOCATION_NOT_FOUND`를 반환한다.
- 자산 연결 후에는 사용 중인 위치 삭제를 차단한다.

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
    "recentAssets": []
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
USER_NOT_FOUND
ORGANIZATION_NOT_FOUND
ORGANIZATION_MEMBER_NOT_FOUND
ORGANIZATION_MEMBER_DUPLICATED
ORGANIZATION_LAST_OWNER_REQUIRED
ASSET_NOT_FOUND
ASSET_CODE_DUPLICATED
CATEGORY_NOT_FOUND
CATEGORY_NAME_DUPLICATED
CATEGORY_IN_USE
LOCATION_NOT_FOUND
LOCATION_NAME_DUPLICATED
LOCATION_IN_USE
VALIDATION_ERROR
```
