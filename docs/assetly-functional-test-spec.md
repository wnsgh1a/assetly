# Assetly 기능 테스트 명세서

이 문서는 Assetly의 기능 요구사항을 테스트 케이스로 추적하고, 구현 단계마다 실행 결과를 누적하는 기준 문서다.

## 1. 테스트 원칙

- 제품 기획서, 화면 흐름, API 명세를 기대 동작의 기준으로 사용한다.
- 구현되지 않은 기능은 실패로 처리하지 않고 `대기`로 분류한다.
- 구현 완료로 표시된 기능은 정상 흐름, 입력 검증, 인증, 권한, 조직 간 데이터 격리를 함께 검증한다.
- 자동 테스트 데이터는 H2 테스트 DB의 트랜잭션 안에서 생성하고 테스트 종료 시 롤백한다.
- 개발·운영 DB에 고정된 예시 데이터나 시드 데이터를 넣지 않는다.
- 결함이 발견되면 테스트를 삭제하거나 기대값을 낮추지 않고 원인을 수정한다.

## 2. 상태 정의

- `통과`: 기대 결과와 실제 결과가 일치한다.
- `실패`: 구현됐지만 기대 결과와 다르다.
- `차단`: 환경 문제로 실행할 수 없다.
- `대기`: 아직 구현되지 않아 실행 대상이 아니다.
- `수동`: 브라우저 상호작용 또는 시각 확인이 필요한 항목이다.

## 3. 테스트 환경

- 백엔드 자동 테스트: Spring Boot Test, MockMvc, JUnit 5, H2 PostgreSQL 호환 모드
- 프런트엔드 정적 검증: Next.js production build와 TypeScript 검사
- 통합 수동 테스트: 로컬 프런트엔드 `http://localhost:3000`, 로컬 백엔드 `http://localhost:8080`
- 운영 데이터 영향: 없음

## 4. 현재 자동화 대상

### 공통

#### FT-COM-001 헬스체크

- 선행 조건: 없음
- 실행: `GET /api/health`
- 기대: `200`, `data.status=UP`, 서비스명과 확인 시각 반환
- 자동화: `CurrentFeatureFunctionalTests.healthCheck`

### 인증

#### FT-AUTH-001 회원가입 성공

- 입력: 유효한 이름, 이메일, 8자 이상 비밀번호
- 기대: `200`, 사용자 ID·이메일·이름 반환, 비밀번호 미노출
- 자동화: `CurrentFeatureFunctionalTests.signup`

#### FT-AUTH-002 이메일 중복 방지

- 선행 조건: 동일 이메일 사용자가 존재함
- 실행: 같은 이메일로 재가입
- 기대: `400`, `USER_EMAIL_DUPLICATED`
- 자동화: `CurrentFeatureFunctionalTests.rejectDuplicatedEmail`

#### FT-AUTH-003 회원가입 입력 검증

- 입력: 잘못된 이메일, 8자 미만 비밀번호, 빈 이름
- 기대: 각 요청에 `400`, `VALIDATION_ERROR`
- 자동화: `CurrentFeatureFunctionalTests.validateSignupRequest`

#### FT-AUTH-004 로그인 성공

- 선행 조건: 가입된 사용자
- 입력: 올바른 이메일과 비밀번호
- 기대: `200`, JWT access token과 사용자 정보 반환
- 자동화: `CurrentFeatureFunctionalTests.login`

#### FT-AUTH-005 로그인 실패 정보 보호

- 입력: 잘못된 비밀번호 또는 존재하지 않는 이메일
- 기대: 두 경우 모두 `400`, `AUTH_INVALID_CREDENTIALS`
- 자동화: `CurrentFeatureFunctionalTests.rejectInvalidCredentials`

#### FT-AUTH-006 내 정보 조회

- 선행 조건: 유효한 JWT
- 실행: `GET /api/auth/me`
- 기대: `200`, 토큰 사용자 정보 반환
- 자동화: `CurrentFeatureFunctionalTests.findMe`

#### FT-AUTH-007 인증 필수 API 보호

