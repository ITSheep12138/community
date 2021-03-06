package com.community.bean;
import java.util.Date;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

  private int id;
  private int fromId;
  private int toId;
  private String conversationId;
  private String content;
  private int status;
  private Date createTime;

}
