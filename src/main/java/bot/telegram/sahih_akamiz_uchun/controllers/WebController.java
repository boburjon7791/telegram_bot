package bot.telegram.sahih_akamiz_uchun.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import bot.telegram.sahih_akamiz_uchun.entities.History;
import bot.telegram.sahih_akamiz_uchun.exceptions.BadRequestException;
import bot.telegram.sahih_akamiz_uchun.services.HistoryService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import bot.telegram.sahih_akamiz_uchun.entities.Service;
import bot.telegram.sahih_akamiz_uchun.entities.User;
import bot.telegram.sahih_akamiz_uchun.entities.Utils;
import bot.telegram.sahih_akamiz_uchun.repositories.UtilsDao;
import bot.telegram.sahih_akamiz_uchun.services.ServiceBot;
import bot.telegram.sahih_akamiz_uchun.services.ServiceBotImpl;
import bot.telegram.sahih_akamiz_uchun.services.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final UserService userService;
    private final ServiceBot serviceBot;
    private final UtilsDao utilsDao;
    private final HistoryService historyService;
    @GetMapping("/users")
    public String users(Model model){
        model.addAttribute("users",userService.users());
        return "users";
    }
    @GetMapping("/user/create")
    public String userCreate(){
        return "user_create";
    }
    @PostMapping("/user/update")
    public void userUpdate(@Valid @ModelAttribute User user,HttpServletResponse response)throws Exception{
        userService.update(user);
        response.sendRedirect("/users");
    }
    @PostMapping("/user/delete")
    public void userDelete(@RequestParam("id")long id, HttpServletResponse response)throws Exception{
        userService.delete(id);
        response.sendRedirect("/users");
    }
    @PostMapping("/user/create")
    public void userCreate(@Valid @ModelAttribute User user, HttpServletResponse response)throws Exception{
        userService.save(user);
        response.sendRedirect("/users");
    }
    @PostMapping("/delete/user/by-phone")
    public String removeByPhone(@RequestParam("phone") String phone, Model model){
        userService.deleteByPhoneNumber(phone);
        model.addAttribute("users",userService.users());
        return "users";
    }
    @GetMapping("/users/by-name")
    public String users(Model model, @RequestParam("name") String value){
        model.addAttribute("users",userService.usersByNameOrPhoneNumber(value));
        return "users";
    }
    @PostMapping("/update/user")
    public String updateUser(@ModelAttribute @Valid User user, Model model){
        userService.update(user);
        model.addAttribute("users",userService.users());
        return "users";
    }
    @PostMapping("/delete/user")
    public String deleteUser(@RequestParam("id") long id, Model model){
        userService.delete(id);
        model.addAttribute("users",userService.users());
        return "users";
    }
    @PostMapping("/service/create")
    public void createService(@ModelAttribute @Valid Service service, HttpServletResponse response) throws IOException{
        service.setName(Character.toUpperCase(service.getName().charAt(0))+service.getName().substring(1).toLowerCase());
        service.setType(service.getType().toLowerCase());
        service.setCurrency(service.getCurrency().toLowerCase());
        serviceBot.save(service);
        response.sendRedirect("/services");
    }
    @GetMapping("/service/create")
    public String createService(Model model){
        return "service_create";
    }
    @PostMapping("/update/service")
    public void updateService(@ModelAttribute @Valid Service service, HttpServletResponse response) throws IOException{
        service.setName(Character.toUpperCase(service.getName().charAt(0))+service.getName().substring(1).toLowerCase());
        service.setType(service.getType().toLowerCase());
        service.setCurrency(service.getCurrency().toLowerCase());
        serviceBot.update(service);
        response.sendRedirect("/services");
        
    }
    @PostMapping("/delete/service")
    public void deleteService(@RequestParam("id") long id, HttpServletResponse response) throws IOException{
        serviceBot.delete(id);
        response.sendRedirect("/services");
    }
    @GetMapping("/services")
    public String services(Model model){
        model.addAttribute("services",serviceBot.services());
        return "services";
    }
    @GetMapping("/services/by-name")
    public String services(Model model, @RequestParam("name") String name){
        model.addAttribute("services",serviceBot.servicesByName(name));
        return "services";
    }
    @GetMapping("/phone/{phone}")
    public void phone(@PathVariable("phone") String phone, HttpServletResponse response) throws Exception{
        utilsDao.setValueByKey(new Utils("phone", phone));
        response.sendRedirect("/");
    }
    @ResponseBody
    @GetMapping(value = "/image/{name}",produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> image(@PathVariable("name") String name){
        return ResponseEntity.ok(serviceBot.getFile(name));
    }
    @ResponseBody
    @GetMapping(value = "/video/{name}",produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> video(@PathVariable("name") String name) throws Exception{
        /*Resource resource=serviceBot.getVideo(name);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename = \"" +
                        resource.getFilename() + "\"")
                .body(resource);*/
        return ResponseEntity.ok(serviceBot.getFile(name));
    }
    @PostMapping(value = "/save/file",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String saveFile(@RequestParam("file") MultipartFile file){
        return serviceBot.saveFile(file);
    }
    @PostMapping(value = "/update/image/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateImage(HttpServletResponse response,@PathVariable("id") long id, @RequestParam("file")MultipartFile file)
    throws Exception{
        if (file!=null && !file.isEmpty()) {
            String fileName = file.getOriginalFilename();
            assert fileName != null;
            if (!fileName.endsWith(".jpg") && !fileName.endsWith(".png")) {
                throw new BadRequestException("Faqat jpg va png formatidagi rasmlarga ruxsat bor");
            }
        }
        serviceBot.saveFile(file,"image",id);
        response.sendRedirect("/services");
    }
    @PostMapping(value = "/update/video/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateVideo(@PathVariable("id") long id, HttpServletResponse response, @RequestParam("file")MultipartFile file)
    throws Exception{
        if (file!=null && !file.isEmpty()) {
            if (!file.getOriginalFilename().endsWith(".mp4")) {
                throw new BadRequestException("Faqat mp4 formatidagi videolarga ruxsat bor");
            }
        }
        serviceBot.saveFile(file, "video",id);
        response.sendRedirect("/services");
    }
    @PostMapping("/delete/file/{id}")
    public void deleteFile(@RequestParam("name")String name,@PathVariable("id")long id, HttpServletResponse response)throws Exception{
        serviceBot.deleteFile(name);
        serviceBot.deleteFileFromService(name, id);
        response.sendRedirect("/services");
    }
    @GetMapping("/history/create")
    public String historyCreate(Model model){
        model.addAttribute("now", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")));
        return "history_create";
    }
    @PostMapping("/history/create")
    public void historyCreate(@Valid @ModelAttribute History history,HttpServletResponse response)throws Exception{
        if(history.getDateTime()==null){
            history.setDateTime(LocalDateTime.now());
        }
        historyService.save(history);
        response.sendRedirect("/histories");
    }
    @GetMapping("/histories")
    public String histories(@RequestParam(required = false,name = "date")LocalDate date, Model model, HttpServletRequest request){
        List<History> histories = historyService.historiesByDate(date);
        model.addAttribute("histories",histories);
        return "histories";
    }
}