- 입력: 토큰 없음 또는 잘못된 토큰
- 실행: 내 정보와 조직 목록 조회
- 기대: `401`, `AUTH_UNAUTHORIZED`, 표준 에러 JSON
- 자동화: `CurrentFeatureFunctionalTests.requireAuthentication`

### 조직

#### FT-ORG-001 가입 직후 빈 조직 목록

- 선행 조건: 가입했지만 조직을 만들지 않은 사용자
- 실행: `GET /api/organizations`
- 기대: `200`, 빈 배열
- 자동화: `CurrentFeatureFunctionalTests.emptyOrganizationList`

#### FT-ORG-002 조직 생성과 Owner 등록

- 실행: 조직 생성 후 내 조직 목록 조회
- 기대: 생성 응답과 목록에 조직이 표시되고 `myRole=OWNER`
- 자동화: `CurrentFeatureFunctionalTests.createAndFindOrganization`

#### FT-ORG-003 조직 입력 검증

- 입력: 빈 조직명, 100자를 초과한 조직명
- 기대: `400`, `VALIDATION_ERROR`
- 자동화: `CurrentFeatureFunctionalTests.validateOrganizationRequest`

#### FT-ORG-004 사용자별 조직 목록 격리

- 선행 조건: 서로 다른 사용자 두 명, 첫 사용자만 조직 보유
- 실행: 두 번째 사용자가 자신의 조직 목록 조회
- 기대: 첫 사용자의 조직이 노출되지 않음
- 자동화: `CurrentFeatureFunctionalTests.isolateOrganizationsByUser`

#### FT-ORG-005 조직 상세 조회

- 선행 조건: 조직 멤버
- 실행: `GET /api/organizations/{organizationId}`
- 기대: `200`, 조직 정보와 요청 사용자의 역할 반환
- 자동화: `OrganizationAccessFunctionalTests.memberFindsOrganization`

#### FT-ORG-006 다른 조직 상세 접근 차단

- 선행 조건: 요청 사용자가 대상 조직의 멤버가 아님
- 기대: `404`, `ORGANIZATION_NOT_FOUND`, 조직 정보 미노출
- 자동화: `OrganizationAccessFunctionalTests.nonMemberCannotFindOrganization`

#### FT-ORG-007 Owner의 조직 수정

- 실행: Owner가 조직 이름과 설명 수정
- 기대: `200`, 수정 결과 저장 및 상세 조회에 반영
- 자동화: `OrganizationAccessFunctionalTests.ownerUpdatesOrganization`

#### FT-ORG-008 Member의 조직 수정 차단

- 실행: Member가 조직 정보 수정
- 기대: `403`, `AUTH_FORBIDDEN`
- 자동화: `OrganizationAccessFunctionalTests.memberCannotUpdateOrganization`

#### FT-ORG-009 조직 수정 입력 검증

- 입력: 빈 조직 이름
- 기대: `400`, `VALIDATION_ERROR`
- 자동화: `OrganizationAccessFunctionalTests.validateOrganizationUpdate`

## 5. 멤버 관리 기능 테스트

#### FT-MEMBER-001 Owner의 멤버 추가와 목록 조회

- 실행: 가입된 사용자 이메일과 역할로 멤버 추가 후 목록 조회
- 기대: `200`, 추가된 사용자 정보와 역할 반환
- 자동화: `OrganizationMemberFunctionalTests.ownerAddsAndFindsMembers`

#### FT-MEMBER-002 중복 멤버와 미가입 사용자 차단

- 기대: 중복 추가는 `409 ORGANIZATION_MEMBER_DUPLICATED`, 미가입 이메일은 `404 USER_NOT_FOUND`
- 자동화: `OrganizationMemberFunctionalTests.rejectsDuplicateAndUnknownUser`

#### FT-MEMBER-003 Manager와 Member의 멤버 관리 차단

- 실행: 권한 없는 사용자가 멤버 목록 조회
- 기대: `403`, `AUTH_FORBIDDEN`
- 자동화: `OrganizationMemberFunctionalTests.regularMemberCannotFindMembers`

