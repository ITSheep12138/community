package com.community.bean;
import java.util.Date;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

  private int id;
  private String username;
  private String password;
  private String salt;
  private String email;
  private int type;
  private int status;
  private String activationCode;
  private String headerUrl;
  private Date createTime;

}
