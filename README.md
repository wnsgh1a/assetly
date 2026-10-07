# Assetly

[![지속적 통합](https://github.com/wnsgh1a/assetly/actions/workflows/ci.yml/badge.svg)](https://github.com/wnsgh1a/assetly/actions/workflows/ci.yml)

Assetly는 소규모 조직을 위한 QR 기반 자산 관리 SaaS입니다.

회사, 연구실, 동아리, 교육기관처럼 여러 장비와 비품을 관리해야 하는 조직이 자산의 위치, 담당자, 상태, 변경 이력, 점검 기록을 한곳에서 관리할 수 있도록 돕는 웹 애플리케이션입니다.

## 프로젝트 목표

이 프로젝트의 목표는 단순한 CRUD 연습 프로젝트를 만드는 것이 아닙니다.

최종 목표는 다음과 같습니다.

> 실제 사용 가능한 소규모 자산관리 SaaS를 기획, 설계, 개발, 배포, 운영해보는 것

Assetly는 포트폴리오용 게시판 프로젝트가 아니라, 사용자가 회원가입하고 조직을 생성한 뒤 자산을 등록하고 QR을 통해 현장에서 관리할 수 있는 작은 업무 서비스를 목표로 합니다.

## 핵심 아이디어

많은 조직에서 자산 정보는 엑셀, 사진 폴더, 메신저, 구두 전달 등으로 흩어져 있습니다.

이런 방식은 다음 문제를 만듭니다.

- 자산의 현재 위치를 알기 어렵다.
- 누가 사용 중인지 확인하기 어렵다.
- 마지막 점검일이나 상태 변경 이력이 남지 않는다.
- 현장에서 확인한 내용을 다시 사무실에서 수기로 입력해야 한다.
- 같은 장소를 여러 번 방문하는 비효율이 생긴다.

Assetly는 이 문제를 QR 기반 흐름으로 해결합니다.

1. 관리자가 자산을 등록한다.
2. 자산별 QR 코드를 발급한다.
3. QR 라벨을 장비나 비품에 부착한다.
4. 현장 담당자가 휴대폰으로 QR을 스캔한다.
5. 자산 상세 정보를 확인한다.
6. 위치, 상태, 담당자, 점검 내용을 현장에서 바로 갱신한다.
7. 모든 변경 이력이 자동으로 기록된다.

## 한 줄 소개

소규모 조직이 자산에 QR을 붙이고, 현장에서 스캔해 위치와 상태를 바로 관리하는 웹 애플리케이션.

## 주요 사용자

Assetly의 1차 사용자는 소규모 조직의 자산 관리자입니다.

예시는 다음과 같습니다.

- 연구실 장비 관리자
- 동아리 비품 관리자
- 스타트업 사무기기 관리자
- 교육기관 기자재 담당자
- 사내 IT 장비 담당자

2차 사용자는 실제 자산을 사용하는 구성원입니다.

- 장비를 대여하는 팀원
- 현장에서 자산 상태를 확인하는 직원
- 점검 결과를 기록하는 담당자

## v1.0 핵심 흐름

Assetly v1.0은 기능 개수보다 실제 사용 흐름 완성을 우선합니다.

### 관리자 흐름

1. 회원가입한다.
2. 로그인한다.
3. 조직을 생성한다.
4. 위치를 등록한다.
5. 카테고리를 등록한다.
6. 자산을 등록한다.
7. QR 코드를 확인한다.
8. 자산 상세를 조회한다.
9. 자산 정보를 수정한다.
10. 변경 이력을 확인한다.

### 현장 사용자 흐름

1. 자산에 붙은 QR을 스캔한다.
2. 자산 상세 페이지에 접근한다.
3. 로그인이 필요하면 로그인한다.
4. 권한이 있으면 위치, 상태, 담당자를 수정한다.
5. 수정 내용이 변경 이력에 남는다.

이 두 흐름이 실제 배포 환경에서 동작하면 v1.0의 핵심 목표는 달성됩니다.

## v1.0 포함 기능

- 회원가입
- 로그인
- JWT 기반 인증
- 조직 생성
- 조직 멤버 구조
- 역할 기반 권한 처리
- 자산 등록
- 자산 목록 조회
- 자산 상세 조회
- 자산 수정
- 자산 삭제 또는 비활성화
- 위치 관리
- 카테고리 관리
- QR 접근 코드 발급
- QR URL 기반 자산 접근
- 자산 변경 이력 기록
- 대시보드 요약

## v1.0에서 제외할 기능

다음 기능은 좋은 기능이지만 v1.0 목표는 아닙니다.

- 결제
- 실시간 채팅
- 복잡한 통계
- 모바일 네이티브 앱
- Kafka
- Redis 기반 고도화
- 소셜 로그인
- 조직별 요금제
- 대량 Excel 가져오기
- 대량 QR 라벨 출력
- 관리자용 슈퍼 어드민
- 마케팅 랜딩 페이지

v1.0에서는 핵심 업무 흐름을 먼저 완성하고, 실제 사용하면서 필요성이 확인된 기능만 v1.1 이후에 추가합니다.

## 기술 스택

### Frontend

- Next.js
- React
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
- Nginx
- GitHub Actions
- AWS EC2 또는 Lightsail
- HTTPS

### Later

필요성이 생기면 다음 기술을 도입합니다.

- S3 호환 Object Storage
- Redis
- 이메일 알림
- 모니터링
- 로그 수집

## 예상 프로젝트 구조

```text
assetly/
  backend/
    src/
    build.gradle
  frontend/
    app/
    package.json
  docs/
    assetly-project-guide.md
    assetly-v1-product-plan.md
    assetly-screen-flow.md
    assetly-db-design.md
    assetly-api-spec.md
    assetly-architecture.md
    assetly-development-roadmap.md
    assetly-decision-log.md
  docker-compose.yml
  README.md
```

## 문서 구조

Assetly는 개발 전에 서비스 방향과 설계를 문서화한 뒤 구현합니다.

현재 문서는 다음 역할을 가집니다.

| 문서 | 역할 |
| --- | --- |
| `docs/assetly-project-guide.md` | 프로젝트 전체 방향과 개발 원칙 |
| `docs/assetly-v1-product-plan.md` | v1.0 제품 기획서 |
| `docs/assetly-screen-flow.md` | 화면 흐름과 사용자 시나리오 |
| `docs/assetly-db-design.md` | DB 모델과 ERD 설명 |
| `docs/assetly-api-spec.md` | REST API 명세 초안 |
| `docs/assetly-architecture.md` | 전체 아키텍처와 배포 구조 |
| `docs/assetly-development-roadmap.md` | 단계별 개발 로드맵 |
| `docs/assetly-decision-log.md` | 중요한 결정과 이유 기록 |
| `docs/assetly-functional-test-spec.md` | 기능 테스트 케이스, 실행 기준, 결과 기록 |

## 핵심 도메인

### User

서비스 사용자 계정입니다.

### Organization

Assetly를 사용하는 조직입니다.

예시는 회사, 연구실, 동아리, 교육기관입니다.

### OrganizationMember

사용자와 조직의 관계입니다.

한 사용자는 여러 조직에 속할 수 있고, 조직마다 다른 역할을 가질 수 있습니다.

### Asset

관리 대상 자산입니다.

예시는 노트북, 모니터, 카메라, 실험 장비, 공구, 비품입니다.

### Location

자산이 위치한 장소입니다.

예시는 개발팀 사무실, 본관 3층, 실험실 A, 창고입니다.

### AssetCategory

자산 분류입니다.

예시는 노트북, 모니터, 카메라, 비품입니다.

### AssetHistory

자산 변경 이력입니다.

누가, 언제, 무엇을 변경했는지 기록합니다.

## 권한 구조

조직 안에서 사용자는 역할을 가집니다.

| 역할 | 설명 |
| --- | --- |
| Owner | 조직 생성자. 조직 설정, 멤버 관리, 모든 기능 사용 가능 |
| Admin | 자산 등록, 수정, 삭제, 사용자 관리 가능 |
| Manager | 자산 상태 변경, 위치 변경, 대여/반납, 점검 가능 |
| Member | 자산 조회, 대여 신청 가능 |

서버는 모든 요청에서 다음을 확인해야 합니다.

1. 로그인한 사용자인가?
2. 해당 조직의 멤버인가?
3. 요청한 작업을 수행할 권한이 있는가?
4. 접근하려는 리소스가 해당 조직에 속하는가?

## QR 접근 방식

자산에는 QR 접근용 공개 코드인 `publicCode`를 둡니다.

QR URL 예시는 다음과 같습니다.

```text
https://assetly.example.com/a/q8K2md91
```

이 URL은 자산번호를 직접 노출하지 않습니다.

QR을 스캔하면 프론트엔드의 `/a/{publicCode}` 경로로 이동하고, 백엔드는 다음을 확인합니다.

1. publicCode에 해당하는 자산이 있는가?
2. 사용자가 로그인했는가?
3. 사용자가 해당 자산의 조직 멤버인가?
4. 해당 사용자가 어떤 작업을 할 수 있는가?

권한이 없는 사용자는 자산 정보를 볼 수 없습니다.

## 개발 로드맵

### Phase 0. 기획과 설계

- 프로젝트 방향 정리
- 제품 기획서 작성
- 화면 흐름 설계
- DB 설계
- API 명세 작성
- 아키텍처 설계

### Phase 1. 프로젝트 뼈대 생성

- 백엔드 Spring Boot 프로젝트 생성
- 프론트엔드 Next.js 프로젝트 생성
- PostgreSQL Docker Compose 구성
- 기본 실행 확인

### Phase 2. 인증과 조직

- 회원가입
- 로그인
- JWT 인증
- 조직 생성
- 조직 멤버 구조
- 권한 체크 기반 구조

### Phase 3. 자산 관리

- 위치 관리
- 카테고리 관리
- 자산 등록
- 자산 목록
- 자산 상세
- 자산 수정

### Phase 4. QR과 변경 이력

- publicCode 생성
- QR 접근 URL
- 자산 변경 이력
- 권한별 자산 상세 접근

### Phase 5. 프론트엔드 앱 완성

- 앱 레이아웃
- 대시보드
- 로딩/에러/빈 상태
- 모바일 자산 상세 화면

### Phase 6. 테스트와 품질 개선

- 인증 테스트
- 조직 권한 테스트
- 자산 CRUD 테스트
- 다른 조직 데이터 접근 차단 테스트
- 핵심 흐름 수동 검증

### Phase 7. 배포와 운영

- Dockerfile
- 운영 Docker Compose
- Nginx
- HTTPS
- GitHub Actions
- 서버 배포

## 로컬 개발 예정 방식

초기 개발은 다음 방식으로 진행할 예정입니다.

```text
PostgreSQL: Docker Compose
Backend: Spring Boot local run
Frontend: Next.js local dev server
```

이후 배포 단계에서 프론트엔드, 백엔드, DB 실행 방식을 운영 환경에 맞게 정리합니다.

## 환경변수 예시

백엔드에서 사용할 환경변수 후보:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/assetly
SPRING_DATASOURCE_USERNAME=assetly
SPRING_DATASOURCE_PASSWORD=assetly
JWT_SECRET=change-me
JWT_ACCESS_TOKEN_EXPIRES_IN=3600
```

프론트엔드에서 사용할 환경변수 후보:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

## 포트폴리오에서 강조할 점

Assetly는 다음 내용을 설명할 수 있는 프로젝트로 만드는 것이 목표입니다.

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

## 현재 구현 상태 (2026-10-07)

Phase 1 프로젝트 뼈대부터 Phase 4 QR 현장 접근과 변경 이력을 완료했고, Phase 5의 조직 전환과 모바일 내비게이션까지 구현되어 있습니다.

완료된 항목:

- Spring Boot 백엔드 기본 구조
- 회원가입 API
- 로그인 API와 JWT access token 발급
- 인증 사용자 조회 기반
- 조직 생성 API
- 내 조직 목록 API
- 조직 상세 조회와 Owner 전용 수정 API
- 재사용 가능한 조직 멤버십·역할 권한 검사
- 비멤버 조직 데이터 접근 차단
- 조직 생성자를 Owner로 등록하는 구조
- 멤버 목록 조회와 가입 사용자 이메일 기반 추가
- Owner/Admin 역할 범위에 따른 역할 변경과 멤버 제거
- 마지막 Owner 보호와 조직 간 멤버 데이터 격리
- 조직별 위치 생성·목록·수정·삭제 API
- 조직별 카테고리 생성·목록·수정·삭제 API
- 기준 정보 이름 중복 방지와 조직 간 데이터 격리
- 자산 등록·검색 목록·상세·역할별 수정·비활성화 API
- 자산번호 중복 방지, 공개 코드 발급, 담당자·위치·카테고리 연결
- 실제 자산 상태 합계와 최근 자산을 사용하는 대시보드 API
- 사용 중인 위치와 카테고리 삭제 차단
- Next.js 프론트엔드 기본 구조
- 회원가입 화면과 API 연결
- 로그인 화면과 API 연결
- 첫 조직 생성 온보딩
- 로그인 후 조직 유무에 따른 화면 이동
- 여러 소속 조직 선택·전환과 선택 상태 유지
- 현재 계정에서 새 조직을 추가하는 진입 경로
- QR 자산 진입 시 해당 자산의 조직을 활성 조직으로 연결
- 모바일 조직 선택, 가로 내비게이션과 로그아웃 컨트롤
- 앱 대시보드 기본 레이아웃
- 조직 설정 화면과 API 연결
- 멤버 관리 화면과 API 연결
- 위치·카테고리 통합 관리 화면과 API 연결
- 자산 목록·등록·상세·수정 화면과 API 연결
- 검색·상태·카테고리·위치 필터와 페이지 이동
- 실제 데이터 기반 대시보드
- 로그인과 조직 권한을 확인하는 `publicCode` 기반 QR 자산 조회
- 자산별 QR 표시, 링크 복사와 SVG 다운로드
- 로그인 후 원래 QR 주소로 복귀하는 모바일 현장 화면
- 자산 등록·필드별 수정·비활성화 변경 이력 기록
- 자산별·조직 전체 변경 이력 화면과 필터
- 실제 변경 이력을 사용하는 대시보드 최근 활동
- PostgreSQL Docker Compose 설정
- Flyway 기반 DB 마이그레이션
- JPA 스키마 자동 변경 비활성화와 스키마 검증
- Maven Wrapper
- H2 기반 백엔드 테스트 환경
- Playwright 기반 데스크톱·모바일 브라우저 테스트
- 비로그인 QR 접근 후 로그인과 원래 자산 화면 복귀 자동 검증
- 모바일 QR 화면의 상태·위치·담당자 변경과 감사 이력 자동 검증
- Java 21·Node.js 22 기반 백엔드·프론트엔드 멀티스테이지 Docker 이미지
- PostgreSQL, 백엔드와 프론트엔드를 연결하는 전체 Docker Compose
- 컨테이너 healthcheck, 시작 순서, 자동 재시작과 PostgreSQL 영속 볼륨
- 운영 프로필의 필수 환경변수와 예시 JWT 키·기본 DB 비밀번호 차단
- 런타임 `BACKEND_URL`을 사용하는 프론트엔드 API 프록시
- GitHub push와 Pull Request에서 백엔드 테스트, 프론트엔드 검사·빌드, Playwright 핵심 흐름을 실행하는 CI
- 실패한 브라우저 테스트의 서버 로그, 스크린샷과 trace를 7일간 보관하는 CI artifact

검증 결과:

- `frontend`에서 `npm run build` 성공
- `frontend`에서 `npm run test:e2e` 성공: 6개 통과, 4개 프로젝트 조건부 건너뜀
- `backend`에서 `.\mvnw.cmd test` 성공: 총 58개 테스트 통과
- Flyway `V1`~`V4` 마이그레이션 적용과 Hibernate 스키마 검증 성공
- 상세 기능 테스트 기준과 실행 기록은 `docs/assetly-functional-test-spec.md` 참고

## 전체 Docker 실행

Docker가 설치된 환경에서는 PostgreSQL, 백엔드와 프론트엔드를 한 번에 실행할 수 있습니다.

### 1. 운영 환경변수 준비

프로젝트 루트에서 예시 파일을 복사합니다.

```powershell
Copy-Item .env.example .env
```

`.env`의 `POSTGRES_PASSWORD`와 `JWT_SECRET`을 반드시 새로운 값으로 교체합니다. `JWT_SECRET`은 48바이트 이상이어야 하며 예시 값이나 기본값이면 백엔드 시작이 차단됩니다.

PowerShell에서 64바이트 무작위 JWT 비밀값을 만들 수 있습니다.

```powershell
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
[Convert]::ToBase64String($bytes)
```

`.env`는 Git에서 제외되므로 저장소에 커밋하지 않습니다.

### 2. 구성 확인과 실행

```powershell
docker compose config
docker compose up --build -d
docker compose ps
```

모든 서비스가 healthy 상태가 되면 `http://localhost:3000`에서 접속합니다. 외부에는 프론트엔드 포트만 공개되며, 프론트엔드가 내부 네트워크의 백엔드로 API 요청을 전달합니다. PostgreSQL 포트는 로컬 호스트에만 바인딩됩니다.

### 3. 로그와 종료

```powershell
docker compose logs -f backend frontend
docker compose down
```

`docker compose down`은 컨테이너만 제거하고 PostgreSQL 데이터 볼륨은 보존합니다. 개발 데이터를 완전히 지울 때만 다음 명령을 사용합니다.

```powershell
docker compose down -v
```

현재 개발 PC에는 Docker가 설치되어 있지 않아 이미지 빌드와 PostgreSQL 컨테이너 기동은 아직 실행 검증하지 못했습니다. Dockerfile과 Compose YAML, 애플리케이션 빌드, API 프록시, health endpoint와 운영 비밀값 차단은 개별 검증했습니다.

## 개별 로컬 실행

### 1. PostgreSQL 실행

Docker가 설치된 환경에서 프로젝트 루트에서 실행합니다.

```powershell
docker compose up -d postgres
```

Docker 없이 개발할 때는 백엔드를 `local` 프로필로 실행해 H2 인메모리 DB를 사용할 수 있습니다.

애플리케이션 시작 시 Flyway가 `backend/src/main/resources/db/migration`의 SQL을 순서대로 적용합니다. JPA는 스키마를 수정하지 않고 엔티티와 실제 스키마가 일치하는지만 검증합니다.

### 2. 백엔드 실행

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

백엔드는 기본적으로 `http://localhost:8080`에서 실행됩니다.

### 3. 프론트엔드 실행

새 터미널에서 실행합니다.

```powershell
cd frontend
npm install
npm run dev
```

프론트엔드는 기본적으로 `http://localhost:3000`에서 실행됩니다. Next.js가 `/api/*` 요청을 로컬 백엔드로 전달합니다.

브라우저 자동 테스트는 백엔드와 프론트엔드를 실행한 상태에서 진행합니다.

```powershell
cd frontend
npx playwright install chromium
npm run test:e2e
```

## GitHub Actions 자동 검증

`.github/workflows/ci.yml`은 `main` push, Pull Request와 수동 실행에서 다음 작업을 수행합니다.

1. Java 21과 Maven 캐시를 준비하고 백엔드 테스트를 실행합니다.
2. Node.js 22와 npm 캐시를 준비하고 ESLint와 production build를 실행합니다.
3. 앞선 검사가 통과하면 H2 기반 백엔드와 production 프론트엔드를 실행합니다.
4. Chromium으로 조직 전환, 인증, QR 자산 조회와 현장 수정 흐름을 검증합니다.
5. 성공 여부와 관계없이 서버 로그와 Playwright 실패 자료를 7일간 artifact로 보관합니다.

같은 브랜치에 새 커밋이 올라오면 진행 중이던 이전 실행은 취소됩니다. 저장소의 `Actions` 탭에서 작업별 결과와 실패 자료를 확인할 수 있습니다.

## 다음 구현 목표

다음 단계는 Phase 5~7의 제품 마감과 배포 준비입니다.

1. 회원가입부터 첫 조직 생성까지 브라우저 자동화
2. Docker 설치 환경에서 PostgreSQL 전체 구성 실기동 검증
3. 실제 서버 배포, HTTPS와 백업 구성