#### FT-MEMBER-004 Owner의 역할 변경

- 실행: Owner가 Member를 Manager로 변경
- 기대: `200`, 변경된 역할 반환 및 저장
- 자동화: `OrganizationMemberFunctionalTests.ownerChangesMemberRole`

#### FT-MEMBER-005 마지막 Owner 보호

- 실행: 유일한 Owner의 역할 변경과 제거 시도
- 기대: `409`, `ORGANIZATION_LAST_OWNER_REQUIRED`
- 자동화: `OrganizationMemberFunctionalTests.protectsLastOwner`

#### FT-MEMBER-006 Admin의 관리 범위 제한

- 실행: Admin이 Manager를 추가하고 Owner/Admin 대상 작업 및 상위 역할 지정을 시도
- 기대: Manager 추가는 성공, 제한된 작업은 `403 AUTH_FORBIDDEN`
- 자동화: `OrganizationMemberFunctionalTests.limitsAdminManagement`

#### FT-MEMBER-007 Owner의 멤버 제거

- 실행: Owner가 Member 제거
- 기대: `200`, 이후 목록에서 제거됨
- 자동화: `OrganizationMemberFunctionalTests.ownerRemovesMember`

#### FT-MEMBER-008 조직 간 멤버 ID 격리

- 실행: 다른 조직의 memberId로 역할 변경과 제거 시도
- 기대: `404`, `ORGANIZATION_MEMBER_NOT_FOUND`
- 자동화: `OrganizationMemberFunctionalTests.isolatesMemberResourcesByOrganization`

## 6. 위치·카테고리 기능 테스트

### 위치

- `FT-LOC-001`: Owner가 이름과 설명으로 위치를 생성하고 공백이 정리된다.
- `FT-LOC-002`: 조직 멤버가 위치 목록을 이름순으로 조회한다.
- `FT-LOC-003`: Admin이 위치 이름과 설명을 수정한다.
- `FT-LOC-004`: Owner가 사용되지 않는 위치를 삭제한다.
- `FT-LOC-005`: 조직 내 중복 이름과 빈 이름을 차단한다.
- `FT-LOC-006`: 일반 멤버의 변경, 비멤버 조회, 다른 조직 locationId 조작을 차단한다.
- 자동화: `ReferenceDataFunctionalTests`의 `FT-LOC-001~006`

### 카테고리

- `FT-CAT-001`: Owner가 카테고리를 생성하고 공백이 정리된다.
- `FT-CAT-002`: 조직 멤버가 카테고리 목록을 이름순으로 조회한다.
- `FT-CAT-003`: Admin이 카테고리 이름을 수정한다.
- `FT-CAT-004`: Owner가 사용되지 않는 카테고리를 삭제한다.
- `FT-CAT-005`: 조직 내 중복 이름과 빈 이름을 차단한다.
- `FT-CAT-006`: 일반 멤버의 변경, 비멤버 조회, 다른 조직 categoryId 조작을 차단한다.
- 자동화: `ReferenceDataFunctionalTests`의 `FT-CAT-001~006`

비활성화되지 않은 자산이 참조하는 위치와 카테고리 삭제는 `LOCATION_IN_USE`, `CATEGORY_IN_USE`로 차단한다.

## 7. 자산과 대시보드 기능 테스트

### 자산

- `FT-ASSET-001`: 필수값으로 자산을 등록하고 publicCode와 참조 정보를 반환한다.
- `FT-ASSET-002`: 대소문자를 정규화한 자산번호는 조직 안에서 중복될 수 없다.
- `FT-ASSET-003`: 검색·상태·카테고리·위치 필터와 페이지 정보를 검증한다.
- `FT-ASSET-004`: 조직 멤버가 상세 정보와 담당자 후보를 조회한다.
- `FT-ASSET-005`: Admin이 자산 기본 정보와 운영 정보를 수정한다.
- `FT-ASSET-006`: Manager는 상태·위치·담당자만 수정한다.
- `FT-ASSET-007`: Member의 수정과 비활성화를 차단한다.
- `FT-ASSET-008`: 비활성화한 자산은 목록·상세에서 제외하고 사용 중 기준 정보를 보호한다.
- `FT-ASSET-009`: 다른 조직의 자산·카테고리·담당자 ID를 사용할 수 없다.
- 자동화: `AssetFunctionalTests`의 `FT-ASSET-001~009`

