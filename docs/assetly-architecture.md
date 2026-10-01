# Assetly 아키텍처 설계

이 문서는 Assetly v1.0의 전체 시스템 구조와 기술 선택 이유를 정리한다.

## 1. 목표 구조

Assetly는 다음 구성으로 시작한다.

```text
Browser
  |
  | HTTPS
  v
Nginx
  |
  +-- Frontend
  |
  +-- Backend API
        |
        v
     PostgreSQL
```

v1.0에서는 단순하고 운영 가능한 구조를 우선한다.

처음부터 복잡한 마이크로서비스, Kafka, Kubernetes 같은 구조는 사용하지 않는다.

## 2. 기술 스택

### Frontend

후보:

- React
- Next.js
- TypeScript
- Tailwind CSS
- TanStack Query

권장:

- Next.js
- TypeScript
- Tailwind CSS
- TanStack Query

이유:

- 화면 라우팅과 앱 구조를 잡기 쉽다.
- 로그인 전/후 페이지를 나누기 좋다.
- API 연동 상태 관리에 TanStack Query가 잘 맞는다.
- Tailwind CSS는 빠르게 업무용 UI를 만들기 좋다.

### Backend

후보:

- Spring Boot
- Spring Security
- JPA
- PostgreSQL Driver
- Validation

권장:

- Spring Boot
- Spring Security
- JPA
- PostgreSQL

이유:

- 인증/권한 처리 경험을 보여주기 좋다.
- 조직 기반 권한 검사를 구현하기 좋다.
- 관계형 데이터 모델과 잘 맞는다.
- 포트폴리오 설명력이 높다.

### Database

권장:

- PostgreSQL

이유:

- 관계형 데이터가 중심이다.
- 조직, 멤버, 자산, 위치, 이력 간 관계가 명확하다.
- 운영 환경에서도 널리 사용된다.

### Infra

v1.0 권장:

- Docker
- Docker Compose
- Nginx
- AWS EC2 또는 Lightsail
- GitHub Actions
- HTTPS

## 3. 개발 환경 구조

로컬 개발 환경은 Docker Compose로 구성한다.

```text
assetly/
  backend/
  frontend/
  docs/
  docker-compose.yml
```

로컬에서 실행되는 서비스:

- frontend
- backend
- postgres

초기에는 프론트엔드와 백엔드를 각각 개발 서버로 실행하고, PostgreSQL만 Docker로 띄우는 방식도 가능하다.

## 4. 백엔드 레이어 구조

Spring Boot 백엔드는 다음 레이어로 나눈다.

```text
controller
service
repository
domain
dto
security
exception
```

### controller

HTTP 요청과 응답을 담당한다.

### service

비즈니스 로직을 담당한다.

조직 권한 확인, 자산 변경 이력 생성 같은 핵심 로직은 service에서 처리한다.

### repository

DB 접근을 담당한다.

### domain

JPA Entity와 enum을 둔다.

### dto

Request, Response 객체를 둔다.

### security

JWT 인증, Security 설정, 현재 사용자 조회 기능을 둔다.

### exception

공통 예외와 에러 응답을 관리한다.

## 5. 권한 처리 구조

Assetly에서 권한 처리는 매우 중요하다.

모든 조직 하위 API는 다음 검사를 한다.

1. 사용자가 로그인했는가?
2. 사용자가 해당 조직의 멤버인가?
3. 사용자의 역할이 필요한 권한을 만족하는가?
4. 접근하려는 리소스가 해당 조직에 속하는가?

권한 체크는 반복되므로 별도 컴포넌트로 분리한다.

예시 이름:

```text
OrganizationAccessChecker
```

역할:

- 조직 멤버 여부 확인
- 역할 확인
- 현재 사용자의 조직 내 역할 반환

## 6. 인증 구조

v1.0에서는 JWT access token 기반 인증으로 시작한다.

로그인 성공 시 access token을 발급한다.

프론트엔드는 token을 저장하고 API 요청마다 Authorization 헤더에 포함한다.

초기 구현에서는 단순화를 위해 refresh token 없이 시작할 수 있다. 이후 운영 수준으로 고도화할 때 refresh token과 token rotation을 도입한다.

## 7. 멀티테넌트 구조

Assetly는 DB를 조직마다 나누지 않는다.

하나의 DB 안에서 `organization_id`로 데이터를 분리하는 shared database 방식을 사용한다.

장점:

- 구현이 단순하다.
- 소규모 SaaS MVP에 적합하다.
- 조직 추가 비용이 낮다.

주의점:

- 모든 쿼리에서 organization 조건을 빠뜨리면 안 된다.
- 리소스 접근 시 조직 소속 검사를 반드시 해야 한다.
- 테스트 코드로 다른 조직 데이터 접근이 막히는지 확인해야 한다.

