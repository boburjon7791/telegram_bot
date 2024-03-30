package bot.telegram.sahih_akamiz_uchun.repositories;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import bot.telegram.sahih_akamiz_uchun.entities.Utils;
import bot.telegram.sahih_akamiz_uchun.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UtilsDao {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    public Utils getValueByKey(String key){
        String sql="""
                select *
                from utils u
                where  u.key=:key
                """;
        Map<String, String> params=Map.of("key",key);
        return Optional.ofNullable(namedParameterJdbcTemplate.queryForObject(sql,params,(rs, rowNum) ->  new Utils(rs.getString("key"),rs.getString("value"))))
               .orElseThrow(()->new NotFoundException("Topilmadi"));
    }
    public void setValueByKey(Utils utils){
        String sql="""
                update utils set value=:value where key=:key
                """;
        namedParameterJdbcTemplate.update(sql, Map.of("key",utils.key,"value",utils.value));
    }
    public List<Utils> utils(){
        String sql="""
            select * from utils
            """;
            Map<String, String> params=new ConcurrentHashMap<>();
            List<Utils> utilsList=namedParameterJdbcTemplate.queryForList(sql, params, Utils.class);
            if (utilsList.isEmpty()) {
                throw new NotFoundException("Not found");
            }
    return utilsList;
    }
    public List<Utils> saveUtil(Utils utils){
        String sql="""
                insert into utils(key, value)
                values(:key, :value)
                """;
        Map<String, String> params=Map.of("key",utils.key,"value",utils.value);
        namedParameterJdbcTemplate.update(sql, params);
        return utils();
    }
}
