package site.koisecret.auth.mapper;

import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;
import site.koisecret.auth.entity.User;

@Repository
public interface UserMapper {

    boolean addUser(User user);

    @Select({"select * from user where phone = #{phone}"})
    User findUserByPhone(String phone);

    @Select("select * from user where id = #{id}")
    User findUserById(int id);

    @Update("update user set name = #{name},province = #{province}, city = #{city}, " +
            "industry = #{industry}, profession = #{profession}, age = #{age} " +
            "where id = #{id}")
    Boolean updateUser(User user);

}