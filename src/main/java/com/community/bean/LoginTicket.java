package com.community.bean;
import java.util.Date;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginTicket {

  private int id;
  private int userId;
  private String ticket;
  private int status;
  private Date expired;

}
