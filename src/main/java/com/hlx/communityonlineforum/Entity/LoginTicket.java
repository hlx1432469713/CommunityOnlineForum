package com.hlx.communityonlineforum.Entity;

import lombok.Data;

import java.util.Date;

@Data
public class LoginTicket {

    private int id;

    private int userId;

    private String ticket;

    private int status;

    private Date expired;

    @Override
    public String toString() {
        return "LoginTicket{" +
                "id=" + id +
                ", userId=" + userId +
                ", ticket='" + ticket + '\'' +
                ", status=" + status +
                ", expired=" + expired +
                '}';
    }
}
