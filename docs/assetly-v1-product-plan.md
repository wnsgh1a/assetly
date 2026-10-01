# Assetly v1.0 제품 기획서

## 1. 프로젝트 개요

Assetly는 소규모 조직을 위한 QR 기반 자산 관리 SaaS다.

회사, 연구실, 동아리, 교육기관처럼 여러 장비와 비품을 관리해야 하는 조직이 자산의 위치, 담당자, 상태, 변경 이력, 점검 기록을 한곳에서 관리할 수 있도록 돕는다.

Assetly의 핵심은 단순한 자산 목록 관리가 아니라, 현장에서 QR을 스캔해 즉시 자산을 확인하고 상태를 갱신하는 업무 흐름을 만드는 것이다.

## 2. 한 줄 설명

소규모 조직이 자산에 QR을 붙이고, 현장에서 스캔해 위치와 상태를 바로 관리하는 웹 애플리케이션.

## 3. 만들려는 이유

자산 정보가 엑셀, 사진 폴더, 메신저, 구두 전달 등으로 흩어져 있으면 실제 위치와 상태를 확인하기 어렵다.

특히 현장에서 자산을 확인한 뒤 다시 사무실로 돌아와 DB나 엑셀을 수정해야 하는 방식은 반복 방문, 기록 누락, 사진 누락, 담당자 혼선 같은 비효율을 만든다.

Assetly는 이 문제를 다음 흐름으로 해결한다.

1. 관리자가 자산을 등록한다.
2. 자산별 QR 코드를 발급한다.
3. QR 라벨을 장비나 비품에 부착한다.
4. 현장 담당자가 휴대폰으로 QR을 스캔한다.
5. 자산 상세 정보를 확인한다.
6. 위치, 담당자, 상태, 사진, 점검 기록을 현장에서 바로 갱신한다.
7. 모든 변경 내역은 이력으로 남는다.

## 4. 타깃 사용자

### 1차 타깃

소규모 조직의 자산 관리자.

예시는 다음과 같다.

- 연구실 장비 관리자
- 동아리 비품 관리자
- 스타트업 사무기기 관리자
- 교육기관 기자재 담당자
- 사내 IT 장비 담당자

### 2차 타깃

자산을 직접 사용하는 구성원.

예시는 다음과 같다.

- 장비를 대여하는 팀원
- 현장에서 자산 상태를 확인하는 직원
- 점검 결과를 기록하는 담당자

## 5. 핵심 문제

Assetly가 해결하려는 문제는 다음 네 가지다.

1. 자산의 현재 위치를 알기 어렵다.
2. 누가 사용 중인지 확인하기 어렵다.
3. 상태 변경과 점검 이력이 남지 않는다.
4. 현장에서 확인한 내용을 다시 수기로 옮겨 적어야 한다.

## 6. 핵심 가치

Assetly의 핵심 가치는 다음과 같다.

- 자산 위치와 상태를 빠르게 확인한다.
- QR 스캔으로 현장 업무를 줄인다.
- 모든 변경 이력을 자동으로 남긴다.
- 조직별로 데이터를 분리해 SaaS처럼 사용할 수 있다.
- 권한에 따라 조회, 수정, 관리 기능을 제한한다.

## 7. 제품 컨셉

Assetly는 웹사이트라기보다 업무용 웹 애플리케이션에 가깝다.

사용자는 브라우저에서 로그인하지만, 사용 경험은 일반 업무 프로그램처럼 설계한다.

주요 화면은 다음과 같다.

- 대시보드
- 자산 목록
- 자산 상세
- 위치 관리
- 대여/반납
- 점검
- 멤버 관리
- 변경 이력
- 설정

## 8. 핵심 사용 시나리오

### 시나리오 1. 조직 생성

1. 사용자가 회원가입한다.
2. 로그인 후 조직을 생성한다.
3. 조직 이름, 설명, 기본 위치 정보를 입력한다.
4. 사용자는 해당 조직의 Owner가 된다.

