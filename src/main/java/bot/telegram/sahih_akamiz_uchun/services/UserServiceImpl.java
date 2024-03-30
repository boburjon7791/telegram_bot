package bot.telegram.sahih_akamiz_uchun.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import bot.telegram.sahih_akamiz_uchun.entities.User;
import bot.telegram.sahih_akamiz_uchun.exceptions.BadRequestException;
import bot.telegram.sahih_akamiz_uchun.exceptions.NotFoundException;
import bot.telegram.sahih_akamiz_uchun.repositories.UserDao;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserDao userDao;
    @Override
    public void save(User user) {
        if (userDao.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new BadRequestException("Ushbu telefon raqam avvaldan mavjud\n");
        }
        saveOrUpdate(user);
    }

    @Override
    public User get(Long id) {
        return userDao.findById(id)
                      .orElseThrow(()->new NotFoundException("Foydalanuvchi topilmadi"));
    }

    @Override
    public void update(User user) {
        if(userDao.existsByPhoneNumberAndIdNotMatch(user.getPhoneNumber(),user.getId())){
            throw new BadRequestException("Ushbu telefon raqam avvaldan mavjud");
        }
        saveOrUpdate(user);
    }
    @Async
    public void saveOrUpdate(User user){
        userDao.save(user);
    }


    @Override
    @Async
    public void delete(Long id) {
        userDao.deleteById(id);
    }

    @Override
    public List<User> users() {
        List<User> users= userDao.findAll(Sort.by(Direction.ASC,"id","firstName"));
        if (users.isEmpty()) {
            throw new NotFoundException("Mijozlar topilmadi");
        }
         return users;
    }

    @Override
    public List<User> usersByNameOrPhoneNumber(String value) {
        List<User> users= userDao.findAllByFirstNameOrPhoneNumberContains(value,Sort.by(Direction.ASC,"id","firstName"));
        if (users.isEmpty()) {
            throw new NotFoundException("Mijozlar topilmadi");
        }
        return users;
    }

    @Override
    @Async
    public void deleteByPhoneNumber(String phoneNumber) {
        userDao.deleteByPhoneNumber(phoneNumber);
    }
    
}
