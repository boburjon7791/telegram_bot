package bot.telegram.sahih_akamiz_uchun.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import bot.telegram.sahih_akamiz_uchun.exceptions.BadRequestException;

import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import bot.telegram.sahih_akamiz_uchun.entities.Service;
import bot.telegram.sahih_akamiz_uchun.exceptions.NotFoundException;
import bot.telegram.sahih_akamiz_uchun.repositories.ServiceDao;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServiceBotImpl implements ServiceBot{
    private final ServiceDao serviceDao;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public static Path root=Path.of("folder");
    static{
        if (!root.toFile().exists()) {
            System.out.println(root.toFile().mkdirs());
        }
    }
    @Override
    public void save(Service service) {
        if (serviceDao.existsByName(service.getName())) {
            throw new BadRequestException("Ushbu servis avvaldan mavjud");
        }
        saveOrUpdate(service);
    }
    @Async
    public void saveOrUpdate(Service service){
        serviceDao.save(service);
    }

    @Override
    @Async
    public void update(Service service) {
        if(!serviceDao.existsById(service.getId())){
            throw new NotFoundException("Servis topilmadi");
        }
        if (serviceDao.existsByNameAndIdNotMatches(service.getName(), service.getId())) {
            throw new BadRequestException("Ushbu servis avvaldan mavjud");
        }
        Service service2=serviceDao.findById(service.getId())
                          .orElseThrow(()->new NotFoundException("Topilmadi"));
        service2.setName(service.getName()!=null ? service.getName() : service2.getName());
        service2.setCurrency(service.getCurrency() != null ? service.getCurrency() : service2.getCurrency());
        service2.setPrice(service.getPrice()!=null ? service.getPrice() : service2.getPrice());
        service2.setImageId(service.getImageId() != null ? service.getImageId() : service2.getImageId());
        service2.setImageChatId(service.getImageChatId() != null ? service.getImageChatId() : service2.getImageChatId());
        service2.setVideoId(service.getVideoId() != null ? service.getVideoId() : service2.getVideoId());
        service2.setVideoChatId(service.getVideoChatId() != null ? service.getVideoChatId() : service2.getVideoChatId());
        service2.setType(service.getType() != null ? service.getType() : service2.getType());
        saveOrUpdate(service2);
    }

    @Override
    public Service get(Long id) {
        return serviceDao.findById(id)
                         .orElseThrow(()->new NotFoundException("Servis topilmadi"));
    }

    @Override
    @Transactional
    @Async
    public void delete(Long id) {
        String sql= """
                select image, video
                from service
                where id=:id
                """;
        Map<String, Object> params=Map.of("id",id);
        RowMapper<List<String>> rowMapper=(rs, rowNum) -> List.of(rs.getString("image"),
                rs.getString("video"));
        List<String> fileNames = new ArrayList<>();
        try {
            fileNames=namedParameterJdbcTemplate.queryForObject(sql, params, rowMapper);
        }catch (Exception ignore){}
        serviceDao.deleteById(id);
        assert fileNames != null;
        fileNames.forEach(this::deleteFile);
    }

    @Override
    @Async
    public void deleteByName(String name) {
        serviceDao.deleteByName(name);
    }

    @Override
    public List<Service> services() {
        List<Service> services=serviceDao.findAll(Sort.by(Direction.ASC,"id","name"));
        if (services.isEmpty()) {
            throw new NotFoundException("Servislar topilmadi");
        }
        return services;
    }

    @Override
    public List<Service> servicesByName(String name) {
        List<Service> services= serviceDao.findAllByNameContains(name,Sort.by(Direction.ASC,"id","name"));
       if (services.isEmpty()) {
          throw new NotFoundException("Servislar topilmadi");
       }
       return services;
    }

    @Override
    @SneakyThrows
    public byte[] getFile(String name) {
        try {
            return Files.readAllBytes(Path.of(root+"/"+name));
        } catch (Exception e) {
            throw new NotFoundException("Fayl topilmadi");
        }
    }

    @Override
    public void deleteFile(String name) {
        try {
            Path path = Path.of(root + "/" + name);
            if (path.toFile().exists()) {
                Files.delete(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SneakyThrows
    public String saveFile(MultipartFile file) {
        if(file==null || file.isEmpty()){
            return "";
        }
        String generatedName=UUID.randomUUID().toString();
        String extension=FilenameUtils.getExtension(file.getOriginalFilename());
        String fullName=generatedName+"."+extension;
        Files.copy(file.getInputStream(), Path.of(root+"/"+fullName), StandardCopyOption.REPLACE_EXISTING);
        return fullName;
    }

    @Override
    public void saveFile(MultipartFile file, String fileType, Long id) {
        String fileName= saveFile(file);
        switch (fileType) {
            case "video"->{
                updateVideo(fileName,id);
            }
            case "image"->{
                updateImage(fileName,id);
            }
            default -> throw new RuntimeException();
        }
    }

    @Override
    @SneakyThrows
    public Resource getVideo(String name) {
        return new UrlResource(root.toFile().getAbsolutePath()+"/"+name);
    }

    @Override
    @Transactional
    public void updateVideo(String name, long id) {
        String sql1= """
                select video
                from service
                where id=:id
                """;
        String oldName="";
        try {
            oldName=namedParameterJdbcTemplate.queryForObject(sql1,Map.of("id",id), String.class);
            deleteFile(oldName);
        }catch (Exception e){
            e.printStackTrace();
        }
        String sql2="""
                update service
                set video=:name,
                    video_id=null
                where id=:id
                """;
        log.info(name+" "+id);
        Map<String, Object> params=Map.of("id",id,"name",name);
        namedParameterJdbcTemplate.update(sql2,params);
    }

    @Override
    @Transactional
    public void updateImage(String name, long id) {
        String sql1= """
                select image
                from service
                where id=:id
                """;
        String oldName="";
        try {
            oldName=namedParameterJdbcTemplate.queryForObject(sql1,Map.of("id",id),String.class);
            deleteFile(oldName);
        }catch (Exception e){
            e.printStackTrace();
        }
        String sql2="""
                update service
                set image=:name,
                    image_id=null
                where id=:id
                """;
        Map<String, Object> params=Map.of("id",id,"name",name);
        namedParameterJdbcTemplate.update(sql2, params);
    }

    @Override
    @Async
    public void deleteFileFromService(String name, long id) {
        String sql1="""
                update service
                set video='no'
                where id=:id and video=:name;
                """;
        
        Map<String, Object> params=Map.of("id",id,"name",name);
        if(namedParameterJdbcTemplate.update(sql1, params)==0){
            String sql2="""
                update service
                set image='no'
                where id=:id and image=:name;
                    """;
            namedParameterJdbcTemplate.update(sql2, params);
        }
        deleteFile(name);
    }

    @Override
    public File getFile2(String image) {
        try {
            return new File(root+"/"+image);
        } catch (Exception e) {
            throw new NotFoundException("Xatolik");
        }
    }
}