### 시나리오 2. 자산 등록

1. 관리자가 자산 등록 화면으로 이동한다.
2. 자산명, 자산번호, 카테고리, 위치, 담당자, 상태를 입력한다.
3. 사진을 등록한다.
4. 저장하면 자산 상세 페이지가 생성된다.
5. 자산별 QR 코드가 발급된다.

### 시나리오 3. QR 스캔

1. 현장 담당자가 자산에 붙은 QR을 스캔한다.
2. 자산 상세 페이지가 열린다.
3. 로그인된 사용자라면 권한에 따라 수정 버튼이 보인다.
4. 위치 변경, 상태 변경, 담당자 변경, 점검 기록을 남길 수 있다.

### 시나리오 4. 변경 이력 확인

1. 관리자가 자산 상세 화면을 연다.
2. 변경 이력 탭에서 자산의 이동, 대여, 반납, 점검, 상태 변경 기록을 확인한다.
3. 누가, 언제, 무엇을 변경했는지 확인할 수 있다.

## 9. 권한 구조

조직 안에서 사용자는 역할을 가진다.

| 역할 | 설명 |
| --- | --- |
| Owner | 조직 생성자. 조직 설정, 멤버 관리, 모든 기능 사용 가능 |
| Admin | 자산 등록, 수정, 삭제, 멤버 관리 가능 |
| Manager | 자산 상태 변경, 위치 변경, 대여/반납, 점검 가능 |
| Member | 자산 조회, 대여 신청 가능 |

서버는 모든 요청에서 다음을 확인해야 한다.

1. 로그인한 사용자인가?
2. 해당 조직의 멤버인가?
3. 요청한 작업을 수행할 권한이 있는가?

## 10. MVP 범위

v1.0에서는 실제 서비스로 동작하기 위해 꼭 필요한 기능만 만든다.

### 포함할 기능

- 회원가입
- 로그인
- JWT 기반 인증
- 조직 생성
- 조직 멤버 목록 조회
- 자산 등록
- 자산 목록 조회
- 자산 상세 조회
- 자산 수정
- 자산 삭제
- 위치 등록
- 위치 목록 조회
- 카테고리 등록
- 카테고리 목록 조회
- QR 코드 발급
- QR URL로 자산 상세 접근
- 자산 변경 이력 기록
- 기본 권한 처리

### v1.0에서 제외할 기능

- 결제
- 실시간 알림
- Redis
- Kafka
- 복잡한 통계
- 모바일 앱
- SSO
- OAuth 로그인
- Excel 가져오기/내보내기
- 고급 검색 엔진
- 대량 QR 출력

위 기능들은 실제 사용하면서 필요성이 확인되면 v1.1 이후에 추가한다.

## 11. v1.1 후보 기능

- 이미지 업로드
- 점검 기록
- 대여/반납
- Excel 내보내기
- 이메일 초대
- 이메일 알림
- 고급 검색
- 자산 상태별 필터
- QR 라벨 출력용 PDF 생성

## 12. v1.2 후보 기능

- S3 연동
- 대량 자산 등록
- 감사 로그 강화
- 조직별 사용량 제한
- 관리자 통계
- 사용자 초대 링크
- 모바일 화면 최적화 고도화
- 결제 구조 검토

## 13. 화면 구성 초안

### 인증

- 로그인
- 회원가입

### 온보딩

- 조직 생성
- 조직 선택

### 메인 앱

- 대시보드
- 자산 목록
- 자산 상세
- 자산 등록/수정
- 위치 목록
- 카테고리 목록
- 멤버 목록
- 변경 이력
- 설정

## 14. 대시보드에 보여줄 정보

v1.0 대시보드는 단순하지만 업무적으로 의미 있어야 한다.

- 전체 자산 수
- 사용 중 자산 수
- 보관 중 자산 수
- 수리 중 자산 수
- 최근 등록된 자산
- 최근 변경 이력

