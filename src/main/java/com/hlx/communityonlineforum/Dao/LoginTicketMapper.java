package com.hlx.communityonlineforum.Dao;

import com.hlx.communityonlineforum.Entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
//@Deprecated 表示此方法已废弃、暂时可用，
// 但以后此类或方法都不会再更新、后期可能会删除，建议后来人不要调用此方法。
public interface LoginTicketMapper {
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status=#{status} where ticket=#{ticket} ",
            "<if test=\"ticket!=null\"> ",
            "and 1=1 ",
            "</if>",
            "</script>"
    })
    int updateStatus(String ticket, int status);
}