### 대시보드

- `FT-DASH-001`: 실제 자산 상태별 합계, 전체 합계, 최근 자산과 등록 이력을 반환한다.
- `FT-DASH-002`: 최근 자산 변경 이력을 실제 데이터 기준으로 반환한다.
- `FT-DASH-003`: 신규 조직은 모든 합계가 0이고 최근 자산이 없다.
- 자동화: `AssetFunctionalTests`의 `FT-DASH-001~003`

### QR과 변경 이력

- `FT-QR-001`: 조직 멤버가 publicCode로 자산과 자신의 역할을 조회한다.
- `FT-QR-002`: 비로그인 요청과 비멤버의 publicCode 조회를 차단한다.
- `FT-HISTORY-001`: 등록·필드 수정·비활성화마다 작업자와 변경 내용이 최신순으로 기록된다.
- `FT-HISTORY-002`: 조직 이력 필터를 지원하고 다른 조직의 접근을 차단한다.
- 자동화: `AssetFunctionalTests`의 `FT-QR-001~002`, `FT-HISTORY-001~002`

## 8. 현재 프런트엔드 시나리오

### FT-WEB-001 회원가입 화면

- `/signup`에서 필수값과 이메일 형식을 브라우저가 검증한다.
- 가입 성공 시 `/login?registered=1`로 이동한다.
- 중복 이메일이면 서버 메시지를 `role=alert` 영역에 표시한다.

### FT-WEB-002 로그인 후 조직 분기

- 조직이 없는 사용자는 `/onboarding/organization`으로 이동한다.
- 조직이 있는 사용자는 `/app`으로 이동한다.
- 로그인 실패 시 서버 메시지를 표시한다.

### FT-WEB-003 조직 온보딩

- 조직 생성 성공 시 `/app`으로 이동한다.
- 인증이 만료됐으면 `/login?next=/onboarding/organization`으로 이동한다.

### FT-WEB-004 앱 진입과 로그아웃

- `/app`은 마지막으로 선택한 소속 조직을 표시하고 저장값이 없으면 첫 번째 조직을 표시한다.
- 조직이 없으면 온보딩으로 이동한다.
- 인증이 없으면 로그인 화면으로 이동하고 토큰을 제거한다.
- 로그아웃하면 토큰을 제거하고 `/login`으로 이동한다.

### FT-WEB-005 빈 상태와 목업 데이터 금지

- 신규 조직의 대시보드 수치는 모두 `0`이다.
- 최근 변경 내역이 없으면 빈 상태 문구를 표시한다.
- 특정 조직, 장비, 담당자 이름을 실제 데이터처럼 하드코딩하지 않는다.

### FT-WEB-006 조직 설정

- 실제 조직 이름과 설명을 입력값으로 표시한다.
- Owner는 수정 후 성공 메시지와 갱신된 조직명을 확인한다.
- Owner가 아닌 멤버는 읽기 전용 화면을 보고 저장 버튼이 표시되지 않는다.

### FT-WEB-007 멤버 관리

- Owner와 Admin에게만 사이드바 멤버 메뉴가 표시된다.
- 가입된 사용자 이메일과 허용된 역할로 멤버를 추가한다.
- 역할 변경을 저장하고 확인 후 멤버를 제거한다.
- 서버의 중복, 권한, 마지막 Owner 보호 오류를 화면에 표시한다.

### FT-WEB-008 위치·카테고리 관리

- `/app/classification`에서 위치와 카테고리를 탭으로 전환한다.
- Owner와 Admin은 항목을 추가하고 인라인으로 수정·삭제한다.
- Manager와 Member는 실제 목록을 읽기 전용으로 조회한다.
- 빈 목록, 로딩, 성공, 서버 오류 상태를 표시한다.

