package site.koisecret.auth.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import site.koisecret.auth.entity.Collection;

import java.util.List;

/**
 * @author by chengsecret
 * @date 2023/4/3.
 */
@Repository
public interface CollectionMapper {

    boolean addCollection(Collection collection);

    @Select("select id, uid, pid from collection where uid=#{uid} and pid=#{pid}")
    Collection findCollection(@Param("uid") int uid, @Param("pid") String pid);

    @Delete("delete from collection where  uid=#{uid} and pid=#{pid} ")
    boolean delCollection(@Param("uid") int uid, @Param("pid") String pid);

    @Select("select pid from collection where uid=#{uid}")
    List<String> findPidsByUid(int uid);

}
