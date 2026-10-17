# Assetly

[![지속적 통합](https://github.com/wnsgh1a/assetly/actions/workflows/ci.yml/badge.svg)](https://github.com/wnsgh1a/assetly/actions/workflows/ci.yml)

소규모 조직을 위한 QR 기반 자산 관리 웹 애플리케이션입니다. 회사, 연구실, 동아리, 교육기관의 장비와 비품을 조직별로 관리하고, 현장에서 QR을 통해 위치·상태·담당자를 확인하거나 변경할 수 있습니다.

## 현재 상태

기준일: **2026-10-18**

회원가입부터 조직 생성, 자산 등록, QR 현장 수정과 변경 이력 조회까지 핵심 업무 흐름이 구현되어 있습니다. 운영용 Docker 구성과 PostgreSQL 데이터 영속성도 GitHub Actions에서 검증했습니다.

**실제 서버 배포는 아직 완료되지 않았습니다.** 공개 접속 주소, HTTPS, 운영 정기 백업·외부 보관과 운영 점검 구성이 남아 있습니다. 백업·복원 도구는 구현·검증됐으며 CI 통과와 운영 서비스 배포 완료는 구분합니다. 현재는 사용자의 요청에 따라 배포 작업을 진행하지 않습니다.

- Phase 0~4: 기획·설계, 프로젝트 기반, 인증·조직, 자산 관리, QR·변경 이력 완료.
- Phase 5: v1 화면 마감과 비배포 환경의 핵심 회귀 점검 완료. 조회 오류·재시도, 저장 보호, 세션 만료와 반응형 화면 검증 포함.
- Phase 6: 현재 핵심 흐름의 백엔드·브라우저 테스트 자동화 완료.
- Phase 7: 운영 컨테이너, CI, PostgreSQL 실기동·데이터 유지 검증 완료. 실제 배포·운영 준비는 진행 중.
- 백업·복원: custom-format 백업과 새 DB 복원 도구, 안전성 테스트와 PostgreSQL 복원 CI 구성. 운영 정기 스케줄·외부 보관·앱 전환 리허설은 미설정.
- Phase 8: 이미지 업로드, 점검, 대여·반납 등의 확장 기능은 미구현.

### 확인된 검증 결과

2026-10-18 화면 마감 검증: 프론트엔드 ESLint·production build 성공. 로컬 H2와 production 프론트엔드의 Playwright **27개 통과, 5개 조건부 제외**. 원격 H2·PostgreSQL 브라우저 테스트도 각각 같은 결과로 통과했습니다.

- 대시보드·목록·이력 조회 오류를 빈 데이터와 구분하고 재시도.
- 자산 선택 항목 조회 실패 시 저장 차단, 재시도 후 입력 보존과 공백 필수값 차단.
- 작업 중 인증 만료 시 로그인 복귀, 조직 전환 시 이전 자산 폼 제거.
- 자산 UI 등록·수정·비활성화, QR SVG 다운로드와 20개 초과 이력 페이지 이동.
- 360·768·1024·1440px에서 주요 9개 경로 가로 넘침 검사 및 자산 상세 스크린샷 확인.

[화면 마감·운영 구성·백업 복원 검증에 성공한 GitHub Actions 실행](https://github.com/wnsgh1a/assetly/actions/runs/39050684930), 코드 커밋 `8abc2a2`에서 다음 항목을 확인했습니다.

- 백엔드: 총 **58개 테스트 통과**.
- 프론트엔드: ESLint 검사와 production build 성공.
- H2 기반 브라우저 테스트: **27개 통과, 5개 프로젝트 조건부 건너뜀**.
- 실제 Docker·PostgreSQL 기반 브라우저 테스트: **27개 통과, 5개 프로젝트 조건부 건너뜀**.
- PostgreSQL 16, 운영 프로필 백엔드와 프론트엔드: 세 서비스 모두 healthy.
- Flyway `V1`~`V4` 마이그레이션과 Hibernate 스키마 검증 성공.
- 컨테이너 제거·재생성 후 계정, 조직, Owner 권한, 자산, 위치, QR 공개 코드와 대시보드 데이터 유지 확인.
- 백업 도구 안전성 테스트 **13개 통과**.
- 실제 DB 백업·새 DB 복원 후 전체 행·비밀번호 해시·QR 코드·이력·Flyway 기록 일치, identity 시퀀스 정상, 원본 불변과 기존 대상 덮어쓰기 차단 확인.

건너뛴 항목은 테스트 프로젝트별 실행 조건에 따른 제외이며, 모든 테스트 조합을 실행했다는 뜻은 아닙니다. 자동화 범위와 상세 실행 기록은 [기능 테스트 명세서](docs/assetly-functional-test-spec.md)를 참고합니다.

## 구현된 기능

### 계정과 조직

- 회원가입, 로그인, JWT access token 기반 인증과 로그아웃.
- 첫 조직 생성 온보딩과 로그인 후 조직 유무에 따른 화면 이동.
- 내 조직 목록, 조직 선택·전환과 선택 상태 유지.
- 현재 계정에서 추가 조직 생성.
- 조직 상세 조회와 Owner 전용 조직 설정 수정.
- 멤버 목록, 가입된 사용자의 이메일을 통한 멤버 추가, 역할 변경과 제거.
- 마지막 Owner 보호, 조직 멤버십·역할 검사와 다른 조직 데이터 접근 차단.

멤버 추가는 **이미 가입한 사용자**를 조직에 연결하는 방식입니다. 이메일 초대 발송 기능은 아직 없습니다.

### 자산과 기준 정보

- 조직별 자산 등록, 상세 조회, 수정과 비활성화.
- 자산 검색, 상태·카테고리·위치 필터와 페이지 이동.
- 자산번호 중복 방지와 위치·카테고리·담당자 연결.
- 위치·카테고리 생성, 조회, 수정과 삭제.
- 기준 정보 이름 중복 방지와 사용 중인 위치·카테고리 삭제 차단.
- 실제 자산 데이터로 계산하는 대시보드와 최근 변경 활동.

자산 삭제는 기록을 보존하는 **비활성화 방식**입니다. 데이터의 조직 소속과 권한은 서버에서 검증합니다.

### QR과 변경 이력

- 자산별 `publicCode` 발급과 `/a/{publicCode}` 접근 경로.
- 자산 상세에서 QR 표시, 링크 복사와 SVG 다운로드.
- 비로그인 QR 접근 시 로그인 후 원래 QR 주소로 복귀.
- QR 자산의 조직을 활성 조직으로 연결.
- 모바일 현장 화면에서 권한에 따른 상태·위치·담당자 변경.
- 자산 생성, 필드별 수정과 비활성화 이력 기록.
- 자산별·조직 전체 변경 이력 조회와 필터.

QR 링크는 비밀 없는 공개 조회 기능이 아닙니다. `publicCode`를 알고 있어도 로그인과 해당 조직 멤버십이 필요합니다.

### 화면과 실행 기반

- 대시보드, 자산 목록·등록·상세·수정, 기준 정보, 멤버 관리, 조직 설정과 이력 화면.
- 모바일 조직 선택, 공통 내비게이션과 로그아웃 컨트롤.
- Next.js의 같은 출처 `/api/*` 프록시와 런타임 `BACKEND_URL` 설정.
- Flyway 마이그레이션, JPA 스키마 검증과 H2 로컬 개발 프로필.
- 운영용 멀티스테이지 Docker 이미지, Compose healthcheck, 시작 순서와 자동 재시작.
- PostgreSQL 영속 볼륨과 운영 환경변수 검증.
- GitHub Actions의 백엔드·프론트엔드·브라우저·Docker 검증.

## 권한 구조

역할은 계정 전체가 아니라 **조직마다 따로** 부여됩니다. 같은 사용자가 한 조직에서는 Owner, 다른 조직에서는 Member일 수 있습니다.

- **Owner**: 조직 설정, 멤버 관리, 자산과 기준 정보 관리, 현장 수정과 조회.
- **Admin**: 자산과 기준 정보 관리, 현장 수정과 조회. 멤버 관리에서는 Manager·Member만 관리할 수 있으며 Owner·Admin을 추가하거나 변경·제거할 수 없습니다. 조직 설정 수정은 불가합니다.
- **Manager**: 자산과 이력 조회, 자산의 상태·위치·담당자 변경. 자산 등록·비활성화와 기준 정보 관리는 불가합니다.
- **Member**: 자산과 변경 이력 조회. 자산 수정은 불가합니다.

마지막 Owner를 제거하거나 다른 역할로 변경할 수 없습니다. 다른 조직의 자산·멤버·위치·카테고리는 ID를 알아도 접근할 수 없도록 검사합니다.

## 처음 사용하는 흐름

1. 회원가입하고 로그인합니다.
2. 첫 조직을 생성합니다. 생성자는 Owner가 됩니다.
3. 위치와 카테고리를 등록합니다.
4. 자산을 등록하고 필요한 위치·카테고리·담당자를 연결합니다.
5. 자산 상세에서 QR을 확인하거나 내려받아 부착합니다.
6. 휴대폰으로 QR을 스캔하고 로그인합니다.
7. 권한에 따라 위치·상태·담당자를 변경합니다.
8. 자산 상세나 조직 이력 화면에서 변경 내용을 확인합니다.

## 데이터 정책

- 고정된 예시 조직·자산을 넣는 시드 데이터는 제공하지 않습니다. 새 DB에서는 회원가입과 조직 생성부터 시작합니다.
- 대시보드 숫자와 활동 내역은 API의 실제 데이터로 표시합니다.
- `local` 프로필은 H2 인메모리 DB를 사용합니다. **백엔드 프로세스 종료 시 데이터가 사라집니다.**
- 전체 Docker 실행은 PostgreSQL 볼륨에 데이터를 보존합니다. 컨테이너 재생성만으로 데이터가 초기화되지 않습니다.
- 브라우저 테스트는 연결한 서버에 테스트 계정·조직·자산을 만듭니다. 운영 DB가 아닌 전용 로컬·테스트 환경에서 실행합니다.
- CI는 격리된 실행 환경을 사용하며 Docker 검증 종료 후 해당 테스트 컨테이너와 볼륨을 정리합니다. 개발 PC나 운영 DB에 CI 데이터를 넣지 않습니다.

## 기술 스택

- 프론트엔드: Next.js 15, React 19, TypeScript, Tailwind CSS, lucide-react, qrcode.react.
- 백엔드: Java 21, Spring Boot, Spring Security, JWT, Spring Data JPA, Maven Wrapper.
- DB: PostgreSQL 16, Flyway. 로컬 개발과 백엔드 테스트는 H2 사용.
- 검증: 백엔드 자동 테스트, ESLint, production build, Playwright Chromium 데스크톱·모바일 프로젝트.
- 실행·CI: Docker, Docker Compose, GitHub Actions, Node.js 22.

정확한 의존성과 버전은 `backend/pom.xml`, `frontend/package.json`과 `frontend/package-lock.json`을 기준으로 합니다. Nginx·HTTPS·클라우드 서버는 현재 완료된 인프라가 아니라 다음 배포 단계의 대상입니다.

## 로컬 개발 실행: Docker 없이

필요한 도구는 **JDK 21**과 **Node.js 22 / npm**입니다. Maven은 저장소의 Wrapper를 사용합니다. 아래 명령은 Windows PowerShell 기준입니다.

### 1. 백엔드

프로젝트 루트에서 실행합니다.

```powershell
cd backend
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

백엔드 주소는 `http://localhost:8080`입니다. `local` 프로필이 H2를 사용하므로 PostgreSQL을 따로 실행할 필요가 없습니다. 이 프로필의 DB와 JWT 설정은 개발 전용이며 운영에 사용하지 않습니다.

### 2. 프론트엔드

새 터미널을 프로젝트 루트에서 열어 실행합니다.

```powershell
cd frontend
npm ci
npm run dev
```

브라우저에서 **http://localhost:3000**으로 접속합니다. 프론트엔드의 `/api/*` 요청은 기본적으로 로컬 백엔드에 전달됩니다. 백엔드 주소를 변경할 때는 프론트엔드 서버의 `BACKEND_URL`을 설정합니다.

각 서버는 해당 터미널에서 `Ctrl+C`로 종료합니다. 이 방법은 빠른 개발·화면 확인용이며, 데이터 유지 확인에는 아래 PostgreSQL 실행 방식을 사용합니다.

## 전체 Docker 실행: PostgreSQL 사용

Docker 엔진과 Docker Compose가 필요합니다. Docker Desktop을 사용하는 경우 먼저 엔진을 실행합니다. 이 방식은 프론트엔드·백엔드·DB를 한 번에 실행하므로 별도의 로컬 Java·Node 서버를 띄우지 않습니다.

현재 개발 PC에는 Docker와 WSL이 설치되어 있지 않으며, 컨테이너 실기동 검증은 GitHub Actions에서 수행했습니다. 아래 절차는 Docker가 설치된 환경에서 사용합니다.

### 1. 환경변수 준비

프로젝트 루트에서 처음 한 번 복사합니다. 기존 `.env`가 있으면 덮어쓰지 않습니다.

```powershell
Copy-Item .env.example .env
```

`.env`에 설정할 항목:

- `POSTGRES_DB`: DB 이름. 기본 예시는 `assetly`.
- `POSTGRES_USER`: DB 계정. 기본 예시는 `assetly`.
- `POSTGRES_PASSWORD`: 새로 생성한 강력한 DB 비밀번호. 예시 값과 기본 비밀번호는 사용하지 않습니다.
- `POSTGRES_PORT`: 호스트의 DB 포트. 기본값 `5432`, 로컬 호스트에만 바인딩됩니다.
- `JWT_SECRET`: 48바이트 이상의 새 비밀값. 예시 값이면 운영 백엔드 시작이 차단됩니다.
- `JWT_ACCESS_TOKEN_EXPIRES_IN`: access token 유효 시간(초). 기본값 `3600`.
- `FRONTEND_PORT`: 브라우저 접속 포트. 기본값 `3000`.

PowerShell에서 64바이트 무작위 JWT 비밀값을 생성하는 예시입니다.

```powershell
$bytes = New-Object byte[] 64
$generator = [Security.Cryptography.RandomNumberGenerator]::Create()
try {
    $generator.GetBytes($bytes)
    [Convert]::ToBase64String($bytes)
} finally {
    $generator.Dispose()
}
```

생성한 값을 `.env`의 `JWT_SECRET`에 설정합니다. `.env`는 Git에서 제외됩니다. 비밀값을 문서·커밋·공개 로그에 남기지 않습니다. 루트 `.env`는 Compose가 읽으며 개별 Spring Boot 실행에 자동 적용되는 파일은 아닙니다.

### 2. 구성 확인과 기동

```powershell
docker compose config --quiet
docker compose up --build --wait --wait-timeout 180
docker compose ps
```

세 서비스가 healthy이면 **http://localhost:3000**에 접속합니다. 포트 충돌 시 `.env`의 `FRONTEND_PORT` 또는 `POSTGRES_PORT`를 변경합니다.

브라우저 접속용 포트는 프론트엔드에 연결됩니다. 백엔드의 `8080`은 Docker 내부 네트워크에서만 사용하고, PostgreSQL 호스트 포트는 `127.0.0.1`에만 바인딩됩니다. 이 구성 자체가 HTTPS나 공개 서버 방화벽 설정을 대신하지는 않습니다.

기동 시 Flyway가 `backend/src/main/resources/db/migration`의 SQL을 적용하고 JPA는 스키마 일치 여부만 검증합니다. JPA의 자동 스키마 변경에 의존하지 않습니다.

### 3. 로그, 종료와 데이터 보존

```powershell
docker compose logs -f backend frontend
docker compose down
```

`docker compose down` 후 같은 프로젝트에서 다시 실행하면 PostgreSQL 볼륨을 재사용합니다. Compose 프로젝트 이름을 바꾸면 다른 볼륨을 사용할 수 있으므로 운영에서는 이름을 일관되게 유지합니다.

**아래 명령은 DB 볼륨과 저장 데이터를 삭제합니다.** 개발 데이터를 의도적으로 초기화할 때만 사용합니다.

```powershell
docker compose down -v
```

## 테스트 실행

### 백엔드

프로젝트 루트 기준:

```powershell
cd backend
.\mvnw.cmd test
```

H2 기반으로 인증, 조직 권한·데이터 격리, 기준 정보, 자산과 변경 이력 등을 검증합니다. PostgreSQL 실기동과 볼륨 유지 검증은 별도의 Docker CI 작업이 담당합니다.

### 프론트엔드 정적 검사와 빌드

프로젝트 루트 기준:

```powershell
cd frontend
npm ci
npm run lint
npm run build
```

### 브라우저 핵심 흐름

전용 테스트 백엔드와 프론트엔드를 먼저 실행한 상태에서 진행합니다.

```powershell
cd frontend
npx playwright install chromium
npm run test:e2e
```

신규 사용자 온보딩, 조직 전환, 로그인·로그아웃, QR 로그인 복귀와 모바일 현장 수정 등을 검증합니다. 연결 주소와 프로젝트 설정은 `frontend/playwright.config.ts`를 기준으로 합니다. 실제 운영 환경을 테스트 대상으로 지정하지 않습니다.

## GitHub Actions

설정 파일: [.github/workflows/ci.yml](.github/workflows/ci.yml)

`main` push, Pull Request와 수동 실행에서 다음 네 작업을 수행합니다.

1. **백엔드 테스트**: Java 21과 Maven Wrapper로 테스트 실행.
2. **프론트엔드 검사·빌드**: Node.js 22, `npm ci`, 백업 도구 안전성 테스트, ESLint와 production build.
3. **브라우저 테스트**: 앞선 두 작업 통과 후 H2 백엔드와 production 프론트엔드로 Playwright 실행.
4. **Docker 및 PostgreSQL 검증**: 앞선 두 작업 통과 후 실제 운영 이미지·PostgreSQL로 데이터 유지와 Playwright 실행.

Docker 검증은 다음 순서로 진행합니다.

1. 실행별 Compose 프로젝트와 임시 비밀번호·JWT 비밀값 생성.
2. 이미지 빌드, PostgreSQL 16과 운영 프로필 앱 기동, 세 서비스 healthy 확인.
3. 계정·조직·위치·자산 생성 후 컨테이너 제거와 같은 볼륨으로 재기동.
4. 새 로그인으로 계정·Owner 권한·자산·위치·QR 공개 코드·대시보드 데이터 유지 확인.
5. 컨테이너 프론트엔드를 대상으로 Playwright 실행.
6. DB 백업·새 DB 복원 후 전체 데이터와 이력·Flyway 기록 비교, identity 시퀀스와 기존 대상 덮어쓰기 차단 확인.
7. 로그·실패 자료 보관과 해당 CI 컨테이너·볼륨 정리. DB 백업 파일은 업로드하지 않습니다.

데이터 유지 검증 스크립트는 [scripts/verify-docker-persistence.mjs](scripts/verify-docker-persistence.mjs)입니다. 컨테이너를 재생성하므로 `assetly-ci-실행번호-시도번호` 형식의 격리된 프로젝트만 허용합니다. 운영 프로젝트에서는 실행하지 않습니다.

같은 브랜치의 새 커밋이 올라오면 이전 진행 중 실행은 취소됩니다. 서버·컨테이너 로그와 Playwright 실패 자료는 artifact로 7일간 보관합니다. 결과는 저장소의 [Actions](https://github.com/wnsgh1a/assetly/actions)에서 확인합니다.

**현재 워크플로는 자동 검증용 CI입니다. 실제 서버로 자동 배포하는 CD는 아직 없습니다.**

## DB 백업과 복원

Node.js 22 이상과 실행 중인 Compose PostgreSQL이 필요합니다. 프로젝트 루트에서 실행합니다.

```powershell
node scripts/database-backup.mjs backup
node scripts/database-backup.mjs restore --file backups/실제백업파일.dump --database assetly_restore_check
node --test scripts/database-backup.test.mjs
```

기본 백업은 `backups/`에 저장됩니다. 복원은 **새 `assetly_restore_*` DB**에만 허용하며 원본 DB와 앱 연결을 변경하지 않습니다. 기존 파일·DB는 덮어쓰지 않습니다. 백업 파일에는 개인정보와 비밀번호 해시가 들어 있으므로 외부 보관·암호화·접근 제한이 필요합니다.

정기 실행과 외부 저장소 전송은 자동 설정되지 않습니다. 상세 절차와 장애 복구 기준은 [백업·복원 가이드](docs/assetly-backup-and-recovery.md)를 참고합니다.

## 저장소 구조

```text
assetly/
  .github/workflows/ci.yml          # 자동 검증
  backend/
    src/main/java/                 # API, 도메인, 인증과 권한
    src/main/resources/
      db/migration/                # Flyway V1~V4
      application*.yml             # 공통, local, prod 설정
    src/test/                      # 백엔드 테스트
    pom.xml
    mvnw.cmd
    Dockerfile
  frontend/
    app/                           # 화면과 API 프록시
    components/                    # 공통 UI
    lib/                           # API 호출과 타입
    e2e/                           # Playwright 테스트
    playwright.config.ts
    package.json
    package-lock.json
    Dockerfile
  scripts/verify-docker-persistence.mjs
  scripts/database-backup.mjs       # 백업·새 DB 복원 CLI
  scripts/verify-database-backup.mjs # CI 전용 실제 복원 검증
  docs/                            # 기획, 설계, 테스트와 결정 기록
  .env.example
  docker-compose.yml
  README.md
```

주요 도메인은 사용자(`User`), 조직(`Organization`), 조직 멤버(`OrganizationMember`), 자산(`Asset`), 위치(`Location`), 카테고리(`AssetCategory`), 변경 이력(`AssetHistory`)입니다.

## 참고 문서

- [프로젝트 가이드](docs/assetly-project-guide.md): 방향, 범위와 개발 원칙.
- [v1 제품 기획](docs/assetly-v1-product-plan.md): 대상 사용자와 핵심 업무 흐름.
- [화면 흐름](docs/assetly-screen-flow.md): 화면 구성과 사용자 시나리오.
- [DB 설계](docs/assetly-db-design.md): 데이터 모델과 관계.
- [API 명세](docs/assetly-api-spec.md): REST API와 권한·응답 구조.
- [아키텍처](docs/assetly-architecture.md): 구성 요소와 배포 구조.
- [개발 로드맵](docs/assetly-development-roadmap.md): 단계별 목표와 진행 상태.
- [결정 기록](docs/assetly-decision-log.md): 변경 이유와 검증 기록.
- [기능 테스트 명세](docs/assetly-functional-test-spec.md): 테스트 케이스, 실행 기준과 결과.
- [백업·복원 가이드](docs/assetly-backup-and-recovery.md): 백업 도구, 별도 DB 복원, 보관 정책과 장애 복구.
- [v1 마감 체크리스트](docs/assetly-release-checklist.md): 비배포 검증 완료 범위와 실제 배포 후 확인할 항목.

계획 문서에는 향후 목표도 포함되어 있습니다. 현재 구현 여부는 이 README의 상태 요약과 결정 기록을 함께 확인합니다.

## 남은 작업과 우선순위

### 배포 목적과 예산 원칙

2026-10-12 기준 **포트폴리오 완성 우선, 호스팅 월 0원 우선 목표**입니다. 소수 외부 사용자의 이용은 환영하되 사업화나 고비용 인프라 확장으로 방향을 바꾸지 않습니다.

무료 서버에 기존 앱·DB를 함께 배포하는 방식을 우선 검토하고, 유료 전환·도메인 구매·과금 자원 생성은 별도 승인 없이는 진행하지 않습니다. 기간 한정 체험은 장기 무료로 간주하지 않습니다. 무료 서버 확보가 어려우면 임시 시연·영상으로 대체하며, 가용성과 데이터 보관 한계는 명시합니다. 아직 배포 공급자는 확정하지 않았습니다.

### v1 배포·운영 준비

1. 실제 배포 서버와 도메인 결정, 운영 비밀값·접근 권한 관리.
2. 서버에 Docker 구성 배포, 리버스 프록시와 HTTPS, 방화벽 구성.
3. 백업 도구를 운영 정기 스케줄·외부 저장소에 연결하고, 외부 사본 복원과 앱 전환 리허설 수행.
4. 장애 확인용 로그·상태 점검, 재시작·업데이트·롤백 절차 정리.
5. 공개 환경에서 회원가입부터 QR 수정까지 최종 검증. 로컬 화면 마감은 완료했으며 실제 서버 주소·HTTPS·ARM 환경 등 배포 차이는 별도 확인합니다.

컨테이너 재생성 후 데이터가 남는다는 검증은 **백업·복원 검증을 대신하지 않습니다.** 영속 볼륨이 있어도 서버나 볼륨 손실에 대비한 별도 백업이 필요합니다.

### v1.1 이후 확장 후보

이미지 업로드, 점검 기록, 대여·반납, 이메일 초대, Excel 내보내기, QR 라벨 PDF와 이메일 알림은 아직 구현하지 않았습니다. 실제 사용에서 필요성을 확인한 뒤 추가합니다.

결제·요금제, 소셜 로그인, 네이티브 모바일 앱, 복잡한 통계와 슈퍼 어드민도 현재 v1 범위에 포함하지 않습니다. 우선 목표는 이미 구현한 핵심 흐름을 실제 운영 환경에서 안전하게 사용할 수 있게 마무리하는 것입니다.
