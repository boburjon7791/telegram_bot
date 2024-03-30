package bot.telegram.sahih_akamiz_uchun.services;

import java.util.List;

import org.springframework.stereotype.Service;

import bot.telegram.sahih_akamiz_uchun.entities.User;

@Service
public interface UserService {
    void save(User user);
    User get(Long id);
    void update(User user);
    void delete(Long id);
    void deleteByPhoneNumber(String phoneNumber);
    List<User> users();
    List<User> usersByNameOrPhoneNumber(String value);
}
