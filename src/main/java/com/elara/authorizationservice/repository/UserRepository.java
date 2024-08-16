package com.elara.authorizationservice.repository;

import com.elara.authorizationservice.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  @Query("select u from User u where u.companyCode = :companyCode and (u.email = :username or u.phone = :username)")
  User findByCompanyCodeAndEmailOrPhone(@Param("companyCode") String companyCode, @Param("username") String username);

  @Query("select u from User u where u.companyCode = :companyCode and (u.email = :email or u.phone = :phone)")
  User findByCompanyCodeAndEmailOrPhone(@Param("companyCode") String companyCode, @Param("email") String email, @Param("phone") String phone);

  User findByEmail(String email);

  User findByPhone(String phone);

  @Query("select u from User u where u.email = :username or u.phone = :username")
  User findByUsername(String username);

  @Query("select mp from User mp where mp.companyCode = :companyCode and mp.userType != :userType")
  List<User> findByCompanyCodeAndUserType(@Param("companyCode") String companyCode, @Param("userType") String userType);

  @Query("select mp from User mp where mp.companyCode = :companyCode and userType != :userType and mp.status = :status")
  List<User> findEnabledUser(@Param("companyCode") String companyCode, @Param("userType") String userType, @Param("status") String status);


  @Query("select mp " +
          "from User mp where " +
          "(:companyCode IS NULL OR mp.companyCode = :companyCode) " +
          "AND (:email IS NULL OR mp.email = :email) " +
          "AND (:phone IS NULL OR mp.phone = :phone) " +
          "AND (:status IS NULL OR mp.status = :status) " +
          "AND (:staffName IS NULL OR mp.staffName LIKE %:staffName%) " +
          "AND (:searchTerm IS NULL OR (email like concat('%', :searchTerm, '%') or phone like concat('%', :searchTerm, '%') or staffName like concat('%', :searchTerm, '%'))) " +
          "AND (:startDate IS NULL OR :endDate IS NULL OR mp.createdAt BETWEEN :startDate AND :endDate) " +
          "AND (userType != :userType)")
  Page<User> searchUser(@Param("companyCode") String companyCode,
                                 @Param("email") String email,
                                 @Param("phone") String phone,
                                 @Param("staffName") String staffName,
                                 @Param("searchTerm") String searchTerm,
                                 @Param("userType") String userType,
                                 @Param("status") String status,
                                 @Param("startDate") String startDate,
                                 @Param("endDate") String endDate,
                                 Pageable pageable);


  @Query("select mp " +
          "from User mp where " +
          "(mp.companyCode = :companyCode) " +
          "AND (mp.status = :status) " +
          "AND (userType = :userType)")
    List<User> findByCompanyCodeAndStatus(@Param("companyCode") String companyCode,
                                          @Param("status") String status, @Param("userType") String userType);

  @Query("select mp " +
          "from User mp where " +
          "(mp.companyCode = :companyCode) " +
          "AND (mp.status = :status) " +
          "AND (userType != :userType)")
  List<User> findByStaffCount(@Param("companyCode") String companyCode,
                                        @Param("status") String status, @Param("userType") String userType);
}
