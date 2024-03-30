package bot.telegram.sahih_akamiz_uchun.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BadRequestException extends RuntimeException{
    private String message;
}