### FT-WEB-009 자산 관리와 대시보드

- `/app/assets`에서 검색·필터·페이지 이동 후 자산 상세로 진입한다.
- Owner와 Admin은 자산을 등록하고 전체 정보를 수정·비활성화한다.
- Manager는 상세에서 상태·위치·담당자만 변경하며 Member는 읽기 전용이다.
- 대시보드 합계와 최근 자산은 실제 API 데이터를 표시한다.

### FT-WEB-010 QR 현장 접근과 변경 이력

- 자산 상세에서 실제 `/a/{publicCode}` URL의 QR을 표시하고 링크 복사와 SVG 다운로드를 제공한다.
- 비로그인 상태로 QR URL에 접근하면 `next`에 원래 경로를 보존해 로그인 후 복귀한다.
- 조직 멤버는 모바일 QR 화면에서 역할에 맞는 자산 정보를 수정한다.
- 자산 상세와 조직 전체 이력 화면에서 실제 변경 내용을 최신순으로 확인한다.
- 대시보드 최근 활동은 실제 변경 이력을 표시한다.

### FT-WEB-011 조직 전환과 모바일 내비게이션

- 조직 선택 목록에 현재 사용자의 모든 소속 조직과 역할을 표시한다.
- 조직 전환 즉시 화면 데이터를 새 조직 기준으로 다시 조회한다.
- 새로고침 후에도 마지막 선택 조직을 유지한다.
- 새 조직 만들기를 선택하면 조직 생성 화면으로 이동한다.
- QR 자산 조회 후 워크스페이스로 돌아가면 해당 자산의 조직을 선택한다.
- 모바일에서 조직 선택, 주요 메뉴와 로그아웃을 사용할 수 있다.
- 자동화: `e2e/organization-switching.spec.ts`

## 9. 핵심 사용자 흐름

### FT-E2E-001 신규 사용자 온보딩

1. 회원가입한다.
2. 로그인한다.
3. 조직이 없어 조직 생성 화면으로 이동한다.
4. 조직을 생성한다.
5. 앱 대시보드에서 조직명과 빈 자산 현황을 확인한다.
6. 로그아웃한다.
7. 보호된 앱 화면에 다시 접근하면 로그인으로 이동한다.

현재 상태: 로그인·로그아웃과 보호 화면 재접근 차단은 Playwright로 자동화됨. 회원가입과 첫 조직 생성 연결 흐름은 수동 실행 대상이다.

### FT-E2E-002 QR 로그인 복귀

1. 비로그인 상태에서 실제 `publicCode` QR 주소에 접근한다.
2. 로그인 화면의 `next`에 원래 QR 주소가 보존되는지 확인한다.
3. 로그인 후 원래 자산 화면과 조직명이 표시되는지 확인한다.
4. QR 자산의 조직이 활성 조직으로 저장되는지 확인한다.

현재 상태: Playwright 데스크톱 프로젝트에서 자동화됨.

### FT-E2E-003 모바일 QR 자산 수정

1. 모바일 뷰포트에서 QR 자산 화면을 연다.
2. 상태, 위치와 담당자를 변경하고 저장한다.
3. 서버 응답값이 폼에 유지되고 성공 메시지가 표시되는지 확인한다.
4. 상태·위치·담당자 변경 이력이 모두 표시되는지 확인한다.

현재 상태: Playwright 모바일 프로젝트에서 자동화됨.

## 10. 향후 자동화할 케이스

- `FT-E2E-004`: 회원가입부터 첫 조직 생성까지 신규 사용자 전체 흐름을 자동화한다.

## 11. 실행 명령

백엔드 전체 테스트:

```powershell
cd backend
.\mvnw.cmd test
```

현재 기능 테스트만 실행:

```powershell
cd backend
.\mvnw.cmd "-Dtest=CurrentFeatureFunctionalTests" test
```

프런트엔드 빌드 검증:

```powershell
cd frontend
npm run build
```

브라우저 자동 테스트(백엔드와 프런트엔드 실행 필요):

```powershell
cd frontend
npm run test:e2e
```

## 12. 통과 기준

