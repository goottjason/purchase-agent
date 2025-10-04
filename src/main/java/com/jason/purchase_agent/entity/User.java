package com.jason.purchase_agent.entity;

import com.jason.purchase_agent.enums.LoginType;
import com.jason.purchase_agent.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 저장하는 JPA 엔티티 클래스
 *
 * 이 시스템의 설계 원칙:
 * 1. 하나의 이메일 주소는 하나의 계정에만 사용 가능 (unique 제약조건)
 * 2. 카카오 로그인과 자체 회원가입은 배타적 관계 (동일 이메일로 둘 다 불가)
 * 3. 모든 사용자는 관리자 승인 후에만 서비스 이용 가능
 * 4. loginType 필드로 로그인 방식을 구분하여 인증 처리 분기
 */

/**
 * 주요 설계 특징
 * 1. 이메일 기반 단일 계정 원칙
 * - @Column(unique = true)로 이메일 중복 완전 차단
 * - 한 이메일로는 카카오 로그인 OR 자체 회원가입 중 하나만 선택
 * 2. LoginType으로 인증 방식 명확 구분
 * - KAKAO: OAuth2 플로우, password 불필요
 * - LOCAL: 폼 로그인, password 필수
 * 3. 관리자 승인 시스템
 * - 모든 사용자는 PENDING 상태로 시작
 * - 관리자 승인 후 USER 권한 부여
 * - 승인 이력 완전 추적 가능
 *
 */

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User {

    /*
     * =================== 기본 식별자 ===================
     */

    /**
     * 사용자 고유 식별자 (Primary Key)
     * - 데이터베이스에서 각 사용자를 구분하는 유일한 번호
     * - AUTO_INCREMENT로 자동 증가 (1, 2, 3, 4...)
     * - Spring Security 내부에서도 사용자 구분에 활용
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * =================== 공통 사용자 정보 ===================
     * 카카오 로그인과 자체 회원가입 모두에서 사용하는 공통 필드들
     */

    /**
     * 사용자 이메일 주소 (시스템 전체에서 유일)
     * - 로그인 시 사용자 식별의 핵심 키
     * - unique = true: 동일한 이메일로 중복 가입 절대 불가
     * - 카카오 로그인 시: 카카오 계정의 이메일 자동 저장
     * - 자체 회원가입 시: 사용자가 입력한 이메일 저장
     *
     * 중요한 비즈니스 규칙:
     * - abc@gmail.com으로 카카오 로그인했으면 → 같은 이메일로 자체 회원가입 불가
     * - abc@gmail.com으로 자체 회원가입했으면 → 같은 이메일로 카카오 로그인 불가
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * 사용자 이름/닉네임
     * - 카카오 로그인 시: 카카오 계정의 닉네임 자동 저장
     * - 자체 회원가입 시: 사용자가 입력한 이름 저장
     * - 화면에 "안녕하세요, {name}님" 형태로 표시용
     */
    @Column(nullable = false)
    private String name;

    /**
     * 사용자 프로필 이미지 URL (선택사항)
     * - 카카오 로그인 시: 카카오 프로필 사진 URL 저장 (있는 경우)
     * - 자체 회원가입 시: null (향후 프로필 이미지 업로드 기능 추가 시 사용)
     * - null 가능: 프로필 이미지가 없어도 시스템 이용에 문제없음
     */
    private String picture;

    /*
     * =================== 로그인 방식 구분 ===================
     */

    /**
     * 로그인 방식 구분자 (중요!)
     * - 이 사용자가 어떤 방식으로 가입했는지 구분하는 핵심 필드
     * - Spring Security 인증 처리 시 이 값에 따라 다른 로직 수행
     * - KAKAO: 카카오 OAuth2로 가입한 사용자
     * - LOCAL: 자체 회원가입으로 가입한 사용자
     *
     * 활용 예시:
     * - 로그인 시 loginType이 KAKAO면 → OAuth2 인증 플로우
     * - 로그인 시 loginType이 LOCAL면 → 폼 로그인 + 패스워드 검증
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    /**
     * 암호화된 비밀번호
     * - 자체 회원가입 사용자만 사용 (loginType = LOCAL)
     * - 카카오 로그인 사용자는 null (카카오에서 인증 처리하므로 불필요)
     * - BCrypt 알고리즘으로 암호화하여 저장
     * - 평문 비밀번호는 절대 저장하지 않음 (보안상 매우 중요)
     *
     * 중요 사항:
     * - loginType이 KAKAO인 사용자는 이 필드가 null
     * - loginType이 LOCAL인 사용자는 이 필드가 반드시 존재해야 함
     */
    private String password;

    /*
     * =================== 카카오 로그인 전용 필드 ===================
     * loginType이 KAKAO인 사용자만 사용하는 필드들
     */

    /**
     * 카카오에서 제공하는 사용자 고유 ID
     * - 카카오 로그인 사용자만 값 존재 (loginType = KAKAO)
     * - 자체 회원가입 사용자는 null (loginType = LOCAL)
     * - 카카오 OAuth2 인증 시 사용자 매칭에 활용
     * - 이메일은 변경될 수 있지만 이 ID는 절대 변경되지 않는 영구 식별자
     *
     * 예시: 카카오 사용자 ID가 "1234567890"인 사용자가
     *       이메일을 변경해도 이 ID로 동일 사용자임을 확인 가능
     */
    private String kakaoId;

    /*
     * =================== 권한 및 승인 관리 ===================
     * 관리자 승인 시스템을 위한 필드들
     */

    /**
     * 사용자 권한 등급
     * - USER: 관리자가 승인한 일반 사용자
     * - ADMIN: 시스템 관리자 (모든 권한)
     *
     * Spring Security 연동:
     * - USER → ROLE_USER (일반 서비스 이용 가능)
     * - ADMIN → ROLE_ADMIN (관리자 기능 + 일반 서비스 모두 이용 가능)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    /**
     * 관리자 승인 완료 여부
     * - true: 관리자가 승인 완료
     * - role과 함께 이중 체크하여 더 안전한 권한 관리
     */
    @Column(nullable = false)
    private boolean approved = true;

    /**
     * 승인 완료된 날짜와 시간
     * - 관리자가 승인 버튼을 클릭한 정확한 시점 기록
     * - null: 아직 승인되지 않음
     * - 값 존재: 승인 완료됨
     * - 통계 분석, 감사 로그, 사용자 문의 처리 등에 활용
     */
    private LocalDateTime approvedAt;

    /**
     * 승인을 처리한 관리자의 식별 정보
     * - 어떤 관리자가 이 사용자를 승인했는지 기록
     * - 관리자의 username 또는 email 저장
     * - 보안 감사, 책임 추적 목적
     * - 문제 발생 시 "누가 언제 승인했는지" 추적 가능
     */
    private String approvedBy;

    /*
     * =================== 생성자 및 편의 메서드 ===================
     */

    /**
     * 카카오 로그인 사용자 생성을 위한 정적 팩토리 메서드
     * - OAuth2 로그인 성공 시 호출됨
     * - 카카오에서 제공받은 사용자 정보로 User 엔티티 생성
     *
     * @param email 카카오 계정 이메일
     * @param name 카카오 닉네임
     * @param picture 카카오 프로필 이미지 URL
     * @param kakaoId 카카오 사용자 고유 ID
     * @return 생성된 User 엔티티 (승인 대기 상태)
     */
    public static User createKakaoUser(String email, String name, String picture, String kakaoId) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.picture = picture;
        user.kakaoId = kakaoId;
        user.loginType = LoginType.KAKAO;       // 카카오 로그인으로 설정
        user.password = null;                   // 카카오 사용자는 비밀번호 불필요
        user.role = UserRole.USER;           // 승인 대기 상태
        user.approved = false;                  // 승인되지 않은 상태
        return user;
    }

    /**
     * 자체 회원가입 사용자 생성을 위한 정적 팩토리 메서드
     * - 회원가입 폼에서 정보 입력 후 호출됨
     * - 입력받은 정보로 User 엔티티 생성
     *
     * @param email 사용자 입력 이메일
     * @param name 사용자 입력 이름
     * @param encodedPassword BCrypt로 암호화된 비밀번호
     * @return 생성된 User 엔티티 (승인 대기 상태)
     */
    public static User createLocalUser(String email, String name, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.name = name;
        user.password = encodedPassword;        // 암호화된 비밀번호 저장
        user.loginType = LoginType.LOCAL;       // 자체 회원가입으로 설정
        user.kakaoId = null;                    // 자체 회원가입은 카카오 ID 불필요
        user.picture = null;                    // 기본적으로 프로필 이미지 없음
        user.role = UserRole.USER;              // 가입후 바로 유저
        user.approved = true;                   // 추후 승인 도입시 필요
        return user;
    }

    /**
     * 관리자가 사용자를 승인 처리할 때 호출하는 메서드
     * - 승인 관련 모든 필드를 한 번에 업데이트
     * - 데이터 일관성 보장을 위해 메서드로 캡슐화
     *
     * @param adminUsername 승인을 처리한 관리자 username
     */
    public void approveUser(String adminUsername) {
        this.role = UserRole.USER;              // 일반 사용자 권한 부여
        this.approved = true;                   // 승인 완료 표시
        this.approvedAt = LocalDateTime.now();  // 현재 시간으로 승인 시간 설정
        this.approvedBy = adminUsername;        // 승인한 관리자 기록
    }

    /**
     * 카카오 로그인 사용자인지 확인하는 편의 메서드
     * - 조건문에서 가독성 향상을 위해 제공
     * - loginType 체크를 메서드로 감싸서 실수 방지
     *
     * @return 카카오 로그인 사용자면 true, 아니면 false
     */
    public boolean isKakaoUser() {
        return LoginType.KAKAO.equals(this.loginType);
    }

    /**
     * 자체 회원가입 사용자인지 확인하는 편의 메서드
     * - 비밀번호 체크 등이 필요한 로직에서 활용
     *
     * @return 자체 회원가입 사용자면 true, 아니면 false
     */
    public boolean isLocalUser() {
        return LoginType.LOCAL.equals(this.loginType);
    }
}