## 15. 주요 데이터 모델 초안

### User

- id
- email
- password
- name
- createdAt
- updatedAt

### Organization

- id
- name
- description
- createdAt
- updatedAt

### OrganizationMember

- id
- organizationId
- userId
- role
- createdAt

### Asset

- id
- organizationId
- categoryId
- locationId
- assetCode
- name
- description
- status
- assignedUserId
- purchaseDate
- purchasePrice
- createdAt
- updatedAt

### AssetCategory

- id
- organizationId
- name
- createdAt

### Location

- id
- organizationId
- name
- description
- createdAt

### AssetHistory

- id
- organizationId
- assetId
- actorId
- actionType
- beforeValue
- afterValue
- memo
- createdAt

## 16. API 초안

### Auth

- `POST /api/auth/signup`
- `POST /api/auth/login`
- `GET /api/auth/me`

### Organizations

- `POST /api/organizations`
- `GET /api/organizations`
- `GET /api/organizations/{organizationId}`
- `PATCH /api/organizations/{organizationId}`

### Members

- `GET /api/organizations/{organizationId}/members`
- `PATCH /api/organizations/{organizationId}/members/{memberId}/role`
- `DELETE /api/organizations/{organizationId}/members/{memberId}`

### Assets

- `POST /api/organizations/{organizationId}/assets`
- `GET /api/organizations/{organizationId}/assets`
- `GET /api/organizations/{organizationId}/assets/{assetId}`
- `PATCH /api/organizations/{organizationId}/assets/{assetId}`
- `DELETE /api/organizations/{organizationId}/assets/{assetId}`
- `GET /api/organizations/{organizationId}/assets/{assetId}/histories`

### QR

- `GET /api/assets/qr/{assetCode}`

### Categories

- `POST /api/organizations/{organizationId}/categories`
- `GET /api/organizations/{organizationId}/categories`

### Locations

- `POST /api/organizations/{organizationId}/locations`
- `GET /api/organizations/{organizationId}/locations`

## 17. 기술 스택 초안

### Frontend

- React 또는 Next.js
- TypeScript
- Tailwind CSS
- TanStack Query

### Backend

- Spring Boot
- Spring Security
- JWT
- JPA
- PostgreSQL

### Infra

- Docker
- Docker Compose
- GitHub Actions
- AWS EC2 또는 Lightsail
- Nginx
- HTTPS

### Later

- S3 호환 스토리지
- Redis
- 모니터링
- 로그 수집

## 18. 개발 순서

1. 제품 기획서 확정
2. 화면 흐름 설계
3. DB ERD 설계
4. API 명세 작성
5. 백엔드 프로젝트 생성
6. 프론트엔드 프로젝트 생성
7. Docker Compose 개발 환경 구성
8. 인증 구현
9. 조직/멤버 구현
10. 자산/위치/카테고리 구현
11. 변경 이력 구현
12. QR 발급 구현
13. 기본 UI 구현
14. 테스트 코드 작성
15. GitHub Actions 구성
16. 운영 서버 배포

## 19. 포트폴리오에서 강조할 점

Assetly는 다음 역량을 보여줄 수 있다.

- 실제 문제에서 출발한 서비스 기획
- 멀티테넌트 SaaS 구조 설계
- 조직과 역할 기반 권한 처리
- JWT 인증
- REST API 설계
- PostgreSQL 기반 관계형 데이터 모델링
- 변경 이력 기반 감사 로그 설계
- QR 기반 현장 업무 흐름 설계
- Docker 기반 개발/운영 환경 구성
- CI/CD와 실제 배포 경험

## 20. 다음 작업

다음으로 확정해야 할 것은 화면 흐름과 DB 구조다.

권장 순서는 다음과 같다.

1. 사용자 흐름 다이어그램 작성
2. 화면 목록 확정
3. DB ERD 작성
4. API 명세 보강
5. 백엔드 프로젝트 생성
