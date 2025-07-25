package bot.telegram.sahih_akamiz_uchun.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import bot.telegram.sahih_akamiz_uchun.entities.Service;

@Repository
public interface ServiceDao extends JpaRepository<Service, Long>{
    @Query(value = "from Service s where upper(s.name) like upper(concat('%',?1,'%'))")
    List<Service> findAllByNameContains(String name, Sort sort);

    boolean existsByName(String name);
    void deleteByName(String name);

    @Query(value="select exists(from Service s where s.name=?1 and s.id<>?2)")
    boolean existsByNameAndIdNotMatches(String name, Long id);

    @Query(nativeQuery = true, value = """
            select case when :fileName = s.image then s.image
                        when :fileName = s.video then s.video
                        else null end
            from Service s where s.id=:id
            """)
    Optional<String> findFileNameByIdAndFileName(Long id, String fileName);
}
