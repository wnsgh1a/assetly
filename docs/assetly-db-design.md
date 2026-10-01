# Assetly DB 설계

이 문서는 Assetly v1.0의 데이터 모델을 정리한다.

Assetly는 여러 조직이 하나의 서비스를 함께 사용하는 SaaS 구조다. 따라서 대부분의 주요 테이블은 `organization_id`를 통해 조직 단위로 데이터를 분리한다.

## 1. 설계 원칙

### 멀티테넌트 구조

Assetly의 데이터는 조직 단위로 분리한다.

한 사용자는 여러 조직에 속할 수 있고, 조직마다 다른 역할을 가질 수 있다.

### 변경 이력 중심

자산의 현재 상태만 저장하지 않는다.

중요한 변경은 `asset_histories`에 기록해서 누가, 언제, 무엇을 바꿨는지 추적할 수 있게 한다.

### 삭제 정책

v1.0에서는 핵심 데이터의 물리 삭제를 최소화한다.

자산은 실제 삭제보다 `deleted_at` 또는 상태값으로 비활성화하는 방향을 우선 검토한다. 다만 MVP 구현 단순화를 위해 초기에는 삭제 API를 제공하되, 내부적으로는 soft delete를 권장한다.

## 2. 주요 엔티티

### users

서비스 사용자 계정이다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| email | varchar | 로그인 이메일 |
| password | varchar | 암호화된 비밀번호 |
| name | varchar | 사용자 이름 |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

제약:

- `email`은 전체 서비스에서 유니크하다.

### organizations

사용자가 생성하는 조직이다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| name | varchar | 조직명 |
| description | text | 조직 설명 |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

### organization_members

사용자와 조직의 관계를 저장한다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| organization_id | bigint | 조직 ID |
| user_id | bigint | 사용자 ID |
| role | varchar | OWNER, ADMIN, MANAGER, MEMBER |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

제약:

- `(organization_id, user_id)`는 유니크하다.

### asset_categories

조직별 자산 카테고리다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| organization_id | bigint | 조직 ID |
| name | varchar | 카테고리명 |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

제약:

- `(organization_id, name)`은 유니크하다.

### locations

조직별 자산 위치다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| organization_id | bigint | 조직 ID |
| name | varchar | 위치명 |
| description | text | 위치 설명 |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

제약:

- `(organization_id, name)`은 유니크하다.

### assets

자산의 현재 상태를 저장한다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| organization_id | bigint | 조직 ID |
| category_id | bigint | 카테고리 ID |
| location_id | bigint | 위치 ID |
| assigned_user_id | bigint | 담당 사용자 ID |
| asset_code | varchar | 자산번호 |
| name | varchar | 자산명 |
| description | text | 설명 |
| status | varchar | AVAILABLE, IN_USE, REPAIR, LOST, DISPOSED |
| purchase_date | date | 구매일 |
| purchase_price | numeric | 구매 가격 |
| deleted_at | timestamp | 삭제 처리일 |
| created_at | timestamp | 생성일 |
| updated_at | timestamp | 수정일 |

제약:

- `(organization_id, asset_code)`는 유니크하다.

### asset_histories

자산 변경 이력을 저장한다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| organization_id | bigint | 조직 ID |
| asset_id | bigint | 자산 ID |
| actor_id | bigint | 작업자 ID |
| action_type | varchar | 작업 종류 |
| field_name | varchar | 변경 필드 |
| before_value | text | 변경 전 값 |
| after_value | text | 변경 후 값 |
| memo | text | 메모 |
| created_at | timestamp | 생성일 |

작업 종류 예시:

- CREATED
- UPDATED
- STATUS_CHANGED
- LOCATION_CHANGED
- ASSIGNEE_CHANGED
- DELETED
- RESTORED

### refresh_tokens

