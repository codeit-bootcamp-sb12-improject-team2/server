# Monew

> 관심 있는 뉴스를 모으고,  
> **개인화된 뉴스와 커뮤니티를 제공하는 뉴스 플랫폼**

<br>

## 👨‍👩‍👧‍👦 팀원 구성 및 구현 기능

| 이름 | 구현 기능 |
| --- | --- |
| **[박교현](https://github.com/hyeon2628)** | 댓글 등록/수정/삭제, 댓글 좋아요, 댓글 필터링, 랭킹, 프론트엔드 |
| **[김태성](https://github.com/ts8191)** | 뉴스 기사 수집, 뉴스 기사 조회, S3 백업, 기사 복구 |
| **[나상준](https://github.com/Nasangjoon)** | 활동 내역 조회, AI 기사 요약, 알림 관리, 랭킹, 로깅, AWS 배포 |
| **[석지예](https://github.com/zziyo8)** | 회원가입, 로그인, 회원 정보 수정, 랭킹 |
| **[류승지](https://github.com/lyoonat)** | 관심사 등록/수정/삭제, 관심사 구독, 키워드 관리, 프론트엔드 |
<br>

## 📌 프로젝트 소개

**MONEW**는 다양한 언론사의 뉴스를 하나의 플랫폼에서 검색하고 관리할 수 있는
**다중 언론사 통합 뉴스 검색 플랫폼**입니다.

사용자는 관심사를 등록하여 관련 뉴스를 손쉽게 모아볼 수 있으며,
AI 기능 요약 기능을 통해 긴 기사의 핵심 내용을 빠르게 확인할 수 있습니다.

또한 조회수와 댓글 수 기반의 랭킹을 제공하여
실시간 주요 이슈와 인기 뉴스를 한눈에 확인할 수 있도록 지원합니다.

- 프로젝트 기간 : **2026.06.22 ~ 2026.07.13**
- 개발 인원 : **5명**
- 개발 방식 : **Git Flow 기반 협업**

<br>

---

# 🛠 기술 스택

## Backend

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-59666C?style=for-the-badge)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge)
![Spring Batch](https://img.shields.io/badge/Spring_Batch-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)

<br>

## Frontend

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)

<br>

## Database & Infra

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=for-the-badge&logo=amazonaws&logoColor=FF9900)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)

<br>

## Communication

![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Discord](https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Jira](https://img.shields.io/badge/Jira-0052CC?style=for-the-badge&logo=jira&logoColor=white)

<br>

---

# ✨ 주요 기능

## 📰 뉴스

- 뉴스 기사 자동 수집
- 기사 목록 및 상세 조회
- 조회수 관리
- 기사 백업 및 복구

---

## ⭐ 관심사

- 관심사 생성 및 관리
- 관심사 구독
- 관심사 기반 기사 조회
- 키워드 관리

---

## 💬 댓글

- 댓글 작성 및 수정
- 댓글 삭제
- 댓글 좋아요
- 댓글 조회

---

## 🔔 알림

- 관심사 기반 알림
- 읽음 처리
- 실시간 알림 관리

---

## 👤 사용자

- 회원가입 및 로그인
- 내 정보 관리
- 기사 조회 기록
- 관심사 관리

---

# 📂 프로젝트 구조

<details>
<summary>접기 / 펼치기</summary>

```bash
src
┗ main
 ┣ java
 ┃ ┗ com.codeit.server
 ┃   ┣ article
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ comment
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ interest
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ notification
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ subscription
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ user
 ┃   ┃ ┣ controller
 ┃   ┃ ┣ dto
 ┃   ┃ ┣ entity
 ┃   ┃ ┣ repository
 ┃   ┃ ┗ service
 ┃   ┣ global
 ┃   ┃ ┣ config
 ┃   ┃ ┣ exception
 ┃   ┃ ┣ security
 ┃   ┃ ┗ util
 ┃   ┗ batch
 ┃     ┣ config
 ┃     ┣ job
 ┃     ┣ scheduler
 ┃     ┗ tasklet
 ┗ resources
```

</details>

<br>

---

# 🌐 서비스

- [MONEW](https://monew.site)

<br>

---

# 📝 프로젝트 회고

추후 작성 예정