package bot.telegram.sahih_akamiz_uchun.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import bot.telegram.sahih_akamiz_uchun.entities.User;
import jakarta.transaction.Transactional;

@Repository
public interface UserDao  extends JpaRepository<User, Long>{
    @Query(value = "from User u where upper(u.firstName) like upper(concat('%',?1,'%')) or u.phoneNumber like concat('%',?1,'%')")
    List<User> findAllByFirstNameOrPhoneNumberContains(String value, Sort sort);
    boolean existsByPhoneNumber(String phoneNumber);
    void deleteByPhoneNumber(String phoneNumber);
    
    @Modifying
    @Transactional
    @Query(value = "update User u set u.firstName=?1, u.phoneNumber=?2 where u.id=?3")
    int updateUser(String firstName, String phoneNumber, Long id);

    @Query(value="select exists(from User u where u.phoneNumber=?1 and u.id<>?2)")
    boolean existsByPhoneNumberAndIdNotMatch(String phoneNumber, Long id);
}
