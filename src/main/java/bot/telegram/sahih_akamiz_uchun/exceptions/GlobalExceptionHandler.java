package bot.telegram.sahih_akamiz_uchun.exceptions;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ModelAndView handler(BadRequestException e){
        ModelAndView modelAndView=new ModelAndView("errors/bad_request");
        modelAndView.addObject("val", e.getMessage());
        modelAndView.addObject("code",400);
        return modelAndView;
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handler(AccessDeniedException e){
        ModelAndView modelAndView=new ModelAndView("errors/forbidden");
        modelAndView.addObject("val",e.getMessage());
        modelAndView.addObject("code",403);
        return modelAndView;
    }
    @ExceptionHandler(NotFoundException.class)
    public ModelAndView handler(NotFoundException e){
        ModelAndView modelAndView=new ModelAndView("errors/not_found");
        modelAndView.addObject("val", e.getMessage());
        modelAndView.addObject("code",404);
        if (e.getMessage().equals("Savdolar tarixi mavjud emas")) {
            modelAndView.addObject("n",1);
            modelAndView.addObject("date", LocalDate.now());
        }
        return modelAndView;
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handler(NoResourceFoundException e){
        ModelAndView modelAndView=new ModelAndView("errors/not_found");
        modelAndView.addObject("val", "Ushbu resurs topilmadi");
        modelAndView.addObject("code",404);
        return modelAndView;
    }
    @ExceptionHandler(Exception.class)
    public ModelAndView handler(Exception e){
        e.printStackTrace();
        ModelAndView modelAndView=new ModelAndView("errors/server_error");
        modelAndView.addObject("val", e.getMessage());
        modelAndView.addObject("code",500);
        return modelAndView;
    }
}