- 현재 구현 기능의 자동 테스트가 전부 통과한다.
- 프런트엔드 production build가 경고성 오류 없이 완료된다.
- 인증이 필요한 API는 토큰 미제공과 변조 토큰을 모두 차단한다.
- 한 사용자의 조직 데이터가 다른 사용자에게 노출되지 않는다.
- 테스트 실행 후 개발 DB에 테스트 데이터가 남지 않는다.

## 13. 실행 기록

실행 기록은 날짜, 대상 버전, 자동 테스트 결과, 수동 테스트 결과, 발견 결함 순서로 갱신한다. 최신 실행 결과가 위에 오도록 기록한다.

### 2026-10-07 GitHub Actions 자동 검증 구성

- 대상: 백엔드 테스트, 프론트엔드 ESLint·production build, Playwright 핵심 사용자 흐름
- 실행 조건: `main` push, Pull Request, GitHub 수동 실행
- 실행 환경: Ubuntu, Java 21, Node.js 22, Chromium, H2 로컬 프로필
- 실패 분석: 백엔드·프론트엔드 로그와 Playwright 결과를 artifact로 7일간 보관
- 로컬 사전 검증: 백엔드 58개 테스트, 프론트엔드 ESLint·build, Playwright 전체 실행
- 원격 검증: GitHub Actions 실행 #2에서 백엔드, 프론트엔드와 브라우저 작업 모두 통과

### 2026-10-07 운영 컨테이너와 환경변수 검증

- 대상: 백엔드·프론트엔드 이미지, 전체 Compose, runtime API 프록시, health endpoint, 운영 비밀값 차단
- 백엔드 결과: 기존 54개와 운영 환경 검증 4개를 포함해 총 58개 통과, 실패 0
- 프런트엔드 결과: standalone production build와 `/api/health` 런타임 프록시 통과
- 설정 결과: 예시 JWT 키를 사용한 `prod` 프로필 시작이 오류 메시지와 종료 코드 1로 차단됨
- 정적 검사: Compose와 운영 YAML 파싱 및 포맷 검사 통과
- 제한 사항: 현재 PC에 Docker가 없어 실제 이미지 빌드와 PostgreSQL 16 컨테이너 기동은 미검증
- 후속 항목: GitHub Actions 자동화와 Docker 설치 환경의 전체 기동 검증

### 2026-10-02 QR 핵심 브라우저 흐름 자동화

- 대상: 비로그인 QR 접근, 로그인 후 원래 주소 복귀, 모바일 상태·위치·담당자 수정, 필드별 변경 이력
- 브라우저 결과: Playwright 데스크톱·모바일 6개 통과, 프로젝트 조건에 따른 4개 건너뜀, 실패 0
- 프런트엔드 결과: production build 및 TypeScript 검사 통과
- 발견 및 수정: 자산 저장 후 폼 재생성으로 성공 메시지가 즉시 사라지던 문제 수정
- 안정화: Playwright 워커 2개와 테스트 제한 시간 45초를 적용하고 production 서버 기준으로 검증
- 후속 항목: 운영 Docker 구성과 신규 사용자 전체 흐름 자동화

### 2026-10-02 조직 전환과 브라우저 자동화

- 대상: 여러 조직 선택·전환, 선택 유지, 새 조직 생성 진입, 모바일 내비게이션, 로그인·로그아웃과 보호 화면
- 브라우저 결과: Playwright 데스크톱·모바일 4개 통과, 프로젝트 조건에 따른 2개 건너뜀, 실패 0
- 프런트엔드 결과: production build 및 TypeScript 검사 통과
- 데이터 처리: 고유 테스트 계정과 조직을 로컬 H2에 생성하며 검증 후 백엔드 재시작으로 초기화
- 후속 항목: QR 로그인 복귀와 모바일 자산 수정 브라우저 자동화

### 2026-10-02 QR 현장 접근과 변경 이력