## 8. QR 접근 구조

QR은 자산의 공개 접근 코드를 포함한다.

```text
/a/{publicCode}
```

프론트엔드는 해당 경로에 접근하면 백엔드에 publicCode로 자산 정보를 요청한다.

백엔드는 다음을 확인한다.

1. publicCode에 해당하는 자산이 있는가?
2. 사용자가 로그인했는가?
3. 사용자가 해당 자산의 조직 멤버인가?
4. 권한에 따라 어떤 액션을 허용할 것인가?

QR URL 자체가 자산 정보를 노출하지 않도록 한다.

## 9. 변경 이력 구조

자산 생성, 수정, 삭제, 위치 변경, 상태 변경 등은 `asset_histories`에 기록한다.

서비스 레이어에서 자산 변경 전후 값을 비교하고, 의미 있는 변경만 이력으로 남긴다.

초기에는 필드별로 이력을 남기는 단순 구조를 사용한다.

예시:

```text
asset_id: 1
actor_id: 3
action_type: LOCATION_CHANGED
field_name: location
before_value: "기획팀"
after_value: "개발팀"
created_at: 2026-10-01T10:00:00
```

## 10. 파일 업로드

v1.0에서는 이미지 업로드를 제외할 수 있다.

이미지 업로드를 넣는 경우에도 처음부터 S3를 붙이지 않고, 로컬 저장 또는 S3 호환 구조를 추상화한 뒤 운영 배포에서 S3를 연결한다.

v1.1에서 권장:

- asset_attachments 테이블 추가
- S3 호환 Object Storage 사용
- 파일 크기 제한
- 이미지 MIME 타입 검증

## 11. 배포 구조

초기 운영 배포는 다음 방식으로 시작한다.

```text
AWS EC2 또는 Lightsail
  |
  +-- Nginx
  +-- Frontend build
  +-- Spring Boot app
  +-- PostgreSQL
```

처음에는 같은 서버에 모두 올릴 수 있다.

서비스가 커지면 DB를 RDS로 분리하고, 파일은 S3로 분리한다.

## 12. CI/CD

GitHub Actions에서 다음을 수행한다.

### Pull Request

- 백엔드 테스트
- 프론트엔드 빌드
- 린트

### main 브랜치 머지

- 테스트
- Docker 이미지 빌드
- 서버 배포

초기에는 자동 배포까지 한 번에 가기 어렵다면, 테스트 자동화부터 먼저 구성한다.

## 13. 테스트 전략

### 백엔드

우선순위:

1. 인증
2. 조직 권한 체크
3. 자산 CRUD
4. 변경 이력 생성
5. 다른 조직 데이터 접근 차단

테스트 종류:

- 단위 테스트
- 서비스 테스트
- API 통합 테스트

### 프론트엔드

우선순위:

1. 로그인 흐름
2. 조직 생성 흐름
3. 자산 목록/상세 흐름
4. QR 접근 흐름

초기에는 수동 테스트 체크리스트로 시작하고, 이후 주요 흐름에 E2E 테스트를 추가한다.

## 14. 보안 기준

v1.0에서 반드시 지켜야 할 기준:

- 비밀번호는 평문 저장 금지
- API 요청마다 JWT 검증
- 조직별 권한 검증
- 다른 조직 리소스 접근 차단
- 에러 메시지에 민감 정보 노출 금지
- CORS 허용 범위 제한
- 운영 환경 secret은 코드에 저장 금지

## 15. 나중에 도입할 수 있는 기술

### Redis

도입 이유가 생기는 경우:

- refresh token 관리
- rate limiting
- 캐시
- 알림 큐

### S3

도입 이유가 생기는 경우:

- 자산 사진 업로드
- QR 라벨 PDF 저장
- 첨부파일 관리

### WebSocket

도입 이유가 생기는 경우:

- 실시간 알림
- 여러 사용자가 같은 자산을 동시에 관리할 때 실시간 갱신

### 검색 엔진

도입 이유가 생기는 경우:

- 자산이 많아져 DB LIKE 검색으로 부족할 때
- 태그, 설명, 이력까지 고급 검색이 필요할 때

## 16. v1.0 아키텍처 결론

Assetly v1.0은 단순하지만 확장 가능한 구조로 시작한다.

핵심은 다음이다.

- Spring Boot 백엔드
- Next.js 프론트엔드
- PostgreSQL
- JWT 인증
- 조직 기반 권한 처리
- QR publicCode 접근
- 변경 이력 기록
- Docker 기반 실행
- GitHub Actions 기반 검증

이 구조만 제대로 구현해도 포트폴리오 프로젝트로 충분히 설명력 있는 서비스가 된다.
