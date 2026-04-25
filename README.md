# E-Commerce Stock Service

## 프로젝트 소개

**ecom-stock**는 MSA 기반 이커머스 시스템의 재고 관리 서비스입니다.
Spring WebFlux를 활용한 리액티브 프로그래밍과 마이크로서비스 아키텍처를 학습하기 위해 개발되었습니다.

---

## 운영 URL

* 백엔드 URL : https://ecom-stock-api.parkging.com
* API 문서 URL : https://ecom-stock-api.parkging.com/apispec.html

---
## 주요 기능

### 재고 관리
- 상품별 재고 조회
- 재고 등록
- 재고 추가
- 재고 차감
- 재고 취소

### 재고 이력 관리
- 재고 변경 이력 기록
- 변경 유형별 추적 (등록/추가/차감/취소)
- 주문 ID 연동 추적

### 인증/인가
- JWT 기반 토큰 인증
- Role 기반 접근 제어
- 상품 등록자만 재고 등록/추가 가능

---  

## 기술 스택

### Backend
- **Java 21**
- **Spring Boot 3.5.11**
- **Spring WebFlux**
- **Spring Data R2DBC**
- **R2DBC MySQL**

### Database
- **H2** : 로컬 개발 (인메모리)
- **MySQL** : 운영 환경

### Security
- **JWT (java-jwt 4.4.0)** : 토큰 기반 인증
- **Jasypt** : 설정값 암호화
- **jBCrypt** : 비밀번호 해싱

### Infrastructure
- **Flyway**
- **Gradle**
- **Docker**
- **K3s**

### Documentation & Monitoring
- **SpringDoc OpenAPI**
- **Spring REST Docs**
- **Spring Boot Actuator**
- **Prometheus**

### Testing
- **JUnit 5**
- **Reactor Test**
- **REST Assured**
- **DataFaker**

---  

## ERD

```mermaid
erDiagram
    STOCK ||--o{ STOCK_HISTORY : "has"
    STOCK {
        bigint id PK
        bigint product_id UK
        bigint quantity
    }
    STOCK_HISTORY {
        bigint id PK
        bigint stock_id FK
        bigint order_id
        varchar type
        bigint change_quantity
        bigint total_quantity
        datetime created_at
        datetime updated_at
    }
```
---  

## 프로젝트 실행

### 요구사항
- Java 21 이상

### 로컬 실행

1. **common 저장소 클론**
```bash  
git clone https://github.com/NG-Archive/ecom-common.git
```

2. **stock 저장소 클론**
```bash  
git clone https://github.com/NG-Archive/ecom-stock.git
```

3. **애플리케이션 빌드**
```bash  
./ecom-stock/gradlew build
```  

4. **애플리케이션 실행**
```bash  
./ecom-stock/gradlew bootRun
```  

### 테스트 실행

1. 테스트 실행
```bash  
# 전체 테스트 실행  
./ecom-stock/gradlew test  
  
# 테스트 및 API 문서 생성  
./ecom-stock/gradlew test openapi3  
```  

2. **API 문서 확인**
- OpenAPI Spec: http://localhost:8080/apispec.yaml  