- 대상: publicCode 보안 조회, 로그인 복귀 경로, QR 표시, 필드별 감사 이력, 조직 이력 필터, 대시보드 최근 활동
- 백엔드 결과: 총 54개 통과, 실패 0, 오류 0, 건너뜀 0
- DB 결과: Flyway `V4` 적용 후 Hibernate 스키마 검증 성공
- 프런트엔드 결과: `/a/[publicCode]`, `/app/history`, QR 도구, 자산별 이력과 최근 활동 production build 통과
- 후속 항목: 브라우저 핵심 흐름 자동화, 조직 전환, 운영 배포 구성

### 2026-10-02 자산 관리와 실제 대시보드

- 대상: 자산 CRUD·필터·페이지, 역할별 수정, 비활성화, 참조 무결성, 실제 대시보드 집계
- 백엔드 결과: 총 49개 통과, 실패 0, 오류 0, 건너뜀 0
- DB 결과: Flyway `V3` 적용 후 Hibernate 스키마 검증 성공
- 프런트엔드 결과: 자산 목록·등록·상세 동적 경로와 대시보드 production build 통과
- 후속 완료: publicCode 공개 접근, QR 표시, 자산 변경 이력과 최근 활동을 다음 기록에서 검증

### 2026-10-02 위치·카테고리 관리

- 대상: 위치·카테고리 CRUD, 이름 중복과 입력 검증, 역할 권한, 조직 간 리소스 격리
- 백엔드 결과: 총 38개 통과, 실패 0, 오류 0, 건너뜀 0
- DB 결과: Flyway `V2` 적용 후 Hibernate 스키마 검증 성공
- 프런트엔드 결과: `/app/classification` 포함 production build 및 TypeScript 검사 통과
- 후속 완료: 자산 참조 추가 후 사용 중 위치·카테고리 삭제 차단 테스트 활성화

### 2026-10-02 멤버 관리

- 대상: 멤버 목록·추가·역할 변경·제거, Admin 관리 범위, 마지막 Owner 보호, 조직 간 데이터 격리
- 백엔드 결과: 총 26개 통과, 실패 0, 오류 0, 건너뜀 0
- 프런트엔드 결과: `/app/members` 멤버 관리 화면 production build 및 TypeScript 검사 통과
- 정책 결과: Owner 전체 관리, Admin의 Manager/Member 관리, 비권한자 `403`, 다른 조직 memberId `404`

### 2026-10-02 Flyway 마이그레이션 도입

- 대상: Flyway V1 스키마 생성, JPA 스키마 검증, 기존 인증·조직 회귀 테스트
- 백엔드 결과: 총 13개 통과, 실패 0, 오류 0, 건너뜀 0
- 확인 사항: H2 PostgreSQL 호환 모드에서 V1 적용 후 Hibernate `validate` 성공
- 미확인 사항: 개발 PC에 Docker가 없어 실제 PostgreSQL 16 컨테이너 실행은 검증하지 못함

### 2026-10-02 조직 접근 권한과 설정

- 대상: 조직 상세, Owner 수정, 비멤버 데이터 격리, Member 수정 차단, 조직 설정 화면
- 백엔드 결과: 총 18개 통과, 실패 0, 오류 0, 건너뜀 0
- 프런트엔드 결과: `/app/settings` 포함 production build 및 TypeScript 검사 통과
- 권한 결과: 비멤버 `404`, 권한 부족 `403`, Owner 수정 `200`

### 2026-10-01 초기 기능 테스트 구축

- 대상: 헬스체크, 인증, 조직 생성·목록, 현재 프런트엔드
- 자동화: 기능 테스트 12개와 애플리케이션 기동 테스트 1개
- 데이터 처리: H2 테스트 트랜잭션 롤백
- 백엔드 결과: 총 13개 통과, 실패 0, 오류 0, 건너뜀 0
- 발견 및 수정: 미인증 요청이 기본 보안 응답을 사용하던 문제를 `401`과 `AUTH_UNAUTHORIZED` 표준 JSON으로 통일
- 프런트엔드 결과: production build 및 TypeScript 검사 통과
- 수동 브라우저 결과: 다음 실행에서 `FT-E2E-001` 전체 흐름을 검증할 예정
