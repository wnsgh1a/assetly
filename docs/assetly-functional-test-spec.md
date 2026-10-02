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

사용 중 삭제 차단은 자산 테이블과 외래 키가 추가되는 다음 단계에서 활성화한다.

## 7. 현재 프런트엔드 수동 시나리오

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

- `/app`은 첫 번째 소속 조직명을 표시한다.
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

## 8. 핵심 사용자 흐름

### FT-E2E-001 신규 사용자 온보딩

1. 회원가입한다.
2. 로그인한다.
3. 조직이 없어 조직 생성 화면으로 이동한다.
4. 조직을 생성한다.
5. 앱 대시보드에서 조직명과 빈 자산 현황을 확인한다.
6. 로그아웃한다.
7. 보호된 앱 화면에 다시 접근하면 로그인으로 이동한다.

현재 상태: 브라우저 수동 실행 대상. API 구간은 자동화됨.

## 9. 향후 구현과 함께 활성화할 케이스

### 자산

- `FT-ASSET-001`: 필수값으로 자산을 등록하고 publicCode를 발급한다.
- `FT-ASSET-002`: 자산번호는 조직 안에서 중복될 수 없다.
- `FT-ASSET-003`: 검색·상태·카테고리·위치 필터와 페이지 정보를 검증한다.
- `FT-ASSET-004`: 조직 멤버가 상세 정보를 조회한다.
- `FT-ASSET-005`: Admin 이상이 기본 정보를 수정한다.
- `FT-ASSET-006`: Manager는 허용된 상태·위치·담당자만 수정한다.
- `FT-ASSET-007`: Member의 수정과 삭제를 차단한다.
- `FT-ASSET-008`: 삭제한 자산은 일반 목록에서 제외하고 이력을 보존한다.
- `FT-ASSET-009`: URL의 조직과 자산의 실제 조직이 다르면 접근을 차단한다.

### QR과 변경 이력

- `FT-QR-001`: publicCode로 자산을 조회한다.
- `FT-QR-002`: 비로그인 사용자를 로그인으로 보내고 원래 URL을 보존한다.
- `FT-QR-003`: 비멤버에게 자산 정보가 노출되지 않는다.
- `FT-HISTORY-001`: 등록·수정·삭제마다 작업자와 변경 내용이 기록된다.
- `FT-HISTORY-002`: 이력은 최신순이며 조직 밖에서 조회할 수 없다.

### 대시보드

- `FT-DASH-001`: 실제 자산 상태별 합계와 전체 합계가 일치한다.
- `FT-DASH-002`: 최근 자산과 최근 이력이 실제 데이터 기준으로 표시된다.
- `FT-DASH-003`: 신규 조직은 모두 0과 빈 목록을 반환한다.

## 10. 실행 명령

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

## 11. 통과 기준

- 현재 구현 기능의 자동 테스트가 전부 통과한다.
- 프런트엔드 production build가 경고성 오류 없이 완료된다.
- 인증이 필요한 API는 토큰 미제공과 변조 토큰을 모두 차단한다.
- 한 사용자의 조직 데이터가 다른 사용자에게 노출되지 않는다.
- 테스트 실행 후 개발 DB에 테스트 데이터가 남지 않는다.

## 12. 실행 기록

실행 기록은 날짜, 대상 버전, 자동 테스트 결과, 수동 테스트 결과, 발견 결함 순서로 갱신한다. 최신 실행 결과가 위에 오도록 기록한다.

### 2026-10-02 위치·카테고리 관리

- 대상: 위치·카테고리 CRUD, 이름 중복과 입력 검증, 역할 권한, 조직 간 리소스 격리
- 백엔드 결과: 총 38개 통과, 실패 0, 오류 0, 건너뜀 0
- DB 결과: Flyway `V2` 적용 후 Hibernate 스키마 검증 성공
- 프런트엔드 결과: `/app/classification` 포함 production build 및 TypeScript 검사 통과
- 후속 항목: 자산 테이블 연결 시 사용 중 위치·카테고리 삭제 차단 테스트 추가

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
