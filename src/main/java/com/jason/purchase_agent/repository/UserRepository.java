package com.jason.purchase_agent.repository;

import com.jason.purchase_agent.entity.User;
import com.jason.purchase_agent.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 정보에 대한 데이터베이스 접근을 담당하는 Repository
 * - JPA 기본 CRUD 메서드 자동 제공
 * - 커스텀 쿼리 메서드 정의
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자 조회 (로그인 시 사용)
     * - 카카오 로그인과 자체 로그인 모두에서 사용
     * - 이메일은 unique 제약조건으로 중복 불가능
     *
     * @param email 조회할 이메일 주소
     * @return Optional<User> 사용자가 존재하면 User 객체, 없으면 empty
     */
    Optional<User> findByEmail(String email);

    /**
     * 카카오 ID로 사용자 조회 (카카오 OAuth2 전용)
     * - 카카오 로그인 시 기존 사용자 확인용
     * - 이메일 변경되어도 kakaoId로 동일 사용자 식별 가능
     *
     * @param kakaoId 카카오에서 제공하는 사용자 고유 ID
     * @return Optional<User> 해당 카카오 ID 사용자가 존재하면 User, 없으면 empty
     */
    Optional<User> findByKakaoId(String kakaoId);

    /**
     * 승인 대기 상태인 사용자 목록 조회 (관리자 페이지용)
     * - 관리자가 승인 처리할 사용자들을 조회
     * - 가입일 순으로 정렬하여 먼저 가입한 사용자부터 처리
     *
     * @param role 조회할 권한 (PENDING)
     * @return List<User> 승인 대기 중인 사용자 목록
     */
    List<User> findByRoleOrderByIdAsc(UserRole role);

    /**
     * 이메일 존재 여부 확인 (중복 가입 방지용)
     * - 회원가입 시 이메일 중복 체크
     * - 데이터베이스 레벨 unique 제약조건과 이중 체크
     *
     * @param email 확인할 이메일 주소
     * @return boolean 이메일이 이미 존재하면 true, 없으면 false
     */
    boolean existsByEmail(String email);
}