JWT refresh token을 저장한다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| id | bigint | PK |
| user_id | bigint | 사용자 ID |
| token | varchar | refresh token |
| expires_at | timestamp | 만료일 |
| revoked_at | timestamp | 폐기일 |
| created_at | timestamp | 생성일 |

v1.0에서 refresh token을 단순화하려면 이 테이블은 후순위로 미룰 수 있다.

## 3. ERD 개념

```text
users
  └─ organization_members ─ organizations
                              ├─ asset_categories
                              ├─ locations
                              ├─ assets
                              │    ├─ asset_histories
                              │    └─ assigned_user_id -> users
                              └─ asset_histories
```

## 4. 관계 설명

### User와 Organization

User와 Organization은 다대다 관계다.

중간 테이블인 `organization_members`를 통해 역할을 가진다.

### Organization과 Asset

Organization은 여러 Asset을 가진다.

모든 Asset은 반드시 하나의 Organization에 속한다.

### Asset과 Category

Asset은 하나의 Category를 가질 수 있다.

Category는 조직별로 관리한다.

### Asset과 Location

Asset은 하나의 Location을 가질 수 있다.

Location은 조직별로 관리한다.

### Asset과 History

Asset은 여러 History를 가진다.

자산이 수정될 때마다 필요한 경우 History가 생성된다.

## 5. enum 후보

### MemberRole

```text
OWNER
ADMIN
MANAGER
MEMBER
```

### AssetStatus

```text
AVAILABLE
IN_USE
REPAIR
LOST
DISPOSED
```

### AssetHistoryActionType

```text
CREATED
UPDATED
STATUS_CHANGED
LOCATION_CHANGED
ASSIGNEE_CHANGED
DELETED
RESTORED
```

## 6. 인덱스 후보

### users

- `email`

### organization_members

- `organization_id`
- `user_id`
- `(organization_id, user_id)`

### assets

- `organization_id`
- `(organization_id, asset_code)`
- `(organization_id, name)`
- `(organization_id, status)`
- `(organization_id, category_id)`
- `(organization_id, location_id)`

### asset_histories

- `organization_id`
- `asset_id`
- `(organization_id, created_at)`

## 7. QR 코드 설계 결정

QR URL은 다음 형태를 우선 사용한다.

```text
/a/{assetCode}
```

다만 `asset_code`가 조직 내에서만 유니크하면 URL만으로 자산을 특정하기 어렵다.

따라서 v1.0에서는 다음 중 하나를 선택해야 한다.

### 선택지 A. assetCode를 전체 서비스 유니크로 만든다

예시:

```text
AST-9X7K2Q
```

장점:

- QR URL이 단순하다.
- `/a/{assetCode}`로 바로 찾을 수 있다.

단점:

- 조직별 자산번호와 별도 공개 코드가 필요할 수 있다.

### 선택지 B. QR 전용 publicCode를 둔다

`assets`에 `public_code` 컬럼을 추가한다.

예시:

```text
/a/q8K2md91
```

장점:

- 조직 내부 자산번호와 외부 접근 코드를 분리할 수 있다.
- 보안과 URL 안정성이 좋다.

단점:

- 컬럼이 하나 더 필요하다.

권장 선택은 B다.

따라서 실제 구현 시 `assets.public_code`를 추가하는 것을 추천한다.

## 8. 수정된 assets 권장 컬럼

QR 설계를 반영하면 `assets`는 다음 컬럼을 추가한다.

| 컬럼 | 타입 | 설명 |
| --- | --- | --- |
| public_code | varchar | QR 접근용 공개 코드 |

제약:

- `public_code`는 전체 서비스에서 유니크하다.

## 9. v1.0 최종 테이블 목록

v1.0에서 우선 구현할 테이블은 다음과 같다.

- users
- organizations
- organization_members
- asset_categories
- locations
- assets
- asset_histories

추후 추가 후보:

- asset_attachments
- inspections
- rentals
- refresh_tokens
- invitations
- notifications
