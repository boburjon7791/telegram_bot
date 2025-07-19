package bot.telegram.sahih_akamiz_uchun.services;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import bot.telegram.sahih_akamiz_uchun.entities.Service;

@Component
public interface ServiceBot {
    Map<String, LocalDateTime> cachedImages=new ConcurrentHashMap<>();
    void save(Service service);
    byte[] getFile(String name);
    Resource getVideo(String name);
    void deleteFile(String name);
    String saveFile(MultipartFile file);
    void saveFile(MultipartFile file, String fileType, Long id);
    void update(Service service);
    Service get(Long id);
    void delete(Long id);
    void deleteServiceFile(Long id, String fileName);
    void deleteByName(String name);
    List<Service> services();
    List<Service> servicesByName(String name);
    void updateVideo(String name, long id);
    void updateImage(String name, long id);
    void deleteFileFromService(String name, long id);
    File getFile2(String image);
}
